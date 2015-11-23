package edu.upenn.cis455.project.crawler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.RobotsInfo;
import edu.upenn.cis455.project.crawler.info.URLInfo;
import edu.upenn.cis455.project.http.HttpClient;
import edu.upenn.cis455.project.parsers.HtmlParser;
import edu.upenn.cis455.project.storage.RobotsInfoDA;
import edu.upenn.cis455.project.storage.S3DocumentDA;

public class CrawlerThread implements Runnable{
	
	private Queue<String> urlQueue;
	private WorkerStatus status;
	private int self_id;
	private int num_workers;
	private ArrayList<DocumentRecord> crawledDocs;
	
	public CrawlerThread(Queue<String> urlQueue, WorkerStatus status, int self_id, int num_workers,ArrayList<DocumentRecord> crawledDocs){
		this.urlQueue = urlQueue;
		this.status = status;
		this.self_id = self_id;
		this.num_workers = num_workers;
		this.crawledDocs = crawledDocs;
	}
	
	@Override
	public void run(){
		
		while(true){
			String url = null;
			if(urlQueue.getSize() == 0){
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}else{
				url = urlQueue.dequeue();
			}
			
			if(url == null){
				continue;
			}
			
			URLInfo urlinfo = new URLInfo(url);
			
			String domain = urlinfo.getHostName();
			String filepath = urlinfo.getFilePath();
			String protocol = urlinfo.getProtocol();
			
			HttpClient httpclient = new HttpClient(urlQueue);
			
			/**
			 * Hash domain
			 */
			int idx = self_id;
			try
			{	
				System.out.println("DOMAIN being hashed: "+domain);
				idx = Hash.hashKey(domain, num_workers);
			}
			catch (NoSuchAlgorithmException e1)
			{
				System.out.println("Could not hash domain: "+e1);
			}
			
			if(idx!=self_id){
				System.out.println("Writing url ["+url+"] to worker "+idx);
				WriteToFile.write(url, idx);
				continue;
			}
			
			/**
			 * Check if document already exists in db. Then send head to check if modified
			 */
			
			S3DocumentDA s3 = new S3DocumentDA();
			DocumentRecord doc = s3.getDocument(url);
			
			if(doc!=null){
				long last = doc.getLastCrawled();
				Date date = new Date(last);
				try
				{
					if(!httpclient.sendHead(url, date)){
						continue;
					}
				}
				catch (IOException e)
				{
					System.out.println("Could not send head request: "+e);
				}
			}
			
			//check if robots.txt exists 
			if(RobotsInfoDA.contains(domain)){
				RobotsInfo info = RobotsInfoDA.getInfo(domain);
				String agent_match = info.getAgentMatch();
				
				if(agent_match.equals("No agent found")){
					//No matching user agent was found
					//DBWrapper.closeDBWrapper();
					continue;
				}
				
//				System.out.println("Agent match: "+agent_match);
				
				if(info.getRobotsInfo() == null){
					System.out.println("robots info is null");
				}
				
				Integer delay = info.getRobotsInfo().getCrawlDelay(agent_match);
				
				Date lastAccessed = info.getLastAccessed();
				Date currentTime = new Date();
				
				//Crawl delay is still active - put back URL in the queue
				if((currentTime.getTime() - lastAccessed.getTime()) < delay*1000){
					urlQueue.enqueue(url);
					//DBWrapper.closeDBWrapper();
					continue;
				}
				
				System.out.println("IN THREAD: url- "+url);

				//Send HEAD request to check if robots txt has been modified
				String robots = protocol+domain+"/robots.txt";
				boolean modified = false;
				
				try
				{
					modified = httpclient.sendHead(robots,lastAccessed);
				}
				catch (IOException e)
				{
					System.out.println("Error sending HEAD request: "+e);
				}
				
				if(modified){
					//If it has been modified, fetch updated robots
					try
					{
						httpclient.fetchRobots(robots,domain);
					}
					catch (Exception e)
					{
						System.out.println("Error fetching Robots.txt modified: "+e);
					}
					urlQueue.enqueue(url);
					//DBWrapper.closeDBWrapper();
					continue;
				}
				
				System.out.println("Robots hasn't been modified");
				
				boolean allowed = false;
				if(info.getRobotsInfo().getAllowedLinks(agent_match)!=null){
					if(info.getRobotsInfo().getAllowedLinks(agent_match).contains(filepath)){
						allowed = true;
					}
				}
				
				//If robots.txt has not been modified, check for allowed/disallowed links
				boolean disallowed = false;
				if(info.getRobotsInfo().getDisallowedLinks(agent_match)!=null){
					for(String link : info.getRobotsInfo().getDisallowedLinks(agent_match)){
//						System.out.println("DISALLOWED: "+link);
//						System.out.println("FILEPATH: "+filepath);
						if(filepath.startsWith(link)){
							disallowed = true;
							break;
						}
					}
				}
				
				if(disallowed && !allowed){
					//DBWrapper.closeDBWrapper();
					System.out.println("URL: "+url+" IS NOT ALLOWED");
					continue;
				}
				
			}else{
				//Robots info for this url does not exist
				System.out.println("Robots txt does not exists");
				String old_url = url;
				url = protocol+domain+"/robots.txt";
				try
				{
					httpclient.fetchRobots(url,domain);
					urlQueue.enqueue(old_url);
				}
				catch (Exception e)
				{
					System.out.println("Error fetching Robots.txt new: ");
					e.printStackTrace();
					
				}
				//DBWrapper.closeDBWrapper();
				continue;
			}
			
			System.out.println("Fetching document for url: "+url);
			//Fetch the actual url document
			String document = null;
			Date current = null;
			try
			{
				if(httpclient.sendHead(url)){
					document = httpclient.fetch(url);
					
					//Update last accessed time
					RobotsInfo info = RobotsInfoDA.getInfo(domain);
					current = new Date();
					info.setLastAccessed(current);
					RobotsInfoDA.putInfo(info);
					//DBWrapper.closeDBWrapper();

				}else{
					continue;
				}
			}
			catch (IOException e)
			{
				System.out.println("Could not fetch document: "+e);
				//DBWrapper.closeDBWrapper();
				continue;
			}
			
			//Update worker status
			synchronized(status){
				status.setLastCrawledUrl(url);
				status.incrementCount();
			}
			
			String content_type = httpclient.getContent_type();
			
			//Parse html documents for links
			if(content_type.startsWith("text/html")){
				HtmlParser.parse(document, url, urlQueue);
			}
			
			//TODO add document to db
			DocumentRecord docrecord = new DocumentRecord(url,document,current.getTime());
			synchronized(this.crawledDocs){
				crawledDocs.add(docrecord);
			}
			
		}
	}

}
