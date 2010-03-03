/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import scala.collection.mutable.Publisher
import scala.util.logging.Logged


trait UILogger extends Logged {
  override def log(s: String) = UILogger.log(s)
}

object UILogger extends Publisher[String] with Logged {
  override def log(msg: String) = publish(msg)
}
