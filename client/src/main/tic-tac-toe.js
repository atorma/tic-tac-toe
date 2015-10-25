"use strict";

var angular = require("angular");
require("angular-material");
require("angular-route");
require("angular-ui-router");

var PIECES = {
    NOUGHT: "NOUGHT",
    CROSS: "CROSS"
};

var GAME_EVENTS = {
    GAME_STARTED: "game started",
    MOVE_COMPLETED: "move completed",
    MOVE_SELECTED: "move selected"
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