"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");
var _ = require("lodash");


describe("GameController", function() {

    var vm;
    var GAME_EVENTS, PIECES;

    var gameService;
    var deferredTurn;
    var players;

    var $scope;
    var $q;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {
    }));

    beforeEach(angular.mock.inject(function(_GAME_EVENTS_, _PIECES_, $rootScope, _$q_) {
        GAME_EVENTS = _GAME_EVENTS_;
        PIECES = _PIECES_;

        $scope = $rootScope.$new();
        spyOn($scope, "$broadcast").and.callThrough();

        $q = _$q_;
    }));

    beforeEach(function() {
        gameService = jasmine.createSpyObj("gameService", [
            "startNewGame",
            "endCurrentGame",
            "getPlayers"
        ]);
        gameService.startNewGame.and.returnValue($q.when());
        gameService.endCurrentGame.and.returnValue($q.when());

        gameService.currentGame = {
            playTurn: function() {
                deferredTurn = $q.defer();
                return deferredTurn.promise;
            },
            board: [[null, null, null], [null, null, null], [null, null, null]]
        };

        players = [
            {
                id: "123",
                name: "Monte Carlo Tree Search"
            },
            {
                id: "456",
                name: "Naive"
            },
            {
                id: "789",
                name: "Random"
            }
        ];
        gameService.getPlayers.and.returnValue($q.when(players));
    });

    beforeEach(angular.mock.inject(function($controller) {
        vm = $controller("GameController", {
            gameService: gameService,
            $scope: $scope
        });

        $scope.$digest();
    }));


    describe("before starting game", function() {

        it("provides default game configuration", function() {
            expect(vm.PIECES).toEqual(PIECES);
            expect(vm.playerList).toEqual(players);

            expect(vm.gameConfig.connectHowMany).toBe(5);
            expect(vm.gameConfig.firstPlayer).toBe("RANDOM");
            expect(vm.gameConfig.board.rows).toBe(18);
            expect(vm.gameConfig.board.columns).toBe(18);
            expect(vm.gameConfig.players[PIECES.X]).toBeDefined();
            expect(vm.gameConfig.players[PIECES.O]).toBeDefined();

            expect(vm.gameConfig.rounds).toBe(1);
        });

        it("there is no game and pause is off", function() {
            expect(vm.gameExists).toBe(false);
            expect(vm.paused).toBe(false);
        });
    });


    describe("on starting new game", function() {

        it("requests game service to start new game using current game config", function() {
            vm.gameConfig.firstPlayer = PIECES.X;

            vm.startGame();
            $scope.$digest();

            expect(gameService.startNewGame).toHaveBeenCalledWith(vm.gameConfig);
        });

        it("if first player is 'RANDOM' configuration passed to game service has random PIECE value", function() {
            vm.gameConfig.firstPlayer = "RANDOM";

            vm.startGame();
            $scope.$digest();

            expect(vm.gameConfig.firstPlayer).toBe("RANDOM"); // not changed here

            expect(gameService.startNewGame).toHaveBeenCalled();
            var serviceArgs = gameService.startNewGame.calls.argsFor(0)[0];
            expect(_.values(PIECES)).toContain(serviceArgs.firstPlayer);
        });

        it("broadcasts event about starting new game", function() {
            vm.startGame();
            $scope.$digest();

            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.GAME_STARTED, gameService.currentGame);
        });

        it("flags that game exists and it is not paused", function() {
            vm.startGame();
            $scope.$digest();

            expect(vm.gameExists).toBe(true);
            expect(vm.paused).toBe(false);
        });

    });


    describe("after game started", function() {

        beforeEach(function() {
            vm.startGame();
            $scope.$digest();
        });

        it("requests the game to play turns and broadcasts them to the game board", function() {
            for (var i = 1; i <= 10; i++) {

                var turnResult = {
                    turnNumber: i,
                    move: {
                        piece: i%2 === 0 ? PIECES.O : PIECES.X,
                        cell: {row: i, column: i}
                    },
                    gameEnded: false,
                    winner: null,
                    winningSequence: null
                };

                deferredTurn.resolve(turnResult);
                $scope.$digest();

                expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, turnResult);
            }
        });

        it("stops when the game ends and broadcasts the result to the game board", function() {
            var endIteration = 5;
            var endResult = {
                turnNumber: endIteration,
                move: {
                    piece: PIECES.O,
                    cell: {row: endIteration, column: 0}
                },
                gameEnded: true,
                winner: PIECES.O,
                winningSequence: {
                    start: {row: 12, column: 5},
                    end: {row: 16, column: 5}
                }
            };

            for (var i = 1; i <= 10; i++) {

                var turnResult;
                if (i === endIteration) {
                    turnResult = endResult;
                } else {
                    turnResult = {
                        turnNumber: i,
                        move: {
                            piece: i%2 === 0 ? PIECES.O : PIECES.X,
                            cell: {row: i, column: i}
                        },
                        gameEnded: false,
                        winner: null,
                        winningSequence: null
                    };
                }

                deferredTurn.resolve(turnResult);
                $scope.$digest();
            }

            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, endResult);
            expect($scope.$broadcast).not.toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: endIteration + 1}));
        });

        it("requests game service to end game when game ends", function() {
            var lastTurnResult = {
                move: {
                    piece: PIECES.O,
                    cell: {row: 16, column: 5}
                },
                gameEnded: true,
                winner: PIECES.O,
                winningSequence: {
                    start: {row: 12, column: 5},
                    end: {row: 16, column: 5}
                }
            };

            deferredTurn.resolve(lastTurnResult);
            $scope.$digest();

            expect(gameService.endCurrentGame).toHaveBeenCalled();
            expect(vm.gameExists).toBe(false);
        });

        it("stops if playing turn fails", function() {

            var errorIteration = 5;
            var error = {
                message: "Something went wrong"
            };

            for (var i = 1; i <= 10; i++) {

                if (i === errorIteration) {
                    deferredTurn.reject(error);
                } else {
                    deferredTurn.resolve({
                        turnNumber: i,
                        move: {
                            piece: i%2 === 0 ? PIECES.O : PIECES.X,
                            cell: {row: i, column: i}
                        },
                        gameEnded: false,
                        winner: null,
                        winningSequence: null
                    });
                }
                $scope.$digest();
            }

            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: errorIteration - 1}));
            expect($scope.$broadcast).not.toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: errorIteration}));
        });

        it("setting to paused prevents next turn from being played and stores next result without broadcasting it", function() {
            var pauseIteration = 5;

            for (var i = 1; i <= 10; i++) {

                deferredTurn.resolve({
                    turnNumber: i,
                    move: {
                        piece: i%2 === 0 ? PIECES.O : PIECES.X,
                        cell: {row: i, column: i}
                    },
                    gameEnded: false,
                    winner: null,
                    winningSequence: null
                });

                if (i === pauseIteration) {
                    vm.setPaused(true);
                }

                $scope.$digest();

            }

            expect(vm.paused).toBe(true);
            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: pauseIteration - 1}));
            expect($scope.$broadcast).not.toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: pauseIteration}));
            expect(vm.pausedResult).toEqual({
                turnNumber: pauseIteration,
                move: {
                    piece: pauseIteration%2 === 0 ? PIECES.O : PIECES.X,
                    cell: {row: pauseIteration, column: pauseIteration}
                },
                gameEnded: false,
                winner: null,
                winningSequence: null
            });
        });

        it("unpausing broadcasts paused result and resumes playing", function() {
            var pauseIteration = 3;
            var unpauseIteration = 6;

            for (var i = 1; i <= 10; i++) {

                deferredTurn.resolve({
                    turnNumber: i,
                    move: {
                        piece: i%2 === 0 ? PIECES.O : PIECES.X,
                        cell: {row: i, column: i}
                    },
                    gameEnded: false,
                    winner: null,
                    winningSequence: null
                });

                if (i === pauseIteration) {
                    vm.setPaused(true);
                } else if (i === unpauseIteration) {
                    vm.setPaused(false);
                }

                $scope.$digest();

            }

            expect(vm.paused).toBe(false);
            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: pauseIteration}));
            for (i = unpauseIteration + 1; i <= 10; i++) {
                expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: i}));
            }
            expect(vm.pausedResult).toBeUndefined();
        });

        it("ending game stops play and requests service to end game", function() {
            var endIteration = 5;

            for (var i = 1; i <= 10; i++) {

                deferredTurn.resolve({
                    turnNumber: i,
                    move: {
                        piece: i%2 === 0 ? PIECES.O : PIECES.X,
                        cell: {row: i, column: i}
                    },
                    gameEnded: false,
                    winner: null,
                    winningSequence: null
                });

                if (i === endIteration) {
                    vm.endGame();
                }

                $scope.$digest();

            }

            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: endIteration - 1})); // -1 because we don't care what the result is after ending game
            expect(gameService.endCurrentGame).toHaveBeenCalled();
            expect(vm.gameExists).toBe(false);
            expect(vm.paused).toBe(false);
        });
    });

});