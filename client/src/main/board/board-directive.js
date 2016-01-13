"use strict";

var angular = require("angular");
var _ = require("lodash");
var visibleSize = require("../utils/visible-size");

angular.module("ticTacToe")
    .directive("board", board);



var GRID_COLOR = "#eee";
var PIECE_COLOR = "#000";


function board(GAME_EVENTS, PIECES, $window, $timeout, $log) {
    return {
        restrict: "E",
        template: '<canvas style="margin: auto; display: block"></canvas>',
        scope: {
            numRows: "=",
            numCols: "="
        },
        link: link
    };

    function link($scope, iElem) {

        var canvas = iElem.find("canvas")[0];
        var ctx = canvas.getContext("2d");

        var numRows, numCols;

        var cellSize; // square cell width = height
        var GRID_LINE_WIDTH = 1;

        var canvasWidth, canvasHeight;
        var windowInnerWidth;

        var board;
        var lastTurnResult;


        $scope.$watch("numRows", function(value) {
            numRows = value;
            resetBoard();
            resizeAndDrawCanvas(true);
        });
        $scope.$watch("numCols", function(value) {
            numCols = value;
            resetBoard();
            resizeAndDrawCanvas(true);
        });

        $window.addEventListener("resize", _.debounce(function() {
            resizeAndDrawCanvas(false);
        }, 150));

        $scope.$on(GAME_EVENTS.GAME_STARTED, onGameStarted);
        $scope.$on(GAME_EVENTS.MOVE_COMPLETED, onMoveCompleted);
        $scope.$on(GAME_EVENTS.RESIZE_BOARD, function() {
            resizeAndDrawCanvas(true);
        });
        $scope.$on(GAME_EVENTS.SHOW_LAST_MOVE, blinkLastMove);

        canvas.onclick = onCanvasClick;


        //---------------------------------------------------//


        function resetBoard() {
            board = undefined;
            lastTurnResult = undefined;
            if (numRows > 0 && numCols > 0) {
                board = [];
                for (var i = 0; i < numRows; i++) {
                    board[i] = [];
                    for (var j = 0; j < numCols; j++) {
                        board[i][j] = null;
                    }
                }
            }
        }


        function resizeAndDrawCanvas(force) {
            if (!force && windowInnerWidth === window.innerWidth) {
                return;
            }

            if (numRows && numCols) {
                resizeCanvas();
                drawGameBoard();
                windowInnerWidth = window.innerWidth;
            }
            if (board) {
                drawPieces();
            }
        }


        function resizeCanvas() {
            // We assume that the parent container can adapt to content vertically but not horizontally.

            // 1st pass: resize to available width
            cellSize = _.floor(canvas.parentNode.clientWidth/numCols); // square cells
            canvasWidth = numCols*cellSize;
            canvasHeight = numRows*cellSize;

            // also clears canvas
            canvas.width = canvasWidth;
            canvas.height = canvasHeight;

            // 2nd pass: resize to visible height
            var visibleHeight = visibleSize(canvas).height;

            cellSize = _.floor(Math.min(canvas.clientWidth/numCols, visibleHeight/numRows));
            canvasWidth = numCols*cellSize;
            canvasHeight = numRows*cellSize;

            // also clears canvas
            canvas.width = canvasWidth;
            canvas.height = canvasHeight;
        }

        function drawGameBoard() {
            ctx.strokeStyle = GRID_COLOR;
            ctx.lineWidth = GRID_LINE_WIDTH;

            // Draw horizontal lines
            for (var i = 0; i < numRows - 1; i++) {
                ctx.beginPath();
                ctx.moveTo(0, cellSize*(i + 1));
                ctx.lineTo(canvas.width, cellSize*(i + 1));
                ctx.stroke();
            }

            // Draw vertical lines
            for (var j = 0; j < numCols - 1; j++) {
                ctx.beginPath();
                ctx.moveTo(cellSize*(j + 1), 0);
                ctx.lineTo(cellSize*(j + 1), canvas.height);
                ctx.stroke();
            }

        }


        function drawPieces() {
            for (var i = 0; i < numRows; i++) {
                for (var j = 0; j < numCols; j++) {
                    var cell = {row: i, column: j};
                    if (board[i][j] === PIECES.X) {
                        drawCross(cell);
                    } else if (board[i][j] === PIECES.O) {
                        drawCircle(cell);
                    }
                }
            }
            if (lastTurnResult && lastTurnResult.winningSequence) {
                drawLine(lastTurnResult.winningSequence.start, lastTurnResult.winningSequence.end);
            }
        }

        function onGameStarted(event, game) {
            numRows = game.board.length;
            numCols = game.board[0].length;
            board = _.cloneDeep(game.board);

            resizeCanvas();
            drawGameBoard();
        }


        function onMoveCompleted(event, result) {
            lastTurnResult = result;

            board[result.move.cell.row][result.move.cell.column] = result.move.piece;

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
            ctx.moveTo(cell.column*cellSize + 0.2*cellSize, cell.row*cellSize + 0.2*cellSize);
            ctx.lineTo((cell.column + 1)*cellSize - 0.2*cellSize, (cell.row + 1)*cellSize - 0.2*cellSize);

            // Upper right to to lower left
            ctx.moveTo((cell.column + 1)*cellSize - 0.2*cellSize, cell.row*cellSize + 0.2*cellSize);
            ctx.lineTo(cell.column*cellSize + 0.2*cellSize, (cell.row + 1)*cellSize - 0.2*cellSize);

            ctx.lineWidth = 3;
            ctx.stroke();
        }


        function drawCircle(cell) {
            ctx.strokeStyle = PIECE_COLOR;

            ctx.beginPath();
            ctx.arc((cell.column + 1/2)*cellSize, (cell.row + 1/2)*cellSize, (cellSize - 3)/2 - 0.1*cellSize, 0, 2*Math.PI);
            ctx.lineWidth = 3;
            ctx.stroke();
        }


        function drawLine(start, end) {

            var startX, startY, endX, endY;

            startX = (start.column + 1/2)*cellSize;
            startY = (start.row + 1/2)*cellSize;
            endX = (end.column + 1/2)*cellSize;
            endY = (end.row + 1/2)*cellSize;

            ctx.beginPath();
            ctx.moveTo(startX, startY);
            ctx.lineTo(endX, endY);

            ctx.strokeStyle = "yellow";
            ctx.lineWidth = 4;
            ctx.stroke();
        }

        function clearCell(cell) {
            ctx.clearRect(cell.column*cellSize + GRID_LINE_WIDTH, cell.row*cellSize + GRID_LINE_WIDTH, cellSize - 2*GRID_LINE_WIDTH, cellSize - 2*GRID_LINE_WIDTH);
        }


        function blinkLastMove() {
            // grab refs so they cannot change in the middle of play
            var piece = lastTurnResult.move.piece;
            var cell = lastTurnResult.move.cell;
            var winningSequence = lastTurnResult.winningSequence;

            var numBlinks = 0;
            var pieceDrawFunction;
            if (piece === PIECES.X) {
                pieceDrawFunction = drawCross;
            }  else {
                pieceDrawFunction = drawCircle;
            }
            var drawFunction = function() {
                pieceDrawFunction(cell);
                if (winningSequence) {
                    drawLine(winningSequence.start, winningSequence.end);
                }
            };

            blink();

            function blink() {
                return blinkOnce().then(function() {
                    numBlinks++;
                    if (numBlinks < 3) {
                        return $timeout(blink, 300, false);
                    }
                });
            }

            function blinkOnce() {
                clearCell(cell);
                return $timeout(drawFunction, 300, false, cell);
            }
        }

        function onCanvasClick(e) {
            var cc = getCanvasCoordinates(e);
            var cell = getCellCoordinates(cc);

            $scope.$emit(GAME_EVENTS.MOVE_SELECTED, cell);
        }


        function getCanvasCoordinates(clickEvent) {
            var rect = canvas.getBoundingClientRect();
            return {
                x: clickEvent.pageX - rect.left,
                y: clickEvent.pageY - rect.top
            };
        }


        function getCellCoordinates(canvasCoordinates) {
            return {
                row: _.floor(canvasCoordinates.y/cellSize),
                column: _.floor(canvasCoordinates.x/cellSize)
            };
        }
    }
}









