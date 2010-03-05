/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import java.awt.FontMetrics
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.SwingUtilities


object ColumnResizer extends UILogger {

  def adjustColumnPreferredWidths(table: JTable) = {
    val columnModel = table.getColumnModel
    for (col <- 0 to table.getColumnCount - 1) {
      var maxWidth = 0
      for (row <- 0 to table.getRowCount - 1) {
        val rend = table.getCellRenderer(row, col)
        val value = table.getValueAt(row, col)
        val comp = rend.getTableCellRendererComponent(table, value, false, false, row, col)
        maxWidth = Math.max(comp.getPreferredSize.width, maxWidth)
      }
      val column = columnModel.getColumn(col)
      val headerRenderer = {
        if (column.getHeaderRenderer != null) column.getHeaderRenderer
        else table.getTableHeader.getDefaultRenderer
      }
      val headerValue = column.getHeaderValue
      val headerComponent = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, col)
      maxWidth = Math.max(maxWidth, headerComponent.getPreferredSize.width)
      column.setPreferredWidth(maxWidth)
    }
  }

  def adjustColumnPreferredWidths2(table: JTable) = {
    val fm = table.getFontMetrics(table.getFont)
    val is = table.getDefaultRenderer(classOf[AnyRef]).asInstanceOf[JComponent].getInsets.left + 1
    val columnModel = table.getColumnModel
    for (col <- 0 to table.getColumnCount - 1) {
      var maxWidth = 0
      for (row <- 0 to table.getRowCount - 1) {
        val w = SwingUtilities.computeStringWidth(fm, table.getValueAt(row, col).toString)
        maxWidth = Math.max(w, maxWidth)
      }
      val column = columnModel.getColumn(col)
      val headerRenderer = {
        if (column.getHeaderRenderer != null) column.getHeaderRenderer
        else table.getTableHeader.getDefaultRenderer
      }
      val headerValue = column.getHeaderValue
      val headerComponent = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, col)
      maxWidth = Math.max(maxWidth, headerComponent.getPreferredSize.width)
      column.setPreferredWidth(maxWidth + is * 2)
    }
  }
}
