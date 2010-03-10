/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import cc.kariya.tvsuggest.util.BoundPropertyBean
import scala.reflect.BeanProperty
import cc.kariya.tvsuggest.ui.UIMain
import java.awt.Component
import xml.{XML, Node}
import java.io.File

@serializable
object ConfigData extends BoundPropertyBean {

  //@BeanProperty
  var inifile = "inifile.xml"
  var index = "cjkindex"
  var db = "sqlite.db"
  var x = 640
  var y = 480

  if (new File(inifile).exists) {
    fromXML(XML.loadFile(inifile))
  }

  def toXML =
    <config>
      <inifile>{inifile}</inifile>
      <index>{index}</index>
      <db>{db}</db>
      <x>{x}</x>
      <y>{y}</y>
    </config>
  def fromXML(xml:Node) = {
    inifile = (xml \ "inifile").text
    index = (xml \ "index").text
    db = (xml \ "db").text
    x = (xml \ "x").text.toInt
    y = (xml \ "y").text.toInt
  }


  def getInifile = inifile
  def setInifile(newvalue: String) = {
    val oldvalue = inifile
    inifile = newvalue
    firePropertyChange("inifile", oldvalue, newvalue)
  }
  
  def getX = x

  def setX(newvalue: Int) = {
    val oldvalue = x
    x = newvalue
    firePropertyChange("x", oldvalue, newvalue)
  }

  def getY = y

  def setY(newvalue: Int) = {
    val oldvalue = y
    y = newvalue
    firePropertyChange("y", oldvalue, newvalue)
  }

  def getIndex = index

  def setIndex(newvalue: String) = {
    val oldvalue = index
    index = newvalue
    firePropertyChange("index", oldvalue, newvalue)
  }

  def getDb = db

  def setDb(newvalue: String) = {
    val oldvalue = db
    db = newvalue
    firePropertyChange("db", oldvalue, newvalue)
  }

}
