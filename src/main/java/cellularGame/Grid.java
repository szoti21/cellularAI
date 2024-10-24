package cellularGame;

public class Grid {
    private int[][] grid;
    private int personHP;
    private float treeDensity;
    private float lionDensity;


    public Grid(int size) {
        grid = new int[size][size];
        personHP = 10;  // Starting HP
        treeDensity = 0.7f;
        lionDensity = 0.2f;

        initializeGrid(treeDensity, lionDensity);  // Randomize trees, lions, and empty spaces
    }

    private void initializeGrid(float treeDensity, float lionDensity) {
        // Place trees, lions, and empty cells randomly
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(i == (grid.length-1) / 2 && j == (grid[i].length-1) / 2){
                    grid[i][j] = CellType.HUMAN.getValue();
                } else {
                    double randomValue = Math.random();
                    if (randomValue < treeDensity) {
                        grid[i][j] = CellType.TREE.getValue();
                    } else if (randomValue < treeDensity + lionDensity) {
                        grid[i][j] = CellType.LION.getValue();
                    } else {
                        grid[i][j] = CellType.EMPTY.getValue();
                    }
                }
            }
        }
    }

    /*
    public boolean movePerson(int dx, int dy) {
        int newX = personX + dx;
        int newY = personY + dy;

        if (newX >= 0 && newY >= 0 && newX < grid.length && newY < grid[0].length) {
            personHP--;  // Movement costs 1 HP
            CellType nextCell = grid[newX][newY];
            switch (nextCell) {
                case TREE:
                    personHP++;  // Gain 1 HP
                    break;
                case LION:
                    return false;  // Person dies
            }
            // Move the person
            grid[personX][personY] = CellType.EMPTY;
            personX = newX;
            personY = newY;
            grid[personX][personY] = CellType.PERSON;
            return true;
        }
        return true;
    }

     */

    public int getPersonHP() {
        return personHP;
    }

    public void printGrid() {
        String color;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] == CellType.EMPTY.getValue()){
                    color = "\u001B[33m";
                } else if (grid[i][j] == CellType.TREE.getValue()) {
                    color = "\u001B[32m";
                } else if (grid[i][j] == CellType.LION.getValue()){
                    color = "\u001B[31m";
                } else if (grid[i][j] == CellType.HUMAN.getValue()){
                    color = "\u001B[37m";
                } else {
                    color = "\u001B[30m";
                }

                System.out.print(color + grid[i][j] + "  ");
            }
            System.out.println();
        }
    }
}
