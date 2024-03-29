package org.atorma.tictactoe.game.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public record OccupiedCell(Piece piece, Cell cell) {


    @JsonValue
    public Map<String, Object> getJsonValue() {
        var map = new HashMap<String, Object>();
        map.put("row", cell.getRow());
        map.put("column", cell.getColumn());
        map.put("piece", piece);
        return map;
    }

    @JsonCreator
    public OccupiedCell(@JsonProperty("row") int row,
                        @JsonProperty("column") int column,
                        @JsonProperty("piece") Piece piece) {
        this(piece, new Cell(row, column));
    }
}
