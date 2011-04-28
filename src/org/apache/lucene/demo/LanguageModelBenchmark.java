package org.apache.lucene.demo;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.codecs.CodecProvider;
import org.apache.lucene.index.codecs.standard.StandardCodec;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LanguageModelQuery;
import org.apache.lucene.search.LanguageModelSimilarityProvider;
import org.apache.lucene.search.SimilarityProvider;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Antonio
 * Date: 4/10/11
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class LanguageModelBenchmark {
    /**
     * Testing Lucene Base against Benchmark.
     */
    public static void main(String[] args) throws Exception {
        String indexST = IndexFiles.INDEX_DIR_ST;
        String indexSM = IndexFiles.INDEX_DIR_SM;
        String indexSTLM = IndexFiles.INDEX_DIR_ST_LM;
        String indexSMLM = IndexFiles.INDEX_DIR_SM_LM;
        
        System.out.println("SMA - ST");
        benchmark(indexSM, false, 0.0f);
        
        System.out.println("SMA - LM 500");
        benchmark(indexSMLM, true, 500.0f);
        
        System.out.println("SMA - LM 750");
        benchmark(indexSMLM, true, 750f);
        
        System.out.println("SMA - LM 1000");
        benchmark(indexSMLM, true, 1000f);
        
        System.out.println("SMA - LM 1500");
        benchmark(indexSMLM, true, 1500f);
        
        System.out.println("SMA - LM 2000");
        benchmark(indexSMLM, true, 2000f);
        
        System.out.println("SMA - LM 2500");
        benchmark(indexSMLM, true, 2500f);
        
        System.out.println("SMA - LM 3000");
        benchmark(indexSMLM, true, 3000f);
        
        System.out.println("SMA - LM 4000");
        benchmark(indexSMLM, true, 4000f);/*
        
        //STD Analyzer
        
        System.out.println("STD - ST");
        benchmark(indexST, false, 0.0f);
        
        System.out.println("STD - LM 0");
        benchmark(indexST, true, 0.0f);
        
        System.out.println("STD - LM .015");
        benchmark(indexST, true, .015f);
        
        System.out.println("STD - LM .03");
        benchmark(indexST, true, .03f);
        
        System.out.println("STD - LM .06");
        benchmark(indexST, true, .06f);
        
        System.out.println("STD - LM .12");
        benchmark(indexST, true, .12f);
        
        System.out.println("STD - LM .25");
        benchmark(indexST, true, .25f);
        
        System.out.println("STD - LM .5");
        benchmark(indexST, true, .5f);
        
        System.out.println("STD - LM .75");
        benchmark(indexST, true, .75f);*/
    }
    
    public static void benchmark(String indexDir, boolean lm, float smooth) {
		try {
	    	SimilarityProvider sp = new LanguageModelSimilarityProvider();
	        IndexReader reader;
	        reader = IndexReader.open(FSDirectory.open(new File(indexDir)), true);// only searching, so read-only=true
	        IndexSearcher searcher = new IndexSearcher(reader);
	        searcher.setSimilarityProvider(sp);	        
	        PrintWriter logger = new PrintWriter(new FileWriter(new File("../stat" + (lm ? "lm" + smooth : "st") +  ".txt")), true);
	        TrecTopicsReader qReader = new TrecTopicsReader();
	        QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(new File("../topicsFile.txt"))));
	        Judge judge = new TrecJudge(new BufferedReader(new FileReader(new File("../qrelsFile.txt"))));
	        judge.validateData(qqs, logger);
	        QualityQueryParser qqParser = new SimpleQQParser("title", "text");
	        
	        QualityStats stats[];
	        SubmissionReport submitLog = null;
	        if (lm) {
	        	LanguageQualityBenchmark qrun = new LanguageQualityBenchmark(qqs, qqParser, searcher, "DOCNO", smooth);
		        stats = qrun.execute(judge, submitLog, logger);
	        } else {
	        	QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, "DOCNO");
		        stats = qrun.execute(judge, submitLog, logger);
	        }
	        QualityStats avg = QualityStats.average(stats);

	        double minMAP = 0, maxMAP = 0, MAP = 0, minR = 0, maxR = 0, RP = 0;
	        boolean first = true;
	        for (int i = 0; i < stats.length; i++) {
	            double TMAP, TR = 0;
	            TMAP = stats[i].getAvp();
	            TR = stats[i].getNumGoodPoints() / stats[i].getMaxGoodPoints();


	            if (first) {
	                minMAP = TMAP;
	                minR = TR;
	                maxMAP = TMAP;
	                maxR = TR;
	                first = false;
	            }

	            if (TMAP < minMAP) {
	                minMAP = TMAP;
	            }
	            if (TMAP > maxMAP) {
	                maxMAP = TMAP;
	            }
	            if (TR < minR) {
	                minR = TR;
	            }
	            if (TR > maxR) {
	                maxR = TR;
	            }
	            RP += TR;
	            MAP += TMAP;
	        }
	        RP = RP / stats.length;
	        MAP = MAP / stats.length;
	        System.out.println("Min MAP :" + minMAP + "  Max MAP : " + maxMAP + " AVG MAP : " + MAP + "\n"
	                + "Min R :" + minR + "  Max R : " + maxR + " AVG R : " + RP + "\n");
	        logger.close();
			reader.close();
		} catch (CorruptIndexException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			
		}
    }
}
