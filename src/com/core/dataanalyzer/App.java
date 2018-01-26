package com.core.dataanalyzer;

import com.core.dataanalyzer.lucene.InvertedIndex;
import com.core.naivebaiseclassifier.classifiers.NaiveBayes;
import com.core.webcrawler.impl.Spider;
import com.datumbox.opensource.dataobjects.NaiveBayesKnowledgeBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
   public static Topic[] topics = new Topic[] {
         new Topic("sport", "http://www.sportingnews.com/"),
         new Topic("science", "https://www.sciencenews.org/"),
         new Topic("music", "https://www.npr.org/music/"),
         new Topic("movies", "http://www.imdb.com"),
         new Topic("economics", "http://www.bbc.com"),
         new Topic("jobs", "http://www.indeed.com")
   };

   public static void main(String[] args) throws IOException {
      crawl(topics);
      System.out.println("CLASSIFICATION: " + classifyArticle(topics,
            "http://www.imdb.com/title/tt4500922/?pf_rd_m=A2FGELUUNOQJNL&pf_rd_p=2750721702&pf_rd_r=1WJCFJF4JBPT696JX7BR&pf_rd_s=right-2&pf_rd_t=15061&pf_rd_i=homepage&ref_=hm_otw_t0",
            true));
   }

   public static void crawl(Topic[] topics) throws IOException {
      for (Topic topic : topics) {
         Spider spider = new Spider();
         spider.search(topic.site);
         spider.getTextCrawled();
         FileWriter fileWriter = new FileWriter("articles/" + topic.name + ".txt");
         fileWriter.write(spider.getTextCrawled());
      }
   }

   public static String classifyArticle(Topic[] topics, String article, boolean print) {
      HashMap<String, String[]> indexStructure = new HashMap<>();

      try {
         for (Topic topic : topics) {
            IndexContainer indexContainer = new IndexContainer(new ArrayList<>());
            InvertedIndex idx = new InvertedIndex();
            idx.indexFile(new File("articles/" + topic.name + ".txt"));

            for (Map.Entry<String, List<InvertedIndex.Tuple>> a : idx.index.entrySet()) {
               indexContainer.add(new IndexContainer.TokenIndex(a.getKey(), a.getValue().size()));
            }

            if (print) {
               System.out.println(topic.name + ":");
               indexContainer.print();
            }

            indexStructure.put(topic.name, indexContainer.getTop50Percent());
         }
      } catch (Exception e) {
         throw new RuntimeException("Fail!", e);
      }

      return classify(unify(indexStructure), getArticleText(article));
   }

   public static String getArticleText(String url) {
      Spider testSpider = new Spider();
      testSpider.search(url, 1);
      return testSpider.getTextCrawled();
   }

   public static Map<String, String[]> unify(Map<String, String[]> indexStructure) {
      Map<String, String[]> tokens = new HashMap<>();
      for (Map.Entry<String, String[]> entry : indexStructure.entrySet()) {
         tokens.put(entry.getKey(), new String[] { String.join(" ", entry.getValue()) });
      }
      return tokens;
   }

   private static String classify(Map<String, String[]> tokens, String articleToClassify) {
      NaiveBayes classifier = new NaiveBayes();
      classifier.setChisquareCriticalValue(1.2);
      classifier.train(tokens);
      NaiveBayesKnowledgeBase knowledgeBase = classifier.getKnowledgeBase();
      NaiveBayes algorithmMasterpiece2 = new NaiveBayes(knowledgeBase);
      return algorithmMasterpiece2.predict(articleToClassify);
   }
}

class Topic {
   public String name;
   public String site;

   public Topic(String name, String site) {
      this.name = name;
      this.site = site;
   }
}
