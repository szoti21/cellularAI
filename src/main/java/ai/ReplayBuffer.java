package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ReplayBuffer {
        List<Experience> buffer;
        int maxSize;

        public ReplayBuffer(int size) {
            this.buffer = new ArrayList<>();
            this.maxSize = size;
        }

        // Add experience to the buffer
        public void store(Experience experience) {
            if (buffer.size() >= maxSize) {
                buffer.remove(0); // Remove the oldest experience if buffer is full
            }
            buffer.add(experience);
        }

        // Sample a random experience batch for training
        public List<Experience> sampleBatch(int batchSize) {
            Collections.shuffle(buffer);
            return buffer.subList(0, batchSize);
        }
    }