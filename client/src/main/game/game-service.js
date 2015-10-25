"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .factory("gameService", gameService);

function gameService(PIECES, $http, $q) {

    var service = {
        currentGame: undefined,
        startNewGame: startNewGame
    };
    return service;


    function startNewGame() {
        return $http.post("rest/games", {})
            .then(function(response) {
                service.currentGame = new Game(response.data);
            });
    }

    function Game(initialGameData) {
        var self = this;
        _.extend(self, initialGameData);

        this.playTurn = playTurn;

        function playTurn() {
            return $http.post("rest/games/"+self.id+"/turns", {
                turnNumber: self.turnNumber
            }).then(function(response) {
                _.extend(self, response.data);
                return response.data;
            });
        }
    }

    function TestGame() {
        var currentPlayer = PIECES.O;

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

            if (currentPlayer === PIECES.O) {
                currentPlayer = PIECES.X;
            } else {
                currentPlayer = PIECES.O;
            }

            return $q.when(result);
        }
    }
}

