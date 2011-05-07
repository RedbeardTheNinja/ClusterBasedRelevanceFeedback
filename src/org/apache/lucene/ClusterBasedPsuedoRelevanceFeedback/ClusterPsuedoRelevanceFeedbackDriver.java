package org.apache.lucene.ClusterBasedPsuedoRelevanceFeedback;

/**
 * Created by IntelliJ IDEA.
 * User: Antonio
 * Date: 4/28/11
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterPsuedoRelevanceFeedbackDriver {

    public static void main(String args[]) throws IOException {

        SimilarityProvider sp = new LanguageModelSimilarityProvider();
        IndexReader reader;
        reader = IndexReader.open(FSDirectory.open(new File("../LMSMIndex/")), true);// only searching, so read-only=true
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarityProvider(sp);
        TopDocs top = searcher.search(new TermQuery(new Term("text", "politician")),100);
        ScoreDoc[] allDocs = top.scoreDocs;
        for(int i : reader.getTermFreqVector(allDocs[0].doc,"text").getTermFrequencies())
            System.out.println(i);
        for(BytesRef i : reader.getTermFreqVector(allDocs[0].doc,"text").getTerms())
            System.out.println(i.bytes[0]);
        System.out.println(reader.getTermFreqVector(allDocs[0].doc,"text").getTerms().length);
        ArrayList<DocumentCluster> clusters = new ArrayList<DocumentCluster>(100);
        for(ScoreDoc sd : allDocs) {
            clusters.add(new DocumentCluster(sd,reader.getTermFreqVector(sd.doc,"text")));
        }
        for(DocumentCluster c : clusters) {
            for(ScoreDoc sd : allDocs) {
                c.addPoint(sd,reader.getTermFreqVector(sd.doc,"text"));
            }
        }
        DocumentCluster first= clusters.get(0),second= clusters.get(0),third = clusters.get(0);
        for(DocumentCluster c : clusters) {
            if(c.getScore() > first.getScore()) {
                first = c;
            } else if(c.getScore() > second.getScore()) {
                second  = c;
            } else if(c.getScore() > third.getScore()) {
                third = c;
            }
        }

        List<ScoreDoc> tops = first.Docs;
        tops.addAll(second.Docs);
        tops.addAll(third.Docs);
        Map<ScoreDoc,Integer> rankings = new HashMap<ScoreDoc,Integer>();
        for(ScoreDoc sd : tops) {
            if(rankings.containsKey(sd))
                rankings.put(sd,rankings.get(sd) + 1);
            else
                rankings.put(sd,1);
        }
        
    }

}
