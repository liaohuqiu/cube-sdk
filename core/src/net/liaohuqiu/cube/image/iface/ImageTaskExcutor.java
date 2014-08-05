package net.liaohuqiu.cube.image.iface;

import java.util.concurrent.Executor;

import net.liaohuqiu.cube.image.ImageLoader.ImageTaskOrder;

/**
 * An Executor to execute ImageTask, it allows the task can be executed in different order.
 */
public interface ImageTaskExcutor extends Executor {
	void setTaskOrder(ImageTaskOrder order);
}