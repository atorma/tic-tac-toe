"use strict";

var _ = require("lodash");

var canvas;
var ctx;

window.onload = function () {
    canvas = document.getElementById("canvas");
    ctx = canvas.getContext("2d");
    ctx.fillRect(10, 10, 20, 30);
};