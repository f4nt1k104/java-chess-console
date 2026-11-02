package chess.model;

public enum PieceColor {
    WHITE(1, "Белые"),
    BLACK(-1, "Чёрные");

    private final int pawnDirection;
    private final String displayName;

    PieceColor(int pawnDirection, String displayName) {
        this.pawnDirection = pawnDirection;
        this.displayName = displayName;
    }

    public int pawnDirection() {
        return pawnDirection;
    }

    public String displayName() {
        return displayName;
    }

    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
