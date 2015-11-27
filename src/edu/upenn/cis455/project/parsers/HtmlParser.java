package edu.upenn.cis455.project.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.storage.UrlDA;


public class HtmlParser
{
	public static void parse(String content, String url, Queue<String> urlQueue){
		System.out.println("PARSING: "+url);
		Document doc = Jsoup.parse(content, url, Parser.htmlParser());
	    extractLinks(doc, urlQueue);
	}

	private static void extractLinks(Document doc, Queue<String> urlQueue) {
		Elements links = doc.select("a[href]");
		for (Element link : links) {
            String link_to_be_added = link.attr("abs:href");
            boolean added = urlQueue.enqueue(link_to_be_added);
            if(!added){
//            	System.out.println("[CAPACITY EXCEEDED] Adding to db");
            	UrlDA.putUrl(link_to_be_added);
            }
        }
	}
	
}
