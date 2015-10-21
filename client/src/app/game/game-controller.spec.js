"use strict";

var angular = require("angular");
require("angular-mocks/ngMock");
require("../tic-tac-toe");

describe("GameController", function() {

    var vm;
    var GAME_EVENTS;
    var $scope;

    beforeEach(angular.mock.module("ticTacToe", function($provide) {

    }));


    beforeEach(angular.mock.inject(function(_GAME_EVENTS_, $rootScope, $controller, $q) {
        GAME_EVENTS = _GAME_EVENTS_;
        $scope = $rootScope.$new();

        vm = $controller("GameController", {
            GAME_EVENTS: GAME_EVENTS,
            $scope: $scope
        });

    }));

    it("is defined", function() {
       expect(vm).toBeDefined();
    });

});