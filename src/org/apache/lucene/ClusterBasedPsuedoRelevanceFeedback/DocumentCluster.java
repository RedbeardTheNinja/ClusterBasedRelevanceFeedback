package org.apache.lucene.ClusterBasedPsuedoRelevanceFeedback;

import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.ScoreDoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Antonio
 * Date: 5/5/11
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentCluster {
    public List<ScoreDoc> Docs = new ArrayList<ScoreDoc>();
    private List<TermFreqVector> points = new ArrayList<TermFreqVector>();
    private TermFreqVector center;
    private final int MAX_POINTS = 3;
    private final double SIM_THRESHOLD = 0.5;

    /*  Creates a new Cluster with sd as its first doc and the given vector as its center, presumably
     *  they are both the same document
     */
    public DocumentCluster(ScoreDoc sd, TermFreqVector tfv) {
        Docs.add(sd);
        points.add(tfv);
        center = tfv;
    }

    //add the doc <sd,tfv> to our cluster iff the cluster is not full
    //and it is within the similarity threshold
    public void addPoint(ScoreDoc sd, TermFreqVector tfv) {
        if(Docs.size() == MAX_POINTS)
            return;
        if(true){//CosineSimilarity(center, tfv) > SIM_THRESHOLD) {
            Docs.add(sd);
            points.add(tfv);
        }
    }

    public double getScore() {
        double score = 1.0;
        for(ScoreDoc sd : Docs) {
            score *= sd.score;
        }
        return score;
    }
}
