package cellularGame;

public enum Moves {
    LEFT(new Position(0, -1)),
    RIGHT(new Position(0, 1)),
    UP(new Position(-1, 0)),
    DOWN(new Position(1, 0));

    private final Position value;

    // Constructor to associate the numeric value with the enum constant
    Moves(Position value) {
        this.value = value;
    }

    // Getter to retrieve the numeric value
    public Position getValue() {
        return value;
    }
}
