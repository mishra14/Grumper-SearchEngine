package test.edu.upenn.cis455;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.DocumentRecordDA;
import junit.framework.TestCase;

/**
 * class to test crawler by passing the limit parameter and without it
 * 
 * @author cis455
 *
 */
public class CrawlerTest extends TestCase
{
	@Test
	public synchronized void testCrawlerWithLimit() throws Exception
	{
		String filePath = "./db";
		deleteFile(new File(filePath));
		String[] args = { "https://dbappserv.cis.upenn.edu/crawltest.html",
				filePath, "10", "5" };
		XPathCrawler.main(args);
		DBWrapper.openDBWrapper(filePath);
		long count = DocumentRecordDA.getSize();
		DBWrapper.closeDBWrapper();
		assertEquals(count, 5);
	}

	/**
	 * method used to delete a db for test re runs
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void deleteFile(File file) throws IOException
	{

		if (file.isDirectory())
		{
			if (file.list().length == 0)
			{

				file.delete();
			}
			else
			{
				String files[] = file.list();
				for (String temp : files)
				{
					File fileDelete = new File(file, temp);
					deleteFile(fileDelete);
				}
				if (file.list().length == 0)
				{
					file.delete();
				}
			}

		}
		else
		{
			file.delete();
		}
	}
}
