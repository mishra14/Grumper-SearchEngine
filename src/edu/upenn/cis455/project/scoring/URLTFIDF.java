package edu.upenn.cis455.project.scoring;

import java.util.ArrayList;
import java.util.Collections;

public class URLTFIDF implements Comparable<URLTFIDF>
{
	private String url;
	private float tfidf;
	private float idf;
	
	public URLTFIDF(String url, float tfidf, float idf){
		this.url = url;
		this.tfidf = tfidf;
		this.idf = idf;
	}
	
	public String getURL(){
		return this.url;
	}
	
	public float getTFIDF(){
		return this.tfidf;
	}
	public float getIDF(){
		return this.idf;
	}


	@Override
	public int compareTo(URLTFIDF compareTFIDF){
		int other = (int) ((URLTFIDF)compareTFIDF).getTFIDF();
		return other - (int)this.tfidf ;
	}
	@Override
    public String toString() {
        return "[ URL=" + url + ", tfidf=" + tfidf + ", idf=" + idf + "]";
    }
	
}
