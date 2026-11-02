package chess.pieces;

import chess.model.*;

public final class Queen extends Piece {
    public Queen(PieceColor color) { super(color); }
    @Override public PieceType getType() { return PieceType.QUEEN; }
    @Override public boolean isValidMove(Position from, Position to) {
        int dr = Math.abs(to.row() - from.row());
        int dc = Math.abs(to.column() - from.column());
        return (dr != 0 || dc != 0) && (dr == dc || dr == 0 || dc == 0);
    }
}
