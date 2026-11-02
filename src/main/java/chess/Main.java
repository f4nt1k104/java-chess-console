package chess;

import chess.cli.ConsoleGame;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        new ConsoleGame().run();
    }
}
