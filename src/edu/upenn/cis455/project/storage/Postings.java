package edu.upenn.cis455.project.storage;

import edu.upenn.cis455.project.scoring.URLTFIDF;

// TODO: Auto-generated Javadoc
/**
 * The Class Postings.
 */
public class Postings implements Comparable<Postings>
{
	
	/** The posting. */
	String posting;
	
	/** The tfidf. */
	float tfidf;
	
	/** The idf. */
	float idf;
	
	/**
	 * Instantiates a new postings.
	 *
	 * @param posting the posting
	 * @param tfidf the tfidf
	 * @param idf the idf
	 */
	public Postings(String posting, float tfidf, float idf){
		this.posting = posting;
		this.tfidf = tfidf;
		this.idf = idf;
	}
	
	/**
	 * Instantiates a new postings.
	 */
	public Postings(){}
	
	/**
	 * Sets the posting.
	 *
	 * @param posting the new posting
	 */
	public void setPosting(String posting){
		this.posting = posting;
	}
	
	/**
	 * Gets the posting.
	 *
	 * @return the posting
	 */
	public String getPosting(){
		return this.posting;
	}
	
	/**
	 * Sets the tfidf.
	 *
	 * @param tfidf the new tfidf
	 */
	public void setTfidf(float tfidf){
		this.tfidf = tfidf;
	}
	
	/**
	 * Gets the tfidf.
	 *
	 * @return the tfidf
	 */
	public float getTfidf(){
		return this.tfidf;
	}
	
	/**
	 * Sets the idf.
	 *
	 * @param idf the new idf
	 */
	public void setIdf(float idf){
		this.idf = idf;
	}
	
	/**
	 * Gets the idf.
	 *
	 * @return the idf
	 */
	public float getIdf(){
		return this.idf;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Postings compareTFIDF){
		int other = (int) ((Postings)compareTFIDF).getTfidf();
		return other - (int)this.tfidf ;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Postings [posting=" + posting + ", tfidf=" + tfidf + ", idf=" + idf +"]";
	}
	
	
}




