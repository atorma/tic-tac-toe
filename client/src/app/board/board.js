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

var currentMethod = drawCross;


window.onload = function () {
    canvas = document.getElementById("canvas");
    ctx = canvas.getContext("2d");

    canvas.width = 400;
    canvas.height = 400;

    cellWidth = canvas.width/numCols;
    cellHeight = canvas.height/numRows;

    drawGameBoard();
    canvas.onclick = onCanvasClick;
};


function drawGameBoard() {
    ctx.strokeStyle = gridColor;

    // Draw horizontal lines
    for (var i = 0; i < numRows - 1; i++) {
        ctx.beginPath();
        ctx.moveTo(0, cellHeight*(i + 1));
        ctx.lineTo(canvas.width, cellHeight*(i + 1));
        ctx.stroke();
    }

    // Draw vertical lines
    for (var i = 0; i < numCols - 1; i++) {
        ctx.beginPath();
        ctx.moveTo(cellWidth*(i + 1), 0);
        ctx.lineTo(cellWidth*(i + 1), canvas.height);
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

function drawLine(start, end) {
    ctx.strokeStyle = pieceColor;

    var startX, startY, endX, endY;

    if (start.row == end.row) { // horizontal
        startX = start.column*cellWidth;
        startY = (start.row + 1/2)*cellHeight;
        endX = (end.column + 1)*cellWidth;
        endY = startY;
    } else if (start.column == end.column) { // vertical
        startX = (start.column + 1/2)*cellWidth;
        startY = start.row*cellHeight;
        endX = startX;
        endY = (end.row + 1)*cellHeight;
    } else if (start.row < end.row && start.column < end.column) { // diagonal top-left to bottom-right
        startX = start.column*cellWidth;
        startY = start.row*cellHeight;
        endX = (end.column + 1)*cellWidth;
        endY = (end.row + 1)*cellHeight;
    } else if (start.row < end.row && start.column > end.column) { // diagonal top-right to bottom-left
        startX = (start.column + 1)*cellWidth;
        startY = start.row*cellHeight;
        endX = end.column*cellWidth;
        endY = (end.row + 1)*cellHeight;
    } else {
        var template = _.template("Invalid line (<%= startRow %>, <%= startCol %>) to (<%= endRow %>, <%= endCol%>)");
        throw template({startRow: start.row, startCol: start.column, endRow: end.row, endCol: end.column});
    }

    ctx.beginPath();
    ctx.moveTo(startX, startY);
    ctx.lineTo(endX, endY);
    ctx.stroke();
}

function onCanvasClick(e) {
    var cc = getCanvasCoordinates(e);
    var cell = getCellCoordinates(cc);

    // TODO temporary, for testing
    currentMethod(cell);
    if (currentMethod == drawCross) {
        currentMethod = drawCircle;
    } else {
        currentMethod = drawCross;
    }
}

function getCanvasCoordinates(clickEvent) {
    return {
        x: clickEvent.pageX - canvas.offsetLeft,
        y: clickEvent.pageY - canvas.offsetTop
    }
}

function getCellCoordinates(canvasCoordinates) {
    return {
        row: _.floor(canvasCoordinates.y/cellHeight),
        column: _.floor(canvasCoordinates.x/cellWidth)
    }
}