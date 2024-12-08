package cellularGame.simple;

import ai.djl.training.TrainingResult;
import ai.rl.TrainCellular;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        TrainingResult result = TrainCellular.runExample();

        float trainScore = result.getTrainEvaluation("avgScore");
        System.out.printf("AVG train score  " + trainScore);

        float validationScore = result.getValidateEvaluation("avgScore");
        System.out.println("AVG validation score: " + validationScore);
    }
}
