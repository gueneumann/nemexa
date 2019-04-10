package de.dfki.lt.nemex.f.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
	
	// TODO:
	// This is eventually not the best way to do it ?
	private Collection<String> intersection(Collection<String> A, Collection<String> B) {
		Collection<String> rtnList = new LinkedList<>();

		if (A.size() <= B.size())
			for (String dto : A) {
				if (B.contains(dto)) {
					rtnList.add(dto);
				}
			}
		else
			for (String dto : B) {
				if (A.contains(dto)) {
					rtnList.add(dto);
				}
			}
		return rtnList;
	}
	
	private Collection<String> union(Collection<String> A, Collection<String> B) {
        Set<String> set = new HashSet<String>();

        set.addAll(A);
        set.addAll(B);

        return (Collection<String>)(set);
    }
	
	/*
	 * cosinus(r,s) = |intersection(r,s)|/sqroot(|r|*|s|))
	 */
	private double cosinusScore(CharacterNgram query, CharacterNgram entity) {
		Collection<String> rtnList = intersection(query.getNgrams(), entity.getNgrams());
		double score = rtnList.size() / Math.sqrt((query.getNgrams().size() * entity.getNgrams().size()));

		return score;
	}

	/*
	 * dice(r,s) = 2*|intersection(r,s)|/(|r|*|s|)
	 */
	private double diceScore(CharacterNgram query, CharacterNgram entity) {
		Collection<String> rtnList = intersection(query.getNgrams(),entity.getNgrams());
        double score = (2.0 * rtnList.size()) / (query.getNgrams().size() + entity.getNgrams().size());
//        System.out.println("Query:     " + query.getNgrams() + " ... " + query.getNgrams().size());
//        System.out.println("Entity:    " + entity.getNgrams() + " ... " + entity.getNgrams().size());
//        System.out.println("Intersect: " + rtnList + " ... " + rtnList.size());
        
		return score;
	}

	private double jaccardScore(CharacterNgram query, CharacterNgram entity) {
		Collection<String> rtnList1 = intersection(query.getNgrams(),entity.getNgrams());
		Collection<String> rtnList2 = union(query.getNgrams(),entity.getNgrams());
        double score = ((1.0 * rtnList1.size()) / rtnList2.size());
		return score;

	}

	private double edScore(String query, String entity) {
		Palign palign = new Palign(1,1,1);
		palign.dp(query, entity);

		return (1.0 * palign.getDistance());

	}

	private double edsScore(String query, String entity) {
		Palign palign = new Palign(1,1,1);
		palign.dp(query, entity);
		return palign.computeEditSimilarity();

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
	
	/*
	 * TODO
	 * This is to filter out candidates whose computed threshold is actually lower/larger than the given one
	 * Currently, this happens mainly for ED and EDS.
	 * The reason might be that for these 
	 * de.dfki.lt.nemex.f.similarity.EditDistanceMeasure.tighterUpperWindowSize(int, int, double)
	 * are NOT yet defined ! -> I have to check this !!!
	 */
	public boolean verifyScore(double computedScore) {
		boolean verified = false;
		if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.COSINE_SIMILARITY_MEASURE
				|| this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.DICE_SIMILARITY_MEASURE
				|| this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE
				|| this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.EDS_SIMILARITY_MEASURE) {
			verified = (computedScore >= this.nemexBean.getSimilarityThreshold()) ? true : false;
		} else if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.ED_SIMILARITY_MEASURE) {
			verified = (computedScore <= this.nemexBean.getSimilarityThreshold()) ? true : false;
		} else
			System.err.println("No threshold for " + this.nemexBean.getSimilarityMeasure());
		return verified;
	}
	
	public double score() {
		double computedScore = 0.0;
		
		if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.COSINE_SIMILARITY_MEASURE) {
			computedScore = this.cosinusScore(this.queryNgram, this.entityNgram);
		}
		else
			if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.DICE_SIMILARITY_MEASURE) {
				computedScore = this.diceScore(this.queryNgram, this.entityNgram);
			}
			else
				if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE) {
					computedScore = this.jaccardScore(this.queryNgram, this.entityNgram);
				}
				else
					if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.ED_SIMILARITY_MEASURE) {
						computedScore = this.edScore(this.queryString, this.entityString);
					}
					else
						if (this.nemexBean.getSimilarityMeasure() == SimilarityMeasure.EDS_SIMILARITY_MEASURE) {
							computedScore = this.edsScore(this.queryString, this.entityString);
						}
						else
							System.exit(0);
		
		return computedScore;
	}
	
}
