package ai;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class QLearningTranslator implements Translator<NDArray, NDArray> {
    @Override
    public Batchifier getBatchifier() {
        return null;  // We don't need batch processing in this case
    }

    @Override
    public NDList processInput(TranslatorContext ctx, NDArray input) {
        return new NDList(input);  // Return the input as is (in the right format)
    }

    @Override
    public NDArray processOutput(TranslatorContext translatorContext, NDList ndList) throws Exception {
        return ndList.get(0);
    }
}