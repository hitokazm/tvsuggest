/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest


import cc.kariya.tvsuggest.engine.Database
import cc.kariya.tvsuggest.engine.Lucene
import cc.kariya.tvsuggest.grabber.IPlugin
import cc.kariya.tvsuggest.grabber.AbstractProgramme
import cc.kariya.tvsuggest.ui.UILogger


object Main  extends UILogger {

  def main_fetch(grabber: IPlugin): Unit = {
    Database.init
    Database.begin
    Database.prestore
    Lucene.begin
    grabber.getChannels.toList foreach {
      case (channelId, channelNames) =>
        val channelName = channelNames.head
        Database.registerChannel(channelId, channelName)
        val progs = grabber.getProgrammes(channelId, channelNames)
        progs.foreach (Database.store _)
        log("deleting index...")
        val del_ids = Database.getDeleteTargets
        del_ids.foreach (Lucene.delete _)
        log("deleting old datas...")
        del_ids.foreach(Database.delete_old_programme _)
        Database.update_new_programmes
        progs.foreach {
          p =>
          val id = Database.select_id(p)
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
}
