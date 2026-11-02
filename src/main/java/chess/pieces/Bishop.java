package chess.pieces;

import chess.model.*;

public final class Bishop extends Piece {
    public Bishop(PieceColor color) { super(color); }
    @Override public PieceType getType() { return PieceType.BISHOP; }
    @Override public boolean isValidMove(Position from, Position to) {
        int dr = Math.abs(to.row() - from.row());
        int dc = Math.abs(to.column() - from.column());
        return dr > 0 && dr == dc;
    }
}
