/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import cc.kariya.tvsuggest.grabber.AbstractProgramme
import cc.kariya.tvsuggest.ui.UILogger
import java.sql.DriverManager


object Database extends UILogger {

  val connection = {
    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection("jdbc:sqlite:sqlite.db")
  }

  def init() = {
    log("initing DB...")

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
CREATE TABLE IF NOT EXISTS Channel(
     Id             INTEGER NOT NULL,
     Sequence       INTEGER NOT NULL,
     Name           TEXT    NOT NULL,
     PRIMARY KEY(Id, Sequence),
     UNIQUE(Name)
);
                             """)

    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS ProgrammeNew(
     ID             INTEGER,
     Start          TEXT    NOT NULL,
     Stop           TEXT    NOT NULL,
     ChannelId      TEXT    NOT NULL,
     Sequence       INTEGER NOT NULL,
     Xml            TEXT    NOT NULL,
     PRIMARY KEY (Start, Stop, ChannelId, Sequence)
);
                            """)

  }

  def begin() = connection.setAutoCommit(false)

  def commit() = connection.commit

  def prestore() ={
    log("prestore...")
    val statement = connection.createStatement
    statement.executeUpdate("DELETE FROM ProgrammeNew;")

    // should use prepareStatement
  }

  def store(p: AbstractProgramme) = {
    //log("storing...")

    val statement = connection.createStatement
    statement.executeUpdate("""
INSERT OR IGNORE INTO Programme
(Start, Stop, ChannelId, Sequence, Xml) VALUES
('%s', '%s', '%s', %d, '%s');
                            """.format(p.start, p.stop, p.channelId, p.sequence, p.toXml))

    val rs = statement.executeQuery("SELECT changes() AS Change;")
    val change = rs.getInt("Change")
    if (change > 0) {
      statement.executeUpdate("""
INSERT OR REPLACE INTO ProgrammeNew
(Id, Start, Stop, ChannelId, Sequence, Xml) VALUES
((SELECT Id FROM Programme WHERE Start = '%s' AND Stop = '%s' AND ChannelID = '%s' AND Sequence = %d),
 '%s', '%s', '%s', %d, '%s');
                              """.format(p.start, p.stop, p.channelId, p.sequence,
                                         p.start, p.stop, p.channelId, p.sequence, p.toXml))
    }
  }

  def getDeleteTargets(): Seq[Int] = {
    log("getting delete targets...")

    val statement = connection.createStatement
    val rs = statement.executeQuery("SELECT Id FROM Programme INTERSECT SELECT Id FROM ProgrammeNew;")

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
    statement.executeUpdate("INSERT INTO Programme SELECT * FROM ProgrammeNew;")
    log("updating...")
    statement.executeUpdate("DELETE FROM ProgrammeNew;")
    log("updated")
  }

  def select_xml(progId: Int): String = {
    log(progId.toString)

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
    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Id FROM Programme WHERE Start = '%s' AND Stop = '%s' AND ChannelId = '%s' AND Sequence = %d;"
      .format(p.start, p.stop, p.channelId, p.sequence))
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

  def registerChannel(id: String, name: String) = {
    log("registering channel: " + name)

    val statement = connection.createStatement
    statement.executeUpdate("""
INSERT OR IGNORE INTO Channel
VALUES(
  '%s',
  COALESCE((SELECT MAX(Sequence) + 1 FROM Channel WHERE Id = '%s'), 0),
  '%s'
)
;
                            """.format(id, id, name))
  }

  def getChannelName(id: String): String = {
    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Name FROM Channel WHERE Id = '%s' AND Sequence = %d;"
      .format(id, 0))
    rs.getString("Name")
  }

  def getChannelId(name: String): String = {
    val statement = connection.createStatement
    val rs = statement.executeQuery(
      "SELECT Id FROM Channel WHERE Name = '%s';"
      .format(name))
    rs.getString("Id")
  }
}

