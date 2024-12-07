package cellularGame;

public enum Moves {
    LEFT(new Position(0, -1), 0),
    RIGHT(new Position(0, 1), 1),
    UP(new Position(-1, 0), 2),
    DOWN(new Position(1, 0), 3);

    private final Position value;
    private final int action;

    // Constructor to associate the numeric value with the enum constant


    Moves(Position value, int action) {
        this.value = value;
        this.action = action;
    }

    // Getter to retrieve the numeric value
    public Position getValue() {
        return value;
    }

    public static Moves getMoveByAction(int action){
        for (Moves move : Moves.values()){
            if (move.action == action){
                return move;
            }
        }
        return null;
    }

}
