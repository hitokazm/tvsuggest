/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine.db


object MetaDataDB extends Database {

  def init() = {
    log("initing DB(MetaData)...")

    val statement = connection.createStatement
    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS MetaData(
     ProgrammeId          INTEGER NOT NULL PRIMARY KEY,
     Preferrence          INTEGER NOT NULL,
     EstimatedPreferrence INTEGER NOT NULL,
     Clicks               INTEGER NOT NULL,
     EstimatedClicks      INTEGER NOT NULL
);
                            """)

    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS Tag(
     Id                   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
     Name                 TEXT NOT NULL
);
                            """)

    statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS Tagging(
     ProgrammeId          INTEGER NOT NULL,
     TagId                INTEGER NOT NULL,
     Tagger               TEXT NOT NULL,
     PRIMARY KEY(ProgrammeId, TagId, Tagger)
);
                            """)

  }

  def selectTagId(name: String): Int = {
    val statement = connection.createStatement
    val rs = statement.executeQuery("SELECT Id FROM Tag WHERE Name = '%s'".format(name))
    if (rs.next) {
      return rs.getInt("Id")
    } else {
      return -1
    }
  }

  def insertTag(name: String): Int = {
    val statement = connection.createStatement

    statement.executeUpdate("INSERT INTO Tag (Name) VALUES('%s')".format(name))

    val rs = statement.executeQuery("SELECT last_insert_rowid() AS Id")
    return rs.getInt("Id")
  }

   def deleteTagging(progId: Int, tagger: String) = {
    val statement = connection.createStatement

    statement.executeUpdate("DELETE FROM Tagging WHERE ProgrammeId = %d AND Tagger = '%s'"
                            .format(progId, tagger))

   }

  def insertTagging(progId: Int, tagId: Int, tagger: String) = {
    val statement = connection.createStatement

    statement.executeUpdate("INSERT INTO Tagging (ProgrammeId, TagId, Tagger) VALUES (%d, %d, '%s')"
                            .format(progId, tagId, tagger))

   }

  def selectTags(progId: Int, tagger: String): Seq[String] = {
    val statement = connection.createStatement

    val rs = statement.executeQuery("""
SELECT
    T2.Name
FROM
    Tagging T1
LEFT OUTER JOIN
    Tag T2
ON
    T1.TagId = T2.Id
WHERE
    T1.ProgrammeId = %d
AND T1.Tagger = '%s'
                            """
                            .format(progId, tagger))

    var l = new scala.collection.mutable.ListBuffer[String]
    while (rs.next) {
      l += rs.getString("Name")
    }
    return l.toList
   }
}
