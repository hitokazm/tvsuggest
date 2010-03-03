/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.grabber


abstract class AbstractProgramme(
  val id: Int,
  val start: String,
  val stop: String,
  val channelId: String,
  val sequence: Int
) {
  def replaceId(new_id: Int): AbstractProgramme
  def description: String
  def toXml: scala.xml.Node
}
