/**
 * 
 */
package uk.org.atownsend.osmimport_02;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author A.Townsend
 *
 * Could be used to store various hashtables of waypoints.  
 * Currently it's just used for the one that we've read from the input GPX file, since that's the only one
 * that we need to "remember" when processing other files of waypoints, routes and tracks.
 * 
 * A waypoint marking an unsurveyed path will have a name such as SOS123456, and will be in the base data.
 * When it's been surveyed it'll get edited on the GPS to DOS123456 and will need to be changed to that in
 * the base data so that it doesn't get extracted subsequently.
 */
public class WaypointHashtable  
{

	private Hashtable<String, Waypoint> waypointHash;
	
	public WaypointHashtable() 
	{
		waypointHash = new Hashtable<String, Waypoint>();
	}
	
	public void put( Waypoint waypoint )
	{
		waypointHash.put( waypoint.getWaypointNumber(), waypoint );
	}
	
	public Waypoint get( String waypointNumber )
	{
		/* ------------------------------------------------------------------------------
		 * When getting data from the Hashtable, make sure that we search with an 
		 * uppercase key.
		 * ------------------------------------------------------------------------------ */
		return waypointHash.get( waypointNumber.toUpperCase() );
	}

}
