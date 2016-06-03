package de.dfki.lt.nemex.f.aligner;

import java.util.Map;

import de.dfki.lt.nemex.f.NemexFContainer;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

public class NoPruneAligner implements AlignerInterface {

	public NoPruneAligner(){
	}

	/**
	 * NoPruneAligner applies no pruning, i.e., it enumerates all candidates without verifying
	 * length restrictions, which means that the overlap similarity will be applied on a huge set
	 * of candidates.
	 */
	@Override
	public Map<Integer, Map<Integer, Long>> findSubstringsForEntity(
			Long entityIndex, int entityNgramLength,
			SimilarityMeasureInterface simFct, double simThreshold,
			NemexFContainer container) {
		// Initialize entity boundaries only if useful
		container.initializeEntityBoundaries(
				entityNgramLength, simFct, simThreshold);

		container.enumerateCandidatePairsMain(container.getPositionList().get(entityIndex));

		return container.getCandidatePairs(
				entityIndex, entityNgramLength, simFct, simThreshold);
	}

}
