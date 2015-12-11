package edu.upenn.cis455.project.emr;

// TODO: Auto-generated Javadoc
/**
 * The Class PageRankControllerThread.
 */
public class PageRankControllerThread extends Thread
{

	/** The controller. */
	private EmrController controller;

	/**
	 * Instantiates a new page rank controller thread.
	 *
	 * @param controller
	 *            the controller
	 */
	public PageRankControllerThread(EmrController controller)
	{
		this.controller = controller;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		try
		{
			// create new cluster
			controller.createCluster();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
