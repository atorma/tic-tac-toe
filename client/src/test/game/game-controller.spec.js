"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");


describe("GameController", function() {

    var vm;
    var GAME_EVENTS, PIECES;

    var gameService;

    var currentGame;
    var deferredMove;

    var $scope, $childScope;
    var $q;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {
    }));

    beforeEach(angular.mock.inject(function(_GAME_EVENTS_, _PIECES_, $rootScope,_$q_) {
        GAME_EVENTS = _GAME_EVENTS_;
        PIECES = _PIECES_;
        $scope = $rootScope.$new();
        $childScope = $scope.$new();
        $q = _$q_;
    }));

    beforeEach(function() {
        currentGame = {};
        currentGame.getMove = function() {
            deferredMove = $q.defer();
            return deferredMove.promise;
        };

        gameService = {
            currentGame: currentGame
        };

    });

    beforeEach(angular.mock.inject(function($controller) {
        vm = $controller("GameController", {
            GAME_EVENTS: GAME_EVENTS,
            gameService: gameService,
            $scope: $scope
        });
    }));


    it("starts by requesting move from the game and broadcasts it for the game board", function() {
        var firstMove = {
            piece: PIECES.CROSS,
            cell: {row: 4, column: 12}
        };
        var broadcastedMove = null;

        $childScope.$on(GAME_EVENTS.MOVE_COMPLETED, function(event, move) {
            broadcastedMove = move;
        });

        vm.startGame();

        deferredMove.resolve(firstMove);
        $scope.$digest();

        expect(broadcastedMove).toEqual(firstMove);
    });


    it("after starting it automatically gets moves and broadcasts them for the game board", function() {
        vm.startGame();
        deferredMove.resolve({});
        $scope.$digest();

        var broadcastedMove;
        $childScope.$on(GAME_EVENTS.MOVE_COMPLETED, function(event, move) {
            broadcastedMove = move;
        });

        for (var i = 1; i <= 10; i++) {
            var move = {
                piece: PIECES.NOUGHT,
                cell: {row: 5, column: 12}
            };
            broadcastedMove = null;

            deferredMove.resolve(move);
            $scope.$digest();

            expect(broadcastedMove).toEqual(move);
        }
    });

    xit("stops when game ends and broadcasts winner (if any) to the game board", function() {

    });

});