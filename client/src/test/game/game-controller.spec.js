"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");


describe("GameController", function() {

    var vm;
    var GAME_EVENTS, PIECES;

    var gameService;
    var players;

    var $scope, $childScope;
    var $q;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {
    }));

    beforeEach(angular.mock.inject(function(_GAME_EVENTS_, _PIECES_, $rootScope, _$q_) {
        GAME_EVENTS = _GAME_EVENTS_;
        PIECES = _PIECES_;
        $scope = $rootScope.$new();
        $childScope = $scope.$new();
        $q = _$q_;
    }));

    beforeEach(function() {
        gameService = jasmine.createSpyObj("gameService", [
            "startNewGame",
            "endCurrentGame",
            "getPlayers"
        ]);
        gameService.startNewGame.and.returnValue($q.when());
        gameService.endCurrentGame.and.returnValue($q.when())

        players = [
            {
                name: "Monte Carlo Tree Search",
                type: "AI"
            },
            {
                name: "Naive",
                type: "AI"
            },
            {
                name: "Random",
                type: "AI"
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

            expect(vm.gameConfig.board.rows).toBe(18);
            expect(vm.gameConfig.board.columns).toBe(18);
            expect(vm.gameConfig.players[PIECES.X]).toBeDefined();
            expect(vm.gameConfig.players[PIECES.O]).toBeDefined();
        });

    });


    describe("on starting new game", function() {

        beforeEach(function() {
            gameService.currentGame = {
                playTurn: function() {
                    return $q.when({
                        gameEnded: true
                    });
                },
                board: [[null, null, null], [null, null, null], [null, null, null]]
            };
        });

        it("requests game service to start new game", function() {
            var gameConfig = {
                board: {rows: 3, columns: 3},
                players: {}
            };
            vm.gameConfig = gameConfig;

            vm.startGame();
            $scope.$digest();

            expect(gameService.startNewGame).toHaveBeenCalledWith(gameConfig);
        });

        it("broadcasts event about starting new game", function() {
            var broadcastedGame = null;
            $childScope.$on(GAME_EVENTS.GAME_STARTED, function(event, game) {
                broadcastedGame = game;
            });

            vm.startGame();
            $scope.$digest();

            expect(broadcastedGame).toEqual(gameService.currentGame);
        });

    });


    describe("after game started", function() {

        var deferredTurn;
        var broadcastedResult;

        beforeEach(function() {
            gameService.currentGame = {
                playTurn: function() {
                    deferredTurn = $q.defer();
                    return deferredTurn.promise;
                }
            };
        });

        beforeEach(function() {
            broadcastedResult = null;
            $childScope.$on(GAME_EVENTS.MOVE_COMPLETED, function(event, result) {
                broadcastedResult = result;
            });
        });

        beforeEach(function() {
            vm.startGame();
            $scope.$digest();
        });

        it("requests the game to play turns and broadcasting them to the game board", function() {
            for (var i = 0; i < 10; i++) {

                var turnResult = {
                    move: {
                        piece: i%2 === 0 ? PIECES.O : PIECES.X,
                        cell: {row: 5, column: 12}
                    },
                    gameEnded: false,
                    winner: null,
                    winningSequence: null
                };

                deferredTurn.resolve(turnResult);
                $scope.$digest();

                expect(broadcastedResult).toEqual(turnResult);
            }
        });

        it("stops when the game ends and broadcasts the result to the game board", function() {

            var lastTurnNumber = 5;
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

            for (var i = 1; i <= 10; i++) {

                var turnResult;
                if (i === lastTurnNumber) {
                    turnResult = lastTurnResult;
                } else {
                    turnResult = {
                        move: {},
                        gameEnded: false,
                        winner: null,
                        winningSequence: null
                    };
                }

                deferredTurn.resolve(turnResult);
                $scope.$digest();
            }

            expect(broadcastedResult).toEqual(lastTurnResult);
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
        });

        it("stops if playing turn fails", function() {

            var errorTurnNumber = 5;
            var error = {
                message: "Something went wrong"
            };

            for (var i = 1; i <= 10; i++) {

                if (i === errorTurnNumber) {
                    deferredTurn.reject(error);
                } else {
                    deferredTurn.resolve({
                        move: {
                            cell: {row: i, column: 0}
                        },
                        gameEnded: false,
                        winner: null,
                        winningSequence: null
                    });
                }
                $scope.$digest();
            }

            expect(broadcastedResult.move.cell.row).toEqual(errorTurnNumber - 1);
        });

    });



});