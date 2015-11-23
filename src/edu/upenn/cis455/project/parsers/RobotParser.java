package edu.upenn.cis455.project.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.project.crawler.info.RobotsTxtInfo;

public class RobotParser
{
		private Pattern comment = Pattern.compile("\\s*#.*");
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
				if(matcher.matches()){
//					System.out.println("COMMENT LINE");
					continue;
				}
				if(line.length()==0)
					continue;
				
				if(!line.contains(":")){
					continue;
				}
				
//				System.out.println("LINE: "+line);
				String [] temp = line.split(":",2);
				if(temp.length != 2){
//					System.out.println("ERROR: Parsing robots.txt for line: "+line);
					continue;
				}
				
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
