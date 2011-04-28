/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Bits;

/**
 *
 * @author Guest
 */
public class LanguageModelQuery extends Query {
    private static int maxTermCount = 1024;
    private float smooth;
	private String field;
    private ArrayList<Term> terms;

    public LanguageModelQuery() {
    	this(0.0f);
    }
    public LanguageModelQuery(float smooth) {
        this.terms = new ArrayList<Term>();
        this.smooth = smooth;
    }

    public void addTerm(Term term) {
        if (terms.size() >= maxTermCount)
            //throw new TooManyTerms();
            return;
        if (terms.size() == 0)
        	field = term.field();
        else if (term.field() != field)
        	throw new IllegalArgumentException("All Language Model terms must be in the same field: " + term);
        terms.add(term);
    }

    public Term[] getTerms() {
        return terms.toArray(new Term[terms.size()]);
    }

    public List<Term> terms() { return terms; }

    public final Iterator<Term> iterator() { return terms().iterator(); }

    protected class LanguageModelWeight extends Weight {
        protected Similarity similarity;
        
        public LanguageModelWeight(IndexSearcher searcher) {
        	this.similarity = searcher.getSimilarityProvider().get(field);
        }
        
        @Override
        public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Query getQuery() { return LanguageModelQuery.this; }

        @Override
        public float getValue() {
            return getBoost();
        }

        @Override
        public void normalize(float norm) {
            //Do Nothing?
        	return;
        }

        @Override
        public Scorer scorer(AtomicReaderContext context, ScorerContext scorerContext) throws IOException {
        	final IndexReader reader = context.reader;
        	final byte[] norms = reader.norms(field);
        	float colLen = reader.terms(field).getSumTotalTermFreq();
            PostingsAndFreq[] postingsFreqs = new PostingsAndFreq[terms.size()];
            final Bits delDocs = reader.getDeletedDocs();
            for (int i = 0; i < terms.size(); i++) {
            	final Term t = terms.get(i);
            	float colFreq = reader.terms(field).totalTermFreq(t.bytes());
            	DocsEnum postingsEnum = reader.termDocsEnum(delDocs, t.field(), t.bytes());
            	postingsFreqs[i] = new PostingsAndFreq(postingsEnum, reader.docFreq(t.field(), t.bytes()), colFreq);
            }
            if (smooth >0)
            	return new LanguageModelScorer(this, postingsFreqs, colLen, similarity, norms, smooth);
            else
            	return new LanguageModelScorer(this, postingsFreqs, colLen, similarity, norms);
        }

        @Override
        public float sumOfSquaredWeights() throws IOException {
            return getBoost() * getBoost();
        }

    }
    @Override
    public Weight weight(IndexSearcher searcher) throws IOException {
        return new LanguageModelWeight(searcher);
    }

    static class PostingsAndFreq implements Comparable<PostingsAndFreq> {
        final DocsEnum postings;
        final int docFreq;
        final float colFreq;

        public PostingsAndFreq(DocsEnum postings, int docFreq, float colFreq) {
            this.postings = postings;
            this.docFreq = docFreq;
            this.colFreq = colFreq;
        }

        public int compareTo(PostingsAndFreq other) {
            return docFreq - other.docFreq;
        }
    }

    @Override
    public String toString(String field) {
    	String tmp = "";
    	if (field.equals(field)) {
    		for(int i=0;i<terms.size()-1;i++) {
    			tmp += terms.get(i) + " ";
    		}
    		tmp += terms.get(terms.size()-1);
    		return tmp;
    	}
    	return "";
    }

}