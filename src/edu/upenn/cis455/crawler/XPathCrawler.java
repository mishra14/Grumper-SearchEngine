package edu.upenn.cis455.crawler;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.DocumentRecordDA;

/**
 * This is the main crawler class. It is responsible for starting the crawler
 * threadpool.
 * 
 * @author cis455
 *
 */
public class XPathCrawler
{
	private static Queue<URL> queue = new Queue<URL>();
	private static HashMap<String, RobotsTxtInfo> robotTxts = new HashMap<String, RobotsTxtInfo>();
	private static ArrayList<XPathCrawlerThread> threads = new ArrayList<XPathCrawlerThread>();
	private static HashSet<URL> seenUrls = new HashSet<URL>();
	private static URL startingUrl;
	private static String dbPath;
	private static long maxSize;
	private static Integer maxCount = new Integer(Integer.MAX_VALUE); // default
																		// value
																		// =
																		// MAX//
	// synchronized - but
	// the call should be in syn
	// block
	private static boolean run = true; // synchronized
	private static final int THREAD_COUNT = 5;

	/**
	 * starts the crawler(s)
	 * 
	 * @param args
	 *            arg 0 - url of the starting page arg 1 - directory of the db
	 *            storage arg 2 - max file size in MB arg 4 - (optional) number
	 *            of files to get before stopping
	 * @throws URISyntaxException
	 */
	public static void main(String[] args)
	{
		if (args.length < 3 || args.length > 4)
		{
			System.out.println("Invalid arguments");
			System.out.println("Ankit Mishra");
			System.out.println("mankit");
			System.exit(-1);
		}
		else
		{
			try
			{
				startingUrl = new URL(args[0]);
				DBWrapper.openDBWrapper(args[1]);
				System.out.println("CURRENT DOCUMENT SIZE = "
						+ DocumentRecordDA.getSize());
				maxSize = Integer.valueOf(args[2]) * 1024 * 1024;
				if (args.length == 4)
				{
					try
					{
						maxCount = Integer.valueOf(args[3]);
					}
					catch (NumberFormatException e)
					{
						System.out.println("Invalid max document count - ");
						e.printStackTrace();
					}
				}
				// start the crawler threads
				for (int i = 0; i < THREAD_COUNT; i++)
				{
					XPathCrawlerThread thread = new XPathCrawlerThread(i);
					thread.start();
					threads.add(thread);
				}
				// add the starting url to the queue
				queue.enqueue(startingUrl);
				while (true)
				{
					if (queue.getSize() == 0)
					{
						boolean shouldEndCrawl = true;
						for (XPathCrawlerThread thread : threads)
						{
							if (thread.getState().equals(Thread.State.RUNNABLE))
							{
								shouldEndCrawl = false;
								break;
							}
						}
						if (shouldEndCrawl)
						{
							System.out
									.println("Stopping crawler : Nothing to crawl");
							setRun(false);

							break;
						}
					}
					if (!isRun())
					{
						boolean shouldEndCrawl = true;
						for (XPathCrawlerThread thread : threads)
						{
							if (thread.getState().equals(Thread.State.RUNNABLE))
							{
								shouldEndCrawl = false;
								break;
							}
						}
						if (shouldEndCrawl)
						{
							System.out
									.println("Stopping crawler main : Max count reached");
							break;
						}
					}
				}

				for (XPathCrawlerThread thread : threads)
				{
					thread.interrupt();
				}
				// wait for threads to end before stopping
				for (int i = 0; i < THREAD_COUNT; i++)
				{
					threads.get(i).join();
				}
			}
			catch (MalformedURLException e)
			{
				System.out.println("Invalid starting url - ");
				e.printStackTrace();
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid max file size - ");
				e.printStackTrace();
			}
			catch (Exception e)
			{
				System.out.println("Invalid db Path - ");
				e.printStackTrace();
			}
		}

		// finally close db store

		DBWrapper.closeDBWrapper();
	}

	public static Queue<URL> getQueue()
	{
		return queue;
	}

	public static void setQueue(Queue<URL> queue)
	{
		XPathCrawler.queue = queue;
	}

	public static URL getStartingUrl()
	{
		return startingUrl;
	}

	public static void setStartingUrl(URL startingUrl)
	{
		XPathCrawler.startingUrl = startingUrl;
	}

	public static String getDbPath()
	{
		return dbPath;
	}

	public static void setDbPath(String dbPath)
	{
		XPathCrawler.dbPath = dbPath;
	}

	public static long getMaxSize()
	{
		return maxSize;
	}

	public static void setMaxSize(int maxSize)
	{
		XPathCrawler.maxSize = maxSize;
	}

	public static Integer getMaxCount()
	{
		return maxCount;
	}

	public static void setMaxCount(Integer maxCount)
	{
		XPathCrawler.maxCount = maxCount;
	}

	public synchronized static boolean isRun()
	{
		return run;
	}

	public synchronized static void setRun(boolean run)
	{
		XPathCrawler.run = run;
	}

	public static ArrayList<XPathCrawlerThread> getThreads()
	{
		return threads;
	}

	public static void setThreads(ArrayList<XPathCrawlerThread> threads)
	{
		XPathCrawler.threads = threads;
	}

	public static HashMap<String, RobotsTxtInfo> getRobotTxts()
	{
		return robotTxts;
	}

	public static void setRobotTxts(HashMap<String, RobotsTxtInfo> robotTxts)
	{
		XPathCrawler.robotTxts = robotTxts;
	}

	public static HashSet<URL> getSeenUrls()
	{
		return seenUrls;
	}

	public static void setSeenUrls(HashSet<URL> seenUrls)
	{
		XPathCrawler.seenUrls = seenUrls;
	}

}
