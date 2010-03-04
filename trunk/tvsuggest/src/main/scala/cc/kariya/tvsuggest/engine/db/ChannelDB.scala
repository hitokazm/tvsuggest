/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine.db


object ChannelDB extends Database {

  def init() = {
    log("initing DB(Channel)...")

    val statement = connection.createStatement
    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS Channel(
     Id             INTEGER NOT NULL,
     Sequence       INTEGER NOT NULL,
     Name           TEXT    NOT NULL,
     PRIMARY KEY(Id, Sequence),
     UNIQUE(Name)
);
                             """)
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
