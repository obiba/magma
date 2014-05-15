package org.obiba.magma.datasource.generated;

import java.util.Random;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;

public class TextValueGenerator extends GeneratedVariableValueSource {

  private static final LoremIpsum LOREM_IPSUM = LoremIpsum.getInstance();

  public TextValueGenerator(Variable variable) {
    super(variable);
  }

  @Override
  protected Value nextValue(Variable variable, GeneratedValueSet valueSet) {
    return valueSet.dataGenerator.nextInt(0, 1) == 0
        ? variable.getValueType().nullValue()
        : variable.getValueType().valueOf(
            LOREM_IPSUM.paragraphs(valueSet.dataGenerator.nextInt(0, 1), valueSet.dataGenerator.nextInt(0, 1) == 0));
  }

  /*
   * https://github.com/oliverdodd/jlorem/blob/master/src/main/java/net/_01001111/text/LoremIpsum.java
   *
   * Copyright 2010 Oliver C Dodd http://01001111.net
   * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
   */
  @SuppressWarnings("SpellCheckingInspection")
  private static class LoremIpsum {

    /*
     * The Lorem Ipsum Standard Paragraph
     */
    private static final String STANDARD =
        "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
            "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    private static final String[] WORDS = { "a", "ac", "accumsan", "ad", "adipiscing", "aenean", "aliquam", "aliquet",
        "amet", "ante", "aptent", "arcu", "at", "auctor", "augue", "bibendum", "blandit", "class", "commodo",
        "condimentum", "congue", "consectetur", "consequat", "conubia", "convallis", "cras", "cubilia", "cum",
        "curabitur", "curae", "cursus", "dapibus", "diam", "dictum", "dictumst", "dignissim", "dis", "dolor", "donec",
        "dui", "duis", "egestas", "eget", "eleifend", "elementum", "elit", "enim", "erat", "eros", "est", "et", "etiam",
        "eu", "euismod", "facilisi", "facilisis", "fames", "faucibus", "felis", "fermentum", "feugiat", "fringilla",
        "fusce", "gravida", "habitant", "habitasse", "hac", "hendrerit", "himenaeos", "iaculis", "id", "imperdiet",
        "in", "inceptos", "integer", "interdum", "ipsum", "justo", "lacinia", "lacus", "laoreet", "lectus", "leo",
        "libero", "ligula", "litora", "lobortis", "lorem", "luctus", "maecenas", "magna", "magnis", "malesuada",
        "massa", "mattis", "mauris", "metus", "mi", "molestie", "mollis", "montes", "morbi", "mus", "nam", "nascetur",
        "natoque", "nec", "neque", "netus", "nibh", "nisi", "nisl", "non", "nostra", "nulla", "nullam", "nunc", "odio",
        "orci", "ornare", "parturient", "pellentesque", "penatibus", "per", "pharetra", "phasellus", "placerat",
        "platea", "porta", "porttitor", "posuere", "potenti", "praesent", "pretium", "primis", "proin", "pulvinar",
        "purus", "quam", "quis", "quisque", "rhoncus", "ridiculus", "risus", "rutrum", "sagittis", "sapien",
        "scelerisque", "sed", "sem", "semper", "senectus", "sit", "sociis", "sociosqu", "sodales", "sollicitudin",
        "suscipit", "suspendisse", "taciti", "tellus", "tempor", "tempus", "tincidunt", "torquent", "tortor",
        "tristique", "turpis", "ullamcorper", "ultrices", "ultricies", "urna", "ut", "varius", "vehicula", "vel",
        "velit", "venenatis", "vestibulum", "vitae", "vivamus", "viverra", "volutpat", "vulputate" };

    private static final String[] PUNCTUATION = { ".", "?" };

    private static final String EOL = System.getProperty("line.separator");

    private static final Random RANDOM = new Random();

    public static LoremIpsum getInstance() {
      return new LoremIpsum();
    }

    private LoremIpsum() {
    }

    /**
     * Get a RANDOM word
     */
    public String randomWord() {
      return WORDS[RANDOM.nextInt(WORDS.length - 1)];
    }

    /**
     * Get a RANDOM PUNCTUATION mark
     */
    public String randomPunctuation() {
      return PUNCTUATION[RANDOM.nextInt(PUNCTUATION.length - 1)];
    }

    /**
     * Get a string of WORDS
     *
     * @param count - the number of WORDS to fetch
     */
    @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
    public String words(int count) {
      StringBuilder s = new StringBuilder();
      while(count-- > 0) s.append(randomWord()).append(" ");
      return s.toString().trim();
    }

    /**
     * Get a sentence fragment
     */
    public String sentenceFragment() {
      return words(RANDOM.nextInt(10) + 3);
    }

    /**
     * Get a sentence
     */
    public String sentence() {
      // first word
      String w = randomWord();
      StringBuilder s = new StringBuilder(w.substring(0, 1).toUpperCase()).append(w.substring(1)).append(" ");
      // commas?
      if(RANDOM.nextBoolean()) {
        int r = RANDOM.nextInt(3) + 1;
        for(int i = 0; i < r; i++)
          s.append(sentenceFragment()).append(", ");
      }
      // last fragment + PUNCTUATION
      return s.append(sentenceFragment()).append(randomPunctuation()).toString();
    }

    /**
     * Get multiple sentences
     *
     * @param count - the number of sentences
     */
    @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
    public String sentences(int count) {
      StringBuilder s = new StringBuilder();
      while(count-- > 0) s.append(sentence()).append("  ");
      return s.toString().trim();
    }

    /**
     * Get a paragraph
     *
     * @useStandard - get the STANDARD Lorem Ipsum paragraph?
     */
    public String paragraph(boolean useStandard) {
      return useStandard ? STANDARD : sentences(RANDOM.nextInt(3) + 2);
    }

    public String paragraph() {
      return paragraph(false);
    }

    /**
     * Get multiple paragraphs
     *
     * @param count - the number of paragraphs
     * @useStandard - begin with the STANDARD Lorem Ipsum paragraph?
     */
    @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
    public String paragraphs(int count, boolean useStandard) {
      if(count == 0) return null;

      StringBuilder s = new StringBuilder();
      while(count-- > 0) {
        s.append(paragraph(useStandard)).append(EOL).append(EOL);
        useStandard = false;
      }
      return s.toString().trim();
    }

    public String paragraphs(int count) {
      return paragraphs(count, false);
    }
  }

}
