package org.atorma.tictactoe;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TicTacToeApplication.class)
@WebAppConfiguration
@ActiveProfiles({"local", "test"})
public abstract class ApplicationTests {

}
