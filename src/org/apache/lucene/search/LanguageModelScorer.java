/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.lucene.search;

import java.io.IOException;

/**
 *
 * @author Guest
 */

public class LanguageModelScorer extends Scorer{
    private final float colLen;
    private static final float DEFAULT_SMOOTHING = 0.15f;
    private final float smooth;
    private final byte[] norms;
    private final Similarity similarity;
    private final LanguageModelQuery.PostingsAndFreq[] postings;
    private int docID;
    private boolean firstTime = true;
    public LanguageModelScorer(Weight weight, LanguageModelQuery.PostingsAndFreq[] postings, float colLen, Similarity similarity, byte[] norms) {
        this(weight, postings, colLen, similarity, norms, DEFAULT_SMOOTHING);
    }
    
    public LanguageModelScorer(Weight weight, LanguageModelQuery.PostingsAndFreq[] postings, float colLen, Similarity similarity, byte[] norms, float smooth) {
        super(weight);
        this.colLen = colLen;
        this.postings = postings;
        this.norms = norms;
        this.similarity = similarity;
        this.smooth = smooth;
    }
    
    @Override
    public float score() throws IOException {
        float score = 1.0f;
        for(LanguageModelQuery.PostingsAndFreq pf : postings) {
            float docLen = similarity.decodeNormValue(norms[docID]);
            float docFreq = 0;
            float colFreq = pf.colFreq;
            if (pf.postings != null && pf.postings.docID() == docID) {
                docFreq = pf.postings.freq();
            }
            score*=((docLen*docFreq)/((docLen+smooth)*docLen))+((smooth*colFreq)/((docLen+smooth)*colLen));
        }
        return score;
    }

    @Override
    public int docID() { return docID; }
    
    public void init() throws IOException {
    	for(LanguageModelQuery.PostingsAndFreq pf : postings) {
    		if (pf.postings != null && pf.postings.docID()!= NO_MORE_DOCS)
    			pf.postings.nextDoc();
    	}
    }
    
    @Override
    public int nextDoc() throws IOException {
        int min = NO_MORE_DOCS;
    	if (firstTime) {
    		docID=-1;
    		init();
        	firstTime=false;
    	}
    	if (docID == NO_MORE_DOCS)
    		return docID;
        for(LanguageModelQuery.PostingsAndFreq pf : postings) {
        	if(pf.postings != null && docID >= 0 && pf.postings.docID() == docID)
        		pf.postings.nextDoc();
        	if(pf.postings != null && pf.postings.docID() != NO_MORE_DOCS && (pf.postings.docID()<min || min == NO_MORE_DOCS))
        		min = pf.postings.docID();
        }
        docID = min;
        return min;
    }

    @Override
    public int advance(int target) throws IOException {
    	int min = NO_MORE_DOCS;
    	for(LanguageModelQuery.PostingsAndFreq pf : postings) {
    		int docID = pf.postings.docID();
    		if(pf.postings != null && docID >= 0 && docID != NO_MORE_DOCS && docID < target)
    			docID = pf.postings.advance(target);
    		if(docID < min)
    			min = docID;
    	}
    	docID = min;
    	return min;
    }

}
