package chess.pieces;

import chess.model.*;

public final class King extends Piece {
    public King(PieceColor color) { super(color); }
    @Override public PieceType getType() { return PieceType.KING; }
    @Override public boolean isValidMove(Position from, Position to) {
        int dr = Math.abs(to.row() - from.row());
        int dc = Math.abs(to.column() - from.column());
        return (dr != 0 || dc != 0) && dr <= 1 && dc <= 1;
    }
}
