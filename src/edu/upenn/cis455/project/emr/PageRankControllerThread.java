package edu.upenn.cis455.project.emr;

public class PageRankControllerThread extends Thread
{
	private EmrController controller;

	public PageRankControllerThread(EmrController controller)
	{
		this.controller = controller;
	}

	public void run()
	{
		try
		{
			//create new cluster
			controller.createCluster();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
