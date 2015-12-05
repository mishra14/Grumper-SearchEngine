package edu.upenn.cis455.project.bean;

public class EmrResult
{
	private String key;
	private String value;
	private boolean valid;

	public EmrResult(String key, String value)
	{
		super();
		this.key = key;
		this.value = value;
		valid = true;
	}

	public EmrResult(String pairLine)
	{
		String[] splits = pairLine.split("\t", 2);
		if (splits.length == 2)
		{
			key = splits[0];
			value = splits[1];
			valid = true;
		}
		else
		{
			valid = false;
		}
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	@Override
	public String toString()
	{
		return "EmrResult [key=" + key + ", value=" + value + ", valid="
				+ valid + "]";
	}

}
