"use strict";

var angular = require("angular");
var _ = require("lodash");

angular.module("ticTacToe")
    .directive("board", board);



var GRID_COLOR = "#eee";
var PIECE_COLOR = "#000";


function board(GAME_EVENTS, PIECES) {
    return {
        restrict: "E",
        template: "<canvas></canvas>",
        scope: {
            size: "@"
        },
        link: link
    };

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

        drawGameBoard();

        $scope.$on(GAME_EVENTS.GAME_STARTED, onGameStarted);
        $scope.$on(GAME_EVENTS.MOVE_COMPLETED, onMoveCompleted);

        canvas.onclick = onCanvasClick;

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

        function onGameStarted(event) {
            canvas.width = 400; // resizing clears canvas
            drawGameBoard();
        }

        function onMoveCompleted(event, result) {
            if (result.move.piece === PIECES.O) {
                drawCircle(result.move.cell);
            } else if (result.move.piece === PIECES.X) {
                drawCross(result.move.cell);
            }

            if (result.winningSequence) {
                drawLine(result.winningSequence.start, result.winningSequence.end);
            }
        }

        function drawCross(cell) {
            ctx.strokeStyle = PIECE_COLOR;

            ctx.beginPath();

            // Upper left to lower right
            ctx.moveTo(cell.column*cellWidth + 0.2*cellWidth, cell.row*cellHeight + 0.2*cellHeight);
            ctx.lineTo((cell.column + 1)*cellWidth - 0.2*cellWidth, (cell.row + 1)*cellHeight - 0.2*cellHeight);

            // Upper right to to lower left
            ctx.moveTo((cell.column + 1)*cellWidth - 0.2*cellWidth, cell.row*cellHeight + 0.2*cellHeight);
            ctx.lineTo(cell.column*cellWidth + 0.2*cellWidth, (cell.row + 1)*cellHeight - 0.2*cellHeight);

            ctx.lineWidth = 3;
            ctx.stroke();
        }


        function drawCircle(cell) {
            ctx.strokeStyle = PIECE_COLOR;

            ctx.beginPath();
            ctx.arc((cell.column + 1/2)*cellWidth, (cell.row + 1/2)*cellWidth, (cellWidth - 3)/2 - 0.1*cellHeight, 0, 2*Math.PI);
            ctx.lineWidth = 3;
            ctx.stroke();
        }


        function drawLine(start, end) {

            var startX, startY, endX, endY;

            startX = (start.column + 1/2)*cellWidth;
            startY = (start.row + 1/2)*cellHeight;
            endX = (end.column + 1/2)*cellWidth;
            endY = (end.row + 1/2)*cellHeight;

            ctx.beginPath();
            ctx.moveTo(startX, startY);
            ctx.lineTo(endX, endY);

            ctx.strokeStyle = "yellow";
            ctx.lineWidth = 4;
            ctx.stroke();
        }

        function onCanvasClick(e) {
            var cc = getCanvasCoordinates(e);
            var cell = getCellCoordinates(cc);

            // TODO handle human player move selection
            //drawCross(cell);
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
}









