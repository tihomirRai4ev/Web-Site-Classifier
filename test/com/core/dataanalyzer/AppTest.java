package com.core.dataanalyzer;

import com.core.webcrawler.impl.Spider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppTest {
   public int calculateSuccessRate(boolean crawl) {
      int failedClassifications = 0;
      int allClassification = 0;
      Topic topics[] = new Topic[] {
            new Topic("music", "http://www.music-news.com/"),
            new Topic("science", "https://www.sciencenews.org/"),
            new Topic("sport", "http://www.sportingnews.com/"),
      };

      for (
            Topic topic : topics)

      {
         Spider testingSpider = new Spider();
         testingSpider.search(topic.site);

         for (String articleUrl : testingSpider.getUrlsCrawled()) {
            String classification = App.classifyArticle(articleUrl, crawl, false);
            System.out.print("expected:<[" + topic.name + "]> \tactual:<[" + classification + "]>");

            try {
               assertEquals(topic.name, classification);
            } catch (Throwable e) {
               System.out.print("\t - fail");
               failedClassifications++;
            }
            System.out.println();
            allClassification++;
         }
      }

      return 100 - 100 * failedClassifications / allClassification;
   }

   @Test
   public void testSuccessfulClassificationMoreThan50Percent() {
      int successRate = calculateSuccessRate(true);
      System.out.println("Success rate: " + successRate + "%");
      assertTrue("Success rate (" + successRate + ") is more than 50%", successRate > 50);
   }

   @Test
   public void longTest() {
      for (int i = 0; i < 30; ++i) {
         try {
            testSuccessfulClassificationMoreThan50Percent();
            Thread.sleep(1000 * 25 * 60);
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }
   }
}
