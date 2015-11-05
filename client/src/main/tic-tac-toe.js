"use strict";

var angular = require("angular");
require("angular-material");
require("angular-route");
require("angular-ui-router");

var PIECES = {
    X: "X",
    O: "O"
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
    .config(configureIcons)
    .run(makeStateAvailableInScope);

require("./route-config");
require("./board");
require("./game");




function configureIcons($mdIconProvider) {
    $mdIconProvider
        .icon("play", "resources/material-design-icons/ic_play_arrow_black_24px.svg")
        .icon("pause", "resources/material-design-icons/ic_pause_black_24px.svg")
        .icon("stop", "resources/material-design-icons/ic_stop_black_24px.svg")
        .icon("forward", "resources/material-design-icons/ic_forward_black_24px.svg");
}

// Injection of $state may trigger a GET, which can show as an 
// "Unexpected request" error in your test. Workaround is to 
// $provide a mock $state to Angular.
function makeStateAvailableInScope($rootScope, $state, $stateParams) {
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
}
