/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import cc.kariya.tvsuggest.engine.db.ChannelDB
import cc.kariya.tvsuggest.engine.db.ProgrammeDB
import cc.kariya.tvsuggest.grabber.AbstractProgramme
import cc.kariya.tvsuggest.ui.UILogger
import cc.kariya.tvsuggest.util.DateUtil
import java.io.File
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import net.moraleboost.lucene.analysis.ja.TinySegmenterAnalyzer
import org.apache.lucene.analysis.cjk.CJKAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.similar.MoreLikeThis
import org.apache.lucene.search.spell.LuceneDictionary
import org.apache.lucene.search.spell.SpellChecker
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.util.Version
import scala.xml.XML


object Lucene  extends UILogger {

  var indexWriter: IndexWriter = null

  val dir = new SimpleFSDirectory(new File("cjkindex"))
  val analyzer = new CJKAnalyzer(Version.LUCENE_CURRENT)
  //val dir = new SimpleFSDirectory(new File("yahooindex"))
  //val analyzer = new YahooAnalyzer()
  //val dir = new SimpleFSDirectory(new File("tinyindex"))
  //val analyzer = new TinySegmenterAnalyzer(Version.LUCENE_CURRENT)

  def store(p: AbstractProgramme): Unit = {
    val doc = new Document
    doc.add(new Field("id",  p.id.toString,    Field.Store.YES, Field.Index.NOT_ANALYZED))
    doc.add(new Field("xml", p.toXml.toString, Field.Store.NO,  Field.Index.ANALYZED))
    indexWriter.addDocument(doc)
  }

  def begin = (indexWriter = new IndexWriter(dir,analyzer,IndexWriter.MaxFieldLength.UNLIMITED))
  def close() = indexWriter.close
  def commit = indexWriter.commit
  def optimize = indexWriter.optimize

  def delete(progId: Int) = {
    //log("deleting " + progId)

    val qp = new QueryParser(Version.LUCENE_CURRENT, "id", analyzer)
    val query = qp.parse("id:%d".format(progId))
    indexWriter.deleteDocuments(query)
  }

  def search(s: String): Array[Array[AnyRef]] = {
    log("Searching for: " + s)

    val qp = new QueryParser(Version.LUCENE_CURRENT, "xml", analyzer)
    val q = qp.parse(s)
    search_common(q)
  }

  def search_common(q: Query): Array[Array[AnyRef]] = {
    val indexSearcher = new IndexSearcher(dir)
    val hs = indexSearcher.search(q, 100)

    log("Searched. %d hits".format(hs.totalHits))

    val ar = hs.scoreDocs map {
      sd =>
      val doc = indexSearcher.doc(sd.doc)
      val progId = doc.getField("id").stringValue.toInt
      val xml_string = ProgrammeDB.select_xml(progId)
      val start = ProgrammeDB.select_start(progId)
      val stop = ProgrammeDB.select_stop(progId)
      val channelId = ProgrammeDB.select_channelId(progId)
      val xml = XML.loadString(xml_string)
      Array(
        progId.asInstanceOf[AnyRef],
        0.asInstanceOf[AnyRef],
        DateUtil.format(start, "yyyyMMddHHmm", "yyyy/MM/dd(E) HH:mm"),
        DateUtil.format(stop, "yyyyMMddHHmm", "HH:mm"),
        ChannelDB.getChannelName(channelId),
        xml \ "title" text,
        xml \ "desc" text )
    }
    indexSearcher.close
    ar
  }

  def searchMoreLikeThis(s: String): Array[Array[AnyRef]] = {
    log("Searching more like: " + s)

    val indexReader = IndexReader.open(dir)

    val mlt = new MoreLikeThis(indexReader)
    mlt.setAnalyzer(analyzer)
    mlt.setFieldNames(Array("xml"))
    val q = mlt.like(new StringReader(s))

//    val q = new MoreLikeThisQuery(s, Array("xml"), new CJKAnalyzer(Version.LUCENE_CURRENT))

    search_common(q)
  }

  def spellcheck(s: String) = {
    val indexReader = IndexReader.open(dir)
    val spellchecker = new SpellChecker(dir)
    spellchecker.indexDictionary(new LuceneDictionary(indexReader, "xml"))
    val suggestions = spellchecker.suggestSimilar(s, 5)
    indexReader.close

    suggestions
  }
}
