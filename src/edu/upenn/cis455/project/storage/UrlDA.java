package edu.upenn.cis455.project.storage;

import java.util.ArrayList;
import java.util.Date;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.UrlRecord;

public class UrlDA
{
	public static ArrayList<String> getURLS(){ //Return null if no queue exists
		
		ArrayList <String> urls = null;
		if (DBWrapper.getStore() != null){
			PrimaryIndex<String, UrlRecord> urlRecord = DBWrapper.getStore().getPrimaryIndex(String.class, UrlRecord.class);
			if(urlRecord!=null){
				EntityCursor<UrlRecord> url_cursor = urlRecord.entities();
				try{
					int idx = 1;
					urls = new ArrayList<String>();
					for(UrlRecord record : url_cursor){
						urls.add(record.getUrl());
						url_cursor.delete();
						idx++;
						if(idx == Queue.MAX)
							break;
					}
				}finally{
					url_cursor.close();
				}
			}
		}
		
		if(urls == null)
			return null;
		
		return urls;
		
	}
	
	public static void putUrl(String url){
		
		UrlRecord record = new UrlRecord(url);
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, UrlRecord> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class,
							UrlRecord.class);
			if (userPrimaryIndex != null)
			{
				userPrimaryIndex.put(record);
			}
		}
		
	}
	
//	public static void main(String [] args) throws Exception{
//		DBWrapper.openDBWrapper("/usr/share/jetty/db");
//		ArrayList<String> urls = UrlDA.getURLS();
//		for(String url: urls){
//			System.out.println("URL: "+url);
//		}
//		DBWrapper.closeDBWrapper();
//		
//	}
}
