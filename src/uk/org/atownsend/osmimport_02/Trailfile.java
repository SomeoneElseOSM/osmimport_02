/**
 * 
 */
package uk.org.atownsend.osmimport_02;

/**
 * @author A.Townsend
 * 
 * The Trailfile class is used to represent files on disk that contain waypoints and routes 
 * that together make up a route or routes.
 * 
 *  For example, one for the "Robin Hood Way" could contain a very large number of waypoints,
 *  and a number of trails (for sections of the main route split to make it easier to manage, 
 *  and also for spurs off the main route).
 *  
 *  A file on disk might be SRH_20130928.gpx or SRH_20130928x.gpx (where x is a single incrementing letter).
 *  S2S_20130928_hhmm.gpx and S2S_20130928_hhmmss.gpx would also be valid.
 *  
 *  All will be shoehorned into the S2S_20130928_hhmmss.gpx format with "x" being assigned to the seconds 
 *  place (value still between A and Z, or whatever the single character was).
 *  
 *  In addition to "S2S" files, "base_data" files (containing waypoints NOT part of a trail indicating
 *  "an unsurveyed footpath may run from here") are also valid.
 *  
 *  Some special cases also are - "OA" for general POIs that aren't "a footpath might start from here"
 *  and "TR" for other markers that don't need to go into base data.
 *
 */
public class Trailfile 
{
	/* ------------------------------------------------------------------------------
	 * Some of the same data is found in the "Waypoint" class.
	 * 
	 *  We don't store "initial" because it's always "S" for non-base-data files.
	 *  "twoLetterAbbreviation" is "  " (two spaces) for "base data".
	 * ------------------------------------------------------------------------------ */
	private String twoLetterAbbreviation;
	private String yyyymmdd;
	private String hhmmss;
	private String fileName;

	/**
	 * qqq06 Javadoc
	 */
	public Trailfile() 
	{
		this.twoLetterAbbreviation = "";
		this.yyyymmdd = "";
		this.hhmmss = "";
	}
	
	
	public Trailfile( String passed_twoLetterAbbreviation, String passed_yyyymmdd, String passed_hhmmss, String passed_fileName ) 
	{
		twoLetterAbbreviation = passed_twoLetterAbbreviation.toUpperCase();
		yyyymmdd = passed_yyyymmdd.toUpperCase();
		hhmmss = passed_hhmmss.toUpperCase();
		//qqq08 probably should not do this
		fileName = passed_fileName.toUpperCase();

		/* ------------------------------------------------------------------------------
		 * If the "hhmmss" value is passed as a single character, it'll be "A", "B", etc.
		 * The idea is that a dated file with "B" on the end is "more recent" than one
		 * with "A".  We cheat and shoehorn this values into a seconds value.
		 * ------------------------------------------------------------------------------ */
		if ( hhmmss.length() == 1 )
		{
			hhmmss = "00000" + hhmmss;
		}
		
		/* ------------------------------------------------------------------------------
		 * If seconds, minutes, or hours values are missed off, pad with zeroes.
		 * ------------------------------------------------------------------------------ */
		while ( hhmmss.length() < 6 )
		{
			hhmmss = hhmmss + "0";
		}
	}
	
	
	/**
	 * @return the twoLetterAbbreviation
	 */
	public String getTwoLetterAbbreviation() 
	{
		return twoLetterAbbreviation;
	}

	
	/**
	 * @param twoLetterAbbreviation the twoLetterAbbreviation to set
	 */
	public void setTwoLetterAbbreviation( String twoLetterAbbreviation ) 
	{
		this.twoLetterAbbreviation = twoLetterAbbreviation.toUpperCase();
	}


	/**
	 * @return the yyyymmdd
	 */
	public String getYyyymmdd() 
	{
		return yyyymmdd;
	}

	
	/**
	 * @param passed_yyyymmdd the yyyymmdd to set
	 */
	public void setYyyymmdd( String passed_yyyymmdd ) 
	{
		this.yyyymmdd = passed_yyyymmdd.toUpperCase();
	}

	/**
	 * @return the hhmmss
	 */
	public String getHhmmss() 
	{
		return hhmmss;
	}

	
	/**
	 * @param passed_hhmmss the hhmmss to set
	 */
	public void setHhmmss( String passed_hhmmss ) 
	{
		this.hhmmss = passed_hhmmss.toUpperCase();
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() 
	{
		return fileName;
	}

	
	/**
	 * @param passed_fileName the fileName to set
	 */
	public void setFileName( String passed_fileName ) 
	{
		//qqq08 probably should not do this
		this.fileName = passed_fileName.toUpperCase();
	}
}
