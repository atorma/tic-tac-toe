"use strict";

var angular = require("angular");

angular.module("ticTacToe")
    .factory("spinnerOverlay", spinnerOverlay);


/**
 * Usage: spinnerOverlay(elementId)
 *
 * Creates an object with functions show() and hide().
 * show() overlays a spinner centered in the given element.
 * The element must be *positioned*.
 *
 */
function spinnerOverlay($compile) {

    return create;


    function create(elementId, diameter) {

        var NO_OP = {
            show: function() {},
            hide: function() {}
        };
        var container;
        var overlay;
        var $scope;

        if (!document) {
            return NO_OP;
        }

        container = document.getElementById(""+elementId);
        if (!container) {
            return NO_OP;
        }
        container = angular.element(container);
        $scope = container.scope();


        return {
            show: show,
            hide: hide
        };


        function show() {
            if (!overlay) {
                createOverlay();
            }
        }

        function hide() {
            if (overlay) {
                overlay.remove();
                overlay = undefined;
            }
        }

        function createOverlay() {
            var spinnerTpl = angular.element('<md-progress-circular md-mode="indeterminate" md-diameter="100px">');
            if (diameter !== undefined) {
                spinnerTpl.attr("md-diameter", diameter);
            }
            var overlayTpl = angular.element('<div class="centered">');
            overlayTpl.append(spinnerTpl);

            overlay = $compile(overlayTpl)($scope);
            container.append(overlay);
        }
    }

}