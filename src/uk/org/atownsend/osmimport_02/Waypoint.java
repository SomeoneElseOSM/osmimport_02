/**
 * qqq06 Javadoc
 */
package uk.org.atownsend.osmimport_02;

/**
 * @author A.Townsend
 *
 * The "Waypoint" class stores all the details that we know about a waypoint 
 * such as one that we've read from a GPX file.
 * 
 * All the data is found in a GPX, apart from "fileNameFoundIn", which is used 
 * when going through the hashtable of GPX waypoints to see which file on disk
 * it's been found in.
 * 
 *  For example - if a waypoint in the GPX "DOS123456" is found, then that
 *  will have come from a base_data file such as "base_data_yyyymmdda.gpx"
 *  and should be changed in there from  SOS123456 to DOS123456 (creating
 *  a new base data_file with a later date or trailing letter ("a" in 
 *  the example above).
 *  
 *  "lat" and "lon" are attributes of the XML node corresponding to the waypoint 
 *  in the GPX; the other ex-GPX info is a child of that node.
 */
public class Waypoint 
{
	/* ------------------------------------------------------------------------------
	 * The initial, twoLetterAbbreviation and waypointNumber together make up the 
	 * name in the GPX 
	 * ------------------------------------------------------------------------------ */
	private String initial;
	private String twoLetterAbbreviation;
	private String waypointNumber;
	
	private String lat;
	private String lon;
	
	private String cmt;
	private String desc;
	private String time;
	private String sym;
	private String ele;
	private String fileNameFoundIn;

	public Waypoint() 
	{
		this.initial = "";
		this.twoLetterAbbreviation = "";
		this.waypointNumber = "";
		
		this.lat = "";
		this.lon = "";

		this.cmt = "";
		this.desc = "";
		this.time = "";
		this.sym = "";
		this.ele = "";
		this.fileNameFoundIn = "";
	}

	
	public Waypoint( String initial, String twoLetterAbbreviation, String waypointNumber, 
			String lat, String lon, 
			String cmt, String desc, String time, String sym, String ele, String fileNameFoundIn ) 
	{
		/* ------------------------------------------------------------------------------
		 * "key" information we uppercase at this point.
		 * ------------------------------------------------------------------------------ */
		this.initial = initial.toUpperCase();
		this.twoLetterAbbreviation = twoLetterAbbreviation.toUpperCase();
		this.waypointNumber = waypointNumber.toUpperCase();
		
		this.lat = lat;
		this.lon = lon;
		
		this.cmt = cmt;
		this.desc = desc;
		this.time = time;
		
		/* ------------------------------------------------------------------------------
		 * There's an argument that the symbol should also be stored uppercased perhaps?
		 * ------------------------------------------------------------------------------ */
		this.sym = sym;
		this.ele = ele;
		this.fileNameFoundIn = fileNameFoundIn;
	}

	/**
	 * @return the first letter, such as D, E, S or V
	 */
	public String getInitial() 
	{
		return initial;
	}

	/**
	 * @param initial the first letter to set
	 */
	public void setInitial(String initial) 
	{
		this.initial = initial;
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
		this.twoLetterAbbreviation = twoLetterAbbreviation;
	}

	/**
	 * @return the waypointNumber
	 */
	public String getWaypointNumber() 
	{
		return waypointNumber;
	}

	/**
	 * @param waypointNumber the waypointNumber to set
	 */
	public void setWaypointNumber( String waypointNumber ) 
	{
		this.waypointNumber = waypointNumber;
	}

	/**
	 * @return the lat
	 */
	public String getLat() 
	{
		return lat;
	}


	/**
	 * @param lat the lat to set
	 */
	public void setLat( String lat ) 
	{
		this.lat = lat;
	}


	/**
	 * @return the lon
	 */
	public String getLon() 
	{
		return lon;
	}


	/**
	 * @param lon the lon to set
	 */
	public void setLon( String lon ) 
	{
		this.lon = lon;
	}


	/**
	 * @return the cmt
	 */
	public String getCmt() 
	{
		return cmt;
	}

	/**
	 * @param cmt the cmt to set
	 */
	public void setCmt( String cmt ) 
	{
		this.cmt = cmt;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() 
	{
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc( String desc ) 
	{
		this.desc = desc;
	}

	/**
	 * @return the time
	 */
	public String getTime() 
	{
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime( String time ) 
	{
		this.time = time;
	}

	/**
	 * @return the sym
	 */
	public String getSym() 
	{
		return sym;
	}

	/**
	 * @param sym the sym to set
	 */
	public void setSym( String sym ) 
	{
		this.sym = sym;
	}

	/**
	 * @return the ele
	 */
	public String getEle() 
	{
		return ele;
	}

	/**
	 * setEle()
	 * 
	 * Remove any decimal places and store the "elevation" string.
	 * 
	 * @param ele the ele to set
	 */
	public void setEle( String ele ) 
	{
		int decimal_place = ele.indexOf( "." );
		
		if ( decimal_place == -1 )
		{
			this.ele = ele;
		}
		else
		{
			if ( decimal_place == 0 )
			{
				this.ele = "0";
			}
			else
			{
				this.ele = ele.substring( 0, decimal_place );
			}
		}
	}

	/**
	 * @return the fileNameFoundIn
	 */
	public String getFileNameFoundIn() 
	{
		return fileNameFoundIn;
	}

	/**
	 * @param fileNameFoundIn the fileNameFoundIn to set
	 */
	public void setFileNameFoundIn( String fileNameFoundIn ) 
	{
		this.fileNameFoundIn = fileNameFoundIn;
	}

}
