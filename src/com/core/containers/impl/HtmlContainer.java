package com.core.containers.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HtmlContainer {
   private Map<String, String> urlToTextBodyContainer;

   public HtmlContainer() {
      urlToTextBodyContainer = new HashMap<>();
   }

   public void add(String url, String textBody) {
      urlToTextBodyContainer.put(url, textBody);
   }

   public String get(String url) {
      return urlToTextBodyContainer.get(url);
   }

   public String getAllText() { //per website?
      StringBuilder helper = new StringBuilder();
      for (String urlText : urlToTextBodyContainer.values()) {
         helper.append(urlText).append(
               " ");/* append something unique to be able
                     * to distinguish when new text body is
                     * appended??*/
      }
      return helper.toString();
   }

   public Set<String> getAllUrls() {
      return urlToTextBodyContainer.keySet();
   }
}
