"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .controller("GameController", GameController);

function GameController(GAME_EVENTS, PIECES, gameService, $scope, $q) {
    var vm = this;

    vm.init = init;
    vm.startGame = startGame;


    init();


    function init() {

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
                $scope.$broadcast(GAME_EVENTS.GAME_STARTED, gameService.currentGame);
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
