"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .controller("GameController", GameController)
    .controller("GameController.ToastController", ToastController);

function GameController(GAME_EVENTS, PIECES, PLAYER_TYPES, gameService, $scope, $q, $mdToast, spinnerOverlay) {
    var vm = this;
    var deferredMove;

    vm.init = init;
    vm.startGame = startGame;
    vm.setPaused = setPaused;
    vm.endGame = endGame;

    $scope.$on(GAME_EVENTS.MOVE_SELECTED, selectHumanPlayerMove);

    init();


    function init() {
        vm.gameExists = false;
        vm.paused = false;
        resetStats();

        return $q.all({
            playerList: gameService.getPlayers(),

            PIECES: PIECES,

            gameConfig: {
                connectHowMany: 5,
                firstPlayer: "RANDOM",
                board: {
                    rows: 18,
                    columns: 18
                },
                players: {},
                rounds: 1
            }

        }).then(function(data) {
            _.extend(vm, data);

            vm.gameConfig.players[PIECES.X] = vm.playerList[0];
            vm.gameConfig.players[PIECES.O] = vm.playerList[1];
        });

    }

    function startGame() {
        var overlay = spinnerOverlay("board-container");
        overlay.show();

        return initRound()
            .then(function() {
                overlay.hide();
            })
            .then(resetStats)
            .then(play);
    }

    function initRound() {
        var gameConfig = _.cloneDeep(vm.gameConfig);
        if (gameConfig.firstPlayer === 'RANDOM') {
            gameConfig.firstPlayer = _.values(PIECES)[_.random(1)];
        }

        return gameService.startNewGame(gameConfig)
            .then(function() {
                vm.currentGame = gameService.currentGame;
                vm.gameExists = true;
                vm.paused = false;
                $scope.$broadcast(GAME_EVENTS.GAME_STARTED, gameService.currentGame);
            });
    }

    function play() {
        if (isAiVsAiGame() && vm.currentGame.turnNumber === 1 && vm.gameStats.roundsPlayed === 0) {
            showProgressToast("Playing...");
        }

        var nextPlayer = vm.gameConfig.players[gameService.currentGame.nextPlayer];
        if (nextPlayer.type === PLAYER_TYPES.AI) {
            if (isHumanVsAiGame()) {
                showProgressToast("Thinking...");
            }
            deferredMove = $q.defer();
            deferredMove.resolve();
        } else if (nextPlayer.type === PLAYER_TYPES.HUMAN) {
            if (isHumanVsAiGame()) {
                showToast("Your turn, human!");
            }
            deferredMove = $q.defer();
        }

        deferredMove.promise
            .then(function(selectedCell) {
                return gameService.currentGame.playTurn(selectedCell);
            })
            .then(function(result) {
                if (vm.gameExists && !vm.paused) {
                    $scope.$broadcast(GAME_EVENTS.MOVE_COMPLETED, result);
                } else if (vm.gameExists && vm.paused) {
                    vm.pausedResult = result;
                }
                return result;
            })
            .then(function(result) {
                if (!vm.gameExists) {
                    return;
                } else if (!result.gameEnded && !vm.paused) {
                    play();
                } else if (result.gameEnded) {
                    gameService.endCurrentGame(); // async, but we don't care whether it succeeds or fails
                    updateStats(result);
                    if (vm.gameStats.roundsPlayed < vm.gameConfig.rounds) {
                        initRound().then(play);
                    } else {
                        endGame();
                    }
                }
            })
            .catch(function(response) {
                var message;
                if (response.status === 404) {
                    message = "Game no is longer active. Games are automatically deleted after 15 minutes of inactivity.";
                } else {
                    message = "Oops. An error occurred (status " + response.status + ")";
                }

                showErrorToast(message).then(endGame);
            });
    }

    function isHumanVsAiGame() {
        var types = _.pluck(vm.gameConfig.players, "type");
        return _.includes(types, PLAYER_TYPES.AI) && _.includes(types, PLAYER_TYPES.HUMAN);
    }

    function isAiVsAiGame() {
        var types = _.pluck(vm.gameConfig.players, "type");
        return _.includes(types, PLAYER_TYPES.AI) && !_.includes(types, PLAYER_TYPES.HUMAN);
    }

    function isHumanVsHumanGame() {
        var types = _.pluck(vm.gameConfig.players, "type");
        return !_.includes(types, PLAYER_TYPES.AI) && _.includes(types, PLAYER_TYPES.HUMAN);
    }


    function showToast(message, additionalOptions) {
        var options = _.extend({
            template: '<md-toast>{{vm.message}}</md-toast>',
            position: "top left",
            locals: {
                message: message
            },
            bindToController: true,
            controller: "GameController.ToastController",
            controllerAs: "vm"
        }, additionalOptions);
        return $mdToast.show(options);
    }

    function showProgressToast(message, additionalOptions) {
        return showToast(message, _.extend({
            template: '<md-toast><spinner></spinner>&nbsp;{{vm.message}}</md-toast>',
            hideDelay: 0
        }, additionalOptions));
    }

    function showErrorToast(message, additionalOptions) {
        return showToast(message, _.extend({
            template: '<md-toast md-theme="error">{{vm.message}} <md-button ng-click="vm.hide()" class="md-action md-icon-button" aria-label="close"><md-icon md-svg-icon="close"></md-icon></md-button></md-toast>',
            hideDelay: 0
        }, additionalOptions));
    }



    function resetStats() {
        vm.gameStats = {
            roundsPlayed: 0,
            ties: 0,
            wins: {}
        };
        vm.gameStats.wins[PIECES.X] = 0;
        vm.gameStats.wins[PIECES.O] = 0;
    }

    function updateStats(result) {
        if (result.gameEnded) {
            vm.gameStats.roundsPlayed++;
            if (result.winner) {
                vm.gameStats.wins[result.winner]++;
            } else {
                vm.gameStats.ties++;
            }
        }
    }

    function setPaused(isPaused) {
        vm.paused = isPaused;
        if (!isPaused) {
            if (vm.pausedResult) {
                $scope.$broadcast(GAME_EVENTS.MOVE_COMPLETED, vm.pausedResult);
                vm.pausedResult = undefined;
            }
            play();
        }
    }

    function endGame() {
        if (gameService.currentGame) {
            gameService.endCurrentGame(); // async, but we don't care whether it succeeds or fails
        }
        vm.gameExists = false;
        vm.paused = false;
        vm.currentGame = undefined;
        $mdToast.hide();
    }

    function selectHumanPlayerMove(event, selectedCell) {
        if (deferredMove) {
            deferredMove.resolve(selectedCell);
        }
    }

}

function ToastController($mdToast) {
    var vm = this;

    vm.hide = hide;

    function hide() {
        $mdToast.hide();
    };
}