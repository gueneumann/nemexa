package de.dfki.lt.nemex.f.aligner;

import java.util.Map;

import de.dfki.lt.nemex.f.NemexFContainer;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

public interface AlignerInterface {

	public Map<Integer, Map<Integer, Long>> findSubstringsForEntity(
			Long entityIndex, int entityNgramLength, SimilarityMeasureInterface simFct, double simThreshold, NemexFContainer container);
}
