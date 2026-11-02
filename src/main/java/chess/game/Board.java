package chess.game;

import chess.model.*;
import chess.pieces.*;

/** Holds positions and knows which squares are attacked. Rule validation belongs to ChessGame. */
public final class Board {
    private final Piece[][] squares = new Piece[8][8];

    public Board() {
        setupBoard();
    }

    private Board(boolean initialize) {
        if (initialize) {
            setupBoard();
        }
    }

    private void setupBoard() {
        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        for (int column = 0; column < 8; column++) {
            squares[0][column] = PieceFactory.create(backRank[column], PieceColor.WHITE);
            squares[1][column] = new Pawn(PieceColor.WHITE);
            squares[6][column] = new Pawn(PieceColor.BLACK);
            squares[7][column] = PieceFactory.create(backRank[column], PieceColor.BLACK);
        }
    }

    public Piece getPiece(Position position) {
        return squares[position.row()][position.column()];
    }

    // Package-private: moves must first pass the public ChessGame rules engine.
    void setPiece(Position position, Piece piece) {
        squares[position.row()][position.column()] = piece;
    }

    void movePiece(Position from, Position to) {
        Piece piece = getPiece(from);
        if (piece == null) {
            throw new IllegalArgumentException("Нет фигуры на " + from);
        }
        setPiece(to, piece);
        setPiece(from, null);
        piece.markMoved();
    }

    public Board copy() {
        Board board = new Board(false);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                board.squares[row][col] = piece == null ? null : piece.copy();
            }
        }
        return board;
    }

    public boolean isPathClear(Position from, Position to) {
        int rowStep = Integer.compare(to.row(), from.row());
        int colStep = Integer.compare(to.column(), from.column());
        int row = from.row() + rowStep;
        int col = from.column() + colStep;
        while (row != to.row() || col != to.column()) {
            if (squares[row][col] != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    public boolean isKingInCheck(PieceColor color) {
        Position king = null;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    king = new Position(row, col);
                    break;
                }
            }
            if (king != null) {
                break;
            }
        }
        if (king == null) {
            throw new IllegalStateException("На доске отсутствует король: " + color);
        }
        return isSquareAttacked(king, color.opposite());
    }

    public boolean isSquareAttacked(Position target, PieceColor attacker) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece == null || piece.getColor() != attacker) {
                    continue;
                }
                Position from = new Position(row, col);
                if (piece.getType() == PieceType.PAWN) {
                    if (target.row() == row + attacker.pawnDirection()
                            && Math.abs(target.column() - col) == 1) {
                        return true;
                    }
                } else if (piece.isValidMove(from, target)) {
                    if (piece.getType() == PieceType.ROOK || piece.getType() == PieceType.BISHOP
                            || piece.getType() == PieceType.QUEEN) {
                        if (isPathClear(from, target)) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String toFenPlacement() {
        StringBuilder fen = new StringBuilder();
        for (int row = 7; row >= 0; row--) {
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(piece.getSymbol());
                }
            }
            if (empty > 0) {
                fen.append(empty);
            }
            if (row != 0) {
                fen.append('/');
            }
        }
        return fen.toString();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("\n    a b c d e f g h\n  +-----------------+\n");
        for (int row = 7; row >= 0; row--) {
            result.append(row + 1).append(" | ");
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                result.append(piece == null ? '.' : piece.getSymbol()).append(' ');
            }
            result.append("| ").append(row + 1).append('\n');
        }
        result.append("  +-----------------+\n    a b c d e f g h\n");
        result.append("Белые — ЗАГЛАВНЫЕ, чёрные — строчные.\n");
        return result.toString();
    }
}
