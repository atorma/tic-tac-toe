"use strict";

var angular = require("angular");

angular.module("ticTacToe")
    .controller("GameController", GameController);

function GameController(GAME_EVENTS, gameService, $scope) {
    var vm = this;

    vm.startGame = startGame;


    function startGame() {
        gameService.currentGame.getMove();
    }

}
