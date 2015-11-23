package edu.upenn.cis455.project.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.RobotsInfo;
import edu.upenn.cis455.project.crawler.CrawlWorkerServlet;
import edu.upenn.cis455.project.parsers.RobotParser;
import edu.upenn.cis455.project.storage.RobotsInfoDA;

public class HttpClient
{
	private Queue<String> urlQueue;
	private String content_type = null;
	
	public HttpClient(Queue<String> urlQueue){
		this.urlQueue = urlQueue;
	}
	
	public String getContent_type()
	{
		return content_type;
	}

	public boolean sendHead(String url) throws IOException
	{
		return sendHead(url,null);
	}
	
	/**
	 * Sends HEAD request
	 * @param url
	 * @param lastAccessed date
	 * @return true if response code is 200
	 * @throws IOException
	 */
	public boolean sendHead(String url, Date lastAccessed) throws IOException
	{
		URL req_url = new URL(url);
		
		if(url.startsWith("https://")){
			HttpsURLConnection connection = (HttpsURLConnection) req_url.openConnection();
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("HEAD");
			connection.setRequestProperty("User-Agent", "cis455crawler");
			
			if(lastAccessed!=null){
				SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				String d = sdf.format(lastAccessed);
				connection.setRequestProperty("If-Modified-Since", d);
			}
			
			if(connection.getResponseCode() == 301){
				String location = connection.getHeaderField("Location");
	//			System.out.println("Redirected to: "+location);
				urlQueue.enqueue(location);
			}
			
			if(connection.getResponseCode()!=200){
				System.out.println("Response code for "+url+" is: "+connection.getResponseCode());
				return false;
			}
			
//			if(!connection.getHeaderField("Content-Language").equals("en")){
//				return false;
//			}
			
			Integer length = connection.getContentLength();
			if(length > CrawlWorkerServlet.max_size*1000000){
				System.out.println("Document greater than max size!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				return false;
			}
			
			return true;
			
		}else if(url.startsWith("http://")){
			HttpURLConnection connection = (HttpURLConnection) req_url.openConnection();
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("HEAD");
			connection.setRequestProperty("User-Agent", "cis455crawler");
			
			if(lastAccessed!=null){
				SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				String d = sdf.format(lastAccessed);
				connection.setRequestProperty("If-Modified-Since", d);
			}
			
			if(connection.getResponseCode() == 301){
				String location = connection.getHeaderField("Location");
	//			System.out.println("Redirected to: "+location);
				urlQueue.enqueue(location);
			}
			
			if(connection.getResponseCode()!=200){
				System.out.println("Response code for "+url+" is: "+connection.getResponseCode());
				return false;
			}
			
//			if(!connection.getHeaderField("Content-Language").equals("en")){
//			return false;
//		}
			
			Integer length = connection.getContentLength();
			if(length > CrawlWorkerServlet.max_size*1000000){
				System.out.println("Document greater than max size!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				return false;
			}
			
			return true;
		
		}
		
		return false;
	}
	
	/**
	 * Fetch robots.txt and store in db
	 * @param robots url
	 * @throws Exception 
	 */
	public void fetchRobots(String robots,String domain) throws Exception
	{
		String robots_doc = fetch(robots);
		//Store robots in db
		RobotParser parser = new RobotParser();
		parser.parse(robots_doc);
		RobotsInfo info = new RobotsInfo();
		info.setDomain(domain);
		info.setLastAccessed(new Date());
		info.setRobotsInfo(parser.getInfo());
		RobotsInfoDA.putInfo(info);
		
	}

	/**
	 * Fetches the requested document and parses the url
	 * @param url
	 * @param urlQueue 
	 * @return the document in string format
	 * @throws IOException
	 */
	public String fetch(String url) throws IOException
	{
		URL req_url = new URL(url);
		InputStream input = null;
		Integer length = null;
		if(url.startsWith("https://")){
			HttpsURLConnection connection = (HttpsURLConnection) req_url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "cis455crawler");
			input = connection.getInputStream();
			
			length = connection.getContentLength();
			content_type = connection.getContentType();
		}else if(url.startsWith("http://")){
			HttpURLConnection connection = (HttpURLConnection) req_url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "cis455crawler");
			input = connection.getInputStream();
			
			length = connection.getContentLength();
			content_type = connection.getContentType();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		int total_read = 0;
		int b;
		StringBuilder s = new StringBuilder();
		if(length == -1)
			length = Integer.MAX_VALUE;
				
		while(total_read<length && ((b = br.read())!=-1)){
			s.append((char)b);
			total_read++;
		}
		
		return s.toString();
		
	}
	
	

	

	

}
