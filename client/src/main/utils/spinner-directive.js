"use strict";

var angular = require("angular");

angular.module("ticTacToe")
    .directive("spinner", spinner);


var TEMPLATE = [
    '<div class="loader">',
        '<svg class="circular" viewBox="0 0 24 24">',
            '<circle class="path" cx="12" cy="12" r="10" fill="none" stroke-width="2" stroke-miterlimit="10"/>',
        '</svg>',
    '</div>',
].join('');

function spinner() {
    return {
        restrict: "E",
        template: TEMPLATE
    };
}

