/**
 * 
 */
package uk.org.atownsend.osmimport_02;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author A.Townsend
 *
 */
public class TestWaypoint 
{

	/**
	 * testUC()
	 * 
	 * Basic test that when we create a waypoint and look at the values; they're what we expect.
	 */
	@Test
	public void testUC() 
	{
		Waypoint waypoint = new Waypoint( "S", "AA", "QWERTY", "1", "2", "cmt", "desc", "Blue Pin", "3", "wibble" );
		
		assertTrue( waypoint.getInitial().equals( "S" ));
		assertTrue( waypoint.getTwoLetterAbbreviation().equals( "AA" ));
		assertTrue( waypoint.getWaypointNumber().equals( "QWERTY" ));
		assertTrue( waypoint.getLat().equals( "1" ));
		assertTrue( waypoint.getLon().equals( "2" ));
		assertTrue( waypoint.getCmt().equals( "cmt" ));
		assertTrue( waypoint.getDesc().equals( "desc" ));
		assertTrue( waypoint.getEle().equals( "3" ));
		assertTrue( waypoint.getSym().equals( "Blue Pin" ));
		assertTrue( waypoint.getFileNameFoundIn().equals( "wibble" ));
	}

	/**
	 * testLC()
	 * 
	 * Check that when we create a waypoint, "key" values are uppercased.
	 */
	@Test
	public void testLC() 
	{
		Waypoint waypoint = new Waypoint( "s", "aa", "qwerty", "0", "0", "cmt", "desc", "Blue Pin", "0", "wibble" );
		
		assertTrue( waypoint.getInitial().equals( "S" ));
		assertTrue( waypoint.getTwoLetterAbbreviation().equals( "AA" ));
		assertTrue( waypoint.getWaypointNumber().equals( "QWERTY" ));
		assertTrue( waypoint.getSym().equals( "Blue Pin" ));
	}

}
