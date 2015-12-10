package edu.upenn.cis455.project.bean;

// TODO: Auto-generated Javadoc
/**
 * The Class EmrResult.
 */
public class EmrResult
{
	
	/** The key. */
	private String key;
	
	/** The value. */
	private String value;
	
	/** The valid. */
	private boolean valid;

	/**
	 * Instantiates a new emr result.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public EmrResult(String key, String value)
	{
		super();
		this.key = key;
		this.value = value;
		valid = true;
	}

	/**
	 * Instantiates a new emr result.
	 *
	 * @param pairLine the pair line
	 */
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

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * Sets the valid.
	 *
	 * @param valid the new valid
	 */
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "EmrResult [key=" + key + ", value=" + value + ", valid="
				+ valid + "]";
	}

}
