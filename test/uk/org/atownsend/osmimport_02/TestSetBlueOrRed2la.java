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
public class TestSetBlueOrRed2la {

	@Test
	public void test() 
	{
		Main testMain = new Main();
		Waypoint testWaypoint = new Waypoint();
		
/* ------------------------------------------------------------------------------
 * If lat is over 54.1854 is Cleveland Way; WU
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "57.20" );
		testWaypoint.setLon( "-2.6" );
		String test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "WU" ));
		
/* ------------------------------------------------------------------------------
 * Anything else lat over 53.8548 is around York; TO
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "54" );
		testWaypoint.setLon( "-2.6" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "TO" ));
		
/* ------------------------------------------------------------------------------
 * Anything else lat over 53.6297 is around North Trent; WS
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "53.7" );
		testWaypoint.setLon( "-2.6" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "WS" ));
		
/* ------------------------------------------------------------------------------
 * South of that, if lon is west of "-1.5234490"; OV
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "53.6" );
		testWaypoint.setLon( "-2.6" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OV" ));
		
/* ------------------------------------------------------------------------------
 * South of that, if lon is east of "-1.2227316"; OW
 * (test a few valid possibilities)
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "53.6" );
		testWaypoint.setLon( "-0.6" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OW" ));

		testWaypoint.setLat( "53.6" );
		testWaypoint.setLon( "+0.6" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OW" ));
		
		testWaypoint.setLat( "53.6" );
		testWaypoint.setLon( "6" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OW" ));

/* ------------------------------------------------------------------------------
 * If in the slot in the middle, if lat over "53.2159106"; OU
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "53.6" );
		testWaypoint.setLon( "-1.4" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OU" ));
		
/* ------------------------------------------------------------------------------
 * If in the slot in the middle, if lat under "53.1075187"; OX
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "53.0" );
		testWaypoint.setLon( "-1.4" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OX" ));
		
/* ------------------------------------------------------------------------------
 * If in the slot in the middle, anything else is OS.
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "53.2" );
		testWaypoint.setLon( "-1.4" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "OS" ));
		
/* ------------------------------------------------------------------------------
 * Invalid lat and lon
 * ------------------------------------------------------------------------------ */
		testWaypoint.setLat( "invalid" );
		testWaypoint.setLon( "-1.4" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "ZZ" ));
		
		testWaypoint.setLat( "53.2" );
		testWaypoint.setLon( "invalid" );
		test2la = testMain.setBlueOrRed2la( testWaypoint );
		assertTrue( test2la, test2la.equals( "ZZ" ));
	}

}
