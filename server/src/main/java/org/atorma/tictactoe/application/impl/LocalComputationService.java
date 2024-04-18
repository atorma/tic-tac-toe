package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.ComputationService;
import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.application.ComputationOutput;
import org.springframework.stereotype.Service;

@Service
public class LocalComputationService implements ComputationService {

    @Override
    public ComputationOutput playTurn(ComputationInput input) {
        var move = input.player().move(input.state(), input.lastMove());
        return new ComputationOutput(input.player(), move);
    }
}
