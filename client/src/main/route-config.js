"use strict";

var angular = require("angular");

angular.module("ticTacToe")
	.config(config);

function config($urlRouterProvider) {
  
  $urlRouterProvider.otherwise("/");
  
}