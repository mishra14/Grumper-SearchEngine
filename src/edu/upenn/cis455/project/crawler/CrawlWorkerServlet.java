package edu.upenn.cis455.project.crawler;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CrawlWorkerServlet extends HttpServlet
{

	private static final long serialVersionUID = 1973672093393240348L;

	private List<String> workers;
	private WorkerStatus status;
	private PingThread pingThread;
	private Socket socket;

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
			pingThread = new PingThread(masterUrl, status);
			pingThread.start();
		}
		catch (MalformedURLException e)
		{
			System.out
					.println("URL exception in worker servlet while creating ping thread");
			e.printStackTrace();
		}
		workers = new ArrayList<String>();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException
	{
		System.out.println("worker : post received");
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

			System.out.println("crawl worker : /runcrawl received");
			// read job info from the request and store it
			String urlString = request.getParameter("urls");
			String numThreads = request.getParameter("crawlthreads");
			int numWorkers = Integer
					.valueOf(request.getParameter("numworkers") == null ? "0"
							: request.getParameter("numworkers"));
			int threadCount = Integer.valueOf(numThreads);
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
			System.out.println("crawl worker : new crawl job - " + urlString);
			// if status is idle then spawn crawl threads
			synchronized (status)
			{
				// update status for the ping thread
				status.setLastCrawledUrl("NA");
				status.setPagesCrawled("0");
				status.setStatus(WorkerStatus.statusType.crawling);
			}
		}
		else if (pathInfo.equalsIgnoreCase("/pushdata")) // get a reduce job
		{
			System.out.println("crawl worker : /pushdata received");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html>" + pageContent.toString() + "</html>");
			response.flushBuffer();
		}
	}
}
