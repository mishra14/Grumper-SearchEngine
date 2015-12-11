package edu.upenn.cis455.project.scoring;

import java.util.ArrayList;
import java.util.Collections;

// TODO: Auto-generated Javadoc
/**
 * The Class URLTFIDF.
 */
public class URLTFIDF implements Comparable<URLTFIDF>
{
	
	/** The url. */
	private String url;
	
	/** The tfidf. */
	private float tfidf;
	
	/** The idf. */
	private float idf;
	
	/**
	 * Instantiates a new urltfidf.
	 *
	 * @param url the url
	 * @param tfidf the tfidf
	 * @param idf the idf
	 */
	public URLTFIDF(String url, float tfidf, float idf){
		this.url = url;
		this.tfidf = tfidf;
		this.idf = idf;
	}
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getURL(){
		return this.url;
	}
	
	/**
	 * Gets the tfidf.
	 *
	 * @return the tfidf
	 */
	public float getTFIDF(){
		return this.tfidf;
	}
	
	/**
	 * Gets the idf.
	 *
	 * @return the idf
	 */
	public float getIDF(){
		return this.idf;
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(URLTFIDF compareTFIDF){
		int other = (int) ((URLTFIDF)compareTFIDF).getTFIDF();
		return other - (int)this.tfidf ;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
        return "[ URL=" + url + ", tfidf=" + tfidf + ", idf=" + idf + "]";
    }
	
}
