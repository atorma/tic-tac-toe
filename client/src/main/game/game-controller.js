"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .controller("GameController", GameController);

function GameController(GAME_EVENTS, PIECES, gameService, $scope, $q) {
    var vm = this;

    vm.init = init;
    vm.startGame = startGame;
    vm.setPaused = setPaused;
    vm.endGame = endGame;

    init();


    function init() {
        vm.gameExists = false;
        vm.paused = false;

        return $q.all({
            playerList: gameService.getPlayers(),

            PIECES: PIECES,

            gameConfig: {
                connectHowMany: 5,
                firstPlayer: PIECES.X,
                board: {
                    rows: 18,
                    columns: 18
                },
                players: {}
            }

        }).then(function(data) {
            _.extend(vm, data);

            vm.gameConfig.players[PIECES.X] = vm.playerList[0];
            vm.gameConfig.players[PIECES.O] = vm.playerList[0];
        });

    }

    function startGame() {
        gameService.startNewGame(vm.gameConfig)
            .then(function() {
                vm.gameExists = true;
                $scope.$broadcast(GAME_EVENTS.GAME_STARTED, gameService.currentGame);
            })
            .then(play);
    }

    function play() {
        playOneTurn()
            .then(function(result) {
                if (!vm.gameExists) {
                    return;
                } else if (!result.gameEnded && !vm.paused) {
                    play();
                } else if (result.gameEnded) {
                    endGame();
                }
            });
    }

    function playOneTurn() {
        return gameService.currentGame.playTurn()
            .then(function(result) {
                if (vm.gameExists && !vm.paused) {
                    $scope.$broadcast(GAME_EVENTS.MOVE_COMPLETED, result);
                } else if (vm.gameExists && vm.paused) {
                    vm.pausedResult = result;
                }
                return result;
            });
    }

    function setPaused(isPaused) {
        vm.paused = isPaused;
        if (!isPaused) {
            if (vm.pausedResult) {
                $scope.$broadcast(GAME_EVENTS.MOVE_COMPLETED, vm.pausedResult);
            }
            vm.pausedResult = undefined;
            play();
        }
    }

    function endGame() {
        gameService.endCurrentGame(); // async, but we don't care whether it succeeds or fails
        vm.gameExists = false;
        vm.paused = false;
    }
}
