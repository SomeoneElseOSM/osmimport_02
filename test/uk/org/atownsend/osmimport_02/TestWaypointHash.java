package uk.org.atownsend.osmimport_02;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestWaypointHash 
{

	/**
	 * testPut1()
	 * 
	 * A basic "do we get the values back that we put in" test.
	 */
	@Test
	public void testPut1() 
	{
		Waypoint waypoint1 = new Waypoint( "S", "AB", "123456", "51.0", "-1.23", "Comment", "Desc", "Sym", "0.00", "" );
		Waypoint waypoint2 = new Waypoint();
		
		WaypointHashtable waypointMap = new WaypointHashtable();
		waypointMap.put( waypoint1 );
		waypoint2 = waypointMap.get( "123456" );
		
		assertTrue( waypoint2.getInitial().equals( "S" ));
		assertTrue( waypoint2.getTwoLetterAbbreviation().equals( "AB" ));
		assertTrue( waypoint2.getWaypointNumber().equals( "123456" ));
		assertTrue( waypoint2.getLat().equals( "51.0" ));
		assertTrue( waypoint2.getLon().equals( "-1.23" ));
		assertTrue( waypoint2.getCmt().equals( "Comment" ));
		assertTrue( waypoint2.getDesc().equals( "Desc" ));
		assertTrue( waypoint2.getSym().equals( "Sym" ));
		assertTrue( waypoint2.getEle().equals( "0.00" ));
		assertTrue( waypoint2.getFileNameFoundIn().equals( "" ));
	}

	/**
	 * testPut2()
	 * 
	 * Check that values are returned in uppercase, and keys are checked case insensitively.
	 */
	@Test
	public void testPut2() 
	{
		Waypoint waypoint1 = new Waypoint( "s", "ab", "ASDfgh", "51.0", "-1.23", "Comment", "Desc", "Sym", "0.00", "" );
		Waypoint waypoint2 = new Waypoint();
		
		WaypointHashtable waypointMap = new WaypointHashtable();
		waypointMap.put( waypoint1 );
		waypoint2 = waypointMap.get( "asdFGH" );
		
		assertTrue( waypoint2.getInitial().equals( "S" ));
		assertTrue( waypoint2.getTwoLetterAbbreviation().equals( "AB" ));
		assertTrue( waypoint2.getWaypointNumber().equals( "ASDFGH" ));
		assertTrue( waypoint2.getLat().equals( "51.0" ));
		assertTrue( waypoint2.getLon().equals( "-1.23" ));
		assertTrue( waypoint2.getCmt().equals( "Comment" ));
		assertTrue( waypoint2.getDesc().equals( "Desc" ));
		assertTrue( waypoint2.getSym().equals( "Sym" ));
		assertTrue( waypoint2.getEle().equals( "0.00" ));
		//qqq07 probably should test something valid here
		assertTrue( waypoint2.getFileNameFoundIn().equals( "" ));
	}

	/**
	 * testPut3()
	 * 
	 * Test that when we update a WaypointHashtable with new values we get the new values back.
	 */
	@Test
	public void testPut3() 
	{
		Waypoint waypoint1 = new Waypoint( "S", "AB", "123456", "51.1", "-1.21", "OldComment", "OldDesc", "OldSym", "0.01", "OldF" );
		Waypoint waypoint2 = new Waypoint( "D", "AC", "123456", "51.2", "-1.22", "NewComment", "NewDesc", "NewSym", "0.02", "NewF" );
		
		WaypointHashtable waypointMap = new WaypointHashtable();
		waypointMap.put( waypoint1 );
		waypointMap.put( waypoint2 );
		Waypoint waypoint3 = waypointMap.get( "123456" );
		
		assertTrue( waypoint3.getInitial().equals( "D" ));
		assertTrue( waypoint3.getTwoLetterAbbreviation().equals( "AC" ));
		assertTrue( waypoint3.getWaypointNumber().equals( "123456" ));
		assertTrue( waypoint3.getLat().equals( "51.2" ));
		assertTrue( waypoint3.getLon().equals( "-1.22" ));
		assertTrue( waypoint3.getCmt().equals( "NewComment" ));
		assertTrue( waypoint3.getDesc().equals( "NewDesc" ));
		assertTrue( waypoint3.getSym().equals( "NewSym" ));
		assertTrue( waypoint3.getEle().equals( "0.02" ));
		assertTrue( waypoint3.getFileNameFoundIn().equals( "NewF" ));
	}
	
}
