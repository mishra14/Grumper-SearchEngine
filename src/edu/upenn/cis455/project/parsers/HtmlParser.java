package edu.upenn.cis455.project.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import edu.upenn.cis455.project.bean.Queue;

// TODO: Auto-generated Javadoc
/**
 * The Class HtmlParser.
 */
public class HtmlParser
{

	/**
	 * Parses the.
	 *
	 * @param content
	 *            the content
	 * @param url
	 *            the url
	 * @param urlQueue
	 *            the url queue
	 */
	public static void parse(String content, String url, Queue<String> urlQueue)
	{
		Document doc = Jsoup.parse(content, url, Parser.htmlParser());
		extractLinks(doc, urlQueue);
	}

	/**
	 * Extract links.
	 *
	 * @param doc
	 *            the doc
	 * @param urlQueue
	 *            the url queue
	 */
	private static void extractLinks(Document doc, Queue<String> urlQueue)
	{
		Elements links = doc.select("a[href]");
		for (Element link : links)
		{
			String link_to_be_added = link.attr("abs:href");
			urlQueue.enqueue(link_to_be_added);
		}
	}

}
