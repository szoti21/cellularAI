package cellularGame;

import static cellularGame.CellState.*;

public class Grid {
    private int[][] grid;
    private int daysLeft;
    public Position playerPosition;
    private float treeDensity;
    private float lionDensity;


    public Grid(int size) {
        grid = new int[size][size];
        daysLeft = 5;
        treeDensity = 0.7f;
        lionDensity = 0.2f;

        initializeGrid(treeDensity, lionDensity);  // Randomize trees, lions, and empty spaces
    }

    private void initializeGrid(float treeDensity, float lionDensity) {
        // Place trees, lions, and empty cells randomly
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(i == (grid.length-1) / 2 && j == (grid[i].length-1) / 2){
                    grid[i][j] = HUMAN.getValue();
                    playerPosition = new Position(4, 4);
                } else {
                    double randomValue = Math.random();
                    if (randomValue < treeDensity) {
                        grid[i][j] = TREE.getValue();
                    } else if (randomValue < treeDensity + lionDensity) {
                        grid[i][j] = LION.getValue();
                    } else {
                        grid[i][j] = EMPTY.getValue();
                    }
                }
            }
        }
    }

    public boolean canMove(Position from){
        return grid[from.getX()][from.getY()] == HUMAN.getValue();
    }

    public void move(Position from, Position direction) {
        Position to = new Position(from.getX() + direction.getX(), from.getY() + direction.getY());

        if(grid[to.getX()][to.getY()] == LION.getValue()){
            //GAME OVER
        } else if (grid[to.getX()][to.getY()] == TREE.getValue()) {
            daysLeft+=2;
        } else {
            daysLeft--;
        }

        grid[from.getX()][from.getY()] = EMPTY.getValue();
        grid[to.getX()][to.getY()] = HUMAN.getValue();
        playerPosition = new Position(to.getX(), to.getY());
    }



    public int getDaysLeft() {
        return daysLeft;
    }

    public void printGrid() {
        String color;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] == EMPTY.getValue()){
                    color = "\u001B[33m";
                } else if (grid[i][j] == TREE.getValue()) {
                    color = "\u001B[32m";
                } else if (grid[i][j] == LION.getValue()){
                    color = "\u001B[31m";
                } else if (grid[i][j] == HUMAN.getValue()){
                    color = "\u001B[37m";
                } else {
                    color = "\u001B[30m";
                }

                System.out.print(color + grid[i][j] + "  ");
            }
            System.out.println();
        }
        System.out.println("\u001B[37m" + "days left: " + daysLeft);
    }
}
