package test.edu.upenn.cis455.project;

import org.junit.Test;

import edu.upenn.cis455.project.bean.User;
import edu.upenn.cis455.project.storage.DBWrapper;
import edu.upenn.cis455.project.storage.UserDA;
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

}
