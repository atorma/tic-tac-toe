"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .controller("GameController", GameController);

function GameController(GAME_EVENTS, PIECES, PLAYER_TYPES, gameService, $scope, $q, $mdToast, $document) {
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
        return initRound()
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
            $mdToast.show({
                template: '<md-toast><spinner></spinner>&nbsp;Playing...</md-toast>',
                position: "top left",
                hideDelay: 0
            });
        }

        var promiseMove;
        var nextPlayer = vm.gameConfig.players[gameService.currentGame.nextPlayer];
        if (nextPlayer.type === PLAYER_TYPES.AI) {
            if (isHumanVsAiGame()) {
                $mdToast.show({
                    template: '<md-toast><spinner></spinner>&nbsp;Thinking...</md-toast>',
                    position: "top left",
                    hideDelay: 0
                });
            }
            promiseMove = $q.when();
        } else if (nextPlayer.type === PLAYER_TYPES.HUMAN) {
            if (isHumanVsAiGame()) {
                $mdToast.show({
                    template: '<md-toast>Your turn, human!</md-toast>',
                    position: "top left"
                });
            }
            deferredMove = $q.defer();
            promiseMove = deferredMove.promise;
        }

        promiseMove
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
                if (isHumanVsAiGame()) {
                    $mdToast.hide();
                }

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
                        vm.gameExists = false;
                        vm.paused = false;
                        $mdToast.hide();
                    }
                }
            })
            .catch(function() {
                $mdToast.hide();
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
        gameService.endCurrentGame(); // async, but we don't care whether it succeeds or fails
        vm.gameExists = false;
        vm.paused = false;
        vm.currentGame = undefined;
        $mdToast.hide();
    }

    function selectHumanPlayerMove(event, selectedCell) {
        deferredMove.resolve(selectedCell);
    }

}
