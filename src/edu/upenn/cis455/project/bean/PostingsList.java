package edu.upenn.cis455.project.bean;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PostingsList.
 */
public class PostingsList
{

	/** The df. */
	private int df;

	/** The postings. */
	private Map<String, Integer> postings;

	/**
	 * Instantiates a new postings list.
	 *
	 * @param df
	 *            the df
	 * @param postings
	 *            the postings
	 */
	public PostingsList(int df, Map<String, Integer> postings)
	{
		super();
		this.df = df;
		this.postings = postings;
	}

	/**
	 * Gets the df.
	 *
	 * @return the df
	 */
	public int getDf()
	{
		return df;
	}

	/**
	 * Sets the df.
	 *
	 * @param df
	 *            the new df
	 */
	public void setDf(int df)
	{
		this.df = df;
	}

	/**
	 * Gets the postings.
	 *
	 * @return the postings
	 */
	public Map<String, Integer> getPostings()
	{
		return postings;
	}

	/**
	 * Sets the postings.
	 *
	 * @param postings
	 *            the postings
	 */
	public void setPostings(Map<String, Integer> postings)
	{
		this.postings = postings;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "PostingsList [df=" + df + ", postings=" + postings + "]";
	}

}
