package org.atorma.tictactoe.game.player.mcts;

/**
 * Parameters of the MCTS algorithm.
 * <p/>
 * Note that disabling all forms of pruning allows utilizing earlier simulations in repeated
 * games, but will very easily cause memory to run out.
 */
public class MCTSParameters {

    enum SimulationStrategy {UNIFORM_RANDOM, NAIVE}

    /** Size of board: rows */
    public int boardRowsNum = 18;

    /** Size of board: columns */
    public int boardColsNum = 18;

    /** How many pieces must be connected to win */
    public int connectHowMany = 5;



    /** How to simulate games */
    public SimulationStrategy simulationStrategy = SimulationStrategy.UNIFORM_RANDOM;

    /** Maximum number of planning rollouts (iterations) to perform */
    public int maxRolloutsNum = Integer.MAX_VALUE;

    /** Maximum number of game turns to simulate */
    public int maxSimulatedGameTurns = Integer.MAX_VALUE;

    /** How long in wall clock milliseconds the player is allowed to plan the next move */
    public long maxThinkTimeMillis = 5000;

    /**
     * true: maxThinkTimeMillis must be taken into account in game simulations
     *
     * false: a simulation is always allowed to run until the game ends
     * or maxSimulatedGameTurns is reached
     */
    public boolean maxThinkTimeIncludesSimulation = false;

    /**
     * The radius (cell distance) of the areas around the player's and the opponent's
     * previous moves to constrain the search to.
     * Integer.MAX_VALUE to allow searching the entire board.
     */
    public int searchRadius = 1;

    /**
     * Around how many past moves, including player's and opponent's moves, to search.
     * E.g. value 2 means searching around the opponent's and the player's previous moves.
     */
    public int pastMovesSearchNumber = Integer.MAX_VALUE;

    /**
     * A scheme defining the players' reward when the game ends at some state, as well as
     * move exploration scores when searching moves.
     */
    public RewardScheme rewardScheme = new WinLossDrawScheme();

    /**
     * true: Prune the siblings of the chosen move in the game tree after each move. This
     * keeps a trail of played moves from the starting state to the current state, up
     * until the end of the game. This is the weakest form of pruning available.
     *
     * false: No sibling node pruning.
     *
     * @see #pruneParent
     */
    public boolean pruneSiblings = true;

    /**
     * true: Prune the parent of the chosen move node in the game tree after each move.
     * No previous moves are kept, but simulation results of potential next moves are.
     *
     * false: No parent pruning.
     *
     * @see #pruneSiblings
     */
    public boolean pruneParent = true;
}

