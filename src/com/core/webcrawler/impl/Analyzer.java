package com.core.webcrawler.impl;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

      Spider scienceSpider = new Spider();
      scienceSpider.search("http://www.iflscience.com/");

      System.out.println(sportSpider.getTextCrawled());
      FileWriter fileWriter = new FileWriter("sport.txt");
      fileWriter.write(sportSpider.getTextCrawled());
      File hoi = new File("20news-18828/sport.txt");

   }
}
