package edu.upenn.cis455.crawler.info;

import java.util.ArrayList;
import java.util.HashMap;

public class RobotsTxtInfo {

	private HashMap<String, ArrayList<String>> disallowedLinks;
	private HashMap<String, ArrayList<String>> allowedLinks;

	private HashMap<String, Integer> crawlDelays;
	private ArrayList<String> sitemapLinks;
	private ArrayList<String> userAgents;

	public RobotsTxtInfo() {
		disallowedLinks = new HashMap<String, ArrayList<String>>();
		allowedLinks = new HashMap<String, ArrayList<String>>();
		crawlDelays = new HashMap<String, Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
	}

	public void addDisallowedLink(String key, String value) {
		if (!disallowedLinks.containsKey(key)) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		} else {
			ArrayList<String> temp = disallowedLinks.get(key);
			if (temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
	}

	public void addAllowedLink(String key, String value) {
		if (!allowedLinks.containsKey(key)) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		} else {
			ArrayList<String> temp = allowedLinks.get(key);
			if (temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
	}

	public void addCrawlDelay(String key, Integer value) {
		crawlDelays.put(key, value);
	}

	public void addSitemapLink(String val) {
		sitemapLinks.add(val);
	}

	public void addUserAgent(String key) {
		userAgents.add(key);
	}

	public boolean containsUserAgent(String key) {
		return userAgents.contains(key);
	}

	public ArrayList<String> getDisallowedLinks(String key) {
		return disallowedLinks.get(key);
	}

	public ArrayList<String> getAllowedLinks(String key) {
		return allowedLinks.get(key);
	}

	public int getCrawlDelay(String key) {
		return crawlDelays.get(key);
	}

	public void print() {
		for (String userAgent : userAgents) {
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
		if (sitemapLinks.size() > 0) {
			System.out.println("# SiteMap Links");
			for (String sitemap : sitemapLinks)
				System.out.println(sitemap);
		}
	}

	public boolean crawlContainAgent(String key) {
		return crawlDelays.containsKey(key);
	}

	public static RobotsTxtInfo parseRobotsTxt(String robots) throws NumberFormatException{
		RobotsTxtInfo info = new RobotsTxtInfo();
		String[] lines = robots.split(System.getProperty("line.separator"));
		String userAgent = null;
		String disallow = null;
		String allow = null;
		int delay = -1;
		for(String line : lines)
		{
			line =line.trim();
			if(line.startsWith("User-agent") && line.contains(":"))
			{
				userAgent = line.split(":")[1].trim();
				info.addUserAgent(userAgent);
			}
			else if(line.startsWith("Disallow") && line.contains(":") && userAgent != null)
			{
				disallow = line.split(":")[1].trim();
				info.addDisallowedLink(userAgent, disallow);
			}
			else if(line.startsWith("Allow") && line.contains(":") && userAgent != null)
			{
				allow = line.split(":")[1].trim();
				info.addAllowedLink(userAgent, allow);
			}
			else if(line.startsWith("Crawl-delay") && line.contains(":") && userAgent != null)
			{
				delay = Integer.valueOf(line.split(":")[1].trim());
				info.addCrawlDelay(userAgent, delay);
			}
		}
		return info;
	}
}
