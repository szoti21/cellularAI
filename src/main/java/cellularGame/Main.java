package cellularGame;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        Grid grid1 = new Grid(10);
        grid1.printGrid();

        for (int i = 0; i < 10; i++) {
            System.out.println();
            String move = sc.nextLine();


            if (move.equals("LEFT") && grid1.canMove(grid1.playerPosition, Moves.LEFT.getValue())){
                grid1.move(grid1.playerPosition, Moves.LEFT.getValue());
            } else if (move.equals("RIGHT") && grid1.canMove(grid1.playerPosition, Moves.RIGHT.getValue())) {
                grid1.move(grid1.playerPosition, Moves.RIGHT.getValue());
            } else if (move.equals("UP") && grid1.canMove(grid1.playerPosition, Moves.UP.getValue())) {
                grid1.move(grid1.playerPosition, Moves.UP.getValue());
            } else if (move.equals("DOWN") && grid1.canMove(grid1.playerPosition, Moves.DOWN.getValue())) {
                grid1.move(grid1.playerPosition, Moves.DOWN.getValue());
            }

            System.out.println();
            grid1.printGrid();
        }



    }
}