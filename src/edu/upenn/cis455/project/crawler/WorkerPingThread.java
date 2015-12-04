package edu.upenn.cis455.project.crawler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import edu.upenn.cis455.project.http.Http;
import edu.upenn.cis455.project.http.HttpResponse;

/**
 * This class is used to instantiate a pinger thread used by the worker servlet
 * to send status updates to the master.
 *
 * @author cis455
 */
public class WorkerPingThread extends Thread
{

	/** The master url. */
	private URL masterUrl;

	/** The socket. */
	private Socket socket;

	/** The self port. */
	private String selfPort;

	/** The worker status. */
	private WorkerStatus workerStatus;

	/** The run. */
	private boolean run;

	/**
	 * Instantiates a new ping thread.
	 *
	 * @param url
	 *            the url
	 * @param status
	 *            the status
	 */
	public WorkerPingThread(URL url, WorkerStatus status)
	{
		this.masterUrl = url;
		this.selfPort = status.getPort();
		this.workerStatus = status;
		this.run = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while (run)
		{
			try
			{
//				System.out
//						.println("crawl worker ping thread : pinging crawl master");
				String host = masterUrl.getHost();
				int port = masterUrl.getPort() == -1 ? masterUrl
						.getDefaultPort() : masterUrl.getPort();
				try
				{
					socket = new Socket(host, port);
					sendPing();
					// System.out.println("ping thread : Response - " +
					// response);
				}
				catch (IOException e)
				{
					System.out
							.println("IOException while opening socket to master");
					e.printStackTrace();
				}

				sleep(10000);
			}
			catch (InterruptedException e)
			{
				System.out.println("Ping Thread interrupted");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send ping.
	 *
	 * @return the http response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	/*
	 * http://localhost:8080/master/workerstatus?port=8081&status=idle&keysread=10&keyswritten=5&job=classes
	 */
	public HttpResponse sendPing() throws IOException
	{
		PrintWriter clientSocketOut = new PrintWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		String status;
		String pagesCrawled;
		String lastCrawledUrl;
		synchronized (workerStatus)
		{
			status = workerStatus.getStatus().toString();
			pagesCrawled = workerStatus.getPagesCrawled();
			lastCrawledUrl = workerStatus.getLastCrawledUrl();
		}
		String url = masterUrl + "?port=" + selfPort + "&status=" + status
				+ "&pagescrawled=" + pagesCrawled + "&lastcrawledurl="
				+ lastCrawledUrl;
		System.out.println("crawl worker : url - " + url);
		clientSocketOut.print("GET " + url + " HTTP/1.0\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.flush();
		return Http.parseResponse(socket);
	}

	/**
	 * Terminate.
	 */
	public void terminate()
	{
		run = false;
	}
}
