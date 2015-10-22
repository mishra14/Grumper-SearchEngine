package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper
{

	private static String envDirectory = null;

	private static Environment env;
	private static EntityStore store;

	public static void openDBWrapper(String path) throws Exception
	{
		System.out.println("DB at - " + path);
		File dbFile = new File(path);
		if (!dbFile.isDirectory())
		{
			if (!dbFile.mkdirs())
			{
				throw new Exception("DB File make dir failed");
			}
			System.out.println("Created new DB Dir at - " + path);
		}
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		envConfig.setTransactional(true);
		storeConfig.setTransactional(true);
		env = new Environment(dbFile, envConfig);
		store = new EntityStore(env, "Crawler Store", storeConfig);
	}

	public static void closeDBWrapper() throws DatabaseException
	{
		if (store != null)
		{
			store.close();
		}
		if (env != null)
		{
			env.close();
		}

	}

	public static String getEnvDirectory()
	{
		return envDirectory;
	}

	public static Environment getDbEnv()
	{
		return env;
	}

	public static EntityStore getStore()
	{
		return store;
	}

	/*
	 * public static void main(String args[]) throws Exception {
	 * openDBWrapper("./db/"); System.out.println(UserDA.putUser(new
	 * User("ankit.mishra", "hie")));
	 * System.out.println(UserDA.getUser("ankit.mishra")); XPath xPath = new
	 * XPath("/html"); System.out.println(XPathDA.putXPath(xPath));
	 * System.out.println(XPathDA.getXPath(xPath.getxPath())); closeDBWrapper();
	 * }
	 */

}
