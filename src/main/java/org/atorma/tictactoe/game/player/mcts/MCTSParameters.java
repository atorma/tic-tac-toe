package org.atorma.tictactoe.game.player.mcts;

/**
 * Parameters of the MCTS algorithm.
 * <p/>
 * Note that disabling all forms of pruning allows utilizing earlier simulations in repeated
 * games, but will very easily cause memory to run out.
 */
public class MCTSParameters {

    enum SimulationStrategy {UNIFORM_RANDOM, RANDOM_ADJACENT, NAIVE}


    /** How to simulate games */
    public SimulationStrategy simulationStrategy = SimulationStrategy.NAIVE;

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
     * Constrains the search to specified "radius" (cell distance) around previous moves.
     * Value 1 means searching next possible moves next to previous moves only.
     * Integer.MAX_VALUE to allow searching the entire board.
     */
    public int searchRadius = 1;

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

    /**
     * After each move, prune descendants of the current node in the game tree greater
     * than this level counting from the current node. This allows controlling how much
     * of simulated games at the potential next states to keep for the next turn.
     * Value 2 keeps the next two turns, so that after the opponent's next move,
     * MCTS still has better data to choose which moves to concentrate simulations on.
     * Value 0 discards simulations of future states.
     *
     * Keeping simulations of next possible states increases memory consumption.
     * However pruning past and illegal moves by using e.g. {@link #pruneParent}
     * mitigates the issue.
     *
     * This value must be non-negative. Use Integer.MAX_VALUE for no pruning.
     *
     * @see #pruneParent
     * @see #pruneSiblings
     */
    public int pruneDescendantLevelsGreaterThan = Integer.MAX_VALUE;
}

