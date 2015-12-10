package edu.upenn.cis455.project.pagerank;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.storage.DynamoDA;

// TODO: Auto-generated Javadoc
/**
 * The Class Map.
 */
public class Map extends Mapper<NullWritable, BytesWritable, Text, Text>
{

	/** The dynamo. */
	private DynamoDA<Float> dynamo = new DynamoDA<Float>(
			"edu.upenn.cis455.project.pagerank", Float.class);

	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN, org.apache.hadoop.mapreduce.Mapper.Context)
	 */
	@Override
	public void map(NullWritable key, BytesWritable value, Context context)
			throws IOException, InterruptedException
	{
		for (UrlList urlList : getUrlLists(value))
		{
			if (urlList != null)
			{
				HashSet<String> forwardLinks = (HashSet<String>) urlList
						.getUrls();
				String url = urlList.getParentUrl();
				Item item = dynamo.getItem("hostName", url);
				Float urlRank = new Float(1);
				if (item != null)
				{
					urlRank = item.getFloat("rank");
				}
				Float edgeWeight = urlRank / forwardLinks.size();
				for (String forwardLink : forwardLinks)
				{
					if (forwardLink != null && !forwardLink.isEmpty())
					{
						Float rankVar = edgeWeight;
						String rankString = rankVar.toString();
						context.write(new Text(forwardLink), new Text(
								rankString));
					}
				}
			}
		}
	}

	/**
	 * Gets the url list.
	 *
	 * @param value the value
	 * @return the url list
	 */
	private UrlList getUrlList(BytesWritable value)
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		UrlList urlList = null;
		try
		{
			urlList = mapper.readValue(new String(value.getBytes()),
					UrlList.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return urlList;
	}

	/**
	 * Gets the url lists.
	 *
	 * @param value the value
	 * @return the url lists
	 */
	private List<UrlList> getUrlLists(BytesWritable value)
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		List<UrlList> urlList = null;
		try
		{
			urlList = mapper.readValue(new String(value.getBytes()),
					new TypeReference<List<UrlList>>()
					{
					});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return urlList;
	}
}
