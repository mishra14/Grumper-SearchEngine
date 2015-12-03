package edu.upenn.cis455.project.storage;


import java.util.ArrayList;
import java.util.HashMap;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class PostingsMarshaller implements DynamoDBMarshaller<ArrayList<Postings>>
{

	@Override
	public String marshall(ArrayList<Postings> postings)
	{	
		StringBuilder marshalled = new StringBuilder();
		int i = 0;
		int size = postings.size() - 1;

		for (Postings posting : postings){
			if (i < size)
			marshalled.append(posting.getPosting() 
					+ " " + posting.getTfidf() + " " + posting.getIdf() + "\t");
			else
				marshalled.append(posting.getPosting() 
						+ " " + posting.getTfidf() + " " + posting.getIdf());
			i++;
		}
		return marshalled.toString();
	}

	@Override
	public ArrayList<Postings> unmarshall(Class<ArrayList<Postings>> clazz, String s)
	{
		ArrayList<Postings> list = new ArrayList<Postings>();
		String[] allPostings = s.split("\t");
		for(String posting : allPostings){		
			Postings postings = new Postings();
			String[] pair = posting.trim().split(" ", 2);
			postings.setPosting(pair[0]);
			pair = pair[1].split(" ");
			postings.setTfidf(Float.parseFloat(pair[0].trim()));
			postings.setIdf(Float.parseFloat(pair[1].trim()));
			list.add(postings);
		}
		return list;
	}
	

}
