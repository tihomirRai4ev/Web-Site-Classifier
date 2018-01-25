package com.core.dataanalyzer;

import com.core.dataanalyzer.lucene.InvertedIndex;
import com.core.webcrawler.impl.Spider;
import com.datumbox.opensource.classifiers.NaiveBayes;
import com.datumbox.opensource.dataobjects.NaiveBayesKnowledgeBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class App {
   private static Spider testSpider;

   public static void main(String[] args) throws IOException {
      HashMap<String, String[]> indexStructure = new HashMap<>();
      String[] topics = new String[] { "sport", "science" };

      for (String topic : topics) {
         IndexContainer indexContainer = new IndexContainer(new ArrayList<>());
         InvertedIndex idx = new InvertedIndex();
         idx.indexFile(new File("articles/" + topic + ".txt"));

         for (Map.Entry<String, List<InvertedIndex.Tuple>> a : idx.index.entrySet()) {
            indexContainer.add(new IndexContainer.TokenIndex(a.getKey(), a.getValue().size()));
         }

         System.out.println(topic + ":");
         indexContainer.print();
         indexStructure.put(topic, indexContainer.getTop50Percent());
      }

      classify(indexStructure);
   }

   private static void classify(HashMap<String, String[]> tokens) {
      NaiveBayes classifier = new NaiveBayes();

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
         tokens2.put(entry.getKey(), new String[] { String.join(" ", entry.getValue()) });
      }

      classifier.setChisquareCriticalValue(1.2);
      classifier.train(tokens2);
      NaiveBayesKnowledgeBase knowledgeBase = classifier.getKnowledgeBase();
      NaiveBayes algorithmMasterpiece2 = new NaiveBayes(knowledgeBase);
      String articleToPredict = testSpider.getTextCrawled();
      String topic = algorithmMasterpiece2.predict(articleToPredict);
      System.out.println("PREDICTION: " + topic);
   }
}

