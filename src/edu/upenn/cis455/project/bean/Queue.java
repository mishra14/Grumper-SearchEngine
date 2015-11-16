package edu.upenn.cis455.project.bean;

import java.util.ArrayList;

/**
 * generic blocking queue implementation to hold different types of data.
 *
 * @author cis455
 * @param <T>
 *            the generic type
 */
public class Queue<T>
{

	/** The queue. */
	private ArrayList<T> queue;

	/**
	 * Instantiates a new queue.
	 */
	public Queue()
	{
		queue = new ArrayList<T>();
	}

	/**
	 * Gets the queue.
	 *
	 * @return the queue
	 */
	public ArrayList<T> getQueue()
	{
		return queue;
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public synchronized int getSize()
	{
		return queue.size();
	}

	/**
	 * Enqueue.
	 *
	 * @param t
	 *            the t
	 */
	public synchronized void enqueue(T t)
	{
		queue.add(queue.size(), t); // add element to the end of the array list
		this.notify();
	}

	/**
	 * Dequeue.
	 *
	 * @return the t
	 */
	public synchronized T dequeue()
	{
		return queue.remove(0); // remove element from the beginning of
	}

	/**
	 * Enqueue all.
	 *
	 * @param list
	 *            the list
	 */
	public synchronized void enqueueAll(ArrayList<T> list)
	{
		for (T t : list)
		{
			enqueue(t); // add all elements from the list
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Queue [queue=" + queue + "]";
	}

}
