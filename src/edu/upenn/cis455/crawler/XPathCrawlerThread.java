package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.upenn.cis455.bean.Channel;
import edu.upenn.cis455.bean.DocumentRecord;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.DocumentRecordDA;
import edu.upenn.cis455.storage.XPathDA;
import edu.upenn.cis455.xpath.XPath;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

/**
 * A Thread class that is executed by the XPathCrawler class
 * 
 * @author cis455
 *
 */
public class XPathCrawlerThread extends Thread
{

	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private int id;
	private static final String CRAWLER_AGENT = "cis455crawler";
	private static final String ALL_AGENT = "*";

	public XPathCrawlerThread(int id)
	{
		this.id = id;
	}

	public void run()
	{
		System.out.println("Crawler Thread " + id + " started");
		while (XPathCrawler.isRun())
		{
			URL url = null;
			while (XPathCrawler.isRun() && url == null)
			{
				synchronized (XPathCrawler.getQueue())
				{
					if (XPathCrawler.getQueue().getSize() > 0)
					{
						// dequeue the first url from the queue
						url = XPathCrawler.getQueue().dequeue();
					}
					else
					{
						try
						{
							// System.out.println("Waiting on url queue - " +
							// id);
							XPathCrawler.getQueue().wait();
							// System.out.println("Done waiting on url queue - "+
							// id);
						}
						catch (InterruptedException e)
						{
							if (XPathCrawler.isRun())
							{
								System.out
										.println("InterruptedException while wating on url queue on thread - "
												+ id + " - ");
								e.printStackTrace();
							}
							else
							{
								break;
							}
						}
					}
				}
				if (XPathCrawler.isRun() && url != null)
				{

					// will come here only after reading a url
					// mark url as seen
					synchronized (XPathCrawler.getSeenUrls())
					{
						XPathCrawler.getSeenUrls().add(url);
					}

					try
					{
						boolean shouldCrawl = true;
						// check for robots.txt in the local map
						RobotsTxtInfo info = getRobotsTxt(url);
						if (info != null
								&& (info.containsUserAgent(CRAWLER_AGENT) || info
										.containsUserAgent(ALL_AGENT)))
						{
							// System.out.println("info - " + info);
							ArrayList<String> disallowedUrls;
							if (info.containsUserAgent(CRAWLER_AGENT))
							{
								disallowedUrls = info
										.getDisallowedLinks(CRAWLER_AGENT);
							}
							else
							{
								disallowedUrls = info
										.getDisallowedLinks(ALL_AGENT);
							}
							// System.out.println("disallowed links - "+
							// disallowedUrls);
							for (String disallowed : disallowedUrls)
							{
								String disallowedUrl = url.getProtocol()
										+ "://" + url.getHost() + disallowed;
								String urlToCompare = url.toString();

								if (urlToCompare.startsWith(disallowedUrl))
								{
									// we should not crawl this url
									System.out.println(url + " : disallowed");
									shouldCrawl = false;
									break;
								}
							}
						}
						if (shouldCrawl)
						{
							// fetch that document
							DocumentRecord documentRecord = getDocumentRecord(url);
							if (documentRecord != null)
							{
								// process document
								processDocument(url, documentRecord);
								// extract urls
								ArrayList<URL> urls = extractUrls(
										documentRecord, url);
								// System.out.println("URLS - "+urls);

								if (urls.size() > 0)
								{ // check for seen urls
									synchronized (XPathCrawler.getSeenUrls())
									{
										urls.removeAll(XPathCrawler
												.getSeenUrls());
									}
									// add all the read url into the back of the
									// queue
									synchronized (XPathCrawler.getQueue())
									{
										XPathCrawler.getQueue()
												.enqueueAll(urls);
									}
								}

							}
							else
							{
								System.out
										.println(url + " : error in download");
							}

						}
						// try to fetch the next url
					}
					catch (UnknownHostException e)
					{
						System.out
								.println("UnknownHostException while opening socket - "
										+ id + " - " + e);
						e.printStackTrace();

					}
					catch (IOException e)
					{
						System.out
								.println("IOException while opening socket - "
										+ id + " - ");
						e.printStackTrace();

					}
					catch (SAXException e)
					{
						System.out
								.println("SAXException while parsing document - "
										+ id + " - " + e);
						e.printStackTrace();

					}
					catch (ParserConfigurationException e)
					{
						System.out
								.println("ParserConfigurationException while parsing document - "
										+ id + " - " + e);
						e.printStackTrace();
					}
				}
			}
		}
	}

	public RobotsTxtInfo getRobotsTxt(URL url) throws UnknownHostException,
			IOException
	{
		RobotsTxtInfo info = null;
		synchronized (XPathCrawler.getRobotTxts())
		{
			if (XPathCrawler.getRobotTxts().containsKey(url.getHost()))
			{
				// robots.txt found use this to check for disallowed url
				info = XPathCrawler.getRobotTxts().get(url.getHost());
				// System.out.println(url + " : robots.txt already fetched");
			}
			else
			{
				URL robotsUrl = new URL(new URL(url.getProtocol() + "://"
						+ url.getHost()), "robots.txt");
				// robots.txt does not exist; fetch it
				if (url.getProtocol().equalsIgnoreCase(HTTP))
				{

					HttpClient httpClient = new HttpClient(robotsUrl);
					info = httpClient.getRobotsTxt();
					// System.out.println("Obtained new robots.txt from " +
					// url);
					// info.print();
				}
				else if (url.getProtocol().equalsIgnoreCase(HTTPS))
				{
					HttpsClient httpsClient = new HttpsClient(robotsUrl);
					info = httpsClient.getRobotsTxt();
					// System.out.println("Obtained new robots.txt from " +
					// url);
					// info.print();
				}
				if (info == null)
				{
					System.out.println(url + " : Error in fetching robots.txt");
				}
				else
				{
					XPathCrawler.getRobotTxts().put(url.getHost(), info);
				}
			}
		}
		return info;
	}

	public DocumentRecord getDocumentRecord(URL url)
			throws NumberFormatException, UnknownHostException, IOException,
			SAXException, ParserConfigurationException
	{
		DocumentRecord documentRecord = null;
		if (url.getProtocol().equalsIgnoreCase(HTTP))
		{
			HttpClient httpClient = new HttpClient(url);
			documentRecord = httpClient.getDocument();
		}
		else if (url.getProtocol().equalsIgnoreCase(HTTPS))
		{
			HttpsClient httpsClient = new HttpsClient(url);
			documentRecord = httpsClient.getDocument();
		}
		return documentRecord;
	}

	public ArrayList<URL> extractUrls(DocumentRecord documentRecord, URL url)
			throws ParserConfigurationException, SAXException, IOException
	{
		ArrayList<URL> urls = null;
		if (documentRecord != null && documentRecord.getDocument() != null)
		{
			urls = new ArrayList<URL>();
			System.out.println(url + " : downloaded");
			// store document in db
			// System.out.println("Storing Document Record - "+documentRecord+"\n"+DocumentRecordDA.putDocument(documentRecord));
			DocumentRecordDA.putDocument(documentRecord);
			// System.out.println(url + " : Stored");
			// read all the url from the document
			NodeList urlNodes = documentRecord.getDocument()
					.getElementsByTagName("a");
			for (int i = 0; i < urlNodes.getLength(); i++)
			{
				Node urlNode = urlNodes.item(i);
				if (urlNode.getAttributes() != null
						&& urlNode.getAttributes().getNamedItem("href") != null)
				{
					String urlString = urlNode.getAttributes()
							.getNamedItem("href").getNodeValue();
					URL newUrl;
					if (urlString.startsWith("http://")
							|| urlString.startsWith("https://"))
					{
						newUrl = new URL(urlString);
						urls.add(newUrl);
					}
					else
					{
						if (url.toString().endsWith("/"))
						{
							newUrl = new URL(url + urlString);
							urls.add(newUrl);
						}
						else
						{
							newUrl = new URL(url.toString().substring(0,
									url.toString().lastIndexOf("/") + 1)
									+ urlString);
							urls.add(newUrl);
						}
					}
				}
			}
		}
		return urls;
	}

	private void processDocument(URL url, DocumentRecord documentRecord)
			throws ParserConfigurationException, SAXException, IOException
	{
		if (documentRecord.isXml())
		{
			ArrayList<XPath> xPathList = XPathDA.getAllXPaths();
			if (xPathList.isEmpty())
			{
				return;
			}
			XPathEngine engine = XPathEngineFactory.getXPathEngine();
			engine.setXPaths(xPathList);
			boolean[] result = engine.evaluate(documentRecord.getDocument());
			for (int i = 0; i < result.length; i++)
			{
				if (result[i])
				{
					for (String channelName : xPathList.get(i)
							.getChannelNames())
					{
						Channel channel = ChannelDA.getChannel(channelName);
						if (channel != null)
						{
							if (!channel.getDocumentIdList().contains(
									url.toString()))
							{
								channel.addDocumentId(url.toString());
							}
							ChannelDA.putChannel(channel);
						}
						else
						{
							System.out.println("channel : " + channelName
									+ " not found");
						}
					}
				}
			}
		}

	}
}
