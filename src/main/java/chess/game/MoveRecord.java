package chess.game;

import chess.model.PieceColor;

public record MoveRecord(int moveNumber, PieceColor color, String notation) {}
