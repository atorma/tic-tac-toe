package org.atorma.tictactoe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import org.atorma.tictactoe.game.player.mcts.MCTSGameInputDTO;
import org.atorma.tictactoe.game.player.mcts.MCTSGameOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;

public class MCTSFunction implements HttpFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCTSFunction.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (!request.getMethod().equals("POST")) {
            LOGGER.warn("Invalid method: {}", request.getMethod());
            response.setStatusCode(405);
            return;
        }
        if (request.getContentType().isEmpty() || !request.getContentType().get().equals("application/json")) {
            LOGGER.warn("Invalid content-type: {}", request.getContentType());
            response.setStatusCode(414);
            return;
        }

        MCTSGameInputDTO game;
        try {
            game = objectMapper.readValue(request.getReader(), MCTSGameInputDTO.class);
        } catch (Exception e) {
            LOGGER.warn("Invalid JSON: {}", e.getMessage());
            response.setStatusCode(400);
            return;
        }

        try {
            var move = game.player().move(game.gameState(), game.lastMove());
            // Output does not include game state to save on network IO
            var output = new MCTSGameOutputDTO(move, game.player());

            response.setStatusCode(200);
            response.setContentType("application/json");
            BufferedWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(output));
        } catch (IOException e) {
            LOGGER.error("Error writing response: {}", e.getMessage(), e);
            response.setStatusCode(500);
        }
    }
}
