package com.webonise.plugins;

import org.apache.maven.artifact.versioning.ComparableVersion;


/**Class representing boundary Version of a version-range
 * 
 * @author webonise
 */
public class LimitVersion
{
	private ComparableVersion version;
	private boolean limitIncluded;
	
	/**
	 * @param versionWithBraces : String version starting or ended by '[' or '(' representing bound inclusive or exclusive
	 */
	public LimitVersion(String versionWithBraces)
	{
		if(versionWithBraces.contains("(")||versionWithBraces.contains(")"))
		{
			this.limitIncluded = false;
		}
		else
		{
			this.limitIncluded = true;	
		}
		this.version = new ComparableVersion(versionWithBraces.replaceAll("\\[|\\(|\\]|\\)", "")); 
	}
	
	/**Method checks weather version of current LimitVersion object
	 * is less than the ComparableVersion supplied as argument. 
	 * if limitIncluded flag is set, then it checks for less or equal
	 * 
	 * @param target : ComparableVersion object to compare to
	 * @return boolean
	 */
	public boolean versionIsLessThan(ComparableVersion target)
	{
		boolean isLess = false;
		if(this.limitIncluded && target.compareTo(this.version)>=0)
		{
			isLess = true;
		}
		else if(target.compareTo(this.version)>0)
		{
			isLess = true;
		}
		else
		{
			isLess = false;
		}
		
		return isLess;
	}
	/**Method checks weather version of current LimitVersion object
	 * is greater than the ComparableVersion supplied as argument.
	 * if limitIncluded flag is set, then it checks for greater or equal
	 * 
	 * @param target : ComparableVersion object to compare to
	 * @return boolean
	 */
	public boolean versionIsMoreThan(ComparableVersion target)
	{
		boolean isMore = false;
		if(this.limitIncluded && target.compareTo(this.version)<=0)
		{
			isMore = true;
		}
		else if(target.compareTo(this.version)<0)
		{
			isMore = true;
		}
		else
		{
			isMore = false;
		}
		return isMore;
	}

}
