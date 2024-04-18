package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.application.ComputationOutput;

public interface ComputationService {

    ComputationOutput playTurn(ComputationInput input);

}

