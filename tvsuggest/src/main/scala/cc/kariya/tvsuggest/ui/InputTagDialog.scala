/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTextArea
import scala.collection.JavaConversions._

class InputTagDialog(val owner: Frame) extends JDialog(owner, "タグを入力", true) {
  
  val input = new JTextArea(5,30)
  val panel = new JPanel
  var tags : Seq[String] = null

  input.setText("click here to input tags ...        ")
  input.setEnabled(false)
  input.setBackground(Color.WHITE)
  input.setLineWrap(true)
  
  input.addMouseListener(new MouseAdapter {
      override def mouseClicked(ev: MouseEvent) = {
        if (!input.isEnabled) {
          input.setEnabled(true)

          if (tags == null || tags.isEmpty) {
            input.setText("")
          } else {
            val buf = new StringBuffer
            for (t <- tags) {
              buf.append(", ")
              buf.append(t)
            }
            input.setText(buf.toString.substring(2))
          }
        }
      }
    })
  
  panel.setLayout(new FlowLayout)
  panel.add(input)

  this.setLayout(new BorderLayout)
  this.add(panel, BorderLayout.CENTER)

  this.pack
  this.setSize(320, 120)
  this.setLocationRelativeTo(null)
  //this.setVisible(true)

  def setTags(tags: Seq[String]) = {
    this.tags = tags
  }

  def getTags = {
    input.getText.split(",").map (_.trim)
  }
}
