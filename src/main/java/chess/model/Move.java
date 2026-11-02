package chess.model;

import java.util.Locale;

/** UCI-style move: e2e4, e7e8q. */
public record Move(Position from, Position to, PieceType promotion) {
    public Move {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Укажите начальную и конечную клетки");
        }
        if (promotion != null && promotion != PieceType.QUEEN && promotion != PieceType.ROOK
                && promotion != PieceType.BISHOP && promotion != PieceType.KNIGHT) {
            throw new IllegalArgumentException("Недопустимая фигура для превращения пешки");
        }
    }

    public Move(Position from, Position to) {
        this(from, to, null);
    }

    public static Move parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Введите ход, например e2e4");
        }
        String text = input.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        if (!text.matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
            throw new IllegalArgumentException("Неверный формат. Пример: e2e4 или e7e8q");
        }
        PieceType promotion = text.length() == 5 ? PieceType.promotionFrom(text.charAt(4)) : null;
        return new Move(Position.parse(text.substring(0, 2)),
                Position.parse(text.substring(2, 4)), promotion);
    }

    /** A promoted pawn becomes a queen by default. */
    public Move withDefaultPromotion(PieceColor movingColor, PieceType movingType) {
        int finalRow = movingColor == PieceColor.WHITE ? 7 : 0;
        if (movingType == PieceType.PAWN && to.row() == finalRow && promotion == null) {
            return new Move(from, to, PieceType.QUEEN);
        }
        return this;
    }

    public String toUci() {
        return from.toString() + to + (promotion == null ? "" : Character.toLowerCase(promotion.letter()));
    }
}
