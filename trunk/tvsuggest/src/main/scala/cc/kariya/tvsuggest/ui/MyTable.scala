/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import cc.kariya.tvsuggest.Util
import cc.kariya.tvsuggest.engine.Lucene
import cc.kariya.tvsuggest.engine.TinySegmenter
import cc.kariya.tvsuggest.engine.Yahoo
import cc.kariya.tvsuggest.engine.YahooAnalyzer
import cc.kariya.tvsuggest.engine.db.ProgrammeDB
import com.jidesoft.swing.TableSearchable
import java.awt.Component
import java.awt.EventQueue
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.StringReader
import javax.swing.AbstractAction
import javax.swing.AbstractCellEditor
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
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.TermAttribute


class MyTable(val tabbed_pane: JTabbedPane, val log_area: JTextArea, val desc_area: JTextArea)
extends JTable {

  this.setAutoCreateRowSorter(true)
  //val searchable = new TableSearchable(this)

  /*  setDefaultRenderer(classOf[AnyRef], new MultiLineCellRenderer
   {
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
   })
   */
  getSelectionModel.addListSelectionListener(
    new ListSelectionListener {
      def valueChanged(ev: ListSelectionEvent) = {
        if (!ev.getValueIsAdjusting) {
          val i = MyTable.this.getSelectedRowCount
          if (i == 1) {
            val v = ev.getSource.asInstanceOf[DefaultListSelectionModel]
            val desc = MyTable.this.getModel.getValueAt(v.getMaxSelectionIndex, COL_DESC).asInstanceOf[String]
            MyTable.this.desc_area.setText(desc)
          }
        }
      }
    })

  def moreLikeThisAction(): AbstractAction = {
    new AbstractAction("MoreLikeThis") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, COL_DESC).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var ar: Array[Array[AnyRef]] = null
          def doInBackground() = {
            ar = Lucene.searchMoreLikeThis(v)
            Lucene.close
          }
          override def done() = {
            if (ar == null || ar.isEmpty) {
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
        val pid = table.getModel.getValueAt(table.getSelectedRow, COL_PID).asInstanceOf[Int]
        new MyWorker(table.log_area) {
          var ar: Array[Array[AnyRef]] = null
          def doInBackground() = {
            ar = ProgrammeDB.searchTimeNeighbor(ProgrammeDB.select_start(pid))
          }
          override def done() = {
            if (ar.isEmpty) {
              JOptionPane.showMessageDialog(null, "No hit!")
            } else {
              val new_table = new MyTable(table.tabbed_pane, table.log_area, table.desc_area)
              new_table.setContents(ar)
              table.tabbed_pane.addTab(ProgrammeDB.select_start(pid).substring(0, 10), new JScrollPane(new_table))
              table.tabbed_pane.setSelectedIndex(table.tabbed_pane.getTabCount - 1)
            }
          }
        }.execute
      }
    }
  }

  def keywordAction(): AbstractAction = {
    new AbstractAction("キーワードを入力") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val pid = table.getModel.getValueAt(table.getSelectedRow, COL_PID).asInstanceOf[Int]
        val rate = table.getModel.getValueAt(table.getSelectedRow, COL_RATING).asInstanceOf[Int]
        println("rate " + rate)
        val doc = table.getModel.getValueAt(table.getSelectedRow, COL_DESC).asInstanceOf[String]
        new MyWorker(table.log_area) {
          var tags: Seq[String] = null
          def doInBackground() = {
            tags = {
              val user_tags = Util.getTags(pid, "user")
              if (user_tags isEmpty) {
                Yahoo.getKeyword(doc)
              } else {
                user_tags
              }
            }
          }
          override def done() = {
            val dlg = new InputTagDialog(UIMain.main_frame)
            dlg.setTags(tags)
            dlg.show
            Util.setTags(pid, dlg.getTags, "user")
            //JOptionPane.showMessageDialog(null, "keywords:" + dlg.getTags(0))
          }
        }.execute
      }
    }
  }

  def tinySegmenterAction(): AbstractAction = {
    new AbstractAction("TinySegmeenterで区切る") {
      def actionPerformed(e: ActionEvent) = {
        val table = MyTable.this
        val v = table.getModel.getValueAt(table.getSelectedRow, COL_DESC).asInstanceOf[String]
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
        val v = table.getModel.getValueAt(table.getSelectedRow, COL_DESC).asInstanceOf[String]
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

  val COL_PID = 0
  val COL_RATING = 1
  val COL_START = 2
  val COL_STOP = 3
  val COL_CH = 4
  val COL_TITLE = 5
  val COL_DESC = 6

  def setContents(arrays: Array[Array[AnyRef]]) = {
    val title = Array("pid", "レ ー テ ィ ン グ", "開始": AnyRef, "終了", "Ch", "タイトル", "内容")
    setModel(new DefaultTableModel(arrays, title))
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
    val renderer = new RatingBarEditorRenderer(this)
    this.getColumnModel.getColumn(COL_RATING).setCellRenderer(renderer)
    this.getColumnModel.getColumn(COL_RATING).setCellEditor(renderer)
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
//    this.getColumn(COL_PID).setMaxWidth(0)
//    this.getColumn(COL_RATING).setMinWidth(80)
    ColumnResizer.adjustColumnPreferredWidths2(this)
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

class RatingBarEditorRenderer(val table: JTable)
extends AbstractCellEditor with TableCellRenderer with TableCellEditor {
  val ratingBarEditor = new RatingBar
  val ratingBarRenderer = new RatingBar

  ratingBarEditor.addChangeListener(new ChangeListener {
    def stateChanged(ev: ChangeEvent) = {
      EventQueue.invokeLater(new Runnable {
        override def run = {
          val row = table.convertRowIndexToModel(table.getSelectedRow)
          val col = table.getSelectedColumn
          table.getModel().setValueAt(ratingBarEditor.getLevel, row, col)
        }
      })
    }
  })

  def getTableCellEditorComponent(
    table: JTable, value: AnyRef, isSelected: Boolean, row: Int, column: Int
  ): Component = {
    val i = value.asInstanceOf[Int]
    ratingBarEditor.setLevel(i)
    ratingBarRenderer.setBackground(table.getBackground)
    return ratingBarEditor
  }

  def getCellEditorValue: AnyRef = {
    return ratingBarEditor.getLevel.asInstanceOf[AnyRef]
  }

  def getTableCellRendererComponent(
    table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
  ): Component = {
    val i = value.asInstanceOf[Int]
    ratingBarRenderer.setLevel(i)
    ratingBarRenderer.setBackground(if (hasFocus) table.getSelectionBackground else table.getBackground)
    return ratingBarRenderer
  }
}
