/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.grabber.sp3

import cc.kariya.tvsuggest.grabber.AbstractProgramme
import cc.kariya.tvsuggest.grabber.IPlugin
import cc.kariya.tvsuggest.ui.UILogger
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import net.htmlparser.jericho._
import scala.collection.JavaConversions._


object Grab extends IPlugin with UILogger {

  def getPluginName = "SP3"

  def description(link: String) = {
    val url = "http://www.sp3.com" + link
    //val src = new Source(new InputStreamReader(new URL(url).openStream, "Windows-31J"))
    val src = new Source(new URL(url))

    src.getTextExtractor.toString
    .replaceAll("＜ＰＲ＞.*＜／ＰＲ＞", "")
  }

  def getChannels() ={
    val ids = List(
  /**/
      "251", "252", "254", "257", "261", "266", "268", "281", "283", "300",
      "303", "306", "307", "318", "320", "343", "362", "372", "602", "607",
      "630", "631", "632", "639", "640", "641", "642", "643", "647", "650",
      "651", "652", "656", "659", "668", "669", "670", "674", "675", "676",
      "687", "707", "730", "746", "747", "749"
    )
    val names = ids

    var m = new scala.collection.immutable.HashMap[String, List[String]]
    for ((id, name) <- ids.zip(names)) {
      m += (id -> List(name))
    }
    m
  }

  def getProgrammes(channelId: String, channelNames: List[String]): List[AbstractProgramme] = {
    log("Fetching channel: " + channelId)

    var table: List[Programme] = List()
    for (i <- 0 to 6) {
//      log("Fetching for day: " + i)
      table :::= getDailyTable(channelId, channelNames, i)
    }
    log("Sorting...")
    val sorted = sortByDateTime(table)
    log("Refining...")
    val refined = refineEndTime(sorted)
    log("Done")
    refined: List[Programme]
  }

  def getDailyTable(channelId: String, channelNames: List[String], offset: Int): List[Programme] = {
    def extractDate(src: Source): String = {
      val date = src.getAllElements(HTMLElementName.B)(0).getContent.getTextExtractor.toString.trim
      val year = 2010
      val month = (date.replaceFirst("^\\[([0-9]+)/([0-9]+)[^0-9].*\\]$", "$1"))
      val day = (date.replaceFirst("^\\[([0-9]+)/([0-9]+)[^0-9].*\\]$", "$2"))
      val MM = month.toInt.formatted("%02d")
      val DD = day.toInt.formatted("%02d")
      year.formatted("%04d") + MM + DD
    }
    val url = "http://www.sp3.com/cgi-bin/bean.pl?rf=0&ofs=" + offset + "&ch=" + channelNames.head
    //val src = new Source(new InputStreamReader(new URL(url).openStream, "Windows-31J"))
    val src = new Source(new URL(url))

    val date = extractDate(src)
    val nextDate = {
      val formatter = new SimpleDateFormat()
      formatter.applyPattern("yyyyMMdd")
      val today = formatter.parse(date)
      val cal = new GregorianCalendar()
      cal.setTime(today)
      cal.add(Calendar.DATE, 1)
      formatter.format(cal.getTime)
    }

    src.getAllElements(HTMLElementName.TD)
    .filter(_.getContent.getTextExtractor.toString.matches("[0-9]{2}:[0-9]{2}.*"))
    .map {
      e => val text = e.getContent.getTextExtractor.toString
           val time = text.substring(0, 5)
           val HH = time.split(":")(0).toInt.formatted("%02d")
           val MM = time.split(":")(1).toInt.formatted("%02d")
           val title = text.substring(5)

           val anchor = e.getAllElements(HTMLElementName.A)
           val link = if (anchor isEmpty) "" else anchor(0).getAttributeValue("href")

           new Programme(
             0, // id temp.
             (if (time < "0500") nextDate else date) + HH + MM,
             "", // stop
             channelId,
             0, // seq
             title,
             link)
    }.toList
  }

  def sortByDateTime(list: List[Programme]): List[Programme] = {
    list sort {(x, y) => x.channelId + x.start < y.channelId + y.start}
  }

  def refineEndTime(list: List[Programme]): List[Programme] = {
    def aux(xs:List[Programme], ys: List[Programme]): List[Programme] = {
      if (xs isEmpty) {
        val y = ys.head
        List(y.replaceStop(""))
      } else {
        val x = xs.head
        val y = ys.head
        y.replaceStop(x.start) :: aux(xs.tail, ys.tail)
      }
    }
    if (list isEmpty) List() else aux(list.tail, list)
  }
}
