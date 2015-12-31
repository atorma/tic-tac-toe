"use strict";

var angular = require("angular");
require("angular-material");

var PIECES = {
    X: "X",
    O: "O"
};

var GAME_EVENTS = {
    GAME_STARTED: "game started",
    MOVE_COMPLETED: "move completed",
    MOVE_SELECTED: "move selected"
};

var PLAYER_TYPES = {
    AI: "AI",
    HUMAN: "HUMAN"
};

angular
    .module("ticTacToe", ["ngMaterial"])
    .constant("GAME_EVENTS", GAME_EVENTS)
    .constant("PIECES", PIECES)
    .constant("PLAYER_TYPES", PLAYER_TYPES)
    .config(configureIcons)
    .config(configureThemes);

require("./board");
require("./game");
require("./utils");


function configureIcons($mdIconProvider) {
    $mdIconProvider
        .icon("play", "resources/material-design-icons/ic_play_arrow_black_24px.svg")
        .icon("pause", "resources/material-design-icons/ic_pause_black_24px.svg")
        .icon("stop", "resources/material-design-icons/ic_stop_black_24px.svg")
        .icon("forward", "resources/material-design-icons/ic_forward_black_24px.svg")
        .icon("close", "resources/material-design-icons/ic_close_black_24px.svg")
        .icon("settings", "resources/material-design-icons/ic_settings_black_24px.svg")
    ;
}

function configureThemes($mdThemingProvider) {
    $mdThemingProvider.theme("error");
}