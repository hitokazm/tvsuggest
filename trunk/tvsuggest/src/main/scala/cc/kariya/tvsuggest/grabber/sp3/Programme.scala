/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.grabber.sp3

import net.htmlparser.jericho._
import scala.collection.JavaConversions._
import cc.kariya.tvsuggest.grabber.AbstractProgramme


class Programme(
  override val id: Int,
  override val start: String,
  override val stop: String,
  override val channelId: String,
  override val sequence: Int,
  val title: String,
  val link: String
) extends AbstractProgramme(id, start, stop, channelId, sequence) {

  def replaceStop(new_stop: String) = {
    new Programme(id, start, new_stop, channelId, sequence, title, link)
  }
  def replaceId(new_id: Int) = {
    new Programme(new_id, start, stop, channelId, sequence, title, link)
  }
  // descritpion should have its cache
  def description(): String = {
    if (link.length == 0) "" else Grab.description(link)
  }

  def toXml() = {
    <programme satrt={start} stop={stop} channel={channelId}>
      <title>{title}</title>
      <desc>{description}</desc>
    </programme>
  }
}
