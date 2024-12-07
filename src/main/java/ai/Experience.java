package ai;

import ai.djl.ndarray.NDArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Experience {
    NDArray state;
    int action;
    float reward;
    NDArray nextState;

    // Constructor to store the experience
    public Experience(NDArray state, int action, float reward, NDArray nextState) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
    }
}
