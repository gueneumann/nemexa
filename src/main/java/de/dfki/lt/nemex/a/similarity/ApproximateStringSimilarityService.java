package de.dfki.lt.nemex.a.similarity;

/*
 *  The interface for approximate string similarity service.
 *  NEMEX
 *  October 2012
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

import java.util.List;

/**
 * <H4>
 * The interface for providing the service for approximate string retrieval.</H4>
 */
public interface ApproximateStringSimilarityService {

	/**
	 * The trivial All-Scan algorithm. This is not efficient and scalable.
	 * 
	 * @param similarityThreshold
	 *            Decimal number between zero and one. One corresponds to exact
	 *            match.
	 * @return List of retrieved strings which are approximately similar to the
	 *         query string.
	 */
	List<String> doApproximateStringMatchingUsingAllScan(
			double similarityThreshold);

	/**
	 * The advanced fast and efficient CPMerge algorithm for approximate string
	 * matching.
	 * 
	 * @param similarityThreshold
	 *            Decimal number between zero and one. One corresponds to exact
	 *            match.
	 * @return List of retrieved strings which are approximately similar to the
	 *         query string.
	 */
	List<String> doApproximateStringMatchingUsingCPMerge(
			double similarityThreshold);
}
