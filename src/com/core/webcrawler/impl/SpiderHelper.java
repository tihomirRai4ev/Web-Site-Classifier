package com.core.webcrawler.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.core.containers.impl.HtmlContainer;

public class SpiderHelper {

   private static final String USER_AGENT =
         "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
   private List<String> links = new LinkedList<String>();
   private HtmlContainer container;

   public SpiderHelper(HtmlContainer container) {
      this.container = container;
   }

   /**
    * This performs all the work. It makes an HTTP request, checks the response,
    * and then gathers
    * up all the links on the page. Perform a searchForWord after the successful
    * crawl
    * 
    * @param url
    *           - The URL to visit
    * @return whether or not the crawl was successful
    */
   public boolean crawl(String url) {
      try {
         // Manual I used: https://jsoup.org/cookbook/extracting-data/attributes-text-html
         Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
         Document htmlDocument = connection.get();
         container.add(url, htmlDocument.body().text());
         //TODO: need to have re-try logic if connection is not OK (200).
         if (connection.response().statusCode() == 200) {
            System.out.println("\n**Visiting** Received web page at " + url);
         }
         // Not sure if we need this below:
         if (!connection.response().contentType().contains("text/html")) {
            System.out.println(
                  "**Failure** Retrieved something other than HTML");
            return false;
         }
         Elements linksOnPage = htmlDocument.select("a[href]");
         System.out.println("Found (" + linksOnPage.size() + ") links");
         for (Element link : linksOnPage) {
            this.links.add(link.absUrl("href"));
         }
         return true;
      } catch (IOException ioe) {
         // We were not successful in our HTTP request
         return false;
      }
   }

   public List<String> getLinks() {
      return this.links;
   }

}