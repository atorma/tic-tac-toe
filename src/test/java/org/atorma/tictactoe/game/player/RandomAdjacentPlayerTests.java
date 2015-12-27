package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.random.RandomAdjacentPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@Category(FastTests.class)
public class RandomAdjacentPlayerTests extends UnitTests {

    RandomAdjacentPlayer player;
    GameState state;

    @Before
    public void setUp() {
        player = new RandomAdjacentPlayer();
        player.setPiece(Piece.X);
    }


    @Test
    public void when_first_move_on_empty_board_then_chooses_some_cell_and_afterwards_starts_placing_pieces_adjacent_to_existing_ones() {
        state = GameState.builder()
                .setBoard(new Piece[18][18])
                .setConnectHowMany(5)
                .setNextPlayer(player.getPiece())
                .build();

        Cell myMove = player.move(state, null);

        assertTrue(myMove.getRow() >= 0 && myMove.getRow() < state.getBoardRows());
        assertTrue(myMove.getColumn() >= 0 && myMove.getColumn() < state.getBoardCols());

        state = state.next(myMove);
        state.print();

        Cell opponentsMove = Utils.pickRandom(state.getAllowedMoves());
        state = state.next(opponentsMove);

        myMove = player.move(state, opponentsMove);
        state = state.next(myMove);
        state.print();
        assertHasAdjacentOccupiedCell(myMove);
    }

    @Test
    public void when_game_starts_with_pieces_on_board_then_places_piece_adjacent_to_existing_piece() {
        state = GameState.builder()
                .setBoard(new Piece[18][18])
                .setConnectHowMany(5)
                .setNextPlayer(player.getPiece().other())
                .build();

        Cell opponentsMove = Utils.pickRandom(state.getAllowedMoves());
        state = state.next(opponentsMove);

        Cell myMove = player.move(state, opponentsMove);
        state = state.next(myMove);
        state.print();
        assertHasAdjacentOccupiedCell(myMove);

        opponentsMove = Utils.pickRandom(state.getAllowedMoves());
        state = state.next(opponentsMove);

        myMove = player.move(state, opponentsMove);
        state = state.next(myMove);
        state.print();
        assertHasAdjacentOccupiedCell(myMove);
    }

    private void assertHasAdjacentOccupiedCell(Cell move) {
        Set<Cell> occupiedCells = new HashSet<>();
        for (int i = 0; i < state.getBoardRows(); i++) {
            for (int j = 0; j < state.getBoardCols(); j++) {
                occupiedCells.add(new Cell(i, j));
            }
        }
        occupiedCells.removeAll(state.getAllowedMoves());

        boolean adjacentOccupied = false;
        for (Cell occupied : occupiedCells) {
            if (Cell.getDistance(occupied, move) == 1) {
                adjacentOccupied = true;
                break;
            }
        }
        assertTrue(adjacentOccupied);
    }
}
