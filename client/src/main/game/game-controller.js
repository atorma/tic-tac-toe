"use strict";

var angular = require("angular");

angular.module("ticTacToe")
    .controller("GameController", GameController);

function GameController(GAME_EVENTS, gameService, $scope) {
    var vm = this;

    vm.startGame = startGame;


    function startGame() {
        play();
    }

    function play() {
        getOneMove().then(play);
    }

    function getOneMove() {
        return gameService.currentGame.getMove()
            .then(function(move) {
                $scope.$broadcast(GAME_EVENTS.MOVE_COMPLETED, move);
            });
    }

}
