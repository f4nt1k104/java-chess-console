package chess.pieces;

import chess.model.*;

public final class Pawn extends Piece {
    public Pawn(PieceColor color) { super(color); }
    @Override public PieceType getType() { return PieceType.PAWN; }
    @Override public boolean isValidMove(Position from, Position to) {
        int dr = (to.row() - from.row()) * getColor().pawnDirection();
        int dc = Math.abs(to.column() - from.column());
        int initialRow = getColor() == PieceColor.WHITE ? 1 : 6;
        return (dc == 0 && (dr == 1 || (dr == 2 && from.row() == initialRow && !hasMoved())))
                || (dc == 1 && dr == 1);
    }
}
