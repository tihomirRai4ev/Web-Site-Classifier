package com.core.dataanalyzer;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.core.webcrawler.impl.Spider;
import com.datumbox.opensource.classifiers.NaiveBayes;
import com.datumbox.opensource.dataobjects.NaiveBayesKnowledgeBase;


public class App {
   private static Spider testSpider;

   public static void main(String[] args) {
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
               "http://fantasygames.nascar.com/articles/2018-daytona-500-in-76-days",
               1);
      } catch (Throwable e) {
         //?
      }
      Map<String, String[]> tokens2 = new HashMap<>();
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

class StrictTokenAnalyzerImpl {
   public String word;
   public int occurrence;

   public StrictTokenAnalyzerImpl(String word, int occurrence) {
      this.word = word;
      this.occurrence = occurrence;
   }
}

class IndexContainerImpl {
   public ArrayList<StrictTokenAnalyzerImpl> strictTokenAnalyzerImpls;

   public IndexContainerImpl(
         ArrayList<StrictTokenAnalyzerImpl> strictTokenAnalyzerImpls) {
      this.strictTokenAnalyzerImpls = strictTokenAnalyzerImpls;
   }

   public void add(StrictTokenAnalyzerImpl strictTokenAnalyzerImpl) {
      strictTokenAnalyzerImpls.add(strictTokenAnalyzerImpl);
   }

   public void sort() {
      strictTokenAnalyzerImpls.sort((o1, o2) -> Integer.compare(o2.occurrence, o1.occurrence));
   }

   public void print() {
      for (StrictTokenAnalyzerImpl strictTokenAnalyzerImpl : strictTokenAnalyzerImpls) {
         System.out.println(
               "The word \"" + strictTokenAnalyzerImpl.word + "\" is found "
                     + strictTokenAnalyzerImpl.occurrence
                     + " times");
      }
   }
}
