"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");


describe("GameController", function() {

    var vm;
    var GAME_EVENTS;
    var gameService;
    var currentGame;
    var $scope;
    var $q;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {

    }));

    beforeEach(angular.mock.inject(function(_GAME_EVENTS_, $rootScope,_$q_) {
        GAME_EVENTS = _GAME_EVENTS_;
        $scope = $rootScope.$new();
        $q = _$q_;
    }));

    beforeEach(function() {
        currentGame = jasmine.createSpyObj("currentGame", [
            "getMove"
        ]);
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


    it("starts by requesting move from the game", function() {
        vm.startGame();
        $scope.$digest();

        expect(currentGame.getMove).toHaveBeenCalled();
    });

});