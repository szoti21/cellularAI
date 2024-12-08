package cellularGame.simple;

import ai.djl.modality.rl.ActionSpace;
import ai.djl.modality.rl.env.RlEnv;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;

public class CellularStep implements RlEnv.Step {

        private NDManager manager;
        private State preState;
        private State postState;
        private NDList action;

        public CellularStep(NDManager manager, State preState, State postState, NDList action) {
            this.manager = manager;
            this.preState = preState;
            this.postState = postState;
            this.action = action;
        }

        
        @Override
        public NDList getPreObservation() {
            return preState.getObservation(manager);
        }

        
        @Override
        public NDList getAction() {
            return action;
        }

        
        @Override
        public NDList getPostObservation() {
            return postState.getObservation(manager);
        }

        
        @Override
        public ActionSpace getPostActionSpace() {
            return postState.getActionSpace(manager);
        }

  
        @Override
        public NDArray getReward() {
            return manager.create(postState.getScore());
        }

        
        @Override
        public boolean isDone() {
            return postState.energy == 0;
        }

        
        @Override
        public void close() {
            manager.close();
        }
    }