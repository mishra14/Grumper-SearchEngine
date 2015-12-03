package edu.upenn.cis455.project.pagerank;

import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.storage.DynamoDA;

public class Map extends Mapper<NullWritable, BytesWritable, Text, Text>
{

	private DynamoDA<Float> dynamo = new DynamoDA<Float>(
			"edu.upenn.cis455.project.pagerank", Float.class);

	@Override
	public void map(NullWritable key, BytesWritable value, Context context)
			throws IOException, InterruptedException
	{
		UrlList urlList = getUrlList(value);
		if (urlList != null)
		{
			HashSet<String> forwardLinks = (HashSet<String>) urlList.getUrls();
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
				Float rankVar = edgeWeight;
				String rankString = rankVar.toString();
				context.write(new Text(forwardLink), new Text(rankString));
			}
		}

	}

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
}
