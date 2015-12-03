package edu.upenn.cis455.project.searchengine;

public class UrlScores
{
	private String url;
	private float tfidf;
	private int count;
	
	public UrlScores(String url, float tfidf, int count)
	{
		this.url = url;
		this.tfidf = tfidf;
		this.count = count;
	}	
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public float getTfidf()
	{
		return tfidf;
	}
	
	public void setTfidf(float tfidf)
	{
		this.tfidf = tfidf;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}
}
