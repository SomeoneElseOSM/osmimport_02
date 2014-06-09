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
public class TestTrailHashset 
{

	@Test
	public void testGetFromEmpty() 
	{
		TrailHashset trail2laHashSet = new TrailHashset();
		
		assertFalse( trail2laHashSet.contains( "" ));
		assertFalse( trail2laHashSet.contains( "O" ));
		assertFalse( trail2laHashSet.contains( "OA" ));
		assertFalse( trail2laHashSet.contains( "OB" ));
		assertFalse( trail2laHashSet.contains( "wibble" ));
	}


	@Test
	public void testGetFromOne() 
	{
		TrailHashset trail2laHashSet = new TrailHashset();
		trail2laHashSet.add( "OA" );
		
		assertFalse( trail2laHashSet.contains( "" ));
		assertFalse( trail2laHashSet.contains( "O" ));
		assertTrue( trail2laHashSet.contains( "OA" ));
		assertFalse( trail2laHashSet.contains( "OB" ));
		assertFalse( trail2laHashSet.contains( "wibble" ));
	}

	@Test
	public void testGetFromTwo() 
	{
		TrailHashset trail2laHashSet = new TrailHashset();
		trail2laHashSet.add( "OA" );
		trail2laHashSet.add( "OC" );
		
		assertFalse( trail2laHashSet.contains( "" ));
		assertFalse( trail2laHashSet.contains( "O" ));
		assertTrue( trail2laHashSet.contains( "OA" ));
		assertFalse( trail2laHashSet.contains( "OB" ));
		assertTrue( trail2laHashSet.contains( "OC" ));
		assertFalse( trail2laHashSet.contains( "wibble" ));
	}

	@Test
	public void testInteration() 
	{
		TrailHashset trail2laHashSet = new TrailHashset();
		trail2laHashSet.add( "OA" );
		trail2laHashSet.add( "OC" );
		
		String trail2laString = "";
		for( String item : trail2laHashSet )
		{
			if ( trail2laString.equals(""))
			{
				trail2laString = item;
			}
			else
			{
				trail2laString = item + ", " + trail2laString;
			}
		}

		/* ------------------------------------------------------------------------------
		 * Note that we're assuming here that the Hashset works like a LIFO queue
		 * Strictly speaking, we're not really bothered about data order, so if this test
		 * fails it isn't necessarily an error.
		 * (TODO add a sort to the data coming out to resolve this)
		 * ------------------------------------------------------------------------------ */
		assertTrue( trail2laString, trail2laString.equals( "OA, OC" ));
	}

}
