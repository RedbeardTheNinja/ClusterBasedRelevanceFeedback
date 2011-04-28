package org.apache.lucene.search;


public class LanguageModelSimilarityProvider implements SimilarityProvider {
	private static final Similarity impl = new LanguageModelSimilarity();
	@Override
	public float coord(int overlap, int maxOverlap) {
		// TODO Auto-generated method stub
		return overlap / (float)maxOverlap;
	}

	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		// TODO Auto-generated method stub
		return 1.0f;
	}

	@Override
	public Similarity get(String field) {
		// TODO Auto-generated method stub
		return impl;
	}

}
