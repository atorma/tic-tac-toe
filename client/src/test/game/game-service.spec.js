"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");


describe("gameService", function() {

    var PIECE;
    var gameService;
    var initialGameData;

    var $q, $scope, $httpBackend;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {
    }));

    beforeEach(angular.mock.inject(function(_PIECES_, _gameService_, _$q_, $rootScope, _$httpBackend_) {
        PIECE = _PIECES_;
        gameService = _gameService_;
        $q = _$q_;
        $scope = $rootScope;
        $httpBackend = _$httpBackend_;
    }));

    beforeEach(function() {
        initialGameData = {
            id: "game id",
            players: {
                O: {
                    id: "id of player who has the CROSS pieces",
                    name: "Name of player CROSS",
                    type: "AI"
                },
                X: {
                    id: "id of player who has the NOUGHT pieces",
                    name: "Name of player NOUGHT",
                    type: "HUMAN"
                }
            },
            currentPlayer: PIECE.O,
            turnNumber: 1,
            move: null,
            gameEnded: false,
            winner: null,
            winningSequence: null
        };
    });


    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe("when new game started", function() {

        // TODO start game with parameters: player types, board size, who starts
        it("requests backend to create a new game and sets up current game", function() {
            $httpBackend.expectPOST("games", {})
                .respond(201, initialGameData);

            gameService.startNewGame();
            $httpBackend.flush();

            expect(gameService.currentGame).toBeDefined();
            expect(gameService.currentGame.id).toEqual(initialGameData.id);
            expect(gameService.currentGame[PIECE.O]).toEqual(initialGameData[PIECE.O]);
            expect(gameService.currentGame[PIECE.X]).toEqual(initialGameData[PIECE.X]);
            expect(gameService.currentGame.currentPlayer).toEqual(initialGameData.currentPlayer);
            expect(gameService.currentGame.turnNumber).toEqual(initialGameData.turnNumber);
            expect(gameService.currentGame.move).toEqual(initialGameData.move);
            expect(gameService.currentGame.gameEnded).toEqual(initialGameData.gameEnded);
            expect(gameService.currentGame.winner).toEqual(initialGameData.winner);
            expect(gameService.currentGame.winningSequence).toEqual(initialGameData.winningSequence);
        });

        it("ends current game if ongoing", function() {
            spyOn(gameService, "endCurrentGame").and.returnValue($q.when());

            $httpBackend.whenPOST("games", {})
                .respond(201, initialGameData);

            gameService.startNewGame();
            $httpBackend.flush();

            expect(gameService.endCurrentGame).toHaveBeenCalled();
        });
    });

    describe("when turn played", function() {

        var turnResponse;

        beforeEach(function() {
            $httpBackend.whenPOST("games")
               .respond(201, initialGameData);

            gameService.startNewGame();
            $httpBackend.flush();
        });

        beforeEach(function() {
            turnResponse = {
                move: {
                    piece: PIECE.O,
                    cell: {row: 5, column: 12}
                },
                gameEnded: false,
                winner: null,
                winningSequence: null,
                turnNumber: 2,
                currentPlayer: PIECE.X
            };
        });

        it("it sends correct turn request to backend", function() {
            $httpBackend.expectPOST("games/"+initialGameData.id+"/turns", {
                turnNumber: initialGameData.turnNumber
            }).respond(201, turnResponse);

            gameService.currentGame.playTurn();
            $httpBackend.flush();
        });

        it("returns the turn result to the caller", function() {
            $httpBackend.expectPOST("games/"+initialGameData.id+"/turns")
                .respond(201, turnResponse);

            var turnResult = null;
            gameService.currentGame.playTurn()
                .then(function(result) {
                    turnResult = result;
                });
            $httpBackend.flush();

            expect(turnResult).toEqual(turnResponse);
        });

        it("updates the game", function() {
            $httpBackend.expectPOST("games/"+initialGameData.id+"/turns")
                .respond(201, turnResponse);

            gameService.currentGame.playTurn()
            $httpBackend.flush();

            expect(gameService.currentGame.currentPlayer).toEqual(turnResponse.currentPlayer);
            expect(gameService.currentGame.turnNumber).toEqual(turnResponse.turnNumber);
            expect(gameService.currentGame.move).toEqual(turnResponse.move);
            expect(gameService.currentGame.gameEnded).toEqual(turnResponse.gameEnded);
            expect(gameService.currentGame.winner).toEqual(turnResponse.winner);
            expect(gameService.currentGame.winningSequence).toEqual(turnResponse.winningSequence);
        });

    });

    describe("when current game ended", function() {

        it("sends request to delete game to backend and clears current game", function() {
            gameService.currentGame = {
                id: "abc-123"
            };

            $httpBackend.expectDELETE("games/abc-123")
                .respond(203);

            gameService.endCurrentGame();
            $httpBackend.flush();

            expect(gameService.currentGame).toBeUndefined();
        });

    });


});