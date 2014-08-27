package in.srain.cube.image.iface;

import in.srain.cube.image.ImageLoader.ImageTaskOrder;

import java.util.concurrent.Executor;

/**
 * An Executor to execute ImageTask, it allows the task can be executed in different order.
 */
public interface ImageTaskExecutor extends Executor {
    void setTaskOrder(ImageTaskOrder order);
}