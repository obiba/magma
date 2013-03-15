package org.obiba.magma.datasource.csv;

/**
 * This index entry into a csv file provides a byte offset to the start and end of a portion of the file. Usually a
 * single record.
 */
public class CsvIndexEntry {

  private final long start;

  private final long end;

  CsvIndexEntry(long start, long end) {
    this.start = start;
    this.end = end;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  @Override
  public String toString() {
    return "CsvIndexEntry [start=" + start + ", end=" + end + "]";
  }

}
