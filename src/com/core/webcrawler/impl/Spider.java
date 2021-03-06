package com.core.webcrawler.impl;

import com.core.containers.impl.HtmlContainer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Spider {
   private static final int MAX_PAGES_TO_SEARCH = 10;
   private Set<String> pagesVisited = new HashSet<String>();
   private List<String> pagesToVisit = new LinkedList<String>();
   private HtmlContainer container;

   public Spider() {
      container = new HtmlContainer();
   }

   public String getTextCrawled() {
      return container.getAllText();
   }

   public Set<String> getUrlsCrawled() {
      return container.getAllUrls();
   }

   /**
    * Our main launching point for the Spider's functionality. Internally it
    * creates spider legs
    * that make an HTTP request and parse the response (the web page).
    * 
    * @param url
    *           - The starting point of the spider
    */
   public void search(String url) {
      search(url, MAX_PAGES_TO_SEARCH);
   }

   public void search(String url, int crawlDeepness) {
      this.pagesToVisit.add(url);
      while (this.pagesVisited.size() < crawlDeepness && !this.pagesToVisit.isEmpty()) {
         String currentUrl = this.nextUrl();
         SpiderHelper processor = new SpiderHelper(container);
         processor.crawl(currentUrl);
         this.pagesVisited.add(currentUrl);
         this.pagesToVisit.addAll(processor.getLinks());
      }
      System.out.println("\n**Done** Visited " + this.pagesVisited.size()
            + " web page(s)");
   }

   /**
    * Returns the next URL to visit (in the order that they were found). We also
    * do a check to make
    * sure this method doesn't return a URL that has already been visited.
    * 
    * @return
    */
   private String nextUrl() {
      String nextUrl = null;
      do {
         if (!this.pagesToVisit.isEmpty()) {
            nextUrl = this.pagesToVisit.remove(0);
         }
      } while (this.pagesVisited.contains(nextUrl));

      return nextUrl;
   }
}