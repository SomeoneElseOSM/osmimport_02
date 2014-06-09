/**
 * 
 */
package uk.org.atownsend.osmimport_02;

import java.util.Hashtable;

/**
 * @author A.Townsend
 * 
 * Used to store a hashtables of the Trailfiles that we've found on disk.
 * It's used when we're trying to find out which is the most recent file for a trail, 
 * reading it in, updating it, and writing it out with the current date and time.
 */
public class TrailfileHashtable 
{
	/**
	 * qqq06 Javadoc
	 */
	private Hashtable<String, Trailfile> trailfileHash;
	
	public TrailfileHashtable() 
	{
		trailfileHash = new Hashtable<String, Trailfile>();
	}
	
	public void put( Trailfile trailfile )
	{
		/* ------------------------------------------------------------------------------
		 * We aim to keep a record of the most recent file that we've found on disk by
		 * looking at its file name (we'll ignore attributes in case these haven't been
		 * preserved by copying to and fro).
		 * 
		 * If we don't have an example for this trail, we'll just add it.
		 * ------------------------------------------------------------------------------ */
		if ( trailfileHash.get( trailfile.getTwoLetterAbbreviation().toUpperCase() ) == null )
		{
			trailfileHash.put( trailfile.getTwoLetterAbbreviation(), trailfile );
		}
		else
		{
			/* ------------------------------------------------------------------------------
			 * If the file already exists, we'll need to check whether the one we've found
			 * is a later date and time than the one that we already know about.
			 * ------------------------------------------------------------------------------ */
			if ( trailfileHash.get( trailfile.getTwoLetterAbbreviation().toUpperCase() ).getYyyymmdd().compareTo( trailfile.getYyyymmdd() ) > 0 )
			{
				/* ------------------------------------------------------------------------------
				 * We don't need to do anything; the yyyymmdd in trailfileHash is already bigger
				 * ------------------------------------------------------------------------------ */
			}
			else
			{
				if ( trailfileHash.get( trailfile.getTwoLetterAbbreviation().toUpperCase() ).getYyyymmdd().compareTo( trailfile.getYyyymmdd() ) == 0 )
				{
					/* ------------------------------------------------------------------------------
					 * The yyyymmdd values are the same so we need to compare hhmmss
					 * ------------------------------------------------------------------------------ */

					if ( trailfileHash.get( trailfile.getTwoLetterAbbreviation().toUpperCase() ).getHhmmss().compareTo( trailfile.getHhmmss() ) > 0 )
					{
						/* ------------------------------------------------------------------------------
						 * We don't need to do anything; the hhmmss in trailfileHash is already bigger
						 * ------------------------------------------------------------------------------ */
					}
					else
					{
						if ( trailfileHash.get( trailfile.getTwoLetterAbbreviation().toUpperCase() ).getHhmmss().compareTo( trailfile.getHhmmss() ) == 0 )
						{
							/* ------------------------------------------------------------------------------
							 * The yyyymmdd and hhmmss values are the same.  This shouldn't happen qqq09
							 * but we'll ignore it for now.
							 * ------------------------------------------------------------------------------ */
						}
						else
						{
							/* ------------------------------------------------------------------------------
							 * Our new file is for a later time, so store in in place of our current one.
							 * ------------------------------------------------------------------------------ */
							trailfileHash.put( trailfile.getTwoLetterAbbreviation(), trailfile );
						}
					}
				}
				else
				{
					/* ------------------------------------------------------------------------------
					 * Our new file is for a later date, so store in in place of our current one.
					 * ------------------------------------------------------------------------------ */
					trailfileHash.put( trailfile.getTwoLetterAbbreviation(), trailfile );
				}
			}
		}

	}
	
	public Trailfile get( String twoLetterAbbreviation )
	{
		return trailfileHash.get( twoLetterAbbreviation );
	}

}
