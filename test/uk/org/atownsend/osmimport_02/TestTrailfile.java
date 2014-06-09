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
public class TestTrailfile 
{

	/**
	 * testPut1()
	 * 
	 * Basic test - do we get out what we put in?
	 */
	@Test
	public void testPut1() 
	{
		Trailfile trailfile = new Trailfile( "AB", "20140214", "012810", "qqq07" );

		assertTrue( trailfile.getTwoLetterAbbreviation(), trailfile.getTwoLetterAbbreviation().equals( "AB" ));
		assertTrue( trailfile.getYyyymmdd(), trailfile.getYyyymmdd().equals( "20140214" ));
		assertTrue( trailfile.getHhmmss(), trailfile.getHhmmss().equals( "012810" ));
	}

	/**
	 * testPut2()
	 * 
	 * If we don't pass a seconds value, is zero seconds assumed?
	 */
	@Test
	public void testPut2() 
	{
		Trailfile trailfile = new Trailfile( "AB", "20140214", "0128", "qqq07" );

		assertTrue( trailfile.getHhmmss(), trailfile.getHhmmss().equals( "012800" ));
	}

	/**
	 * testPut3()
	 * 
	 * If we don't pass a minutes value, is zero minutes and seconds assumed?
	 */
	@Test
	public void testPut3() 
	{
		Trailfile trailfile = new Trailfile( "AB", "20140214", "01", "qqq07" );

		assertTrue( trailfile.getHhmmss(), trailfile.getHhmmss().equals( "010000" ));
	}

	/**
	 * testPut4()
	 * 
	 * If we don't pass an hours value, is zero hours, minutes and seconds assumed?
	 */
	@Test
	public void testPut4() 
	{
		Trailfile trailfile = new Trailfile( "AB", "20140214", "", "qqq07" );

		assertTrue( trailfile.getHhmmss(), trailfile.getHhmmss().equals( "000000" ));
	}

	/**
	 * testPut5uc()
	 * 
	 * If we pass a single character in place of hhmmss, does it go in the "seconds" slot?
	 */
	@Test
	public void testPut5uc() 
	{
		Trailfile trailfile = new Trailfile( "AB", "20140214", "Z", "qqq07" );

		assertTrue( trailfile.getHhmmss(), trailfile.getHhmmss().equals( "00000Z" ));
	}

	/**
	 * testPut5lc()
	 * 
	 * Is a passed single character case insensitive?
	 */
	@Test
	public void testPut5lc() 
	{
		Trailfile trailfile = new Trailfile( "AB", "20140214", "n", "qqq07" );

		assertTrue( trailfile.getHhmmss(), trailfile.getHhmmss().equals( "00000N" ));
	}

}
