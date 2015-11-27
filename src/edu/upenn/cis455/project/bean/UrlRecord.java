package edu.upenn.cis455.project.bean;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.project.crawler.info.URLInfo;

@Entity
public class UrlRecord
{
	@PrimaryKey
	String url;
	String domain;
	
	public UrlRecord(){
		
	}
	
	public UrlRecord(String url){
		this.url = url;
		URLInfo info = new URLInfo(url);
		this.domain = info.getHostName();
	}
	
	public String getUrl()
	{
		return url;
	}

	public String getDomain()
	{
		return domain;
	}
}
