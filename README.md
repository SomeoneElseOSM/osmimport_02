osmimport_02
============
This processes a GPX file from a Garmin GPS, and generates unique track files from it, each containing all of
the new waypoints in the GPX.

The waypoint handling allows an extract of "places to go" to be sent to the GPS beforehand, with places visited
removed from the list on the GPS (by renaming the first letter of the place from "S" to "D").

Waypoints renumbered by a previous run of this routine will have a name such as XYYnnnnnn where "X" is an initial letter, "YY" is a "2-letter abbreviation" for a series of waypoints (such as a long distrance trail) and "nnnnnn" is a unique number.

Waypoints with an intiial "S" or "V" are assumed to be existing "places to visit" and aren't added to the produced track files.

Waypoints named "D" or "E" are assumed to be "ticked off" places - they're included, and a GPX file on disk starting "base_data" is updated with their details.

Waypoints named something else are assigned a new number depending on their type - green ones (which I use for "places where paths meet that don't need a future survey") are numbers "DTRnnnnnn"; red and blue ones ("places where paths meet that do need a future survey") Syynnnnnn, where "yy" is geographically derived from hardcoded internal values; most others "DOAnnnnnn".  The exception to the "DOA" rule is where "-t=ZZ" is passed on the command line - in this case a file on disk SZZ_yyyymmdd_hhmmss.gpx is created or updated (where yyyymmdd_hhmmss is the current date and time).

In each case the number "nnnnnn" is read from a file.

New Garmin waypoints will tend to be a 3-digit number such as "123".  It's also possible to pass a description file containing lines such as:

123 More information than can be entered in a Garmin GPS comment

This extra information is appended to the waypoint in the resulting track files ready for upload to OSM.

Example command line:

java -jar c:\Utils\osmimport_02.jar -c=c:\Temp\osm\cntr.txt -d=c:\temp\osm\20140525a.eml -k=c:\temp\osm\ -i=c:\temp\osm\20140525b.gpx -t=RH -b=5

This means use "c:\Temp\osm\cntr.txt" as the counter file, use "c:\temp\osm\20140525a.eml" as the description file containing comments and "c:\temp\osm\20140525b.gpx" as the input GPX containing Waypoints, Routes and Tracks.  "-t=RH" means that a file SRH_yyyymmdd_hhmmss.gpx will be created (using the current data and time) incorporating any new "Trail" points ("-t" is interpreted as "Trail") and also the contents of the most recent existing SRH_yyyymmdd_hhmmss.gpx file.  "-b=5" is just the level of debug output produced (the higher the number, the more verbose).

