package org.atorma.tictactoe.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import org.atorma.tictactoe.application.ComputationService;
import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.application.ComputationOutput;
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

        try {
            var googleCredentials = GoogleCredentials.getApplicationDefault();
            if (googleCredentials instanceof IdTokenProvider) {
                idTokenBuilder = IdTokenCredentials.newBuilder()
                        .setIdTokenProvider((IdTokenProvider) googleCredentials)
                        .setTargetAudience(moveFunctionUrl);
            } else {
                throw new IllegalArgumentException("Credentials are not an instance of IdTokenProvider.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ComputationOutput computeMove(ComputationInput input) {
        var player = input.player();
        if (player.moveIsHeavyComputation()) {
            return sendMoveToCloudFunction(input);
        } else {
            LOGGER.info("Computing move locally");
            return localComputationService.computeMove(input);
        }
    }

    private ComputationOutput sendMoveToCloudFunction(ComputationInput input) {
        LOGGER.info("Sending move computation to cloud function...");
        var startTime = System.currentTimeMillis();
        try {
            var tokenCredential = idTokenBuilder.build();
            var adapter = new HttpCredentialsAdapter(tokenCredential);
            var transport = new NetHttpTransport();
            var request = transport.createRequestFactory(adapter)
                    .buildPostRequest(new GenericUrl(moveFunctionUrl), new JsonHttpContent(input));
            var response = request.execute();
            var output = objectMapper.readValue(response.getContent(), ComputationOutput.class);
            LOGGER.info("Cloud function computed move in {} ms", System.currentTimeMillis() - startTime);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class JsonHttpContent implements HttpContent {
        private final Object object;

        JsonHttpContent(Object object) {
            this.object = object;
        }

        @Override
        public long getLength() {
            return -1L;
        }

        @Override
        public String getType() {
            return "application/json; charset=UTF-8";
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
