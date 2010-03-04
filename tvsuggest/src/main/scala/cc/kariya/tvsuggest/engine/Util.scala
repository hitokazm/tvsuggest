/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest


import cc.kariya.tvsuggest.engine.Lucene
import cc.kariya.tvsuggest.grabber.IPlugin
import cc.kariya.tvsuggest.engine.db.ChannelDB
import cc.kariya.tvsuggest.engine.db.Database
import cc.kariya.tvsuggest.engine.db.MetaDataDB
import cc.kariya.tvsuggest.engine.db.ProgrammeDB
import cc.kariya.tvsuggest.ui.UILogger


object Util extends UILogger {

  def fetch(grabber: IPlugin): Unit = {
    Database.init
    Database.begin
    ProgrammeDB.prestore
    Lucene.begin
    grabber.getChannels.toList foreach {
      case (channelId, channelNames) =>
        val channelName = channelNames.head
        ChannelDB.registerChannel(channelId, channelName)
        val progs = grabber.getProgrammes(channelId, channelNames)
        progs.foreach (ProgrammeDB.store _)
        log("deleting index...")
        val del_ids = ProgrammeDB.getDeleteTargets
        del_ids.foreach (Lucene.delete _)
        log("deleting old datas...")
        del_ids.foreach(ProgrammeDB.delete_old_programme _)
        ProgrammeDB.update_new_programmes
        progs.foreach {
          p =>
          val id = ProgrammeDB.select_id(p)
          Lucene.store(p.replaceId(id))
        }
    }
    log("optimizing index...")
    Lucene.optimize
    Database.commit
    Lucene.commit
    Lucene.close
    log("finished")
  }

  def getTags(progId: Int, tagger: String) = {
    MetaDataDB.selectTags(progId, tagger)
  }

  def setTags(progId: Int, tags: Seq[String], tagger: String) = {
    MetaDataDB.deleteTagging(progId, tagger)
    for (tagName <- tags) {
      val tagId = {
        val id = MetaDataDB.selectTagId(tagName)
        if (id < 0) {
          MetaDataDB.insertTag(tagName)
        } else {
          id
        }
      }
      MetaDataDB.insertTagging(progId, tagId, tagger)
    }
  }
}
