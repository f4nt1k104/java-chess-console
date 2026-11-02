package chess.game;

import chess.model.*;
import chess.pieces.Piece;
import chess.pieces.PieceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Game rules, turns, special moves, check/checkmate and draw detection. */
public final class ChessGame {
    private Board board = new Board();
    private PieceColor turn = PieceColor.WHITE;
    private GameStatus status = GameStatus.ACTIVE;
    private PieceColor winner;
    private Position enPassantTarget;
    private int halfmoveClock;
    private int fullmoveNumber = 1;

    private final Map<String, Integer> repetitions = new HashMap<>();
    private final List<MoveRecord> history = new ArrayList<>();

    public ChessGame() {
        repetitions.put(positionKey(), 1);
    }

    /** Defensive copy so clients cannot change a live board. */
    public Board getBoard() {
        return board.copy();
    }

    public PieceColor getTurn() {
        return turn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public PieceColor getWinner() {
        return winner;
    }

    public List<MoveRecord> getHistory() {
        return List.copyOf(history);
    }

    public boolean isInCheck() {
        return board.isKingInCheck(turn);
    }

    public void playMove(Move requestedMove) {
        if (status != GameStatus.ACTIVE) {
            throw new IllegalArgumentException("Партия завершена. Введите new для новой игры");
        }
        Piece piece = board.getPiece(requestedMove.from());
        if (piece == null) {
            throw new IllegalArgumentException("На клетке " + requestedMove.from() + " нет фигуры");
        }
        if (piece.getColor() != turn) {
            throw new IllegalArgumentException("Сейчас ходят " + turn.displayName().toLowerCase());
        }
        Move move = requestedMove.withDefaultPromotion(turn, piece.getType());
        validatePseudoLegal(move, turn);

        Board next = board.copy();
        applyMove(next, move, enPassantTarget);
        if (next.isKingInCheck(turn)) {
            throw new IllegalArgumentException("Ход недопустим: ваш король окажется под шахом");
        }

        Piece captured = board.getPiece(move.to());
        boolean capture = captured != null || (piece.getType() == PieceType.PAWN
                && move.from().column() != move.to().column());
        boolean pawnMove = piece.getType() == PieceType.PAWN;
        Position nextEnPassant = null;
        if (pawnMove && Math.abs(move.to().row() - move.from().row()) == 2) {
            nextEnPassant = new Position((move.to().row() + move.from().row()) / 2,
                    move.from().column());
        }

        int moveNumber = fullmoveNumber;
        PieceColor movingColor = turn;
        board = next;
        enPassantTarget = nextEnPassant;
        halfmoveClock = (pawnMove || capture) ? 0 : halfmoveClock + 1;
        if (turn == PieceColor.BLACK) {
            fullmoveNumber++;
        }
        turn = turn.opposite();
        repetitions.merge(positionKey(), 1, Integer::sum);
        updateStatus();

        String notation = move.toUci();
        if (status == GameStatus.CHECKMATE) {
            notation += "#";
        } else if (board.isKingInCheck(turn)) {
            notation += "+";
        }
        history.add(new MoveRecord(moveNumber, movingColor, notation));
    }

    public void resign() {
        if (status != GameStatus.ACTIVE) {
            throw new IllegalArgumentException("Партия уже завершена");
        }
        winner = turn.opposite();
        status = GameStatus.RESIGNED;
    }

    public String getStatusMessage() {
        return switch (status) {
            case ACTIVE -> board.isKingInCheck(turn)
                    ? "Шах! Ходят " + turn.displayName().toLowerCase() + "."
                    : "Ходят " + turn.displayName().toLowerCase() + ".";
            case CHECKMATE -> "Мат! Победили " + winner.displayName().toLowerCase() + ".";
            case STALEMATE -> "Пат — ничья.";
            case DRAW_THREEFOLD -> "Ничья: позиция повторилась три раза.";
            case DRAW_FIFTY_MOVE -> "Ничья: 50 ходов без взятия и движения пешек.";
            case DRAW_INSUFFICIENT_MATERIAL -> "Ничья: недостаточно материала для мата.";
            case RESIGNED -> "Сдача партии. Победили " + winner.displayName().toLowerCase() + ".";
        };
    }

    public List<String> getLegalMoves() {
        if (status != GameStatus.ACTIVE) {
            return List.of();
        }
        List<String> moves = new ArrayList<>();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Position from = new Position(fromRow, fromCol);
                Piece piece = board.getPiece(from);
                if (piece == null || piece.getColor() != turn) {
                    continue;
                }
                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        Position to = new Position(toRow, toCol);
                        Move candidate = new Move(from, to);
                        if (isLegal(candidate, turn)) {
                            if (piece.getType() == PieceType.PAWN && (toRow == 0 || toRow == 7)) {
                                for (PieceType type : new PieceType[]{PieceType.QUEEN,
                                        PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
                                    moves.add(new Move(from, to, type).toUci());
                                }
                            } else {
                                moves.add(candidate.toUci());
                            }
                        }
                    }
                }
            }
        }
        moves.sort(String::compareTo);
        return List.copyOf(moves);
    }

    /** Standard FEN, useful for debugging or setting up a position on an external chess board. */
    public String getFen() {
        String side = turn == PieceColor.WHITE ? "w" : "b";
        String ep = enPassantTarget == null ? "-" : enPassantTarget.toString();
        return board.toFenPlacement() + " " + side + " " + castlingRights()
                + " " + ep + " " + halfmoveClock + " " + fullmoveNumber;
    }

    private void validatePseudoLegal(Move move, PieceColor color) {
        Position from = move.from();
        Position to = move.to();
        Piece piece = board.getPiece(from);
        Piece target = board.getPiece(to);
        if (from.equals(to)) {
            throw new IllegalArgumentException("Исходная и конечная клетки совпадают");
        }
        if (piece == null || piece.getColor() != color) {
            throw new IllegalArgumentException("На исходной клетке нет вашей фигуры");
        }
        if (target != null && target.getColor() == color) {
            throw new IllegalArgumentException("Нельзя взять собственную фигуру");
        }
        if (target != null && target.getType() == PieceType.KING) {
            throw new IllegalArgumentException("Короля не берут: цель игры — поставить мат");
        }

        boolean reachingLastRank = piece.getType() == PieceType.PAWN
                && to.row() == (color == PieceColor.WHITE ? 7 : 0);
        if (move.promotion() != null && !reachingLastRank) {
            throw new IllegalArgumentException("Превращение возможно только на последней горизонтали");
        }

        if (piece.getType() == PieceType.KING && from.row() == to.row()
                && Math.abs(to.column() - from.column()) == 2) {
            validateCastling(from, to, piece);
            return;
        }
        if (!piece.isValidMove(from, to)) {
            throw new IllegalArgumentException("Эта фигура не может так ходить");
        }

        switch (piece.getType()) {
            case PAWN -> validatePawn(from, to, color, target);
            case BISHOP, ROOK, QUEEN -> {
                if (!board.isPathClear(from, to)) {
                    throw new IllegalArgumentException("Путь фигуры перекрыт");
                }
            }
            case KING, KNIGHT -> { /* Shape and capture were checked above. */ }
        }
    }

    private void validatePawn(Position from, Position to, PieceColor color, Piece target) {
        if (from.column() == to.column()) {
            if (target != null) {
                throw new IllegalArgumentException("Пешка не может брать прямо");
            }
            if (Math.abs(to.row() - from.row()) == 2) {
                Position intermediate = new Position(from.row() + color.pawnDirection(), from.column());
                if (board.getPiece(intermediate) != null) {
                    throw new IllegalArgumentException("Путь пешки перекрыт");
                }
            }
        } else if (target == null) {
            if (!to.equals(enPassantTarget)) {
                throw new IllegalArgumentException("Пешка берёт только по диагонали (или на проходе)");
            }
            Piece passedPawn = board.getPiece(new Position(from.row(), to.column()));
            if (passedPawn == null || passedPawn.getType() != PieceType.PAWN
                    || passedPawn.getColor() == color) {
                throw new IllegalArgumentException("Взятие на проходе сейчас невозможно");
            }
        }
    }

    private void validateCastling(Position from, Position to, Piece king) {
        int rank = king.getColor() == PieceColor.WHITE ? 0 : 7;
        if (king.hasMoved() || from.row() != rank || from.column() != 4
                || to.row() != rank || (to.column() != 2 && to.column() != 6)) {
            throw new IllegalArgumentException("Рокировка невозможна: король уже двигался или стоит не на месте");
        }
        boolean kingSide = to.column() == 6;
        Position rookSquare = new Position(rank, kingSide ? 7 : 0);
        Piece rook = board.getPiece(rookSquare);
        if (rook == null || rook.getType() != PieceType.ROOK
                || rook.getColor() != king.getColor() || rook.hasMoved()) {
            throw new IllegalArgumentException("Рокировка невозможна: ладья отсутствует или уже двигалась");
        }
        if (!board.isPathClear(from, rookSquare)) {
            throw new IllegalArgumentException("Рокировка невозможна: между королём и ладьёй есть фигуры");
        }
        PieceColor opponent = king.getColor().opposite();
        int transitColumn = kingSide ? 5 : 3;
        if (board.isSquareAttacked(from, opponent)
                || board.isSquareAttacked(new Position(rank, transitColumn), opponent)
                || board.isSquareAttacked(to, opponent)) {
            throw new IllegalArgumentException("Нельзя рокировать через шах или из-под шаха");
        }
    }

    private static void applyMove(Board state, Move move, Position epTarget) {
        Piece piece = state.getPiece(move.from());
        if (piece.getType() == PieceType.PAWN
                && move.from().column() != move.to().column()
                && state.getPiece(move.to()) == null && move.to().equals(epTarget)) {
            state.setPiece(new Position(move.from().row(), move.to().column()), null);
        }
        if (piece.getType() == PieceType.KING
                && Math.abs(move.from().column() - move.to().column()) == 2) {
            int rank = move.from().row();
            boolean kingSide = move.to().column() == 6;
            state.movePiece(new Position(rank, kingSide ? 7 : 0),
                    new Position(rank, kingSide ? 5 : 3));
        }
        state.movePiece(move.from(), move.to());
        if (piece.getType() == PieceType.PAWN && (move.to().row() == 0 || move.to().row() == 7)) {
            Piece replacement = PieceFactory.create(
                    move.promotion() == null ? PieceType.QUEEN : move.promotion(), piece.getColor());
            replacement.markMoved();
            state.setPiece(move.to(), replacement);
        }
    }

    private boolean isLegal(Move move, PieceColor color) {
        try {
            validatePseudoLegal(move, color);
            Board simulated = board.copy();
            applyMove(simulated, move, enPassantTarget);
            return !simulated.isKingInCheck(color);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean hasAnyLegalMove() {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Position from = new Position(fromRow, fromCol);
                Piece piece = board.getPiece(from);
                if (piece == null || piece.getColor() != turn) {
                    continue;
                }
                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        if (isLegal(new Move(from, new Position(toRow, toCol)), turn)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void updateStatus() {
        if (!hasAnyLegalMove()) {
            if (board.isKingInCheck(turn)) {
                status = GameStatus.CHECKMATE;
                winner = turn.opposite();
            } else {
                status = GameStatus.STALEMATE;
            }
        } else if (insufficientMaterial()) {
            status = GameStatus.DRAW_INSUFFICIENT_MATERIAL;
        } else if (halfmoveClock >= 100) {
            status = GameStatus.DRAW_FIFTY_MOVE;
        } else if (repetitions.getOrDefault(positionKey(), 0) >= 3) {
            status = GameStatus.DRAW_THREEFOLD;
        }
    }

    private boolean insufficientMaterial() {
        List<Position> bishops = new ArrayList<>();
        int knights = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null || piece.getType() == PieceType.KING) {
                    continue;
                }
                if (piece.getType() == PieceType.BISHOP) {
                    bishops.add(new Position(row, col));
                } else if (piece.getType() == PieceType.KNIGHT) {
                    knights++;
                } else {
                    return false;
                }
            }
        }
        if (bishops.isEmpty() && knights == 0) {
            return true;
        }
        if (bishops.size() + knights == 1) {
            return true;
        }
        if (knights > 0) {
            return false;
        }
        int squareColor = (bishops.getFirst().row() + bishops.getFirst().column()) % 2;
        return bishops.stream().allMatch(square -> (square.row() + square.column()) % 2 == squareColor);
    }

    private String castlingRights() {
        StringBuilder rights = new StringBuilder();
        for (PieceColor color : new PieceColor[]{PieceColor.WHITE, PieceColor.BLACK}) {
            int rank = color == PieceColor.WHITE ? 0 : 7;
            Piece king = board.getPiece(new Position(rank, 4));
            if (king == null || king.getType() != PieceType.KING
                    || king.getColor() != color || king.hasMoved()) {
                continue;
            }
            for (int rookCol : new int[]{7, 0}) {
                Piece rook = board.getPiece(new Position(rank, rookCol));
                if (rook != null && rook.getType() == PieceType.ROOK
                        && rook.getColor() == color && !rook.hasMoved()) {
                    if (color == PieceColor.WHITE) {
                        rights.append(rookCol == 7 ? 'K' : 'Q');
                    } else {
                        rights.append(rookCol == 7 ? 'k' : 'q');
                    }
                }
            }
        }
        return rights.isEmpty() ? "-" : rights.toString();
    }

    private String positionKey() {
        String ep = "-";
        if (enPassantTarget != null) {
            int fromRow = enPassantTarget.row() - turn.pawnDirection();
            for (int delta : new int[]{-1, 1}) {
                int fromCol = enPassantTarget.column() + delta;
                if (fromCol >= 0 && fromCol < 8 && fromRow >= 0 && fromRow < 8) {
                    Piece candidate = board.getPiece(new Position(fromRow, fromCol));
                    if (candidate != null && candidate.getType() == PieceType.PAWN
                            && candidate.getColor() == turn) {
                        ep = enPassantTarget.toString();
                        break;
                    }
                }
            }
        }
        return board.toFenPlacement() + " " + turn + " " + castlingRights() + " " + ep;
    }
}
