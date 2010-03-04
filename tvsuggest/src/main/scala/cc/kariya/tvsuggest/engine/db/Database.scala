/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine.db

import cc.kariya.tvsuggest.ui.UILogger
import java.sql.DriverManager


object Database extends UILogger {
  val connection = {
    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection("jdbc:sqlite:sqlite.db")
  }

  def init: Unit = {
    log("initing DB(common)...")

    ProgrammeDB.init
    ChannelDB.init
    MetaDataDB.init
  }

  def begin = connection.setAutoCommit(false)
  def commit = connection.commit
}

class Database extends UILogger {
  val connection = Database.connection
  
  //def begin = Database.begin
  //def commit = Database.commit
}
