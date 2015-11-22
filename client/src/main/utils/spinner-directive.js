"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .directive("spinner", spinner);


var templateFn = _.template([
    '<div class="loader" style="width: <%= size %>px">',
        '<svg class="circular" viewBox="0 0 <%= size %> <%= size %>">',
            '<circle class="path" cx="<%= center %>" cy="<%= center %>" r="<%= radius %>" fill="none" stroke-width="<%= width %>" stroke-miterlimit="10"/>',
        '</svg>',
    '</div>',
].join(''));

function spinner() {
    return {
        restrict: "E",
        template: template
    };

    function template(tElem, tAttrs) {
        var size = tAttrs.size ? parseInt(tAttrs.size) : 24;
        var center = size/2;
        var radius = size/2 - 2;
        var width = _.floor(radius/5);
        return templateFn({size: size, center: center, radius: radius, width: width});
    }
}

