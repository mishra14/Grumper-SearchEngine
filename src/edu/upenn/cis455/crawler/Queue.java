package edu.upenn.cis455.crawler;

import java.util.ArrayList;

/**
 * que class
 * 
 * @author cis455
 *
 */
public class Queue<T>
{
	private ArrayList<T> queue;

	public Queue()
	{
		queue = new ArrayList<T>();
	}

	public ArrayList<T> getQueue()
	{
		return queue;
	}

	public synchronized int getSize()
	{
		return queue.size();
	}

	public synchronized void enqueue(T t)
	{
		queue.add(queue.size(), t); // add element to the end of the array list
		this.notify();
	}

	public synchronized T dequeue()
	{
		synchronized (XPathCrawler.getMaxCount())
		{
			int count = XPathCrawler.getMaxCount();
			count--;
			XPathCrawler.setMaxCount(count);
			if (count < 0)
			{
				// max count reached
				System.out.println("Max Count reached : Crawler stopping");
				XPathCrawler.setRun(false);
			}
		}
		return queue.remove(0); // remove element from the beginning of

	}

	public synchronized void enqueueAll(ArrayList<T> list)
	{
		for (T t : list)
		{
			enqueue(t); // add all elements from the list
		}
	}

}
