package com.srain.cube.image;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueueAgent implements BlockingQueue<Runnable> {

	@Override
	public Runnable remove() {
		return null;
	}

	@Override
	public Runnable poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Runnable element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Runnable peek() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addAll(Collection<? extends Runnable> collection) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Runnable> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Runnable e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offer(Runnable e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(Runnable e) throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean offer(Runnable e, long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Runnable take() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int remainingCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int drainTo(Collection<? super Runnable> c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int drainTo(Collection<? super Runnable> c, int maxElements) {
		// TODO Auto-generated method stub
		return 0;
	}
}
