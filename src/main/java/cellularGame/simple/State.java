package cellularGame.simple;

import ai.djl.modality.rl.ActionSpace;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import cellularGame.CellState;
import cellularGame.GameConfiguration;
import cellularGame.Moves;
import cellularGame.Position;

import java.util.Arrays;
import java.util.List;

import static cellularGame.CellState.*;
import static cellularGame.CellState.HUMAN;

public class State {

        int[][] board;
        int turn;
        int energy;

        public State(int[][] board, int turn, int energy) {
            this.board = board;
            this.turn = turn;
            this.energy = energy;
        }

        //translates the state to an NDLIst
        public NDList getObservation(NDManager manager) {
            int[] boardFlat = Arrays.stream(board)
                    .flatMapToInt(Arrays::stream)
                    .toArray();
            return new NDList(manager.create(boardFlat), manager.create(turn), manager.create(energy));
        }


    public NDList getObservationWithAction(NDManager manager) {
        int[] boardFlat = Arrays.stream(board)
                .flatMapToInt(Arrays::stream)
                .toArray();
        int[][] array = new int[1][boardFlat.length];
        array[0] = boardFlat;

        return new NDList(manager.create(array), manager.create(turn), manager.create(energy), manager.create(1));
    }

        //returns with the moves on the board
        public ActionSpace getActionSpace(NDManager manager) {
            ActionSpace actionSpace = new ActionSpace();

            Position player = getPlayerPos();

            for(Moves move: Moves.values()){
                int x = player.getX() + move.getValue().getX();
                int y = player.getY() + move.getValue().getY();

                if(x >= 0 && x < GameConfiguration.GRID_SIZE && y >= 0 && y < GameConfiguration.GRID_SIZE){
                    actionSpace.add(new NDList(manager.create(move.getAction())));
                }

            }

            return actionSpace;
        }

        public float getScore() {
            return turn * 10 + energy;
        }

        public Position getPlayerPos(){
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if(board[i][j] == HUMAN.getValue()){
                        return new Position(i, j);
                    }

                }
            }

            return null;
        }

        @Override
        public String toString() {
            String color;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if(board[i][j] == EMPTY.getValue()){
                        color = "\u001B[33m";
                    } else if (board[i][j] == TREE.getValue()) {
                        color = "\u001B[32m";
                    } else if (board[i][j] == LION.getValue()){
                        color = "\u001B[31m";
                    } else if (board[i][j] == HUMAN.getValue()){
                        color = "\u001B[37m";
                    } else {
                        color = "\u001B[30m";
                    }

                    sb.append(color + board[i][j] + "  ");
                }
                sb.append("\n");
            }
            sb.append("\u001B[37m" + "days left: " + energy);

            return sb.toString();
        }

    }