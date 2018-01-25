package com.core.dataanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

import com.core.webcrawler.impl.Spider;
import com.datumbox.opensource.classifiers.NaiveBayes;
import com.datumbox.opensource.dataobjects.NaiveBayesKnowledgeBase;


public class App {
   private static final String FULLPATH_FIELD = "fullpath";
   private static final String FILENAME_FIELD = "filename";
   private static final String CONTENTS_FIELD = "contents";
   static Spider testSpider;
   private static final int HITS_LIMIT = 10;

   private static IndexWriter createIndex(Directory index, Analyzer analyzer)
         throws IOException {
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
         doc.add(new StringField(FULLPATH_FIELD, f.getAbsolutePath(),
               Field.Store.YES));
         doc.add(new StringField(FILENAME_FIELD, f.getName(), Field.Store.YES));
         doc.add(new TextField(CONTENTS_FIELD, new FileReader(f)));
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return doc;
   }

   private static void loadData(Directory index, String path, Analyzer analyzer)
         throws IOException {
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

   private static ScoreDoc[] getScoredDocs(Query q, int hitsLimit,
         IndexSearcher searcher) throws IOException {
      // TODO get top documents and extract ScoreDocs[] from them
      return searcher.search(q, hitsLimit).scoreDocs;
   }

   private static Query buildQuery(String querystr, Analyzer analyzer)
         throws ParseException {
      // TODO construct query object based on CONTENTS_FIELD
      return new QueryParser(CONTENTS_FIELD, analyzer).parse(querystr);
   }

   private static IndexSearcher createSearcher(IndexReader reader) {
      IndexSearcher searcher = new IndexSearcher(reader);
      // TODO set similarity to the searcher
      searcher.setSimilarity(IndexSearcher.getDefaultSimilarity());

      return searcher;
   }

   private static void search(Directory index, Query q, boolean verbose)
         throws IOException {
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

            String output = String.format("%d.\t%d\t%f\t%s", (i + 1), docId,
                  score, d.get(FULLPATH_FIELD));

            System.out.println(output);
            if (verbose) {
               System.out.println("\nExplanation of hit score calculation: ");
               System.out.println(searcher.explain(q, docId));
            }
            System.out.println(
                  "----------------------------------------------------------------");
         }

      }
   }

   public static void main(String[] args) throws IOException {
      Directory index = new RAMDirectory();
      String articlesPath =
            "C:\\Users\\traychev\\Documents\\workspace-sts-3.8.3.RELEASE\\TopicClassifierDataMining\\articles";
      Analyzer analyzer = createDefaultAnalyzer();
      loadData(index, articlesPath, analyzer);

      try {
         IndexWriter writer =
               new IndexWriter(FSDirectory.open(new File(articlesPath)
                     .toPath()), new IndexWriterConfig(analyzer));
         IndexReader reader = DirectoryReader.open(writer, true, true);
         TermStats[] commonTerms = HighFreqTerms.getHighFreqTerms(reader, 30,
               null, new HighFreqTerms.DocFreqComparator());
         for (TermStats commonTerm : commonTerms) {
            System.out.println(commonTerm.termtext.utf8ToString());
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      HashMap<String, IndexContainerImpl> forBayesRaychev = new HashMap<>();
      String[] topics = new String[] { "sport", "science" };
      for (String topic : topics) {
         IndexContainerImpl indexContainer = new IndexContainerImpl(
               new ArrayList<>());
         try {
            LuceneInvertedIndex idx = new LuceneInvertedIndex();
            idx.indexFile(new File("articles/" + topic + ".txt"));
            for (Map.Entry<String, List<LuceneInvertedIndex.Tuple>> a : idx.index
                  .entrySet()) {
               indexContainer.add(new StrictTokenAnalyzerImpl(a.getKey(), a
                     .getValue().size()));
            }
         } catch (Exception e) {
            e.printStackTrace();
         }

         indexContainer.sort();
         indexContainer.print();

         forBayesRaychev.put(topic, indexContainer);
      }

      classifier(forBayesRaychev);
   }

   private static void classifier(
         HashMap<String, IndexContainerImpl> forBayesRaychev) {

      NaiveBayes algorithmMasterpiece = new NaiveBayes();
      Map<String, String[]> tokens = new HashMap<String, String[]>();
      for (Map.Entry<String, IndexContainerImpl> entry : forBayesRaychev
            .entrySet()) {
         String[] words = new String[entry
               .getValue().strictTokenAnalyzerImpls.size()];
         int i = 0;
         for (StrictTokenAnalyzerImpl token : entry
               .getValue().strictTokenAnalyzerImpls) {
            words[i++] = token.word;
         }
         tokens.put(entry.getKey(), words);
      }
      try {
         Spider sportSpider = new Spider();
         sportSpider.search("http://www.sportingnews.com/");
         sportSpider.getTextCrawled();
         FileWriter fileWriter = new FileWriter("articles/sport.txt");
         fileWriter.write(sportSpider.getTextCrawled());

         Spider scienceSpider = new Spider();
         scienceSpider.search("http://www.iflscience.com/");
         System.out.println(sportSpider.getTextCrawled());
         fileWriter = new FileWriter("articles/science.txt");
         fileWriter.write(scienceSpider.getTextCrawled());

         testSpider = new Spider();
         testSpider.search(
               "http://fantasygames.nascar.com/articles/2018-daytona-500-in-76-days", 1);
      } catch (Throwable e) {
         //?
      }
      Map<String, String[]> tokens2 = new HashMap();
      for (Map.Entry<String, String[]> entry : tokens.entrySet()) {
         tokens2.put(entry.getKey(), new String[] { String.join(" ", entry
               .getValue()) });
      }

      algorithmMasterpiece.setChisquareCriticalValue(1.2);
      algorithmMasterpiece.train(tokens2);
      NaiveBayesKnowledgeBase knowledgeBase = algorithmMasterpiece
            .getKnowledgeBase();
      NaiveBayes algorithmMasterpiece2 = new NaiveBayes(knowledgeBase);
      String articleToPredict = testSpider.getTextCrawled();
      String topic = algorithmMasterpiece2.predict(articleToPredict);
      System.out.println("PREDICTION: " + topic);
   }
}

class StrictTokenAnalyzerImpl extends Object {
   public String word;
   public int occurance;

   public StrictTokenAnalyzerImpl(String word, int occurrence) {
      this.word = word;
      this.occurance = occurrence;
   }
}

class IndexContainerImpl extends Object {
   public ArrayList<StrictTokenAnalyzerImpl> strictTokenAnalyzerImpls;

   public IndexContainerImpl(
         ArrayList<StrictTokenAnalyzerImpl> strictTokenAnalyzerImpls) {
      this.strictTokenAnalyzerImpls = strictTokenAnalyzerImpls;
   }

   public void add(StrictTokenAnalyzerImpl strictTokenAnalyzerImpl) {
      strictTokenAnalyzerImpls.add(strictTokenAnalyzerImpl);
   }

   public void sort() {
      strictTokenAnalyzerImpls.sort((o1, o2) -> {
         if (o1.occurance < o2.occurance)
            return 1;
         else if (o1.occurance > o2.occurance)
            return -1;
         else
            return 0;
      });
   }

   public void print() {
      for (StrictTokenAnalyzerImpl strictTokenAnalyzerImpl : strictTokenAnalyzerImpls) {
         System.out.println(
               "The word \"" + strictTokenAnalyzerImpl.word + "\" is found "
                     + strictTokenAnalyzerImpl.occurance
                     + " times");
      }
   }
}
