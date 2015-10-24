"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");


describe("gameService", function() {

    var gameService;

    var $q, $scope;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {
    }));

    beforeEach(angular.mock.inject(function(_gameService_, _$q_, $rootScope) {
        gameService = _gameService_;
        $q = _$q_;
        $scope = $rootScope;
    }));


    describe("when new game started", function() {

        it("it sets current game as new game", function() {
            expect(gameService.currentGame).not.toBeDefined();

            gameService.startNewGame();
            $scope.$digest();

            expect(gameService.currentGame).toBeDefined();
        });

    });




});