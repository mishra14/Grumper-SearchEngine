package edu.upenn.cis455.project.storage;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

// TODO: Auto-generated Javadoc
/**
 * DBWrapper class that is used to create, open and close berkley DB.
 *
 * @author cis455
 */
public class DBWrapper
{

	/** The env directory. */
	private static String envDirectory = null;

	/** The env. */
	private static Environment env;
	
	/** The store. */
	private static EntityStore store;

	/**
	 * Open db wrapper.
	 *
	 * @param path the path
	 * @throws Exception the exception
	 */
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
		envConfig.setLockTimeout(4, TimeUnit.SECONDS);
		storeConfig.setTransactional(true);
		env = new Environment(dbFile, envConfig);
		store = new EntityStore(env, "Crawler Store", storeConfig);
	}

	/**
	 * Close db wrapper.
	 *
	 * @throws DatabaseException the database exception
	 */
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

	/**
	 * Gets the env directory.
	 *
	 * @return the env directory
	 */
	public static String getEnvDirectory()
	{
		return envDirectory;
	}

	/**
	 * Gets the db env.
	 *
	 * @return the db env
	 */
	public static Environment getDbEnv()
	{
		return env;
	}

	/**
	 * Gets the store.
	 *
	 * @return the store
	 */
	public static EntityStore getStore()
	{
		return store;
	}
	
}
