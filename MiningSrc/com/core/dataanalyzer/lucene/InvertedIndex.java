package com.core.dataanalyzer.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvertedIndex {
   public Map<String, List<Tuple>> index = new HashMap<String, List<Tuple>>();
   public List<String> files = new ArrayList<String>();

   public void indexFile(File file) throws IOException {
      int fileNumber = files.indexOf(file.getPath());
      if (fileNumber == -1) {
         files.add(file.getPath());
         fileNumber = files.size() - 1;
      }

      int pos = 0;
      BufferedReader reader = new BufferedReader(new FileReader(file));
      for (String line = reader.readLine(); line != null; line = reader
            .readLine()) {
         for (String _word : line.split("\\W+")) {
            String word = _word.toLowerCase();
            pos++;
            if (stopwords.contains(word))
               continue;
            List<Tuple> idx = index.get(word);
            if (idx == null) {
               idx = new LinkedList<>();
               index.put(word, idx);
            }
            idx.add(new Tuple(fileNumber, pos));
         }
      }
      System.out.println("indexed " + file.getPath() + " " + pos + " words");
   }

   public void search(List<String> words) {
      for (String _word : words) {
         Set<String> answer = new HashSet<String>();
         String word = _word.toLowerCase();
         List<Tuple> idx = index.get(word);
         if (idx != null) {
            for (Tuple t : idx) {
               answer.add(files.get(t.fileno));
            }
         }
         System.out.print(word);
         for (String f : answer) {
            System.out.print(" " + f);
         }
         System.out.println("");
      }
   }

   public class Tuple {
      public int fileno;
      public int position;

      public Tuple(int fileno, int position) {
         this.fileno = fileno;
         this.position = position;
      }
   }

   /* java -cp bin org.traychev&lkanev^2.InvertedIndex "huntsman,merit,dog,the,gutenberg,lovecraft,olympian" lakenv.txt pg7025.txt pg82.txt pg9090.tx. */List<String> stopwords = Arrays.asList("a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your");
}