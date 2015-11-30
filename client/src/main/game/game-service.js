"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .factory("gameService", gameService);

function gameService($http, $q) {

    var service = {
        currentGame: undefined,
        getPlayers: getPlayers,
        startNewGame: startNewGame,
        endCurrentGame: endCurrentGame
    };
    return service;


    function getPlayers() {
        return $http.get("players")
            .then(function(response) {
                return response.data;
            });
    }

    function startNewGame(gameParams) {
        return service.endCurrentGame()
            .then(function() {
                return $http.post("games", gameParams);
            })
            .then(function(response) {
                service.currentGame = new Game(response.data);
            });
    }

    function endCurrentGame() {
        if (!service.currentGame) {
            return $q.when();
        }
        return $http.delete("games/"+service.currentGame.id)
            .then(function() {
                service.currentGame = undefined;
            });
    }

    function Game(initialGameData) {
        var self = this;
        _.extend(self, initialGameData);

        this.playTurn = playTurn;

        function playTurn(selectedMove) {
            return $http.post("games/"+self.id+"/turns", {
                turnNumber: self.turnNumber,
                move: selectedMove
            }).then(function(response) {
                _.extend(self, response.data);
                var move = response.data.move;
                self.board[move.cell.row][move.cell.column] = move.piece;
                return response.data;
            });
        }
    }

}

