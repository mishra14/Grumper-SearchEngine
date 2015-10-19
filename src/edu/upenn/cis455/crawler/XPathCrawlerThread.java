package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.http.HttpClient;

public class XPathCrawlerThread extends Thread {

	private int id;

	public XPathCrawlerThread(int id) {
		this.id = id;
	}

	public void run() {
		while (XPathCrawler.isRun()) {
			synchronized (XPathCrawler.getQueue()) {
				if (XPathCrawler.getQueue().getSize() > 0) {
					// dequeue the first url from the queue
					URL url = XPathCrawler.getQueue().dequeue();
					// fetch that document
					try {
						HttpClient httpClient = new HttpClient(url);
						Document document = httpClient.getDocument();
						if (document != null) {
							System.out.println(url + " : downloaded");
							System.out.println(document);
						}
					} catch (UnknownHostException e) {
						System.out
								.println("UnknownHostException while opening socket - "
										+ id + " - " + e);

					} catch (IOException e) {
						System.out
								.println("IOException while opening socket - "
										+ id + " - " + e);

					} catch (SAXException e) {
						System.out
								.println("SAXException while parsing document - "
										+ id + " - " + e);

					} catch (ParserConfigurationException e) {
						System.out
								.println("ParserConfigurationException while parsing document - "
										+ id + " - " + e);

					}

					// read all the url from the document

					// add all the read url into the back of the queue

					// try to fetch the next url
				} else {

					try {
						System.out.println("Waiting on url queue - " + id);
						XPathCrawler.getQueue().wait();
						System.out.println("Done waiting on url queue - " + id);
					} catch (InterruptedException e) {
						if (XPathCrawler.isRun()) {
							System.out
									.println("InterruptedException while wating on url queue on thread - "
											+ id + " - " + e);
						}
					}
				}

			}
		}
	}
}
