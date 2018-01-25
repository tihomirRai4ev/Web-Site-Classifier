package com.core.dataanalyzer;

import java.util.ArrayList;

class IndexContainer {
   public ArrayList<TokenIndex> tokenIndex;

   public IndexContainer(
         ArrayList<TokenIndex> tokenIndex) {
      this.tokenIndex = tokenIndex;
   }

   public void add(TokenIndex tokenIndex) {
      this.tokenIndex.add(tokenIndex);
   }

   public void sort() {
      tokenIndex.sort((o1, o2) -> Integer.compare(o2.occurrence, o1.occurrence));
   }

   public String[] getTop50Percent() {
      sort();
      String[] top = new String[tokenIndex.size() / 2];

      for (int i = 0; i < tokenIndex.size() / 2; ++i) {
         top[i] = tokenIndex.get(i).word;
      }

      return top;
   }

   public void print() {
      for (TokenIndex tokenIndex : this.tokenIndex) {
         System.out.println(
               "The word \"" + tokenIndex.word + "\" is found "
                     + tokenIndex.occurrence
                     + " times + \n");
      }
   }

   static class TokenIndex {
      public String word;
      public int occurrence;

      public TokenIndex(String word, int occurrence) {
         this.word = word;
         this.occurrence = occurrence;
      }
   }
}
