package edu.upenn.cis455.project.storage;

import edu.upenn.cis455.project.scoring.URLTFIDF;

public class Postings implements Comparable<Postings>
{
	String posting;
	float tfidf;
	float idf;
	public Postings(String posting, float tfidf, float idf){
		this.posting = posting;
		this.tfidf = tfidf;
		this.idf = idf;
	}
	public Postings(){}
	public void setPosting(String posting){
		this.posting = posting;
	}
	
	public String getPosting(){
		return this.posting;
	}
	
	public void setTfidf(float tfidf){
		this.tfidf = tfidf;
	}
	
	public float getTfidf(){
		return this.tfidf;
	}
	
	public void setIdf(float idf){
		this.idf = idf;
	}
	
	public float getIdf(){
		return this.idf;
	}

	@Override
	public int compareTo(Postings compareTFIDF){
		int other = (int) ((Postings)compareTFIDF).getTfidf();
		return other - (int)this.tfidf ;
	}
	@Override
	public String toString()
	{
		return "Postings [posting=" + posting + ", tfidf=" + tfidf + ", idf=" + idf +"]";
	}
	
	
}




