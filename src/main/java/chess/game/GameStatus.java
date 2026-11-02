package chess.game;

public enum GameStatus {
    ACTIVE,
    CHECKMATE,
    STALEMATE,
    DRAW_THREEFOLD,
    DRAW_FIFTY_MOVE,
    DRAW_INSUFFICIENT_MATERIAL,
    RESIGNED
}
