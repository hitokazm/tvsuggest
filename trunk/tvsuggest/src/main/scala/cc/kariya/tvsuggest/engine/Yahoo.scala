/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import cc.kariya.tvsuggest.ui.UILogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import scala.io.Source
import scala.xml.XML
import scala.xml.parsing.ConstructingParser


object Yahoo extends UILogger {
  
  val appid = "p.mKOpOxg65Mx6ut2Kx1q5QLziw_v7THAb4elPCO0SSXOJUWQXAeYCgGsm1XroSjsg--"
  
  def getKeyword(doc: String) : Array[String] = {
    val url = new URL(
      "http://jlp.yahooapis.jp/KeyphraseService/V1/extract?appid=%s&sentence=%s"
      .format(appid, URLEncoder.encode(doc, "UTF-8")))
    val conn = url.openConnection
    val in = conn.getInputStream
    val reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))
    var line = ""
    var lines = ""
    while (line != null) {
      if (!line.startsWith("<?xml")) {
        lines += line + "\n"
      }
      line = reader.readLine
    }
    val xml = XML.loadString(lines)

   //log(xml \\ "Keyphrase" map {x => x.text} toString)

    xml \\ "Keyphrase" map {x => x.text.toString} toArray
  }

  def getWords(doc: String): Seq[String] = {
    val url = new URL(
      "http://jlp.yahooapis.jp/MAService/V1/parse?appid=%s&results=ma&filter=1|2|3|4|5|6|7|8|9|10|11|12|13&response=baseform&sentence=%s"
      .format(appid, URLEncoder.encode(doc, "UTF-8")))
    val reader = new BufferedReader(new InputStreamReader(url.openConnection.getInputStream, "UTF-8"))
    var line = ""
    val buf = new StringBuffer
    while (line != null) {
      buf.append(line)
      line = reader.readLine
    }
    val xml = XML.loadString(buf.toString.replaceFirst("<\\?xml.*\\?>", ""))
    xml \\ "baseform" map {x => x.text.toString}
  }

}
