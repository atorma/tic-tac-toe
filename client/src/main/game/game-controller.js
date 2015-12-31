"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .controller("GameController", GameController)
    .controller("GameController.ToastController", ToastController);

function GameController(GAME_EVENTS, PIECES, PLAYER_TYPES, gameService, $scope, $q, $mdToast, spinnerOverlay, $mdMedia) {
    var vm = this;
    var deferredMove;
    var boardSpinner;

    vm.init = init;
    vm.startGame = startGame;
    vm.setPaused = setPaused;
    vm.endGame = endGame;
    vm.toggleConfigMode = toggleConfigMode;


    $scope.$on(GAME_EVENTS.MOVE_SELECTED, selectHumanPlayerMove);

    $scope.$watch(screenIsSmall, handleSmallScreen);


    init();


    function init() {
        vm.gameExists = false;
        vm.paused = false;
        vm.showConfig = false;
        vm.screenIsSmall = true;
        boardSpinner = spinnerOverlay("board-container");
        resetStats();

        return $q.all({
            playerList: gameService.getPlayers(),

            PIECES: PIECES,

            gameConfig: {
                connectHowMany: 5,
                firstPlayer: "RANDOM",
                board: {
                    rows: vm.screenIsSmall ? 15 : 18,
                    columns: vm.screenIsSmall ? 15 : 18
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
        return initRound()
            .then(resetStats)
            .then(function() {
                if (isAiVsAiGame()) {
                    showProgressToast("Playing AI vs AI...");
                }
            })
            .then(play);
    }

    function initRound() {
        boardSpinner.show();

        var gameConfig = _.cloneDeep(vm.gameConfig);
        if (gameConfig.firstPlayer === 'RANDOM') {
            gameConfig.firstPlayer = _.values(PIECES)[_.random(1)];
        }

        return gameService.startNewGame(gameConfig)
            .then(function() {
                boardSpinner.hide();
                vm.currentGame = gameService.currentGame;
                vm.gameExists = true;
                vm.paused = false;
                $scope.$broadcast(GAME_EVENTS.GAME_STARTED, gameService.currentGame);
            })
            .catch(handleError);
    }

    function play() {
        var nextPlayer = vm.gameConfig.players[gameService.currentGame.nextPlayer];
        if (nextPlayer.type === PLAYER_TYPES.AI) {
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
                if (!isAiVsAiGame()) {
                    boardSpinner.show();
                }
                return gameService.currentGame.playTurn(selectedCell);
            })
            .then(function(result) {
                boardSpinner.hide();
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
                if (vm.gameExists) {
                    return handleError(response);
                } else {
                    return $q.reject(response);
                }
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
        deferredMove = undefined;
        $mdToast.hide();
    }

    function selectHumanPlayerMove(event, selectedCell) {
        if (!vm.gameExists) {
            return;
        }
        if (vm.gameConfig.players[gameService.currentGame.nextPlayer].type !== PLAYER_TYPES.HUMAN) {
            return;
        }
        if (gameService.currentGame.board[selectedCell.row][selectedCell.column]) {
            return;
        }

        deferredMove.resolve(selectedCell);
    }


    function handleError(response) {
        boardSpinner.hide();

        var message;
        if (response.status === 404) {
            message = "Game no is longer active. Games are automatically deleted after 15 minutes of inactivity.";
        } else {
            message = "Oops. The game ended to an error. Sorry.";
        }

        showErrorToast(message).then(endGame);
        return $q.reject(response);
    }


    function screenIsSmall() {
        return $mdMedia("(max-width: 599px)") || $mdMedia("(max-height: 799px)");
    }

    function handleSmallScreen(screenIsSmall) {
        vm.screenIsSmall = screenIsSmall;
    }

    function toggleConfigMode() {
        vm.showConfig = !vm.showConfig;
    }

}

function ToastController($mdToast) {
    var vm = this;

    vm.hide = hide;

    function hide() {
        $mdToast.hide();
    }
}