package chess.pieces;

import chess.model.*;

public final class Rook extends Piece {
    public Rook(PieceColor color) { super(color); }
    @Override public PieceType getType() { return PieceType.ROOK; }
    @Override public boolean isValidMove(Position from, Position to) {
        return !from.equals(to) && (from.row() == to.row() || from.column() == to.column());
    }
}
