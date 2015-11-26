package edu.upenn.cis455.project.storage;

public class Postings
{
	String posting;
	float tfidf;
	
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

	@Override
	public String toString()
	{
		return "Postings [posting=" + posting + ", tfidf=" + tfidf + "]";
	}
	
	
}




