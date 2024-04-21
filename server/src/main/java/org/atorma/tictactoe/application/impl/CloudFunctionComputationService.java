package org.atorma.tictactoe.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import org.atorma.tictactoe.application.ComputationService;
import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.application.ComputationOutput;
import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

@Service
@Profile("cloud")
public class CloudFunctionComputationService implements ComputationService {
    private final Logger LOGGER = LoggerFactory.getLogger(CloudFunctionComputationService.class);
    private final LocalComputationService localComputationService = new LocalComputationService();
    private final IdTokenCredentials.Builder idTokenBuilder;
    private final String moveFunctionUrl;
    private final ObjectMapper objectMapper;

    public CloudFunctionComputationService(
            @Value("${MOVE_FUNCTION_URL}") String moveFunctionUrl,
            @Autowired ObjectMapper objectMapper
    ) {
        this.moveFunctionUrl = moveFunctionUrl;
        this.objectMapper = objectMapper;

        IdTokenCredentials.Builder tempBuilder;
        try {
            var googleCredentials = GoogleCredentials.getApplicationDefault();
            if (googleCredentials instanceof IdTokenProvider) {
                tempBuilder = IdTokenCredentials.newBuilder()
                        .setIdTokenProvider((IdTokenProvider) googleCredentials)
                        .setTargetAudience(moveFunctionUrl);
            } else {
                throw new IllegalArgumentException("Credentials are not an instance of IdTokenProvider.");
            }
        } catch (IOException e) {
            tempBuilder = null;
            LOGGER.info("Google application credentials not found. Calls will be unauthenticated");
        }
        idTokenBuilder = tempBuilder;
    }

    @Override
    public ComputationOutput computeMove(ComputationInput input) {
        var player = input.player();
        if (player.moveIsHeavyComputation()) {
            if (player instanceof MCTSPlayer mctsPlayer) {
                LOGGER.info("MCTS tree before computation: {}", mctsPlayer.getLastMove());
            }
            var output = sendMoveToCloudFunction(input);
            if (player instanceof MCTSPlayer mctsPlayer) {
                LOGGER.info("MCTS tree after computation: {}", mctsPlayer.getLastMove());
            }
            return output;
        } else {
            LOGGER.info("Computing move locally");
            return localComputationService.computeMove(input);
        }
    }

    private ComputationOutput sendMoveToCloudFunction(ComputationInput input) {
        LOGGER.info("Sending move computation to cloud function...");
        var startTime = System.currentTimeMillis();
        var requestFactory = getRequestFactory();
        try {
            var request = requestFactory
                    .buildPostRequest(new GenericUrl(moveFunctionUrl), new JsonHttpContent(input));
            var response = request.execute();
            var output = objectMapper.readValue(response.getContent(), ComputationOutput.class);
            LOGGER.info("Cloud function computed move in {} ms", System.currentTimeMillis() - startTime);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequestFactory getRequestFactory() {
        var transport = new NetHttpTransport();
        if (idTokenBuilder != null) {
            var tokenCredential = idTokenBuilder.build();
            var adapter = new HttpCredentialsAdapter(tokenCredential);
            return transport.createRequestFactory(adapter);
        } else {
            return transport.createRequestFactory();
        }
    }

    private class JsonHttpContent extends AbstractHttpContent {
        private final Object object;

        JsonHttpContent(Object object) {
            super(new HttpMediaType("application", "json"));
            this.object = object;
        }


        @Override
        public boolean retrySupported() {
            return false;
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            objectMapper.writeValue(outputStream, object);
        }
    }

}
