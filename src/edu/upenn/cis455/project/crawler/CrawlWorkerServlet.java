package edu.upenn.cis455.project.crawler;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.storage.DBWrapper;
import edu.upenn.cis455.project.storage.QueueDA;

public class CrawlWorkerServlet extends HttpServlet
{

	private static final long serialVersionUID = 1973672093393240348L;
	
	//Max size in megabytes
	public static final int max_size = 1;
	public static final int threshold = 5000;
	

	private List<String> workers;
	private int numWorkers;
	private WorkerStatus status;
	private WorkerPingThread pingThread;
	private Queue<String> urlQueue;
	private String port;
	private ArrayList<DocumentRecord> crawledDocs;
	private ArrayList<UrlList> urlMappings;
	
	private TimerTask timerTask;
	private Timer timer;
	
	public void init()
	{
		System.out.println("crawl worker servlet started");
		port = getServletConfig().getInitParameter("selfport");
		status = new WorkerStatus(port, "NA", "0", WorkerStatus.statusType.idle);
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
		crawledDocs = new ArrayList<DocumentRecord>();
		urlMappings = new ArrayList<UrlList>();
		
		//Setup db wrapper
		try
		{
			DBWrapper.openDBWrapper("./db");
		}
		catch (Exception e)
		{
			System.out.println("Could not open DB Wrapper: "+e);
		}
		
		urlQueue = QueueDA.getQueue();
		if(urlQueue == null){
			System.out.println("New url queue");
			urlQueue = new Queue<String>();
		}
		
		//Start the timer task to push data to db
		timerTask = new PushToDB(this.workers, this.crawledDocs, this.urlMappings);
		
		timer = new Timer(true);
		timer.scheduleAtFixedRate(timerTask, 60000, 60000);
	}
	
	public void destroy(){
		
		System.out.println("DESTROY METHOD CALLED!!!");
		//Force push any remaining data to db
		timer.cancel();
		new PushToDB(workers,this.crawledDocs,this.urlMappings).run();
		
		//Put current state of urlQueue onto berkeley db
		QueueDA.putQueue(urlQueue, new Date());
		System.out.println("URL PUSHED!!!");
		DBWrapper.closeDBWrapper();
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
			System.out.println("CRAWL WORKER : /runcrawl received with urls - "
					+ urlString);
			String numThreads = request.getParameter("crawlthreads");
			int threadCount = Integer.valueOf(numThreads);
			updateWorkerList(request);
			
			int self_id = getSelfId(request.getLocalAddr());
			
			addToQueue(request.getParameter("urls"));
//			System.out.println("crawl worker : queue - "+urlQueue);
			// if status is idle then spawn crawl thread
			boolean isCrawling = false;
			
			synchronized (status)
			{
				if (status.getStatus().equals(WorkerStatus.statusType.idle))
				{
					// update status for the ping thread
					status.setLastCrawledUrl("NA");
					status.setPagesCrawled("0");
					status.setStatus(WorkerStatus.statusType.crawling);
					isCrawling = false;
				}
				else if(status.getStatus().equals(WorkerStatus.statusType.crawling)){
					isCrawling = true;
				}
			}
			
			
			//Start crawler threads only if crawling has not already started
			if(!isCrawling){
				Thread [] threads = new Thread[threadCount];
				System.out.println("STARTING IN WORKER: "+self_id+" NUMTHREADS: "+threadCount);
				for(int i=0;i<threadCount;i++){
					CrawlerThread crawlerThread = new CrawlerThread(urlQueue, status, self_id, crawledDocs, urlMappings);
					Thread thread = new Thread(crawlerThread);
					thread.start();
					threads[i] = thread;
				}
			}
			
		}
		else if (pathInfo.equalsIgnoreCase("/pushdata"))
		{
			// add these new urls into the frontier
			System.out.println("[CRAWL WORKER] PUSHDATA RECEIVED");
//			response.setContentType("text/html");
//			PrintWriter out = response.getWriter();
//			out.print("<html>" + pageContent.toString() + "</html>");
//			response.flushBuffer();
			addToQueue(request.getParameter("urls"));
//			System.out.println("Crawl worker : Queue - "+urlQueue);
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

	private int getSelfId(String ip)
	{
		String local = ip+":"+this.port;
		System.out.println("local ip: "+local);
		int i;
		for(i=0;i<this.workers.size();i++){
			if(workers.get(i).equals(local))
				break;
		}
		
		return i;
	}

	private void addToQueue(String urlString)
	{
		if(urlString == null){
			System.out.println("urlString is null");
		}
		String[] urls = urlString.split(";");
		urlQueue.enqueueAll(new ArrayList<String>(Arrays.asList(urls)));
	}

	private void updateWorkerList(HttpServletRequest request)
	{
		numWorkers = Integer
				.valueOf(request.getParameter("numworkers") == null ? "0"
						: request.getParameter("numworkers"));
		
		CrawlerThread.num_workers = numWorkers;
//		System.out.println("[NUMWORKERS] : "+numWorkers);
//		List<String> workerList = new ArrayList<String>();
		synchronized (workers)
		{
			workers.clear();
			for (int i = 0; i < numWorkers; i++)
			{
				String worker = request.getParameter("worker" + (i + 1));
				if (worker != null)
				{
					this.workers.add(worker);
				}
			}
		}
		System.out.println("crawl worker : updated qorker list to - " + workers);
	}
}
