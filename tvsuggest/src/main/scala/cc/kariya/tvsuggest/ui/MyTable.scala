/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import cc.kariya.tvsuggest.engine.Lucene
import cc.kariya.tvsuggest.engine.TinySegmenter
import cc.kariya.tvsuggest.engine.Yahoo
import cc.kariya.tvsuggest.engine.YahooAnalyzer
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.StringReader
import javax.swing.AbstractAction
import javax.swing.DefaultListSelectionModel
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.TermAttribute


class MyTable(val tabbed_pane: JTabbedPane, val log_area: JTextArea, val desc_area: JTextArea)
extends JTable {
  setDefaultRenderer(classOf[AnyRef], new MultiLineCellRenderer /*{
                                                                 val birow_color = new Color(204, 204, 255)
                                                                 val org_color = Color.WHITE
                                                                 override def getTableCellRendererComponent(
                                                                 tbl: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, r: Int, c: Int
                                                                 ) = {
                                                                 val component = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c)
                                                                 if (r % 2 == 0) {
                                                                 setBackground(birow_color)
                                                                 } else {
                                                                 setBackground(org_color)
                                                                 }
                                                                 component
                                                                 }
  }*/)

  getSelectionModel.addListSelectionListener(
    new ListSelectionListener {
      def valueChanged(ev: ListSelectionEvent) = {
        if (!ev.getValueIsAdjusting) {
          val i = MyTable.this.getSelectedRowCount
          if (i == 1) {
            val v = ev.getSource.asInstanceOf[DefaultListSelectionModel]
            val desc = MyTable.this.getModel.getValueAt(v.getMaxSelectionIndex, 4).asInstanceOf[String]
            MyTable.this.desc_area.setText(desc)
          }
        }
      }
    })

  def moreLikeThisAction(): AbstractAction = {
    new AbstractAction("MoreLikeThis") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, 4).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var ar: Array[Array[AnyRef]] = null
          def doInBackground() = {
            ar = Lucene.searchMoreLikeThis(v)
            Lucene.close
          }
          override def done() = {
            if (ar.isEmpty) {
              JOptionPane.showMessageDialog(null, "No hit!")
            } else {
              val new_table = new MyTable(table.tabbed_pane, table.log_area, table.desc_area)
              new_table.setContents(ar)
              table.tabbed_pane.addTab(v.substring(0, 10), new JScrollPane(new_table))
              table.tabbed_pane.setSelectedIndex(table.tabbed_pane.getTabCount - 1)
            }
          }
        }.execute
      }
    }
  }

  def timeNeighborAction(): AbstractAction = {
    new AbstractAction("同時間帯を検索") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, 0).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var ar: Array[Array[AnyRef]] = null
          def doInBackground() = {
            ar = Lucene.searchTimeNeighbor(v)
            Lucene.close
          }
          override def done() = {
            if (ar.isEmpty) {
              JOptionPane.showMessageDialog(null, "No hit!")
            } else {
              val new_table = new MyTable(table.tabbed_pane, table.log_area, table.desc_area)
              new_table.setContents(ar)
              table.tabbed_pane.addTab(v.substring(0, 10), new JScrollPane(new_table))
              table.tabbed_pane.setSelectedIndex(table.tabbed_pane.getTabCount - 1)
            }
          }
        }.execute
      }
    }
  }

  def keywordAction(): AbstractAction = {
    new AbstractAction("キーワードを抽出") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, 4).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var keywords: Array[String] = null
          def doInBackground() = {
            keywords = Yahoo.getKeyword(v)
          }
          override def done() = {
            if (keywords.isEmpty) {
              JOptionPane.showMessageDialog(null, "No hit!")
            } else {
              var s = ""
              for (k <- keywords) {
                s += "," + k
              }
              JOptionPane.showMessageDialog(null, "keywords:" + s)
            }
          }
        }.execute
      }
    }
  }

  def tinySegmenterAction(): AbstractAction = {
    new AbstractAction("TinySegmeenterで区切る") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, 4).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var keywords: Array[String] = null
          def doInBackground() = {
            keywords = TinySegmenter.getWords(v)
          }
          override def done() = {
            if (keywords.isEmpty) {
              JOptionPane.showMessageDialog(null, "No hit!")
            } else {
              var s = ""
              for (k <- keywords) {
                s += "[" + k + "]"
              }
              JOptionPane.showMessageDialog(null, "words:" + s)
            }
          }
        }.execute
      }
    }
  }

  def yahooSegmenterAction(): AbstractAction = {
    new AbstractAction("Yahoo形態素分析で区切る") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, 4).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var words: TokenStream = null
          def doInBackground() = {
            words = new YahooAnalyzer().tokenStream("", new StringReader(v))
          }
          override def done() = {
            JOptionPane.showMessageDialog(null, "words:" + words.toString)
            println(v)
            while (words.incrementToken) {
              println(words.getAttribute(classOf[TermAttribute]).toString)
            }
          }
        }.execute
      }
    }
  }

  def setContents(arrays: Array[Array[AnyRef]]) = {
    val title = Array("開始": AnyRef, "終了", "Ch", "タイトル", "内容")
    setModel(new DefaultTableModel(arrays, title))
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
    ColumnResizer.adjustColumnPreferredWidths2(this)
    addMouseListener(new MouseAdapter {
        override def mouseReleased(ev: MouseEvent) = {
          if (ev.isPopupTrigger) {
            val c = ev.getSource.asInstanceOf[JComponent]
            val popup = new JPopupMenu
            popup.add(moreLikeThisAction)
            popup.add(timeNeighborAction)
            popup.add(keywordAction)
            popup.add(tinySegmenterAction)
            popup.add(yahooSegmenterAction)
            popup.show(c, ev.getX, ev.getY)
            ev.consume
          }
        }
      })
  }
}

class MultiLineCellRenderer extends JTextArea with TableCellRenderer {
  setLineWrap(true)
  setWrapStyleWord(true)
  setOpaque(true)

  val noFocusBorder = new EmptyBorder(1, 1, 1, 1)
  def getTableCellRendererComponent(
    table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
  ) = {
    if (isSelected) {
      super.setForeground(table.getSelectionForeground)
      super.setBackground(table.getSelectionBackground)
    } else {
      super.setForeground(table.getForeground)
      super.setBackground(table.getBackground)
    }
    setFont(table.getFont)
    if (hasFocus) {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"))
    } else {
      setBorder(noFocusBorder)
    }
    setText(if (value == null) "" else value.toString)
    this //.asInstanceOf[Component]
  }
}
