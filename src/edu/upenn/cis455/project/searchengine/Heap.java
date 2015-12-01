package edu.upenn.cis455.project.searchengine;

import java.util.*;

import edu.upenn.cis455.project.storage.Postings;

public class Heap
{
	private PriorityQueue<Postings> myHeap;
	
	public Heap(int initialCapacity)
	{
		myHeap = new PriorityQueue<Postings>(initialCapacity, 
				new Comparator<Postings>()
				{
					public int compare(Postings p1, Postings p2)
					{
						if (p1.getTfidf() > p2.getTfidf())
							return -1;
						else if (p1.getTfidf() == p2.getTfidf())
							return 0;
						else
							return 1;
					}
				});
	}
	
	public void addAll(ArrayList<Postings> postings)
	{
		myHeap.addAll(postings);
	}
	
	public Postings remove()
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
