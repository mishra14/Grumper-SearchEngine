package test.edu.upenn.cis455;

import org.junit.Test;

import edu.upenn.cis455.bean.Channel;
import edu.upenn.cis455.bean.DocumentRecord;
import edu.upenn.cis455.bean.User;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.DocumentRecordDA;
import edu.upenn.cis455.storage.UserDA;
import junit.framework.TestCase;

/**
 * This class tests the storage API by storing and then reading various entities
 * 
 * @author cis455
 *
 */
public class StorageTest extends TestCase
{

	@Test
	public void testUserStorageValidLookUp() throws Exception
	{
		User user = new User("ankit.mishra", "password");
		DBWrapper.openDBWrapper("./db");
		UserDA.putUser(user);
		User storedUser = UserDA.getUser(user.getUserName());
		DBWrapper.closeDBWrapper();
		assertEquals(storedUser.getUserName(), user.getUserName());
		assertEquals(storedUser.getPassword(), user.getPassword());
	}

	@Test
	public void testUserStorageInValidLookUp() throws Exception
	{
		User user = new User("ankit.mishra", "password");
		DBWrapper.openDBWrapper("./db");
		UserDA.putUser(user);
		User storedUser = UserDA.getUser("wrong username");
		DBWrapper.closeDBWrapper();
		assertEquals(storedUser, null);
	}

	@Test
	public void testChannelStorageValidLookUp() throws Exception
	{
		DBWrapper.openDBWrapper("./db");
		User user = new User("tess", "t");
		UserDA.putUser(user);
		Channel channel = new Channel("peace", "tess");
		DocumentRecord document = new DocumentRecord("ankitmishra.me",
				"content", true, false, 1000000);
		DocumentRecordDA.putDocument(document);
		channel.addDocumentId("ankitmishra.me");
		channel.addXPath("/rss/channel/item/title[@ID= \"war\" ]");
		ChannelDA.putChannel(channel);
		Channel storedChannel = ChannelDA.getChannel("peace");
		DBWrapper.closeDBWrapper();
		assertEquals(storedChannel.getChannelName(), channel.getChannelName());
		assertEquals(storedChannel.getUsername(), channel.getUsername());
		assertEquals(storedChannel.getDocumentIdList(),
				channel.getDocumentIdList());
		assertEquals(storedChannel.getxPaths(), channel.getxPaths());
	}

	@Test
	public void testChannelStorageInValidLookUp() throws Exception
	{
		DBWrapper.openDBWrapper("./db");
		User user = new User("tess", "t");
		UserDA.putUser(user);
		Channel channel = new Channel("peace", "tess");
		DocumentRecord document = new DocumentRecord("ankitmishra.me",
				"content", true, false, 1000000);
		DocumentRecordDA.putDocument(document);
		channel.addDocumentId("ankitmishra.me");
		channel.addXPath("/html");
		ChannelDA.putChannel(channel);
		Channel storedChannel = ChannelDA.getChannel("random channel name");
		DBWrapper.closeDBWrapper();
		assertEquals(storedChannel, null);

	}

}
