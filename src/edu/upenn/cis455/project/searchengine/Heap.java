package edu.upenn.cis455.project.searchengine;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class Heap
{
	private PriorityQueue<SimpleEntry<String, Float>> myHeap;
	
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
	
	public void add(String key, Float value)
	{
		SimpleEntry<String, Float> entry = new SimpleEntry<String, Float>(key, value);
		myHeap.add(entry);
	}
	
	public void addAll(Collection<SimpleEntry<String, Float>> scores)
	{
		myHeap.addAll(scores);
	}
	
	public SimpleEntry<String, Float> remove()
	{
		return myHeap.remove();
	}
	
	public boolean isEmpty()
	{
		return myHeap.isEmpty();
	}
	
	public int size()
	{
		return myHeap.size();
	}
}
