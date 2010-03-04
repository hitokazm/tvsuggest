/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import java.io.StringReader
import org.apache.lucene.analysis.tokenattributes.TermAttribute
import org.apache.lucene.util.Version

object TinySegmenter {

  def getWords(doc: String): Array[String] = {
    val seg = new net.moraleboost.lucene.analysis.ja.TinySegmenterAnalyzer(Version.LUCENE_CURRENT)
    .tokenStream("xml", new StringReader(doc))

    var list = new scala.collection.mutable.ListBuffer[String]
    while (seg.incrementToken) {
      list += seg.getAttribute(classOf[TermAttribute]).term
    }
    list.toArray
  }
}
