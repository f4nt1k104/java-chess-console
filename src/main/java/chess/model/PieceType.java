package chess.model;

public enum PieceType {
    KING('K'), QUEEN('Q'), ROOK('R'), BISHOP('B'), KNIGHT('N'), PAWN('P');

    private final char letter;

    PieceType(char letter) {
        this.letter = letter;
    }

    public char letter() {
        return letter;
    }

    public static PieceType promotionFrom(char letter) {
        return switch (Character.toLowerCase(letter)) {
            case 'q' -> QUEEN;
            case 'r' -> ROOK;
            case 'b' -> BISHOP;
            case 'n' -> KNIGHT;
            default -> throw new IllegalArgumentException("Превращение: укажите q, r, b или n");
        };
    }
}
