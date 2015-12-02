package edu.upenn.cis455.project.searchengine;

import java.util.*;

public class Heap
{
	private PriorityQueue<UrlScores> myHeap;
	
	public Heap(int initialCapacity)
	{
		
		myHeap = new PriorityQueue<UrlScores>(initialCapacity, 
				new Comparator<UrlScores>()
				{
			public int compare(UrlScores url1, UrlScores url2)
			{
				
				if (url1.getCount() > url2.getCount())
					return -1;
				else if (url1.getCount() == url2.getCount())
				{
					if (url1.getTfidf() > url2.getTfidf())
					{
						return -1;
					}
					
					else if (url1.getTfidf() == url2.getTfidf())
					{
						return 0;
					}
					
					else
						return 1;
				}
				else
					return 1;
			}
				});
		
	}
	
	public void addAll(Collection<UrlScores> scores)
	{
		myHeap.addAll(scores);
	}
	
	public UrlScores remove()
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
