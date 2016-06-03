package de.dfki.lt.nemex.f.aligner;

import java.util.Map;

import de.dfki.lt.nemex.f.NemexFContainer;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

public class LazyPruneAligner implements AlignerInterface {
	
	public LazyPruneAligner(){
	}

	/**
	 * Lazy-count pruning: 
	 * <p>if length of entity-position-list is < T_l, then prune entity
	 * <p>which mean: do not enumerate-candidate-pairs
	 */
	@Override
	public Map<Integer, Map<Integer, Long>> findSubstringsForEntity(
			Long entityIndex, int entityNgramLength,
			SimilarityMeasureInterface simFct, double simThreshold,
			NemexFContainer container) {

		// Lazy-count pruning: 
		// if length of position-list of current entity is < T_l, then prune entity
		// which mean: do not enumerate-candidate-pairs

		if (!(container.getPositionList().get(entityIndex).size()
				<
				container.getLowerEntityLength())
				)
		{
			// The rest is as in the BaseLineAligner
			// Initialize entity boundaries only if useful
			container.initializeEntityBoundaries(
					entityNgramLength, simFct, simThreshold);

			container.enumerateCandidatePairsMain(container.getPositionList().get(entityIndex));

			return container.getCandidatePairs(
					entityIndex, entityNgramLength, simFct, simThreshold);
		}
		else
			return null;
	}
}
