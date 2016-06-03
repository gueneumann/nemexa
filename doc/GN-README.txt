GN, March, 2014 - my comments on new version of NEMEX-A

************** Running

- running as web service:
	- copy target/nemex.war file to /Applications/apache-tomcat-7.0.52/webapps
	- start tomcat using /Applications/apache-tomcat-7.0.52/bin/start.sh
	- in browser: http://127.0.0.1:8080/nemex/
	- stop tomcat using /Applications/apache-tomcat-7.0.52/bin/shutdown.sh

- running from Eclipse and java application:
	de.dfki.lt.nemex.Main_NemexA.main(String[])
- configuration file: 
	src/main/webapp/resources/configurations.xml
	/NEMEX/src/main/java/de/dfki/lt/nemex/a/data/Gazetteer.java

APRIL, 2014:

NEMEXA is now at GitLab


JULY, 2014:

allow also tokens as index: 
- generalize ngram and words to token.
- support tokenization as a means of computing ngrams

allow also partition of string into n-substrings

currently, NEMEXA only supports set-based similarity functions
extend it to support character-based similarity functions as well 
-> see Faeri

AUGUST, 2014:

IMPORTANT: the difference with CharacterNgramWithDuplicate and CharacterNgramUnique

CharacterNgramWithDuplicate returns a list of ngrams which is position preserving wrt. input string
CharacterNgramUnique returns a hashmap which is not position preserving.

For Faeri I will use CharacterNgramWithDuplicate