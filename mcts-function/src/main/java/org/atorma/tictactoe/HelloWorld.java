package org.atorma.tictactoe;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;

public class HelloWorld implements HttpFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorld.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        LOGGER.error("Incoming request: {}", request);
        BufferedWriter writer = response.getWriter();
        writer.write("Hello World!");
    }
}
