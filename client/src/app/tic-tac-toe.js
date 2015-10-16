"use strict";

var angular = require("angular");
require("angular-material");
require("angular-route");
require("angular-ui-router");

angular
    .module("ticTacToe", ["ngMaterial", "ui.router"])
    .run(makeStateAvailableInScope);


require("./board");


// Injection of $state may trigger a GET, which can show as an 
// "Unexpected request" error in your test. Workaround is to 
// $provide a mock $state to Angular.
function makeStateAvailableInScope($rootScope, $state, $stateParams) {
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
}
