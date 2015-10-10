package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UCTBonusTests {

    private GameState someState;

    @Before
    public void setUp() {
        Piece[][] board = new Piece[3][3];
        board[1][1] = Piece.CROSS;
        someState = new GameState(3, board, Piece.ROUND);
    }

    @Test
    public void uct_bonus_for_root_node_is_infinite() {
        MoveNode root = new MoveNode(someState, null, new WinLossDrawScheme());
        root.propagateSimulatedResult(someState);
        root.propagateSimulatedResult(someState);
        assertEquals(Double.POSITIVE_INFINITY, UCT.getUCTBonus(root, 1), 0);
    }

    @Test
    public void uct_bonus_for_node_that_has_zero_plays_is_infinite() {
        MoveNode root = new MoveNode(someState, null, new WinLossDrawScheme());
        root.expandAll();
        MoveNode child = root.getChildren().get(0);
        assertEquals(Double.POSITIVE_INFINITY, UCT.getUCTBonus(child, 1), 0);
    }

    @Test
    public void compute_uct_bonus_for_played_node() {
        MoveNode root = new MoveNode(someState, null, new WinLossDrawScheme());
        root.expandAll();
        MoveNode child1 = root.getChildren().get(0);
        child1.propagateSimulatedResult(someState);
        child1.propagateSimulatedResult(someState);
        MoveNode child2 = root.getChildren().get(1);
        child2.propagateSimulatedResult(someState);

        assertEquals(1.0481, UCT.getUCTBonus(child1, 1), 0.0001);
        assertEquals(1.4823, UCT.getUCTBonus(child2, 1), 0.0001);
    }
}
