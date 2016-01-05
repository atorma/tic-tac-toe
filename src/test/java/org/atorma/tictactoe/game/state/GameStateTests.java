package org.atorma.tictactoe.game.state;

import org.atorma.tictactoe.FastTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.*;

import static org.junit.Assert.*;

@Category(FastTests.class)
public class GameStateTests {

    @Test
    public void get_allowed_moves_when_no_winner() {
        Piece[][] board = {
                {null, Piece.X, Piece.O},
                {null, Piece.X, null},
                {null, Piece.O, null}
        };

        GameState gameState = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        List<Cell> allowedMoves = gameState.getAllowedMoves();

        assertEquals(5, allowedMoves.size());
        for (Cell position : allowedMoves) {
            boolean isValid = false;
            if (position.getRow() == 0 && position.getColumn() == 0) {
                isValid = true;
            } else if (position.getRow() == 1 && (position.getColumn() == 0 || position.getColumn() == 2)) {
                isValid = true;
            } else if (position.getRow() == 2 && (position.getColumn() == 0 || position.getColumn() == 2)) {
                isValid = true;
            }
            assertTrue(isValid);
        }
    }

    @Test
    public void no_allowed_moves_when_winner_found() {
        Piece[][] board = {
                {Piece.O, Piece.X, Piece.O},
                {null,    Piece.X, null},
                {null,    Piece.X, null}
        };

        GameState endState = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.O).build();
        assertEquals(Piece.X, endState.getWinner());
        assertTrue(endState.isAtEnd());
        assertEquals(0, endState.getAllowedMoves().size());

        board[0][1] = null;
        GameState nearEndState = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(null, nearEndState.getWinner());
        assertFalse(nearEndState.isAtEnd());
        assertEquals(5, nearEndState.getAllowedMoves().size());

        endState = nearEndState.next(new Cell(0, 1));
        assertEquals(Piece.X, endState.getWinner());
        assertTrue(endState.isAtEnd());
        assertEquals(0, endState.getAllowedMoves().size());
    }

    @Test
    public void exception_if_trying_to_make_a_move_that_is_not_allowed() {
        Piece[][] board = {
                {Piece.O, null,    Piece.O},
                {null,    Piece.X, null},
                {null,    Piece.X, null}
        };
        GameState state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();

        // Null not allowed
        assertFalse(state.isAllowed(null));
        assertIllegalArgumentExceptionWithMove(null, state);

        // Out of bounds not allowed
        assertFalse(state.isAllowed(new Cell(3, 0)));
        assertIllegalArgumentExceptionWithMove(new Cell(3, 0), state);

        // Occupied cells not allowed
        for (Cell c : Arrays.asList(new Cell(0, 0), new Cell(0, 2), new Cell(1, 1), new Cell(2, 1))) {
            assertFalse(state.isAllowed(c));
            assertIllegalArgumentExceptionWithMove(c, state);
        }

        // No cell allowed when game is at end
        state = state.next(new Cell(0, 1));
        assertTrue(state.isAtEnd());
        for (int i = 0; i < state.getBoardRows(); i++) {
            for (int j = 0; j < state.getBoardCols(); j++) {
                Cell c = new Cell(i, j);
                assertFalse(state.isAllowed(c));
                assertIllegalArgumentExceptionWithMove(c, state);
            }
        }
    }

    private void assertIllegalArgumentExceptionWithMove(Cell move, GameState state) {
        try {
            state.next(move);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void get_state_after_move() {
        Piece[][] board = {
                {null, Piece.X, Piece.O},
                {null, Piece.X, null},
                {null, Piece.O, null}
        };

        GameState gameState1 = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(Piece.X, gameState1.getNextPlayer());
        assertEquals(4, gameState1.getNumPieces());
        assertEquals(5, gameState1.getAllowedMoves().size());

        Cell move = new Cell(0, 0);
        assertTrue(gameState1.isAllowed(move));
        Piece[][] expectedBoard = {
                {Piece.X, Piece.X, Piece.O},
                {null,        Piece.X, null},
                {null,        Piece.O, null}
        };

        GameState gameState2 = gameState1.next(move);
        assertFalse(gameState2.isAllowed(move));
        assertEqualBoards(expectedBoard, getBoardAsArray(gameState2));
        assertEquals(Piece.O, gameState2.getNextPlayer());
        assertEquals(5, gameState2.getNumPieces());
        assertEquals(4, gameState2.getAllowedMoves().size());
    }

    private Piece[][] getBoardAsArray(GameState state) {
        Piece[][] array = new Piece[state.getBoardRows()][state.getBoardCols()];
        for (int i = 0; i < state.getBoardRows(); i++) {
            for (int j = 0; j < state.getBoardCols(); j++) {
                array[i][j] = state.getPiece(i, j);
            }
        }
        return array;
    }

    private static void assertEqualBoards(Piece[][] expected, Piece[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    @Test
    public void check_for_winner_and_tie() {
        GameState state;

        Piece[][] noWinnerNoTie = {
                {null, Piece.X, Piece.O},
                {null, Piece.X, null},
                {null, Piece.O, null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(noWinnerNoTie).setNextPlayer(Piece.X).build();
        assertEquals(null, state.getWinner());
        assertEquals(false, state.isTie());
        assertEquals(2, state.getLongestSequence(Piece.X).getLength());
        assertEquals(1, state.getLongestSequence(Piece.O).getLength());

        Piece[][] roundHorizontal = {
                {null,        Piece.X, Piece.X},
                {Piece.O, Piece.O, Piece.O},
                {null,        Piece.X, Piece.X}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(roundHorizontal).setNextPlayer(Piece.O).build();
        assertEquals(Piece.O, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] crossVertical = {
                {Piece.O, Piece.O, Piece.X},
                {null,        Piece.X, Piece.X},
                {Piece.O, Piece.O, Piece.X}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(crossVertical).setNextPlayer(Piece.O).build();
        assertEquals(Piece.X, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] roundDiagTopLeft = {
                {Piece.O, Piece.O, Piece.X},
                {Piece.X, Piece.O, Piece.X},
                {null,        Piece.X, Piece.O}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(roundDiagTopLeft).setNextPlayer(Piece.O).build();
        assertEquals(Piece.O, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] crossDiagTopRight = {
                {Piece.O, Piece.O, Piece.X},
                {Piece.O, Piece.X, Piece.X},
                {Piece.X, Piece.O, Piece.O}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(crossDiagTopRight).setNextPlayer(Piece.O).build();
        assertEquals(Piece.X, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] tie = {
                {Piece.X, Piece.O, Piece.X},
                {Piece.O, Piece.O, Piece.X},
                {Piece.X, Piece.X, Piece.O}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(tie).setNextPlayer(Piece.O).build();
        assertEquals(null, state.getWinner());
        assertEquals(true, state.isTie());
        assertEquals(2, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

    }

    @Test // correctness of the assertions best verified by pen and paper...
    public void get_updated_sequences_and_longest_sequence_after_consecutive_moves() {
        GameState state = GameState.builder().setConnectHowMany(5).setBoard(new Piece[10][10]).setNextPlayer(Piece.X).build();
        printBoardAndLongestSequences(state);
        assertTrue(state.getUpdatedSequences().isEmpty());
        assertEquals(0, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(5, 5));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(5, 5), new Cell(5, 5))
        ), state.getUpdatedSequences());
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(1, 3));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(1, 3), new Cell(1, 3))
        ), state.getUpdatedSequences());
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(1, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(3, 3));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(3, 3), new Cell(3, 3))
        ), state.getUpdatedSequences());
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(1, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(2, 2));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(2, 2), new Cell(2, 2)),
                new Sequence(new Cell(1, 3), new Cell(2, 2))
        ), state.getUpdatedSequences());
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(4, 4));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(4, 4), new Cell(4, 4)),
                new Sequence(new Cell(3, 3), new Cell(5, 5))
        ), state.getUpdatedSequences());
        assertEquals(3, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(1, 2));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(1, 2), new Cell(1, 3)),
                new Sequence(new Cell(1, 2), new Cell(2, 2)),
                new Sequence(new Cell(1, 2), new Cell(1, 2))
        ), state.getUpdatedSequences());
        assertEquals(3, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(6, 6));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(6, 6), new Cell(6, 6)),
                new Sequence(new Cell(3, 3), new Cell(6, 6))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(7, 7));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(7, 7), new Cell(7, 7))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(1, 1));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(1, 1), new Cell(1, 1))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(1, 4));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(1, 2), new Cell(1, 4)),
                new Sequence(new Cell(1, 4), new Cell(1, 4))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(3, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(2, 4));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(2, 4), new Cell(2, 4)),
                new Sequence(new Cell(2, 4), new Cell(3, 3))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(3, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(1, 5));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(1, 2), new Cell(1, 5)),
                new Sequence(new Cell(1, 5), new Cell(1, 5))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(4, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(1, 6));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(1, 6), new Cell(1, 6))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(4, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(3, 1));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(3, 1), new Cell(3, 1)),
                new Sequence(new Cell(1, 3), new Cell(3, 1))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(4, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(3, 4));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(3, 3), new Cell(3, 4)),
                new Sequence(new Cell(2, 4), new Cell(4, 4)),
                new Sequence(new Cell(3, 4), new Cell(3, 4))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(4, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(0, 4));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(0, 4), new Cell(0, 4)),
                new Sequence(new Cell(0, 4), new Cell(1, 4)),
                new Sequence(new Cell(0, 4), new Cell(1, 5)),
                new Sequence(new Cell(0, 4), new Cell(3, 1))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(4, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(5, 4));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(5, 4), new Cell(5, 5)),
                new Sequence(new Cell(2, 4), new Cell(5, 4)),
                new Sequence(new Cell(5, 4), new Cell(5, 4))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(4, state.getLongestSequence(Piece.O).getLength());

        state = state.next(new Cell(4, 0));
        printBoardAndLongestSequences(state);
        assertEquals(asSet(
                new Sequence(new Cell(4, 0), new Cell(4, 0)),
                new Sequence(new Cell(0, 4), new Cell(4, 0))
        ), state.getUpdatedSequences());
        assertEquals(4, state.getLongestSequence(Piece.X).getLength());
        assertEquals(5, state.getLongestSequence(Piece.O).getLength());
    }

    private void printBoardAndLongestSequences(GameState gameState) {
        System.out.print(gameState.getStringRepresentation());
        for (Piece p : Piece.values()) System.out.println(p + ": " + gameState.getLongestSequence(p));
        System.out.println();
        System.out.println();
    }

    private Set<Sequence> asSet(Sequence... sequences) {
        return new HashSet<>(Arrays.asList(sequences));
    }

    @Test
     public void check_longest_sequence_on_diagonal_top_left_bottom_right_and_on_column() {
        // Cross: diagonal left-right (5, 12) to (9, 16), Round: column (2, 2) to (5, 2)
        GameState state = GameState.builder().setConnectHowMany(5).setBoard(new Piece[18][18]).setNextPlayer(Piece.X).build();
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(5, 12));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(2, 2));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(6, 13));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(3, 2));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(7, 14));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(4, 2));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(8, 15));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(5, 2));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(9, 16));
        assertEquals(Piece.O, state.getNextPlayer());

        printBoardAndLongestSequences(state);

        assertEquals(5, state.getLongestSequence(Piece.X).getLength());
        assertEquals(5, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(12, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(9, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(16, state.getLongestSequence(Piece.X).getEnd().getColumn());

        assertEquals(4, state.getLongestSequence(Piece.O).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getStart().getRow());
        assertEquals(2, state.getLongestSequence(Piece.O).getStart().getColumn());
        assertEquals(5, state.getLongestSequence(Piece.O).getEnd().getRow());
        assertEquals(2, state.getLongestSequence(Piece.O).getEnd().getColumn());
    }

    @Test
    public void check_longest_sequence_on_diagonal_top_right_bottom_left_and_on_row() {
        // Cross: diagonal right-left (5, 12) to (9, 8), Round: row (2, 2) to (2, 5)
        GameState state = GameState.builder().setConnectHowMany(5).setBoard(new Piece[18][18]).setNextPlayer(Piece.X).build();
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(5, 12));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(2, 2));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(6, 11));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(2, 3));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(7, 10));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(2, 4));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(8, 9));
        assertEquals(Piece.O, state.getNextPlayer());
        state = state.next(new Cell(2, 5));
        assertEquals(Piece.X, state.getNextPlayer());
        state = state.next(new Cell(9, 8));
        assertEquals(Piece.O, state.getNextPlayer());

        printBoardAndLongestSequences(state);

        assertEquals(5, state.getLongestSequence(Piece.X).getLength());
        assertEquals(5, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(12, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(9, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(8, state.getLongestSequence(Piece.X).getEnd().getColumn());

        assertEquals(4, state.getLongestSequence(Piece.O).getLength());
        assertEquals(2, state.getLongestSequence(Piece.O).getStart().getRow());
        assertEquals(2, state.getLongestSequence(Piece.O).getStart().getColumn());
        assertEquals(2, state.getLongestSequence(Piece.O).getEnd().getRow());
        assertEquals(5, state.getLongestSequence(Piece.O).getEnd().getColumn());
    }

    @Test
    public void check_longest_sequences_at_boundaries() {
        Piece[][] board;
        GameState state;

        board = new Piece[][] {
                {Piece.X,   Piece.X,    null},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(2, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {null,          Piece.X,    Piece.X},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(2, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {Piece.X,   null,           null},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {null,          null,           null},
                {null,          null,           null},
                {Piece.X,   null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(2, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(2, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {Piece.X,   null,           null},
                {null,          Piece.X,    null},
                {null,          null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(2, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(1, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {Piece.X,   null,           null},
                {null,          Piece.X,    null},
                {null,          null,           Piece.X}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(3, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(2, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {null,          null,           Piece.X},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(1, state.getLongestSequence(Piece.X).getLength());
        assertEquals(0, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).getEnd().getColumn());

        board = new Piece[][] {
                {null,          null,           null},
                {null,          Piece.X,    null},
                {Piece.X,   null,           null}
        };
        state = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        assertEquals(2, state.getLongestSequence(Piece.X).getLength());
        assertEquals(1, state.getLongestSequence(Piece.X).getStart().getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).getStart().getColumn());
        assertEquals(2, state.getLongestSequence(Piece.X).getEnd().getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).getEnd().getColumn());
    }

    @Test
    public void get_all_sequences() {
        Piece[][] board = new Piece[6][6];
        board[1][2] = Piece.X;
        board[2][1] = Piece.X;
        board[2][2] = Piece.X;
        board[3][2] = Piece.X;
        board[0][4] = Piece.O;
        board[0][5] = Piece.O;

        GameState gameState = GameState.builder().setConnectHowMany(3).setBoard(board).setNextPlayer(Piece.X).build();
        Map<Piece, List<Sequence>> sequences = gameState.getAllSequences();

        System.out.println(sequences);
        assertEquals(11, sequences.get(Piece.X).size());
        assertEquals(7, sequences.get(Piece.O).size());
    }


    @Test
    public void test_sequence_where_winner_was_not_correctly_determined_in_game() {
        GameState state = GameState.builder()
                .setBoard(new Piece[18][18])
                .setConnectHowMany(5)
                .setNextPlayer(Piece.X)
                .build();

        state = state.next(new Cell(7, 5)); // 1
        state = state.next(new Cell(7, 6)); // 2
        state = state.next(new Cell(8, 6)); // 3
        state = state.next(new Cell(9, 7)); // 4
        state = state.next(new Cell(8, 7)); // 5
        state = state.next(new Cell(8, 5)); // 6
        state = state.next(new Cell(6, 7)); // 7
        state = state.next(new Cell(7, 4)); // 8
        state = state.next(new Cell(9, 6)); // 9
        state = state.next(new Cell(7, 8)); // 10
        state = state.next(new Cell(7, 7)); // 11
        state = state.next(new Cell(6, 6)); // 12
        state = state.next(new Cell(5, 7)); // 13
        state = state.next(new Cell(4, 7)); // 14
        state = state.next(new Cell(9, 5)); // 15
        state = state.next(new Cell(6, 8)); // 16
        state = state.next(new Cell(10, 4)); // 17
        state = state.next(new Cell(11, 3)); // 18
        state = state.next(new Cell(8, 8)); // 19
        state = state.next(new Cell(5, 6)); // 20
        state = state.next(new Cell(6, 5)); // 21
        state = state.next(new Cell(3, 6)); // 22
        state = state.next(new Cell(4, 6)); // 23
        state = state.next(new Cell(2, 5)); // 24
        state = state.next(new Cell(5, 8)); // 25
        state = state.next(new Cell(10, 6)); // 26
        state = state.next(new Cell(8, 9)); // 27
        state = state.next(new Cell(8, 10)); // 28
        state = state.next(new Cell(9, 4)); // 29
        state = state.next(new Cell(1, 4)); // 30
        state = state.next(new Cell(0, 3)); // 31
        state = state.next(new Cell(3, 7)); // 32
        state = state.next(new Cell(9, 3)); // 33
        state = state.next(new Cell(9, 2)); // 34
        state = state.next(new Cell(10, 5)); // 35
        state = state.next(new Cell(11, 4)); // 36
        state = state.next(new Cell(11, 5)); // 37
        state = state.next(new Cell(12, 6)); // 38
        state = state.next(new Cell(8, 2)); // 39
        state = state.next(new Cell(7, 1)); // 40
        state = state.next(new Cell(11, 6)); // 41
        state = state.next(new Cell(8, 3)); // 42
        state = state.next(new Cell(12, 7)); // 43
        state = state.next(new Cell(13, 8)); // 44
        state = state.next(new Cell(12, 5)); // 45
        state = state.next(new Cell(13, 5)); // 46
        state = state.next(new Cell(13, 4)); // 47
        state = state.next(new Cell(14, 3)); // 48
        state.print();
        state = state.next(new Cell(10, 7)); // 49
        state.print();
        state = state.next(new Cell(10, 1)); // 50
        state.print();
        state = state.next(new Cell(9, 8)); // 51
        state.print();
        assertEquals(Piece.X, state.getWinner()); // Has 6 in a row!
        try {
            state = state.next(new Cell(11, 0)); // 52
            state.print();
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

}
