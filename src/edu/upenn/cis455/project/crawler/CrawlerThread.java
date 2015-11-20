package edu.upenn.cis455.project.crawler;

import java.io.IOException;
import java.util.Date;

import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.RobotsInfo;
import edu.upenn.cis455.project.crawler.info.URLInfo;
import edu.upenn.cis455.project.http.HttpClient;
import edu.upenn.cis455.project.storage.RobotsInfoDA;

public class CrawlerThread implements Runnable{
	
	private Queue<String> urlQueue;
	
	public CrawlerThread(Queue<String> urlQueue){
		this.urlQueue = urlQueue;
	}
	
	@Override
	public void run(){
		
		while(true){
			
			String url = urlQueue.dequeue();
			URLInfo urlinfo = new URLInfo(url);
			
			String domain = urlinfo.getHostName();
			String filepath = urlinfo.getFilePath();
			String protocol = urlinfo.getProtocol();
			
			/**
			 * TODO Hash domain??
			 */
			
			/**
			 * TODO check if document already exists in db. Then send head to check if modified
			 */
			
			//TODO check if robots.txt exists 
			if(RobotsInfoDA.contains(domain)){
				RobotsInfo info = RobotsInfoDA.getInfo(domain);
				String agent_match = info.getAgentMatch();
				
				if(agent_match.equals("No agent found")){
					//No matching user agent was found
					continue;
				}
				
				int delay = info.getRobotsInfo().getCrawlDelay(agent_match);
				Date lastAccessed = info.getLastAccessed();
				Date currentTime = new Date();
				
				//Crawl delay is still active - put back URL in the queue
				if((currentTime.getTime() - lastAccessed.getTime()) < delay*1000){
					urlQueue.enqueue(url);
					continue;
				}
				
				//Send HEAD request to check if robots txt has been modified
				String robots = protocol+domain+"/robots.txt";
				boolean modified = false;
				
				try
				{
					modified = HttpClient.sendHead(robots,lastAccessed);
				}
				catch (IOException e)
				{
					System.out.println("Error sending HEAD request: "+e);
				}
				
				if(modified){
					//If it has been modified, fetch updated robots
					try
					{
						HttpClient.fetchRobots(robots,domain);
					}
					catch (Exception e)
					{
						System.out.println("Error fetching Robots.txt: "+e);
					}
					urlQueue.enqueue(url);
					continue;
				}
				
				//If robots.txt has not been modified, check for allowed/disallowed links
				boolean disallowed = false;
				if(info.getRobotsInfo().getDisallowedLinks(agent_match)!=null){
					for(String link : info.getRobotsInfo().getDisallowedLinks(agent_match)){
						if(filepath.startsWith(link)){
							disallowed = true;
							break;
						}
					}
				}
				
				if(info.getRobotsInfo().getAllowedLinks(agent_match)!=null){
					if(info.getRobotsInfo().getAllowedLinks(agent_match).contains(filepath)){
						disallowed = false;
					}
				}
				
				if(disallowed){
					continue;
				}
				
				
			}else{
				//Robots info for this url does not exist
				urlQueue.enqueue(url);
				url = protocol+domain+"/robots.txt";
				try
				{
					HttpClient.fetchRobots(url,domain);
				}
				catch (Exception e)
				{
					System.out.println("Error fetching Robots.txt: "+e);
				}
				continue;
			}
			
			//Fetch the actual url document
			String document;
			try
			{
				if(HttpClient.sendHead(url)){
					document = HttpClient.fetch(url);
					
					//Update last accessed time
					RobotsInfo info = RobotsInfoDA.getInfo(domain);
					info.setLastAccessed(new Date());
					RobotsInfoDA.putInfo(info);
				}
			}
			catch (IOException e)
			{
				System.out.println("Could not fetch document: "+e);
				continue;
			}
			
			
			
			
		}
	}

}
