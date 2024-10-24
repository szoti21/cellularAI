package cellularGame;

public enum CellState {
    EMPTY(0),   // Empty space represented by 0
    TREE(1),    // Tree represented by 1
    LION(2),    // Lion represented by 2
    HUMAN(3);  // Person (the player) represented by 3

    private final int value;  // Numeric value for each cell type

    // Constructor to associate the numeric value with the enum constant
    CellState(int value) {
        this.value = value;
    }

    // Getter to retrieve the numeric value
    public int getValue() {
        return value;
    }
}
