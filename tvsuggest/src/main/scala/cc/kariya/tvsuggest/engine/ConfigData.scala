/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.engine

import scala.reflect.BeanProperty

object ConfigData {

  @BeanProperty
  var index = "cjkindex"
  @BeanProperty
  var db = "sqlite.db"

}
