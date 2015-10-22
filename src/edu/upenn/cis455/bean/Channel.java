package edu.upenn.cis455.bean;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.xpath.XPath;

@Entity
public class Channel
{
	@PrimaryKey
	private String channelId;
	private ArrayList<XPath> xPaths;
	private ArrayList<String> matchingDocuments;
	
}
