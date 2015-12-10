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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimerTask;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.http.Http;
import edu.upenn.cis455.project.http.HttpResponse;
import edu.upenn.cis455.project.storage.S3DocumentDA;
import edu.upenn.cis455.project.storage.S3UrlListDA;

public class PushToDB extends TimerTask
{
	
	private ArrayList<DocumentRecord> crawledDocs;
	private List<String>workers;
	private ArrayList<UrlList> urlMappings;
	
	public PushToDB(List<String> workers, ArrayList<DocumentRecord> crawledDocs, ArrayList<UrlList> urlMappings){
		this.crawledDocs = crawledDocs;
		this.workers = workers;
		this.urlMappings = urlMappings;
	}
	
	@Override
	public void run()
	{
//		System.out.println("!!!!!!!!!!!!!!!!!! SENDING PUSHDATA !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		int numWorkers;
		
		synchronized(workers){
			numWorkers = workers.size();
		}
		
//		System.out.println("[PUSHDB] Numworkers: "+numWorkers);
		for(int i=0;i<numWorkers;i++){
			File file = new File("./"+i+".txt");
			
			if(!file.exists())
				continue;
			
			System.out.println("File: "+file.getName());
			
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(file));
			}
			catch (FileNotFoundException e)
			{
				System.out.println("Error Getting Buffered Reader");
				e.printStackTrace();
			}
			
			StringBuilder body = new StringBuilder();
			ArrayList<String> post = new ArrayList<String>();
			
			try
			{
				body.append(br.readLine());
				String line;
				while((line=br.readLine())!=null){
					body.append(";"+line);
					if(body.toString().getBytes().length > 180000){
						System.out.println("SIZE EXCEEDED");
						post.add(body.toString());
						body = new StringBuilder();
						body.append(br.readLine());
					}
				}
				
				post.add(body.toString());
			}
			
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.println("SIZE OF POST BODY: "+body.toString().getBytes().length);
			String current_worker;
			
			synchronized(workers){
				if(i>=workers.size()){
					break;
				}else{
					current_worker = workers.get(i);
				}
			}
			
			String workerUrl = "http://" + current_worker + "/worker/pushdata";
			System.out.println("[Worker] : Sending Pushdata to - " + workerUrl);
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
			System.out.println("Sending pushdata to host: "+host+" port: "+port);
			Socket socket = null;
			PrintWriter clientSocketOut = null;
			HttpResponse response = null;
			try
			{
				for(String post_body: post){
					
//					System.out.println("[PUSHDATA] Opening new socket for port: "+port);
					socket = new Socket(host, port);
					clientSocketOut = new PrintWriter(new OutputStreamWriter(
							socket.getOutputStream()));
					
					clientSocketOut.print("POST " + url.toString() + " HTTP/1.0\r\n");
					clientSocketOut.print("Content-Length:" + post_body.length() + "\r\n");
					clientSocketOut
							.print("Content-Type:application/x-www-form-urlencoded\r\n");
					clientSocketOut.print("\r\n");
					clientSocketOut.print("urls="+post_body);
					clientSocketOut.print("\r\n");
					clientSocketOut.print("\r\n");
					clientSocketOut.flush();
//					System.out.println("Sent Post");
					
					response = null;
					if(socket!=null){
						response = Http.parseResponse(socket);
						if (response!=null && !response.getResponseCode().equalsIgnoreCase("200"))
						{
							System.out.println("[WORKER] : worker " + current_worker
									+ "DID NOT ACCEPT the crawl job");
							
						}else{
							System.out.println(current_worker+" ACCEPTED JOB!!");
						}
					}
					
					if(clientSocketOut != null){
						clientSocketOut.close();
						System.out.println("PrintWriter closed");
					}
					
					if(socket!=null){
						socket.close();
						System.out.println("Socket closed");
					}
					
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if(response!=null && response.getResponseCode().equalsIgnoreCase("200")){
				boolean result = file.delete();
				if(!result){
					System.out.println("FILE WAS NOT DELETED: "+file.getName());
				}else{
					System.out.println("FILE WAS DELETED SUCCESSFULLY!!");
				}
				System.out.println("[PushToDB] Sent pushdata successfully!!");
			}
		}
		
//		System.out.println("!!!!!!!!!!!!!!!!!! FINISHED PUSHDATA !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
//		System.out.println("PUSHING TO DB");
		S3DocumentDA s3 = new S3DocumentDA();
		synchronized(crawledDocs){
			for(DocumentRecord doc : crawledDocs){
//				System.out.println("Adding: "+doc.getDocumentId());
				String hashcode = s3.getHashCodeFromDynamo(doc.getDocumentId());
				
				s3.putDocument(doc);
				System.out.println("Added to s3: "+doc.getDocumentId());
				
				try
				{
					if(hashcode!=null && !hashcode.equals(Hash.hashKey(doc.getDocumentString())))
					{
						s3.deleteDocumentWithHashCode(hashcode);
						System.out.println("Deleted from s3: "+doc.getDocumentId()+" hashcode: "+hashcode);
					}
				}
				catch (NoSuchAlgorithmException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			crawledDocs.clear();
		}
		
		S3UrlListDA urlDA = new S3UrlListDA();
		
		synchronized(urlMappings){
			for(UrlList urllist: urlMappings){
				UrlList dbEntry = urlDA.getUrlList(urllist.getParentUrl());
				if(dbEntry!=null){
					HashSet <String> combinedSet = new HashSet<String>();
					combinedSet.addAll(dbEntry.getUrls());
					combinedSet.addAll(urllist.getUrls());
					dbEntry.setUrls(combinedSet);
					urlDA.putUrlList(dbEntry);
				}else{
					System.out.println("[Domain Does Not Exist] Adding url Mapping for : "+urllist.getParentUrl());
					urlDA.putUrlList(urllist);
				}
			}
			
			urlMappings.clear();
		}
		
//		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! FINISHED PUSHING TO DB !!!!!!!!!!!!!!!!!!!!!!!!!!!1");
		
	}
	
}
