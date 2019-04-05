package de.dfki.lt.nemex.f.verify;

import java.util.Collection;
import java.util.LinkedList;

import de.dfki.lt.nemex.a.ngram.CharacterNgram;
import de.dfki.lt.nemex.a.ngram.CharacterNgramWithDuplicate;
import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;


public class Verifier {
	
	// Ngrams are used to token-level similarity like cosinus, dice, jaccars
	private CharacterNgram queryNgram ;
	
	private CharacterNgram entityNgram ;
	
	// Strings are used to character level similarity like ED and EDS
	
	private String queryString ;
	
	private String entityString ;
	
	private NemexFBean nemexBean;
	
	public Verifier(NemexFBean nemexFbean){
		this.nemexBean = nemexFbean;
	}
	
	private CharacterNgram createCharacterNgramFromQueryString(String string){
		CharacterNgram ngram = null;
		if (!this.nemexBean.isIgnoreDuplicateNgrams()) {
			ngram = new CharacterNgramWithDuplicate(string, this.nemexBean.getnGramSize());
		} else {
			System.err.println("Currently, only CharacterNgramWithDuplicate class preserves ngram ordering, "
					+ "so only this is currently supported in NemexF!");
			System.err.println("Thus make sure that 'this.getNemexFBean().ignoreDuplicateNgrams = false;'");
			System.exit(0);
		}
		return ngram;
	}
	
	private Collection<String> intersection(Collection<String> A, Collection<String> B) {
		Collection<String> rtnList = new LinkedList<>();
	    for(String dto : A) {
	        if(B.contains(dto)) {
	            rtnList.add(dto);
	        }
	    }
	    return rtnList;
	}
	
	/*
	 * cosinus(r,s) = |intersection(r,s)|/sqroot(|r|*|s)|)
	 */
	
	// TODO - does not work !!!
	private double cosinusScore(CharacterNgram query, CharacterNgram entity) {
        double score = 
        		intersection(
        				query.getNgrams(),entity.getNgrams()).size() / 
        		Math.sqrt((query.getGramSize() + entity.getGramSize()));
        System.out.println(query.getNgrams().toString());
		return score;

	}

	private double diceScore(CharacterNgram query, CharacterNgram entity) {

		return 0.0;

	}

	private double jaccardScore(CharacterNgram query, CharacterNgram entity) {

		return 0.0;

	}

	private double edScore(String query, String entity) {

		return 0.0;

	}

	private double edsScore(String query, String entity) {

		return 0.0;

	}
	
	public void setEntity(String entity) {
		if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.COSINE_SIMILARITY_MEASURE ||
				this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.DICE_SIMILARITY_MEASURE ||
				this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE){
			// TODO: check whether CharacterNgramWithDuplicate is correct
			// I think so: de.dfki.lt.nemex.f.NemexFController.setCharacterNgramFromQueryString(String)
			this.entityNgram = createCharacterNgramFromQueryString(entity);
		}
		else
			entityString = entity;
	}
	
	public void setQuery(String query) {	
		if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.COSINE_SIMILARITY_MEASURE ||
				this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.DICE_SIMILARITY_MEASURE ||
				this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE){
			// TODO: check whether CharacterNgramWithDuplicate is correct
			// I think so: de.dfki.lt.nemex.f.NemexFController.setCharacterNgramFromQueryString(String)
			this.queryNgram = createCharacterNgramFromQueryString(query);
		}
		else
			queryString = query;
	}
	
	
	public double score() {
		double computedScore = 0.0;
		
		if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.COSINE_SIMILARITY_MEASURE) {
			computedScore = this.cosinusScore(this.queryNgram, this.entityNgram);
		}
		else
			if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.DICE_SIMILARITY_MEASURE) {
				this.diceScore(this.queryNgram, this.entityNgram);
			}
			else
				if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE) {
					this.jaccardScore(this.queryNgram, this.entityNgram);
				}
				else
					if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.ED_SIMILARITY_MEASURE) {
						this.edScore(this.queryString, this.entityString);
					}
					else
						if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.EDS_SIMILARITY_MEASURE) {
							this.edsScore(this.queryString, this.entityString);
						}
						else
							System.exit(0);
			
		
		return computedScore;
	}
	
}
