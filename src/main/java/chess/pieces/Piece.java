package chess.pieces;

import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Position;

/** Geometry is defined polymorphically; occupied squares and king safety are checked by ChessGame. */
public abstract class Piece {
    private final PieceColor color;
    private boolean moved;

    protected Piece(PieceColor color) {
        if (color == null) {
            throw new IllegalArgumentException("Цвет фигуры не задан");
        }
        this.color = color;
    }

    public final PieceColor getColor() {
        return color;
    }

    public final boolean hasMoved() {
        return moved;
    }

    public final void markMoved() {
        moved = true;
    }

    public abstract PieceType getType();

    /** Returns true if the movement shape fits this figure (not a complete legal-move check). */
    public abstract boolean isValidMove(Position from, Position to);

    public final char getSymbol() {
        char symbol = getType().letter();
        return color == PieceColor.WHITE ? symbol : Character.toLowerCase(symbol);
    }

    public final Piece copy() {
        Piece copy = PieceFactory.create(getType(), color);
        if (moved) {
            copy.markMoved();
        }
        return copy;
    }
}
