package edu.upenn.cis455.project.scoring;

public class TFIDF
{
	public static float getTFIDF(int tf, float idf)
	{
		float tfidf = tf * idf;
		return tfidf;
	}
	
	public static float getIDF(int df, int numdocs){
		float idf = (float) Math.log(numdocs/df);
		return idf;
	}
	
	

}
