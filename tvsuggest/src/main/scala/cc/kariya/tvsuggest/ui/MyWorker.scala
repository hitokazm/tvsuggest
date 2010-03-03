/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import javax.swing.JTextArea
import javax.swing.SwingWorker
import scala.collection.mutable.Publisher
import scala.collection.mutable.Subscriber

abstract class MyWorker(
  val jTextArea: JTextArea
) extends SwingWorker[Unit, String] with Subscriber[String, Publisher[String]] {

  UILogger.subscribe(this)

  val buf = new StringBuffer

  def notify(publisher: Publisher[String], event: String) = {
    buf.append(event)
    buf.append("\n")
    jTextArea.setText(buf.toString)
    jTextArea.setCaretPosition(jTextArea.getText.length)
  }
}
