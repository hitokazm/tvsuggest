/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import java.io.BufferedReader
import java.io.Reader
import java.io.StringReader
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.WhitespaceTokenizer


class YahooAnalyzer extends Analyzer {
  val buf = new StringBuffer
  val buf2 = new StringBuffer
  var pos = -1

  def tokenStream(fieldName: String, reader: Reader): TokenStream = {
    if (buf.length == 0) {
      val br = new BufferedReader(reader);
      var line = ""
      while (line != null) {
        buf.append(line)
        buf.append(' ')
        line = br.readLine
      }
    }
    val words = Yahoo.getWords(buf.toString)
    for (w <- words) {
      buf2.append(w)
      buf2.append(' ')
    }
    new WhitespaceTokenizer(new StringReader(buf2.toString))
  }
}
