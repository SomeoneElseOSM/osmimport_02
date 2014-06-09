package uk.org.atownsend.osmimport_02;

import java.util.Hashtable;

/**
 * @author A.Townsend
 *
 * Could be used to store hashtables of strings, 
 * but currently just the one that we've read from the description file.
 */
public class DescriptionHashtable 
{

	private Hashtable<String, String> stringHash;

	public DescriptionHashtable() 
	{
		stringHash = new Hashtable<String, String>();
	}

	/* ------------------------------------------------------------------------------
	 * A hashtable stores only one value per key.
	 * 
	 * However, if we write the same key twice in the description file we want both
	 * of those to appear against the key, so there's an extra call to "get" here.
	 * 
	 * This means that if the description file contains:
	 * 003 webble
	 * 003 wubble
	 * 
	 * And the GPX comment is "wabble", the resulting comment will be:
	 * webble;wubble;wabble
	 * 
	 * Note that descriptions are stored against uppercase keys.
	 * ------------------------------------------------------------------------------ */
	public void put( String key, String data )
	{
		if ( stringHash.get( key.toUpperCase() ) == null )
		{
			stringHash.put( key.toUpperCase(), data );
		}
		else
		{
			stringHash.put( key.toUpperCase(), stringHash.get( key.toUpperCase() ) + ";" + data  );
		}
	}
	
	public String get( String key )
	{
		return stringHash.get( key.toUpperCase() );
	}

}
