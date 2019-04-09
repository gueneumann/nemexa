/**
 * Created on 9th April 2019
 * by gune00 
 * LT Lab.
 * German Research Center for Artificial Intelligence
 * (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 * http://www.dfki.de
 * Saarbruecken, Saarland, Germany
 */

package de.dfki.lt.nemex.f.verify;


/** 
 *<p>This class implement the dynamic programming algorithm.
 * It computes the pairwise alignment using a flexible cost function.
 * The implementation is based on Giegerich & Wheller (1996).
 * Palign.dp() computes the minimal alignment of two strings by computing
 * the edit distance of all prefixes of two strings s1 and s2.
 * The resulting value is in matrix-element (|s1|, |s2|).
 * It uses the dynamic programming DP technique.</p>
 * 
 * <p>The DP algorithm has important geometric properties: 
 * 1) adjacent entries in horizontal and vertical direction only differ by 0 or 1, and 
 * 2) Forward diagonals are non-decreasing and adjacent entries differ by 0 and 1.</p>
 * 
 * <p> I will also adapt the Ukonnen strategy of early pruning and a trie-based method for processing
 * sets of strings simultaneously.</p>
 * 
 * 
 * @see http://www.biosino.org/mirror/www.techfak.uni-bielefeld.de/bcd/Curric/PrwAli/
 * @see /Users/gune00/dfki/E-Books/SequenceAnalysis/prwali.pdf
 * 
 * 
 */

// The current version seems to be a bot slower than the SBCL version 
// -> check whether I used all proper java stuff and object style

public class Palign {
	// private means only visible in this class
	 private String s1;

	 private String s2;

	// protected means visible for subclasses; do not know whether it makes sense :-)
	protected int[][] distanceMatrix ;

	private int distance ;
	/**
	 * The following parameters store the weights for the individual edit operations.
	 * Using 1 as default implements the unit cost (also known as Levenshtein Distance)
	 */
	private int insertionWeight = 1;
	private int deletionWeight = 1;
	private int replacementWeight = 1;

	// Set and get functions for weights of the operations
	
	public String getSourceString () {
		return s1;
	}
	
	public String getTargetString () {
		return s2;
	}
	
	public void setSourceString (String source) {
		s1 = source;
	}
	
	public void setTargetString (String target) {
		s2 = target;
	}
	
	public void setInsertionWeight (int value) {
		insertionWeight = value;
	}

	public int getInsertionWeight () {
		return insertionWeight;
	}

	public void setDeletionWeight (int value) {
		deletionWeight = value;
	}

	public int getDeletionWeight () {
		return deletionWeight;
	}

	public void setReplacementWeight (int value) {
		replacementWeight = value;
	}

	public int getReplacementWeight () {
		return replacementWeight;
	}

	public int getDistance () {
		return distance;
	}
	
	public void setDistance () {
		 distance = distanceMatrix[getSourceString().length() - 1][getTargetString().length() - 1];
	}

	// Constructors
	public Palign() {
		insertionWeight = 1;
		deletionWeight = 1;
		replacementWeight = 1;
	}
	
	/**
	 * Create class with w1=insertion, w2=deletion, and w3=replacement weights
	 * @param w1
	 * @param w2
	 * @param w3
	 */

	public Palign(int w1, int w2, int w3) {
		insertionWeight = w1;
		deletionWeight = w2;
		replacementWeight = w3;
	}

	// Methods

	/**
	 * Determine new cost value based on previous cost and weight of operation.
	 * @param char1 current char of pattern
	 * @param char2 current char of word
	 * @param prevCost previous cost
	 * @return new costs
	 */
	int computeInsertionCosts (char char1, char char2, int prevCost){
		return prevCost+getInsertionWeight();
	}

	int computeDeletionCosts (char char1, char char2, int prevCost){
		return prevCost+getDeletionWeight();	
	}	

	int computeReplacementCosts (char char1, char char2, int prevCost){
		if (char1 == char2)
			return prevCost;
		else
			return prevCost+getReplacementWeight();
	}
	
	public double computeEditSimilarity() {
		double similarity = (double) (1.0 - ((double) this.getDistance()  / Math.max(this.s1.length(), this.s2.length())));
		return similarity;
	}

	// TODO Printing is not adjusted with different digit sizes of integers
	public void printDistanceMatrix () {
		System.out.print("  ");
		for (int i = 0; i < getTargetString().length(); i++) {
			System.out.print(getTargetString().charAt(i)+" ");
		}
		System.out.println();
		for (int i = 0; i < getSourceString().length(); i++) {
			System.out.print(getSourceString().charAt(i)+" ");
			
			for (int j = 0; j < getTargetString().length(); j++) {
				System.out.print(distanceMatrix[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();	
	}

	/**
	 * <p>The dynamic programming algorithm: Models the distance of two strings by the number of operations 
	 * that are necessary to turn string s to string t. Only one-character edit operations are performed. 
	 * We use '-' as the gap operator.</p>
	 * <p>(a, a) denotes a match (no change form s to t)</p>
	 * <p>(a, -) denotes the deletion of character a in s</p>
	 * <p>(a, b) denotes the replacement of character a (in s) by b (in t); a and b are not equal.</p>
	 * <p>(-, b) denotes the insertion of character b in s</p>
	 * 
	 * The class has been defined final because I do not assume it will be used and refined in subclasses
	 * 
	 * @param string1 the pattern
	 * @param string2 the source string
	 * @return the number of operations used to turn string1  to string2
	 */
	public final int dp (String string1, String string2) {
		// This is a naive way to avoid taking care of different starting indices of 
		// Strings and array :-)

		setSourceString(" ".concat(string1));
		setTargetString(" ".concat(string2));

		// NOTE: since the initialization value of int is 0, all cells of the array are automatically 
		// initialized with 0 as well!
		distanceMatrix = new int[getSourceString().length()][getTargetString().length()];

		// Initialization of pattern string by matching the empty string:
		for ( int i = 1; i < getSourceString().length(); i++ ) {
			// define d_w(0:s1:i,0:s2:0) = d_w(0:s1:i-1,0:s2:0) + w(i,-)	
			distanceMatrix[i][0] = computeDeletionCosts(getSourceString().charAt(i), '-', distanceMatrix[i-1][0]);
		}

		for ( int j = 1; j < getTargetString().length(); j++ ) {
			// incrementally process char by char target string, with all chars of pattern string
			// Initially compare with empty char of pattern
			// define d_w(0:s1:0,0:s2:j) = d_w(0:s1:0,0:s2:j) + w(-,j)
			distanceMatrix[0][j] = computeInsertionCosts('-', getTargetString().charAt(j), distanceMatrix[0][j-1]);
		
			for ( int i = 1; i < getSourceString().length(); i++ ) {
				// Compare complete pattern  with current char of target taking into account previous
				// amount of minimal changes:
				// Loop through all characters from s1, and apply all edit operations with current character of t.
				// For current cell[i][j] the costs are computed using the previous costs just surrounding it.
				distanceMatrix[i][j] = 
						Math.min(Math.min(
								// replacement or matching, i.e., check whether ss1(i) and ss2(j) are equal or not
								// previous costs from left diagonal cell (north-west)
								computeReplacementCosts(getSourceString().charAt(i), getTargetString().charAt(j), distanceMatrix[i-1][j-1])
								, 
								// deletion of character a in s1 which means insert gap operator '-'
								// previous cost from upper cell (north)
								computeDeletionCosts(getSourceString().charAt(i), '-', distanceMatrix[i-1][j])
								)
								,
								// insertion; previous cost from left element (west)
								computeInsertionCosts('-', getTargetString().charAt(j), distanceMatrix[i][j-1])
								);

			}
		}

		// Extract distance
		setDistance();
		// return distance value d
		return getDistance();
	}
}
