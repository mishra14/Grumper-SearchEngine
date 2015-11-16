package edu.upenn.cis455.project.crawler;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.project.bean.Queue;

public class CrawlWorkerServlet extends HttpServlet
{

	private static final long serialVersionUID = 1973672093393240348L;

	private List<String> workers;
	private WorkerStatus status;
	private WorkerPingThread pingThread;
	private Queue<String> urlQueue;

	public void init()
	{
		System.out.println("crawl worker servlet started");
		status = new WorkerStatus(getServletConfig().getInitParameter(
				"selfport"), "NA", "0", WorkerStatus.statusType.idle);
		System.out.println("crawl worker : status - " + status);
		URL masterUrl;
		try
		{
			String url = "http://"
					+ getServletConfig().getInitParameter("master")
					+ "/master/workerstatus";
			System.out.println("crawl worker servlet : master url - " + url);
			masterUrl = new URL(url);
			pingThread = new WorkerPingThread(masterUrl, status);
			pingThread.start();
		}
		catch (MalformedURLException e)
		{
			System.out
					.println("URL exception in worker servlet while creating ping thread");
			e.printStackTrace();
		}
		workers = new ArrayList<String>();
		urlQueue = new Queue<String>();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException
	{
		System.out.println("crawl worker : post received");
		String pathInfo = request.getPathInfo();
		StringBuilder pageContent = new StringBuilder();
		if (pathInfo.equalsIgnoreCase("/runcrawl")) // run crawl
		{
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			pageContent.append("Received request with urls - "
					+ request.getParameter("urls"));
			out.print("<html>" + pageContent.toString() + "</html>");
			response.flushBuffer();
			String urlString = request.getParameter("urls");
			System.out.println("crawl worker : /runcrawl received with urls - "
					+ urlString);
			// String numThreads = request.getParameter("crawlthreads");
			// int threadCount = Integer.valueOf(numThreads);
			updateWorkerList(request);
			addToQueue(request.getParameter("urls"));
			System.out.println("crawl worker : queue - "+urlQueue);
			// if status is idle then spawn crawl thread
			synchronized (status)
			{
				if (status.getStatus().equals(WorkerStatus.statusType.idle))
				{
					// update status for the ping thread
					status.setLastCrawledUrl("NA");
					status.setPagesCrawled("0");
					status.setStatus(WorkerStatus.statusType.crawling);
				}
			}
		}
		else if (pathInfo.equalsIgnoreCase("/pushdata"))
		{
			// add these new urls into the frontier
			System.out.println("crawl worker : /pushdata received");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html>" + pageContent.toString() + "</html>");
			response.flushBuffer();
			addToQueue(request.getParameter("urls"));
			System.out.println("crawl worker : queue - "+urlQueue);
		}
		else if (pathInfo.equalsIgnoreCase("/updateWorkers"))
		{
			// add these new urls into the frontier
			System.out.println("crawl worker : /updateworkers received");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html>" + pageContent.toString() + "</html>");
			response.flushBuffer();
			updateWorkerList(request);
		}
	}

	private void addToQueue(String urlString)
	{
		String[] urls = urlString.split(";");
		urlQueue.enqueueAll(new ArrayList<String>(Arrays.asList(urls)));
	}

	private void updateWorkerList(HttpServletRequest request)
	{
		int numWorkers = Integer
				.valueOf(request.getParameter("numworkers") == null ? "0"
						: request.getParameter("numworkers"));
		List<String> workerList = new ArrayList<String>();
		for (int i = 0; i < numWorkers; i++)
		{
			String worker = request.getParameter("worker" + (i + 1));
			if (worker != null)
			{
				workerList.add(worker);
			}
		}
		synchronized (workers)
		{
			workers = workerList;
		}
		System.out
				.println("crawl worker : updated qorker list to - " + workers);
	}
}
