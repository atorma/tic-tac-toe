"use strict";

var angular = require("angular");

angular.module("ticTacToe")
    .config(configureAngularRouter)
    .config(configureUiRouter);

function configureAngularRouter($urlRouterProvider) {

    $urlRouterProvider.otherwise("/");

}

function configureUiRouter($stateProvider) {

    $stateProvider.state("gameConfig", {
        url: "/"
    });

    $stateProvider.state("gamePlay", {
        url: "/games/:id"
    });

}