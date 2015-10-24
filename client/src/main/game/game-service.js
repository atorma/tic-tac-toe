"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .factory("gameService", gameService);

function gameService(PIECES, $q) {

    var service = {
        currentGame: undefined,
        startNewGame: startNewGame
    };
    return service;


    function startNewGame() {
        service.currentGame = new TestGame();
        return $q.when();
    }


    function TestGame() {
        var currentPlayer = PIECES.CROSS;

        this.playTurn = playTurn;


        function playTurn() {
            var gameEnded = _.random(0, 1, true) > 0.9;
            var winningSequence;
            if (gameEnded) {
                winningSequence = {
                    start: {
                        row: _.random(0, 17),
                        column: _.random(0, 17)
                    },
                    end: {
                        row: _.random(0, 17),
                        column: _.random(0, 17)
                    }
                };
            }
            var result = {
                move: {
                    piece: currentPlayer,
                    cell: {
                        row: _.random(0, 17),
                        column: _.random(0, 17)
                    }
                },
                gameEnded: gameEnded,
                winningSequence: winningSequence
            };

            if (currentPlayer === PIECES.CROSS) {
                currentPlayer = PIECES.NOUGHT;
            } else {
                currentPlayer = PIECES.CROSS;
            }

            return $q.when(result);
        }
    }
}

