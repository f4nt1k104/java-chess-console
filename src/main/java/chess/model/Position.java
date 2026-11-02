package chess.model;

import java.util.Locale;

/** A square on the board: row 0 = rank 1, column 0 = file a. */
public record Position(int row, int column) {
    public Position {
        if (row < 0 || row > 7 || column < 0 || column > 7) {
            throw new IllegalArgumentException("Координаты должны быть в пределах доски (0..7)");
        }
    }

    // Also keep the conventional accessor names used in the original learning project.
    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public static Position parse(String text) {
        if (text == null || !text.toLowerCase(Locale.ROOT).matches("[a-h][1-8]")) {
            throw new IllegalArgumentException("Неверная клетка: " + text + ". Пример: e2");
        }
        String square = text.toLowerCase(Locale.ROOT);
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + column) + (char) ('1' + row);
    }
}
