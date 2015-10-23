"use strict";

var angular = require("angular");
require("angular-material");
require("angular-route");
require("angular-ui-router");

var PIECES = {
    NOUGHT: "O",
    CROSS: "X"
};

var GAME_EVENTS = {
    MOVE_COMPLETED: "moveCompleted",
    MOVE_SELECTED: "moveSelected"
};

angular
    .module("ticTacToe", ["ngMaterial", "ui.router"])
    .constant("GAME_EVENTS", GAME_EVENTS)
    .constant("PIECES", PIECES)
    .run(makeStateAvailableInScope);

require("./board");
require("./game");

// Injection of $state may trigger a GET, which can show as an 
// "Unexpected request" error in your test. Workaround is to 
// $provide a mock $state to Angular.
function makeStateAvailableInScope($rootScope, $state, $stateParams) {
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
}
