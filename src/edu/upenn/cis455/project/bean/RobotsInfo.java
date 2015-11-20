package edu.upenn.cis455.project.bean;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.project.crawler.info.RobotsTxtInfo;

@Entity
public class RobotsInfo{
	
	@PrimaryKey
	private String domain;
	
	private RobotsTxtInfo robotsInfo;
	private String agentMatch;
	private Date lastAccessed;
	
	public String getDomain()
	{
		return domain;
	}
	
	public void setDomain(String domain)
	{
		this.domain = domain;
	}
	
	public RobotsTxtInfo getRobotsInfo()
	{
		return robotsInfo;
	}
	
	public void setRobotsInfo(RobotsTxtInfo robotsInfo)
	{
		this.robotsInfo = robotsInfo;
		setAgentMatch("cis455crawler");
	}
	
	public String getAgentMatch()
	{
		return agentMatch;
	}
	
	private void setAgentMatch(String agentMatch)
	{
		if(robotsInfo.containsUserAgent(agentMatch)){
			this.agentMatch = agentMatch;
		}
		else if(robotsInfo.containsUserAgent("*")){
			this.agentMatch = "*";
		}else{
			this.agentMatch = "No agent found";
		}
	}
	
	public Date getLastAccessed()
	{
		return lastAccessed;
	}

	public void setLastAccessed(Date lastAccessed)
	{
		this.lastAccessed = lastAccessed;
	}
	
}
