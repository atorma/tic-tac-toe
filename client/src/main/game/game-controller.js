"use strict";

var angular = require("angular");

angular.module("ticTacToe")
    .controller("GameController", GameController);

function GameController(GAME_EVENTS, gameService, $scope) {
    var vm = this;

    vm.startGame = startGame;

    function startGame() {
        gameService.startNewGame()
            .then(function() {
                $scope.$broadcast(GAME_EVENTS.GAME_STARTED);
            })
            .then(play);
    }

    function play() {
        playOneTurn()
            .then(function(result) {
                if (!result.gameEnded) {
                    play();
                } else {
                    gameService.endCurrentGame();
                }
            })
            .catch(function(reason) {
                // TODO error message or something
            });
    }

    function playOneTurn() {
        return gameService.currentGame.playTurn()
            .then(function(result) {
                $scope.$broadcast(GAME_EVENTS.MOVE_COMPLETED, result);
                return result;
            });
    }

}
