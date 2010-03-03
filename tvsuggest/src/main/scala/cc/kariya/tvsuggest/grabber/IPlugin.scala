/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.grabber


trait IPlugin {

  def getPluginName(): String
  
  def getChannels: Map[String, List[String]]
  def getProgrammes(id: String, names: List[String]): List[AbstractProgramme]

}
