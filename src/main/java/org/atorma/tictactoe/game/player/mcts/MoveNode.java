package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.*;

import java.util.*;

/**
 * Node in Monte Carlo Tree Search.
 * <p/>
 * Stores
 * <ul>
 * <li>a move and the game state it lead to</li>
 * <li>expected total reward each player will receive
 * if this move is chosen (based on simulations done by other components)</li>
 * </ul>
 */
public class MoveNode {

    private GameState state;
    private List<Cell> unexpandedMoves;
    private Cell cell;
    private MoveNode parent;
    private List<MoveNode> children = new ArrayList<>();
    private Map<Piece, Integer> wins = new EnumMap<>(Piece.class);
    private Map<Piece, Double> rewardSums = new EnumMap<>(Piece.class);
    private int numPlays = 0;

    private MoveNode root;
    private RewardScheme rewardScheme; // Non-null for root only, we only need one instance

    /**
     * Creates the root node of a game tree.
     *
     * @param gameState
     *  starting state
     * @param cell
     *  the move that lead to the starting state, or null if no move (empty board)
     * @param rewardScheme
     *  the reward scheme to use for scoring moves
     */
    public MoveNode(GameState gameState, Cell cell, RewardScheme rewardScheme) {
        this();
        this.root = this;
        this.state = gameState.getCopy();
        this.cell = cell;
        this.unexpandedMoves = new ArrayList<>(state.getAllowedMoves()); // sorted by rows then columns
        this.rewardScheme = rewardScheme;
    }

    private MoveNode() {
        this.wins.put(Piece.CROSS, 0);
        this.wins.put(Piece.ROUND, 0);
        this.rewardSums.put(Piece.CROSS, (double) 0);
        this.rewardSums.put(Piece.ROUND, (double) 0);
    }

    private MoveNode(MoveNode parent, Cell cell) {
        this();
        this.parent = parent;
        this.root = parent.root;
        this.cell = cell;
        this.state = parent.state.next(cell);
        this.unexpandedMoves = new ArrayList<>(state.getAllowedMoves()); // sorted by rows then columns
    }

    /**
     * Returns the move that put the game in the current state.
     *
     * @see #getGameState()
     * @see #getParent()
     */
    public Cell getMove() {
        return cell;
    }

    /**
     * Returns the state of the game after this move.
     *
     * @see #getMove()
     */
    public GameState getGameState() {
        return this.state;
    }

    public MoveNode getRoot() {
        return root;
    }

    /** Returns the previous state/move. */
    public MoveNode getParent() {
        return parent;
    }

    /**
     * Returns the currently expanded next moves.
     *
     * @see #expandAll()
     * @see #expandRandom()
     * @see #expandRandomIn(Collection)
     */
    public List<MoveNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean isFullyExpanded() {
        return unexpandedMoves.isEmpty();
    }

    public boolean isFullyExpandedIn(Rectangle... rectangles) {
        return isFullyExpandedIn(Arrays.asList(rectangles));
    }

    public boolean isFullyExpandedIn(Collection<Rectangle> rectangles) {
        if (isFullyExpanded()) {
            return true;
        }

        for (Rectangle rectangle : rectangles) {
            for (int i = rectangle.getUpperLeftCorner().getRow(); i <= rectangle.getLowerRightCorner().getRow(); i++) {
                int first = indexOfEqualOrNext(new Cell(i, rectangle.getUpperLeftCorner().getColumn()), unexpandedMoves);
                int last = indexOfEqualOrPrev(new Cell(i, rectangle.getLowerRightCorner().getColumn()), unexpandedMoves);
                if (first <= last) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Computes the next possible moves from this move and sets them as children of this node.
     * This can be a huge memory hog.
     */
    public void expandAll() {
        for (int i = 0; i < unexpandedMoves.size(); i++) {
            expand(i);
        }
    }

    /**
     * Expands this node by one random move and adds it as a child node.
     *
     * @return
     *  child node representing the added move, or null if no more moves to expand
     */
    public MoveNode expandRandom() {
        if (unexpandedMoves.isEmpty()) {
            return null;
        }
        return expand(Utils.random.nextInt(unexpandedMoves.size()));
    }

    /**
     * @see #expandRandomIn(Collection)
     */
    public MoveNode expandRandomIn(Rectangle... rectangles) {
        return expandRandomIn(Arrays.asList(rectangles));
    }

    /**
     * Expands this node by one random move within the given rectangles
     * and adds it as a child node.
     *
     * @return
     *  child node representing the added move,
     *  or null if no more moves to expand in the rectangles
     */
    public MoveNode expandRandomIn(Collection<Rectangle> rectangles) {
        if (isFullyExpanded()) {
            return null;
        }

        List<Integer> candidates = new ArrayList<>();
        for (Rectangle rectangle : rectangles) {
            for (int i = rectangle.getUpperLeftCorner().getRow(); i <= rectangle.getLowerRightCorner().getRow(); i++) {
                int first = indexOfEqualOrNext(new Cell(i, rectangle.getUpperLeftCorner().getColumn()), unexpandedMoves);
                int last = indexOfEqualOrPrev(new Cell(i, rectangle.getLowerRightCorner().getColumn()), unexpandedMoves);
                for (int nodeIndex = first; nodeIndex <= last; nodeIndex++) {
                    candidates.add(nodeIndex);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return expand(Utils.pickRandom(candidates));
    }

    /**
     * Finds the index of position equal to the key or the next one if equal key does not exist.
     *
     * @param key
     *  The key
     * @param positionList
     *  The list to search. Must be sorted first by rows, then by columns.
     * @return
     *  The index, or positionList.size() if no equal nor next position in positionList
     */
    private int indexOfEqualOrNext(Cell key, List<Cell> positionList) {
        int result = Collections.binarySearch(positionList, key, new CellRowOrderComparator());
        if (result >= 0) {
            return result; // equal
        } else { // result = -(insertion point) - 1, where insertion point is index of first element _greater_ than key
            int ins = -result - 1;
            return ins; // next actual element, or positionList.size()
        }
    }

    /**
     * Finds the index of position equal to the key or the previous one if equal key does not exist.
     *
     * @param key
     *  The key
     * @param positionList
     *  The list to search. Must be sorted first by rows, then by columns.
     * @return
     *  The index, or -1 if no equal nor previous position in positionList
     */
    private int indexOfEqualOrPrev(Cell key, List<Cell> positionList) {
        int result = Collections.binarySearch(positionList, key, new CellRowOrderComparator());
        if (result >= 0) {
            return result; // equal
        } else { // result = -(insertion point) - 1, where insertion point is index of first element _greater_ than key
            int ins = -result - 1;
            return ins - 1; // previous actual element, or -1
        }
    }

    private MoveNode expand(int unexpandedIndex) {
        Cell position = unexpandedMoves.get(unexpandedIndex);
        unexpandedMoves.remove(unexpandedIndex);
        MoveNode child = new MoveNode(this, position);
        this.children.add(child);
        return child;
    }


    public MoveNode findMoveTo(Cell nextPosition) {
        // Already expanded?
        for (MoveNode node : children) {
            if (node.getMove().equals(nextPosition)) {
                return node;
            }
        }
        // Must be unexpanded then, or else not an allowed move
        for (int i = 0; i < unexpandedMoves.size(); i++) {
            Cell move = unexpandedMoves.get(i);
            if (move.equals(nextPosition)) {
                return expand(i);
            }
        }
        return null;
    }

    /**
     * Selects next moves that have the highest expected reward for the player
     * whose turn it is at this node.
     *
     * Only considers the current children of this node, not unexpanded moves.
     *
     * Use this after searching using {@link #getBestExploratoryMoves()} and running
     * simulations from them.
     *
     * @return
     *  the best moves candidates to play (all have equal expected reward)
     * @throws IllegalStateException
     *  if this node has no children (game at end or no children expanded)
     */
    public List<MoveNode> getBestMoves() throws IllegalStateException {
        if (children.isEmpty()) {
            throw new IllegalStateException("Node has no children");
        }

        return Utils.max(children, element -> element.getExpectedReward(state.getTurn()));
    }

    /**
     * Selects the best moves to explore for the player whose turn it
     * is at this node.
     *
     * Only considers the current children of this node, not unexpanded moves.
     *
     * Ties are broken randomly.
     *
     * @return
     *  the best move candidates to explore (all have equal exploration score)
     * @throws IllegalStateException
     *  if this node has no children (game at end or no children expanded)
     */
    public List<MoveNode> getBestExploratoryMoves() throws IllegalStateException {
        if (children.isEmpty()) {
            throw new IllegalStateException("Node has no children");
        }

        return Utils.max(children, element -> element.getExplorationScore(state.getTurn()));
    }

    public double getExplorationScore(Piece player) {
        return this.getExpectedReward(player) + root.rewardScheme.getExplorationBonus(player, this);
    }

    public double getExpectedReward(Piece player) {
        double n = this.getNumPlays();
        double R = this.rewardSums.get(player);
        double r = n > 0 ? R/n : R;
        return r;
    }

    public int getWins(Piece piece) {
        return wins.get(piece);
    }

    /** Returns the number of simulated game results starting from this node. */
    public int getNumPlays() {
        return numPlays;
    }

    /**
     * Records the result of a simulated game that started from this node
     * and ended at state endState.
     * <p/>
     * This updates the expected rewards of all the previous moves.
     */
    public void propagateSimulatedResult(GameState endState) {
        Piece winner = endState.getWinner();

        Map<Piece, Double> rewards = new EnumMap<>(Piece.class);
        rewards.put(Piece.CROSS, root.rewardScheme.getReward(Piece.CROSS, endState));
        rewards.put(Piece.ROUND, root.rewardScheme.getReward(Piece.ROUND, endState));

        MoveNode current = this;
        while (current != null) {
            if (winner != null) {
                int updatedWins = current.wins.get(winner) + 1;
                current.wins.put(winner, updatedWins);
            }

            current.rewardSums.put(Piece.CROSS, current.rewardSums.get(Piece.CROSS) + rewards.get(Piece.CROSS));
            current.rewardSums.put(Piece.ROUND, current.rewardSums.get(Piece.ROUND) + rewards.get(Piece.ROUND));

            current.numPlays = current.numPlays + 1;

            current = current.parent;
        }
    }

    public List<MoveNode> getPathToRoot() {
        ArrayList<MoveNode> pathToRoot = new ArrayList<>();
        MoveNode current = this;
        while (current != null) {
            pathToRoot.add(current);
            current = current.parent;
        }
        return pathToRoot;
    }

    /**
     * Makes this node the root node of the tree,
     * essentially pruning all the parent and sister
     * nodes of this node.
     */
    public void makeRoot() {
        this.rewardScheme = root.rewardScheme;
        this.root = this;
        this.parent = null;
    }

    /**
     * Prunes this game tree so that there is only one branch from the root down to this node.
     * In other words, removes all alternative moves that could have been from the root state
     * until this node's state. You can replicate the past game by moving along the child node
     * path from the root to this node.
     */
    public void pruneOtherBranchesOnPathToRoot() {
        MoveNode child = this;
        MoveNode parent = this.parent;
        while (parent != null) {
            parent.children.clear();
            parent.children.add(child);
            child = parent;
            parent = parent.parent;
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MoveNode{");
        if (cell != null) {
            sb.append("Move (").append(cell.getRow()).append(", ").append(cell.getColumn()).append(")");
        } else {
            sb.append("root");
        }
        sb.append(", ");
        sb.append(Piece.CROSS).append(" E[r]: ").append(Utils.round(getExpectedReward(Piece.CROSS), 3));
        sb.append(", ");
        sb.append(Piece.ROUND).append(" E[r]: ").append(Utils.round(getExpectedReward(Piece.ROUND), 3));
        sb.append(", ");
        sb.append(Piece.CROSS).append(" wins: ").append(getWins(Piece.CROSS));
        sb.append(", ");
        sb.append(Piece.ROUND).append(" wins: ").append(getWins(Piece.ROUND));
        sb.append(", ");
        sb.append("Total: ").append(getNumPlays());
        sb.append("}");
        return sb.toString();
    }

    public String printStatsFor(Piece player) {
        StringBuilder sb = new StringBuilder();
        sb.append("MoveNode{");
        if (cell != null) {
            sb.append("Move (").append(cell.getRow()).append(", ").append(cell.getColumn()).append(")");
        } else {
            sb.append("root");
        }
        sb.append(", ");
        sb.append(player).append(" E[r]: ").append(Utils.round(getExpectedReward(player), 3));
        sb.append(", ");
        sb.append(player).append(" wins: ").append(getWins(player)).append(" out of ").append(getNumPlays());
        sb.append("}");
        return sb.toString();
    }


}
