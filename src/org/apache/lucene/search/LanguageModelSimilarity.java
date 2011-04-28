package org.apache.lucene.search;

import org.apache.lucene.index.FieldInvertState;

public class LanguageModelSimilarity extends Similarity {

	@Override
	//Used to store Doc Length
	public float computeNorm(FieldInvertState state) {
		return (float)state.getLength();
	}
	
	//Following doesn't matter in LM
	
	@Override
	public float sloppyFreq(int distance) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float tf(float freq) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float idf(int docFreq, int numDocs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
