package com.core.webcrawler.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Analyzer {
   /**
    * Our Mining entry point.
    *
    * @param args
    */
   public static void main(String[] args) throws IOException {
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
   }
}
