package edu.upenn.cis455.project.bean;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class Rank.
 */
@Entity
public class Rank
{

	/** The url. */
	@PrimaryKey
	private String url;

	/** The rank. */
	private Float rank;

	/**
	 * Instantiates a new rank.
	 */
	public Rank()
	{
		super();
	}

	/**
	 * Instantiates a new rank.
	 *
	 * @param url
	 *            the url
	 * @param rank
	 *            the rank
	 */
	public Rank(String url, float rank)
	{
		this.url = url;
		this.rank = rank;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url
	 *            the new url
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/**
	 * Gets the rank.
	 *
	 * @return the rank
	 */
	public Float getRank()
	{
		return rank;
	}

	/**
	 * Sets the rank.
	 *
	 * @param rank
	 *            the new rank
	 */
	public void setRank(Float rank)
	{
		this.rank = rank;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Rank [url=" + url + ", rank=" + rank + "]";
	}

}
