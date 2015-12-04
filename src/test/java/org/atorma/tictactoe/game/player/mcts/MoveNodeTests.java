package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.state.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@Category(FastTests.class)
public class MoveNodeTests {

    @Test
    public void find_move_returns_correct_move_even_if_node_not_expanded_yet() {
        GameState startingState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(startingState, null, new WinLossDrawScheme());
        assertTrue(root.getChildren().isEmpty());

        Cell position = new Cell(0, 0);
        MoveNode child = root.findMoveTo(position);
        assertTrue(position.equals(child.getMove()));
    }

    @Test
    public void when_initial_state_is_end_state_then_root_has_no_allowed_moves() {
        Piece[][] alreadyAtEndBoard = new Piece[3][3];
        alreadyAtEndBoard[0][0] = Piece.X;
        alreadyAtEndBoard[1][1] = Piece.X;
        alreadyAtEndBoard[2][2] = Piece.X;
        alreadyAtEndBoard[0][1] = Piece.O;
        alreadyAtEndBoard[0][2] = Piece.O;

        GameState alreadyAtEndState = GameState.builder().setConnectHowMany(3).setBoard(alreadyAtEndBoard).setNextPlayer(Piece.O).build();
        alreadyAtEndState.print();

        MoveNode root = new MoveNode(alreadyAtEndState, null, new WinLossDrawScheme());
        assertTrue(root.getAllowedMoves().isEmpty());
    }

    @Test
    public void when_child_move_should_represent_and_end_state_then_no_allowed_moves_in_child() {
        Piece[][] almostAtEndBoard = new Piece[3][3];
        almostAtEndBoard[1][1] = Piece.X;
        almostAtEndBoard[2][2] = Piece.X;
        almostAtEndBoard[0][1] = Piece.O;
        almostAtEndBoard[0][2] = Piece.O;

        GameState nearlyEndState = GameState.builder().setConnectHowMany(3).setBoard(almostAtEndBoard).setNextPlayer(Piece.X).build();
        nearlyEndState.print();

        MoveNode root = new MoveNode(nearlyEndState, null, new WinLossDrawScheme());
        root.expandAll();
        MoveNode endStateNode = root.findMoveTo(new Cell(0, 0));
        GameState endState = nearlyEndState.next(endStateNode.getMove());
        endState.print();
        assertTrue(endState.isAtEnd());
        assertTrue(endState.getAllowedMoves().isEmpty());
        System.out.println(endStateNode.getAllowedMoves());
        assertTrue(endStateNode.getAllowedMoves().isEmpty());
    }

    @Test
    public void expand_random_in_rectangle_returns_allowed_move_in_rectangle_and_adds_it_as_child_node() {
        Rectangle rectangle = new Rectangle(5, 5, 6, 6); // 2 x 2 rectangle

        GameState state = GameState.builder().setConnectHowMany(5).setBoard(new Piece[18][18]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(state, null, new WinLossDrawScheme());
        assertTrue(root.getChildren().isEmpty());
        assertFalse(root.isFullyExpandedIn(rectangle));

        MoveNode child1 = root.expandRandomIn(rectangle);
        assertTrue(root.getChildren().contains(child1));
        assertTrue(rectangle.contains(child1.getMove()));
        assertFalse(root.isFullyExpandedIn(rectangle));

        MoveNode child2 = root.expandRandomIn(rectangle);
        assertTrue(root.getChildren().contains(child2));
        assertTrue(rectangle.contains(child2.getMove()));
        assertFalse(root.isFullyExpandedIn(rectangle));

        MoveNode child3 = root.expandRandomIn(rectangle);
        assertTrue(root.getChildren().contains(child3));
        assertTrue(rectangle.contains(child3.getMove()));
        assertFalse(root.isFullyExpandedIn(rectangle));

        MoveNode child4 = root.expandRandomIn(rectangle);
        assertTrue(root.getChildren().contains(child4));
        assertTrue(rectangle.contains(child4.getMove()));
        assertTrue(root.isFullyExpandedIn(rectangle));

        assertNull(root.expandRandomIn(rectangle));
    }

    @Test
    public void expand_random_in_list_of_rectangles() {
        Rectangle rectangle1 = new Rectangle(5, 5, 5, 5); // 1 x 1 rectangle
        Rectangle rectangle2 = new Rectangle(6, 6, 6, 6); // 1 x 1 rectangle

        GameState state = GameState.builder().setConnectHowMany(5).setBoard(new Piece[18][18]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(state, null, new WinLossDrawScheme());
        assertTrue(root.getChildren().isEmpty());
        assertFalse(root.isFullyExpandedIn(rectangle1, rectangle2));

        MoveNode child1 = root.expandRandomIn(rectangle1, rectangle2);
        assertTrue(root.getChildren().contains(child1));
        assertTrue(rectangle1.contains(child1.getMove()) || rectangle2.contains(child1.getMove()));
        assertFalse(root.isFullyExpandedIn(rectangle1, rectangle2));

        MoveNode child2 = root.expandRandomIn(rectangle1, rectangle2);
        assertTrue(root.getChildren().contains(child2));
        assertTrue(rectangle1.contains(child2.getMove()) || rectangle2.contains(child2.getMove()));
        assertTrue(root.isFullyExpandedIn(rectangle1, rectangle2));

        assertNull(root.expandRandomIn(rectangle1, rectangle2));
    }

    @Test
    public void expand_random_when_area_is_entire_board() {
        Rectangle rectangle = new Rectangle(0, 0, 2, 2);

        GameState state = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(state, null, new WinLossDrawScheme());

        MoveNode expanded;
        do {
            expanded = root.expandRandomIn(rectangle);
            if (expanded != null) {
                assertTrue(rectangle.contains(expanded.getMove()));
            }
        } while (expanded != null);

        assertEquals(3*3, root.getChildren().size());
        assertTrue(root.isFullyExpandedIn(rectangle));
        assertTrue(root.isFullyExpanded());
    }

    @Test
    public void propagate_results_using_win_loss_draw_scheme() {
        GameState startingState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(startingState, null, new WinLossDrawScheme());
        root.expandAll();

        MoveNode child1 = root.getChildren().get(0);
        MoveNode child2 = root.getChildren().get(1);

        child1.expandAll();
        MoveNode grandChild1 = child1.getChildren().get(0);

        GameState crossWins = GameState.builder().setConnectHowMany(3).setBoard(new Piece[][]{
                {Piece.X, Piece.O, Piece.O},
                {Piece.O, Piece.X, Piece.O},
                {null, Piece.X, Piece.X}
        }).setNextPlayer(Piece.O).build();

        child1.propagateSimulatedResult(crossWins);

        assertEquals(1, root.getNumPlays());
        assertEquals(1, root.getWins(Piece.X));
        assertEquals(0, root.getWins(Piece.O));
        assertEquals(1, root.getExpectedReward(Piece.X), 0);
        assertEquals(-1, root.getExpectedReward(Piece.O), 0);

        assertEquals(1, child1.getNumPlays());
        assertEquals(1, child1.getWins(Piece.X));
        assertEquals(0, child1.getWins(Piece.O));
        assertEquals(1, child1.getExpectedReward(Piece.X), 0);
        assertEquals(-1, child1.getExpectedReward(Piece.O), 0);

        assertEquals(0, child2.getNumPlays());
        assertEquals(0, child2.getWins(Piece.X));
        assertEquals(0, child2.getWins(Piece.O));
        assertEquals(0, child2.getExpectedReward(Piece.O), 0);
        assertEquals(0, child2.getExpectedReward(Piece.X), 0);

        assertEquals(0, grandChild1.getNumPlays());
        assertEquals(0, grandChild1.getWins(Piece.X));
        assertEquals(0, grandChild1.getWins(Piece.O));
        assertEquals(0, grandChild1.getExpectedReward(Piece.X), 0);
        assertEquals(0, grandChild1.getExpectedReward(Piece.O), 0);


        grandChild1.propagateSimulatedResult(crossWins);

        assertEquals(2, root.getNumPlays());
        assertEquals(2, root.getWins(Piece.X));
        assertEquals(0, root.getWins(Piece.O));
        assertEquals(1, root.getExpectedReward(Piece.X), 0);
        assertEquals(-1, root.getExpectedReward(Piece.O), 0);

        assertEquals(2, child1.getNumPlays());
        assertEquals(2, child1.getWins(Piece.X));
        assertEquals(0, child1.getWins(Piece.O));
        assertEquals(1, child1.getExpectedReward(Piece.X), 0);
        assertEquals(-1, child1.getExpectedReward(Piece.O), 0);

        assertEquals(0, child2.getNumPlays());
        assertEquals(0, child2.getWins(Piece.X));
        assertEquals(0, child2.getWins(Piece.O));
        assertEquals(0, child2.getExpectedReward(Piece.X), 0);
        assertEquals(0, child2.getExpectedReward(Piece.O), 0);

        assertEquals(1, grandChild1.getNumPlays());
        assertEquals(1, grandChild1.getWins(Piece.X));
        assertEquals(0, grandChild1.getWins(Piece.O));
        assertEquals(1, grandChild1.getExpectedReward(Piece.X), 0);
        assertEquals(-1, grandChild1.getExpectedReward(Piece.O), 0);


        GameState roundWins = GameState.builder().setConnectHowMany(3).setBoard(new Piece[][]{
                {Piece.X, Piece.X, Piece.O},
                {Piece.O, Piece.O, Piece.O},
                {null, Piece.X, Piece.X}
        }).setNextPlayer(Piece.X).build();

        child2.propagateSimulatedResult(roundWins);

        assertEquals(3, root.getNumPlays());
        assertEquals(2, root.getWins(Piece.X));
        assertEquals(1, root.getWins(Piece.O));
        assertEquals(0.33333, root.getExpectedReward(Piece.X), 0.01);
        assertEquals(-0.3333, root.getExpectedReward(Piece.O), 0.01);

        assertEquals(2, child1.getNumPlays());
        assertEquals(2, child1.getWins(Piece.X));
        assertEquals(0, child1.getWins(Piece.O));
        assertEquals(1, child1.getExpectedReward(Piece.X), 0);
        assertEquals(-1, child1.getExpectedReward(Piece.O), 0);

        assertEquals(1, child2.getNumPlays());
        assertEquals(0, child2.getWins(Piece.X));
        assertEquals(1, child2.getWins(Piece.O));
        assertEquals(-1, child2.getExpectedReward(Piece.X), 0);
        assertEquals(1, child2.getExpectedReward(Piece.O), 0);

        assertEquals(1, grandChild1.getNumPlays());
        assertEquals(1, grandChild1.getWins(Piece.X));
        assertEquals(0, grandChild1.getWins(Piece.O));
        assertEquals(1, grandChild1.getExpectedReward(Piece.X), 0);
        assertEquals(-1, grandChild1.getExpectedReward(Piece.O), 0);


        GameState tie = GameState.builder().setConnectHowMany(3).setBoard(new Piece[][]{
                {Piece.X, Piece.X, Piece.O},
                {Piece.O, Piece.O, Piece.X},
                {Piece.X, Piece.O, Piece.X}
        }).setNextPlayer(Piece.O).build();

        grandChild1.propagateSimulatedResult(tie);

        assertEquals(4, root.getNumPlays());
        assertEquals(2, root.getWins(Piece.X));
        assertEquals(1, root.getWins(Piece.O));

        assertEquals(3, child1.getNumPlays());
        assertEquals(2, child1.getWins(Piece.X));
        assertEquals(0, child1.getWins(Piece.O));

        assertEquals(1, child2.getNumPlays());
        assertEquals(0, child2.getWins(Piece.X));
        assertEquals(1, child2.getWins(Piece.O));


        assertEquals(1, grandChild1.getWins(Piece.X));
        assertEquals(0, grandChild1.getWins(Piece.O));
        assertEquals(2, grandChild1.getNumPlays());
    }

    @Test
    public void getBestMove_and_getExploratoryMove_return_some_move_when_no_visits_to_next_moves() {
        GameState emptyBoardCrossStarts = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(emptyBoardCrossStarts, null, new WinLossDrawScheme());
        root.expandAll();

        MoveNode nextMove = root.getBestMoves().get(0);
        assertNotNull(nextMove);
        assertTrue(emptyBoardCrossStarts.getAllowedMoves().contains(nextMove.getMove()));

        nextMove = root.getBestExploratoryMoves().get(0);
        assertNotNull(nextMove);
        assertTrue(emptyBoardCrossStarts.getAllowedMoves().contains(nextMove.getMove()));
    }

    @Test
    public void getBestMove_returns_move_with_highest_expected_value() {
        GameState emptyBoardCrossStarts = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(emptyBoardCrossStarts, null, new WinLossDrawScheme());

        root.expandAll();
        MoveNode crossWinsMove = root.getChildren().get(0);
        MoveNode roundWinsMove = root.getChildren().get(1);

        // Assume these are results of two simulations

        GameState crossWinsState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[][]{
                {Piece.X, Piece.O, Piece.O},
                {Piece.O, Piece.X, Piece.O},
                {null, Piece.X, Piece.X}
        }).setNextPlayer(Piece.O).build();
        GameState roundWinsState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[][]{
                {Piece.X, Piece.X, Piece.O},
                {Piece.O, Piece.O, Piece.O},
                {null, Piece.X, Piece.X}
        }).setNextPlayer(Piece.X).build();

        crossWinsMove.propagateSimulatedResult(crossWinsState);
        roundWinsMove.propagateSimulatedResult(roundWinsState);

        MoveNode nextMove = root.getBestMoves().get(0);
        assertEquals(crossWinsMove, nextMove);
    }

    @Test
    public void prune_other_branches_on_path_to_root() {
        GameState startingState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(startingState, null, new WinLossDrawScheme());
        root.expandAll();
        root.getChildren().forEach(c -> {
            c.expandAll();
            c.getChildren().forEach(gc -> gc.expandAll());
        });

        MoveNode someChild = Utils.pickRandom(root.getChildren());
        MoveNode someGrandChild = Utils.pickRandom(someChild.getChildren());
        MoveNode someGrandGrandChild = Utils.pickRandom(someGrandChild.getChildren());
        someGrandGrandChild.expandAll();

        someGrandGrandChild.pruneOtherBranchesOnPathToRoot();

        assertEquals(Arrays.asList(someChild), root.getChildren());
        assertEquals(Arrays.asList(someGrandChild), someChild.getChildren());
        assertEquals(Arrays.asList(someGrandGrandChild), someGrandChild.getChildren());
        assertTrue(someGrandGrandChild.getChildren().size() > 1);
    }

    @Test
    public void make_node_the_root_node() {
        GameState startingState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(startingState, null, new WinLossDrawScheme());
        root.expandAll();
        MoveNode someChild = Utils.pickRandom(root.getChildren());

        someChild.makeRoot();
        assertNull(someChild.getParent());

        someChild.expandAll();
        assertTrue(someChild.getChildren().size() > 1);
    }

    @Test
    public void prune_descendant_levels() {
        GameState startingState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(startingState, null, new WinLossDrawScheme());
        root.expandAll();
        root.getChildren().forEach(child -> {
            child.expandAll();
            child.getChildren().forEach(grandChild -> grandChild.expandAll());
        });
        int rootNumChildren = root.getChildren().size();

        assertTrue(root.isFullyExpanded());
        MoveNode someChild = Utils.pickRandom(root.getChildren());
        assertTrue(someChild.isFullyExpanded());

        // This should do nothing because we have only two levels expanded
        root.pruneDescendantLevelsGreaterThan(100);
        assertTrue(root.isFullyExpanded());
        someChild = Utils.pickRandom(root.getChildren());
        assertTrue(someChild.isFullyExpanded());

        // This should leave only the child nodes but prune grandchildren
        root.pruneDescendantLevelsGreaterThan(1);
        assertTrue(root.isFullyExpanded());
        someChild = Utils.pickRandom(root.getChildren());
        assertFalse(someChild.isFullyExpanded());
        assertTrue(someChild.getChildren().isEmpty());

        // This should prune all descendants
        root.pruneDescendantLevelsGreaterThan(0);
        assertFalse(root.isFullyExpanded());
        assertTrue(root.getChildren().isEmpty());

        // Expand again
        root.expandAll();
        assertEquals(rootNumChildren, root.getChildren().size());
        assertTrue(root.isFullyExpanded());
    }
}
