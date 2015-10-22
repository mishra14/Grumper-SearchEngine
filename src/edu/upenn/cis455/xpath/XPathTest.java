package edu.upenn.cis455.xpath;

import com.sleepycat.persist.model.Persistent;

/**
 * This class holds a XPath Test object
 * 
 * @author cis455
 *
 */
@Persistent
public class XPathTest
{

	private boolean isStep;
	private boolean isTextFilter;
	private boolean isContainsFilter;
	private boolean isAttFilter;
	private XPathStep step;
	private String attName;
	private String queryString;
	private int afterTest;

	public XPathTest()
	{
		this.isStep = false;
		this.isTextFilter = false;
		this.isAttFilter = false;
		this.isContainsFilter = false;
	}

	public boolean isStep()
	{
		return isStep;
	}

	public void setStep(boolean isStep)
	{
		this.isStep = isStep;
	}

	public boolean isTextFilter()
	{
		return isTextFilter;
	}

	public void setTextFilter(boolean isTextFilter)
	{
		this.isTextFilter = isTextFilter;
	}

	public boolean isContainsFilter()
	{
		return isContainsFilter;
	}

	public void setContainsFilter(boolean isContainsFilter)
	{
		this.isContainsFilter = isContainsFilter;
	}

	public boolean isAttFilter()
	{
		return isAttFilter;
	}

	public void setAttFilter(boolean isAttFilter)
	{
		this.isAttFilter = isAttFilter;
	}

	public XPathStep getStep()
	{
		return step;
	}

	public void setStep(XPathStep step)
	{
		this.step = step;
	}

	public String getAttName()
	{
		return attName;
	}

	public void setAttName(String attName)
	{
		this.attName = attName;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public void setQueryString(String queryString)
	{
		this.queryString = queryString;
	}

	public int getAfterTest()
	{
		return afterTest;
	}

	public void setAfterTest(int afterTest)
	{
		this.afterTest = afterTest;
	}

	@Override
	public String toString()
	{
		return "XPathTest [isStep=" + isStep + ", isTextFilter=" + isTextFilter
				+ ", isContainsFilter=" + isContainsFilter + ", isAttFilter="
				+ isAttFilter + ", \nstep=" + step + ", attName=" + attName
				+ ", queryString=" + queryString + ", afterTest=" + afterTest
				+ "]";
	}

}
