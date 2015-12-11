package test.edu.upenn.cis455.project;

import org.junit.Test;

import edu.upenn.cis455.project.bean.User;
import edu.upenn.cis455.project.storage.DBWrapper;
import edu.upenn.cis455.project.storage.UserDA;
import junit.framework.TestCase;

// TODO: Auto-generated Javadoc
/**
 * This class tests the storage API by storing and then reading various
 * entities.
 *
 * @author cis455
 */
public class StorageTest extends TestCase
{

	/**
	 * Test user storage valid look up.
	 *
	 * @throws Exception
	 *             the exception
	 */
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

	/**
	 * Test user storage in valid look up.
	 *
	 * @throws Exception
	 *             the exception
	 */
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
