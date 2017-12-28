package com.core.webcrawler.impl;

public class App {
   /**
    * Our Mining entry point.
    *
    * @param args
    */
   public static void main(String[] args) {
      Spider spider = new Spider();
      spider.search("https://www.sportal.bg/");//:D
      spider.getTextCrawled();
   }
}
