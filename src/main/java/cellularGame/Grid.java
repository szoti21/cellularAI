package cellularGame;

import ai.djl.Model;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.training.Trainer;

import java.util.Arrays;
import java.util.Random;

import static cellularGame.CellState.*;
import static cellularGame.GameConfiguration.GRID_SIZE;

public class Grid {
    private int[][] grid;
    private int daysLeft;
    private int score;
    public Position playerPosition;
    private float treeDensity;
    private float lionDensity;


    public Grid(int size) {
        grid = new int[size][size];
        daysLeft = 5;
        treeDensity = 0.7f;
        lionDensity = 0.2f;
        score = 0;

        initializeGrid(treeDensity, lionDensity);  // Randomize trees, lions, and empty spaces
    }

    private void initializeGrid(float treeDensity, float lionDensity) {
        // Place trees, lions, and empty cells randomly
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(i == (grid.length-1) / 2 && j == (grid[i].length-1) / 2){
                    grid[i][j] = HUMAN.getValue();
                    playerPosition = new Position(GRID_SIZE/2, GRID_SIZE/2);
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

    public boolean canMove(Position from, Position direction){
        Position to = new Position(from.getX() + direction.getX(), from.getY() + direction.getY());
        if (to.getX() < grid.length &&
                to.getY() < grid[0].length &&
                to.getX() >= 0 &&
                to.getY() >= 0 &&
                grid[from.getX()][from.getY()] == HUMAN.getValue()){
            return true;
        }else {
            System.out.println("Can't move");
            return false;
        }

    }


    public void move(Position from, Position direction) {
        Position to = new Position(from.getX() + direction.getX(), from.getY() + direction.getY());

        if(grid[to.getX()][to.getY()] == LION.getValue()){
            daysLeft = 0;
        } else if (grid[to.getX()][to.getY()] == TREE.getValue()) {
            daysLeft+=2;
            score++;
        } else {
            daysLeft--;
            score++;
        }
        grid[from.getX()][from.getY()] = EMPTY.getValue();
        grid[to.getX()][to.getY()] = HUMAN.getValue();
        playerPosition = new Position(to.getX(), to.getY());
    }

    public boolean moveLeadsToTree(Position from, Position direction) {
        Position to = new Position(from.getX() + direction.getX(), from.getY() + direction.getY());
        return grid[to.getX()][to.getY()] == TREE.getValue();
    }

    public boolean moveLeadsToLion(Position from, Position direction) {
        Position to = new Position(from.getX() + direction.getX(), from.getY() + direction.getY());
        return grid[to.getX()][to.getY()] == LION.getValue();
    }


    public boolean terminalState() {
        return daysLeft <= 0;
    }

    public int getScore() {
        return score * 10 + daysLeft;
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

    public int[] flattenGrid(){
        int[] flattenedGrid = Arrays.stream(grid)
                .flatMapToInt(Arrays::stream)
                .toArray();

        int[] fullState = new int[flattenedGrid.length + 2];
        System.arraycopy(flattenedGrid, 0, fullState, 0, flattenedGrid.length);
        fullState[flattenedGrid.length] = score;    // Current score
        fullState[flattenedGrid.length + 1] = daysLeft; // Remaining food

        return fullState;
    }
    public static Grid getGridByArray(int[] flatGrid){
        Grid grid = new Grid(GRID_SIZE);
        grid.score = flatGrid[flatGrid.length-2];
        grid.daysLeft = flatGrid[flatGrid.length-1];
        for (int row = 0; row < GRID_SIZE; row++){
            grid.grid[row] = Arrays.copyOfRange(flatGrid, row * GRID_SIZE, (row + 1) * GRID_SIZE);
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid.grid[i][j] == HUMAN.getValue()){
                    grid.playerPosition = new Position(i, j);
                }
            }
        }

        return grid;
    }

}
