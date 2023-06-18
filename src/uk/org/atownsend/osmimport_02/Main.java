/**
 * 
 */
package uk.org.atownsend.osmimport_02;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author A.Townsend
 *
 */
public class Main 
{
	final static int Log_Debug_Off = 0;			// Used to turn debug off
	final static int Log_Deliberately = 1;		// Something that we want to inform the user of that is not an error.
	final static int Log_Serious = 1;			// A serious error has occurred, or we want to write something to stdout regardless.
	final static int Log_Error = 2;				// An error that we can work around has occurred
	final static int Log_Warning = 3;			// Not currently used
	final static int Log_Return = 4; 			// Return values from top-level subroutines
	final static int Log_Informational_1 = 5;	// Important informational stuff
	final static int Log_Top_Routine_Start = 6;	// top-level routine start code
	final static int Log_Low_Routine_Start = 7; // low-level routing start code
	final static int Log_Informational_2 = 8;	// Any other informational stuff

	final static int Max_sos_cntr = 999999;
	final static int Max_trk_cntr = 9999;

	final static String param_debug = "-b=";		// It's "-b" rather than "-d" because "-d" is used for "description file".
	final static String param_cntr = "-c=";			// "counter file"
	final static String param_input_main = "-i=";	// "main input GPX file" (possibly waypoints and definitely track)
	final static String param_input_sup1 = "-j=";	// "supplementary input GPX file" (optional, waypoints only)
	final static String param_description = "-d=";	// "description file" (typically a saved plain text email)
	final static String param_t = "-t=";			// The two-letter trail name that "Trail Head" is to be saved as
	final static String param_y = "-y=";			// (and other trail names for other symbols - "Bike Trail") ...
	final static String param_u = "-u=";			// "Skull and Crossbones"
	final static String param_p = "-p=";			// "Ski Resort"
	final static String param_a = "-a=";			// "Hunting Area"
	final static String param_s = "-s=";			// "RV Park"
	final static String param_f = "-f=";			// "Glider Area"
	final static String param_g = "-g=";			// ... up to "Ultralight Area".
	final static String param_path = "-k=";			// The path in which to search for existing GPX files to update

	private int arg_debug = 2;					// Default to logging errors only
	private String arg_cntr_file = "";			// Default to no counter file
	private int last_sos_cntr = 0;
	private int last_trk_cntr = 0;
	private int new_sos_cntr = 0;
	private int new_trk_cntr = 0;
	private String arg_input_file_main = "";	// Default to no main input file
	private String arg_input_file_sup1 = "";	// Default to no supplementary input file
	private String arg_description_file = "";	// Default to no description file
	private String arg_t = "";					// Default to no "trail" 2la
	private String arg_y = "";
	private String arg_u = "";
	private String arg_p = "";
	private String arg_a = "";
	private String arg_s = "";
	private String arg_f = "";
	private String arg_g = "";
	private String arg_path = "";
	
	/* ------------------------------------------------------------------------------
	 * If this gets set, we've found something while processing command line 
	 * arguments that would stop us doing any further processing.
	 * 
	 * An example of this would be a reference to a description file containing
	 * base64 encoded data - in that case we want to correct the error first, so that
	 * waypoints are processed with the data from the description file.
	 * ------------------------------------------------------------------------------ */
	private boolean args_invalid = false; 
	
	File myCntrFile;						// The File opened to read the current counter 
	File myInputFileMain;					// The File opened to read the main GPX file
	File myInputFileSup1;					// The File opened to read the supplementary GPX file
	File myDescriptionFile;					// The File opened to read the description info
	File myPathFile;						// The File opened to read a directory for existing GPX files
	
	TrailfileHashtable myTrailfileHashtable;	// Used to store any trailfiles that we find on disk.

	/* ------------------------------------------------------------------------------
	 * mainGpxRootElement is the root element of the main GPX file that we've read in.  
	 * It's populated as part of the "getParams" process.
	 * Other root elements are for supplementary GPX files
	 * ------------------------------------------------------------------------------ */
	Element mainGpxRootElement = null;
	Element sup1GpxRootElement = null;

	/* ------------------------------------------------------------------------------
	 * This contains extended descriptions read from a description file if 
	 * param_description is passed on the command line.
	 * ------------------------------------------------------------------------------ */
	DescriptionHashtable descriptionHashTable = new DescriptionHashtable();

	/* ------------------------------------------------------------------------------
	 * Current date and time
	 * 
	 * This is calculated once at the start of processing and used whenever we need
	 * to create a file with "the current date and time".
	 * ------------------------------------------------------------------------------ */
	DateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd" );
	DateFormat timeFormat = new SimpleDateFormat( "HHmmss" ); // 24-hour clock
	Date currentDateAndTime = new Date();
	
	/* ------------------------------------------------------------------------------
	 * This flag is used in two places - in processTrail2laHashSet to decide whether
	 * we have already updated the base data file based on the input file contents, and
	 * in outputCounterFile to tell whether a new base data file was created at all.
	 * ------------------------------------------------------------------------------ */
	boolean base_data_already_processed = false;
	
	/* ------------------------------------------------------------------------------
	 * When we're writing out updated versions of a GPX file with today's date, we'll
	 * use this stream: 
	 * ------------------------------------------------------------------------------ */
	FileOutputStream newGpxFileStream;
	
	/* ------------------------------------------------------------------------------
	 * Local methods
	 * ------------------------------------------------------------------------------ */
	/* ------------------------------------------------------------------------------
	 * These two are used for testing only.  "last_error" is set by "debugout()". 
	 * ------------------------------------------------------------------------------ */
	private String last_error = "";
	
	public String getLastError()
	{
		return last_error;
	}

	
	public int getDebug()
	{
		return arg_debug;
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Also used for testing only 
	 * ------------------------------------------------------------------------------ */
	public Element getMainGpxRootElement()
	{
		return mainGpxRootElement;
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Also used for testing only 
	 * ------------------------------------------------------------------------------ */
	public Element getSup1GpxRootElement()
	{
		return sup1GpxRootElement;
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Also used for testing only 
	 * ------------------------------------------------------------------------------ */
	public DescriptionHashtable getDescriptionHashTable()
	{
		return descriptionHashTable;
	}
		
		
	/* ------------------------------------------------------------------------------
	 * Also used for testing only 
	 * ------------------------------------------------------------------------------ */
	public boolean getArgsInvalid()
	{
		return args_invalid;
	}
		
		
	public String getCntrFile()
	{
		return arg_cntr_file;
	}

	
	/* ------------------------------------------------------------------------------
	 * Only used from unit tests (to set the file name to something to open to write
	 * to after the attempted open for read failed). 
	 * ------------------------------------------------------------------------------ */
	public void setCntrFile( String passed_cntr_file )
	{
		arg_cntr_file = passed_cntr_file;
	}

	
	public String getInputFileMain()
	{
		return arg_input_file_main;
	}

	
	public String getDescriptionFile()
	{
		return arg_description_file;
	}

	
	public String getPath()
	{
		return arg_path;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgT( String passed_arg_t )
	{
		arg_t = passed_arg_t;
	}

	
	public String getArgT()
	{
		return arg_t;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgY( String passed_arg_y )
	{
		arg_y = passed_arg_y;
	}

	
	public String getArgY()
	{
		return arg_y;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgU( String passed_arg_u )
	{
		arg_u = passed_arg_u;
	}

	
	public String getArgU()
	{
		return arg_u;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgP( String passed_arg_p )
	{
		arg_p = passed_arg_p;
	}

	
	public String getArgP()
	{
		return arg_p;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgA( String passed_arg_a )
	{
		arg_a = passed_arg_a;
	}

	
	public String getArgA()
	{
		return arg_a;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgS( String passed_arg_s )
	{
		arg_s = passed_arg_s;
	}

	
	public String getArgS()
	{
		return arg_s;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgF( String passed_arg_f )
	{
		arg_f = passed_arg_f;
	}

	
	public String getArgF()
	{
		return arg_f;
	}

	
	/* ------------------------------------------------------------------------------
	 * "set" is only used from unit tests 
	 * ------------------------------------------------------------------------------ */
	public void setArgG( String passed_arg_g )
	{
		arg_g = passed_arg_g;
	}

	
	public String getArgG()
	{
		return arg_g;
	}

	
/* ------------------------------------------------------------------------------
 * We have two sets of values for the SOS counter and the track counter, what 
 * they were previously, and what they've been most recently reset to, stored
 * like this:
 * 
 * 111035 ; sos_cntr (old was 110771)
 * 2860 ; trk_cntr (old was 2856)
  
 * The reason for doing this is so that if the file needs to be edited manually
 * in order to run an import again from the previous values, it can be. 
 * 
 * In normal operation "get" is only called on the "last" values and "set" on
 * the "new" values.  The "set" methods for the "last" values are only used
 * from unit tests, to ensure that values are actively set to zero after an
 * error elsewhere.
 * ------------------------------------------------------------------------------ */
	public int getLastSosCntr()
	{
		return last_sos_cntr;
	}
	
	
	public int getLastTrkCntr()
	{
		return last_trk_cntr;
	}
	

	public void setLastSosCntr( int passed_sos_cntr )
	{
		last_sos_cntr = passed_sos_cntr;
	}
	
	
	public void setLastTrkCntr( int passed_trk_cntr )
	{
		last_trk_cntr = passed_trk_cntr;
	}
	
	
/* ------------------------------------------------------------------------------
 * Two versions of this exist - one to return an integer, another to return it
 * passed with leading zeroes as a 6-character string.
 * ------------------------------------------------------------------------------ */
	public int getNewSosCntrInt()
	{
		return new_sos_cntr;
	}
	
	
	public String getNewSosCntrString()
	{
		return String.format( "%06d", new_sos_cntr );
	}
	
	
/* ------------------------------------------------------------------------------
 * Again, two versions of this exist - one to return an integer, another to return it
 * passed with leading zeroes as a 4-character string.
 * ------------------------------------------------------------------------------ */
	public int getNewTrkCntrInt()
	{
		return new_trk_cntr;
	}
	
	
	public String getNewTrkCntrString()
	{
		return String.format( "%04d", new_trk_cntr );
	}
	
	
	

/* ------------------------------------------------------------------------------
 * The "SOS" counter is used to number Garmin waypoints, which will end up
 * being named something like "DTR123456".
 * 
 * "D" means "not to be saved in the base data file containing waypoints
 * to be sent to the device next time".
 * 
 * "TR" is one of a series of 2 letter abbreviations for the type of data.
 * Most refer to specific trails ("PB" == "Pennine Bridleway", for example).
 * "TR" just means "location data not to be stored in the base data".
 * 
 *  The six-digit number is unique for each waypoint.  It was designed in 2009 
 *  to last about ten years at current mapping frequency, and is on target.
 * ------------------------------------------------------------------------------ */
	/**
     * setNewSosCntr() 
     * Check that the passed value is valid, and set the internal value to it.
	 * @param passed_sos_cntr  the value to set.  
     *  
     * @return true if it works, false if not
	 */
	public boolean setNewSosCntr( int passed_sos_cntr )
	{
		if (( passed_sos_cntr >= 0 ) && ( passed_sos_cntr <= Max_sos_cntr ))
		{
			new_sos_cntr = passed_sos_cntr;
			return true;
		}
		else
		{
    		debugout( Log_Serious, "setNewSosCntr() value out of range: " + passed_sos_cntr );
			return false;
		}
	}
	
	
	/**
 	 * incNewSosCntr() 
	 * Check that the current value is at least one less than the maximum, 
	 * and if so increment it.
	 *  
	 * @return true if it works, false if not
	 */
	public boolean incNewSosCntr( )
	{
		if ( new_sos_cntr < Max_sos_cntr )
		{
			new_sos_cntr++;
			return true;
		}
		else
		{
    		debugout( Log_Serious, "incNewSosCntr() value would be out of range: " + new_sos_cntr );
			return false;
		}
	}
	
	
/* ------------------------------------------------------------------------------
 * Similarly the "TRK" counter is used to number Garmin tracks, which will end up
 * being named something like "TR1234".
 * 
 * "TS" just means "Track".
 * 
 *  The six-digit number is unique for each track.  It's designed to
 *  last about ten years at current mapping frequency.
 * ------------------------------------------------------------------------------ */
	/**
	 * setNewTrkCntr() 
	 * Check that the passed value is valid, and set the internal value to it.
	 * @param passed_trk_cntr  the value to set.  
	 *  
	 * @return true if it works, false if not
	 */
	public boolean setNewTrkCntr( int passed_trk_cntr )
	{
		if (( passed_trk_cntr >= 0 ) && ( passed_trk_cntr <= Max_trk_cntr ))
		{
			new_trk_cntr = passed_trk_cntr;
			return true;
		}
		else
		{
    		debugout( Log_Serious, "setNewTrkCntr() value out of range: " + passed_trk_cntr );
			return false;
		}
	}
	
	
	/**
 	 * incNewTrkCntr() 
	 * Check that the current value is at least one less than the maximum, 
	 * and if so increment it.
	 *  
	 * @return true if it works, false if not
	 */
	public boolean incNewTrkCntr( )
	{
		if ( new_trk_cntr < Max_trk_cntr )
		{
			new_trk_cntr++;
			return true;
		}
		else
		{
    		debugout( Log_Serious, "incNewTrkCntr() value would be out of range: " + new_trk_cntr );
			return false;
		}
	}
	
	
	/**
	 * Obtain the value of the part of a line up to the first space.  
	 * We're not interested in anything after that - it's a human-readable comment only.
	 *   
	 * @param passed_line  The line to parse
	 * @return the integer value of the line up to the first space, or -1 in the event of an error.
	 */
	int parseCntrLine( String passed_line )
	{
		int line_cntr = -1;
		int space_pos = passed_line.indexOf( ' ' );
		
		if ( space_pos > 0 )
		{
			try
			{
				line_cntr = Integer.valueOf( passed_line.substring( 0, space_pos) );
			}
			catch( Exception ex )
			{
	    		debugout( Log_Error, "parseCntrLine(): " + ex.getMessage());
			}
		}
		
		return line_cntr;
	}
	
	
	/**
	 * readAndCloseCntrFile()
	 * 
	 * The "counter" file stores the counter values for "last assigned waypoint number
	 * and "last assigned track number".
	 * 
	 * This method is called when we encounter the parameter for the counter file on 
	 * the command line.
	 * 
	 * Once we've finished doing all the processing, the new values are written out 
	 * via writeCntrFile().
	 */
    public void readAndCloseCntrFile()
    {
    	try
    	{
            InputStream inputStream = new FileInputStream( myCntrFile );
            DataInputStream dataInputStream = new DataInputStream( inputStream );
            
            String node_line = dataInputStream.readLine();
            last_sos_cntr = parseCntrLine( node_line );
            
            if ( last_sos_cntr == -1 )
            {
        		last_sos_cntr = 0;
        		last_trk_cntr = 0;
        		debugout( Log_Error, "readCntrFile(): last_sos_cntr invalid" );
        		args_invalid = true;
    			arg_cntr_file = "!file";
            }
            else
            {
                String track_line = dataInputStream.readLine();
                last_trk_cntr = parseCntrLine( track_line );
                
                if ( last_trk_cntr == -1 )
                {
            		last_sos_cntr = 0;
            		last_trk_cntr = 0;
            		debugout( Log_Error, "readCntrFile(): last_trk_cntr invalid" );
            		args_invalid = true;
        			arg_cntr_file = "!file";
                }
            }
            
            
            inputStream.close();
    	}
    	catch( Exception ex )
    	{
    		last_sos_cntr = 0;
    		last_trk_cntr = 0;
    		debugout( Log_Error, "readCntrFile(): " + ex.getMessage());
    		
/* ------------------------------------------------------------------------------
 * If there's an error reading from the counter file, allow people to know 
 * about it.
 * ------------------------------------------------------------------------------ */
    		args_invalid = true;
			arg_cntr_file = "!file";
    	}
    }
    
    
    /**
     * writeCntrFile() writes sos_cntr and trk_cntr values to cntr_file.
     *  
     * @return true if it works, false if not
     */
	public boolean writeCntrFile() 
	{
		try
		{
/* ------------------------------------------------------------------------------
 * Open the file (not appending to any existing one) and write the two lines 
 * to it. 
 * ------------------------------------------------------------------------------ */
			FileWriter cntrFileStream = new FileWriter( getCntrFile(), false );
			BufferedWriter cntrFileWriter = new BufferedWriter( cntrFileStream );
			cntrFileWriter.write( new_sos_cntr +  " ; sos_cntr (old was " + last_sos_cntr + ")" );
			cntrFileWriter.newLine();
			cntrFileWriter.write( new_trk_cntr +  " ; trk_cntr (old was " + last_trk_cntr + ")" );
			cntrFileWriter.newLine();
			cntrFileWriter.close();
			return true;
		}
		catch( Exception ex )
		{
			debugout( Log_Informational_1, "Error writing output file: " + ex.getMessage() );
			return false;
		}
	}
	
	
	/**
	 * readAndCloseDescriptionFile()
	 * 
	 * The "description" file extra descriptions to go with waypoint comments.
	 * It's expected to be a saved plain-text email message containing lines such as:
	 * 
	 * 001 some more information
	 * 
	 * When waypoint 001 is encountered it'll have the comment accepted or entered 
	 * on the device.  This defaults to date and time for "new" features 
	 * (e.g. "02-JAN-14 16:29:00") and blank for existing features.
	 * 
	 * If there's extra information in the description file this is added at the front of
	 * the line, creating a description such as "some more information;02-JAN-14 16:29:00".
	 * 
	 * In order to be able to do this, we store the description information 
	 * in "descriptionHashTable".
	 */
    public void readAndCloseDescriptionFile()
    {
    	try
    	{
            InputStream inputStream = new FileInputStream( myDescriptionFile );
            DataInputStream dataInputStream = new DataInputStream( inputStream );

            boolean moreToRead = true;
            String description_line = "";
    		int space_pos = -1;
			String description_key = "";
			String description_desc = "";
            
            while( moreToRead )
            {
            	try
            	{
                    description_line = dataInputStream.readLine();
                    space_pos = description_line.indexOf( ' ' );
                    
                    /* ------------------------------------------------------------------------------
                     * The first "space" character is the separator between the "key" and the "value".
                     * We're not interested in lines with no space or a space at the start.
                     * 
                     * This process will add information from all lines (including message headers)
                     * to descriptionHashTable, but that won't cause a problem as we won't look for
                     * those.
                     * ------------------------------------------------------------------------------ */
                    if ( space_pos > 0 )
                    {
                    	try
                    	{
                        	description_key = description_line.substring( 0, space_pos );
                        	description_desc = description_line.substring( space_pos+1 );
                        	
                            /* ------------------------------------------------------------------------------
                             * Handling of a header line indicating base64 encoding is a special case.  If
                             * we've found that, we want to reject the description file altogether, since
                             * it won't have other useful information it it.
                             * ------------------------------------------------------------------------------ */
                        	if ( description_key.equals( "Content-Transfer-Encoding:" ) && description_desc.equals( "base64" ))
                        	{
                        		moreToRead = false;
                        		args_invalid = true;
                        		debugout( Log_Error, "readDescriptionFile(): " + arg_description_file + " is base64 encoded" );
                    			arg_description_file = "!file";
                        	}
                        	else
                        	{
                        		if ( description_key.equalsIgnoreCase( "note" ))
                        		{
                            		/* ------------------------------------------------------------------------------
                                     * Entries in the description file starting with "note" are just intended to be
                                     * written to stdout. 
                                     * ------------------------------------------------------------------------------ */
                            		debugout( Log_Deliberately, "NOTE: " + description_desc );
                        		}
                        		else
                        		{ // not a note
                            		/* ------------------------------------------------------------------------------
                                     * Keys are stored in the hashtable in upper case, but that is handled within 
                                     * "put"
                                     * ------------------------------------------------------------------------------ */
                                	descriptionHashTable.put( description_key, description_desc );
                        		}
                        	} // not base64
                    	}
                    	catch( Exception ex )
                    	{
                            /* ------------------------------------------------------------------------------
                             * A catch here means something invalid about this line.
                             * We'll just go around again and process the next one.
                             * ------------------------------------------------------------------------------ */
                    	}
                    }
            	}
            	catch( Exception ex )
            	{
                    /* ------------------------------------------------------------------------------
                     * A catch here means end of file (or something invalid about the file that
                     * stops us from reading it.
                     * ------------------------------------------------------------------------------ */
            		moreToRead = false;
            	}
            } // while
            		
            inputStream.close();
    	}
    	catch( Exception ex )
    	{
            /* ------------------------------------------------------------------------------
             * A catch here means something very wrong with the file - we can't read
             * anything from it.
             * ------------------------------------------------------------------------------ */
    		debugout( Log_Error, "readDescriptionFile(): " + ex.getMessage());
    		
			/* ------------------------------------------------------------------------------
			 * If there's an error reading from the description  file, allow people to know.
			 * ------------------------------------------------------------------------------ */
			arg_description_file = "!file";
			args_invalid = true;
    	}
    }
    
    
    void readAllFilesOnPath()
    {
		debugout( Log_Informational_2, "readAllFilesOnPath()" );

		myTrailfileHashtable = new TrailfileHashtable();
    	File[] listOfFiles = myPathFile.listFiles();

    	String currentYyyymmdd = ( dateFormat.format( currentDateAndTime ));
    	String currentHhmmss = ( timeFormat.format( currentDateAndTime ));
    	
    	for ( int i = 0; i < listOfFiles.length; i++ )
    	{
    		if (( listOfFiles[i].isFile()) && !args_invalid )
    		{
    			/* ------------------------------------------------------------------------------
    			 * We're interested in files that:
    			 * 1) End (case insensitive) in .GPX
    			 * 2) Starts "Sxx_" or "base_data_".
    			 * 3) Has a date, or date or time, or similar, in it.
    			 * ------------------------------------------------------------------------------ */
				if ( listOfFiles[i].toString().toUpperCase().endsWith( ".GPX" ))
				{
					Trailfile myTrailfile;
					String my2la;
					String myYyyymmdd;
					String myHhmmss;
					
					if ( listOfFiles[i].toString().regionMatches( true, arg_path.length(), "base_data_", 0, 10 ))
					{
						my2la = "  ";
						
						if ( listOfFiles[i].toString().length() < ( arg_path.length()+22 ))
						{
							debugout( Log_Informational_2, "Nonmatching base data file (too short): " + listOfFiles[i] );
						}
						else
						{
							myYyyymmdd = listOfFiles[i].toString().substring( arg_path.length()+10, arg_path.length()+18 );
							
							if ( listOfFiles[i].toString().length() == ( arg_path.length()+22 ))
							{
								if ( myYyyymmdd.compareTo( currentYyyymmdd ) <= 0 )
								{
									myHhmmss = "000000";
									myTrailfile = new Trailfile( my2la, myYyyymmdd, myHhmmss, listOfFiles[i].toString() );
									myTrailfileHashtable.put( myTrailfile );
									
									debugout( Log_Informational_2, "Matching base data file (just date): " + listOfFiles[i] );
									debugout( Log_Informational_2, "my2la: " + my2la );
									debugout( Log_Informational_2, "myHhmmss: " + myHhmmss );
								}
								else
								{
									/* ------------------------------------------------------------------------------
									 * The file that we've found on disk claims to be "in the future", so stop 
									 * processing and force the user to fix it before continuing. 
									 * ------------------------------------------------------------------------------ */
									args_invalid = true;
	                        		debugout( Log_Serious, "readAllFilesOnPath(): " + listOfFiles[i] + " claims to be from the future.  Please check it and if necessary, delete." );
									debugout( Log_Informational_2, "Nonmatching base data file (just date): " + listOfFiles[i] );
								}
								
								debugout( Log_Informational_2, "myYyyymmdd: " + myYyyymmdd );
							}
							else
							{
								if ( listOfFiles[i].toString().regionMatches( true, arg_path.length()+18, "_", 0, 1 ))
								{
									myHhmmss = listOfFiles[i].toString().substring( arg_path.length()+19, listOfFiles[i].toString().length()-4 );
									
									/* ------------------------------------------------------------------------------
									 * The string comparison below will work whether "myHhmmss" is 4 characters 
									 * or longer. 
									 * ------------------------------------------------------------------------------ */
									if ( (myYyyymmdd+myHhmmss).compareTo( (currentYyyymmdd+currentHhmmss) ) <= 0 )
									{
										myTrailfile = new Trailfile( my2la, myYyyymmdd, myHhmmss, listOfFiles[i].toString() );
										myTrailfileHashtable.put( myTrailfile );
										
										debugout( Log_Informational_2, "Matching base data file (date and time): " + listOfFiles[i] );
										debugout( Log_Informational_2, "my2la: " + my2la );
									}
									else
									{
										debugout( Log_Informational_2, "myYyyymmdd+myHhmmss: " + myYyyymmdd+myHhmmss );
										debugout( Log_Informational_2, "currentYyyymmdd+currentHhmmss: " + currentYyyymmdd+currentHhmmss );
										
										/* ------------------------------------------------------------------------------
										 * The file that we've found on disk claims to be "in the future", so stop 
										 * processing and force the user to fix it before continuing. 
										 * ------------------------------------------------------------------------------ */
										args_invalid = true;
		                        		debugout( Log_Serious, "readAllFilesOnPath(): " + listOfFiles[i] + " claims to be from the future.  Please check it and if necessary, delete." );
										debugout( Log_Informational_2, "Nonmatching base data file (date and time): " + listOfFiles[i] );
									}
									
									debugout( Log_Informational_2, "myYyyymmdd: " + myYyyymmdd );
									debugout( Log_Informational_2, "myHhmmss: " + myHhmmss );
								}
								else
								{
									debugout( Log_Informational_2, "Nonmatching base data file (date and something else): " + listOfFiles[i] );
								}
								
							}
						}
					}
					else
					{
						if (( listOfFiles[i].toString().regionMatches( true, arg_path.length(),   "S", 0, 1 )) &&
							( listOfFiles[i].toString().regionMatches( true, arg_path.length()+3, "_", 0, 1 )))
						{
							if ( listOfFiles[i].toString().length() < ( arg_path.length()+16 ))
							{
								debugout( Log_Informational_2, "Nonmatching other file (too short): " + listOfFiles[i] );
							}
							else
							{
								my2la = listOfFiles[i].toString().substring( arg_path.length()+1, arg_path.length()+3 );
								myYyyymmdd = listOfFiles[i].toString().substring( arg_path.length()+4, arg_path.length()+12 );
								
								if ( listOfFiles[i].toString().length() == ( arg_path.length()+16 ))
								{
									if ( myYyyymmdd.compareTo( currentYyyymmdd ) <= 0 )
									{
										myHhmmss = "000000";
										myTrailfile = new Trailfile( my2la, myYyyymmdd, myHhmmss, listOfFiles[i].toString() );
										myTrailfileHashtable.put( myTrailfile );
										
										debugout( Log_Informational_2, "Matching other file (just date): " + listOfFiles[i] );
										debugout( Log_Informational_2, "my2la: " + my2la );
										debugout( Log_Informational_2, "myHhmmss: " + myHhmmss );
									}
									else
									{
										/* ------------------------------------------------------------------------------
										 * The file that we've found on disk claims to be "in the future", so stop 
										 * processing and force the user to fix it before continuing. 
										 * ------------------------------------------------------------------------------ */
										args_invalid = true;
		                        		debugout( Log_Serious, "readAllFilesOnPath(): " + listOfFiles[i] + " claims to be from the future.  Please check it and if necessary, delete." );
										debugout( Log_Informational_2, "Nonmatching other file (just date): " + listOfFiles[i] );
									}
									
									debugout( Log_Informational_2, "myYyyymmdd: " + myYyyymmdd );
								}
								else
								{
									if ( listOfFiles[i].toString().length() == ( arg_path.length()+17 ))
									{
										myHhmmss = listOfFiles[i].toString().substring( arg_path.length()+12, arg_path.length()+13 );
										
										if ( myYyyymmdd.compareTo( currentYyyymmdd ) <= 0 )
										{
											myTrailfile = new Trailfile( my2la, myYyyymmdd, myHhmmss, listOfFiles[i].toString() );
											myTrailfileHashtable.put( myTrailfile );
											
											debugout( Log_Informational_2, "Matching other file (date + 1): " + listOfFiles[i] );
											debugout( Log_Informational_2, "my2la: " + my2la );
										}
										else
										{
											/* ------------------------------------------------------------------------------
											 * The file that we've found on disk claims to be "in the future", so stop 
											 * processing and force the user to fix it before continuing. 
											 * ------------------------------------------------------------------------------ */
											args_invalid = true;
			                        		debugout( Log_Serious, "readAllFilesOnPath(): " + listOfFiles[i] + " claims to be from the future.  Please check it and if necessary, delete." );
											debugout( Log_Informational_2, "Nonmatching other file (date + 1): " + listOfFiles[i] );
										}
										
										debugout( Log_Informational_2, "myYyyymmdd: " + myYyyymmdd );
										debugout( Log_Informational_2, "myHhmmss: " + myHhmmss );
									}
									else
									{
										if ( listOfFiles[i].toString().regionMatches( true, arg_path.length()+12, "_", 0, 1 ))
										{
											myHhmmss = listOfFiles[i].toString().substring( arg_path.length()+13, listOfFiles[i].toString().length()-4 );
											
											/* ------------------------------------------------------------------------------
											 * The string comparison below will work whether "myHhmmss" is 4 characters 
											 * or longer.
											 * ------------------------------------------------------------------------------ */
											if ( (myYyyymmdd+myHhmmss).compareTo( (currentYyyymmdd+currentHhmmss) ) <= 0 )
											{
												myTrailfile = new Trailfile( my2la, myYyyymmdd, myHhmmss, listOfFiles[i].toString() );
												myTrailfileHashtable.put( myTrailfile );
												
												debugout( Log_Informational_2, "Matching other file (date and time): " + listOfFiles[i] );
												debugout( Log_Informational_2, "my2la: " + my2la );
											}
											else
											{
												/* ------------------------------------------------------------------------------
												 * The file that we've found on disk claims to be "in the future", so stop 
												 * processing and force the user to fix it before continuing. 
												 * ------------------------------------------------------------------------------ */
												args_invalid = true;
				                        		debugout( Log_Serious, "readAllFilesOnPath(): " + listOfFiles[i] + " claims to be from the future.  Please check it and if necessary, delete." );
												debugout( Log_Informational_2, "Nonmatching other file (date and time): " + listOfFiles[i] );
											}
											
											debugout( Log_Informational_2, "myYyyymmdd: " + myYyyymmdd );
											debugout( Log_Informational_2, "myHhmmss: " + myHhmmss );
										}
										else
										{
											debugout( Log_Informational_2, "Nonmatching file (date and something else): " + listOfFiles[i] );
										}
									}
								}
							}
						}
						else
						{
							debugout( Log_Informational_2, "Nonmatching file: " + listOfFiles[i] );
						}
					}
				}
				else
				{
					debugout( Log_Informational_2, "Nonmatching file (not gpx): " + listOfFiles[i] );
				}

    		}
    		else
    		{
    			/* ------------------------------------------------------------------------------
    			 * Ignore subdirectories
    			 * ------------------------------------------------------------------------------ */
    		}
    	}
    }
    
	/**
	 * @param args
	 * qqq06 add Javadoc or remove altogether
	 */
	public static void main(String[] args) 
	{
		Main m = new Main();
		m.getParams( args );

/* ------------------------------------------------------------------------------
 * If we have some XML data, process it.
 * ------------------------------------------------------------------------------ */
		if (( m.mainGpxRootElement != null ) && ( m.args_invalid == false )) 
		{
			m.processInputGpxXml();
		}
	}

	/**
	 * getParams(String[] args) 
	 * Passed parameters from "main" or unit tests, this evaluates parameters and acts on them:
	 * 
	 * o It sets the debug level.
	 * o It reads in "cntr" values for the "sos" and "track" counters
	 * o It reads in the input file, parses it and places the root of the XML in "gpxRootElement"  
	 * 
	 * @param args  Arguments from the command line 
	 */
	void getParams(String[] args) 
	{
		for ( int i=0; i<args.length; i++ )
		{
			/* ------------------------------------------------------------------------------
			 * Is this argument valid?
			 * 
			 * Valid arguments are 2 or more characters long.  There's no explicit try/catch
			 * on substring checks up to length; this should prevent errors there.
			 * 
			 * If "args_invalid" is set something has been found that means that we will 
			 * abort processing.  For example, if "readAndCloseDescriptionFile()" finds a 
			 * description file and reads it, but it's base64 encoded, we abort processing.
			 * 
			 * If something sets "args_invalid" it will write debug to stdout explaining
			 * the problem.
			 * ------------------------------------------------------------------------------ */
			if (( args[i].length() >= 2) && !args_invalid )
			{
				debugout( Log_Informational_2, "arg: " + i );
				debugout( Log_Informational_2, "arg length: " + args[i].length() );
				
				/* ------------------------------------------------------------------------------
				 * Debug level
				 * 
				 * Parameters are processed in order along the command line.  It makes sense
				 * to pass "debug" as the first parameter, if required, as it will then be in 
				 * effect during the processing of the other parameters.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_debug ))
				{	
					try
					{
						arg_debug = Integer.valueOf( args[i].substring( param_debug.length() ));
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * Any failure above just means that we leave arg_debug at 0; we don't need to
						 * abort procesing.
						 * ------------------------------------------------------------------------------ */
					}
					
					debugout( Log_Informational_2, "arg_debug: " + arg_debug );
				} // debug level

				/* ------------------------------------------------------------------------------
				 * Counter file
				 * 
				 * In order that waypoints and tracks are uniquely numbered, the last number used 
				 * for each is stored in a small text file passed from the command line.  The format
				 * is as follows:
				 * 
				 * 000002 ; sos_cntr (old was 000001)
				 * 0002 ; trk_cntr (old was 0001)
				 * 
				 * The first part of each line is what we're interested in when reading the file.  
				 * The second part is only used if it needs to be manually reset (by human 
				 * intervention). 
				 * 
				 * The parameter for a counter file is not required (if it is not passed counting
				 * will just start from 0) but if it is passed the file must exist and contain
				 * valid values.  "readAndCloseCntrFile()" sets args_invalid if values are not
				 * valid.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_cntr ))
				{	
					arg_cntr_file = args[i].substring( param_cntr.length() );

					try
					{
						myCntrFile = new File( arg_cntr_file );
						
						/* ------------------------------------------------------------------------------
						 * Open the file, read last_node and last_track from the file and close it. 
						 * ------------------------------------------------------------------------------ */
						readAndCloseCntrFile();
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * If there's an error opening the counter file, don't pretend that it wasn't 
						 * specified on the command line.
						 * ------------------------------------------------------------------------------ */
						arg_cntr_file = "!file";
						args_invalid = true;
						
						debugout( Log_Informational_1, "Error opening counter file: " + ex.getMessage() );
					}
					
					debugout( Log_Informational_2, "arg_cntr_file: " + arg_cntr_file );
					debugout( Log_Informational_2, "arg_cntr_file length: " + arg_cntr_file.length() );
				} // Counter file

				/* ------------------------------------------------------------------------------
				 * Input file containing the main GPX to process
				 * 
				 * If specified, we read the file into an HTML document here.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_input_main ))
				{	
					arg_input_file_main = args[i].substring( param_input_main.length() );

					try
					{
						myInputFileMain = new File( arg_input_file_main );
					    DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
					    DocumentBuilder myBuilder = myFactory.newDocumentBuilder();
					    InputStream inputStream = new FileInputStream( myInputFileMain );
					
					    Document myDocument = myBuilder.parse( inputStream );
					    mainGpxRootElement = myDocument.getDocumentElement();
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * If there's an error opening or processing the input file, don't pretend 
						 * that it wasn't specified on the command line. 
						 * ------------------------------------------------------------------------------ */
						arg_input_file_main = "!file";
						args_invalid = true;

						debugout( Log_Serious, "Error opening main input file: " + ex.getMessage() );
					}
					
					debugout( Log_Informational_2, "arg_input_file_main: " + arg_input_file_main );
					debugout( Log_Informational_2, "arg_input_file_main length: " + arg_input_file_main.length() );
				} // Main Input file

				/* ------------------------------------------------------------------------------
				 * Supplementary input file containing another GPX to process
				 * 
				 * If specified, we read the file into an HTML document here.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_input_sup1 ))
				{	
					arg_input_file_sup1 = args[i].substring( param_input_sup1.length() );

					try
					{
						myInputFileSup1 = new File( arg_input_file_sup1 );
					    DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
					    DocumentBuilder myBuilder = myFactory.newDocumentBuilder();
					    InputStream inputStream = new FileInputStream( myInputFileSup1 );
					
					    Document myDocument = myBuilder.parse( inputStream );
					    sup1GpxRootElement = myDocument.getDocumentElement();
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * If there's an error opening or processing the supplementary input file,  
						 * just pretend that it wasn't specified on the command line. 
						 * ------------------------------------------------------------------------------ */
						arg_input_file_sup1 = "";
						debugout( Log_Error, "Error opening supplementary input file: " + ex.getMessage() );
					}
					
					debugout( Log_Informational_2, "arg_input_file: " + arg_input_file_main );
					debugout( Log_Informational_2, "arg_input_file length: " + arg_input_file_main.length() );
				} // Supplementary Input file

				/* ------------------------------------------------------------------------------
				 * Description file containing information to add to the GPX
				 * (see the javadoc for "readAndCloseDescriptionFile")
				 * 
				 * Note that multiple description files can be supplied on the command line - 
				 * "descriptionHashTable" which contains the data read from the file isn't 
				 * cleared within "readAndCloseDescriptionFile()".
				 * 
				 * The parameter for a description file is not required but if it is passed the 
				 * file must exist and not contain invalid data (which currently means 
				 * "be base64 encoded").  "readAndCloseDescriptionFile()" sets args_invalid if 
				 * values are not valid.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_description ))
				{	
					arg_description_file = args[i].substring( param_description.length() );

					try
					{
						myDescriptionFile = new File( arg_description_file );
						readAndCloseDescriptionFile();
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * If there's an error opening or processing the input file, don't pretend 
						 * that it wasn't specified on the command line. 
						 * ------------------------------------------------------------------------------ */
						arg_description_file = "!file";
						args_invalid = true;

						debugout( Log_Serious, "Error opening description file: " + ex.getMessage() );
					}
					
					debugout( Log_Informational_2, "arg_description_file: " + arg_description_file );
					debugout( Log_Informational_2, "arg_description_file length: " + arg_description_file.length() );
				} // Description file

				/* ------------------------------------------------------------------------------
				 * Path in which to search for existing GPX files to update.
				 * "readAllFilesOnPath()" reads all files on that path and updates 
				 * myTrailfileHashtable with valid ones that it finds.
				 * 
				 * If it finds that claim to be "from the future", it sets "args_invalid" to true
				 * and stops.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_path ))
				{	
					arg_path = args[i].substring( param_path.length() );

					try
					{
						myPathFile = new File( arg_path );
						/* ------------------------------------------------------------------------------
						 * If files that claim to be "in the future" are found, "args_invalid" will be
						 * set and we will stop processing.
						 * ------------------------------------------------------------------------------ */
						readAllFilesOnPath();
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * "readAllFilesOnPath()" isn't supposed to throw an Exception; this try/catch
						 * is just in case it does.
						 * ------------------------------------------------------------------------------ */
						arg_path = "!file";
						args_invalid = true;

						debugout( Log_Serious, "Error processing path : " + ex.getMessage() );
					}
					
					debugout( Log_Informational_2, "arg_path: " + arg_path );
					debugout( Log_Informational_2, "arg_path: " + arg_path.length() );
				} // Path

				/* ------------------------------------------------------------------------------
				 * Parameters for the abbreviations follow.
				 * If waypoints for the Pennine Bridleway ("PB") were stored as "Trail Head" 
				 * markers, then "-tPB" will be passed for that parameter.
				 * Currently errors here don't cause "args_invalid" to be set (which is a 
				 * debateable question).
				 *  
				 * "trail" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_t ))
				{	
					arg_t = args[i].substring( param_t.length() );


					if ( arg_t.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_t: " + arg_t );
					}
					else
					{
						debugout( Log_Error, "arg_t not 2 exactly characters long: " + arg_t );
						arg_t = "";
					}
					
					debugout( Log_Informational_2, "arg_t length: " + arg_t.length() );
				} // -t

				/* ------------------------------------------------------------------------------
				 * "bike" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_y ))
				{	
					arg_y = args[i].substring( param_y.length() );


					if ( arg_y.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_y: " + arg_y );
					}
					else
					{
						debugout( Log_Error, "arg_y not 2 exactly characters long: " + arg_y );
						arg_y = "";
					}
					
					debugout( Log_Informational_2, "arg_y length: " + arg_y.length() );
				} // -y

				/* ------------------------------------------------------------------------------
				 * "skull" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_u ))
				{	
					arg_u = args[i].substring( param_u.length() );


					if ( arg_u.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_u: " + arg_u );
					}
					else
					{
						debugout( Log_Error, "arg_u not 2 exactly characters long: " + arg_u );
						arg_u = "";
					}
					
					debugout( Log_Informational_2, "arg_u length: " + arg_u.length() );
				} // -u

				/* ------------------------------------------------------------------------------
				 * "ski" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_p ))
				{	
					arg_p = args[i].substring( param_p.length() );


					if ( arg_p.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_p: " + arg_p );
					}
					else
					{
						debugout( Log_Error, "arg_p not 2 exactly characters long: " + arg_p );
						arg_p = "";
					}
					
					debugout( Log_Informational_2, "arg_p length: " + arg_p.length() );
				} // -p

				/* ------------------------------------------------------------------------------
				 * "Hunting Area" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_a ))
				{	
					arg_a = args[i].substring( param_a.length() );


					if ( arg_a.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_a: " + arg_a );
					}
					else
					{
						debugout( Log_Error, "arg_a not 2 exactly characters long: " + arg_a );
						arg_a = "";
					}
					
					debugout( Log_Informational_2, "arg_a length: " + arg_a.length() );
				} // -a

				/* ------------------------------------------------------------------------------
				 * "RV Park" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_s ))
				{	
					arg_s = args[i].substring( param_s.length() );


					if ( arg_s.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_s: " + arg_s );
					}
					else
					{
						debugout( Log_Error, "arg_s not 2 exactly characters long: " + arg_s );
						arg_s = "";
					}
					
					debugout( Log_Informational_2, "arg_s length: " + arg_s.length() );
				} // -s

				/* ------------------------------------------------------------------------------
				 * "Glider Area" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_f ))
				{	
					arg_f = args[i].substring( param_f.length() );


					if ( arg_f.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_f: " + arg_f );
					}
					else
					{
						debugout( Log_Error, "arg_f not 2 exactly characters long: " + arg_f );
						arg_f = "";
					}
					
					debugout( Log_Informational_2, "arg_f length: " + arg_f.length() );
				} // -f

				/* ------------------------------------------------------------------------------
				 * "Ultralight Area" 2-letter abbreviation.  Expected to be exactly two characters long.
				 * ------------------------------------------------------------------------------ */
				if ( args[i].startsWith( param_g ))
				{	
					arg_g = args[i].substring( param_g.length() );


					if ( arg_g.length() == 2 )
					{
						debugout( Log_Informational_2, "arg_g: " + arg_g );
					}
					else
					{
						debugout( Log_Error, "arg_g not 2 exactly characters long: " + arg_g );
						arg_g = "";
					}
					
					debugout( Log_Informational_2, "arg_g length: " + arg_g.length() );
				} // -g

				
				//qqq01 more parameter handling will be added here if it is needed.
			} // At least 2 characters
		} // for each parameter
	} // getParams()


	/**
	 * myGetNodeValue()
	 * 
	 * Iterate through "#text" children of a node to find the XML value corresponding to an XML key.
	 * 
	 * @param passed_node
	 * @return  a String created by concatenating any "#text" nodes.  
	 */
	private String myGetNodeValue( Node passed_node )
	{
		String return_string = "";
		
		NodeList childNodes = passed_node.getChildNodes();
		int childCount = childNodes.getLength();
		
		/* ------------------------------------------------------------------------------
		* The actual text value is stored in a "#text" node
		* ------------------------------------------------------------------------------ */
		for ( int i=0; i<childCount; i++ ) 
		{
			String childNodeName = "";
			
			try
			{
				childNodeName = childNodes.item(i).getNodeName();
			}
			catch( Exception ex )
			{
				// Carry on with a blank childNodeName
			}
			
			if ( childNodeName.equals( "#text" ))
			{
				try
				{
					return_string = return_string + childNodes.item(i).getNodeValue();
				}
				catch( Exception ex )
				{
					// Carry on with a blank childNodeValue (set to "" at the top of this method
				}
			}
			/* ------------------------------------------------------------------------------
			* We don't expect to see any other "childNodeName"s
			* ------------------------------------------------------------------------------ */
		}
		
		return return_string;
	}

	
	/**
	 * processInputGpxXml3()
	 * 
	 * Read through the node tree of the GPX file, reading the waypoints into waypointMap.
	 *  
	 * @param level_1_xmlnodes  	Previously obtained, a NodeList of top-level xml nodes.
	 * @param num_l1_xmlnodes   	Previously obtained, the number of top-level xml nodes.
	 * @param inputGpxWaypointMap			The map to which to add waypoints to.
	 * @param trail3laHashSet		Populated as we go with DOA, SOS, SPB etc.
	 * @param trail2laHashSet		Populated as we go with OA, OS, BR etc.
	 * @param process_descriptions  If set, prepend any appplicable description from a separate file to waypoint comments
	 * @param processing_new_gpx	If set, generate new numbers if required for new waypoints.
	 */
	void processInputGpxXml3( NodeList level_1_xmlnodes, int num_l1_xmlnodes, 
			WaypointHashtable inputGpxWaypointMap, TrailHashset trail3laHashSet, TrailHashset trail2laHashSet, 
			boolean process_descriptions, boolean processing_new_gpx )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * Iterate through the contents of the GPX file
		 * ------------------------------------------------------------------------------------------------------------ */
		for ( int cntr_1 = 0; cntr_1 < num_l1_xmlnodes; ++cntr_1 ) 
		{
			Node this_l1_item = level_1_xmlnodes.item( cntr_1 );
			String l1_item_type = this_l1_item.getNodeName();
				
			/* ------------------------------------------------------------------------------------------------------------
			 * Other than #text nodes, here we're expecting:
			 * metadata
			 * wpt
			 * rte
			 * trk
			 * ------------------------------------------------------------------------------------------------------------ */
			if ( !l1_item_type.equals( "#text" ))
			{
				if ( l1_item_type.equals( "metadata" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * "metadata" contains the bounds
					 * It doesn't seem to be needed for anything, so we ignore it.
					 * ------------------------------------------------------------------------------------------------------------ */
				}
				else if ( l1_item_type.equals( "wpt" ))
				{
					processInputGpxXmlWaypointAttr( this_l1_item, 
							trail3laHashSet, trail2laHashSet, inputGpxWaypointMap, process_descriptions, processing_new_gpx );
				}
				else if ( l1_item_type.equals( "rte" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * A "rte" is a route.
					 * We do not need to process routes in the input GPX from the survey. 
					 * ------------------------------------------------------------------------------------------------------------ */
				}
				else if ( l1_item_type.equals( "trk" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * A "trk" is a track
					 * We do need to process, but only to renumber and output one file per track (with all waypoints), providing
					 * we have an arg_path on which to create it.
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( arg_path.equals( "" ))
					{
						debugout( Log_Informational_2, "No arg_path so not calling processInputGpxXmlTrk" );
					}
					else
					{
						processInputGpxXmlTrk( this_l1_item, inputGpxWaypointMap );
					}
				}
				else
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Something else that we're not expecting
					 * ------------------------------------------------------------------------------------------------------------ */
					debugout( Log_Informational_1, "processInputGpxXml3() l1_item_type: " + l1_item_type + " not known" );
				}
			}
		} // for everything in the GPX
	}
	

	void processInputGpxXmlTrk( Node this_l1_item, WaypointHashtable inputGpxWaypointMap )
	{
		boolean newTrkCntrOk = false;
		String trkLine = "";
		byte[] contentInBytes = trkLine.getBytes();
		
		try
		{
			/* ------------------------------------------------------------------------------------------------------------
			 * A "trk" is a track
			 * we do need to process it.
			 * We're not expecting attributes for a track.
			 * ------------------------------------------------------------------------------------------------------------ */
			NodeList level_2_xmlnodes = this_l1_item.getChildNodes();
			int num_l2_xmlnodes = level_2_xmlnodes.getLength();
			debugout( Log_Informational_2, "processInputGpxXmlTrk() L2 nodes found: " + num_l2_xmlnodes );
				
			for ( int cntr_2 = 0; cntr_2 < num_l2_xmlnodes; ++cntr_2 ) 
			{
				Node this_l2_item = level_2_xmlnodes.item( cntr_2 );
				String l2_item_type = this_l2_item.getNodeName();
					
				if ( !l2_item_type.equals("#text" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Here we're expecting most but not necessarily all of:
					 * 
					 * name
					 * extensions
					 * trkseg
					 * qqq03 what other parts of a waypoint in the GPX might we see?
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( l2_item_type.equals( "name" ))
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * Show the name in debug, but we won't actually use it.
						 * ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_2, "name: " + myGetNodeValue( this_l2_item ) );
					} 
					else if ( l2_item_type.equals( "trkseg" ))
					{
						debugout( Log_Informational_2, "trkseg found" );
						newTrkCntrOk = incNewTrkCntr();

						if ( newTrkCntrOk )
						{
							debugout( Log_Informational_2, "Resultant track: TR" + getNewTrkCntrString() );
							createNewTrackFile();

							debugout( Log_Informational_2, "last_sos_cntr: " + last_sos_cntr + ", next_sos_cntr: " + new_sos_cntr );
							for ( int cntr_1 = last_sos_cntr+1; cntr_1 <= new_sos_cntr; cntr_1++ )
							{
								Waypoint waypoint = inputGpxWaypointMap.get( String.format( "%06d", cntr_1 ) );
									
								if ( waypoint == null )
								{
									debugout( Log_Error, "processInputGpxXmlTrk:  Expected but not found: " + String.format( "%06d", cntr_1 ) );
								}
								else
								{
									writeWptToGpx( waypoint );
								}
							} // for

							trkLine = "    <trk>\n";
							contentInBytes = trkLine.getBytes();
							newGpxFileStream.write(contentInBytes);

							trkLine = "      <name>" + "TS" + getNewTrkCntrString() + "</name>\n";
							contentInBytes = trkLine.getBytes();
							newGpxFileStream.write(contentInBytes);

							processOldGpxXmlTrksegChildren( this_l2_item );
							
							trkLine = "    </trk>\n";
							contentInBytes = trkLine.getBytes();
							newGpxFileStream.write(contentInBytes);
							closeNewGpxFile();
						}
						else
						{
							debugout( Log_Error, "Error calculating key for storing track: " + getNewTrkCntrString() );
						}
					}
					else if ( l2_item_type.equals( "extensions" ))
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * We can ignore "extensions"
						 * ------------------------------------------------------------------------------------------------------------ */
					}
					else
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * Something else that we're not expecting, that doesn't necessarily imply an error
						 * ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_1, "processInputGpxXmlTrk() l2_item_type: " + l2_item_type + " not known" );
					}
				} // not #text
			} // for the children of a trk
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an error opening or processing the GPX file here
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "processInputGpxXmlTrk: Error writing TRK to  GPX file: " + ex.getMessage() );
		}
	}
	
	
	/**
	 * outputCounterFile()
	 * 
	 * Output the updated counter values in the same format as written to the counter file
	 * onto stdout so the user can see them.
	 * 
	 * Also output the newly updated base data filename.
	 */
	void outputCounterFile()
	{
		debugout( Log_Deliberately, new_sos_cntr + " ; sos_cntr (old was " + last_sos_cntr + ")" );
		debugout( Log_Deliberately, new_trk_cntr + " ; trk_cntr (old was " + last_trk_cntr + ")" );

		try
		{
			if ( base_data_already_processed )
			{
				debugout( Log_Deliberately, "New base data file: base_data_" + dateFormat.format( currentDateAndTime ) + "_" + timeFormat.format( currentDateAndTime ) + ".GPX" );
			}
			else
			{
				debugout( Log_Deliberately, "Unchanged base data file: " + myTrailfileHashtable.get( "  " ).getFileName() );
			}
		}
		catch( Exception ex )
		{
			debugout( Log_Deliberately, "Base data file unavailable" );
		}
	}
	
	
	/**
	 * outputTrail3laHashSet()
	 * 
	 * Output to stdout a list of the types of the points that we have found, like this:
	 * 
	 * New points: DTR, SOU, DOA found
	 * 
	 * @param trail3laHashSet
	 */
	void outputTrail3laHashSet( TrailHashset trail3laHashSet )
	{
		String trail3laString = "";
		for( String item : trail3laHashSet )
		{
			if ( trail3laString.equals( "" ))
			{
				trail3laString = item;
			}
			else
			{
				/* ------------------------------------------------------------------------------
				 * The assignment is done this way around because the Hashset seems to work
				 *  like a LIFO queue - add A then B, and iterate through it, and you get B first.
				 * ------------------------------------------------------------------------------ */
				trail3laString = item + ", " + trail3laString;
			}
		} // for
		
		if ( trail3laString.equals( "" ))
		{
			debugout( Log_Serious, "No new points found." );
		}
		else
		{
			debugout( Log_Serious, "New points: " + trail3laString + " found." );
		}
	}
	
	
	/**
	 * processTrail2laHashSet()
	 * 
	 * For each member of trail2laHashSet:
	 * If it's not a base data two-letter abbreviation, call processOneTrail2la() with it and inputGpxWaypointMap
	 * (all the new and changed waypoints).  processOneTrail2la() then adds new items and marks items as deleted
	 * by changing the first letter from S to D or E as required.
	 * 
	 * If it is a base data two-letter abbreviation the processing is similar but the base data file is only
	 * processed once.  The "base_data_already_processed" flag is held at instance level rather than here
	 * because it is also used in outputCounterFile to indicate to the user what a new base data file name
	 * will have been.
	 * 
	 * @param trail2laHashSet  - a set of all of the two-letter abbreviations that we are interested in.
	 * @param inputGpxWaypointMap  - the waypoints that we've read from the input GPX file 
	 */
	void processTrail2laHashSet( TrailHashset trail2laHashSet, WaypointHashtable inputGpxWaypointMap )
	{
		for( String trail2laHashSetitem : trail2laHashSet )
		{
			if (( trail2laHashSetitem.equals( "OS" )) ||	// base data (middle, middle)
				( trail2laHashSetitem.equals( "OU" )) ||	// base data (middle, north)
				( trail2laHashSetitem.equals( "OX" )) ||	// base data (middle, south)
				( trail2laHashSetitem.equals( "OV" )) ||	// base data (west)
				( trail2laHashSetitem.equals( "OW" )) ||	// base data (east)
				( trail2laHashSetitem.equals( "WS" )) ||	// base data, north of Doncaster
				( trail2laHashSetitem.equals( "TO" )) ||	// base data, north of Tadcaster
				( trail2laHashSetitem.equals( "WU" )) ||	// base data, north of Helmsley
				( trail2laHashSetitem.equals( "OT" )) ||	// base data, pubs, shipwrecks etc.
				( trail2laHashSetitem.equals( "TD" )))		// Indicative of a parameter missed from the command line
			{
				/* ------------------------------------------------------------------------------
				 * For these I'll need to process base data.
				 * 
				 * "myTrailfileHashtable" is intitialised when we start processing files.  It is
				 * updated with the most recent file containing either base data or 
				 * an abbreviation. 
				 * 
				 * We will expect to often find multiple types of base data items in 
				 * trail2laHashSet.  The first time we find one we process all base data items.
				 * ------------------------------------------------------------------------------ */
				if ( base_data_already_processed )
				{
					debugout( Log_Informational_2, "Base data already processed for: " + trail2laHashSetitem );
				}
				else
				{
					Trailfile base_data = myTrailfileHashtable.get( "  " );
					if ( base_data == null )
					{
						debugout( Log_Informational_2, "Base data not found for: " + trail2laHashSetitem );
					}
					else
					{
						debugout( Log_Informational_2, "Base data found for: " + trail2laHashSetitem + ", " + base_data.getYyyymmdd() + ", " + base_data.getHhmmss() );
						processOneTrail2la( "  ", inputGpxWaypointMap );
					}
					
					base_data_already_processed = true;
				}
				
			}
			else
			{
				/* ------------------------------------------------------------------------------
				 * We've found waypoints in a new GPX corresponding to a trail (such as "PB" for
				 * the Pennine Bridleway).  Is there already a file for that trail on disk?
				 * 
				 * These might either be new points in the form "Sxx123456" or deleted old points
				 * in the form Dxx123456.
				 * ------------------------------------------------------------------------------ */
				Trailfile item_file = myTrailfileHashtable.get( trail2laHashSetitem );
				if ( item_file == null )
				{
					/* ------------------------------------------------------------------------------
					 * If we can't find a file for the 2la, it might not be an error - all sorts
					 * of stuff is contained within base_data and will be processed when we
					 * process base data.  Output an information line out here though.
					 * ------------------------------------------------------------------------------ */
					debugout( Log_Informational_1, "Item file not found for: " + trail2laHashSetitem );
				}
				else
				{
					debugout( Log_Informational_2, "Item file found for: " + trail2laHashSetitem + ", " + item_file.getYyyymmdd() + ", " + item_file.getHhmmss() );
					processOneTrail2la( trail2laHashSetitem, inputGpxWaypointMap );
				}
			}
		}
	}
	
	
	/**
	 * processOneTrail2la()
	 * Read in the old GPX file for 2-letter abbreviation passedFound2la and apply changes according to
	 * waypoints in inputGpxWaypointMap.
	 * 
	 * @param passedFound2la  - the 2-letter that we are interested in, or "  " for base data.
	 * @param inputGpxWaypointMap
	 */
	void processOneTrail2la( String passedFound2la, WaypointHashtable inputGpxWaypointMap )
	{
		debugout( Log_Top_Routine_Start, "processOneTrail2la( " + passedFound2la + " )" );
		debugout( Log_Informational_2, "Item file: " + myTrailfileHashtable.get( passedFound2la ).getFileName() );

		try
		{
			File myOldGpxFile = new File( myTrailfileHashtable.get( passedFound2la ).getFileName() );
		    DocumentBuilderFactory myFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder myBuilder = myFactory.newDocumentBuilder();
		    InputStream myOldGpxStream = new FileInputStream( myOldGpxFile );
		
		    Document myDocument = myBuilder.parse( myOldGpxStream );
		    Element oldGpxRootElement = myDocument.getDocumentElement();

		    createNewGpxFile( passedFound2la );
		    processOldGpxXml( oldGpxRootElement, passedFound2la, inputGpxWaypointMap );
		    closeNewGpxFile();
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an error opening or processing the GPX file here
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "processOneTrail2la:  Error opening old GPX file: " + myTrailfileHashtable.get( passedFound2la ).getFileName() + ", " + ex.getMessage() );
		}
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Create a file for either "base data" (if passedFound2la is blank) or for the
	 * waypoints indicated by passedFound2la.  It will have todays's date and time
	 * and we will copy all waypoints from the previous equivalent file to it, as well
	 * as adding any new ones. 
	 * ------------------------------------------------------------------------------ */
	void createNewGpxFile( String passedFound2la )
	{
		String newGpxFileName;
		
		if ( passedFound2la.equals( "  " ))
		{
			newGpxFileName = arg_path + "base_data_" + 
					dateFormat.format( currentDateAndTime ) + "_" +
					timeFormat.format( currentDateAndTime ) + ".GPX";
		}
		else
		{
			newGpxFileName = arg_path + "S" + 
					myTrailfileHashtable.get( passedFound2la ).getTwoLetterAbbreviation() + "_" + 
					dateFormat.format( currentDateAndTime ) + "_" +
					timeFormat.format( currentDateAndTime ) + ".GPX";
		}
		
		createNewFileCommon( newGpxFileName);
	}
	
	
	/* ------------------------------------------------------------------------------
	 * Create a file for a GPX track.  It will be named using a unique number
	 * and we will copy all new waypoints from the input file to it.
	 * ------------------------------------------------------------------------------ */
	void createNewTrackFile()
	{
		String newGpxFileName = arg_path + "TS" + getNewTrkCntrString() + "a.GPX";
		createNewFileCommon( newGpxFileName);
	}
	
	
	void createNewFileCommon( String newGpxFileName )
	{
		debugout( Log_Informational_2, "New GPX file: " + newGpxFileName );
		String headerXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n";
		String headerGpx = "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.13.7\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensions/v3/GpxExtensionsv3.xsd http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n";
		
		try
		{
			File newGpxFile = new File( newGpxFileName );
			newGpxFile.createNewFile();
			newGpxFileStream = new FileOutputStream( newGpxFile );

			byte[] contentInBytes = headerXml.getBytes();
			newGpxFileStream.write(contentInBytes);
			
			contentInBytes = headerGpx.getBytes();
			newGpxFileStream.write(contentInBytes);
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an error opening or processing the GPX file here
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "createNewFileCommon:  Error creating new GPX file: " + newGpxFileName + ", " + ex.getMessage() );
		}
	}
	
	
	void closeNewGpxFile(  )
	{
		String trailerGpx = "</gpx>\n";
		
		try
		{
			byte[] contentInBytes = trailerGpx.getBytes();
			newGpxFileStream.write(contentInBytes);
			
			newGpxFileStream.flush();
			newGpxFileStream.close();
			debugout( Log_Informational_2, "closeNewGpxFile()" );
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an writing the trailer data to or closing the GPX file here:
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "closeNewGpxFile:  Error closing new GPX file: " + ex.getMessage() );
		}
	}
	
	
	/* ------------------------------------------------------------------------------
	 * The GPX processing in "processOldGpxXml()" etc. is split into smaller sections to
	 * make the code a bit more legible.
	 * ------------------------------------------------------------------------------ */
	void processOldGpxXml( Element passed_oldGpxRootElement, String passedFound2la, WaypointHashtable inputGpxWaypointMap )
	{
		if ( mainGpxRootElement.getNodeType() == Node.ELEMENT_NODE ) 
		{
			debugout( Log_Informational_2, "element" );
			
			NodeList level_1_xmlnodes = passed_oldGpxRootElement.getChildNodes();
			int num_l1_xmlnodes = level_1_xmlnodes.getLength();
			debugout( Log_Informational_2, "Notes L1 nodes found: " + num_l1_xmlnodes );

			processOldGpxXml3( level_1_xmlnodes, num_l1_xmlnodes, passedFound2la, inputGpxWaypointMap );
		}
		else
		{
			/* ------------------------------------------------------------------------------
			 * We couldn't understand the root element of the GPX
			 * Ensure that the user sees the message, but continue processing.
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Informational_1, "gpxRootElement is not an element; old GPX cannot be processed." );
		}
	} // processOldGpxXml


	/**
	 * processOldGpxXml3()
	 * 
	 * Read through the node tree of the GPX file, reading the waypoints into waypointMap.
	 *  
	 * @param level_1_xmlnodes  	Previously obtained, a NodeList of top-level xml nodes.
	 * @param num_l1_xmlnodes   	Previously obtained, the number of top-level xml nodes.
	 */
	void processOldGpxXml3( NodeList level_1_xmlnodes, int num_l1_xmlnodes, String passedFound2la, WaypointHashtable inputGpxWaypointMap )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * We only need to append new waypoints once - after the last old waypoint and before the first route, track
		 * (or after everything if no route or track).
		 * ------------------------------------------------------------------------------------------------------------ */
		boolean new_wpts_written_yet = false;
		
		/* ------------------------------------------------------------------------------------------------------------
		 * Iterate through the contents of the GPX file
		 * ------------------------------------------------------------------------------------------------------------ */
		for ( int cntr_1 = 0; cntr_1 < num_l1_xmlnodes; ++cntr_1 ) 
		{
			Node this_l1_item = level_1_xmlnodes.item( cntr_1 );
			String l1_item_type = this_l1_item.getNodeName();
				
			/* ------------------------------------------------------------------------------------------------------------
			 * Other than #text nodes, here we're expecting:
			 * metadata
			 * wpt
			 * rte
			 * trk
			 * ------------------------------------------------------------------------------------------------------------ */
			if ( !l1_item_type.equals( "#text" ))
			{
				if ( l1_item_type.equals( "metadata" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * "metadata" contains the bounds
					 * We don't need to use it for anything.
					 * ------------------------------------------------------------------------------------------------------------ */
				}
				else if ( l1_item_type.equals( "wpt" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * A "wpt" is a waypoint.  We do want to transfer waypoint info
					 * ------------------------------------------------------------------------------------------------------------ */
					processOldGpxXmlWaypointAttr( this_l1_item, inputGpxWaypointMap );
				}
				else if ( l1_item_type.equals( "rte" ))
				{
					if ( !new_wpts_written_yet )
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * If we've got here then we must have processed all the waypoints in the old GPX, 
						 * and must be ready to add our new ones.
						 * ------------------------------------------------------------------------------------------------------------ */
						processNewGpxWpts( passedFound2la, inputGpxWaypointMap );
						new_wpts_written_yet = true;
					}
					
					/* ------------------------------------------------------------------------------------------------------------
					 * A "rte" is a route
					 * ------------------------------------------------------------------------------------------------------------ */
					processOldGpxXmlRouteChildren( this_l1_item );
				}
				else if ( l1_item_type.equals( "trk" ))
				{
					if ( !new_wpts_written_yet )
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * If we've got here then we must have processed all the waypoints in the old GPX, 
						 * and must be ready to add our new ones.
						 * ------------------------------------------------------------------------------------------------------------ */
						processNewGpxWpts( passedFound2la, inputGpxWaypointMap );
						new_wpts_written_yet = true;
					}
					
					/* ------------------------------------------------------------------------------------------------------------
					 * A "trk" is a track
					 * We're not expecting old base_data or route files to contain tracks (and for my purposes they don't need to)
					 * so I'll ignore any tracks found here. 
					 * ------------------------------------------------------------------------------------------------------------ */
				}
				else
				{
					if ( !new_wpts_written_yet )
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * If we've got here then we must have processed all the waypoints in the old GPX, 
						 * and must be ready to add our new ones.
						 * ------------------------------------------------------------------------------------------------------------ */
						processNewGpxWpts( passedFound2la, inputGpxWaypointMap );
						new_wpts_written_yet = true;
					}
					
					/* ------------------------------------------------------------------------------------------------------------
					 * Something else that we're not expecting
					 * ------------------------------------------------------------------------------------------------------------ */
					debugout( Log_Informational_1, "processOldGpxXml3() l1_item_type: " + l1_item_type + " not known" );
				}
			}
		} // for everything in the GPX

		if ( !new_wpts_written_yet )
		{
			/* ------------------------------------------------------------------------------------------------------------
			 * If we've got here then we must have processed all the waypoints in the old GPX, 
			 * and must be ready to add our new ones.
			 * ------------------------------------------------------------------------------------------------------------ */
			processNewGpxWpts( passedFound2la, inputGpxWaypointMap );
			new_wpts_written_yet = true;
		}
	}
	

	void processNewGpxWpts( String passedFound2la, WaypointHashtable inputGpxWaypointMap )
	{
		debugout( Log_Informational_2, "last_sos_cntr: " + last_sos_cntr + ", next_sos_cntr: " + new_sos_cntr );
		
		for ( int cntr_1 = last_sos_cntr+1; cntr_1 <= new_sos_cntr; cntr_1++ )
		{
			Waypoint waypoint = inputGpxWaypointMap.get( String.format( "%06d", cntr_1 ) );
			
			if ( waypoint == null )
			{
				debugout( Log_Error, "processNewGpxWpts:  Expected but not found: " + String.format( "%06d", cntr_1 ) );
			}
			else
			{
				if ( passedFound2la.equals( "  " ))
				{
					if (( waypoint.getTwoLetterAbbreviation().equals( "OS" )) ||	// base data (middle, middle)
						( waypoint.getTwoLetterAbbreviation().equals( "OU" )) ||	// base data (middle, north)
						( waypoint.getTwoLetterAbbreviation().equals( "OX" )) ||	// base data (middle, south)
						( waypoint.getTwoLetterAbbreviation().equals( "OV" )) ||	// base data (west)
						( waypoint.getTwoLetterAbbreviation().equals( "OW" )) ||	// base data (east)
						( waypoint.getTwoLetterAbbreviation().equals( "WS" )) ||	// base data, north of Doncaster
						( waypoint.getTwoLetterAbbreviation().equals( "TO" )) ||	// base data, north of Tadcaster
						( waypoint.getTwoLetterAbbreviation().equals( "WU" )) ||	// base data, north of Helmsley
						( waypoint.getTwoLetterAbbreviation().equals( "OT" )) ||	// base data, pubs, shipwrecks etc.
						( waypoint.getTwoLetterAbbreviation().equals( "TD" )))		// Indicative of a parameter missed from the command line
					{
						/* ------------------------------------------------------------------------------
						 * If debug is required here:
						 * debugout( Log_Informational_2, "base_data found: " + String.format( "%06d", cntr_1 ) );
						 * ------------------------------------------------------------------------------ */
						writeWptToGpx( waypoint );
					}
				}
				else
				{
					if ( waypoint.getTwoLetterAbbreviation().equals( passedFound2la ))
					{
						/* ------------------------------------------------------------------------------
						 * If debug is required here:
						 * debugout( Log_Informational_2, "passedFound2la found: " + String.format( "%06d", cntr_1 ) );
						 * ------------------------------------------------------------------------------ */
						writeWptToGpx( waypoint );
					}
				}
			}
		}
	}
	
	
	/**
	 * processOldGpxXmlRouteChildren()
	 * 
	 * Obtain the attributes of the XML node for this route, 
	 * to process the child attributes such as "rtept", etc.
	 *  
	 * @param this_l1_item
	 */
	void processOldGpxXmlRouteChildren( Node this_l1_item )
	{
		String wptHeader = "  <rte>\n";
		String wptLine = "";
		
		try
		{
			byte[] contentInBytes = wptHeader.getBytes();
			newGpxFileStream.write(contentInBytes);

			/* ------------------------------------------------------------------------------------------------------------
			 * Routes are expected to have tags (XML child nodes) only 
			 * ------------------------------------------------------------------------------------------------------------ */
			NodeList level_2_xmlnodes = this_l1_item.getChildNodes();
			int num_l2_xmlnodes = level_2_xmlnodes.getLength();
			debugout( Log_Informational_2, "wpt: L2 nodes found: " + num_l2_xmlnodes );
			
			for ( int cntr_2 = 0; cntr_2 < num_l2_xmlnodes; ++cntr_2 ) 
			{
				Node this_l2_item = level_2_xmlnodes.item( cntr_2 );
				String l2_item_type = this_l2_item.getNodeName();
				
				if ( !l2_item_type.equals("#text" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Here we're expecting most but not necessarily all of:
					 * 
					 * name
					 * extensions
					 * rtept
					 * qqq03 what other parts of a waypoint in the GPX might we see?
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( l2_item_type.equals( "name" ))
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * We're just processing an old WPT so we don't need to worry about renumbering.  Just store the current name 
						 * in "number" and set the other parts of the name to empty strings.
						 * ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_2, "name: " + myGetNodeValue( this_l2_item ) );
						wptLine = "    <name>" + myGetNodeValue( this_l2_item ) + "</name>\n";
						contentInBytes = wptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					} 
					else if ( l2_item_type.equals( "rtept" ))
					{
						processOldGpxXmlRteptAttr( this_l2_item );
					}
					else if ( l2_item_type.equals( "extensions" ))
					{
						/* ------------------------------------------------------------------------------------------------------------
						* We can ignore "extensions"
						* ------------------------------------------------------------------------------------------------------------ */
					}
					else
					{
						/* ------------------------------------------------------------------------------------------------------------
						* Something else that we're not expecting, that doesn't necessarily imply an error
						* ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_1, "processOldGpxXmlRouteChildren:  l2_item_type: " + l2_item_type + " not known" );
					}
				} // not #text
			} // for the children of a wpt
		
			/* ------------------------------------------------------------------------------------------------------------
			 * We have now processed all of this route.  Write the trailer.
			 * ------------------------------------------------------------------------------------------------------------ */
			String wptTrailer = "  </rte>\n";
			
			contentInBytes = wptTrailer.getBytes();
			newGpxFileStream.write(contentInBytes);
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an error opening or processing the GPX file here
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "Error writing RTE to  GPX file: " + ex.getMessage() );
		}
	}
	
	
	void processOldGpxXmlTrksegChildren( Node this_l2_item )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * Trksegs are expected to be tags (XML child nodes) only  
		 * ------------------------------------------------------------------------------------------------------------ */
		NodeList level_3_xmlnodes = this_l2_item.getChildNodes();
		int num_l3_xmlnodes = level_3_xmlnodes.getLength();
		debugout( Log_Informational_2, "processOldGpxXmlTrksegChildren(): L3 nodes found: " + num_l3_xmlnodes );
		
		try
		{
			String trkLine = "      <trkseg>\n";
			byte[] contentInBytes = trkLine.getBytes();
			newGpxFileStream.write(contentInBytes);
			
			for ( int cntr_3 = 0; cntr_3 < num_l3_xmlnodes; ++cntr_3 ) 
			{
				Node this_l3_item = level_3_xmlnodes.item( cntr_3 );
				String l3_item_type = this_l3_item.getNodeName();
				
				if ( !l3_item_type.equals("#text" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Here we're expecting most but not necessarily all of:
					 * 
					 * trkpt
					 * ele
					 * time
					 * qqq03 what other parts of a trkseg in the GPX might we see?
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( l3_item_type.equals( "trkpt" ))
					{
						debugout( Log_Informational_2, "trkpt found" );
						processOldGpxXmlTrkptAttr( this_l3_item );
					}
					else
					{
						/* ------------------------------------------------------------------------------------------------------------
						* Something else that we're not expecting, that doesn't necessarily imply an error
						* ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_1, "processOldGpxXmlTrksegChildren() l3_item_type: " + l3_item_type + " not known" );
					}
				}
			}
			
			trkLine = "      </trkseg>\n";
			contentInBytes = trkLine.getBytes();
			newGpxFileStream.write(contentInBytes);
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an error opening or processing the GPX file here
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "processOldGpxXmlRouteChildren:  Error writing TRK to  GPX file: " + ex.getMessage() );
		}
	}
	
	
	void processOldGpxXmlTrkptAttr( Node this_l3_item )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * Route points are expected to have attributes and tags (XML child nodes)  
		 * ------------------------------------------------------------------------------------------------------------ */
		NodeList level_4_xmlnodes = this_l3_item.getChildNodes();
		int num_l4_xmlnodes = level_4_xmlnodes.getLength();
		debugout( Log_Informational_2, "processOldGpxXmlTrkptAttr() L4 nodes found: " + num_l4_xmlnodes );

		/* ------------------------------------------------------------------------------------------------------------
		 * Route points can have both attributes (e.g. "lon", "lat") and tags (XML child nodes) - process the attributes first. 
		 * ------------------------------------------------------------------------------------------------------------ */
		Node lat_node = null;
		Node lon_node = null;
				
		if ( this_l3_item.hasAttributes() )
		{
			NamedNodeMap item_attributes = this_l3_item.getAttributes();
			lat_node = item_attributes.getNamedItem( "lat" );
			lon_node = item_attributes.getNamedItem( "lon" );
		} // attributes

		if ( lat_node == null )
		{
			debugout( Log_Informational_1, "No trkpt lat found" );
        }
		else
		{
			debugout( Log_Informational_2, "Lat: " + lat_node.getNodeValue() );
		}

		if ( lon_node == null )
		{
			debugout( Log_Informational_1, "No trkpt lon found" );
        }
		else
		{
			debugout( Log_Informational_2, "Lon: " + lon_node.getNodeValue() );
		}

		/* ------------------------------------------------------------------------------------------------------------
		 * Only continue processing this trkpt if it has valid lat and lon. 
		 * ------------------------------------------------------------------------------------------------------------ */
		if (( lat_node != null ) && ( lon_node != null ))
		{
			String trkptLine = "      <trkpt lat=\"" + lat_node.getNodeValue() + "\" lon=\"" + lon_node.getNodeValue() + "\">\n";
			
			try
			{
				byte[] contentInBytes = trkptLine.getBytes();
				newGpxFileStream.write(contentInBytes);

				processOldGpxXmlTrkptChildren( level_4_xmlnodes, num_l4_xmlnodes );
				
				trkptLine = "      </trkpt>\n";
				contentInBytes = trkptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			catch( Exception ex )
			{
				/* ------------------------------------------------------------------------------
				 * If there's an error opening or processing the GPX file here
				 * ------------------------------------------------------------------------------ */
				debugout( Log_Error, "processOldGpxXmlTrkptAttr:  Error writing TRKPT to  GPX file: " + ex.getMessage() );
			}
			
		} // valid lat and lon
	}
	
	void processOldGpxXmlTrkptChildren( NodeList level_4_xmlnodes, int num_l4_xmlnodes )
	{
		for ( int cntr_4 = 0; cntr_4 < num_l4_xmlnodes; ++cntr_4 ) 
		{
			String trkptLine;
			byte[] contentInBytes;
			
			Node this_l4_item = level_4_xmlnodes.item( cntr_4 );
			String l4_item_type = this_l4_item.getNodeName();
			
			try
			{
				if ( !l4_item_type.equals("#text" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Here we're expecting most but not necessarily all of:
					 * 
					 * ele
					 * time
					 * qqq03 what other parts of a trkpt in the GPX might we see?
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( l4_item_type.equals( "ele" ))
					{
						debugout( Log_Informational_2, "ele: " + myGetNodeValue( this_l4_item ) );
						trkptLine = "        <ele>" + myGetNodeValue( this_l4_item ) + "</ele>\n";
						contentInBytes = trkptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l4_item_type.equals( "time" ))
					{
						debugout( Log_Informational_2, "time: " + myGetNodeValue( this_l4_item ) );
						trkptLine = "        <time>" + myGetNodeValue( this_l4_item ) + "</time>\n";
						contentInBytes = trkptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else
					{
						/* ------------------------------------------------------------------------------------------------------------
						* Something else that we're not expecting, that doesn't necessarily imply an error
						* ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_1, "processOldGpxXmlTrkptChildren() l4_item_type: " + l4_item_type + " not known" );
					}
				}
			}
			catch( Exception ex )
			{
				/* ------------------------------------------------------------------------------
				 * If there's an error opening or processing the GPX file here
				 * ------------------------------------------------------------------------------ */
				debugout( Log_Error, "processOldGpxXmlTrkptChildren:  Error writing TRKPT to  GPX file: " + ex.getMessage() );
			}
		}
	}
	
	
	void processOldGpxXmlRteptAttr( Node this_l2_item )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * Route points are expected to have attributes and tags (XML child nodes)  
		 * ------------------------------------------------------------------------------------------------------------ */
		NodeList level_3_xmlnodes = this_l2_item.getChildNodes();
		int num_l3_xmlnodes = level_3_xmlnodes.getLength();
		debugout( Log_Informational_2, "processOldGpxXmlRteptAttr() L3 nodes found: " + num_l3_xmlnodes );

		/* ------------------------------------------------------------------------------------------------------------
		 * Route points can have both attributes (e.g. "lon", "lat") and tags (XML child nodes) - process the attributes first. 
		 * ------------------------------------------------------------------------------------------------------------ */
		Node lat_node = null;
		Node lon_node = null;
				
		if ( this_l2_item.hasAttributes() )
		{
			NamedNodeMap item_attributes = this_l2_item.getAttributes();
			lat_node = item_attributes.getNamedItem( "lat" );
			lon_node = item_attributes.getNamedItem( "lon" );
		} // attributes

		if ( lat_node == null )
		{
			debugout( Log_Informational_1, "No rtept lat found" );
        }
		else
		{
			debugout( Log_Informational_2, "Lat: " + lat_node.getNodeValue() );
		}

		if ( lon_node == null )
		{
			debugout( Log_Informational_1, "No rtept lon found" );
        }
		else
		{
			debugout( Log_Informational_2, "Lon: " + lon_node.getNodeValue() );
		}

		/* ------------------------------------------------------------------------------------------------------------
		 * Only continue processing this rtept if it has valid lat and lon. 
		 * ------------------------------------------------------------------------------------------------------------ */
		if (( lat_node != null ) && ( lon_node != null ))
		{
			String rteptLine = "    <rtept lat=\"" + lat_node.getNodeValue() + "\" lon=\"" + lon_node.getNodeValue() + "\">\n";
			
			try
			{
				byte[] contentInBytes = rteptLine.getBytes();
				newGpxFileStream.write(contentInBytes);

				processOldGpxXmlRteptChildren( level_3_xmlnodes, num_l3_xmlnodes );
				
				rteptLine = "    </rtept>\n\n";
				contentInBytes = rteptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			catch( Exception ex )
			{
				/* ------------------------------------------------------------------------------
				 * If there's an error opening or processing the GPX file here
				 * ------------------------------------------------------------------------------ */
				debugout( Log_Error, "processOldGpxXmlRteptAttr:  Error writing RTEPT to  GPX file: " + ex.getMessage() );
			}
			
		} // valid lat and lon
	}
	
	void processOldGpxXmlRteptChildren( NodeList level_3_xmlnodes, int num_l3_xmlnodes )
	{
		for ( int cntr_3 = 0; cntr_3 < num_l3_xmlnodes; ++cntr_3 ) 
		{
			String rteptLine;
			byte[] contentInBytes;
			
			Node this_l3_item = level_3_xmlnodes.item( cntr_3 );
			String l3_item_type = this_l3_item.getNodeName();
			
			try
			{
				if ( !l3_item_type.equals("#text" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Here we're expecting most but not necessarily all of:
					 * 
					 * ele
					 * name
					 * cmt
					 * desc
					 * sym
					 * extensions
					 * qqq03 what other parts of a rtept in the GPX might we see?
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( l3_item_type.equals( "ele" ))
					{
						debugout( Log_Informational_2, "ele: " + myGetNodeValue( this_l3_item ) );
						rteptLine = "      <ele>" + myGetNodeValue( this_l3_item ) + "</ele>\n";
						contentInBytes = rteptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l3_item_type.equals( "name" ))
					{
						debugout( Log_Informational_2, "name: " + myGetNodeValue( this_l3_item ) );
						rteptLine = "      <name>" + myGetNodeValue( this_l3_item ) + "</name>\n";
						contentInBytes = rteptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l3_item_type.equals( "cmt" ))
					{
						debugout( Log_Informational_2, "cmt: " + myGetNodeValue( this_l3_item ) );
						rteptLine = "      <cmt>" + myGetNodeValue( this_l3_item ) + "</cmt>\n";
						contentInBytes = rteptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l3_item_type.equals( "desc" ))
					{
						debugout( Log_Informational_2, "desc: " + myGetNodeValue( this_l3_item ) );
						rteptLine = "      <desc>" + myGetNodeValue( this_l3_item ) + "</desc>\n";
						contentInBytes = rteptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l3_item_type.equals( "time" ))
					{
						debugout( Log_Informational_2, "time: " + myGetNodeValue( this_l3_item ) );
						rteptLine = "      <time>" + myGetNodeValue( this_l3_item ) + "</time>\n";
						contentInBytes = rteptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l3_item_type.equals( "sym" ))
					{
						debugout( Log_Informational_2, "sym: " + myGetNodeValue( this_l3_item ) );
						rteptLine = "      <sym>" + myGetNodeValue( this_l3_item ) + "</sym>\n";
						contentInBytes = rteptLine.getBytes();
						newGpxFileStream.write(contentInBytes);
					}
					else if ( l3_item_type.equals( "extensions" ))
					{
						/* ------------------------------------------------------------------------------------------------------------
						* We can ignore "extensions"
						* ------------------------------------------------------------------------------------------------------------ */
					}
					else
					{
						/* ------------------------------------------------------------------------------------------------------------
						* Something else that we're not expecting, that doesn't necessarily imply an error
						* ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_1, "processOldGpxXmlRteptChildren() l3_item_type: " + l3_item_type + " not known" );
					}
				}
			}
			catch( Exception ex )
			{
				/* ------------------------------------------------------------------------------
				 * If there's an error opening or processing the GPX file here
				 * ------------------------------------------------------------------------------ */
				debugout( Log_Error, "processOldGpxXmlRteptChildren:  Error writing RTEPT to  GPX file: " + ex.getMessage() );
			}
		}
	}
	
	
	/**
	 * processOldGpxXmlWaypointAttr()
	 * 
	 * Obtain the attributes of the XML node for this waypoint, 
	 * and if there's a valid lat and lon call processOldGpxXmlWaypointChildren() 
	 * to process the child attributes such as "name", "cmt", etc.
	 *  
	 * @param this_l1_item
	 * @param inputGpxWaypointMap  - the waypoint map from the input GPX that we will transfer details from.
	 */
	void processOldGpxXmlWaypointAttr( Node this_l1_item, WaypointHashtable inputGpxWaypointMap )
	{
			/* ------------------------------------------------------------------------------------------------------------
			 * A "wpt" is a waypoint
			 * we do need to process it.
			 * ------------------------------------------------------------------------------------------------------------ */
			NodeList level_2_xmlnodes = this_l1_item.getChildNodes();
			int num_l2_xmlnodes = level_2_xmlnodes.getLength();
			debugout( Log_Informational_2, "wpt: L2 nodes found: " + num_l2_xmlnodes );
					
			Waypoint waypoint = new Waypoint();
			/* ------------------------------------------------------------------------------------------------------------
			 * waypoints can have both attributes (e.g. "lon", "lat") and tags (XML child nodes) - process the attributes first. 
			 * ------------------------------------------------------------------------------------------------------------ */
			Node lat_node = null;
			Node lon_node = null;
					
			if ( this_l1_item.hasAttributes() )
			{
				NamedNodeMap item_attributes = this_l1_item.getAttributes();
				lat_node = item_attributes.getNamedItem( "lat" );
				lon_node = item_attributes.getNamedItem( "lon" );
			} // attributes

			if ( lat_node == null )
			{
				debugout( Log_Informational_1, "No wpt lat found" );
            }
			else
			{
				waypoint.setLat( lat_node.getNodeValue() );
				debugout( Log_Informational_2, "Lat: " + lat_node.getNodeValue() );
			}

			if ( lon_node == null )
			{
				debugout( Log_Informational_1, "No wpt lon found" );
            }
			else
			{
				waypoint.setLon( lon_node.getNodeValue() );
				debugout( Log_Informational_2, "Lon: " + lon_node.getNodeValue() );
			}

			/* ------------------------------------------------------------------------------------------------------------
			 * Only continue processing this waypoint if it has valid lat and lon. 
			 * ------------------------------------------------------------------------------------------------------------ */
			if (( lat_node != null ) && ( lon_node != null ))
			{
				processOldGpxXmlWaypointChildren( level_2_xmlnodes, num_l2_xmlnodes, waypoint, inputGpxWaypointMap );
			} // valid lat and lon
	}
	
	
	/**
	 * processOldGpxXmlWaypointChildren()
	 * 
	 * We found valid lat and lon attributes for the XML node for a waypoint, 
	 * in the "old" GPX so process the XML children such as "name", "cmt", etc.
	 * 
	 * Once we know all about this waypoint we optionally add description information and renumber.
	 * We then add to the "waypointMap" which has been passed through.
	 * 
	 * @param level_1_xmlnodes
	 * @param num_l1_xmlnodes
	 * @param level_2_xmlnodes
	 * @param num_l2_xmlnodes
	 * @param waypoint
	 */
	void processOldGpxXmlWaypointChildren( NodeList level_2_xmlnodes, int num_l2_xmlnodes, Waypoint waypoint, WaypointHashtable inputGpxWaypointMap )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * This is set if we find a "name" match when processing the old GPX file.  When we do, we also want to 
		 * transfer any of "cmt", "desc", "time" and "sym" that occur. 
		 * 
		 * processOldGpxXmlWaypointChildren is called per XML node that has valid lat and lon attributes, so this is
		 * reset to false before processing each GPX waypoint.
		 * ------------------------------------------------------------------------------------------------------------ */
		boolean transferFromInputGpx = false;
		
		for ( int cntr_2 = 0; cntr_2 < num_l2_xmlnodes; ++cntr_2 ) 
		{
			Node this_l2_item = level_2_xmlnodes.item( cntr_2 );
			String l2_item_type = this_l2_item.getNodeName();
			
			if ( !l2_item_type.equals("#text" ))
			{
				/* ------------------------------------------------------------------------------------------------------------
				 * Here we're expecting most but not necessarily all of:
				 * 
				 * name
				 * cmt
				 * desc
				 * sym
				 * extensions
				 * ele
				 * time (rarely - we ignore it if we see it.)
				 * qqq03 what other parts of a waypoint in the GPX might we see?
				 * ------------------------------------------------------------------------------------------------------------ */
				if ( l2_item_type.equals( "name" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * We're just processing an old WPT, but call "processXmlWaypointNoRenumber" 
					 * so that each bit is in the correct place
					 * ------------------------------------------------------------------------------------------------------------ */
					waypoint.setWaypointNumber( myGetNodeValue( this_l2_item ));
					boolean wayPointOk = processXmlWaypointNoRenumber( waypoint );
					
					/* ------------------------------------------------------------------------------------------------------------
					 * If we were successfully able to move the "old" waypoint fields to the correct place
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( wayPointOk )
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * ... and the same waypoint number exists in the new input GPX 
						 * ------------------------------------------------------------------------------------------------------------ */
						if ( inputGpxWaypointMap.get( waypoint.getWaypointNumber() ) != null )
						{
							/* ------------------------------------------------------------------------------------------------------------
							 * ... and it starts with D or E in the new input GPX, rename it to that in the old GPX too.
							 * 
							 * We also check that the 2la in each file matches.  This is regardless of which "old" GPX file we think that
							 * we're processing - there are all sorts of odd 2las for various routes store in base_data.
							 * 
							 * Also tell the user that a "deleted" waypoint has been found, and apply other changes from the input GPX to
							 * the "old" GPX, so that if comment has been changed from "E" (meaning to me there's a path that goes east  
							 * and needs survey) to "JE" (meaning to me that a path should be joined to the east and doesn't need survey)
							 * it's the latter that ends up in base data or 2la file on disk.  The track files to upload are written 
							 * with the details from the input GPX and so already include updated details.
							 * 
							 * Note that updates in the input GPX that don't involve marking a waypoint as "D" or "E" don't get written
							 * to an updated GPX file on disk.
							 * ------------------------------------------------------------------------------------------------------------ */
							if ((( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getInitial().startsWith( "D" )) ||
								 ( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getInitial().startsWith( "E" ))) &&
								(  inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getTwoLetterAbbreviation().equals( waypoint.getTwoLetterAbbreviation() ) ))
							{
								debugout( Log_Deliberately, "Deleted: " + inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getInitial() + waypoint.getTwoLetterAbbreviation() + waypoint.getWaypointNumber() );
								transferFromInputGpx = true;
								waypoint.setInitial( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getInitial() );
							}
							else
							{
								/* ------------------------------------------------------------------------------------------------------------
								 * This waypoint number doesn't start with D or E in the new input GPX
								 * ------------------------------------------------------------------------------------------------------------ */
							}
						}
						else
						{
							/* ------------------------------------------------------------------------------------------------------------
							 * This waypoint number doesn't exist in the new input GPX
							 * ------------------------------------------------------------------------------------------------------------ */
						}
					}
					else
					{
						/* ------------------------------------------------------------------------------------------------------------
						 * An error occurred renumbering; set it back how it was.
						 * ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Error, "processOldGpxXmlWaypointChildren() !wayPointOk: " + myGetNodeValue( this_l2_item ) );
						waypoint.setInitial( "" );
						waypoint.setTwoLetterAbbreviation( "" );
					}
					
					debugout( Log_Informational_2, "processOldGpxXmlWaypointChildren() name: " + myGetNodeValue( this_l2_item ) );
				} 
				else if ( l2_item_type.equals( "cmt" ))
				{
					if ( transferFromInputGpx )
					{
						waypoint.setCmt( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getCmt() );
					}
					else
					{
						waypoint.setCmt( myGetNodeValue( this_l2_item ) );
					}
					
					debugout( Log_Informational_2, "cmt: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "desc" ))
				{
					if ( transferFromInputGpx )
					{
						waypoint.setDesc( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getDesc() );
					}
					else
					{
						waypoint.setDesc( myGetNodeValue( this_l2_item ) );
					}
					
					debugout( Log_Informational_2, "desc: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "time" ))
				{
					if ( transferFromInputGpx )
					{
						waypoint.setTime( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getTime() );
					}
					else
					{
						waypoint.setTime( myGetNodeValue( this_l2_item ) );
					}
					
					debugout( Log_Informational_2, "time: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "sym" ))
				{
					if ( transferFromInputGpx )
					{
						waypoint.setSym( inputGpxWaypointMap.get( waypoint.getWaypointNumber()).getSym() );
					}
					else
					{
						waypoint.setSym( myGetNodeValue( this_l2_item ) );
					}
					
					debugout( Log_Informational_2, "sym: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "extensions" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					* We can ignore "extensions"
					* ------------------------------------------------------------------------------------------------------------ */
				}
				else if ( l2_item_type.equals( "ele" ))
				{
					waypoint.setEle( myGetNodeValue( this_l2_item ) );
					debugout( Log_Informational_2, "ele: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "time" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					* We can ignore "time", if it appears (which it rarely does).
					* ------------------------------------------------------------------------------------------------------------ */
				}
				else
				{
					/* ------------------------------------------------------------------------------------------------------------
					* Something else that we're not expecting, that doesn't necessarily imply an error
					* ------------------------------------------------------------------------------------------------------------ */
					debugout( Log_Informational_1, "processOldGpxXmlWaypointChildren() l2_item_type: " + l2_item_type + " not known" );
				}
			} // not #text
		} // for the children of a wpt
	
		/* ------------------------------------------------------------------------------------------------------------
		 * We now know all there is to know from the GPX about this wpt.
		 * ------------------------------------------------------------------------------------------------------------ */
		writeWptToGpx( waypoint );
	}	

	
	void writeWptToGpx( Waypoint waypoint)
	{
		try
		{
			String wptHeader = "  <wpt lat=\"" + waypoint.getLat() + "\" lon=\"" + waypoint.getLon() + "\">\n";
			String wptLine = "";
			
			byte[] contentInBytes = wptHeader.getBytes();
			newGpxFileStream.write(contentInBytes);

			if ( waypoint.getEle() != "" )
			{
				wptLine = "    <ele>" + waypoint.getEle() + "</ele>\n";
				contentInBytes = wptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			
			/* ------------------------------------------------------------------------------------------------------------
			 * From an old GPX, the whole name may be in getWaypointNumber() and the other fields blank.
			 * For a new one, other fields are needed.
			 * ------------------------------------------------------------------------------------------------------------ */
			if ( waypoint.getWaypointNumber() != "" )
			{
				wptLine = "    <name>" + waypoint.getInitial() + waypoint.getTwoLetterAbbreviation() + waypoint.getWaypointNumber() + "</name>\n";
				contentInBytes = wptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			
			if ( waypoint.getCmt() != "" )
			{
				wptLine = "    <cmt>" + waypoint.getCmt() + "</cmt>\n";
				contentInBytes = wptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			
			if ( waypoint.getDesc() != "" )
			{
				wptLine = "    <desc>" + waypoint.getDesc() + "</desc>\n";
				contentInBytes = wptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			
			/* ------------------------------------------------------------------------------------------------------------
			 * We don't write out "time" for "time" points but "desc" so that MapSource can load the data.
			 * ------------------------------------------------------------------------------------------------------------ */
			if ( waypoint.getTime() != "" )
			{
				wptLine = "    <desc>" + waypoint.getTime() + "</desc>\n";
				contentInBytes = wptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			
			if ( waypoint.getSym() != "" )
			{
				wptLine = "    <sym>" + waypoint.getSym() + "</sym>\n";
				contentInBytes = wptLine.getBytes();
				newGpxFileStream.write(contentInBytes);
			}
			
			// I don't believe that we need "extensions".
			
			String wptTrailer = "  </wpt>\n\n";
			contentInBytes = wptTrailer.getBytes();
			newGpxFileStream.write(contentInBytes);
		}
		catch( Exception ex )
		{
			/* ------------------------------------------------------------------------------
			 * If there's an error opening or processing the GPX file here
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Error, "writeWptToGpx:  Error writing WPT to  GPX file: " + waypoint.getWaypointNumber() + ", " + ex.getMessage() );
		}
	}
	
	
	void processInputGpxXml2a( NodeList level_1_xmlnodes, int num_l1_xmlnodes, 
							  WaypointHashtable inputGpxWaypointMap, TrailHashset trail3laHashSet, TrailHashset trail2laHashSet )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * Read through all the nodes in the GPX and populate waypointMap, and also trail3laHashSet and trail2laHashSet.
		 * 
		 * The first boolean flag at the end says "and process descriptions from descriptionHashTable"
		 * The second says "we're processing a new GPX from a device and therefore need to generate numbers 
		 * for new points".
		 * ------------------------------------------------------------------------------------------------------------ */
		processInputGpxXml3( level_1_xmlnodes, num_l1_xmlnodes, inputGpxWaypointMap, trail3laHashSet, trail2laHashSet, true, true );
	}

	
	void processInputGpxXml2b( WaypointHashtable inputGpxWaypointMap, TrailHashset trail3laHashSet, TrailHashset trail2laHashSet )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * trail3laHashSet has been updated with details of the various trails that we have found (including a leading
		 * "D" or "S"). A line is written to stdout for the user's benefit about this.
		 * ------------------------------------------------------------------------------------------------------------ */
		outputTrail3laHashSet( trail3laHashSet );

		/* ------------------------------------------------------------------------------------------------------------
		 * trail2laHashSet has been updated with details of the various trails that we have found, without the leading
		 * "D" or "S".  We can now look for examples of these files already on disk, if we've been given an arg_path
		 * to search from
		 * ------------------------------------------------------------------------------------------------------------ */
		if ( arg_path.equals( "" ))
		{
			debugout( Log_Informational_2, "No arg_path so not calling processTrail2laHashSet" );
		}
		else
		{
			processTrail2laHashSet( trail2laHashSet, inputGpxWaypointMap );
		}
		
		/* ------------------------------------------------------------------------------------------------------------
		 * The updated "counter" contents are written to stdout for information.
		 * ------------------------------------------------------------------------------------------------------------ */
		outputCounterFile();
	}


	/* ------------------------------------------------------------------------------
	 * The GPX processing in "processInputGpxXml()" etc. is split into smaller sections to
	 * make the code a bit more legible.
	 * 
	 * IF we have valid data, all the actual processing is called from here.
	 * Only if that succeeds is writeCntrFile() called.
	 * ------------------------------------------------------------------------------ */
	void processInputGpxXml()
	{
		if ( mainGpxRootElement.getNodeType() == Node.ELEMENT_NODE ) 
		{
			debugout( Log_Informational_2, "element" );
			
			NodeList main_level_1_xmlnodes = mainGpxRootElement.getChildNodes();
			int main_num_l1_xmlnodes = main_level_1_xmlnodes.getLength();
			debugout( Log_Informational_2, "Notes L1 nodes found: " + main_num_l1_xmlnodes );

			/* ------------------------------------------------------------------------------
			 * Initialise the SOS and TRK counters.
			 * ------------------------------------------------------------------------------ */
			boolean newSosCntrOk = setNewSosCntr( getLastSosCntr() );
			boolean newTrkCntrOk = setNewTrkCntr( getLastTrkCntr() );
			
			if ( newSosCntrOk && newTrkCntrOk)
			{
				/* ------------------------------------------------------------------------------------------------------------
				 * Initialise the waypoint map into which we'll store new waypoints, 
				 * and the sets in which we'll store details of which trail abbreviations we have seen so far - both the 
				 * "3-letter" version printed out for the user and the "2-letter" version used for locating files on disk.
				 * ------------------------------------------------------------------------------------------------------------ */
				WaypointHashtable inputGpxWaypointMap = new WaypointHashtable();
				TrailHashset trail3laHashSet = new TrailHashset();
				TrailHashset trail2laHashSet = new TrailHashset();

				/* ------------------------------------------------------------------------------------------------------------
				 * The "supplementary" GPX file should have only waypoints in it.  Process it first so that all waypoints are
				 * known about before looking at tracks. 
				 * ------------------------------------------------------------------------------------------------------------ */
				if ( sup1GpxRootElement != null )
				{
					NodeList sup1_level_1_xmlnodes = sup1GpxRootElement.getChildNodes();
					int sup1_num_l1_xmlnodes = sup1_level_1_xmlnodes.getLength();
					processInputGpxXml2a( sup1_level_1_xmlnodes, sup1_num_l1_xmlnodes, inputGpxWaypointMap, trail3laHashSet, trail2laHashSet );
				}

				/* ------------------------------------------------------------------------------------------------------------
				 * The "main" GPX file may have waypoints and tracks in it.  
				 * We need to process tracks only after all waypoints. 
				 * ------------------------------------------------------------------------------------------------------------ */
				processInputGpxXml2a( main_level_1_xmlnodes, main_num_l1_xmlnodes, inputGpxWaypointMap, trail3laHashSet, trail2laHashSet );

				processInputGpxXml2b( inputGpxWaypointMap, trail3laHashSet, trail2laHashSet );
				
				/* ------------------------------------------------------------------------------
				 * If we don't have a valid arg_path then we won't have written out any changes
				 * to existing files or any new track files.
				 * ------------------------------------------------------------------------------ */
				if ( arg_path.equals( "" ))
				{
					debugout( Log_Informational_2, "No arg_path so not calling writeCntrFile()" );
				}
				else
				{
					writeCntrFile();
				}
			}
			else
			{
				/* ------------------------------------------------------------------------------
				 * If we can't calculate new counter values we need to fix that issue before
				 * processing the GPX file.
				 * ------------------------------------------------------------------------------ */
				debugout( Log_Serious, "Couldn't assign SOS or TRK counter" );
			}
		}
		else
		{
			/* ------------------------------------------------------------------------------
			 * We couldn't understand the root element of the GPX
			 * ------------------------------------------------------------------------------ */
			debugout( Log_Informational_1, "gpxRootElement is not an element; input GPX cannot be processed." );
		}
	} // processInputGpxXml


	/**
	 * processInputGpxXmlWaypointAttr()
	 * 
	 * Obtain the attributes of the XML node for this waypoint, 
	 * and if there's a valid lat and lon call processInputGpxXmlWaypointChildren() 
	 * to process the child attributes such as "name", "cmt", etc.
	 *  
	 * @param this_l1_item
	 * @param trail3laHashSet
	 * @param trail2laHashSet
	 * @param inputGpxWaypointMap
	 * @param process_descriptions
	 * @param processing_new_gpx
	 */
	void processInputGpxXmlWaypointAttr( Node this_l1_item, 
			TrailHashset trail3laHashSet, TrailHashset trail2laHashSet, WaypointHashtable inputGpxWaypointMap,
			boolean process_descriptions, boolean processing_new_gpx )
	{
			/* ------------------------------------------------------------------------------------------------------------
			 * A "wpt" is a waypoint
			 * we do need to process it.
			 * ------------------------------------------------------------------------------------------------------------ */
			NodeList level_2_xmlnodes = this_l1_item.getChildNodes();
			int num_l2_xmlnodes = level_2_xmlnodes.getLength();
			debugout( Log_Informational_2, "wpt: L2 nodes found: " + num_l2_xmlnodes );
					
			Waypoint waypoint = new Waypoint();
			/* ------------------------------------------------------------------------------------------------------------
			 * waypoints can have both attributes (e.g. "lon", "lat") and tags (XML child nodes) - process the attributes first. 
			 * ------------------------------------------------------------------------------------------------------------ */
			Node lat_node = null;
			Node lon_node = null;
					
			if ( this_l1_item.hasAttributes() )
			{
				NamedNodeMap item_attributes = this_l1_item.getAttributes();
				lat_node = item_attributes.getNamedItem( "lat" );
				lon_node = item_attributes.getNamedItem( "lon" );
			} // attributes

			if ( lat_node == null )
			{
				debugout( Log_Informational_1, "No wpt lat found" );
            }
			else
			{
				waypoint.setLat( lat_node.getNodeValue() );
				debugout( Log_Informational_2, "Lat: " + lat_node.getNodeValue() );
			}

			if ( lon_node == null )
			{
				debugout( Log_Informational_1, "No wpt lon found" );
            }
			else
			{
				waypoint.setLon( lon_node.getNodeValue() );
				debugout( Log_Informational_2, "Lon: " + lon_node.getNodeValue() );
			}

			/* ------------------------------------------------------------------------------------------------------------
			 * Only continue processing this waypoint if it has valid lat and lon. 
			 * ------------------------------------------------------------------------------------------------------------ */
			if (( lat_node != null ) && ( lon_node != null ))
			{
				processInputGpxXmlWaypointChildren( level_2_xmlnodes, num_l2_xmlnodes, 
						trail3laHashSet, trail2laHashSet, inputGpxWaypointMap, waypoint, process_descriptions, processing_new_gpx );
			} // valid lat and lon
	}
	
	
	/**
	 * processInputGpxXmlWaypointChildren()
	 * 
	 * We found valid lat and lon attributes for the XML node for a waypoint, 
	 * so process the XML children such as "name", "cmt", etc.
	 * 
	 * Once we know all about this waypoint we optionally add description information and renumber.
	 * We then add to the "waypointMap" which has been passed through.
	 * 
	 * @param level_1_xmlnodes
	 * @param num_l1_xmlnodes
	 * @param level_2_xmlnodes
	 * @param num_l2_xmlnodes
	 * @param trail3laHashSet
	 * @param trail2laHashSet
	 * @param inputGpxWaypointMap
	 * @param waypoint
	 * @param process_descriptions
	 * @param processing_new_gpx
	 */
	void processInputGpxXmlWaypointChildren( NodeList level_2_xmlnodes, int num_l2_xmlnodes, 
			TrailHashset trail3laHashSet, TrailHashset trail2laHashSet, WaypointHashtable inputGpxWaypointMap, Waypoint waypoint,
			boolean process_descriptions, boolean processing_new_gpx )
	{
		for ( int cntr_2 = 0; cntr_2 < num_l2_xmlnodes; ++cntr_2 ) 
		{
			Node this_l2_item = level_2_xmlnodes.item( cntr_2 );
			String l2_item_type = this_l2_item.getNodeName();
			
			if ( !l2_item_type.equals("#text" ))
			{
				/* ------------------------------------------------------------------------------------------------------------
				 * Here we're expecting most but not necessarily all of:
				 * 
				 * name
				 * cmt
				 * desc
				 * sym
				 * extensions
				 * ele
				 * qqq03 what other parts of a waypoint in the GPX might we see?
				 * ------------------------------------------------------------------------------------------------------------ */
				if ( l2_item_type.equals( "name" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Once we've got all the fields we'll decide whether we need to renumber or not.  In the meantime, we'll just
					 * store the current name in "number" and set the other parts of the name to empty strings.
					 * ------------------------------------------------------------------------------------------------------------ */
					waypoint.setInitial( "" );
					waypoint.setTwoLetterAbbreviation( "" );
					waypoint.setWaypointNumber( myGetNodeValue( this_l2_item ));
					debugout( Log_Informational_2, "name: " + myGetNodeValue( this_l2_item ) );
				} 
				else if ( l2_item_type.equals( "cmt" ))
				{
					waypoint.setCmt( myGetNodeValue( this_l2_item ) );
					debugout( Log_Informational_2, "cmt: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "desc" ))
				{
					waypoint.setDesc( myGetNodeValue( this_l2_item ) );
					debugout( Log_Informational_2, "desc: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "time" ))
				{
					waypoint.setTime( myGetNodeValue( this_l2_item ) );
					debugout( Log_Informational_2, "time: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "sym" ))
				{
					waypoint.setSym( myGetNodeValue( this_l2_item ) );
					debugout( Log_Informational_2, "sym: " + myGetNodeValue( this_l2_item ) );
				}
				else if ( l2_item_type.equals( "extensions" ))
				{
					/* ------------------------------------------------------------------------------------------------------------
					* We can ignore "extensions"
					* ------------------------------------------------------------------------------------------------------------ */
				}
				else if ( l2_item_type.equals( "ele" ))
				{
					waypoint.setEle( myGetNodeValue( this_l2_item ) );
					debugout( Log_Informational_2, "ele: " + myGetNodeValue( this_l2_item ) );
				}
				else
				{
					/* ------------------------------------------------------------------------------------------------------------
					* Something else that we're not expecting, that doesn't necessarily imply an error
					* ------------------------------------------------------------------------------------------------------------ */
					debugout( Log_Informational_1, "processInputGpxXmlWaypointChildren() l2_item_type: " + l2_item_type + " not known" );
				}
			} // not #text
		} // for the children of a wpt
	
		/* ------------------------------------------------------------------------------------------------------------
		 * We now know all there is to know from the GPX about this wpt.
		 * ------------------------------------------------------------------------------------------------------------ */

		/* ------------------------------------------------------------------------------------------------------------
		 * When assigning a number to a waypoint we may run out (because there's a maximum of 6 characters.
		 * If that happens, this will be set to false:
		 * ------------------------------------------------------------------------------------------------------------ */
		boolean wayPointOk = true;
		
		if ( process_descriptions )
		{
			/* ------------------------------------------------------------------------------------------------------------
			 * getWaypointNumber() is still the original one that it was assigned on the GPX.
			 * Using that, if required, we apply any extended description that there might be in "descriptionHashTable".
			 * 
			 * Strings are stored in descriptionHashTable using upper case keys, but that is handled within "get" and "put".
			 * ------------------------------------------------------------------------------------------------------------ */
			if ( descriptionHashTable.get( waypoint.getWaypointNumber() ) != null )
			{
				waypoint.setCmt( descriptionHashTable.get( waypoint.getWaypointNumber() ) + ";" + waypoint.getCmt() );
				debugout( Log_Informational_2, "waypoint: " + waypoint.getWaypointNumber() + " comment is now: " + waypoint.getCmt() );
			}
		} // process_descriptions
		
		/* ------------------------------------------------------------------------------------------------------------
		 * If we're processing a new GPX file then we may need to renumber some points.  For example, a new green dot 
		 * called 001 will get assigned a number such as "DTRaaaaaa" where aaaaaa is a sequential number read from the
		 * counter file.  
		 * ------------------------------------------------------------------------------------------------------------ */
		if ( processing_new_gpx )
		{
			try
			{
				/* ------------------------------------------------------------------------------------------------------------
				 * Any waypoint that starts with "S" or "V" will be ignored, as we believe that it will be an existing numbered
				 * waypoint in the GPX.  "DCxx" are created on the fly by the C# OsmImport in mode 2 (exporting an area from
				 * base data) and can also be ignored.
				 * ------------------------------------------------------------------------------------------------------------ */
				if (( waypoint.getWaypointNumber().substring( 0, 1 ).equals( "S"  ))  ||
					( waypoint.getWaypointNumber().substring( 0, 1 ).equals( "V"  ))  ||
					( waypoint.getWaypointNumber().substring( 0, 2 ).equals( "DC" )))
				{
					debugout( Log_Informational_2, "Ignoring waypoint: " + waypoint.getWaypointNumber() );
				} // S or V, or DCNE etc.
				else
				{
					debugout( Log_Informational_2, "Need to process waypoint: " + waypoint.getWaypointNumber() );
					/* ------------------------------------------------------------------------------------------------------------
					 * If it's a D or an E we don't need to renumber it, but we do need to store the initial, 2la and waypoint 
					 * number in the right place.
					 * ------------------------------------------------------------------------------------------------------------ */
					if (( waypoint.getWaypointNumber().substring( 0, 1 ).equals( "D"  ))  ||
						( waypoint.getWaypointNumber().substring( 0, 1 ).equals( "E"  )))
					{
						wayPointOk = processXmlWaypointNoRenumber( waypoint );
						/* ------------------------------------------------------------------------------------------------------------
						 * If the 2la isn't yet in trail2laHashSet, we'll add it.  If an old file for the 2la exists a new one will
						 * be created and the waypoint that has changed will be changed from S to D or E as appropriate.
						 * ------------------------------------------------------------------------------------------------------------ */
						debugout( Log_Informational_2, "Potential 2la deletion: " + waypoint.getTwoLetterAbbreviation() );
						trail2laHashSet.add( waypoint.getTwoLetterAbbreviation() );
					} // D or E
					else
					{
						wayPointOk = processXmlWaypointRenumber( trail3laHashSet, trail2laHashSet, waypoint );

					} // not DE (and not SV)
				} // not SV or DCNE etc.
			}
			catch( Exception ex )
			{
				/* ------------------------------------------------------------------------------------------------------------
				 * A catch here means that we've found a zero- or 1-character waypoint.
				 * Even if 1-character, it can't be a meaningful S, V, D, or E, so treat as a new waypoint that needs a new
				 * number.
				 * ------------------------------------------------------------------------------------------------------------ */
				wayPointOk = processXmlWaypointRenumber( trail3laHashSet, trail2laHashSet, waypoint );
			}
		}
		else
		{
			/* ------------------------------------------------------------------------------------------------------------
			 * If we're not processing a new GPX we don't need to renumber but do need to move the data from the
			 * waypoint number field to the initial and two-letter abbreviation.
			 * ------------------------------------------------------------------------------------------------------------ */
			wayPointOk = processXmlWaypointNoRenumber( waypoint );
		}
		
		if ( wayPointOk )
		{
			inputGpxWaypointMap.put( waypoint );
		}
	}	

	
	/**
	 * processXmlWaypointNoRenumber()
	 * 
	 * Currently waypoint.getWaypointNumber() contains the initial, 2-letter abbreviation and actual waypoint number.
	 * We move these values into their correct fields.
	 * 
	 * If an error occurs (e.g. due to string manipulation), we return "false" to indicate this.
	 * 
	 * @param waypoint
	 * @return true if it worked, false if not
	 */
	boolean processXmlWaypointNoRenumber( Waypoint waypoint )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * Something may go wrong with the string assignments.
		 * If that happens, this will be set to false.
		 * ------------------------------------------------------------------------------------------------------------ */
		boolean wayPointOk = true;
		
		debugout( Log_Informational_2, "Don't need to renumber waypoint: " + waypoint.getWaypointNumber() );
		
		try
		{
			waypoint.setInitial( waypoint.getWaypointNumber().substring( 0, 1 ) );
			waypoint.setTwoLetterAbbreviation( waypoint.getWaypointNumber().substring( 1, 3 ) );
			waypoint.setWaypointNumber( waypoint.getWaypointNumber().substring( 3 ) );
		
			debugout( Log_Informational_2, "Waypoint: " + waypoint.getInitial() + waypoint.getTwoLetterAbbreviation() + waypoint.getWaypointNumber() );
		}
		catch( Exception ex )
		{
			wayPointOk = false;
			debugout( Log_Informational_1, "Exception calculating key for storing waypoint: " + waypoint.getInitial() + waypoint.getTwoLetterAbbreviation() + waypoint.getWaypointNumber() );
		}
		
		return wayPointOk;
	}

	
	/**
	 * processXmlWaypointRenumber()
	 * 
	 * We've got a waypoint that isn't already marked as S, V, D or E 
	 * (e.g. a new "blue flag" that needs to be assigned an "SOS" number)
	 * We generate a new number for it, if we can, and populate trail3laHashSet and trail2laHashSet as appropriate. 
	 * 
	 * @param trail3laHashSet
	 * @param trail2laHashSet
	 * @param waypoint
	 * @return true if it worked, false if not
	 */
	boolean processXmlWaypointRenumber( TrailHashset trail3laHashSet, TrailHashset trail2laHashSet, Waypoint waypoint )
	{
		/* ------------------------------------------------------------------------------------------------------------
		 * When assigning a number to a waypoint we may run out (because there's a maximum of 6 characters.
		 * If that happens, this will be set to false.
		 * ------------------------------------------------------------------------------------------------------------ */
		boolean wayPointOk = true;
		
		/* ------------------------------------------------------------------------------------------------------------
		 * If it's not an S or a V or DCNE etc., and not a D or an E we do need to assign a new number. 
		 * ------------------------------------------------------------------------------------------------------------ */
		try
		{
			/* ------------------------------------------------------------------------------------------------------------
			 * New "blue" or "red" points get assigned a geographically derived 2la and an initial of "S". 
			 * ------------------------------------------------------------------------------------------------------------ */
			if (( waypoint.getSym().equals( "Flag, Blue"  )) ||
				( waypoint.getSym().equals( "Block, Blue" )) ||
				( waypoint.getSym().equals( "Pin, Blue"   )) ||
				( waypoint.getSym().equals( "Flag, Red"   )) ||
				( waypoint.getSym().equals( "Block, Red"  )) ||
				( waypoint.getSym().equals( "Pin, Red"    )))
			{
				waypoint.setInitial( "S" );
				waypoint.setTwoLetterAbbreviation( setBlueOrRed2la( waypoint ));
			} // blue or red
			else
			{
				/* ------------------------------------------------------------------------------------------------------------
				 * New "green" points are "TR" 
				 * ------------------------------------------------------------------------------------------------------------ */
				if (( waypoint.getSym().equals( "Flag, Green"  )) ||
					( waypoint.getSym().equals( "Block, Green" )) ||
					( waypoint.getSym().equals( "Pin, Green"   )) ||
					( waypoint.getSym().equals( "City (Small)"    )))
				{
					waypoint.setTwoLetterAbbreviation( "TR" );
					
					try
					{
						if( waypoint.getCmt().substring( 0, 4 ).equalsIgnoreCase( "keep" ))
						{
							waypoint.setInitial( "S" );
						}
						else
						{
							waypoint.setInitial( "D" );
						}
					}
					catch( Exception ex )
					{
						/* ------------------------------------------------------------------------------
						 * An exception here isn't an error - it just means that the length of the 
						 * waypoint comment is less than 4.
						 * ------------------------------------------------------------------------------ */
						waypoint.setInitial( "D" );
					}
				} // green
				else
				{
					/* ------------------------------------------------------------------------------------------------------------
					 * Not blue, red or green 
					 * ------------------------------------------------------------------------------------------------------------ */
					if ( waypoint.getSym().equals( "Trail Head"  ))
					{
						waypoint.setInitial( "S" );
						
						/* ------------------------------------------------------------------------------------------------------------
						 * If e.g. a "-t=PB" argument is passed, then "Trail Head" markers will be set to "PB"  
						 * ------------------------------------------------------------------------------------------------------------ */
						if ( getArgT().equals( "" ))
						{
							waypoint.setTwoLetterAbbreviation( "TD" );
						}
						else
						{
							waypoint.setTwoLetterAbbreviation( getArgT() );
						}
					}
					else if ( waypoint.getSym().equals( "Bike Trail"  ))
					{
						if ( getArgY().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgY() );
						}
					}
					else if ( waypoint.getSym().equals( "Skull and Crossbones"  ))
					{
						if ( getArgU().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgU() );
						}
					}
					else if ( waypoint.getSym().equals( "Ski Resort"  ))
					{
						if ( getArgP().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgP() );
						}
					}
					else if ( waypoint.getSym().equals( "Hunting Area"  ))
					{
						if ( getArgA().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgA() );
						}
					}
					else if ( waypoint.getSym().equals( "RV Park"  ))
					{
						if ( getArgS().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgS() );
						}
					}
					else if ( waypoint.getSym().equals( "Glider Area"  ))
					{
						if ( getArgF().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgF() );
						}
					}
					else if ( waypoint.getSym().equals( "Ultralight Area"  ))
					{
						if ( getArgG().equals( "" ))
						{
							waypoint.setInitial( "D" );
							waypoint.setTwoLetterAbbreviation( "OA" );
						}
						else
						{
							waypoint.setInitial( "S" );
							waypoint.setTwoLetterAbbreviation( getArgG() );
						}
					}
					else if ( waypoint.getSym().equals( "Shipwreck"  ))
					{
						waypoint.setInitial( "S" );
						waypoint.setTwoLetterAbbreviation( "OT" );
					}
					else
					{
						/* ------------------------------------------------------------------------------
						 * Anything else
						 * ------------------------------------------------------------------------------ */
						waypoint.setInitial( "D" );
						waypoint.setTwoLetterAbbreviation( "OA" );
					}
				} // ! blue, red or green
			} // ! blue or red

			trail3laHashSet.add( waypoint.getInitial() + waypoint.getTwoLetterAbbreviation() );
			trail2laHashSet.add( waypoint.getTwoLetterAbbreviation() );
			
			boolean newSosCntrOk = incNewSosCntr();

			if ( newSosCntrOk )
			{
				waypoint.setWaypointNumber( getNewSosCntrString() );
				debugout( Log_Informational_2, "Resultant waypoint: " + waypoint.getInitial() + waypoint.getTwoLetterAbbreviation() + waypoint.getWaypointNumber() );
			}
			else
			{
				wayPointOk = false;
				debugout( Log_Informational_1, "Error calculating key for storing waypoint: " + getNewSosCntrInt() );
			}
		}
		catch( Exception ex )
		{
			wayPointOk = false;
			debugout( Log_Informational_1, "Exception calculating key for storing waypoint: " + waypoint.getInitial() + waypoint.getTwoLetterAbbreviation() + waypoint.getWaypointNumber() );
		}
		
		return wayPointOk;
	}
	
	
	String setBlueOrRed2la( Waypoint waypoint )
	{
		try
		{
			float waypointLat = Float.valueOf( waypoint.getLat() );
			float waypointLon = Float.valueOf( waypoint.getLon() );
/* -----------------------------------------------------------------------------------------------------------
 * Rules for deciding what new 2la new "SOS" waypoints should be labelled as:
 *
 * If lat is over 54.1854 is Cleveland Way; WU
 * Anything else lat over 53.8548 is around York; TO
 * Anything else lat over 53.6297 is around North Trent; WS
 *
 * South of that:
 * if lon is west of "-1.5234490"; OV
 * if lon is east of "-1.2227316"; OW
 *
 * If in the slot in the middle:
 * If lat over "53.2159106"; OU
 * If lat under "53.1075187"; OX
 *
 * Anything else is OS.
 * ---------------------------------------------------------------------------------------------------------*/
			if ( waypointLat > 54.1854 ) // north of
			{
				return "WU";
			}
			else if ( waypointLat > 53.8548 ) // north of
			{
				return "TO";
			}
			else if ( waypointLat > 53.6297 ) // north of
			{
				return "WS";
			}
			else if ( waypointLon < -1.5234490 ) // west of
			{
				return "OV";
			}
			else if ( waypointLon > -1.2227316 ) // east of
			{
				return "OW";
			}
			else if ( waypointLat > 53.2159106 ) // north of
			{
				return "OU";
			}
			else if ( waypointLat < 53.1075187 ) // south of
			{
				return "OX";
			}
			else
			{
				return "OS"; // everything else
			}
		}
		catch( Exception ex )
		{
			debugout( Log_Informational_1, "Exception calculating 2la for : " + waypoint.getLat() + waypoint.getLon() );
			return "ZZ"; // error
		}
	}
	
	
	private void debugout( int passed_debug_level, String passed_string ) 
	{
		
		if ( arg_debug >= passed_debug_level )
		{
			last_error = passed_string;
			System.out.println( passed_string );
		}
	}



} // Main class
