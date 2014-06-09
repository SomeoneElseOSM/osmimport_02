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
public class TestDescriptionHashtable 
{

	/**
	 * testGetFromEmpty()
	 * 
	 * Before we've stored anything, when we try to retrieve something, is the result null?
	 */
	@Test
	public void testGetFromEmpty() 
	{
		DescriptionHashtable descriptionHashtable = new DescriptionHashtable();
		
		assertTrue( descriptionHashtable.get( "  " ) == null );
	}

	/**
	 * testOne() 
	 * 
	 * Basic "do we get back when we put in" test.
	 */
	@Test
	public void testOne() 
	{
		DescriptionHashtable descriptionHashtable = new DescriptionHashtable();
		descriptionHashtable.put( "key1", "data1" );
		
		assertTrue( descriptionHashtable.get( "key1" ), descriptionHashtable.get( "key1" ).equals( "data1" ));
		assertTrue( descriptionHashtable.get( "key2" ) == null );
	}

	/**
	 * testTwo() 
	 * 
	 * Test key case insensitivity
	 */
	@Test
	public void testTwo() 
	{
		DescriptionHashtable descriptionHashtable = new DescriptionHashtable();
		descriptionHashtable.put( "kEy1", "data1" );
		
		assertTrue( descriptionHashtable.get( "keY1" ), descriptionHashtable.get( "keY1" ).equals( "data1" ));
	}

	/**
	 * testThree() 
	 * 
	 * Test description key addition.
	 */
	@Test
	public void testThree() 
	{
		DescriptionHashtable descriptionHashtable = new DescriptionHashtable();
		descriptionHashtable.put( "key1", "data1" );
		descriptionHashtable.put( "key2", "data2" );
		descriptionHashtable.put( "key1", "data3" );
		
		assertTrue( descriptionHashtable.get( "key1" ), descriptionHashtable.get( "key1" ).equals( "data1;data3" ));
		assertTrue( descriptionHashtable.get( "key2" ), descriptionHashtable.get( "key2" ).equals( "data2" ));
	}

	/**
	 * testFour() 
	 * 
	 * Test case insensitive description key addition.
	 */
	@Test
	public void testFour() 
	{
		DescriptionHashtable descriptionHashtable = new DescriptionHashtable();
		descriptionHashtable.put( "kEy1", "data1" );
		descriptionHashtable.put( "keY1", "data3" );
		
		assertTrue( descriptionHashtable.get( "Key1" ), descriptionHashtable.get( "Key1" ).equals( "data1;data3" ));
	}

}
