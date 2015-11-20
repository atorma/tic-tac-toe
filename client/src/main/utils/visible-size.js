"use strict";

function getVisibleSize(element) {
    var br = element.getBoundingClientRect();
    var visible = {};

    if (br.top >= 0) {
        visible.height = Math.min(br.height, window.innerHeight - br.top);
    } else {
        visible.height = Math.min(br.bottom, window.innerHeight);
    }

    if (br.left >= 0) {
        visible.width = Math.min(br.width, window.innerWidth - br.left);
    } else {
        visible.width = Math.min(br.right, window.innerWidth);
    }

    return visible;
}

module.exports = getVisibleSize;