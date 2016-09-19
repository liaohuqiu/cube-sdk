package in.srain.cube.image.iface;

import java.util.concurrent.Executor;

/**
 * An Executor to execute ImageTask, it allows the task can be executed in different order.
 */
public interface ImageTaskExecutor extends Executor {
    void setTaskOrder(int order);
}