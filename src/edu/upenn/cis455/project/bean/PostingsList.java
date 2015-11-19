package edu.upenn.cis455.project.bean;

import java.util.Map;

public class PostingsList
{
	private int df;
	private Map<String, Integer> postings;

	public PostingsList(int df, Map<String, Integer> postings)
	{
		super();
		this.df = df;
		this.postings = postings;
	}

	public int getDf()
	{
		return df;
	}

	public void setDf(int df)
	{
		this.df = df;
	}

	public Map<String, Integer> getPostings()
	{
		return postings;
	}

	public void setPostings(Map<String, Integer> postings)
	{
		this.postings = postings;
	}

	@Override
	public String toString()
	{
		return "PostingsList [df=" + df + ", postings=" + postings + "]";
	}

}
