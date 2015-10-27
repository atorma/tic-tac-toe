"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .factory("gameService", gameService);

function gameService(PIECES, $http, $q) {

    var service = {
        currentGame: undefined,
        startNewGame: startNewGame,
        endCurrentGame: endCurrentGame
    };
    return service;


    function startNewGame() {
        return service.endCurrentGame()
            .then(function() {
                return $http.post("games", {});
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

        function playTurn() {
            return $http.post("games/"+self.id+"/turns", {
                turnNumber: self.turnNumber
            }).then(function(response) {
                _.extend(self, response.data);
                return response.data;
            });
        }
    }

}

