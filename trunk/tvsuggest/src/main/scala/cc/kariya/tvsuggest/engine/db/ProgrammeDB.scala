/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine.db

import cc.kariya.tvsuggest.grabber.AbstractProgramme
import cc.kariya.tvsuggest.util.DateUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import scala.xml.XML


object ProgrammeDB extends Database {

  def init = {
    log("initing DB(Programme)...")

    val statement = connection.createStatement
    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS Programme(
     Id             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
     Start          TEXT    NOT NULL,
     Stop           TEXT    NOT NULL,
     ChannelId      TEXT    NOT NULL,
     Sequence       INTEGER NOT NULL,
     Xml            TEXT    NOT NULL,
     UNIQUE (Start, Stop, ChannelId, Sequence)
);
                             """)

    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS ProgrammeNew(
     Id             INTEGER,
     Start          TEXT    NOT NULL,
     Stop           TEXT    NOT NULL,
     ChannelId      TEXT    NOT NULL,
     Sequence       INTEGER NOT NULL,
     Xml            TEXT    NOT NULL,
     PRIMARY KEY (Start, Stop, ChannelId, Sequence)
);
                            """)

  }

  def prestore() ={
    log("prestore...")
    val statement = connection.createStatement
    statement.executeUpdate("DELETE FROM ProgrammeNew;")

    // should use prepareStatement
  }

  def store(p: AbstractProgramme) = {
    //log("storing...")

    val statement = connection.createStatement

    val rs = statement.executeQuery("""
SELECT Id FROM Programme WHERE Start = '%s' AND  Stop = '%s' AND ChannelId = '%s' AND Sequence = %d;
                                    """
                                    .format(p.start, p.stop, p.channelId, p.sequence))
    if (!rs.next) {
      statement.executeUpdate("""
INSERT OR IGNORE INTO Programme
(Start, Stop, ChannelId, Sequence, Xml) VALUES
('%s', '%s', '%s', %d, '%s');
                              """.format(p.start, p.stop, p.channelId, p.sequence, p.toXml))

    } else {
      val id = rs.getInt("Id")
      //log("dup " + id)
      statement.executeUpdate("""
INSERT OR REPLACE INTO ProgrammeNew
(Id, Start, Stop, ChannelId, Sequence, Xml) VALUES
(%d, '%s', '%s', '%s', %d, '%s');
                              """.format(id, p.start, p.stop, p.channelId, p.sequence, p.toXml))
    }
  }

  def getDeleteTargets(): Seq[Int] = {
    log("getting delete targets...")

    val statement = connection.createStatement
    val rs = statement.executeQuery("SELECT Id FROM ProgrammeNew;")

    var l = new scala.collection.mutable.ListBuffer[Int]
    while (rs.next) {
      val id = rs.getInt("Id")
      l += id
    }
    l toSeq
  }

  def delete_old_programme(progId: Int) = {
    //log("deleting old data...")

    val statement = connection.createStatement
    statement.executeUpdate("DELETE FROM Programme WHERE Id = %d"
                            .format(progId))
  }

  def update_new_programmes() = {
    log("updating new data...")

    val statement = connection.createStatement
    statement.executeUpdate("REPLACE INTO Programme SELECT * FROM ProgrammeNew;")
    log("updating...")
    statement.executeUpdate("DELETE FROM ProgrammeNew;")
    log("updated")
  }

  def select_xml(progId: Int): String = {
    //log(progId.toString)

    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Xml FROM Programme WHERE Id = %d;"
      .format(progId))
    rs.getString("Xml")
  }

  def select_start(progId: Int): String = {
    //log(p.toString)

    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Start FROM Programme WHERE Id = %d;"
      .format(progId))
    rs.getString("Start")
  }

  def select_stop(progId: Int): String = {
    //log(p.toString)

    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Stop FROM Programme WHERE Id = %d;"
      .format(progId))
    rs.getString("Stop")
  }

  def select_channelId(progId: Int): String = {
    //log(p.toString)

    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT ChannelId FROM Programme WHERE Id = %d;"
      .format(progId))
    rs.getString("ChannelId")
  }

  def select_id(p: AbstractProgramme): Int = {
    return select_id(p.start, p.stop, p.channelId, p.sequence)
  }

  def select_id(start: String, stop: String, channelId: String, sequence: Int): Int = {
    //log("%s %s %s %d".format(start, stop, channelId, sequence))

    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Id FROM Programme WHERE Start = '%s' AND Stop = '%s' AND ChannelId = '%s' AND Sequence = %d;"
      .format(start, stop, channelId, sequence))
    return rs.getInt("Id")
  }

  def search(s: String): Array[String] = {
    val statement = connection.createStatement
    val rs = statement.executeQuery("SELECT xml FROM Programme WHERE xml like '%" + s + "%';")
    var l = new scala.collection.mutable.ListBuffer[String]
    while (rs.next) {
      l += rs.getString("xml")
    }
    l.toArray
  }

  def searchTimeNeighbor(s: String): Array[Array[AnyRef]] = {
    //log("Searching near: " + s)

    val (from, to) = {
      val formatter = new SimpleDateFormat()
      formatter.applyPattern("yyyyMMddHHmm")
      val now = formatter.parse(s.substring(0, 12))
      val cal1 = new GregorianCalendar()
      val cal2 = new GregorianCalendar()
      cal1.setTime(now)
      cal1.add(Calendar.MINUTE, -30)
      cal2.setTime(now)
      cal2.add(Calendar.MINUTE, 30)
      (formatter.format(cal1.getTime), formatter.format(cal2.getTime))
    }

    val statement = connection.createStatement
    val rs = statement.executeQuery("""
SELECT Id, Start, Stop, CHannelId, Sequence, Xml FROM Programme WHERE Start BETWEEN '%s' AND '%s'
                                    """
                                    .format(from, to))
    var l = new scala.collection.mutable.ListBuffer[Array[AnyRef]]
    while (rs.next) {
      val id = rs.getInt("Id")
      val start = rs.getString("Start")
      val stop = rs.getString("Stop")
      val channelId = rs.getString("channelId")
      val xml = XML.loadString(rs.getString("xml"))

      l += Array(
        rs.getInt("Id").asInstanceOf[AnyRef],
        0.asInstanceOf[AnyRef],
        DateUtil.format(start, "yyyyMMddHHmm", "yyyy/MM/dd(E) HH:mm"),
        DateUtil.format(stop, "yyyyMMddHHmm", "HH:mm"),
        ChannelDB.getChannelName(channelId),
        xml \ "title" text,
        xml \ "desc" text 
      )
    }
    return l.toArray
  }
}

