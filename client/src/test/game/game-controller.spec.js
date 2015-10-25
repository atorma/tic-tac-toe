"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");


describe("GameController", function() {

    var vm;
    var GAME_EVENTS, PIECES;

    var gameService;

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
            "startNewGame"
        ]);
        gameService.startNewGame.and.returnValue($q.when());
    });

    beforeEach(angular.mock.inject(function($controller) {
        vm = $controller("GameController", {
            GAME_EVENTS: GAME_EVENTS,
            gameService: gameService,
            $scope: $scope
        });
    }));



    describe("on starting new game", function() {

        beforeEach(function() {
            gameService.currentGame = {
                playTurn: function() {
                    return $q.when({
                        gameEnded: true
                    });
                }
            };
        });

        it("requests game service to start new game", function() {
            vm.startGame();
            $scope.$digest();

            expect(gameService.startNewGame).toHaveBeenCalled();
        });

        it("broadcasts event about starting new game", function() {
            var broadcastedEvent = null;
            $childScope.$on(GAME_EVENTS.GAME_STARTED, function(event) {
                broadcastedEvent = event;
            });

            vm.startGame();
            $scope.$digest();

            expect(broadcastedEvent.name).toEqual(GAME_EVENTS.GAME_STARTED);
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
                        piece: i%2 === 0 ? PIECES.CROSS : PIECES.NOUGHT,
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
                    piece: PIECES.CROSS,
                    cell: {row: 16, column: 5}
                },
                gameEnded: true,
                winner: PIECES.CROSS,
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