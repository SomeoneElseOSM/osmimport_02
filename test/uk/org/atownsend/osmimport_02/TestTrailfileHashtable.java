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
public class TestTrailfileHashtable 
{

	/**
	 * testGetFromEmpty()
	 * 
	 * Before we've stored anything, when we try to retrieve something, is the result null?
	 */
	@Test
	public void testGetFromEmpty() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		
		assertTrue( trailfileHashtable.get( "  " ) == null );
	}

	/**
	 * testOneYYYYMMDDHHMMSS() 
	 * 
	 * Basic "do we get back when we put in" test.
	 */
	@Test
	public void testOneYYYYMMDDHHMMSS() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		Trailfile trailfile1 = new Trailfile( "AB", "20140101", "234959", "qqq07" );
		trailfileHashtable.put( trailfile1 );
		Trailfile trailfile2 = trailfileHashtable.get( "AB" );
				
		assertTrue( trailfile2.getYyyymmdd(), trailfile2.getYyyymmdd().equals( "20140101" ));
		assertTrue( trailfile2.getHhmmss(), trailfile2.getHhmmss().equals( "234959" ));
	}

	/**
	 * testTwoYYYYMMDDHHMMSSa() 
	 * 
	 * If we put two different hhmmss values, do we get the later one back?
	 */
	@Test
	public void testTwoYYYYMMDDHHMMSSa() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		Trailfile trailfile1 = new Trailfile( "AB", "20140101", "234958", "qqq07" );
		Trailfile trailfile2 = new Trailfile( "AB", "20140101", "234959", "qqq07" );
		trailfileHashtable.put( trailfile1 );
		trailfileHashtable.put( trailfile2 );
		Trailfile trailfile3 = trailfileHashtable.get( "AB" );
				
		assertTrue( trailfile3.getYyyymmdd(), trailfile3.getYyyymmdd().equals( "20140101" ));
		assertTrue( trailfile3.getHhmmss(), trailfile3.getHhmmss().equals( "234959" ));
	}

	/**
	 * testTwoYYYYMMDDHHMMSSb() 
	 * 
	 * And is that true regardless of the order we added them?
	 */
	@Test
	public void testTwoYYYYMMDDHHMMSSb() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		Trailfile trailfile2 = new Trailfile( "AB", "20140101", "234959", "qqq07" );
		Trailfile trailfile1 = new Trailfile( "AB", "20140101", "234958", "qqq07" );
		trailfileHashtable.put( trailfile1 );
		trailfileHashtable.put( trailfile2 );
		Trailfile trailfile3 = trailfileHashtable.get( "AB" );
				
		assertTrue( trailfile3.getYyyymmdd(), trailfile3.getYyyymmdd().equals( "20140101" ));
		assertTrue( trailfile3.getHhmmss(), trailfile3.getHhmmss().equals( "234959" ));
	}

	/**
	 * testTwoYYYYMMDDa() 
	 * 
	 * Same again with dates
	 */
	@Test
	public void testTwoYYYYMMDDa() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		Trailfile trailfile2 = new Trailfile( "AB", "20140101", "", "qqq07" );
		Trailfile trailfile1 = new Trailfile( "AB", "20140102", "", "qqq07" );
		trailfileHashtable.put( trailfile1 );
		trailfileHashtable.put( trailfile2 );
		Trailfile trailfile3 = trailfileHashtable.get( "AB" );
				
		assertTrue( trailfile3.getYyyymmdd(), trailfile3.getYyyymmdd().equals( "20140102" ));
		assertTrue( trailfile3.getHhmmss(), trailfile3.getHhmmss().equals( "000000" ));
	}
	
	
	/**
	 * testTwoYYYYMMDDb() 
	 * 
	 * ... and the other way around.
	 */
	@Test
	public void testTwoYYYYMMDDb() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		Trailfile trailfile1 = new Trailfile( "AB", "20140102", "", "qqq07" );
		Trailfile trailfile2 = new Trailfile( "AB", "20140101", "", "qqq07" );
		trailfileHashtable.put( trailfile1 );
		trailfileHashtable.put( trailfile2 );
		Trailfile trailfile3 = trailfileHashtable.get( "AB" );
				
		assertTrue( trailfile3.getYyyymmdd(), trailfile3.getYyyymmdd().equals( "20140102" ));
		assertTrue( trailfile3.getHhmmss(), trailfile3.getHhmmss().equals( "000000" ));
	}

	/**
	 * testThree() 
	 * 
	 * And a test that an hhmmss value is "later" than an "a or b" value (after 10 seconds past midnight).
	 */
	@Test
	public void testThree() 
	{
		TrailfileHashtable trailfileHashtable = new TrailfileHashtable();
		Trailfile trailfile1 = new Trailfile( "AB", "20140103", "c", "qqq07" );
		Trailfile trailfile2 = new Trailfile( "AB", "20140103", "000010", "qqq07" );
		trailfileHashtable.put( trailfile1 );
		trailfileHashtable.put( trailfile2 );
		Trailfile trailfile3 = trailfileHashtable.get( "AB" );
				
		assertTrue( trailfile3.getYyyymmdd(), trailfile3.getYyyymmdd().equals( "20140103" ));
		assertTrue( trailfile3.getHhmmss(), trailfile3.getHhmmss().equals( "000010" ));
	}

}
