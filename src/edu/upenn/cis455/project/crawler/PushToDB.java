package edu.upenn.cis455.project.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.http.Http;
import edu.upenn.cis455.project.http.HttpResponse;
import edu.upenn.cis455.project.storage.DBWrapper;
import edu.upenn.cis455.project.storage.QueueDA;
import edu.upenn.cis455.project.storage.S3DocumentDA;

public class PushToDB extends TimerTask
{
	private int numWorkers;
	private ArrayList<DocumentRecord> crawledDocs;
	private List<String>workers;
	
	public PushToDB(int numWorkers, List<String> workers, ArrayList<DocumentRecord> crawledDocs){
		this.numWorkers = numWorkers;
		this.crawledDocs = crawledDocs;
		this.workers = workers;
	}
	
	@Override
	public void run()
	{
//		System.out.println("!!!!!!!!!!!!!!!!!! SENDING PUSHDATA !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		for(int i=0;i<numWorkers;i++){
			File file = new File("./"+i+".txt");
			
			if(!file.exists())
				continue;
			
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(file));
			}
			catch (FileNotFoundException e)
			{
				System.out.println("Error sending pushdata");
				e.printStackTrace();
			}
			StringBuilder body = new StringBuilder();
			
			try
			{
				body.append(br.readLine());
				String line;
				while((line=br.readLine())!=null){
					body.append(";"+line);
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String workerUrl = "http://" + this.workers.get(i) + "/worker/pushdata";
			System.out.println("Worker : sending pushdata to - " + workerUrl);
			URL url = null;
			try
			{
				url = new URL(workerUrl);
			}
			catch (MalformedURLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String host = url.getHost();
			int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
			Socket socket = null;
			PrintWriter clientSocketOut = null;
			try
			{
				socket = new Socket(host, port);
				clientSocketOut = new PrintWriter(new OutputStreamWriter(
						socket.getOutputStream()));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			clientSocketOut.print("POST " + url.toString() + " HTTP/1.0\r\n");
			clientSocketOut.print("Content-Length:" + body.toString().length() + "\r\n");
			clientSocketOut
					.print("Content-Type:application/x-www-form-urlencoded\r\n");
			clientSocketOut.print("\r\n");
			clientSocketOut.print(body.toString());
			clientSocketOut.print("\r\n");
			clientSocketOut.print("\r\n");
			clientSocketOut.flush();
			
			HttpResponse response = null;
			try
			{
				response = Http.parseResponse(socket);
				if (!response.getResponseCode().equalsIgnoreCase("200"))
				{
					System.out.println("Master : worker " + workers.get(i)
							+ "did not accept the crawl job");
					
				}
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			clientSocketOut.close();
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(response.getResponseCode().equalsIgnoreCase("200")){
				boolean result = file.delete();
				if(!result){
					System.out.println("FILE WAS NOT DELETED: "+file.getName());
				}else{
					System.out.println("FILE WAS DELETED SUCCESSFULLY!!");
				}
			}
		}
		
//		System.out.println("!!!!!!!!!!!!!!!!!! FINISHED PUSHDATA !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
//		System.out.println("PUSHING TO DB");
		S3DocumentDA s3 = new S3DocumentDA();
		synchronized(crawledDocs){
			for(DocumentRecord doc : crawledDocs){
				System.out.println("Adding: "+doc.getDocumentId());
				s3.putDocument(doc);
			}
			
			crawledDocs.clear();
		}
		
//		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! FINISHED PUSHING TO DB !!!!!!!!!!!!!!!!!!!!!!!!!!!1");
		
	}
	
}
