package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.Piece;

public record MCTSPlayerDTO(
        MCTSParameters params,
        Piece mySide,
        MoveNode lastMove
) { }
