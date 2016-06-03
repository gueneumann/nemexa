package de.dfki.lt.nemex.f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.a.ngram.CharacterNgram;
import de.dfki.lt.nemex.a.ngram.CharacterNgramWithDuplicate;
import de.dfki.lt.nemex.f.data.Candidates;
import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.data.NemexFIndex;
/*
 * This will be basically the main function called with all relevant parameters from  Main_NemexF.java
 * it will initialize the NemexFContainter and will call the pruning strategies
 */
public class NemexFController {
	private static final Logger LOG = LoggerFactory.getLogger(NemexFController.class);
	// Variables
	private CharacterNgram characterNgramFromQueryString = null;
	private NemexFBean nemexFBean;

	private NemexFContainer nemexFContainer = null;

	// found candidates are basically tuple of <span, entities>
	// where span is a substring in the input string (its left position in the query
	// plus its ngram length, D=<left,len>).
	// and entities a list of ID of matching entities for the substring
	// DONE: I think always a single entity only ! but have to check ! SEEMS TO BE TRUE
	private List<Map<Integer, Map<Integer, Long>>> allFoundCandidates = 
			new ArrayList<Map<Integer, Map<Integer, Long>>>();

	//List of found and selected candidates
	private Candidates candidates ;

	// Getters and Setters

	public Candidates getCandidates() {
		return candidates;
	}

	public void setCandidates(Candidates candidates) {
		this.candidates = candidates;
	}

	public CharacterNgram getCharacterNgramFromQueryString() {
		return characterNgramFromQueryString;
	}

	public void setCharacterNgramFromQueryString(String queryString){
		if (!this.getNemexFBean().isIgnoreDuplicateNgrams()) {
			characterNgramFromQueryString = new CharacterNgramWithDuplicate(queryString,
					this.getNemexFBean().getnGramSize());
		} else {
			LOG.error("Currently, only CharacterNgramWithDuplicate class preserves ngram ordering, "
					+ "so only this is currently supported in NemexF!");
			LOG.error("Thus make sure that 'this.getNemexFBean().ignoreDuplicateNgrams = false;'");
			System.exit(0);

		}
	}

	public NemexFContainer getNemexFContainer() {
		return nemexFContainer;
	}

	public void setNemexFContainer(NemexFContainer container) {
		this.nemexFContainer = container;
	}

	public List<Map<Integer, Map<Integer, Long>>> getAllFoundCandidates() {
		return allFoundCandidates;
	}

	public void setAllFoundCandidates(List<Map<Integer, Map<Integer, Long>>> allFoundCandidates) {
		this.allFoundCandidates = allFoundCandidates;
	}

	public NemexFBean getNemexFBean() {
		return nemexFBean;
	}

	public void setNemexFBean(NemexFBean nemexFBean) {
		this.nemexFBean = nemexFBean;
	}


	// Class initialization

	public NemexFController(NemexFBean nemexFBean){
		this.nemexFBean = nemexFBean;

		NemexFIndex.loadNewGazetteer(
				this.getNemexFBean().getGazetteerFilePath(), 
				this.getNemexFBean().getDelimiter(), 
				this.getNemexFBean().isDelimiterSwitchOff(), 
				this.getNemexFBean().getnGramSize(), 
				this.getNemexFBean().isIgnoreDuplicateNgrams());
	}

	// methods

	public List<String> getNgramListfromNgramClass (){
		return ((CharacterNgramWithDuplicate) this.getCharacterNgramFromQueryString()).getNgrams();
	}

	/**
	 * <p>Sets up important paramemeters for the NemexFController to start with.
	 * <p>- creates ngram list for query input string
	 * <p>- initializes result data structure
	 * <p>- sets the similarity function and threshold
	 * <p>- initializes the NemexFContainer object

	 */
	public void reset(){
		// Reset result object
		this.setAllFoundCandidates(new ArrayList<Map<Integer, Map<Integer, Long>>>());
		this.setCandidates(new Candidates(this.getNemexFBean().getGazetteerFilePath()));
		
		this.getCandidates().setCnt(0);

		// Create NemexFContainer and provide ngram as list
		this.setNemexFContainer(
				new NemexFContainer(this.getNgramListfromNgramClass(),this.getNemexFBean()));


		// Initialize NemexFContainer with single heap strategy which means that
		// the whole input string is used to create the heap.
		this.getNemexFContainer().initializeNemexFContainer();
		LOG.info("Input string:\n" + this.getNemexFBean().getQueryString());

		LOG.info("Input ngrams (len="  
				+ this.getNemexFContainer().getNgramInputTextLength()
				+ "):\n"
				+ this.getNemexFContainer().getNgramInputText());
		LOG.info("Initial Heap: " + this.getNemexFContainer().getMinHeap().toString());
		LOG.info("Aligner:      " + this.getNemexFBean().getAligner().toString());
		LOG.info("Selector:      " + this.getNemexFBean().getSelector().toString());

	}

	private void processHeap() throws Exception{
		// Loop until heap is empty
		while (!this.getNemexFContainer().getMinHeap().isEmpty()){
			// First, retrieve and remove top element of the heap
			this.getNemexFContainer().setToplevelElement(
					this.getNemexFContainer().getMinHeap().poll());

			// Set local variables
			Long entityIndex = this.getNemexFContainer().getToplevelElement();
			// I have to get the entity in order to compute the ngram length
			List<String> entity = NemexFIndex.getEntry(this.getNemexFBean().getGazetteerFilePath(), entityIndex+1);

			// TODO This is why I need to retrieve the dictionary string! 
			// To improve memory footprint, pre-compute it when loading the dictionary and store it as last element in 
			// dictionary list -> then I can try more efficient dictionary entry, e.g., only ngram length ...
			int entityNgramLength = (entity.get(1).length() - this.getNemexFBean().getnGramSize() + 1);
			// store ngram-length of entity in container
			this.getNemexFContainer().setEntityNgramLength(entityNgramLength);

			// for storing substrings matching for entity
			Map<Integer, Map<Integer, Long>> candidatePairs = null;

			/* Initialize lower bound of entity
			 * These bounds are used to determine the length of a candidate window for an entity
			 * e, and as such, a candidate matching substring. This is basically done in the next step ... 
			 */
			this.getNemexFContainer().initializeLowerBoundOnEntityLength(
					entityNgramLength, this.getNemexFBean().getSimFct(), this.getNemexFBean().getSimilarityThreshold());

			//						LOG.info("Enter new iteration!");
			//						LOG.info("Popped Top element: " + entityIndex);
			//						LOG.info("Remaining Heap: " + this.getNemexFContainer().getMinHeap().toString());
			//						LOG.info("Entity: " + entity);
			//						LOG.info("Entity ngram len: " + entityNgramLength);
			//						LOG.info("Min size: " + this.getNemexFContainer().getMinSizeForFeatureSetY());
			//						LOG.info("Max size: " + this.getNemexFContainer().getMaxSizeForFeatureSetY());

			//			this.getNemexFContainer().printInvertedListsForInput();
			//			this.getNemexFContainer().printPositionListsForInput();

			// Call the aligner specific method for finding and extracting candidate pairs of substrings and entities

			/* ... namely here, where the bounds are used to determine the length of a candidate window for an entity
			 * e, and as such, a candidate matching substring. This is basically done later 
			 * by using an element of the position list of entity e (the top element of the heap)
			 * as center of such a window, and then increasing the counter of e in the count-array.
			 * Later, we can check for valid windows how often entity e occurs and can decide to prune it or to keep it.
			 */
			candidatePairs = 
					this.getNemexFBean().getAligner().findSubstringsForEntity(
							entityIndex, entityNgramLength, 
							this.getNemexFBean().getSimFct(), 
							this.getNemexFBean().getSimilarityThreshold(),
							this.getNemexFContainer() );

			// this.getNemexFContainer().printCountHash();

			if (! (candidatePairs == null)){
				if (!candidatePairs.isEmpty()){
					allFoundCandidates.add(candidatePairs);
				}
			}
			// Process the next entity if there is any.
			this.getNemexFContainer().adjustNemexFContainer(entityIndex);
		}
	}

	// Main method
	public void process() {

		LOG.info("Looping heap ... ");

		long time1 = System.currentTimeMillis();

		try {
			this.processHeap();
		} catch (Exception e) {
			e.printStackTrace();
		}

		long time2 = System.currentTimeMillis();
		LOG.info("System time (msec): " + (time2-time1));
		LOG.info("Number of Identified different entities: " + this.getAllFoundCandidates().size());
	}

	public void selectCandidates(){
		LOG.info("Select candidates mentions ... ");

		long time1 = System.currentTimeMillis();
		for (Map<Integer, Map<Integer, Long>> foundCandidates : this.getAllFoundCandidates()){
			long entityIndex = getEntityIndex(foundCandidates)+1;
			this.getCandidates().addCandidates(entityIndex, 
					this.getNemexFBean().getSelector().BucketCandidates(entityIndex, foundCandidates));
		}

		long time2 = System.currentTimeMillis();
		LOG.info("System time (msec): " + (time2-time1));
	}

	// Printing

	public int countAllFoundCandidates (){
		int cnt = 0;
		for  (int i=0; i < this.getAllFoundCandidates().size(); i++){
			for (Integer val : this.getAllFoundCandidates().get(i).keySet()){
				cnt = cnt + this.getAllFoundCandidates().get(i).get(val).size();
			}
		}
		return cnt;
	}

	private long getEntityIndex(Map<Integer, Map<Integer, Long>> foundCandidates) {
		int startElement = (int) foundCandidates.keySet().toArray()[0];
		List<Integer> lengthListofStartElement = 
				new ArrayList<Integer>(foundCandidates.get(startElement).keySet());
		long entityIndex = foundCandidates.get(startElement).get(lengthListofStartElement.get(0));
		return entityIndex;
	}

	public void printSelectedCandidates(){
		LOG.info("All items (number of all ngram matches in all candidate windows of all entities with >= min occurrence): " +  this.getNemexFContainer().itemCounter);
		LOG.info("All pairs (candidate windows with <left-pos, distance>): " +  this.countAllFoundCandidates());
		LOG.info("Selected found NE mentions:  " + this.getCandidates().getCnt());

		System.out.println(this.getCandidates().toString());

	}
}
