/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui

import com.jgoodies.forms.layout.CellConstraints
import com.jgoodies.forms.layout.ColumnSpec
import com.jgoodies.forms.layout.FormLayout
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.LayoutManager
import java.awt.LayoutManager2
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


class StatusBar extends JPanel {

  setLayout(new BorderLayout)
  setPreferredSize(new Dimension(getWidth, 23))
  val resizeIconLabel = new JLabel(new TrinagleSquareWindowsCornerIcon)
  resizeIconLabel.setOpaque(false)

  val rightPanel = new JPanel(new BorderLayout)
  // rightPanel.add(new JLabel(new TrinagleSquareWindowsCornerIcon()), BorderLayout.SOUTH);
  rightPanel.add(resizeIconLabel, BorderLayout.SOUTH)
  rightPanel.setOpaque(false)
  add(rightPanel, BorderLayout.EAST)

  val contentPanel = new JPanel( )
  contentPanel.setOpaque(false)
  val layout2 = new FormLayout("2dlu, pref:grow", "3dlu, fill:10dlu, 2dlu")

  contentPanel.setLayout(layout2)
  add(contentPanel, BorderLayout.CENTER)

  //setBackground(new Color(236, 233, 216))
  setBackground(new Color(223, 224, 229))


  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    var y = 0
    g.setColor(new Color(156, 154, 140))
    g.drawLine(0, y, getWidth, y)
    y += 1
    g.setColor(new Color(196, 194, 183))
    g.drawLine(0, y, getWidth, y)
    y += 1
    g.setColor(new Color(218, 215, 201))
    g.drawLine(0, y, getWidth, y)
    y += 1
    g.setColor(new Color(233, 231, 217))
    g.drawLine(0, y, getWidth, y)

    y = getHeight - 3
    g.setColor(new Color(233, 232, 218))
    g.drawLine(0, y, getWidth, y)
    y += 1
    g.setColor(new Color(233, 231, 216))
    g.drawLine(0, y, getWidth, y)
    y += 1
    g.setColor(new Color(221, 221, 220))
    g.drawLine(0, y, getWidth, y)
  }

  def setMainLeftComponent(component: JComponent) = {
    contentPanel.add(component, new CellConstraints(2, 2))
  }

  var layoutCoordinateX = 2
  var layoutCoordinateY = 2

  def addRightComponent(component: JComponent, dialogUnits: Int) = {
    layout2.appendColumn(new ColumnSpec("2dlu"))
    layout2.appendColumn(new ColumnSpec(dialogUnits + "dlu"))
    layoutCoordinateX += 1

    contentPanel.add(
      new SeparatorPanel(Color.GRAY, Color.WHITE),
      new CellConstraints(layoutCoordinateX, layoutCoordinateY)
    )
    layoutCoordinateX += 1
    contentPanel.add(
      component,
      new CellConstraints(layoutCoordinateX, layoutCoordinateY)
    )
  }
}

class SeparatorPanel(val leftColor: Color, val rightColor: Color) extends JPanel {
  setOpaque(false)

  override def paintComponent(g: Graphics) = {
    g.setColor(leftColor)
    g.drawLine(0, 0, 0, getHeight)
    g.setColor(rightColor)
    g.drawLine(1, 0, 1, getHeight)
  }
}

class TrinagleSquareWindowsCornerIcon extends Icon {
  val THREE_D_EFFECT_COLOR = new Color(255, 255, 255)
  val SQUARE_COLOR_LEFT = new Color(184, 180, 163)
  val SQUARE_COLOR_TOP_RIGHT = new Color(184, 180, 161)
  val SQUARE_COLOR_BOTTOM_RIGHT = new Color(184, 181, 161)

  //Dimensions
  val WIDTH = 12;
  val HEIGHT = 12;

  def getIconHeight() = WIDTH
  def getIconWidth() = HEIGHT

  def paintIcon(c: Component, g: Graphics, x: Int, y: Int) = {
    //Layout a row and column "grid"
    val firstRow = 0
    val firstColumn = 0
    val rowDiff = 4
    val columnDiff = 4

    val secondRow = firstRow + rowDiff
    val secondColumn = firstColumn + columnDiff
    val thirdRow = secondRow + rowDiff
    val thirdColumn = secondColumn + columnDiff

    //Draw the white squares first, so the gray squares will overlap
    draw3dSquare(g, firstColumn + 1, thirdRow + 1)

    draw3dSquare(g, secondColumn + 1, secondRow + 1)
    draw3dSquare(g, secondColumn + 1, thirdRow + 1)

    draw3dSquare(g, thirdColumn + 1, firstRow + 1)
    draw3dSquare(g, thirdColumn + 1, secondRow + 1)
    draw3dSquare(g, thirdColumn + 1, thirdRow + 1)

    //draw the gray squares overlapping the white background squares
    drawSquare(g, firstColumn, thirdRow)

    drawSquare(g, secondColumn, secondRow)
    drawSquare(g, secondColumn, thirdRow)

    drawSquare(g, thirdColumn, firstRow)
    drawSquare(g, thirdColumn, secondRow)
    drawSquare(g, thirdColumn, thirdRow)
  }

  def draw3dSquare(g: Graphics, x: Int, y: Int) = {
    val oldColor = g.getColor() //cache the old color
    g.setColor(THREE_D_EFFECT_COLOR) //set the white color
    g.fillRect(x, y, 2, 2) //draw the square
    g.setColor(oldColor) //reset the old color
  }

  def drawSquare(g: Graphics, x: Int, y: Int) = {
    val oldColor = g.getColor()
    g.setColor(SQUARE_COLOR_LEFT)
    g.drawLine(x, y, x, y + 1)
    g.setColor(SQUARE_COLOR_TOP_RIGHT)
    g.drawLine(x + 1, y, x + 1, y)
    g.setColor(SQUARE_COLOR_BOTTOM_RIGHT)
    g.drawLine(x + 1, y + 1, x + 1, y + 1)
    g.setColor(oldColor)
  }

}
