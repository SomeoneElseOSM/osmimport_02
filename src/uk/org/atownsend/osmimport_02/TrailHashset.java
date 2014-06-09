/**
 * 
 */
package uk.org.atownsend.osmimport_02;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author A.Townsend
 * 
 * TrailHashSet is used to store two-letter and three-letter abbreviations of which waypoints we've seen so far
 * in a file that we're processing (as opposed to TrailfileHashtable, which does something similar for
 * files on disk), but is a Hashtable rather than a HashSet.
 *  
 * We do this (in the case of the 3-letter abbreviation) so that we can print out details of the 
 * abbreviations found for documentation to the user.  In the case of the 2-letter abbreviations it's done so that
 * if we've found a DXY123456 in our input GPX, we can look in a file on disk for SXY123456 and rename it there to
 * DXY123456.   
 *
 * It must implement the iterable interface because one of the things that we need to do is to
 * print a list to stdout of all the  two-letter abbreviations found.
 * 
 * The data stored in each entry of the 3-letter version is D or S followed by the 2la. 
 */
public class TrailHashset implements Iterable<String> 
{
	private HashSet<String> trailSet;
	
	public TrailHashset() 
	{
		trailSet = new HashSet<String>();
	}

	public void add( String trailAbb )
	{
		trailSet.add( trailAbb );
	}
	
	public boolean contains( String trailAbb )
	{
		return trailSet.contains( trailAbb );
	}

	@Override
	public Iterator<String> iterator() 
	{
		return trailSet.iterator();
	}
}
