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
    private List<Cell> allowedMoves;
    private Map<Piece, Sequence> longestSequences;
    private Map<Piece, List<Sequence>> allSequences;

    /**
     * @param connectHowMany
     *  how many pieces need to be connected to win
     * @param board
     *  state of game board
     * @param nextPlayer
     *  player whose turn is next
     */
    public GameState(int connectHowMany, Piece[][] board, Piece nextPlayer) {
        this(connectHowMany, new DenseArrayBoard(board), nextPlayer, false);
    }

    /**
     * @param connectHowMany
     *  how many pieces need to be connected to win
     * @param board
     *  state of game board
     * @param nextPlayer
     *  player whose turn is next
     */
    public GameState(int connectHowMany, Board board, Piece nextPlayer) {
        this(connectHowMany, board, nextPlayer, true);
    }

    /**
     * @param template
     *  state of game board
     * @param nextPlayer
     *  player whose turn is next (overrides template state turn)
     */
    public GameState(GameState template, Piece nextPlayer) {
        GameState copy = template.getCopy();
        this.board = copy.board;
        this.allowedMoves = copy.allowedMoves;
        this.longestSequences = copy.longestSequences;
        this.connectHowMany = copy.connectHowMany;
        this.nextPlayer = nextPlayer;
    }

    private GameState(int connectHowMany, Board board, Piece nextPlayer, boolean copy) {
        if (copy) {
            this.board = board.copy();
        } else {
            this.board = board;
        }
        this.nextPlayer = nextPlayer;
        this.connectHowMany = connectHowMany;

        findSequencesFromScratch();
        checkAllowedMoves();
    }

    private GameState() {
        // For move() method
    }


    public int getBoardRows() {
        return board.getNumRows();
    }

    public int getBoardCols() {
        return board.getNumCols();
    }

    public int getConnectHowMany() {
        return connectHowMany;
    }

    public Piece getNextPlayer() {
        return nextPlayer;
    }

    public Piece getPiece(int row, int col) {
        return board.get(new Cell(row, col));
    }

    public Piece getPiece(Cell position) {
        return board.get(position);
    }

    public int getNumPieces() {
        return getBoardRows()*getBoardCols() - getAllowedMoves().size();
    }

    /** Returns allowed moves sorted first by row, then by column. */
    public List<Cell> getAllowedMoves() {
        return Collections.unmodifiableList(allowedMoves);
    }

    public boolean isAllowed(Cell move) {
        return Collections.binarySearch(this.allowedMoves, move, new CellRowOrderComparator()) >= 0;
    }

    public boolean isTie() {
        return getWinner() == null && getAllowedMoves().isEmpty();
    }

    public boolean isAtEnd() {
        return getWinner() != null || isTie();
    }


    private void checkAllowedMoves() {
        allowedMoves = new ArrayList<>(getBoardRows() * getBoardCols());

        if (getWinner() != null) {
            return;
        }

        for (int i = 0; i < board.getNumRows(); i++) {
            for (int j = 0; j < board.getNumCols(); j++) {
                if (board.get(new Cell(i, j)) == null) {
                    allowedMoves.add(new Cell(i, j));
                }
            }
        }
    }

    public GameState getCopy() {
        GameState nextState = new GameState();
        nextState.connectHowMany = this.connectHowMany;
        nextState.nextPlayer = this.nextPlayer;
        nextState.board = this.board.copy();
        nextState.allowedMoves = new ArrayList<>(this.allowedMoves);
        nextState.longestSequences = new EnumMap<>(this.longestSequences);
        return nextState;
    }

    /**
     * Returns a new GameState resulting from a move to given position.
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

        board.set(position, this.nextPlayer);

        this.nextPlayer = this.nextPlayer.other();

        int positionIndex = Collections.binarySearch(this.allowedMoves, position, new CellRowOrderComparator());
        this.allowedMoves.remove(positionIndex);

        findSequencesThatCrossCell(position);
    }


    public Map<Piece, Sequence> getLongestSequences() {
        return Collections.unmodifiableMap(longestSequences);
    }

    public Sequence getLongestSequence(Piece player) {
        return longestSequences.get(player);
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
        if (longestSequences.get(Piece.X).length == connectHowMany) {
            return Piece.X;
        } else if (longestSequences.get(Piece.O).length == connectHowMany) {
            return Piece.O;
        } else {
            return null;
        }
    }

    /**
     * @return
     *  all sequences on the game board
     */
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
     * Finds longest sequences ({@link #longestSequences}) by going through the entire game board (4 times).
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
        longestSequences = new EnumMap<>(Piece.class);
        for (Piece piece : Piece.values()) {
            longestSequences.put(piece, new Sequence(0, null, null));
        }
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

        if (longestSequences.get(piece).length < length) {
            longestSequences.put(piece, sequence);
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
}
