package edu.upenn.cis455.project.searchengine;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

// TODO: Auto-generated Javadoc
/**
 * The Custom Class Heap.
 */
public class Heap
{
	
	private PriorityQueue<SimpleEntry<String, Float>> myHeap;
	
	/**
	 * Instantiates a new heap.
	 *
	 * @param initialCapacity the initial capacity
	 */
	public Heap(int initialCapacity)
	{
		
		myHeap = new PriorityQueue<SimpleEntry<String, Float>>(initialCapacity, 
				new Comparator<SimpleEntry<String, Float>>()
				{
			public int compare(SimpleEntry<String, Float> entry1, SimpleEntry<String, Float> entry2)
			{
				
				if (entry1.getValue() > entry2.getValue())
					return -1;
				
				else if (entry1.getValue() == entry2.getValue())
				{
					return 0;
				}
				
				else
					return 1;
			}
				});
		
	}
	
	/**
	 * Adds the key value pair to Heap.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void add(String key, Float value)
	{
		SimpleEntry<String, Float> entry = new SimpleEntry<String, Float>(key, value);
		myHeap.add(entry);
	}
	
	/**
	 * Adds all the key value pairs to the Heap.
	 *
	 * @param scores the scores
	 */
	public void addAll(Collection<SimpleEntry<String, Float>> scores)
	{
		myHeap.addAll(scores);
	}
	
	/**
	 * Removes the entry from the Heap.
	 *
	 * @return the simple entry
	 */
	public SimpleEntry<String, Float> remove()
	{
		return myHeap.remove();
	}
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty()
	{
		return myHeap.isEmpty();
	}
	
	/**
	 * Size.
	 *
	 * @return the size as int
	 */
	public int size()
	{
		return myHeap.size();
	}
}
