package org.atorma.tictactoe.game.state;

import java.util.*;

/**
 * A representation of a game state.
 */
public class GameState {

    private enum Direction {
        HORIZONTAL, VERTICAL, LEFT_RIGHT_DIAGONAL, RIGHT_LEFT_DIAGONAL
    }

    private int connectHowMany;
    private Board board;
    private Piece nextPlayer;
    private Optional<List<Cell>> allowedMoves = Optional.empty();
    private Sequence longestSequenceX;
    private Sequence longestSequenceO;
    private Map<Piece, List<Sequence>> allSequences;


    public static Builder builder() {
        return new GameState.Builder();
    }

    private GameState() {}


    /**
     * Returns a new state object resulting from a move to given position.
     *
     * @see
     * #update(Cell)
     */
    public GameState next(Cell position) {
        GameState next = this.getCopy();
        next.update(position);
        return next;
    }

    /**
     * Updates THIS state with the given move.
     *
     * @see
     * #next(Cell)
     */
    public void update(Cell position) {
        Piece existingPiece = getPiece(position);
        if (existingPiece != null) {
            throw new IllegalArgumentException("Illegal move: " + position + " already occupied by " + existingPiece);
        }

        board.set(position, nextPlayer);

        nextPlayer = nextPlayer.other();

        findSequencesThatCrossCell(position);

        if (allowedMoves.isPresent()) {
            int positionIndex = Collections.binarySearch(allowedMoves.get(), position, new CellRowOrderComparator());
            allowedMoves.get().remove(positionIndex);
        }
    }

    public GameState getCopy() {
        GameState nextState = new GameState();
        nextState.connectHowMany = this.connectHowMany;
        nextState.nextPlayer = this.nextPlayer;
        nextState.board = copyBoard();
        nextState.longestSequenceX = this.longestSequenceX;
        nextState.longestSequenceO = this.longestSequenceO;
        return nextState;
    }

    private Board copyBoard() {
        return this.board.copy();
    }


    public int getBoardRows() {
        return board.getNumRows();
    }

    public int getBoardCols() {
        return board.getNumCols();
    }

    public int getNumPieces() {
        return board.getNumPieces();
    }

    public Piece getPiece(int row, int col) {
        return board.get(new Cell(row, col));
    }

    public Piece getPiece(Cell position) {
        return board.get(position);
    }


    public int getConnectHowMany() {
        return connectHowMany;
    }

    public Piece getNextPlayer() {
        return nextPlayer;
    }


    /**
     * Checks whether a move in the given cell is allowed,
     * i.e. the cell is within the board's boundaries and unoccupied.
     *
     * @param cell
     * @return
     *  true if the move is allowed
     */
    public boolean isAllowed(Cell cell) {
        if (cell == null) {
            return false;
        }

        if (cell.getRow() >= 0 && cell.getRow() < getBoardRows()
                && cell.getColumn() >= 0 && cell.getColumn() < getBoardCols()
                && getPiece(cell) == null) {
            return true;
        } else {
            return false;
        }
    }

    /** Returns allowed moves sorted first by row, then by column. */
    public List<Cell> getAllowedMoves() {
        if (!allowedMoves.isPresent()) {
            checkAllowedMoves();
        }
        return Collections.unmodifiableList(allowedMoves.get());
    }

    private void checkAllowedMoves() {
        allowedMoves = Optional.of(new ArrayList<>(getBoardRows()*getBoardCols() - getNumPieces()));

        if (getWinner() != null) {
            return;
        }

        for (int i = 0; i < getBoardRows(); i++) {
            for (int j = 0; j <getBoardCols(); j++) {
                Cell cell = new Cell(i, j);
                if (board.get(cell) == null) {
                    allowedMoves.get().add(cell);
                }
            }
        }
    }


    public boolean isTie() {
        return getWinner() == null && getNumPieces() == getBoardRows()*getBoardCols();
    }

    public boolean isAtEnd() {
        return getWinner() != null || isTie();
    }

    /**
     * Returns the winner of this game (if any). The winning sequence
     * is obtained using {@link #getLongestSequence(Piece)}.
     *
     * @return
     *  the winner of this game, null if no winner
     *
     * @see #getLongestSequence(Piece)
     */
    public Piece getWinner() {
        if (longestSequenceX.length == connectHowMany) {
            return Piece.X;
        } else if (longestSequenceO.length == connectHowMany) {
            return Piece.O;
        } else {
            return null;
        }
    }

    public Sequence getLongestSequence(Piece player) {
        if (player == Piece.X) {
            return longestSequenceX;
        } else if ( player == Piece.O) {
            return longestSequenceO;
        } else {
            throw new IllegalArgumentException("Invalid piece " + player);
        }
    }

    public Map<Piece, List<Sequence>> getAllSequences() {
        allSequences = new HashMap<>();
        for (Piece piece : Piece.values()) {
            allSequences.put(piece, new ArrayList<>());
        }
        findSequencesFromScratch();
        Map<Piece, List<Sequence>> retVal = allSequences;
        allSequences = null; // to prevent high memory consumption in case GameStates are stored
        return retVal;
    }

    /**
     * Finds longest sequences by going through the entire game board (4 times).
     * If {@link #allSequences} is not null, finds also all sequences.
     *
     * @see #findSequencesThatCrossCell(Cell)
     */
    private void findSequencesFromScratch() {
        initLongestSequences();

        int checked = 0;
        for (int i = 0; i < getBoardRows(); i++) {
            HorizontalIterator iter = new HorizontalIterator(i, -1);
            checked += findSequencesInLine(iter);
        }
        assert checked == getBoardRows()*getBoardCols();

        checked = 0;
        for (int j = 0; j < getBoardCols(); j++) {
            VerticalIterator iter = new VerticalIterator(-1, j);
            checked += findSequencesInLine(iter);
        }
        assert checked == getBoardRows()*getBoardCols();

        checked = 0;
        for (int j = 0; j < getBoardCols(); j++) { // Upper half
            LeftRightDiagonalIterator iter = new LeftRightDiagonalIterator(-1, j-1);
            checked += findSequencesInLine(iter);
        }
        for (int i = 1; i < getBoardRows(); i++) { // Lower half
            LeftRightDiagonalIterator iter = new LeftRightDiagonalIterator(i-1, -1);
            checked += findSequencesInLine(iter);
        }
        assert checked == getBoardRows()*getBoardCols();

        checked = 0;
        for (int j = getBoardCols() - 1; j >= 0; j--) { // Upper half
            RightLeftDiagonalIterator iter = new RightLeftDiagonalIterator(-1, j+1);
            checked += findSequencesInLine(iter);
        }
        for (int i = 1; i < getBoardRows(); i++) { // Lower half
            RightLeftDiagonalIterator iter = new RightLeftDiagonalIterator(i-1, getBoardCols());
            checked += findSequencesInLine(iter);
        }
        assert checked == getBoardRows()*getBoardCols();

    }

    private void initLongestSequences() {
        longestSequenceX = new Sequence(0, null, null);
        longestSequenceO = new Sequence(0, null, null);
    }

    private int findSequencesInLine(Iterator<Cell> lineIterator) {
        Cell start = lineIterator.next();
        Cell prev;
        Cell end = start;
        Piece current = board.get(start);
        int length = 1;
        int checked = 1;

        while (lineIterator.hasNext()) {
            prev = end;
            end = lineIterator.next();
            Piece piece = board.get(end);
            if (piece == current) {
                length++;
            } else {
                recordSequence(current, length, start, prev);
                start = end;
                length = 1;
                current = piece;
            }
            checked++;
        }
        recordSequence(current, length, start, end);

        return checked;
    }

    private void recordSequence(Piece piece, int length, Cell start, Cell end) {
        if (piece == null) return;

        Sequence sequence = new Sequence(length, start, end);

        if (piece == Piece.X) {
            if (longestSequenceX.length < sequence.length) {
                longestSequenceX = sequence;
            }
        } else {
            if (longestSequenceO.length < sequence.length) {
                longestSequenceO = sequence;
            }
        }

        if (allSequences != null) {
            allSequences.get(piece).add(sequence);
        }
    }


    /**
     * After a move (assuming sequences are up-to-date) we can update sequences by
     * just looking in all the directions starting from the updated cell until
     * we hit a different piece.
     *
     * @param lastMove
     *  an updated cell (all found sequences cross this cell)
     */
    private void findSequencesThatCrossCell(Cell lastMove) {
        findSequencesThatCrossCell(lastMove, Direction.HORIZONTAL);
        findSequencesThatCrossCell(lastMove, Direction.VERTICAL);
        findSequencesThatCrossCell(lastMove, Direction.LEFT_RIGHT_DIAGONAL);
        findSequencesThatCrossCell(lastMove, Direction.RIGHT_LEFT_DIAGONAL);
    }

    private void findSequencesThatCrossCell(Cell lastMove, Direction direction) {
        ListIterator<Cell> iter1, iter2;
        switch (direction) {
            case HORIZONTAL:
                iter1 = new HorizontalIterator(lastMove);
                iter2 = new HorizontalIterator(lastMove);
                break;
            case VERTICAL:
                iter1 = new VerticalIterator(lastMove);
                iter2 = new VerticalIterator(lastMove);
                break;
            case LEFT_RIGHT_DIAGONAL:
                iter1 = new LeftRightDiagonalIterator(lastMove);
                iter2 = new LeftRightDiagonalIterator(lastMove);
                break;
            case RIGHT_LEFT_DIAGONAL:
                iter1 = new RightLeftDiagonalIterator(lastMove);
                iter2 = new RightLeftDiagonalIterator(lastMove);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction");
        }

        Piece piece = board.get(lastMove);
        int length = 1;

        Cell last = lastMove;
        while (iter1.hasNext()) {
            Cell next = iter1.next();
            if (board.get(next) != piece) {
                break;
            }
            length++;
            last = next;
        }

        Cell first = lastMove;
        while (iter2.hasPrevious()) {
            Cell prev = iter2.previous();
            if (board.get(prev) != piece) {
                break;
            }
            length++;
            first = prev;
        }

        recordSequence(piece, length, first, last);
    }


    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.getNumRows(); i++) {
            for (int j = 0; j < board.getNumCols(); j++) {
                Piece piece = board.get(new Cell(i, j));
                String marker;
                if (piece == Piece.X) {
                    marker = " X ";
                } else if (piece == Piece.O) {
                    marker = " O ";
                } else {
                    marker = " - ";
                }
                sb.append(marker);
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public void print() {
        System.out.println(getStringRepresentation());
    }



    public static class Sequence {
        public final int length;
        public final Cell start;
        public final Cell end;

        public Sequence(int length, Cell start, Cell end) {
            this.length = length;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            if (start == null) {
                return "Sequence{" +
                        "length=" + length +
                        '}';
            } else {
                return "Sequence{" +
                        "length=" + length +
                        ", start=(" + start.getRow() + ", " + start.getColumn() + ")" +
                        ", end=(" + end.getRow() + ", " + end.getColumn() + ")" +
                        '}';
            }
        }
    }


    private abstract class IteratorBase implements ListIterator<Cell> {
        protected int row;
        protected int col;

        public final Cell current() {
            return new Cell(row, col);
        }

        public final Cell next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            updateNext();
            return new Cell(row, col);
        }

        /** Update row and column indices to the next position. */
        protected abstract void updateNext();

        public final Cell previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            updatePrevious();
            return new Cell(row, col);
        }

        /** Update row and column indices to the previous position. */
        protected abstract void updatePrevious();

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(Cell cell) {
            throw new UnsupportedOperationException();
        }

        public void add(Cell cell) {
            throw new UnsupportedOperationException();
        }
    }

    /** Iterates horizontally starting from the given position. */
    private class HorizontalIterator extends IteratorBase {

        private HorizontalIterator(Cell cell) {
            this(cell.row, cell.column);
        }

        private HorizontalIterator(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int nextIndex() {
            return col + 1;
        }

        public int previousIndex() {
            return col - 1;
        }

        public boolean hasNext() {
            return nextIndex() < getBoardCols();
        }

        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        protected void updateNext() {
            col++;
        }

        protected void updatePrevious() {
            col--;
        }

    }

    /** Iterates vertically starting from the given position. */
    private class VerticalIterator extends IteratorBase {

        private VerticalIterator(Cell cell) {
            this(cell.row, cell.column);
        }

        private VerticalIterator(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int nextIndex() {
            return row + 1;
        }

        public int previousIndex() {
            return row - 1;
        }

        public boolean hasNext() {
            return nextIndex() < getBoardRows();
        }

        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        protected void updateNext() {
            row++;
        }

        protected void updatePrevious() {
            row--;
        }

    }

    /** Iterates diagonally in upper left - lower right manner starting from the given position. */
    private class LeftRightDiagonalIterator extends IteratorBase {
        private int k;
        private final int diagLength;

        private LeftRightDiagonalIterator(Cell cell) {
            this(cell.row, cell.column);
        }

        private LeftRightDiagonalIterator(int row, int col) {
            this.row = row;
            this.col = col;
            this.k = Math.min(row, col);
            int firstRow = row - k;
            int firstCol = col - k;
            diagLength = Math.min(getBoardRows() - firstRow, getBoardCols() - firstCol);
        }

        public int nextIndex() {
            return k + 1;
        }

        public int previousIndex() {
            return k - 1;
        }

        public boolean hasNext() {
            return nextIndex() < diagLength;
        }

        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        protected void updateNext() {
            k++;
            row++;
            col++;
        }

        protected void updatePrevious() {
            k--;
            row--;
            col--;
        }

    }

    /** Iterates diagonally in upper right - lower left manner starting from the given position. */
    private class RightLeftDiagonalIterator extends IteratorBase {
        private int k;
        private final int diagLength;

        private RightLeftDiagonalIterator(Cell cell) {
            this(cell.row, cell.column);
        }

        private RightLeftDiagonalIterator(int row, int col) {
            this.row = row;
            this.col = col;
            this.k = Math.min(row, getBoardCols() - 1 - col);
            int firstRow = row - k;
            int firstCol = col + k;
            diagLength = Math.min(getBoardRows() - firstRow, firstCol + 1);
        }

        public int nextIndex() {
            return k + 1;
        }

        public int previousIndex() {
            return k - 1;
        }

        public boolean hasNext() {
            return nextIndex() < diagLength;
        }

        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        protected void updateNext() {
            k++;
            row++;
            col--;
        }

        protected void updatePrevious() {
            k--;
            row--;
            col++;
        }

    }

    public static class Builder {

        private Integer connectHowMany = null;
        private Piece[][] board = null;
        private Piece nextPlayer = null;
        private GameState template = null;

        /**
         * @param connectHowMany
         *  how many pieces need to be connected to win
         * @return
         *  this
         */
        public Builder setConnectHowMany(int connectHowMany) {
            this.connectHowMany = connectHowMany;
            return this;
        }

        /**
         * @param board
         *  state of the game board
         * @return
         *  this
         */
        public Builder setBoard(Piece[][] board) {
            this.board = board;
            return this;
        }

        /**
         * @param nextPlayer
         *  player whose turn is next
         * @return
         *  this
         */
        public Builder setNextPlayer(Piece nextPlayer) {
            this.nextPlayer = nextPlayer;
            return this;
        }

        /**
         * @param template
         *  template with which to initialize the new state
         * @return
         *  this
         */
        public Builder setTemplate(GameState template) {
            this.template = template;
            return this;
        }

        public GameState build() {
            GameState state;
            boolean fullInit;

            if (template != null) {
                state = template.getCopy();
                fullInit = false;
                if (board != null) {
                    state.board = new DenseArrayBoard(board);
                    fullInit = true;
                }
                if (connectHowMany != null) {
                    state.connectHowMany = connectHowMany;
                }
                if (nextPlayer != null) {
                    state.nextPlayer = nextPlayer;
                }
            } else {
                if (board == null) {
                    throw new IllegalArgumentException("Initial board is undefined");
                }
                if (connectHowMany == null) {
                    throw new IllegalArgumentException("Number of pieces to connect to win is undefined");
                }
                if (nextPlayer == null) {
                    throw new IllegalArgumentException("Next player is undefined");
                }

                state = new GameState();
                fullInit = true;
                state.nextPlayer = nextPlayer;
                state.board = new DenseArrayBoard(board);
                state.connectHowMany = connectHowMany;

            }

            if (fullInit) {
                state.findSequencesFromScratch();
            }

            return state;
        }
    }
}
