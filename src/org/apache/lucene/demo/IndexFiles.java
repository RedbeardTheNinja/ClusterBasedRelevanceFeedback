package org.apache.lucene.demo;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.codecs.CodecProvider;
import org.apache.lucene.index.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.index.codecs.standard.StandardCodec;
import org.apache.lucene.search.LanguageModelSimilarityProvider;
import org.apache.lucene.search.SimilarityProvider;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucenesandbox.xmlindexingdemo.XMLDocumentHandlerTxt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/** Index all text files under a directory. */
public class IndexFiles {
  
  private IndexFiles() {}

  static final String INDEX_DIR_ST_LM ="../LMSTIndex/";
  static final String INDEX_DIR_SM_LM = "../LMSMIndex/";
  static final String INDEX_DIR_SM = "../SMIndex/";
  static final String INDEX_DIR_ST = "../SMIndex/";
  
  /** Index all text files under a directory. */
  public static void main(String[] args) {    
    final File docDir = new File("../frcr_collection/");
    index(INDEX_DIR_SM, docDir, false);
    index(INDEX_DIR_SM_LM, docDir, true);
    
  }
  public static void index(String indexDir, File docDir, boolean lm) {	  
    try {
      CodecProvider cp = new CodecProvider();
      SimilarityProvider sp = new LanguageModelSimilarityProvider();
      StandardCodec codec = new StandardCodec();
      //LanguageModelCodec codec = new LanguageModelCodec();
      //IndexWriterConfig configST = new IndexWriterConfig(Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40));
      IndexWriterConfig configSM = new IndexWriterConfig(Version.LUCENE_40, new SimpleAnalyzer());
      cp.register(codec);
      //config.setCodecProvider(cp);
      //configST.setSimilarityProvider(sp);
      if (lm)
    	  configSM.setSimilarityProvider(sp);
      Date start = new Date();
      IndexWriter writer = new IndexWriter(FSDirectory.open(new File(indexDir)), configSM);
      System.out.println("Indexing to directory '" +indexDir+ "'...");
      indexDocs(writer, docDir);
      System.out.println("Optimizing...");
      writer.optimize();
      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  static void indexDocs(IndexWriter writer, File file)
    throws IOException {
    // do not try to index files that cannot be read
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {
        System.out.println("adding " + file);
		try {
				XMLDocumentHandlerTxt hdlr = new XMLDocumentHandlerTxt(file);
				Iterator docs = hdlr.getDocuments().iterator();
				while (docs.hasNext()) {
					Document doc = (Document) docs.next();
					writer.addDocument(doc);
			}
        }
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        catch (FileNotFoundException fnfe) {
          ;
        }
      }
    }
  }
  
}
