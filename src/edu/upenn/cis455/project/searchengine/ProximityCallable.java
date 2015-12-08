package edu.upenn.cis455.project.searchengine;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

public class ProximityCallable implements Callable<PriorityQueue<Entry<String, Float>>>
{
	ArrayList<String> query;
	int maxWindowSize;
	
	public ProximityCallable(ArrayList<String> query, int maxWindowSize)
	{
		this.query = query;
		this.maxWindowSize = maxWindowSize;
	}
	@Override
	public PriorityQueue<Entry<String, Float>> call() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
