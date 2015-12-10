package edu.upenn.cis455.project.crawler.info;

import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.persist.model.Persistent;

// TODO: Auto-generated Javadoc
/**
 * Class to hold information obtained by parsing robots.txt
 * 
 * @author cis455
 *
 */
@Persistent
public class RobotsTxtInfo
{

	/** The disallowed links. */
	private HashMap<String, ArrayList<String>> disallowedLinks;
	
	/** The allowed links. */
	private HashMap<String, ArrayList<String>> allowedLinks;

	/** The crawl delays. */
	private HashMap<String, Integer> crawlDelays;
	
	/** The sitemap links. */
	private ArrayList<String> sitemapLinks;

	/** The user agents. */
	private ArrayList<String> userAgents;

	/**
	 * Instantiates a new robots txt info.
	 */
	public RobotsTxtInfo()
	{
		disallowedLinks = new HashMap<String, ArrayList<String>>();
		allowedLinks = new HashMap<String, ArrayList<String>>();
		crawlDelays = new HashMap<String, Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
	}

	/**
	 * Adds the disallowed link.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addDisallowedLink(String key, String value)
	{
		if (!disallowedLinks.containsKey(key))
		{
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
		else
		{
			ArrayList<String> temp = disallowedLinks.get(key);
			if (temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
	}

	/**
	 * Adds the allowed link.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addAllowedLink(String key, String value)
	{
		if (!allowedLinks.containsKey(key))
		{
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
		else
		{
			ArrayList<String> temp = allowedLinks.get(key);
			if (temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
	}

	/**
	 * Adds the crawl delay.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addCrawlDelay(String key, Integer value)
	{
		crawlDelays.put(key, value);
	}

	/**
	 * Adds the sitemap link.
	 *
	 * @param val the val
	 */
	public void addSitemapLink(String val)
	{
		sitemapLinks.add(val);
	}

	/**
	 * Adds the user agent.
	 *
	 * @param key the key
	 */
	public void addUserAgent(String key)
	{
		userAgents.add(key);
	}

	/**
	 * Contains user agent.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean containsUserAgent(String key)
	{
		return userAgents.contains(key);
	}

	/**
	 * Gets the disallowed links.
	 *
	 * @param key the key
	 * @return the disallowed links
	 */
	public ArrayList<String> getDisallowedLinks(String key)
	{
		return disallowedLinks.get(key);
	}

	/**
	 * Gets the allowed links.
	 *
	 * @param key the key
	 * @return the allowed links
	 */
	public ArrayList<String> getAllowedLinks(String key)
	{
		return allowedLinks.get(key);
	}

	/**
	 * Gets the crawl delay.
	 *
	 * @param key the key
	 * @return the crawl delay
	 */
	public int getCrawlDelay(String key)
	{
		if (crawlDelays.get(key) == null)
		{
			// System.out.println("Crawl delay not set for this agent: "+key);
			return 1;
		}
		return crawlDelays.get(key);
	}

	/**
	 * Prints the.
	 */
	public void print()
	{
		for (String userAgent : userAgents)
		{
			System.out.println("User-Agent: " + userAgent);
			ArrayList<String> dlinks = disallowedLinks.get(userAgent);
			if (dlinks != null)
				for (String dl : dlinks)
					System.out.println("Disallow: " + dl);
			ArrayList<String> alinks = allowedLinks.get(userAgent);
			if (alinks != null)
				for (String al : alinks)
					System.out.println("Allow: " + al);
			if (crawlDelays.containsKey(userAgent))
				System.out
						.println("Crawl-Delay: " + crawlDelays.get(userAgent));
			System.out.println();
		}
		if (sitemapLinks.size() > 0)
		{
			System.out.println("# SiteMap Links");
			for (String sitemap : sitemapLinks)
				System.out.println(sitemap);
		}
	}

	/**
	 * Crawl contain agent.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean crawlContainAgent(String key)
	{
		return crawlDelays.containsKey(key);
	}

	/**
	 * Parses the robots txt.
	 *
	 * @param robots the robots
	 * @return the robots txt info
	 * @throws NumberFormatException the number format exception
	 */
	public static RobotsTxtInfo parseRobotsTxt(String robots)
			throws NumberFormatException
	{
		RobotsTxtInfo info = new RobotsTxtInfo();
		String[] lines = robots.split(System.getProperty("line.separator"));
		String userAgent = null;
		String disallow = null;
		String allow = null;
		int delay = -1;
		for (String line : lines)
		{
			line = line.trim();
			if (line.startsWith("User-agent") && line.contains(":"))
			{
				if (line.split(":").length > 1)
				{
					userAgent = line.split(":")[1].trim();
					info.addUserAgent(userAgent);
				}
			}
			else if (line.startsWith("Disallow") && line.contains(":")
					&& userAgent != null)
			{
				if (line.split(":").length > 1)
				{
					disallow = line.split(":")[1].trim();
					info.addDisallowedLink(userAgent, disallow);
				}
			}
			else if (line.startsWith("Allow") && line.contains(":")
					&& userAgent != null)
			{
				if (line.split(":").length > 1)
				{
					allow = line.split(":")[1].trim();
					info.addAllowedLink(userAgent, allow);
				}
			}
			else if (line.startsWith("Crawl-delay") && line.contains(":")
					&& userAgent != null)
			{
				if (line.split(":").length > 1)
				{
					delay = Integer.valueOf(line.split(":")[1].trim());
					info.addCrawlDelay(userAgent, delay);
				}
			}
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "RobotsTxtInfo [disallowedLinks=" + disallowedLinks
				+ ", allowedLinks=" + allowedLinks + ", crawlDelays="
				+ crawlDelays + ", sitemapLinks=" + sitemapLinks
				+ ", userAgents=" + userAgents + "]";
	}

}
