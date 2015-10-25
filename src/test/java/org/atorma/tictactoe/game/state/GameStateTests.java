package org.atorma.tictactoe.game.state;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GameStateTests {

    @Test
    public void get_allowed_moves_when_no_winner() {
        Piece[][] board = {
                {null, Piece.X, Piece.O},
                {null, Piece.X, null},
                {null, Piece.O, null}
        };

        GameState gameState = new GameState(3, board, Piece.X);
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
                {null,        Piece.X, null},
                {null,        Piece.X, null}
        };

        GameState gameState = new GameState(3, board, Piece.O);
        List<Cell> allowedMoves = gameState.getAllowedMoves();

        assertEquals(0, allowedMoves.size());
    }

    @Test
    public void get_state_after_move() {
        Piece[][] board = {
                {null, Piece.X, Piece.O},
                {null, Piece.X, null},
                {null, Piece.O, null}
        };

        GameState gameState1 = new GameState(3, board, Piece.X);
        assertEquals(Piece.X, gameState1.getTurn());
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
        assertEquals(Piece.O, gameState2.getTurn());
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
        state = new GameState(3, noWinnerNoTie, Piece.X);
        assertEquals(null, state.getWinner());
        assertEquals(false, state.isTie());
        assertEquals(2, state.getLongestSequence(Piece.X).length);
        assertEquals(1, state.getLongestSequence(Piece.O).length);

        Piece[][] roundHorizontal = {
                {null,        Piece.X, Piece.X},
                {Piece.O, Piece.O, Piece.O},
                {null,        Piece.X, Piece.X}
        };
        state = new GameState(3, roundHorizontal, Piece.O);
        assertEquals(Piece.O, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] crossVertical = {
                {Piece.O, Piece.O, Piece.X},
                {null,        Piece.X, Piece.X},
                {Piece.O, Piece.O, Piece.X}
        };
        state = new GameState(3, crossVertical, Piece.O);
        assertEquals(Piece.X, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] roundDiagTopLeft = {
                {Piece.O, Piece.O, Piece.X},
                {Piece.X, Piece.O, Piece.X},
                {null,        Piece.X, Piece.O}
        };
        state = new GameState(3, roundDiagTopLeft, Piece.O);
        assertEquals(Piece.O, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] crossDiagTopRight = {
                {Piece.O, Piece.O, Piece.X},
                {Piece.O, Piece.X, Piece.X},
                {Piece.X, Piece.O, Piece.O}
        };
        state = new GameState(3, crossDiagTopRight, Piece.O);
        assertEquals(Piece.X, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] tie = {
                {Piece.X, Piece.O, Piece.X},
                {Piece.O, Piece.O, Piece.X},
                {Piece.X, Piece.X, Piece.O}
        };
        state = new GameState(3, tie, Piece.O);
        assertEquals(null, state.getWinner());
        assertEquals(true, state.isTie());
        assertEquals(2, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

    }

    @Test // correctness of the assertions best verified by pen and paper...
    public void get_longest_sequence_after_consecutive_moves() {
        GameState state = new GameState(5, new Piece[10][10], Piece.X);
        printBoardAndLongestSequences(state);
        assertEquals(0, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(5, 5));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(1, 3));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(1, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(3, 3));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(1, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(2, 2));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(4, 4));
        printBoardAndLongestSequences(state);
        assertEquals(3, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(1, 2));
        printBoardAndLongestSequences(state);
        assertEquals(3, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(6, 6));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(7, 7));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(1, 1));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(1, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(3, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(2, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(3, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(1, 5));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(4, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(1, 6));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(4, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(3, 1));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(4, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(3, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(4, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(0, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(4, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(5, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(4, state.getLongestSequence(Piece.O).length);

        state = state.next(new Cell(4, 0));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.X).length);
        assertEquals(5, state.getLongestSequence(Piece.O).length);
    }

    private void printBoardAndLongestSequences(GameState gameState) {
        System.out.print(gameState.getStringRepresentation());
        for (Piece p : Piece.values()) System.out.println(p + ": " + gameState.getLongestSequence(p));
        System.out.println();
        System.out.println();
    }

    @Test
     public void check_longest_sequence_on_diagonal_top_left_bottom_right_and_on_column() {
        // Cross: diagonal left-right (5, 12) to (9, 16), Round: column (2, 2) to (5, 2)
        GameState state = new GameState(5, new Piece[18][18], Piece.X);
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(5, 12));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(2, 2));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(6, 13));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(3, 2));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(7, 14));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(4, 2));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(8, 15));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(5, 2));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(9, 16));
        assertEquals(Piece.O, state.getTurn());

        printBoardAndLongestSequences(state);

        assertEquals(5, state.getLongestSequence(Piece.X).length);
        assertEquals(5, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(12, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(9, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(16, state.getLongestSequence(Piece.X).end.getColumn());

        assertEquals(4, state.getLongestSequence(Piece.O).length);
        assertEquals(2, state.getLongestSequence(Piece.O).start.getRow());
        assertEquals(2, state.getLongestSequence(Piece.O).start.getColumn());
        assertEquals(5, state.getLongestSequence(Piece.O).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.O).end.getColumn());
    }

    @Test
    public void check_longest_sequence_on_diagonal_top_right_bottom_left_and_on_row() {
        // Cross: diagonal right-left (5, 12) to (9, 8), Round: row (2, 2) to (2, 5)
        GameState state = new GameState(5, new Piece[18][18], Piece.X);
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(5, 12));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(2, 2));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(6, 11));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(2, 3));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(7, 10));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(2, 4));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(8, 9));
        assertEquals(Piece.O, state.getTurn());
        state = state.next(new Cell(2, 5));
        assertEquals(Piece.X, state.getTurn());
        state = state.next(new Cell(9, 8));
        assertEquals(Piece.O, state.getTurn());

        printBoardAndLongestSequences(state);

        assertEquals(5, state.getLongestSequence(Piece.X).length);
        assertEquals(5, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(12, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(9, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(8, state.getLongestSequence(Piece.X).end.getColumn());

        assertEquals(4, state.getLongestSequence(Piece.O).length);
        assertEquals(2, state.getLongestSequence(Piece.O).start.getRow());
        assertEquals(2, state.getLongestSequence(Piece.O).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.O).end.getRow());
        assertEquals(5, state.getLongestSequence(Piece.O).end.getColumn());
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
        state = new GameState(3, board, Piece.X);
        assertEquals(2, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {null,          Piece.X,    Piece.X},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(2, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {Piece.X,   null,           null},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {null,          null,           null},
                {null,          null,           null},
                {Piece.X,   null,           null}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(2, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {Piece.X,   null,           null},
                {null,          Piece.X,    null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(2, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(1, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {Piece.X,   null,           null},
                {null,          Piece.X,    null},
                {null,          null,           Piece.X}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(3, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {null,          null,           Piece.X},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(1, state.getLongestSequence(Piece.X).length);
        assertEquals(0, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.X).end.getColumn());

        board = new Piece[][] {
                {null,          null,           null},
                {null,          Piece.X,    null},
                {Piece.X,   null,           null}
        };
        state = new GameState(3, board, Piece.X);
        assertEquals(2, state.getLongestSequence(Piece.X).length);
        assertEquals(1, state.getLongestSequence(Piece.X).start.getRow());
        assertEquals(1, state.getLongestSequence(Piece.X).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.X).end.getRow());
        assertEquals(0, state.getLongestSequence(Piece.X).end.getColumn());
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

        GameState gameState = new GameState(3, board, Piece.X);
        Map<Piece, List<GameState.Sequence>> sequences = gameState.getAllSequences();

        System.out.println(sequences);
        assertEquals(11, sequences.get(Piece.X).size());
        assertEquals(7, sequences.get(Piece.O).size());
    }




}
