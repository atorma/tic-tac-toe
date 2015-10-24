"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .directive("board", board);


function board() {
    return {
        restrict: "E",
        template: "<canvas></canvas>",
        scope: {
            size: "@"
        },
        link: link
    };
}


var GRID_COLOR = "#eee";
var PIECE_COLOR = "#000";

function link($scope, iElem, iAttrs) {
    var canvas = iElem.find("canvas")[0];
    var ctx = canvas.getContext("2d");

    var size = _.floor(parseInt(iAttrs.size));
    var numRows = size;
    var numCols = size;

    canvas.width = 400;
    canvas.height = 400;

    var cellWidth = canvas.width/numCols;
    var cellHeight = canvas.height/numRows;

    var currentMethod = drawCross;


    canvas.onclick = onCanvasClick;


    drawGameBoard();


    //---------------------------------------------------//

    function drawGameBoard() {
        ctx.strokeStyle = GRID_COLOR;

        // Draw horizontal lines
        for (var i = 0; i < numRows - 1; i++) {
            ctx.beginPath();
            ctx.moveTo(0, cellHeight*(i + 1));
            ctx.lineTo(canvas.width, cellHeight*(i + 1));
            ctx.stroke();
        }

        // Draw vertical lines
        for (var j = 0; j < numCols - 1; j++) {
            ctx.beginPath();
            ctx.moveTo(cellWidth*(j + 1), 0);
            ctx.lineTo(cellWidth*(j + 1), canvas.height);
            ctx.stroke();
        }

    }


    function drawCross(cell) {
        ctx.strokeStyle = PIECE_COLOR;

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
        ctx.strokeStyle = PIECE_COLOR;

        ctx.beginPath();
        ctx.arc((cell.column + 1/2)*cellWidth, (cell.row + 1/2)*cellWidth, cellWidth/2, 0, 2*Math.PI);
        ctx.stroke();
    }


    function drawLine(start, end) {

        var startX, startY, endX, endY;

        if (start.row === end.row) { // horizontal
            startX = start.column*cellWidth;
            startY = (start.row + 1/2)*cellHeight;
            endX = (end.column + 1)*cellWidth;
            endY = startY;
        } else if (start.column === end.column) { // vertical
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

        ctx.strokeStyle = PIECE_COLOR;
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
        if (currentMethod === drawCross) {
            currentMethod = drawCircle;
        } else {
            currentMethod = drawCross;
        }
    }

    function getCanvasCoordinates(clickEvent) {
        return {
            x: clickEvent.pageX - canvas.offsetLeft,
            y: clickEvent.pageY - canvas.offsetTop
        };
    }

    function getCellCoordinates(canvasCoordinates) {
        return {
            row: _.floor(canvasCoordinates.y/cellHeight),
            column: _.floor(canvasCoordinates.x/cellWidth)
        };
    }
}




