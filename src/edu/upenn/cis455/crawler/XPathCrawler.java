package edu.upenn.cis455.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import edu.upenn.cis455.storage.DBWrapper;

public class XPathCrawler {
	private static Queue<URL> queue = new Queue<URL>();
	private static ArrayList<XPathCrawlerThread> threads = new ArrayList<XPathCrawlerThread>();
	private static HashSet<URL> seenUrls = new HashSet<URL>();
	private static URL startingUrl;
	private static String dbPath;
	private static int maxSize;
	private static int maxCount; // synchronized - but the call should be in syn
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
	 */
	public static void main(String[] args) {
		if (args.length < 3 || args.length > 4) {
			System.out.println("Invalid arguments");
			System.out.println("Ankit Mishra");
			System.out.println("mankit");
			System.exit(-1);
		} else {
			try {
				startingUrl = new URL("https://dbappserv.cis.upenn.edu/crawltest.html");//args[0]);//
				DBWrapper.openDBWrapper(args[1]);
				maxSize = Integer.valueOf(args[2]);
				if (args.length == 4) {
					try {
						maxCount = Integer.valueOf(args[3]);
					} catch (NumberFormatException e) {
						System.out.println("Invalid max document count - " + e);
					}
				}
				// start the crawler threads
				for (int i = 0; i < THREAD_COUNT; i++) {
					XPathCrawlerThread thread = new XPathCrawlerThread(i);
					thread.start();
					threads.add(thread);
				}
				// add the starting url to the queue
				queue.enqueue(startingUrl);

			} catch (MalformedURLException e) {
				System.out.println("Invalid starting url - " + e);
			} catch (NumberFormatException e) {
				System.out.println("Invalid max file size - " + e);
			} catch (Exception e) {
				System.out.println("Invalid db Path - " + e);
			}
		}

		// finally close db store
		DBWrapper.closeDBWrapper();
	}

	public static Queue<URL> getQueue() {
		return queue;
	}

	public static void setQueue(Queue<URL> queue) {
		XPathCrawler.queue = queue;
	}

	public static URL getStartingUrl() {
		return startingUrl;
	}

	public static void setStartingUrl(URL startingUrl) {
		XPathCrawler.startingUrl = startingUrl;
	}

	public static String getDbPath() {
		return dbPath;
	}

	public static void setDbPath(String dbPath) {
		XPathCrawler.dbPath = dbPath;
	}

	public static int getMaxSize() {
		return maxSize;
	}

	public static void setMaxSize(int maxSize) {
		XPathCrawler.maxSize = maxSize;
	}

	public static int getMaxCount() {
		return maxCount;
	}

	public static void setMaxCount(int maxCount) {
		XPathCrawler.maxCount = maxCount;
	}

	public static boolean isRun() {
		return run;
	}

	public static void setRun(boolean run) {
		XPathCrawler.run = run;
	}

	public static ArrayList<XPathCrawlerThread> getThreads() {
		return threads;
	}

	public static void setThreads(ArrayList<XPathCrawlerThread> threads) {
		XPathCrawler.threads = threads;
	}

	public static HashSet<URL> getSeenUrls() {
		return seenUrls;
	}
	

}
