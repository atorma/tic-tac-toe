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
    drawCross({row: 8, column: 10});
    drawCircle({row: 10, column: 8});
    drawLine({row: 0, column: 0}, {row: 4, column: 0}); // vertical
    drawLine({row: 1, column: 1}, {row: 1, column: 5}); // horizontal
    drawLine({row: 2, column: 2}, {row: 6, column: 6}); // diagonal left to right
    drawLine({row: 0, column: 17}, {row: 4, column: 13}); // diagonal right to left
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

function drawCross(cell) {
    ctx.strokeStyle = pieceColor;

    ctx.beginPath();
    ctx.moveTo(cell.column*cellWidth, cell.row*cellHeight);
    ctx.lineTo((cell.column + 1)*cellWidth, (cell.row + 1)*cellHeight);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo((cell.column + 1)*cellWidth, cell.row*cellHeight);
    ctx.lineTo(cell.column*cellWidth, (cell.row + 1)*cellHeight);
    ctx.stroke();
}

function drawCircle(cell) {
    ctx.strokeStyle = pieceColor;

    ctx.beginPath();
    ctx.arc((cell.column + 1/2)*cellWidth, (cell.row + 1/2)*cellWidth, cellWidth/2, 0, 2*Math.PI);
    ctx.stroke();
}

function drawLine(startCell, endCell) {
    ctx.strokeStyle = pieceColor;

    var startX, startY, endX, endY;

    if (startCell.row == endCell.row) { // horizontal
        startX = startCell.column*cellWidth;
        startY = (startCell.row + 1/2)*cellHeight;
        endX = (endCell.column + 1)*cellWidth;
        endY = startY;
    } else if (startCell.column == endCell.column) { // vertical
        startX = (startCell.column + 1/2)*cellWidth;
        startY = startCell.row*cellHeight;
        endX = startX;
        endY = (endCell.row + 1)*cellHeight;
    } else if (startCell.row < endCell.row && startCell.column < endCell.column) { // diagonal top-left to bottom-right
        startX = startCell.column*cellWidth;
        startY = startCell.row*cellHeight;
        endX = (endCell.column + 1)*cellWidth;
        endY = (endCell.row + 1)*cellHeight;
    } else if (startCell.row < endCell.row && startCell.column > endCell.column) { // diagonal top-right to bottom-left
        startX = (startCell.column + 1)*cellWidth;
        startY = startCell.row*cellHeight;
        endX = endCell.column*cellWidth;
        endY = (endCell.row + 1)*cellHeight;
    } else {
        var template = _.template("Invalid line (<%= startRow %>, <%= startCol %>) to (<%= endRow %>, <%= endCol%>)");
        throw template({startRow: startCell.row, startCol: startCell.column, endRow: endCell.row, endCol: endCell.column});
    }

    ctx.beginPath();
    ctx.moveTo(startX, startY);
    ctx.lineTo(endX, endY);
    ctx.stroke();
}