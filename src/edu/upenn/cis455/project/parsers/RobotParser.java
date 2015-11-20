package edu.upenn.cis455.project.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.project.crawler.info.RobotsTxtInfo;

public class RobotParser
{
		private static Pattern comment = Pattern.compile("\\s*#.*");
		private RobotsTxtInfo info;
		
		public RobotsTxtInfo getInfo()
		{
			return info;
		}

		public void parse(String text){
			info = new RobotsTxtInfo();
			String [] lines = text.split("\n");
			String last_agent = null;
			
			for(String line : lines){
				Matcher matcher = comment.matcher(line);
				//We do not need comments
				if(matcher.matches())
					continue;
				if(line.length()==0)
					continue;
				
				String [] temp = line.split(":");
				String header = temp[0].trim().toLowerCase();
				String value = temp[1].trim();
				if(header.equals("user-agent")){
					last_agent = value;
					info.addUserAgent(value);
				}
				else if(header.equals("disallow")){
					info.addDisallowedLink(last_agent, value);
				}
				else if(header.equals("crawl-delay")){
					int val = Integer.parseInt(value);
					info.addCrawlDelay(last_agent, val);
				}
				else if(header.equals("allow")){
					info.addAllowedLink(last_agent, value);
				}
				else if(header.equals("sitemap")){
					info.addSitemapLink(value);
				}
			} //end of parsing
		}
}
