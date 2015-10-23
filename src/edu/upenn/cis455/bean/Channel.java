package edu.upenn.cis455.bean;

import java.util.HashSet;
import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import edu.upenn.cis455.xpath.XPath;

@Entity
public class Channel
{
	@PrimaryKey
	private String channelName;
	@SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = XPath.class, onRelatedEntityDelete = DeleteAction.ABORT)
	private HashSet<String> xPaths;
	@SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = DocumentRecord.class, onRelatedEntityDelete = DeleteAction.NULLIFY)
	private HashSet<String> documentIdList;

	public Channel()
	{
	}

	public Channel(String channelName)
	{
		this.channelName = channelName;
		xPaths = new HashSet<String>();
		documentIdList = new HashSet<String>();

	}

	public String getChannelName()
	{
		return channelName;
	}

	public void setChannelName(String channelName)
	{
		this.channelName = channelName;
	}

	public HashSet<String> getxPaths()
	{
		return xPaths;
	}

	public void setxPaths(HashSet<String> xPaths)
	{
		this.xPaths = xPaths;
	}

	public HashSet<String> getDocumentIdList()
	{
		return documentIdList;
	}

	public void setDocumentIdList(HashSet<String> documentIdList)
	{
		this.documentIdList = documentIdList;
	}

	public boolean addDocumentId(String documentId)
	{
		if (!documentIdList.contains(documentId))
		{
			return documentIdList.add(documentId);
		}
		return false;
	}

	public boolean removeDocumentId(String documentId)
	{
		return documentIdList.remove(documentId);
	}

	public boolean addXPath(String xPathString)
	{
		if (!xPaths.contains(xPathString))
		{
			return xPaths.add(xPathString);
		}
		return false;
	}

	public boolean removeXPath(String xPathString)
	{
		return xPaths.remove(xPathString);
	}

	@Override
	public String toString()
	{
		return "Channel [channelName=" + channelName + ", xPaths=" + xPaths
				+ ", documentIdList=" + documentIdList + "]";
	}

}
