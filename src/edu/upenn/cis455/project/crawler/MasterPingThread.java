package edu.upenn.cis455.project.crawler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.upenn.cis455.project.http.Http;
import edu.upenn.cis455.project.http.HttpResponse;

public class MasterPingThread extends Thread
{

	/** The socket. */
	private Socket socket;

	private static long INACTIVE_INTERVAL = 30000;
	private static long SLEEP_INTERVAL = 10000;
	private Map<String, WorkerStatus> workers;

	private List<String> workerList;

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
	public MasterPingThread(Map<String, WorkerStatus> workers,
			List<String> workerList)
	{
		this.workerList = workerList;
		this.workers = workers;
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
				synchronized (workerList)
				{

//					System.out
//							.println("crawl master ping thread : updating active worker list");
					updateWorkers();
					StringBuilder workerString = buildWorkerString();
					for (String worker : workerList)
					{
						String body = "numworkers=" + workerList.size() + "&"
								+ workerString.toString();
						sendUpdatedListToWorker(worker, body);
					}
				}
			}
			/*catch (InterruptedException e)
			{
				System.out.println("Ping Thread interrupted");
				e.printStackTrace();
			}*/
			catch (UnknownHostException e)
			{
				System.out.println("Ping Thread UnknownHostException");
				e.printStackTrace();
			}
			catch (IOException e)
			{
				System.out.println("Ping Thread IOException");
				e.printStackTrace();
			}
			try
			{
				sleep(SLEEP_INTERVAL);
			}
			catch (InterruptedException e)
			{
				System.out.println("Ping Thread InterruptedException");
				e.printStackTrace();
			}

		}
	}

	private StringBuilder buildWorkerString()
	{
		StringBuilder workerString = new StringBuilder();
		for (int i = 0; i < workerList.size(); i++)
		{
			workerString.append("worker" + (i + 1) + "=" + workerList.get(i));
			if (i < workerList.size() - 1)
			{
				workerString.append("&");
			}
		}
		return workerString;
	}

	private void sendUpdatedListToWorker(String worker, String body)
			throws UnknownHostException, IOException
	{
		String workerUrl = "http://" + worker + "/worker/updateworkers";
//		System.out.println("master : updating worker list to - " + workerUrl);
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
			System.out.println("Crawl master ping thread : worker " + worker
					+ "did not accept the updated worker list");
		}
	}

	/**
	 * Terminate.
	 */
	public void terminate()
	{
		run = false;
	}

	private void updateWorkers()
	{
//		System.out.println("crawl master ping thread : updating");
		ArrayList<String> inActiveWorkers = new ArrayList<String>();
		for (Map.Entry<String, WorkerStatus> entry : workers.entrySet())
		{
			long now = (new Date()).getTime();
			if (now - entry.getValue().getTimestamp() > INACTIVE_INTERVAL)
			{
				inActiveWorkers.add(entry.getKey());
			}
		}
		workerList.removeAll(inActiveWorkers);
		synchronized (workers)
		{
			workers.keySet().removeAll(inActiveWorkers);
		}
//		System.out.println("crawl master ping thread : removing workers - "
//				+ inActiveWorkers);
//		System.out.println("crawl master ping thread : updated worker List - "
//				+ workerList);
//		System.out.println("crawl master ping thread : updated worker map - "
//				+ workers);

	}
}
