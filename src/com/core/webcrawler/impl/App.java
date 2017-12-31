package com.core.webcrawler.impl;

public class App {
   /**
    * Our Mining entry point.
    *
    * @param args
    */
   public static void main(String[] args) {
      Spider sportSpider = new Spider();
      sportSpider.search("https://www.sportal.bg/");//:D
      sportSpider.getTextCrawled();

      Spider scienceSpider = new Spider();
      scienceSpider.search("http://www.iflscience.com/");
      scienceSpider.getTextCrawled();
   }
}
