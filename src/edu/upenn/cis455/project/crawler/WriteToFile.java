package edu.upenn.cis455.project.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// TODO: Auto-generated Javadoc
/**
 * The Class WriteToFile.
 */
public class WriteToFile
{

	/**
	 * Write.
	 *
	 * @param url
	 *            the url
	 * @param idx
	 *            the idx
	 */
	public static synchronized void write(String url, int idx)
	{
		PrintWriter out;
		try
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter("url/"
					+ idx + ".txt", true)));
			out.println(url);
			out.close();
		}
		catch (IOException e)
		{
			System.out.println("Could not write to file: " + e);
		}
	}
}
