package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.ComputationService;
import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.application.ComputationOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Profile("cloud")
public class CloudFunctionComputationService implements ComputationService {
    private final Logger LOGGER = LoggerFactory.getLogger(CloudFunctionComputationService.class);
    private final RestClient restClient;
    private final LocalComputationService localComputationService = new LocalComputationService();

    public CloudFunctionComputationService(
            @Value("${MOVE_FUNCTION_URL}") String moveFunctionUrl
    ) {
        restClient = RestClient.builder()
                .baseUrl(moveFunctionUrl)
                .build();
    }

    @Override
    public ComputationOutput computeMove(ComputationInput input) {
        var player = input.player();
        if (player.moveIsHeavyComputation()) {
            LOGGER.info("Sending move computation to cloud function...");
            var startTime = System.currentTimeMillis();
            var response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(input)
                    .retrieve()
                    .toEntity(ComputationOutput.class);
            LOGGER.info("Cloud function computed move in {} ms", System.currentTimeMillis() - startTime);
            return response.getBody();
        } else {
            LOGGER.info("Computing move locally");
            return localComputationService.computeMove(input);
        }
    }


}
