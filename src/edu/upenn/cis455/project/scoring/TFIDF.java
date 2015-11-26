package edu.upenn.cis455.project.scoring;

public class TFIDF
{
	public static double compute(int tf, int df, int numdocs)
	{
		double tfidf = tf * Math.log(numdocs/df);
		return tfidf;
	}
}
