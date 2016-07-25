/**
 * 
 */
package uk.org.atownsend.osmimport_02;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author A.Townsend
 *
 */
public class TestParams 
{
	/* ------------------------------------------------------------------------------
	 * Test file names. "writeTestFile" is assumed not to exist (it's deleted in the
	 * "@before" method), and "readTestFile" is assumed to have valid data in it
	 * (it's created there).
	 * ------------------------------------------------------------------------------ */
	final static String readTestCntrExistsFile = "c:\\temp\\osm\\cntr_exists.txt";
	final static String readTestCntrNotexistsFile = "c:\\temp\\osm\\cntr_notexists.txt";
	final static String writeTestCntrFile = "c:\\temp\\osm\\cntr_test.txt";
	final static String garbageTestCntrFile = "c:\\temp\\osm\\cntr_garbage.txt";

	final static String readTestInputExistsFile = "c:\\temp\\osm\\input.gpx";
	final static String readTestInputNotexistsFile = "c:\\temp\\osm\\doesntexist.gpx";

	final static String initialSosCntr = "987654";
	final static String initialTrkCntr = "8765";

	final static String readTestDescriptionExistsFile = "c:\\temp\\osm\\description.txt";
	final static String readTestDescriptionNotexistsFile = "c:\\temp\\osm\\doesntexist.txt";
	final static String readTestDescriptionBase64File = "c:\\temp\\osm\\base64.txt";

	
	/* ------------------------------------------------------------------------------
	 * Called before any other methods - set up the test data.
	 * ------------------------------------------------------------------------------ */
	@Before
	public void testParamsBefore() 
	{
		/* ------------------------------------------------------------------------------
		 * Delete "writeTestFile"
		 * ------------------------------------------------------------------------------ */
		File f = new File( writeTestCntrFile );
		f.delete();
		
		/* ------------------------------------------------------------------------------
		 * Create "readTestFile"
		 * ------------------------------------------------------------------------------ */
		try
		{
			/* ------------------------------------------------------------------------------
			 * Open the file (not appending to any existing one) and write the two lines 
			 * to it. 
			 * ------------------------------------------------------------------------------ */
			FileWriter cntrFileStream = new FileWriter( readTestCntrExistsFile, false );
			BufferedWriter cntrFileWriter = new BufferedWriter( cntrFileStream );
			cntrFileWriter.write( initialSosCntr + " ; sos_cntr (old was 000001)" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( initialTrkCntr + " ; trk_cntr (old was 0001)" );
			cntrFileWriter.newLine();
			cntrFileWriter.close();
		}
		catch( Exception ex )
		{
			fail( "Exception in testParamsBefore() at 'Create readTestFile'" );
		}

		/* ------------------------------------------------------------------------------
		 * Create "garbageTestFile"
		 * ------------------------------------------------------------------------------ */
		try
		{
			/* ------------------------------------------------------------------------------
			 * Open the file (not appending to any existing one) and write the two lines 
			 * to it. 
			 * ------------------------------------------------------------------------------ */
			FileWriter cntrFileStream = new FileWriter( garbageTestCntrFile, false );
			BufferedWriter cntrFileWriter = new BufferedWriter( cntrFileStream );
			cntrFileWriter.write( "garbage ; sos_cntr (old was 000001)" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "more garbage ; trk_cntr (old was 0001)" );
			cntrFileWriter.newLine();
			cntrFileWriter.close();
		}
		catch( Exception ex )
		{
			fail( "Exception in testParamsBefore() at 'Create garbageTestFile'" );
		}

		/* ------------------------------------------------------------------------------
		 * Create "readTestDescriptionExistsFile"
		 * ------------------------------------------------------------------------------ */
		try
		{
			/* ------------------------------------------------------------------------------
			 * Open the file (not appending to any existing one) and write some lines 
			 * to it, including a duplicate for 003.
			 * ------------------------------------------------------------------------------ */
			FileWriter cntrFileStream = new FileWriter( readTestDescriptionExistsFile, false );
			BufferedWriter cntrFileWriter = new BufferedWriter( cntrFileStream );
			cntrFileWriter.write( "001 wibble" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "aaa wobble" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "003 webble" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "003 wubble" );
			cntrFileWriter.newLine();
			cntrFileWriter.close();
		}
		catch( Exception ex )
		{
			fail( "Exception in testParamsBefore() at 'Create readTestFile'" );
		}

		/* ------------------------------------------------------------------------------
		 * Create "readTestDescriptionBase64File"
		 * ------------------------------------------------------------------------------ */
		try
		{
			/* ------------------------------------------------------------------------------
			 * Open the file (not appending to any existing one) and write some lines 
			 * to it, including "Content-Transfer-Encoding: base64"
			 * ------------------------------------------------------------------------------ */
			FileWriter cntrFileStream = new FileWriter( readTestDescriptionBase64File, false );
			BufferedWriter cntrFileWriter = new BufferedWriter( cntrFileStream );
			cntrFileWriter.write( "001 wibble" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "Content-Transfer-Encoding: base64" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "003 webble" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( "003 wubble" );
			cntrFileWriter.newLine();
			cntrFileWriter.close();
		}
		catch( Exception ex )
		{
			fail( "Exception in testParamsBefore() at 'Create readTestFile'" );
		}

	}
	
	
	/* ------------------------------------------------------------------------------
	 * The tests themselves
	 * 
	 * getters and setters
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testsetNewSosCntrOK() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		boolean set_result = testMain.setNewSosCntr( 0 );
		assertTrue( set_result == true );
		assertTrue( testMain.getNewSosCntrInt() == 0 );
		
		set_result = testMain.setNewSosCntr( 999999 );
		assertTrue( set_result == true );
		assertTrue( testMain.getNewSosCntrInt() == 999999 );
	}

	@Test
	public void testsetNewSosCntrFailNegative() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		boolean set_result = testMain.setNewSosCntr( -1 );
		assertTrue( set_result == false );
	}

	
	@Test
	public void testsetNewSosCntrFailLargePositive() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		boolean set_result = testMain.setNewSosCntr( 1000000 );
		assertTrue( set_result == false );
	}

	
	@Test
	public void testsetNewTrkCntrOK() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		boolean set_result = testMain.setNewTrkCntr( 0 );
		assertTrue( set_result == true );
		assertTrue( testMain.getNewSosCntrInt() == 0 );
		
		set_result = testMain.setNewTrkCntr( 9999 );
		assertTrue( set_result == true );
		assertTrue( testMain.getNewTrkCntrInt() == 9999 );
	}

	@Test
	public void testsetNewTrkCntrFailNegative() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		boolean set_result = testMain.setNewTrkCntr( -1 );
		assertTrue( set_result == false );
	}

	
	@Test
	public void testsetNewTrkCntrFailLargePositive() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		boolean set_result = testMain.setNewTrkCntr( 10000 );
		assertTrue( set_result == false );
	}

	
	@Test
	public void testparseCntrLine() 
	{
		Main testMain = new Main();
		int set_result = testMain.parseCntrLine( "0 some other text" );
		assertTrue( set_result == 0 );

		/* ------------------------------------------------------------------------------
		 * This test is with a value bigger than either Max_sos_cntr or Max_trk_cntr to
		 * check that it can deal with either.
		 * ------------------------------------------------------------------------------ */
		set_result = testMain.parseCntrLine( "1234567890 some other text" );
		assertTrue( set_result == 1234567890 );

		set_result = testMain.parseCntrLine( "garbage some other text" );
		assertTrue( set_result == -1 );
	}


	/* ------------------------------------------------------------------------------
	 * No parameters passed
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamNoParams() 
	{
		Main testMain = new Main();
		String[] argString = new String[0];
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The gpxRootElement should be null as we have not read a file in.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getMainGpxRootElement() == null );
	}


	/* ------------------------------------------------------------------------------
	 * Debug parameter
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamDebug1() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-b=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The debug value should be what we set it to.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getDebug() == 1 );
		
		/* ------------------------------------------------------------------------------
		 * The gpxRootElement should also be null as we have not read an input file in.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getMainGpxRootElement() == null );
	}

	
	/* ------------------------------------------------------------------------------
	 * I had expected a non-found file would fail here, but it doesn't, hence the
	 * lack of a test here.  However, trying to read values from a non-found file 
	 * does fail and is tested below.
	 * ------------------------------------------------------------------------------ */
//	@Test
//	public void testParamCntrNofile() 
//	{
//		Main testMain = new Main();
//		String[] argString = new String[1];
//		argString[0] = "-c=wibble";
//		testMain.start( argString );
//		assertTrue( testMain.getCntrFile(), testMain.getCntrFile().equals( "!file" ));
//	}
	
	
	/* ------------------------------------------------------------------------------
	 * Counter file parameter
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamCntrFileOpen() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrExistsFile;
		testMain.getParams( argString );
		assertTrue( testMain.getCntrFile(), testMain.getCntrFile().equals( readTestCntrExistsFile ));
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Counter file passed, but contains rubbish
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamCntrFileReadGarbage() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + garbageTestCntrFile;
		testMain.getParams( argString );

		/* ------------------------------------------------------------------------------
		 * With garbage in the file, we should get these three values set: 
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getCntrFile().equals( "!file" ));
		assertTrue( testMain.getLastSosCntr() == 0 );
		assertTrue( testMain.getLastTrkCntr() == 0 );
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Counter file passed, but does not exist
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamCntrFileReadNotExist() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + readTestCntrNotexistsFile;
		
		/* ------------------------------------------------------------------------------
		 * Set the "last" values to something other than zero. 
		 * ------------------------------------------------------------------------------ */
		testMain.setLastSosCntr( 99 );
		testMain.setLastTrkCntr( 99 );
		
		/* ------------------------------------------------------------------------------
		 * "start" will try to open the file, fail, and should reset the values to zero.   
		 * ------------------------------------------------------------------------------ */
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * If we can't open or read the file, we should know that, and "last" values 
		 * should be zero.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getCntrFile(), testMain.getCntrFile().equals( "!file" ));
		assertTrue( testMain.getLastSosCntr() == 0 );
		assertTrue( testMain.getLastTrkCntr() == 0 );
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Counter file passed - check we can write to it
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamCntrFileWrite() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-c=" + writeTestCntrFile;
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * We're expecting the file not to exist already, so this test is here to make
		 * sure that it doesn't.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getCntrFile().equals( "!file" ));
		testMain.setCntrFile( writeTestCntrFile );
		
		/* ------------------------------------------------------------------------------
		 * Set the counters and write to the new file.
		 * ------------------------------------------------------------------------------ */
		testMain.setNewSosCntr( 123456 );
		testMain.setNewTrkCntr( 1234 );
		boolean set_result = testMain.writeCntrFile();
		/* ------------------------------------------------------------------------------
		 * We're expecting the write to succeed and return true.
		 * ------------------------------------------------------------------------------ */
		assertTrue( set_result == true );
		
		/* ------------------------------------------------------------------------------
		 * Internally set the "last" values to something other than what we wrote above.
		 * ------------------------------------------------------------------------------ */
		testMain.setLastSosCntr( 0 );
		testMain.setLastTrkCntr( 0 );
		/* ------------------------------------------------------------------------------
		 * Read in the new values, and check that they are what we wrote.
		 * ------------------------------------------------------------------------------ */
		testMain.readAndCloseCntrFile();
		assertTrue( testMain.getLastSosCntr() == 123456 );
		assertTrue( testMain.getLastTrkCntr() == 1234 );
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Input file passed, but does not exist
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamInputFileReadNotExist() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-i=" + readTestInputNotexistsFile;
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * If we can't open or read the file, we should know that
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getInputFileMain(), testMain.getInputFileMain().equals( "!file" ));
	}

	
	/* ------------------------------------------------------------------------------
	 * Input file passed, and does exist
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamInputFileReadExist() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-i=" + readTestInputExistsFile;
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * We shouldn't get a "can't open file" error here.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getLastError() + " " + testMain.getInputFileMain(), !testMain.getInputFileMain().equals( "!file" ));
		
		/* ------------------------------------------------------------------------------
		 * The gpxRootElement should not be null after we've read a file in
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getMainGpxRootElement() != null );
	}


	/* ------------------------------------------------------------------------------
	 * Description file passed, but does not exist
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamDescriptionFileReadNotExist() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-d=" + readTestDescriptionNotexistsFile;
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The arguments should NOT be valid if a file that does not exist was passed
		 * as a parameter. 
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgsInvalid() == true );
		
		/* ------------------------------------------------------------------------------
		 * If we can't open or read the file, we should know that
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getDescriptionFile(), testMain.getDescriptionFile().equals( "!file" ));
	}

	
	/* ------------------------------------------------------------------------------
	 * Description file passed, and does exist.
	 * 
	 * Also check that the test data that we wrote to it in the "@before" method
	 * is present. 
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamDescriptionFileReadExist() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-d=" + readTestDescriptionExistsFile;
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The arguments should be valid 
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgsInvalid() == false );
		
		/* ------------------------------------------------------------------------------
		 * We shouldn't get a "can't open file" error here.
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getLastError() + " " + testMain.getDescriptionFile(), !testMain.getDescriptionFile().equals( "!file" ));
		
		/* ------------------------------------------------------------------------------
		 * The descriptionHashTable should not be null after we've read a file in
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getDescriptionHashTable() != null );
		
		/* ------------------------------------------------------------------------------
		 * It should contain an entry for "001" and that entry should be "wibble".
		 * It should contain an entry for "aaa" (regardless of case) and that entry should be "wobble"
		 * It should contain an entry for "003" and that entry should be "webble;wubble".
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getDescriptionHashTable().get( "001" ), testMain.getDescriptionHashTable().get( "001" ).equals( "wibble" ) );
		assertTrue( testMain.getDescriptionHashTable().get( "aaa" ), testMain.getDescriptionHashTable().get( "aaa" ).equals( "wobble" ) );
		assertTrue( testMain.getDescriptionHashTable().get( "AAA" ), testMain.getDescriptionHashTable().get( "AAA" ).equals( "wobble" ) );
		assertTrue( testMain.getDescriptionHashTable().get( "003" ), testMain.getDescriptionHashTable().get( "003" ).equals( "webble;wubble" ) );
	}


	/* ------------------------------------------------------------------------------
	 * Description file passed, exists, but contains base64 data.
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamDescriptionFileReadBase64() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-d=" + readTestDescriptionBase64File;
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The arguments should be invalid as we've found base64 data in 
		 * the description file 
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgsInvalid() == true );
		
		/* ------------------------------------------------------------------------------
		 * And we shouldn't have a valid file name here:
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getDescriptionFile(), testMain.getDescriptionFile().equals( "!file" ));
	}


	/* ------------------------------------------------------------------------------
	 * Path parameter not passed
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamPathNotpassed() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-b=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The arguments should not be invalid in any way 
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgsInvalid() == false );
		
		/* ------------------------------------------------------------------------------
		 * And we shouldn't have a valid path here:
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getPath(), testMain.getPath().equals( "" ));
	}


	/* ------------------------------------------------------------------------------
	 * Path parameter passed and valid
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamPathPassed() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-k=c:\\temp\\";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * The arguments should not be invalid in any way 
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgsInvalid() == false );
		
		/* ------------------------------------------------------------------------------
		 * And we shouldn't have a valid path here:
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getPath(), testMain.getPath().equals( "c:\\temp\\" ));
	}


	/* ------------------------------------------------------------------------------
	 * Various methods for setting trail types for -t, -y, -u, -p, -a, -s, -f, -g.
	 * ------------------------------------------------------------------------------ */
	@Test
	public void testParamInputArgTBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-t=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgT().equals( "" ));
	}

	
	@Test
	public void testParamInputArgT1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-t=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgT().equals( "" ));
	}

	
	@Test
	public void testParamInputArgTOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-t=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgT().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgTNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-t=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgT().equals( "" ));
	}

	@Test
	public void testParamInputArgYBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-y=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgY().equals( "" ));
	}

	
	@Test
	public void testParamInputArgY1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-y=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgY().equals( "" ));
	}

	
	@Test
	public void testParamInputArgYOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-y=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgY().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgYNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-y=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgY().equals( "" ));
	}

	@Test
	public void testParamInputArgUBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-u=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgU().equals( "" ));
	}

	
	@Test
	public void testParamInputArgU1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-u=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgU().equals( "" ));
	}

	
	@Test
	public void testParamInputArgUOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-u=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgU().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgUNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-u=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgU().equals( "" ));
	}
	@Test
	public void testParamInputArgPBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-p=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgP().equals( "" ));
	}

	
	@Test
	public void testParamInputArgP1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-p=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgP().equals( "" ));
	}

	
	@Test
	public void testParamInputArgPOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-p=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgP().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgPNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-p=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgP().equals( "" ));
	}
	@Test
	public void testParamInputArgABlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-a=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgA().equals( "" ));
	}

	
	@Test
	public void testParamInputArgA1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-a=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgA().equals( "" ));
	}

	
	@Test
	public void testParamInputArgAOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-a=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgA().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgANotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-a=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgA().equals( "" ));
	}
	@Test
	public void testParamInputArgSBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-s=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgS().equals( "" ));
	}

	
	@Test
	public void testParamInputArgS1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-s=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgS().equals( "" ));
	}

	
	@Test
	public void testParamInputArgSOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-s=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgS().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgSNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-s=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgS().equals( "" ));
	}
	@Test
	
	public void testParamInputArgFBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-f=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgF().equals( "" ));
	}

	
	@Test
	public void testParamInputArgF1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-f=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgF().equals( "" ));
	}

	
	@Test
	public void testParamInputArgFOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-f=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgF().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgFNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-f=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgF().equals( "" ));
	}

	public void testParamInputArgGBlank() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-g=";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgG().equals( "" ));
	}

	
	@Test
	public void testParamInputArgG1Char() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-g=1";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should still be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgG().equals( "" ));
	}

	
	@Test
	public void testParamInputArgGOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-g=OK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * Valid parameter passed, should not be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgG().equals( "OK" ));
	}

	
	@Test
	public void testParamInputArgGNotOk() 
	{
		Main testMain = new Main();
		String[] argString = new String[1];
		argString[0] = "-g=NOK";
		testMain.getParams( argString );
		
		/* ------------------------------------------------------------------------------
		 * No valid parameter passed, should be blank
		 * ------------------------------------------------------------------------------ */
		assertTrue( testMain.getArgG().equals( "" ));
	}

}
