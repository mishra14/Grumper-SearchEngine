package edu.upenn.cis455.project.parsers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.storage.UrlDA;


public class HtmlParser
{
	
	public static void parse(String content, String url, Queue<String> urlQueue, ArrayList<UrlList> urlMappings){
//		System.out.println("PARSING: "+url);
		Document doc = Jsoup.parse(content, url, Parser.htmlParser());
		extractLinks(url, doc, urlQueue, urlMappings);
	}

	private static void extractLinks(String parent_url, Document doc, Queue<String> urlQueue, ArrayList<UrlList> urlMappings){
		HashSet <String> urls = new HashSet <String>();
		Elements links = doc.select("a[href]");
		for (Element link : links) {
            String link_to_be_added = link.attr("abs:href");
            URL url = null;
			try
			{
				url = new URL(link_to_be_added);
			}
			catch (MalformedURLException e)
			{
//				System.out.println("Malformed for url: "+link_to_be_added);
//				e.printStackTrace();
				continue;
			}
//            URLInfo info = new URLInfo(link_to_be_added);
            if(url.getHost()!=null){
            	urls.add(url.getHost());
//            	System.out.println("Added to set: "+url.getHost());
            }
            
            boolean added = urlQueue.enqueue(link_to_be_added);
            if(!added){
//            	System.out.println("[CAPACITY EXCEEDED] Adding to db");
            	UrlDA.putUrl(link_to_be_added);
            }
        }
		
		Date date = new Date();
		
		URL url = null;
		try
		{
			url = new URL(parent_url);
		}
		catch (MalformedURLException e)
		{
			System.out.println("Malformed for parent url: "+parent_url);
			e.printStackTrace();
			return;
		}
		UrlList urllist = new UrlList(url.getHost(), urls, true, date.getTime());
		
		synchronized(urlMappings){
			urlMappings.add(urllist);
		}
	}
	
//	public static void main(String [] args){
//		URL url = null;
//		try
//		{
//			url = new URL("http://www.blog.coursera.org/");
//		}
//		catch (MalformedURLException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(url.getHost());
//		
//	}
	
}
