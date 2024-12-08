package ai.rl;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.rl.ActionSpace;
import ai.djl.modality.rl.agent.EpsilonGreedy;
import ai.djl.modality.rl.agent.QAgent;
import ai.djl.modality.rl.agent.RlAgent;
import ai.djl.modality.rl.env.RlEnv;
import ai.djl.ndarray.*;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.SequentialBlock;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingResult;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.training.tracker.LinearTracker;
import ai.djl.training.tracker.Tracker;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import cellularGame.GameConfiguration;
import cellularGame.simple.SimpleCellular;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TrainCellular {

    private TrainCellular() {}

    public static void main(String[] args) throws IOException, TranslateException, MalformedModelException {
        //TrainCellular.runExample();
        TrainCellular.playExample();
    }

    public static void playExample() throws MalformedModelException, IOException, TranslateException {
        Block modelBlock = getBlock();

        Model model = Model.newInstance("cellular");
        model.setBlock(modelBlock);

        model.load(Paths.get("build/model"), "cellular");

        try (Predictor<NDList, NDList> predictor =
                 model.newPredictor(new NoopTranslator())) {
            NDManager manager = NDManager.newBaseManager("PyTorch");
            SimpleCellular game = new SimpleCellular(manager);
            NDArray result = predictor.predict(game.getObservationWithAction()).singletonOrThrow();
            System.out.println("This is the result of using the trained model: " + result.toString());

            NDList takeAction = chooseAction(game, result);
            System.out.println("Take Action: " + takeAction.get(0));
        }
    }

    public static NDList chooseAction(SimpleCellular game, NDArray actionScores) {
        ActionSpace actionSpace = game.getActionSpace();
        System.out.println("actionspace: " + actionSpace.get(0).get(0));
        System.out.println("actionspace: " + actionSpace.get(1).get(0));
        System.out.println("actionspace: " + actionSpace.get(2).get(0));
        System.out.println("actionspace: " + actionSpace.get(3).get(0));
        int bestAction = Math.toIntExact(actionScores.argMax().getLong(new long[0]));
        return (NDList)actionSpace.get(bestAction);
    }
    public static TrainingResult runExample() throws IOException {
        String engine = "PyTorch";
        //String engine = "TensorFlow";
        int epoch = 6;
        int batchSize = 32;
        int replayBufferSize = 1024;
        int gamesPerEpoch = 128;

        // Validation is deterministic, thus one game is enough
        int validationGamesPerEpoch = 1;
        float rewardDiscount = 0.9f;

        SimpleCellular game = new SimpleCellular(NDManager.newBaseManager(engine), batchSize, replayBufferSize);

        Block block = getBlock();

        try (Model model = Model.newInstance("cellular", engine)) {
            model.setBlock(block);

            DefaultTrainingConfig config = setupTrainingConfig();
            try (Trainer trainer = model.newTrainer(config)) {
                trainer.initialize(new Shape(batchSize, GameConfiguration.NUM_OF_CELLS), new Shape(batchSize),new Shape(batchSize), new Shape(batchSize));

                trainer.notifyListeners(listener -> listener.onTrainingBegin(trainer));

                // Constructs the agent to train and play with
                RlAgent agent = new QAgent(trainer, rewardDiscount);
                Tracker exploreRate =
                        LinearTracker.builder()
                                .setBaseValue(0.9f)
                                .optSlope(-.9f / (epoch * gamesPerEpoch * 7))
                                .optMinValue(0.01f)
                                .build();
                agent = new EpsilonGreedy(agent, exploreRate);

                float avgValidationScore = 0;
                float avgTotalScore = 0;

                for (int i = 0; i < epoch; i++) {
                    int trainingScore = 0;
                    for (int j = 0; j < gamesPerEpoch; j++) {

                        float result = game.runEnvironment(agent, true);
                        RlEnv.Step[] batchSteps = game.getBatch();
                        agent.trainBatch(batchSteps);
                        trainer.step();

                        trainingScore += result;
                    }

                    avgTotalScore = (float) trainingScore / gamesPerEpoch;
                    System.out.println("AVG Training score: " + avgTotalScore);

                    trainer.notifyListeners(listener -> listener.onEpoch(trainer));

                    // Counts win rate after playing {validationGamesPerEpoch} games
                    int validationScore = 0;
                    for (int j = 0; j < validationGamesPerEpoch; j++) {
                        float result = game.runEnvironment(agent, false);
                        validationScore += result;
                    }

                    avgValidationScore = (float) validationScore / validationGamesPerEpoch;
                    System.out.println("Validation score: " + avgValidationScore);
                }

                trainer.notifyListeners(listener -> listener.onTrainingEnd(trainer));

                TrainingResult trainingResult = trainer.getTrainingResult();
                trainingResult.getEvaluations().put("validate_avgScore", avgValidationScore);
                trainingResult.getEvaluations().put("train_avgScore", avgTotalScore);

                model.save(Paths.get("build/model"), "cellular");
                return trainingResult;
            }
        }
    }

    public static Block getBlock() {
        return new SequentialBlock()
                .add(
                        arrays -> {
                            NDArray board = arrays.get(0); // Shape(N, GameConfiguration.NUM_OF_CELLS)
                            NDArray turn = arrays.get(1).reshape(-1, 1); // Shape(N, 1)
                            NDArray energy = arrays.get(2).reshape(-1, 1); // Shape(N, 1)
                            NDArray combined;


                            NDArray action = arrays.get(3).reshape(-1, 1); // Shape(N, 1)
                            // Concatenate to a combined vector of Shape(N, 11)
                            //System.out.println("Board shape: " + board.getShape());
                            //System.out.println("Turn shape: " + turn.getShape());
                            //System.out.println("Energy shape: " + energy.getShape());
                            //System.out.println("Action shape: " + action.getShape());
                            combined = NDArrays.concat(new NDList(board, turn, energy, action), 1);


                            return new NDList(combined.toType(DataType.FLOAT32, true));
                        })
                .add(new Mlp(GameConfiguration.NUM_OF_CELLS + 3, 1, new int[] {128, 64}));
    }

    public static DefaultTrainingConfig setupTrainingConfig() {
        return new DefaultTrainingConfig(Loss.l2Loss())
                .addTrainingListeners(TrainingListener.Defaults.basic())
               // .optDevices(arguments.getMaxGpus())
                .optOptimizer(Adam.builder().optLearningRateTracker(Tracker.fixed(0.0001F)).build());
    }

}