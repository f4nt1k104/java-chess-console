package chess.cli;

import chess.game.ChessGame;
import chess.game.GameStatus;
import chess.game.MoveRecord;
import chess.model.Move;
import chess.model.PieceColor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Human-vs-human console UI; does not contain any chess rule logic. */
public final class ConsoleGame {
    private ChessGame game = new ChessGame();

    public void run() {
        System.out.println("=== JAVA CHESS | Консольные шахматы ===");
        showHelp();
        showPosition();

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\n" + game.getTurn().displayName() + " > ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\nВыход из игры.");
                    return;
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                String command = input.toLowerCase(Locale.ROOT);
                try {
                    switch (command) {
                        case "exit", "quit", "выход" -> {
                            System.out.println("До встречи!");
                            return;
                        }
                        case "help", "помощь" -> showHelp();
                        case "board", "доска" -> showPosition();
                        case "history", "история" -> showHistory();
                        case "moves", "ходы" -> showLegalMoves();
                        case "fen" -> System.out.println(game.getFen());
                        case "new", "restart", "новая" -> {
                            game = new ChessGame();
                            System.out.println("Начата новая партия.");
                            showPosition();
                        }
                        case "resign", "сдаться" -> {
                            game.resign();
                            System.out.println(game.getStatusMessage());
                        }
                        default -> {
                            Move move = parseMoveOrCastling(input);
                            game.playMove(move);
                            showPosition();
                            if (game.getStatus() != GameStatus.ACTIVE) {
                                System.out.println("Введите new, чтобы сыграть снова, или exit для выхода.");
                            }
                        }
                    }
                } catch (IllegalArgumentException exception) {
                    System.out.println("Ошибка: " + exception.getMessage());
                }
            }
        }
    }

    private Move parseMoveOrCastling(String input) {
        String normalized = input.trim().toLowerCase(Locale.ROOT).replace('0', 'o');
        if (normalized.equals("o-o") || normalized.equals("o-o-o")) {
            String row = game.getTurn() == PieceColor.WHITE ? "1" : "8";
            return Move.parse("e" + row + (normalized.equals("o-o") ? "g" : "c") + row);
        }
        return Move.parse(input);
    }

    private void showPosition() {
        System.out.print(game.getBoard());
        System.out.println(game.getStatusMessage());
    }

    private void showHistory() {
        List<MoveRecord> history = game.getHistory();
        if (history.isEmpty()) {
            System.out.println("Пока не было ходов.");
            return;
        }
        System.out.println("История ходов (координатная запись):");
        for (MoveRecord move : history) {
            System.out.print(move.color() == PieceColor.WHITE
                    ? move.moveNumber() + ". " + move.notation()
                    : " " + move.notation() + "\n");
        }
        if (history.getLast().color() == PieceColor.WHITE) {
            System.out.println();
        }
    }

    private void showLegalMoves() {
        List<String> moves = game.getLegalMoves();
        if (moves.isEmpty()) {
            System.out.println("Нет доступных ходов.");
        } else {
            System.out.println("Допустимые ходы (" + moves.size() + "): "
                    + String.join(", ", moves));
        }
    }

    private void showHelp() {
        System.out.println("""
                Игра для двух человек за одним компьютером.
                Введите ход: e2e4, g1f3, e7e8q (превращение в ферзя).
                Можно писать с пробелом: e2 e4. Без буквы превращения — ферзь.
                Рокировка: O-O или O-O-O (либо e1g1 / e1c1).
                Команды: help, board, moves, history, fen, resign, new, exit.
                Фигуры: K — король, Q — ферзь, R — ладья,
                        B — слон, N — конь, P — пешка.
                """);
    }
}
