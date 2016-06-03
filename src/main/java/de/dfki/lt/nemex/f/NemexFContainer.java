package de.dfki.lt.nemex.f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.data.NemexFIndex;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

/**
 * <p>ngramInputText: ngram representation of input text; 
		<p>- can use type de.dfki.lt.nemex.a.ngram.CharacterNgram
		<p>- have to make sure that ngram list is position preserviong so have to use CharacterNgramWithDuplicates
	<p>ngramInputTextLength: 
		<p>- is defined by above

	<p>NemexFBean: binds all necessary parameters for performing search.

  	<p>invertedLists: a list of lists of Long or NIL
  		<p>- index follows ngramInputText
  		<p>- for each ngram in ngramInputText, it retrieves the inverted list, which is a list of ids of the entries that 
  			contain this ngram or it returns the empty list of ngram has no entries
  		<p>- the positions in ngramInputText and invertedLists are synchron, i.e., the i-th invertedList corresponds to the i-th ngram
  		<p>- in my Lisp implementation I copy the inverted lists before I am push them here! I guess I need that here as well


  	<p>position-list: actually a hash
  		a mapping between top elements of inverted-lists and their position in the ngram-list 
  		this means: when the inverted index of a ngram is determined, its first element is the top-element,
  		and its positions in ngramInputText are stored.
  		Map<Long, List<Integer>>
  		this actually indicates which ngrams of an entry of the dictionary are aligned in the input ngram list
  		NOTE: if the element is processed completely it will be removed from the corresponding list in invertedLists
  		and this is why we need to clone it

  	<p>min-heap
  		these top-elements are also stored in the min heap
  		thus the elements of our min heap are Long

  		<p>- find java library for Fibonacci min heap -> 
			I think import java.util.PriorityQueue; 
			should do it because it already implements a min-heap as default
			see also how it is used in JTIG


  	<p>toplevel-element
  		the popped element of the heap its type is Long

  	<p>count-array
  		the array that records an entity's occurrence number for each possible substring of the document
  		in my Lisp version I am using a hashmap from spans to integers which records the occurrence of an entity
  		e at all positions in ngram-list
  		thus the spans correspond to candidate substrings in the input that eventually are similar with e
  		Map<Integer, Map<Integer, Integer>> -> (left -> (right -> counts))

  	<p>valid-substring-interval
  		store the lower/upper bounds for current entity
  		has to be computed online with the used sim function
  		in NemexA two separate functions are defined, both 
  		de.dfki.lt.nemex.a.similarity.ApproximateStringSimilarityImpl.findMinSizeForFeatureSetY(double)
  		de.dfki.lt.nemex.a.similarity.ApproximateStringSimilarityImpl.findMaxSizeForFeatureSetY(double)
  		could also use two variables here:
  		int minSizeForFeatureSetY = findMinSizeForFeatureSetY(similarityThreshold);
		int maxSizeForFeatureSetY = findMaxSizeForFeatureSetY(similarityThreshold);

  	<p>lower-entity-length
  		T_l
  		has to be computed online with the used sim function
  		it is the same as minSizeForFeatureSetY -> CHECK !!!
 */
public class NemexFContainer {
	private static final Logger LOG = LoggerFactory.getLogger(NemexFContainer.class);
	// Variables

	// Make sure that ngramInputText ngrams are position preserving
	// Currently, this is only the case when 
	// this.ignoreDuplicateNgrams = false;
	private List<String> ngramInputText = new ArrayList<String>();
	private Integer ngramInputTextLength = ngramInputText.size();

	private NemexFBean nemexFBean ;

	// invertedLists and positionLists are synchronous in length and position index
	// Both together give us a kind of bidirectional connection of input ngram elements to 
	// list of entities and entities to their position in the ngram list of the input
	private List<List<Long>> invertedLists = null;
	private Map<Long, List<Integer>> positionList = null;

	// min heap
	private PriorityQueue<Long> minHeap = new PriorityQueue<Long>();
	private Long toplevelElement = null;

	// count-array -> I implement it as a multi-layer hashMap
	private Map<Integer, Map<Integer, Integer>> countSpanHash = new HashMap<Integer, Map<Integer, Integer>>();

	// valid-substring-interval for current entity
	private int minSizeForFeatureSetY = 0;
	private int maxSizeForFeatureSetY = 0;

	// lower-entity-length
	private int lowerEntityLength = minSizeForFeatureSetY;
	private int entityNgramLength = 0;;
	public int itemCounter = 0;

	// Getters and Setters

	public int getEntityNgramLength() {
		return entityNgramLength;
	}

	public void setEntityNgramLength(int entityNgramLength) {
		this.entityNgramLength = entityNgramLength;
	}

	public List<String> getNgramInputText() {
		return ngramInputText;
	}

	public void setNgramInputText(List<String> ngramInputText) {
		this.ngramInputText = ngramInputText;
	}

	public Integer getNgramInputTextLength() {
		return ngramInputTextLength;
	}

	public void setNgramInputTextLength(Integer ngramInputTextLength) {
		this.ngramInputTextLength = ngramInputTextLength;
	}

	public List<List<Long>> getInvertedLists() {
		return invertedLists;
	}

	public void setInvertedLists(List<List<Long>> invertedLists) {
		this.invertedLists = invertedLists;
	}

	public Map<Long, List<Integer>> getPositionList() {
		return positionList;
	}

	public void setPositionList(Map<Long, List<Integer>> positionList) {
		this.positionList = positionList;
	}

	public PriorityQueue<Long> getMinHeap() {
		return minHeap;
	}

	public void setMinHeap(PriorityQueue<Long> minHeap) {
		this.minHeap = minHeap;
	}

	public Long getToplevelElement() {
		return toplevelElement;
	}

	public void setToplevelElement(Long toplevelElement) {
		this.toplevelElement = toplevelElement;
	}

	public Map<Integer, Map<Integer, Integer>> getCountSpanHash() {
		return countSpanHash;
	}

	public void setCountSpanHash(Map<Integer, Map<Integer, Integer>> countSpanHash) {
		this.countSpanHash = countSpanHash;
	}

	public int getMinSizeForFeatureSetY() {
		return minSizeForFeatureSetY;
	}

	public void setMinSizeForFeatureSetY(int minSizeForFeatureSetY) {
		this.minSizeForFeatureSetY = minSizeForFeatureSetY;
	}

	public int getMaxSizeForFeatureSetY() {
		return maxSizeForFeatureSetY;
	}

	public void setMaxSizeForFeatureSetY(int maxSizeForFeatureSetY) {
		this.maxSizeForFeatureSetY = maxSizeForFeatureSetY;
	}

	public int getLowerEntityLength() {
		return lowerEntityLength;
	}

	public void setLowerEntityLength(int lowerEntityLength) {
		this.lowerEntityLength = lowerEntityLength;
	}


	public NemexFBean getNemexFBean() {
		return nemexFBean;
	}

	public void setNemexFBean(NemexFBean nemexFBean) {
		this.nemexFBean = nemexFBean;
	}

	// Class initialization


	public NemexFContainer(List<String> ngramInputText){
		this.ngramInputText = ngramInputText;
		this.ngramInputTextLength = ngramInputText.size();
		this.invertedLists = new ArrayList<List<Long>>();
		this.positionList = new HashMap<Long, List<Integer>>();
		this.minHeap = new PriorityQueue<Long>();
		this.countSpanHash = new HashMap<Integer, Map<Integer, Integer>>();
	}

	public NemexFContainer(List<String> ngramInputText, NemexFBean nemexFBean)
	{
		this.ngramInputText = ngramInputText;
		this.ngramInputTextLength = ngramInputText.size();

		this.nemexFBean = nemexFBean;
		this.invertedLists = new ArrayList<List<Long>>();
		this.positionList = new HashMap<Long, List<Integer>>();
		this.minHeap = new PriorityQueue<Long>();
		this.countSpanHash = new HashMap<Integer, Map<Integer, Integer>>();
	}

	// Methods


	// **************** Initialization of NemexFContainer

	public void initializeNemexFContainer(){
		// iterate through all ngrams of input
		LOG.info("Ngrams: " + this.ngramInputText.toString());
		for (int i = 0; i < this.ngramInputTextLength; i++){
			LOG.info("Next ngram: " + i + " " + this.ngramInputText.get(i));
			// get copy of inverted index for i-th ngram
			List<Long> invertedIndexList = NemexFIndex.getInvertedIndexAndCopy(
					this.getNemexFBean().getGazetteerFilePath(), this.ngramInputText.get(i));
			LOG.info("Inverted list: " + invertedIndexList.toString());

			if (!invertedIndexList.isEmpty()){
				Long topElement = invertedIndexList.get(0);
				// determine position of ngram in the tokenized document
				// NOTE: I start from 0 in document (faerie starts from 1) -> TAKE CARE OF THIS!!!
				this.createOrUpdatePositionList(topElement, i);
				LOG.info("Init Position list: top: " + topElement + " ngram: " + i + " " + this.positionList.toString());
			}
			// save copied list for ngram found at i-th position in i-th position of ngram-input (or empty list)
			// I assume that new position lists are added to the end of invertedList
			// the order of inverted list according to order in input
			this.invertedLists.add(invertedIndexList);
		}	
		this.itemCounter = 0;
	}

	/**
	 * <p>Returns: for each entity, its position in the input list at TOP position
	 * <p>Method:
	 * <p>collect all positions of ngram in input document
	 * <p>if top-element exists then 
	 * <p>extend its position list or
	 * <p>create position list and add element to heap
	 * <p>initially, the order is in decreasing position
	 */
	private void createOrUpdatePositionList(Long topElement, int i) {
		if (this.positionList.containsKey(topElement)) {
			// make sure that position list is in correct increasing order
			this.positionList.get(topElement).add(i);
		}
		else
		{
			List<Integer> newPositionList = new ArrayList<Integer>();
			newPositionList.add(i);
			this.positionList.put(topElement, newPositionList);
			// only if position list is new a heap element has to be created
			if (!this.getMinHeap().contains(topElement))
				this.minHeap.add(topElement);
		}
	}

	public void initializeEntityBoundaries (int entityNgramLength, SimilarityMeasureInterface simFct, double simThreshold){
		this.setMinSizeForFeatureSetY(simFct.findMinSize(entityNgramLength, simThreshold));
		this.setMaxSizeForFeatureSetY(simFct.findMaxSize(entityNgramLength, simThreshold));

		//Have to initialize the countHash here as well
		this.setCountSpanHash(new HashMap<Integer, Map<Integer, Integer>>());
	}

	public void initializeLowerBoundOnEntityLength (int entityNgramLength, SimilarityMeasureInterface simFct, double simThreshold){
		this.setLowerEntityLength(simFct.findLowerBoundOfEntity(entityNgramLength, simThreshold));
	}

	// **************** For enumerating candidate matching substrings and entities
	/**
	 * <p>for each e's ngram and position in the complete unit count occurrence of entity in all valid windows (or substrings) 
	 * constrained by the length restrictions
	 */

	/**
	 * Computes the upper bound for entity e. For ED and EDS we use the value of @NemexFContainer.getMaxSizeForFeatureSetY
	 * and for DICE, JACCARD and COSINE we define tighter bounds using the current length of the position list s of e
	 * because they depend on |e intersect s|.
	 * @param positionList: is assumed to be a valid window, i.e., T_l <= P_e[pi, ..., pj] <= T_e
	 * @param simFctStringName
	 * @return
	 */
	public int computeUpperBoundWindowSize (List<Integer> positionList){
		if (this.getNemexFBean().getSimilarityMeasure() == SimilarityMeasure.COSINE_SIMILARITY_MEASURE ||
				this.getNemexFBean().getSimilarityMeasure() == SimilarityMeasure.DICE_SIMILARITY_MEASURE ||
				this.getNemexFBean().getSimilarityMeasure() == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE){
			return 
					this.getNemexFBean().getSimFct().tighterUpperWindowSize(this.entityNgramLength, 
							positionList.size(), this.getNemexFBean().getSimilarityThreshold());	
		}
		else 
			if (this.getNemexFBean().getSimilarityMeasure() == SimilarityMeasure.ED_SIMILARITY_MEASURE ||
			this.getNemexFBean().getSimilarityMeasure() == SimilarityMeasure.EDS_SIMILARITY_MEASURE)
				return 
						this.getMaxSizeForFeatureSetY();
			else 
			{System.err.println("Still to implement :- " + this.getNemexFBean().getSimilarityMeasure());
			System.exit(0);
			return 
					-1;
			}
	}

	/**
	 * Given a candidate window count occurrence of entity e for all elements between tau_e <= positionList <= T_e 
	 * (actually simFct dependent tighter bound used here)
	 * @param positionList
	 */
	public void enumerateCandidatePairsMain(List<Integer> positionList){
		enumerateCandidatePairs(positionList, 
				getMinSizeForFeatureSetY(), computeUpperBoundWindowSize(positionList));
	}

	public void enumerateCandidatePairs (List<Integer> positionList, int lower, int upper) {

		for (int i = 0; i < positionList.size(); i++){
			this.countOccurrenceOfentityAtithPosition(positionList.get(i),lower,upper);
		}
	}

	/**
	 * <p>for i-th element in position list, for relevant substrings increase counter in count-array
	 * <p>a substring is a string that ends at ith position, and starts from a left position which is computed
	 * <p>using the length restrictions of entity e
	 * <p>#|
	 	<p>NOTE: l refers to LENGTH of substring in terms of NGRAMS
		<p>Only D[i-l+1,l], ..., D[i,l], with TAU_entity <= l <= T_entity can contain i-th inverted list;
		<p>Do iteration for l taking care of proper setting of i when computing 
		<p>(D-index starts at 1 -> NO, I AM STARTING FROM 0, V-index starts at 0)
		<p>V[i-l+1][l], ..., V[i][l]
		<p>also note: second dimension actually is an index for l, i.e., V[0][0] means: 1-st element with length min-val
		<p>
		<p>Steps: 
		<p>0. ith-position is right boundary for substrings
		<p>1. get lower (min-val) and upper (max-val) bound for entity e
		<p>2. for all distances l:
		<p>3. compute start position start-from of substring according to i-l+1,l
			<p>(i-l+1 for same reason as for determining second dimension of count array)
			<p>(if start position is less then 0, set it to 0)
		<p>4. loop for start-from to ith-position, and increase counter in V[i,l]
		<p>NOTE: second dimension of array corresponds to index of different distances, hence
		<p>have to compute by normalizing it with l with min-val
		<p>|#
	 */
	private void countOccurrenceOfentityAtithPosition(int entityPositionIndex, int min, int max) {
		// Only TAU_entity <= l <= T_entity can contain i-th inverted list

		for (int l = min; l <= max; l++) {
			// jump l steps back from i-position -> to take into account sim-threshold
			// Basically it means that the current position is the center of 
			// the current substring (window) whose size is between -l and l, 
			int startIndex = entityPositionIndex - l + 1;
			// if start-index points outside dimension then set it to 0
			int startFrom = (startIndex > 0) ? startIndex : 0;

			// D[i-l+1,l], ..., D[i,l]
			for (int i = startFrom; i <= entityPositionIndex; i++){
				// if substring D[i,l] has less then l tokens it is not valid
				// which happens if ith position is too close to the end of the document and l is too large
				// so that right boundary of document would be broken
				// System.out.println("..... Span i:" + i + " l:" + l);
				if ((i + l) > this.getNgramInputTextLength()){ 
					// System.out.println ("....... Substring in input has less than l tokens !");
					break;
				}
				// (i, l) is a valid input span for entity so increase its counter
				incrementCountSpanHash(i, l);
				this.itemCounter++;
			}
		}
	}

	/**
	 * To count occurrence of an entity.
	 * I use two linked hash tables for left and len, instead of a vector
	 * of length |D|+l-1 as described in paper, page 533, section complexity.
	 * hashmap: left -> len -> count
	 * @param left
	 * @param len
	 */
	private void incrementCountSpanHash(int left, int len) {
		if (this.getCountSpanHash().containsKey(left)){
			// if left span exists, check whether len exists
			if (this.getCountSpanHash().get(left).containsKey(len)){
				// if so, get its value, and increment it
				int count = this.getCountSpanHash().get(left).get(len);
				this.getCountSpanHash().get(left).put(len, ++count);	
			}
			else{
				// create new len for known left span
				this.getCountSpanHash().get(left).put(len, 1);
			}
		}
		else
		{ // if left span does not exist then also len does not exist, so
			// create hash for len (with initial counter), and add left span
			Map<Integer, Integer> rightSpan = new HashMap<Integer, Integer>();
			rightSpan.put(len, 1);
			this.getCountSpanHash().put(left, rightSpan);
		}
	}

	// **************** Check and collect matching substrings for a single entity

	/**
	 * <p>Read-off candidates for entity
	 * <p>-> The pair <e,D[i,l]> (entity e and document substring D[i=start,l=length]) is a candidate pair, if 
   			<p>its occurrence number V[i,l] >= T, 
   			<p>where T is min-overlap(length(e), length(D[i,l]), sim-fct, sim-threshold)
   				NOTE: need to compute length of e here !
		<p>-> how and where to call this function efficiently ?
		<p>-> I will do it after count-array has been created for entity, because otherwise every time a counter
   			<p>is increased I would have to check it
		<p>THIS ALSO CHANGED TO MAKE USE OF HASHTABLE: BENEFIT NOW: ONLY SPANS WHICH ARE SEEN AT LEAST ONE TIME ARE ACTUALLY 
		<p>VISITED NOT THE COMPLETE 2-DIM ARRAY AS BEFORE
	 */

	/*
	 * Found candidates are stored in a hash-based index:
	 * all entities are indexed via left position in document and length of window
	 * it seems that accessing via left->len-> always gives only a single entity
	 */
	//TODO is this the verification step?
	public Map<Integer, Map<Integer, Long>> getCandidatePairs (Long entityIndex, int entityNgramLength, 
			SimilarityMeasureInterface simFct, double simThreshold){
		//Single place where I can use TreeMap or HashMap
		Map<Integer, Map<Integer, Long>> foundCandidates = new HashMap<Integer, Map<Integer, Long>>();

		for (int left : this.getCountSpanHash().keySet()){
			for (int len : this.getCountSpanHash().get(left).keySet()){
				// counter should be no smaller than the min-overlap
				// in Lisp version I use >, here I use >= !!
				// TODO
				// this is the place where I need to access the ngram length of an entity
				if (this.getCountSpanHash().get(left).get(len)
						>=
						simFct.findTauMinOverlap(entityNgramLength, len, simThreshold)
						){
					// if so, push candidate and entity
					upgradeFoundCandidates(left, len, entityIndex, foundCandidates);
				}
			}
		}
		return foundCandidates;
	}

	// foundCandidates just keeps an inverted index from: LEFT -> LEN -> E
	// implemented as Map<Integer, Map<Integer, Long>>

	private void upgradeFoundCandidates(int left, int len, Long entityIndex,
			Map<Integer, Map<Integer, Long>> foundCandidates) {
		// System.err.println("s:"+left+":l:"+len+":e:"+entityIndex);
		if (foundCandidates.containsKey(left)){
			// add new len for known left span of known entity
			foundCandidates.get(left).put(len, entityIndex);
		}
		else
		{ // Else create new span-entitiyList entry
			Map<Integer, Long> lengthMap = new HashMap<Integer, Long>();
			lengthMap.put(len, entityIndex);
			foundCandidates.put(left, lengthMap);
		}
	}

	// **************** Adjust NemexFContainer

	/**
	 * <p>Adjust NemexFContainer:

 		<p>- pop top element instances from inverted lists  
 		<p>- select top elements from these inverted lists
   			insert them into position list using positions of old top element; 
  			check if already exists, then update -> order not important here -> not sure: must be sorted !
 		<p>- insert them into the heap
		<p>- remove old top element from position list
 		<p>- pop top element from heap
   			and assign it as new toplevel-element

	 */

	/*
	 * NOTE on sorting
		now make sure position list is sorted -> sort it !
		or add element via merging

		I HATE IT, but it seemed to be necessary in order for the pruning stuff to work !
		can i avoid it ???
		-> at least delay it until the pruner is called who needs a sorted list
	 */

	public void adjustNemexFContainer(Long topEntityIndex) throws Exception{
		if (this.positionList.get(topEntityIndex) == null){
			Exception myException = 
					new ArithmeticException("Adjustment of topelement's position list is NULL!: " + topEntityIndex);
			throw myException;
		}
		else
			for (int i = 0; i < this.positionList.get(topEntityIndex).size(); i++){
				int pos = this.positionList.get(topEntityIndex).get(i);
				// Get inverted list at position pos, and pop its first element
				// which is topEntityIndex
				// System.out.println("..... Pos: " + pos + " => Inverted list: " + this.invertedLists.get(pos));
				if (!this.invertedLists.get(pos).isEmpty()) 
					this.invertedLists.get(pos).remove(0);
				// If this inverted list still has elements
				if (!this.invertedLists.get(pos).isEmpty()){
					// Get its new top element
					Long newTopEntityIndex = this.invertedLists.get(pos).get(0);
					// System.out.println("Old top: " + topEntityIndex + " New top:  " + newTopEntityIndex);
					List<Integer> posList = this.positionList.get(newTopEntityIndex);
					if (posList == null){
						// This case means that we have a ngram pointing to an entity which did not occur
						// in top position by some other ngram in the input, but now it is in top position
						List<Integer> posNewList = new ArrayList<Integer>();
						posNewList.add(pos);
						this.positionList.put(newTopEntityIndex, posNewList);
					}
					else
					{	// NOTE: it is not guaranteed that position list is automatically ordered after
						// adding a new element; see comment above
						posList.add(pos);
					}
					// Add new entityIndex as top element to minHeap
					if (!this.getMinHeap().contains(newTopEntityIndex)) 
						this.getMinHeap().add(newTopEntityIndex);
				}
			}

		//		if (this.itemCounter > 0)
		//			System.out.println("No of matches for entity: " +  topEntityIndex + " --> " + this.itemCounter);
		//		this.itemCounter = 0;
		// TODO check this  comment ! It is from old Lisp version, but valid here as well ?
		// remove old top-element and its position list from the hash array
		// should be ok because top-element will never occur in rest of inverted lists because
		// inverted lists are assumed to be sorted in increasing order and are also visited via the min heap
		// I think causes errors because position lists are not sorted YET

		// NOTE: this seems to cause an error at least for the :no case !
		// this.positionList.remove(topEntityIndex);
		// Does this make sense ?
		countSpanHash = new HashMap<Integer, Map<Integer, Integer>>();
	}

	// **************** Printing methods

	// Helpers

	public void printInvertedListsForInput (){
		System.out.println("Curent value of InvertedLists:");
		int cnt = 0;
		for (List<Long> list : this.invertedLists){
			System.out.println(this.getNgramInputText().get(cnt) + "[" + cnt + "]: " + list);
			cnt++;
		}
	}

	public void printPositionListsForInput (){
		System.out.println("Curent value of positionLists:");
		for (Long key : this.positionList.keySet()){
			System.out.println(key + ": " + this.positionList.get(key));
		}
	}

	public void printCountHash (){
		for (Integer left : this.getCountSpanHash().keySet()){
			for (Integer len : this.getCountSpanHash().get(left).keySet()){
				System.out.print(
						"[" + left + "," + len + "]: " 
								+ this.getCountSpanHash().get(left).get(len)
								+ "; "
						);
			}
			System.out.println("");
		}
	}

}
