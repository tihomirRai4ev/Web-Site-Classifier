package com.core.dataanalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * This is a simple demo created specially for Information Retrieval course held at FMI.
 * In order to complete the current exercise, please, first read http://www.lucenetutorial.com/lucene-in-5-minutes.html
 * Detailed presentation from Chris Manning and Pandu Nayak can be found here: https://goo.gl/R9F78j
 */

public class App {
   private static final String FULLPATH_FIELD = "fullpath";
   private static final String FILENAME_FIELD = "filename";
   private static final String CONTENTS_FIELD = "contents";
   private static final int HITS_LIMIT = 10;

   private static IndexWriter createIndex(Directory index, Analyzer analyzer) throws IOException {
      IndexWriterConfig config = new IndexWriterConfig(analyzer);

      return new IndexWriter(index, config);
   }

   private static Analyzer createDefaultAnalyzer() {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      analyzerMap.put(CONTENTS_FIELD, new StandardAnalyzer());

      return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);
   }

   private static Document createDocument(File f) {
      // FULLPATH_FIELD (string), FILENAME_FIELD (string), CONTENTS_FIELD (text)

      Document doc = new Document();
      try {
         FileReader fileReader = new FileReader(f);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         doc.add(new StringField(FULLPATH_FIELD, f.getAbsolutePath(), Field.Store.YES));
         doc.add(new StringField(FILENAME_FIELD, f.getName(), Field.Store.YES));
         doc.add(new TextField(CONTENTS_FIELD, new FileReader(f)));
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return doc;
   }

   private static void loadData(Directory index, String path, Analyzer analyzer) throws IOException {
      try (IndexWriter w = createIndex(index, analyzer)) {
         Files.walk(Paths.get(path))
               .parallel()
               .filter(f -> Files.isRegularFile(f))
               .map(Path::toFile)
               .map(App::createDocument)
               .filter(Objects::nonNull)
               .forEach(doc -> {
                  System.out.println(doc.get(FULLPATH_FIELD));
                  // TODO add document to the store through the IndexWriter object
                  try {
                     w.addDocument(doc);
                  } catch (IOException e) {
                     e.printStackTrace();
                  }
               });

         System.out.println("Loaded documents " + w.numDocs());
      }
   }

   private static ScoreDoc[] getScoredDocs(Query q, int hitsLimit, IndexSearcher searcher) throws IOException {
      // TODO get top documents and extract ScoreDocs[] from them
      return searcher.search(q, hitsLimit).scoreDocs;
   }

   private static Query buildQuery(String querystr, Analyzer analyzer) throws ParseException {
      // TODO construct query object based on CONTENTS_FIELD
      return new QueryParser(CONTENTS_FIELD, analyzer).parse(querystr);
   }

   private static IndexSearcher createSearcher(IndexReader reader) {
      IndexSearcher searcher = new IndexSearcher(reader);
      // TODO set similarity to the searcher
      searcher.setSimilarity(IndexSearcher.getDefaultSimilarity());

      return searcher;
   }

   private static void search(Directory index, Query q, boolean verbose) throws IOException {
      // 3. search

      // reader can only be closed when there
      // is no need to access the documents any more.
      try (IndexReader reader = DirectoryReader.open(index)) {
         IndexSearcher searcher = createSearcher(reader);

         ScoreDoc[] hits = getScoredDocs(q, HITS_LIMIT, searcher);

         // 4. display results
         System.out.println("Found " + hits.length + " hits.");

         for (int i = 0; i < hits.length; ++i) {
            // TODO Extract docId and score from hits object
            int docId = hits[i].doc;
            double score = hits[i].score;

            Document d = searcher.doc(docId);

            String output = String.format("%d.\t%d\t%f\t%s", (i + 1), docId, score, d.get(FULLPATH_FIELD));

            System.out.println(output);
            if (verbose) {
               System.out.println("\nExplanation of hit score calculation: ");
               System.out.println(searcher.explain(q, docId));
            }
            System.out.println("----------------------------------------------------------------");
         }

      }
   }

   public static void main(String[] args) throws IOException, ParseException {
      Directory index = new RAMDirectory();
      String path = "X:\\Dropbox\\source-code\\java\\projects\\Web-Site-Classifier\\";
      Analyzer analyzer = createDefaultAnalyzer();
      loadData(index, path, analyzer);

      try {
         IndexWriter writer = new IndexWriter(FSDirectory.open(new File(".").toPath()), new IndexWriterConfig(analyzer));
         IndexReader reader = DirectoryReader.open(writer,true, true);
         TermStats[] commonTerms = HighFreqTerms.getHighFreqTerms(reader, 30, null, new HighFreqTerms.DocFreqComparator());
         for (TermStats commonTerm : commonTerms) {
            System.out.println(commonTerm.termtext.utf8ToString());
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
