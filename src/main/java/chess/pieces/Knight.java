package chess.pieces;

import chess.model.*;

public final class Knight extends Piece {
    public Knight(PieceColor color) { super(color); }
    @Override public PieceType getType() { return PieceType.KNIGHT; }
    @Override public boolean isValidMove(Position from, Position to) {
        int dr = Math.abs(to.row() - from.row());
        int dc = Math.abs(to.column() - from.column());
        return (dr == 1 && dc == 2) || (dr == 2 && dc == 1);
    }
}
