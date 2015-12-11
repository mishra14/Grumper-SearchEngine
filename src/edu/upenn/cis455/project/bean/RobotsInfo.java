package edu.upenn.cis455.project.bean;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.project.crawler.info.RobotsTxtInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class RobotsInfo.
 */
@Entity
public class RobotsInfo
{

	/** The domain. */
	@PrimaryKey
	private String domain;

	/** The robots info. */
	private RobotsTxtInfo robotsInfo;

	/** The agent match. */
	private String agentMatch;

	/** The last accessed. */
	private Date lastAccessed;

	/**
	 * Gets the domain.
	 *
	 * @return the domain
	 */
	public String getDomain()
	{
		return domain;
	}

	/**
	 * Sets the domain.
	 *
	 * @param domain
	 *            the new domain
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	/**
	 * Gets the robots info.
	 *
	 * @return the robots info
	 */
	public RobotsTxtInfo getRobotsInfo()
	{
		return robotsInfo;
	}

	/**
	 * Sets the robots info.
	 *
	 * @param robotsInfo
	 *            the new robots info
	 */
	public void setRobotsInfo(RobotsTxtInfo robotsInfo)
	{
		this.robotsInfo = robotsInfo;
		setAgentMatch("cis455crawler");
	}

	/**
	 * Gets the agent match.
	 *
	 * @return the agent match
	 */
	public String getAgentMatch()
	{
		return agentMatch;
	}

	/**
	 * Sets the agent match.
	 *
	 * @param agentMatch
	 *            the new agent match
	 */
	private void setAgentMatch(String agentMatch)
	{
		if (robotsInfo.containsUserAgent(agentMatch))
		{
			this.agentMatch = agentMatch;
		}
		else if (robotsInfo.containsUserAgent("*"))
		{
			this.agentMatch = "*";
		}
		else
		{
			this.agentMatch = "No agent found";
		}
	}

	/**
	 * Gets the last accessed.
	 *
	 * @return the last accessed
	 */
	public Date getLastAccessed()
	{
		return lastAccessed;
	}

	/**
	 * Sets the last accessed.
	 *
	 * @param lastAccessed
	 *            the new last accessed
	 */
	public void setLastAccessed(Date lastAccessed)
	{
		this.lastAccessed = lastAccessed;
	}

}
