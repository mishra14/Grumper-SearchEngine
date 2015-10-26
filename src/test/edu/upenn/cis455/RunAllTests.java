package test.edu.upenn.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase
{
	public static Test suite()
	{
		try
		{
			Class[] testClasses = {
					test.edu.upenn.cis455.XPathEngineTest.class,
					test.edu.upenn.cis455.DomParserTest.class,
					test.edu.upenn.cis455.ServletTest.class,
					test.edu.upenn.cis455.StorageTest.class,
					test.edu.upenn.cis455.CrawlerTest.class};

			return new TestSuite(testClasses);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
