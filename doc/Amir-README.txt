Amir message from 26.02.2014:

As promised, I finished the development of the requested features for
NEMEX before the end of this month. I have just committed the latest
version of NEMEX to the following SVN repository of mine, where you
could check it out using the user name "gneumann" and password
"gneumann2013":

https://svn.moinware.org/svn/nemex/

If you follow the brief instructions at
"https://svn.moinware.org/svn/nemex/NEMEX/doc/UserManual.txt", you
could easily build it using Maven, i.e. to generate the complete
distribution including all the dependencies in one ZIP file in one
step. Then, you could use it as a library, as a J2EE web application,
as a standalone Java (J2SE) application or via XML-RPC.

Concerning the requested new features, please find a brief explanation
of the performed tasks below. Should you need more information, please
always feel free to ask me:

1. One project instead of two projects:

As you see it is now exactly one project, in which you could generate
both the WAR file for the J2EE web application as well as the complete
distribution (ZIP) and Nemex.jar library in one step using the Maven
command "mvn assembly:assembly", as explained in the short manual.

2. Internal data structure for the complete dictionary entries, i.e.
all parts, and the possibility of exporting this internal data
structure to a file on the disk and importing (loading) it later:

Please check out
"https://svn.moinware.org/svn/nemex/NEMEX/src/main/java/de/dfki/lt/nemex/a/NEMEX_A.java",
where the two methods "exportGazetteer" and "importAndLoadGazetteer"
provide the APIs for this purpose.

3. Let NEMEX start up even with empty dictionary and allow addition of
new dictionary entries during the runtime:

As you see in the following dictionary is empty and NEMEX could work fine:

https://svn.moinware.org/svn/nemex/NEMEX/src/main/webapp/resources/empty.txt

Please check out
"https://svn.moinware.org/svn/nemex/NEMEX/src/main/java/de/dfki/lt/nemex/a/data/Gazetteer.java",
where the method "addNewEntry" provides the API for adding new entries
to create the dictionary incrementally.

Please give it a try. If you need any more information or have any
problems with it please feel free to contact me.