package ai;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.*;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.*;
import ai.djl.training.dataset.*;
import ai.djl.training.loss.*;
import ai.djl.training.optimizer.*;
import ai.djl.translate.*;
import cellularGame.Grid;
import cellularGame.Moves;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cellularGame.GameConfiguration.GRID_SIZE;


public class NeuralNetworkAI {
    static ReplayBuffer replayBuffer = new ReplayBuffer(200);
    static QLearningTranslator qLearningTranslator = new QLearningTranslator();
    public static void main(String[] args) throws TranslateException {


        Model model = Model.newInstance("cellular-model");
        SequentialBlock block = new SequentialBlock();

        block.add(Blocks.batchFlattenBlock());
        block.add(Linear.builder().setUnits(128).build());
        block.add(Activation.reluBlock());
        block.add(Linear.builder().setUnits(4).build());

        model.setBlock(block);

        Trainer trainer = model.newTrainer(new DefaultTrainingConfig(Loss.l2Loss()));
        trainer.initialize(new Shape(1, GRID_SIZE * GRID_SIZE + 2));

        // Training loop pseudocode:
        for (int episode = 0; episode < 3; episode++) {
            Grid grid = new Grid(GRID_SIZE);
            try (NDManager manager = NDManager.newBaseManager()) {
                // Convert fullState to NDArray
                NDArray stateNDArray = manager.create(grid.flattenGrid()).toType(DataType.INT32, true);

                // Expand dimensions to add a batch dimension (1, total state size)
                NDArray djlState = stateNDArray.expandDims(0);

                while (!grid.terminalState()) {
                    Moves action = chooseAction(djlState, 1.0, model, trainer); // Exploration-exploitation
                    NDArray reward = grid.getScore();
                    grid.move(grid.playerPosition, action.getValue());
                    NDArray nextState = manager.create(grid.flattenGrid());

                    Experience experience = new Experience(currentState, action, reward, nextState);
                    replayBuffer.store(experience);

                    trainModel(trainer, replayBuffer, manager, model);
                }
            }

        }


    }

    public static Moves chooseAction(NDArray state, double epsilon, Model model, Trainer trainer) {
        Random random = new Random();

        List<Moves> validMoves = new ArrayList<>();
        Grid grid = Grid.getGridByArray(state.toIntArray());
        for (Moves move : Moves.values()){
            if (grid.canMove(grid.playerPosition, move.getValue())) {
                validMoves.add(move);
            }
        }

        // Step 1: Decide between exploration and exploitation
        if (random.nextDouble() < epsilon) {
            // Exploration: Random action
            return validMoves.get(random.nextInt(validMoves.size())); // Assume 4 possible actions
        } else {
            // Exploitation: Choose action with the highest Q-value
            NDList stateBatch = new NDList(state.expandDims(0)); // Batch dimension for model
            NDArray qValues = trainer.evaluate(stateBatch).singletonOrThrow();

            // Extract the Q-values for all actions
            float[] qArray = qValues.toFloatArray();

            // Custom heuristic adjustment for known objects (e.g., trees, lions)
            adjustQValuesBasedOnEnvironment(qArray, validMoves, grid);

            // Return the action with the highest Q-value
            return maxIndex(qArray);
        }
    }

    // Helper: Adjust Q-values based on custom rules
    private static void adjustQValuesBasedOnEnvironment(float[] qValues, List<Moves> validMoves, Grid grid) {
        // Assume you have access to the environment's current state
        // Example: Adjust Q-values to favor trees and avoid lions
        for (int action = 0; action < qValues.length; action++) {
            Moves move = Moves.getMoveByAction(action);
            if (!validMoves.contains(move)){
                qValues[action] -= 1000.0;
            } else if (grid.moveLeadsToTree(grid.playerPosition, move.getValue())) {
                qValues[action] += 2.0; // Boost Q-value for tree direction
            }else if (grid.moveLeadsToLion(grid.playerPosition, move.getValue())) {
                qValues[action] -= 100.0; // Penalize Q-value for lion direction
            }
        }
    }

    // Helper: Find the index of the max value in an array
    private static Moves maxIndex(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return Moves.getMoveByAction(maxIndex);
    }

    public void trainModel(Trainer trainer, ReplayBuffer replayBuffer, int batchSize, float gamma, NDManager manager, Model model) {
        // Sample a batch of experiences from the replay buffer
        List<Experience> batch = replayBuffer.sampleBatch(batchSize);

        // Prepare the input and target arrays
        NDArray states = manager.create(new int[]{batchSize, GRID_SIZE * GRID_SIZE + 2});  // Assuming 'stateSize' is the flattened state size
        NDArray targets = manager.create(new int[]{batchSize, 4});  // Assuming 4 possible actions

        // Loop through the batch and fill the states and targets arrays
        for (int i = 0; i < batchSize; i++) {
            Experience exp = batch.get(i);

            // Get the current state and action taken
            NDArray state = exp.state;
            int action = exp.action;

            // Get the reward and next state
            float reward = exp.reward;
            NDArray nextState = exp.nextState;

            // Compute the target Q-value for the current experience
            Predictor<NDArray, NDArray> predictor = model.newPredictor(qLearningTranslator);

            NDArray nextStateQValues;

            try {
                nextStateQValues = predictor.predict(nextState);
            } catch (TranslateException e) {
                throw new RuntimeException(e);
            }

            float maxNextQValue = nextStateQValues.max().getFloat();  // Maximum Q-value for the next state

            // Target Q-value for the action taken
            float targetQValue = reward + gamma * maxNextQValue;

            // Fill the target array, updating the target Q-value for the action taken
            targets.set(new NDIndex(action), targetQValue);

            // Fill the states array
            states.set(new NDIndex(i), state);
        }
        NDArray predictions = trainer.forward(states);

        // Compute the loss and backpropagate
        NDArray loss = lossFunction.evaluate(targets, predictions);
        loss.backward();

        // Update the model weights
        trainer.step();
    }
}



