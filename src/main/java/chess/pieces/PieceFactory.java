package chess.pieces;

import chess.model.PieceColor;
import chess.model.PieceType;

public final class PieceFactory {
    private PieceFactory() {}

    public static Piece create(PieceType type, PieceColor color) {
        return switch (type) {
            case KING -> new King(color);
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            case PAWN -> new Pawn(color);
        };
    }
}
