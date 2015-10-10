package org.atorma.tictactoe.game;

import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GameStateTests {

    @Test
    public void get_allowed_moves_when_no_winner() {
        Piece[][] board = {
                {null, Piece.CROSS, Piece.ROUND},
                {null, Piece.CROSS, null},
                {null, Piece.ROUND, null}
        };

        GameState gameState = new GameState(3, board, Piece.CROSS);
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
                {Piece.ROUND, Piece.CROSS, Piece.ROUND},
                {null,        Piece.CROSS, null},
                {null,        Piece.CROSS, null}
        };

        GameState gameState = new GameState(3, board, Piece.ROUND);
        List<Cell> allowedMoves = gameState.getAllowedMoves();

        assertEquals(0, allowedMoves.size());
    }

    @Test
    public void get_state_after_move() {
        Piece[][] board = {
                {null, Piece.CROSS, Piece.ROUND},
                {null, Piece.CROSS, null},
                {null, Piece.ROUND, null}
        };

        GameState gameState1 = new GameState(3, board, Piece.CROSS);
        assertEquals(Piece.CROSS, gameState1.getTurn());
        assertEquals(4, gameState1.getNumPieces());
        assertEquals(5, gameState1.getAllowedMoves().size());

        Cell move = new Cell(0, 0);
        assertTrue(gameState1.isAllowed(move));
        Piece[][] expectedBoard = {
                {Piece.CROSS, Piece.CROSS, Piece.ROUND},
                {null,        Piece.CROSS, null},
                {null,        Piece.ROUND, null}
        };

        GameState gameState2 = gameState1.next(move);
        assertFalse(gameState2.isAllowed(move));
        assertEqualBoards(expectedBoard, getBoardAsArray(gameState2));
        assertEquals(Piece.ROUND, gameState2.getTurn());
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
                {null, Piece.CROSS, Piece.ROUND},
                {null, Piece.CROSS, null},
                {null, Piece.ROUND, null}
        };
        state = new GameState(3, noWinnerNoTie, Piece.CROSS);
        assertEquals(null, state.getWinner());
        assertEquals(false, state.isTie());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(1, state.getLongestSequence(Piece.ROUND).length);

        Piece[][] roundHorizontal = {
                {null,        Piece.CROSS, Piece.CROSS},
                {Piece.ROUND, Piece.ROUND, Piece.ROUND},
                {null,        Piece.CROSS, Piece.CROSS}
        };
        state = new GameState(3, roundHorizontal, Piece.ROUND);
        assertEquals(Piece.ROUND, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] crossVertical = {
                {Piece.ROUND, Piece.ROUND, Piece.CROSS},
                {null,        Piece.CROSS, Piece.CROSS},
                {Piece.ROUND, Piece.ROUND, Piece.CROSS}
        };
        state = new GameState(3, crossVertical, Piece.ROUND);
        assertEquals(Piece.CROSS, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] roundDiagTopLeft = {
                {Piece.ROUND, Piece.ROUND, Piece.CROSS},
                {Piece.CROSS, Piece.ROUND, Piece.CROSS},
                {null,        Piece.CROSS, Piece.ROUND}
        };
        state = new GameState(3, roundDiagTopLeft, Piece.ROUND);
        assertEquals(Piece.ROUND, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] crossDiagTopRight = {
                {Piece.ROUND, Piece.ROUND, Piece.CROSS},
                {Piece.ROUND, Piece.CROSS, Piece.CROSS},
                {Piece.CROSS, Piece.ROUND, Piece.ROUND}
        };
        state = new GameState(3, crossDiagTopRight, Piece.ROUND);
        assertEquals(Piece.CROSS, state.getWinner());
        assertEquals(false, state.isTie());

        Piece[][] tie = {
                {Piece.CROSS, Piece.ROUND, Piece.CROSS},
                {Piece.ROUND, Piece.ROUND, Piece.CROSS},
                {Piece.CROSS, Piece.CROSS, Piece.ROUND}
        };
        state = new GameState(3, tie, Piece.ROUND);
        assertEquals(null, state.getWinner());
        assertEquals(true, state.isTie());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

    }

    @Test // correctness of the assertions best verified by pen and paper...
    public void get_longest_sequence_after_consecutive_moves() {
        GameState state = new GameState(5, new Piece[10][10], Piece.CROSS);
        printBoardAndLongestSequences(state);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(5, 5));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(1, 3));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(1, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(3, 3));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(1, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(2, 2));
        printBoardAndLongestSequences(state);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(4, 4));
        printBoardAndLongestSequences(state);
        assertEquals(3, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(1, 2));
        printBoardAndLongestSequences(state);
        assertEquals(3, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(6, 6));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(7, 7));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(1, 1));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(1, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(3, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(2, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(3, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(1, 5));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(1, 6));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(3, 1));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(3, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(0, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(5, 4));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);

        state = state.next(new Cell(4, 0));
        printBoardAndLongestSequences(state);
        assertEquals(4, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(5, state.getLongestSequence(Piece.ROUND).length);
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
        GameState state = new GameState(5, new Piece[18][18], Piece.CROSS);
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(5, 12));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(2, 2));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(6, 13));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(3, 2));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(7, 14));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(4, 2));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(8, 15));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(5, 2));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(9, 16));
        assertEquals(Piece.ROUND, state.getTurn());

        printBoardAndLongestSequences(state);

        assertEquals(5, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(5, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(12, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(9, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(16, state.getLongestSequence(Piece.CROSS).end.getColumn());

        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).start.getRow());
        assertEquals(2, state.getLongestSequence(Piece.ROUND).start.getColumn());
        assertEquals(5, state.getLongestSequence(Piece.ROUND).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.ROUND).end.getColumn());
    }

    @Test
    public void check_longest_sequence_on_diagonal_top_right_bottom_left_and_on_row() {
        // Cross: diagonal right-left (5, 12) to (9, 8), Round: row (2, 2) to (2, 5)
        GameState state = new GameState(5, new Piece[18][18], Piece.CROSS);
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(5, 12));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(2, 2));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(6, 11));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(2, 3));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(7, 10));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(2, 4));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(8, 9));
        assertEquals(Piece.ROUND, state.getTurn());
        state = state.next(new Cell(2, 5));
        assertEquals(Piece.CROSS, state.getTurn());
        state = state.next(new Cell(9, 8));
        assertEquals(Piece.ROUND, state.getTurn());

        printBoardAndLongestSequences(state);

        assertEquals(5, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(5, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(12, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(9, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(8, state.getLongestSequence(Piece.CROSS).end.getColumn());

        assertEquals(4, state.getLongestSequence(Piece.ROUND).length);
        assertEquals(2, state.getLongestSequence(Piece.ROUND).start.getRow());
        assertEquals(2, state.getLongestSequence(Piece.ROUND).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.ROUND).end.getRow());
        assertEquals(5, state.getLongestSequence(Piece.ROUND).end.getColumn());
    }

    @Test
    public void check_longest_sequences_at_boundaries() {
        Piece[][] board;
        GameState state;

        board = new Piece[][] {
                {Piece.CROSS,   Piece.CROSS,    null},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(2, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(1, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {null,          Piece.CROSS,    Piece.CROSS},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(2, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(1, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {Piece.CROSS,   null,           null},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {null,          null,           null},
                {null,          null,           null},
                {Piece.CROSS,   null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(2, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {Piece.CROSS,   null,           null},
                {null,          Piece.CROSS,    null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(2, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(1, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(1, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {Piece.CROSS,   null,           null},
                {null,          Piece.CROSS,    null},
                {null,          null,           Piece.CROSS}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(3, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {null,          null,           Piece.CROSS},
                {null,          null,           null},
                {null,          null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(0, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).end.getColumn());

        board = new Piece[][] {
                {null,          null,           null},
                {null,          Piece.CROSS,    null},
                {Piece.CROSS,   null,           null}
        };
        state = new GameState(3, board, Piece.CROSS);
        assertEquals(2, state.getLongestSequence(Piece.CROSS).length);
        assertEquals(1, state.getLongestSequence(Piece.CROSS).start.getRow());
        assertEquals(1, state.getLongestSequence(Piece.CROSS).start.getColumn());
        assertEquals(2, state.getLongestSequence(Piece.CROSS).end.getRow());
        assertEquals(0, state.getLongestSequence(Piece.CROSS).end.getColumn());
    }

    @Test
    public void get_all_sequences() {
        Piece[][] board = new Piece[6][6];
        board[1][2] = Piece.CROSS;
        board[2][1] = Piece.CROSS;
        board[2][2] = Piece.CROSS;
        board[3][2] = Piece.CROSS;
        board[0][4] = Piece.ROUND;
        board[0][5] = Piece.ROUND;

        GameState gameState = new GameState(3, board, Piece.CROSS);
        Map<Piece, List<GameState.Sequence>> sequences = gameState.getAllSequences();

        System.out.println(sequences);
        assertEquals(11, sequences.get(Piece.CROSS).size());
        assertEquals(7, sequences.get(Piece.ROUND).size());
    }




}
