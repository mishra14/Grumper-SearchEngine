package edu.upenn.cis455.project.crawler;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.*;
import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.http.Http;
import edu.upenn.cis455.project.http.HttpResponse;
import edu.upenn.cis455.project.storage.S3DocumentDA;

/**
 * This class is master servlet class that is responsible for receiving jobs
 * from the user interface and pass the jobs to the workers.
 *
 * @author cis455
 */

public class CrawlMasterServlet extends HttpServlet
{

	static final long serialVersionUID = 455555001;

	private Socket socket;

	private static final String css = "<head>" + "<style>" + "table, th, td {"
			+ "    border: 1px solid black;" + "    border-collapse: collapse;"
			+ "}" + "th, td {" + "    padding: 5px;" + "}" + "</style>"
			+ "</head>";

	private Map<String, WorkerStatus> workers;

	private List<String> workerList;

	private MasterPingThread pingThread;

	private static long INACTIVE_INTERVAL = 30000;

	public void init()
	{
		System.out.println("Master servlet ready");
		this.workers = new HashMap<String, WorkerStatus>();
		this.workerList = new ArrayList<String>();
		pingThread = new MasterPingThread(workers, workerList);
		pingThread.start();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		String pathInfo = request.getPathInfo();
		StringBuilder pageContent = new StringBuilder();
		if (pathInfo == null || pathInfo.equalsIgnoreCase("/")) // homepage
		{
			response.sendRedirect("/");
		}
		else if (pathInfo.equalsIgnoreCase("/addseedurls"))
		{
			// create new job
			String urlString = request.getParameter("seedurls");
			String crawlThreads = request.getParameter("crawlthreads");
			String urls[] = urlString.split(";");
			// hash url host names and separate them between workers
			try
			{
				assignJob(urls, crawlThreads);
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			// send the urls to all the active workers

			pageContent.append("Recieved urls - " + urlString);

			// redirect to the status page
			// response.sendRedirect("/master/status");

		}
		PrintWriter out = response.getWriter();
		out.print("<html>" + css + pageContent.toString() + "</html>");
		response.flushBuffer();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException
	{
		
		System.out.println("crawler master : get received");
		response.setContentType("text/html");

		String pathInfo = request.getPathInfo();
		StringBuilder pageContent = new StringBuilder();
		if (pathInfo == null || pathInfo.equalsIgnoreCase("/")) // homepage
		{
			pageContent.append(getHomePage());
		}
		else if (pathInfo.equalsIgnoreCase("/status"))
		{
			S3DocumentDA s3 = new S3DocumentDA();
			DocumentRecord doc = new DocumentRecord("http://ankitmishra.me",
					"This is a test document String", (new Date()).getTime());
			System.out.println(doc);
			s3.putDocument(doc);
			System.out.println(s3.getDocument(doc.getDocumentId()));
			//s3.deleteDocument(doc);
			System.out.println(s3.documentExists(doc));
			pageContent.append(getStatusPage());
		}
		else if (pathInfo.equalsIgnoreCase("/workerstatus"))
		{
			String port = request.getParameter("port");
			WorkerStatus.statusType status = WorkerStatus.statusType
					.valueOf(request.getParameter("status"));
			String lastCrawledUrl = request.getParameter("lastcrawledurl");
			String pagesCrawled = request.getParameter("pagescrawled");
			WorkerStatus workerStatus = new WorkerStatus(port, lastCrawledUrl,
					pagesCrawled, status);
			synchronized (workerList)
			{
				if (!workerList.contains(request.getRemoteAddr() + ":" + port))
				{
					workerList.add(request.getRemoteAddr() + ":" + port);
				}
				synchronized (workers)
				{
					workers.put(request.getRemoteAddr() + ":" + port,
							workerStatus);
				}
			}
			System.out.println("master : " + "Updated worker - "
					+ workerStatus.toString());
			pageContent.append("Updated worker - " + workerStatus.toString());
		}
		else
		{
			pageContent.append("<head><title>Master</title></head>"
					+ "<body>Unknown Url<br><br>"
					+ "Please use <a href=\"/master/status\">status page</a> "
					+ " to give a new job and check worker status !<br>"
					+ "</body>");
		}
		PrintWriter out = response.getWriter();
		out.print("<html>" + css + pageContent.toString() + "</html>");
		response.flushBuffer();
	}

	private String getHomePage()
	{
		return "<head><title>Master</title></head>"
				+ "<body><h2>Master Servlet home page </h2><br><br>"
				+ "Please use <a href=\"/master/status\">status page</a> "
				+ " to give a new job and check worker status !<br>"
				+ "</body>";
	}

	private String getStatusPage()
	{
		StringBuilder pageContent = new StringBuilder();

		pageContent.append("<head><title>Master</title></head>"
				+ "<body><h2>Crawler Master Servlet status page </h2><br>");
		pageContent.append("Submit new seed urls - <br><br>"
				+ "<form action=\"/master/addseedurls\" method=\"post\">"
				+ "Seed Urls(seperated by ; ):<br>"
				+ "<input type=\"text\" name =\"seedurls\"><br>"
				+ "Number of crawl threads:<br>"
				+ "<input type=\"text\" name =\"crawlthreads\"><br>"
				+ "<input type=\"submit\" value =\"Submit\">"
				+ "</form><br><br>");

		pageContent.append("Worker status - <br>" + "<table>"
				+ "<th>IP:Port</th>" + "<th>Status</th>"
				+ "<th>Last Crawled Url</th>" + "<th>Pages Crawled</th>");
		synchronized (workers)
		{
			for (Map.Entry<String, WorkerStatus> entry : workers.entrySet())
			{
				long now = (new Date()).getTime();
				if (now - entry.getValue().getTimestamp() <= INACTIVE_INTERVAL)
				{
					pageContent.append("<tr>" + "<td>" + entry.getKey()
							+ "</td>" + "<td>" + entry.getValue().getStatus()
							+ "</td>" + "<td>"
							+ entry.getValue().getLastCrawledUrl() + "</td>"
							+ "<td>" + entry.getValue().getPagesCrawled()
							+ "</td></tr>");
				}

			}
		}

		pageContent.append("</table></body>");

		return pageContent.toString();
	}

	private void assignJob(String[] urls, String crawlThreads)
			throws IOException, NoSuchAlgorithmException
	{
		ArrayList<String> activeWorkers;
		synchronized (workerList)
		{
			activeWorkers = new ArrayList<String>(workerList);
		}
		synchronized (workers)
		{
			for (Map.Entry<String, WorkerStatus> entry : workers.entrySet())
			{
				long now = (new Date()).getTime();
				if (now - entry.getValue().getTimestamp() > INACTIVE_INTERVAL)
				{
					activeWorkers.remove(entry.getKey());
				}
			}
		}

		if (activeWorkers.size() < 1)
		{
			System.out.println("crawl master : no active worker");
			return;
		}

		Map<String, StringBuilder> urlMapping = new HashMap<String, StringBuilder>();
		for (String url : urls)
		{
			int index = Hash.hashKey(url, activeWorkers.size());
			String worker = activeWorkers.get(index);
			if (urlMapping.containsKey(worker))
			{
				StringBuilder value = urlMapping.get(worker);
				urlMapping.put(worker, value.append(";" + url));
			}
			else
			{
				StringBuilder value = new StringBuilder();
				urlMapping.put(worker, value.append(url));
			}
		}
		sendJob(urlMapping, activeWorkers, crawlThreads);
	}

	private void sendJob(Map<String, StringBuilder> urlMapping,
			ArrayList<String> activeWorkers, String crawlThreads)
			throws IOException
	{
		StringBuilder workerString = new StringBuilder();
		for (int i = 0; i < activeWorkers.size(); i++)
		{
			workerString
					.append("worker" + (i + 1) + "=" + activeWorkers.get(i));
			if (i < activeWorkers.size() - 1)
			{
				workerString.append("&");
			}
		}
		for (String worker : activeWorkers)
		{
			String urls = urlMapping.get(worker).toString();
			String body = "urls=" + urls + "&" + "crawlthreads=" + crawlThreads
					+ "&" + "numworkers=" + activeWorkers.size() + "&"
					+ workerString.toString();
			sendJobToWorker(worker, body);
		}
	}

	private void sendJobToWorker(String worker, String body)
			throws UnknownHostException, IOException
	{
		String workerUrl = "http://" + worker + "/worker/runcrawl";
		System.out.println("master : sending crawl request to - " + workerUrl);
		URL url = new URL(workerUrl);
		String host = url.getHost();
		int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
		socket = new Socket(host, port);
		PrintWriter clientSocketOut = new PrintWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		clientSocketOut.print("POST " + url.toString() + " HTTP/1.0\r\n");
		clientSocketOut.print("Content-Length:" + body.length() + "\r\n");
		clientSocketOut
				.print("Content-Type:application/x-www-form-urlencoded\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.print(body);
		clientSocketOut.print("\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.flush();
		HttpResponse response = Http.parseResponse(socket);
		if (!response.getResponseCode().equalsIgnoreCase("200"))
		{
			System.out.println("Master : worker " + worker
					+ "did not accept the crawl job");
		}
	}
}