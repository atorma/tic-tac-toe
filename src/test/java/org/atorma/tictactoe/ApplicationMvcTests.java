package org.atorma.tictactoe;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for testing the actual application using MockMVC to send requests.
 */
public abstract class ApplicationMvcTests extends ApplicationTests {

    @Autowired WebApplicationContext webAppContext;
    protected MockMvc mockMvc;

    @Before
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    }
}
