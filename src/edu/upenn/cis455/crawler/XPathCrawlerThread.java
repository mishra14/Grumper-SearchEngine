package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.upenn.cis455.http.HttpClient;
import edu.upenn.cis455.http.HttpsClient;

public class XPathCrawlerThread extends Thread {

	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private ArrayList<URL> urls;
	private int id;

	public XPathCrawlerThread(int id) {
		this.id = id;
		urls = new ArrayList<URL>();
	}

	public void run() {
		while (XPathCrawler.isRun()) {
			URL url = null;
			while ( XPathCrawler.isRun() && url == null ) {
				synchronized (XPathCrawler.getQueue()) {
					if (XPathCrawler.getQueue().getSize() > 0) {
						// dequeue the first url from the queue
						url = XPathCrawler.getQueue().dequeue();
					} else {
						try {
							System.out.println("Waiting on url queue - " + id);
							XPathCrawler.getQueue().wait();
							System.out.println("Done waiting on url queue - "
									+ id);
						} catch (InterruptedException e) {
							if (XPathCrawler.isRun()) {
								System.out
										.println("InterruptedException while wating on url queue on thread - "
												+ id + " - " + e);
							}
						}
					}
				}
				if( XPathCrawler.isRun() && url != null)
				{
					//will come here only after reading a url
					//mark url as seen
					synchronized (XPathCrawler.getSeenUrls()) {
						XPathCrawler.getSeenUrls().add(url);
					}
					// fetch that document
					try {
						Document document = null;
						System.out.println("Thread "+id+" obtained url - "+url);
						if (url.getProtocol().equalsIgnoreCase(HTTP)) {
							HttpClient httpClient = new HttpClient(url);
							document = httpClient.getDocument();
						} else if (url.getProtocol().equalsIgnoreCase(HTTPS)) {
							HttpsClient httpsClient = new HttpsClient(url);
							document = httpsClient.getDocument();
						}

						if (document != null ) {
							urls.clear(); // remove previous urls
							System.out.println(url + " : downloaded");
							// read all the url from the document
							NodeList urlNodes = document.getElementsByTagName("a");
							for (int i = 0; i < urlNodes.getLength(); i++) {
								Node urlNode = urlNodes.item(i);
								if (urlNode.getAttributes() != null && urlNode.getAttributes()
										.getNamedItem("href") !=null) {
									String urlString = urlNode.getAttributes().getNamedItem("href").getNodeValue();
									if(urlString.startsWith("http://")||urlString.startsWith("https://"))
									{
										urls.add(new URL(urlString));
									}
									else
									{
										urls.add(new URL(url,urlString));

									}
								}
							}
							System.out.println("URLS - "+urls);
							if(urls.size()>0)
							{
								//check for seen urls
								synchronized (XPathCrawler.getSeenUrls()) {
									urls.removeAll(XPathCrawler.getSeenUrls());
								}
								// add all the read url into the back of the queue
								synchronized (XPathCrawler.getQueue()) {
									XPathCrawler.getQueue().enqueueAll(urls);
								}
							}

							// try to fetch the next url
						} else {
							System.out.println(url + " : error in download");
						}
					} catch (UnknownHostException e) {
						System.out
								.println("UnknownHostException while opening socket - "
										+ id + " - " + e);

					} catch (IOException e) {
						System.out.println("IOException while opening socket - "
								+ id + " - " + e);

					} catch (SAXException e) {
						System.out.println("SAXException while parsing document - "
								+ id + " - " + e);

					} catch (ParserConfigurationException e) {
						System.out
								.println("ParserConfigurationException while parsing document - "
										+ id + " - " + e);
					}
				}
			}
		}
	}
}
