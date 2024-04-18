package org.atorma.tictactoe.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.player.mcts.*;
import org.atorma.tictactoe.game.state.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@Tag("FastTests")
public class JSONSerializationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONSerializationTests.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void serializes_and_deserializes_MCTSParameters() throws JsonProcessingException {
        var params = new MCTSParameters();

        var json = objectMapper.writeValueAsString(params);
        LOGGER.info(json);
        var deserialized = objectMapper.readValue(json, MCTSParameters.class);

        assertEquals(params.simulationStrategy, deserialized.simulationStrategy);
        assertEquals(params.maxRolloutsNum, deserialized.maxRolloutsNum);
        assertEquals(params.gamesPerRollout, deserialized.gamesPerRollout);
        assertEquals(params.maxSimulatedGameTurns, deserialized.maxSimulatedGameTurns);
        assertEquals(params.maxThinkTimeMillis, deserialized.maxThinkTimeMillis);
        assertEquals(params.maxThinkTimeIncludesSimulation, deserialized.maxThinkTimeIncludesSimulation);
        assertEquals(params.rewardScheme, deserialized.rewardScheme);
        assertEquals(params.searchRadius, deserialized.searchRadius);
        assertEquals(params.pruneSiblings, deserialized.pruneSiblings);
        assertEquals(params.pruneParent, deserialized.pruneParent);
        assertEquals(params.pruneDescendantLevelsGreaterThan, deserialized.pruneDescendantLevelsGreaterThan);
    }

    @Test
    public void serializes_and_deserializes_piece() throws JsonProcessingException {
        var piece = Piece.X;

        var json = objectMapper.writeValueAsString(piece);
        LOGGER.info(json);
        var deserialized = objectMapper.readValue(json, Piece.class);

        assertEquals(Piece.X, deserialized);
    }

    @Test
    public void serializes_and_deserializes_Board() throws JsonProcessingException {
        var board = new DenseArrayBoard(new Piece[][] {
                {null, Piece.X, null},
                {null, Piece.O, null},
                {null, null,    null}
        });

        var json = objectMapper.writeValueAsString(board);
        LOGGER.info(json);
        var deserialized = objectMapper.readValue(json, Board.class);

        assertEquals(board, deserialized);
    }

    @Test
    public void serializes_and_deserializes_GameState() throws JsonProcessingException {
        GameState gameState = GameState.builder()
                .setBoard(new Piece[][] {
                        {null, Piece.X, null},
                        {null, Piece.O, null},
                        {null, null,    null}
                })
                .setConnectHowMany(3)
                .setNextPlayer(Piece.O)
                .build();

        var json = objectMapper.writeValueAsString(gameState);
        LOGGER.info(json);
        var deserialized = objectMapper.readValue(json, GameState.class);

        assertEquals(gameState.getConnectHowMany(), deserialized.getConnectHowMany());
        assertEquals(gameState.copyBoard(), deserialized.copyBoard());
        assertEquals(gameState.getNextPlayer(), deserialized.getNextPlayer());
        assertEquals(gameState.getAllowedMoves(), deserialized.getAllowedMoves());
        assertEquals(gameState.getLongestSequence(Piece.X), deserialized.getLongestSequence(Piece.X));
        assertEquals(gameState.getLongestSequence(Piece.O), deserialized.getLongestSequence(Piece.O));
        assertEquals(gameState.getUpdatedSequences(), deserialized.getUpdatedSequences());
        assertEquals(gameState.getAllSequences(), deserialized.getAllSequences());
    }

    @Test
    public void serializes_and_deserializes_MoveNode() throws JsonProcessingException {
        GameState startState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build();
        MoveNode root = new MoveNode(startState, null, new WinLossDrawScheme());
        MoveNode child = root.expandRandom();
        MoveNode grandChild = child.expandRandom();

        GameState endState = startState
                .next(new Cell(0,0))
                .next(new Cell(0,1))
                .next(new Cell(1,0))
                .next(new Cell(1,1))
                .next(new Cell(2,0));
        assertTrue(endState.isAtEnd());
        grandChild.propagateSimulatedResult(endState);

        var json = objectMapper.writeValueAsString(root);
        LOGGER.info(json);
        var deserializedRoot = objectMapper.readValue(json, MoveNode.class);

        assertEquals(root.getId(), deserializedRoot.getId());
        assertEquals(root.getMove(), deserializedRoot.getMove());
        assertEquals(root.getNextPlayer(), deserializedRoot.getNextPlayer());
        assertNull(deserializedRoot.getParent());
        assertSame(deserializedRoot, deserializedRoot.getRoot());
        assertEquals(root.getChildren().stream().map(MoveNode::getMove).toList(),
                deserializedRoot.getChildren().stream().map(MoveNode::getMove).toList());
        assertFalse(deserializedRoot.isEndState());
        assertFalse(deserializedRoot.isFullyExpanded());
        assertEquals(root.getWins(Piece.X), deserializedRoot.getWins(Piece.X));
        assertEquals(root.getExpectedReward(Piece.X), deserializedRoot.getExpectedReward(Piece.X));
        assertEquals(root.getNumPlays(), deserializedRoot.getNumPlays());

        var deserializedChild = deserializedRoot.getChildren().stream()
                .filter(n -> n.getId().equals(child.getId()))
                .findFirst().get();
        assertEquals(child.getMove(), deserializedChild.getMove());
        assertEquals(child.getNextPlayer(), deserializedChild.getNextPlayer());
        assertEquals(deserializedRoot, deserializedChild.getParent());
        assertEquals(deserializedRoot, deserializedChild.getRoot());
        assertEquals(child.getChildren().stream().map(MoveNode::getMove).toList(),
                deserializedChild.getChildren().stream().map(MoveNode::getMove).toList());
        assertFalse(deserializedChild.isEndState());
        assertFalse(deserializedChild.isFullyExpanded());
        assertEquals(child.getWins(Piece.O), deserializedChild.getWins(Piece.O));
        assertEquals(child.getExpectedReward(Piece.O), deserializedChild.getExpectedReward(Piece.O));
        assertEquals(child.getNumPlays(), deserializedChild.getNumPlays());

        var deserializedGrandChild = deserializedChild.getChildren().stream()
                .filter(n -> n.getId().equals(grandChild.getId()))
                .findFirst().get();
        assertEquals(grandChild.getMove(), deserializedGrandChild.getMove());
        assertEquals(grandChild.getNextPlayer(), deserializedGrandChild.getNextPlayer());
        assertEquals(deserializedChild, deserializedGrandChild.getParent());
        assertEquals(deserializedRoot, deserializedGrandChild.getRoot());
        assertEquals(grandChild.getChildren().stream().map(MoveNode::getMove).toList(),
                deserializedGrandChild.getChildren().stream().map(MoveNode::getMove).toList());
        assertFalse(deserializedGrandChild.isEndState());
        assertFalse(deserializedGrandChild.isFullyExpanded());
        assertEquals(grandChild.getWins(Piece.X), deserializedGrandChild.getWins(Piece.X));
        assertEquals(grandChild.getExpectedReward(Piece.X), deserializedGrandChild.getExpectedReward(Piece.X));
        assertEquals(grandChild.getNumPlays(), deserializedGrandChild.getNumPlays());
    }

    @Test
    public void serializes_and_deserializes_mcts_player_and_game() throws JsonProcessingException {
        var params = new MCTSParameters();
        params.maxRolloutsNum = 1;
        params.maxSimulatedGameTurns = 1;
        MCTSPlayer player = new MCTSPlayer(params);
        player.setPiece(Piece.X);
        var gameState = GameState.builder()
                .setBoard(new Piece[3][3])
                .setConnectHowMany(3)
                .setNextPlayer(player.getPiece())
                .build();
        var move = player.move(gameState, null);
        gameState = gameState.next(move);
        var opponentsLastMove = gameState.getAllowedMoves().getFirst();
        gameState = gameState.next(opponentsLastMove);

        var computationInput = new ComputationInput(player, gameState, opponentsLastMove);

        var json = objectMapper.writeValueAsString(computationInput);
        LOGGER.info(json);
        var deserialized = objectMapper.readValue(json, ComputationInput.class);

        assertEquals(opponentsLastMove, deserialized.lastMove());
    }
}
