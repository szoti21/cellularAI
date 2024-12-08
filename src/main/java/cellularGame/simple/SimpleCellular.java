package cellularGame.simple;

import ai.djl.modality.rl.ActionSpace;
import ai.djl.modality.rl.LruReplayBuffer;
import ai.djl.modality.rl.ReplayBuffer;
import ai.djl.modality.rl.env.RlEnv;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import cellularGame.CellState;
import cellularGame.GameConfiguration;
import cellularGame.Moves;
import cellularGame.Position;

import java.util.Arrays;

import static cellularGame.CellState.*;
import static cellularGame.CellState.EMPTY;
import static cellularGame.GameConfiguration.GRID_SIZE;

public class SimpleCellular implements RlEnv {

    private NDManager manager;
    private State state;
    private ReplayBuffer replayBuffer;

    public SimpleCellular(NDManager manager, int batchSize, int replayBufferSize) {
        this(manager, new LruReplayBuffer(batchSize, replayBufferSize));
    }

    public SimpleCellular(NDManager manager, ReplayBuffer replayBuffer) {
        this.manager = manager;
        this.state = new State(new int[GameConfiguration.GRID_SIZE][GameConfiguration.GRID_SIZE], 1, GameConfiguration.INITIAL_ENERGY);
        initializeGrid();
        this.replayBuffer = replayBuffer;
    }

    public SimpleCellular(NDManager manager) {
        this.state = new State(new int[GameConfiguration.GRID_SIZE][GameConfiguration.GRID_SIZE], 1, GameConfiguration.INITIAL_ENERGY);
        this.manager = manager;
        initializeGrid();
    }

    @Override
    public void reset() {
        initializeGrid();
        state.turn = 1;
        state.energy = GameConfiguration.INITIAL_ENERGY;
    }

    private void initializeGrid() {
        // Place trees, lions, and empty cells randomly
        for (int i = 0; i < state.board.length; i++) {
            for (int j = 0; j < state.board[i].length; j++) {
                if(i == (state.board.length-1) / 2 && j == (state.board[i].length-1) / 2){
                    state.board[i][j] = HUMAN.getValue();
                } else {
                    double randomValue = Math.random();
                    if (randomValue < GameConfiguration.TREE_DENSITY) {
                        state.board[i][j] = TREE.getValue();
                    } else if (randomValue < GameConfiguration.TREE_DENSITY + GameConfiguration.LION_DENSITY) {
                        state.board[i][j] = LION.getValue();
                    } else {
                        state.board[i][j] = EMPTY.getValue();
                    }
                }
            }
        }
    }
    
    @Override
    public void close() {
        manager.close();
    }
    
    @Override
    public NDList getObservation() {
        return state.getObservation(manager);
    }
    public NDList getObservationWithAction() {
        return state.getObservationWithAction(manager);
    }
    @Override
    public ActionSpace getActionSpace() {
        return state.getActionSpace(manager);
    }

    @Override
    public Step step(NDList action, boolean training) {
        State preState = state;

        int move = action.singletonOrThrow().getInt();
        Moves boardMove = Moves.getMoveByAction(move);
        Position player = state.getPlayerPos();

        int newX = player.getX() + boardMove.getValue().getX();
        int newY = player.getY() + boardMove.getValue().getY();

        int valueOnNewPos = state.board[newX][newY];

        int energy = preState.energy - 1;
        int turn = preState.turn + 1;

        if(valueOnNewPos == LION.getValue()){
            energy = 0;
            turn = preState.turn;
        }

        if(valueOnNewPos == TREE.getValue()){
            energy += GameConfiguration.TREE_ENERGY;
        }

        state = new State(preState.board.clone(), turn, energy);
        state.board[player.getX()][player.getY()] = EMPTY.getValue();
        state.board[newX][newY] = HUMAN.getValue();

        CellularStep step = new CellularStep(manager.newSubManager(), preState, state, action);

        if (training) {
            replayBuffer.addStep(step);
        }

        //System.out.println(this.toString());

        return step;
    }

    @Override
    public Step[] getBatch() {
        return replayBuffer.getBatch();
    }

    
    @Override
    public String toString() {
        return state.toString();
    }
}