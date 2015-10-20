"use strict";

var _ = require("lodash");

var canvas;
var ctx;

var numRows = 18; // TODO parameterize
var numCols = 18; // TODO parameterize

var cellWidth;
var cellHeight;

var gridColor = "#eee";
var pieceColor = "#000";


window.onload = function () {
    canvas = document.getElementById("canvas");
    ctx = canvas.getContext("2d");

    canvas.width = 400;
    canvas.height = 400;

    cellWidth = canvas.width/numCols;
    cellHeight = canvas.height/numRows;

    drawGameBoard();
    drawCross(8, 10);
    drawCircle(10, 8);
    drawLine(0, 0, 5, 0); // vertical
    drawLine(1, 1, 1, 6); // horizontal
    drawLine(2, 2, 7, 7); // diagonal left to right
    drawLine(0, 17, 5, 12); // diagonal right to left
};


function drawGameBoard() {
    ctx.strokeStyle = gridColor;

    // Draw horizontal lines
    for (var i = 0; i < numRows - 1; i++) {
        ctx.beginPath();
        ctx.moveTo(0, cellHeight*(i + 1) + 0.5);
        ctx.lineTo(canvas.width, cellHeight*(i + 1) + 0.5);
        ctx.stroke();
    }

    // Draw vertical lines
    for (var i = 0; i < numCols - 1; i++) {
        ctx.beginPath();
        ctx.moveTo(cellWidth*(i + 1) + 0.5, 0);
        ctx.lineTo(cellWidth*(i + 1) + 0.5, canvas.height);
        ctx.stroke();
    }

}

function drawCross(row, col) {
    ctx.strokeStyle = pieceColor;

    ctx.beginPath();
    ctx.moveTo(col*cellWidth, row*cellHeight);
    ctx.lineTo((col + 1)*cellWidth, (row + 1)*cellHeight);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo((col + 1)*cellWidth, row*cellHeight);
    ctx.lineTo(col*cellWidth, (row + 1)*cellHeight);
    ctx.stroke();
}

function drawCircle(row, col) {
    ctx.strokeStyle = pieceColor;

    ctx.beginPath();
    ctx.arc((col + 1/2)*cellWidth, (row + 1/2)*cellWidth, cellWidth/2, 0, 2*Math.PI);
    ctx.stroke();
}

function drawLine(startRow, startCol, endRow, endCol) {
    ctx.strokeStyle = pieceColor;

    var startX, startY, endX, endY;

    if (startRow == endRow) { // horizontal
        startX = startCol*cellWidth;
        startY = (startRow + 1/2)*cellHeight;
        endX = (endCol + 1)*cellWidth;
        endY = startY;
    } else if (startCol == endCol) { // vertical
        startX = (startCol + 1/2)*cellWidth;
        startY = startRow*cellHeight;
        endX = startX;
        endY = (endRow + 1)*cellHeight;
    } else if (startRow < endRow && startCol < endCol) { // diagonal top-left to bottom-right
        startX = startCol*cellWidth;
        startY = startRow*cellHeight;
        endX = (endCol + 1)*cellWidth;
        endY = (endRow + 1)*cellHeight;
    } else if (startRow < endRow && startCol > endCol) { // diagonal top-right to bottom-left
        startX = (startCol + 1)*cellWidth;
        startY = startRow*cellHeight;
        endX = endCol*cellWidth;
        endY = (endRow + 1)*cellHeight;
    } else {
        var template = _.template("Invalid line (<%= startRow %>, <%= startCol %>) to (<%= endRow %>, <%= endCol %>)");
        throw template({startRow: startRow, startCol: startCol, endRow: endRow, endCol: endCol});
    }

    ctx.beginPath();
    ctx.moveTo(startX, startY);
    ctx.lineTo(endX, endY);
    ctx.stroke();
}