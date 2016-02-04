"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");
var _ = require("lodash");


describe("GameController", function() {

    var vm;
    var GAME_EVENTS, PIECES, PLAYER_TYPES;

    var gameService;
    var deferredTurn;
    var players;

    var $scope;
    var $q;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {
    }));

    beforeEach(angular.mock.inject(function(_GAME_EVENTS_, _PIECES_, _PLAYER_TYPES_, $rootScope, _$q_, $httpBackend) {
        GAME_EVENTS = _GAME_EVENTS_;
        PIECES = _PIECES_;
        PLAYER_TYPES = _PLAYER_TYPES_;

        $scope = $rootScope.$new();
        spyOn($scope, "$broadcast").and.callThrough();

        $q = _$q_;

        $httpBackend.whenGET(/resources.*/).respond(200);
    }));

    beforeEach(function() {
        gameService = jasmine.createSpyObj("gameService", [
            "startNewGame",
            "endCurrentGame",
            "getPlayers"
        ]);

        gameService.startNewGame.and.callFake(function() {
            // Set up fake game
            var currentGame = jasmine.createSpyObj("gameService.currentGame", ["playTurn"]);
            currentGame.nextPlayer = PIECES.X;
            currentGame.board = [
                [null, null, null],
                [null, null, null],
                [null, null, null]
            ];
            currentGame.playTurn.and.callFake(function() {
                if (currentGame.nextPlayer === PIECES.X) {
                    currentGame.nextPlayer = PIECES.O;
                } else {
                    currentGame.nextPlayer = PIECES.X;
                }
                deferredTurn = $q.defer();
                return deferredTurn.promise;
            });

            gameService.currentGame = currentGame;
            return $q.when();
        });

        gameService.endCurrentGame.and.returnValue($q.when());

        players = [
            {
                id: "123",
                name: "Monte Carlo Tree Search",
                type: PLAYER_TYPES.AI
            },
            {
                id: "456",
                name: "Naive",
                type: PLAYER_TYPES.AI
            },
            {
                id: "789",
                name: "Random",
                type: PLAYER_TYPES.AI
            }
        ];
        gameService.getPlayers.and.returnValue($q.when(players));
    });

    beforeEach(angular.mock.inject(function($controller) {
        vm = $controller("GameController", {
            gameService: gameService,
            $scope: $scope
        });

        vm.init();

        $scope.$digest();
    }));


    describe("before starting game", function() {

        it("provides default game configuration", function() {
            expect(vm.PIECES).toEqual(PIECES);
            expect(vm.playerList).toEqual(players);

            expect(vm.gameConfig.connectHowMany).toBe(5);
            expect(vm.gameConfig.firstPlayer).toBe("RANDOM");
            expect(vm.gameConfig.board.rows).toBeDefined();
            expect(vm.gameConfig.board.columns).toBeDefined();
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

        it("exposes current game to UI, flags that game exists and it is not paused", function() {
            vm.startGame();
            $scope.$digest();

            expect(vm.currentGame).toBe(gameService.currentGame);
            expect(vm.gameExists).toBe(true);
            expect(vm.paused).toBe(false);
        });

        it("resets initial game stats", function() {
            vm.gameStats = {
                currentRound: 100,
                wins: {},
                ties: 1
            };
            vm.gameStats.wins[PIECES.X] = 44;
            vm.gameStats.wins[PIECES.O] = 65;

            vm.startGame();
            $scope.$digest();

            expect(vm.gameStats.currentRound).toBe(1);
            expect(vm.gameStats.wins[PIECES.X]).toEqual(0);
            expect(vm.gameStats.wins[PIECES.O]).toEqual(0);
            expect(vm.gameStats.ties).toEqual(0);
        });
    });


    describe("after ai-vs-ai game started", function() {

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
            var endTurn = 5;
            var endResult = {
                turnNumber: endTurn,
                move: {
                    piece: PIECES.O,
                    cell: {row: endTurn, column: 0}
                },
                gameEnded: true,
                winner: PIECES.O,
                winningSequence: {
                    start: {row: 12, column: 5},
                    end: {row: 16, column: 5}
                }
            };

            for (var turn = 1; turn <= 10; turn++) {

                var turnResult;
                if (turn === endTurn) {
                    turnResult = endResult;
                } else {
                    turnResult = {
                        turnNumber: turn,
                        move: {
                            piece: turn%2 === 0 ? PIECES.O : PIECES.X,
                            cell: {row: turn, column: turn}
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
            expect($scope.$broadcast).not.toHaveBeenCalledWith(GAME_EVENTS.MOVE_COMPLETED, jasmine.objectContaining({turnNumber: endTurn + 1}));
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
            expect(vm.currentGame).toBeUndefined();
        });
    });

    describe("multi-round game", function() {

        beforeEach(function() {
            vm.gameConfig.rounds = 10;

            vm.startGame();
            $scope.$digest();
        });

        it("starts new rounds automatically and updates game stats", function() {

            // Play 6 rounds
            for (var round = 1; round <= 6; round++) {
                for (var turn = 1; turn <= 10; turn++) {

                    var gameEnded = turn === 10;
                    var winner = null;
                    if (gameEnded && round <= 3) {
                        winner = PIECES.X;
                    } else if (gameEnded && round <= 5) {
                        winner = PIECES.O;
                    } else {
                        winner = null; // tie
                    }

                    var turnResult = {
                        turnNumber: turn,
                        move: {
                            piece: turn%2 === 0 ? PIECES.O : PIECES.X,
                            cell: {row: turn, column: turn}
                        },
                        gameEnded: gameEnded,
                        winner: winner,
                        winningSequence: null
                    };

                    deferredTurn.resolve(turnResult);
                    $scope.$digest();
                }
            }

            // 7th round started
            expect(vm.gameStats.currentRound).toBe(7);
            // Stats from previous 6 rounds
            expect(vm.gameStats.wins[PIECES.X]).toBe(3);
            expect(vm.gameStats.wins[PIECES.O]).toBe(2);
            expect(vm.gameStats.ties).toBe(1);
        });

    });

    describe("after human vs ai game started", function() {

        beforeEach(function() {
            // AI starts: X predefined to start in fake currentGame
            vm.gameConfig.players[PIECES.X] =  {
                id: "123",
                name: "Monte Carlo Tree Search",
                type: PLAYER_TYPES.AI
            };
            vm.gameConfig.players[PIECES.O] = {
                id: "111",
                name: "Me",
                type: PLAYER_TYPES.HUMAN
            };
        });

        beforeEach(function() {
            vm.startGame();
            $scope.$digest();
        });

        it("waits for human player's move after AI move", function() {
            var aiTurnResult = {
                gameEnded: false
            };
            deferredTurn.resolve(aiTurnResult);
            $scope.$digest();

            expect(gameService.currentGame.playTurn.calls.count()).toBe(1);
        });

        it("on 'move selected' event it plays turn with the selected cell", function() {
            deferredTurn.resolve({gameEnded: false});
            $scope.$digest();

            var selectedCell = {row: 1, column: 2};
            $scope.$emit(GAME_EVENTS.MOVE_SELECTED, selectedCell);
            $scope.$digest();

            expect(gameService.currentGame.playTurn).toHaveBeenCalledWith(selectedCell);
        });

        it("ignores 'move selected' if it is not the human player's turn", function() {
            // No move yet, AI still thinking, but human selects cell
            var justClicking = {row: 1, column: 2};
            $scope.$emit(GAME_EVENTS.MOVE_SELECTED, justClicking);
            $scope.$digest();
            expect(gameService.currentGame.playTurn).not.toHaveBeenCalledWith(justClicking);

            // AI makes its move...
            deferredTurn.resolve({gameEnded: false});
            gameService.currentGame.nextPlayer = PIECES.O;
            $scope.$digest();

            // Human player's actual move
            var okMove = {row: 0, column: 1};
            $scope.$emit(GAME_EVENTS.MOVE_SELECTED, okMove);
            gameService.currentGame.nextPlayer = PIECES.X;
            $scope.$digest();
            expect(gameService.currentGame.playTurn).toHaveBeenCalledWith(okMove);

            // AI thinking again
            $scope.$emit(GAME_EVENTS.MOVE_SELECTED, justClicking);
            $scope.$digest();
            expect(gameService.currentGame.playTurn).not.toHaveBeenCalledWith(justClicking);
        });

        it("ignores 'move selected' if human player selects cell that is already occupied", function() {
            deferredTurn.resolve({gameEnded: false});
            $scope.$digest();

            gameService.currentGame.board = [
                [null, null, null],
                [null, null, "X"],
                [null, null, null]
            ];

            var selectedCell = {row: 1, column: 2};
            $scope.$emit(GAME_EVENTS.MOVE_SELECTED, selectedCell);
            $scope.$digest();

            expect(gameService.currentGame.playTurn).not.toHaveBeenCalledWith(selectedCell);
        });

        it("broadcasts request to show last move on command", function() {
            vm.replayLastMove();

            $scope.$digest();

            expect($scope.$broadcast).toHaveBeenCalledWith(GAME_EVENTS.SHOW_LAST_MOVE);
        });
    });

});