  /*
   * To change this template, choose Tools | Templates
   * and open the template in the editor.
   */

  package cc.kariya.tvsuggest.engine

  import cc.kariya.tvsuggest.util.BoundPropertyBean
  import scala.reflect.BeanProperty
  import cc.kariya.tvsuggest.ui.UIMain
  import java.awt.Component

  object ConfigData extends BoundPropertyBean {

    //@BeanProperty
    var index = "cjkindex"
    var db = "sqlite.db"
    var x = 640
    var y = 480

    def getX = x
    def setX(newvalue: Int) = {
      val oldvalue = x
      x = newvalue
      firePropertyChange("x", oldvalue, newvalue)
      UIMain.main_frame.setSize(x, UIMain.main_frame.getSize.getWidth.toInt)
    }
    def getY = y
    def setY(newvalue: Int) = {
      val oldvalue = y
      y = newvalue
      firePropertyChange("y", oldvalue, newvalue)
      UIMain.main_frame.setSize(UIMain.main_frame.getSize.getHeight.toInt, y)
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
