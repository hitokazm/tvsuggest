/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui


import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.image.FilteredImageSource
import java.awt.image.ImageProducer
import java.awt.image.RGBImageFilter
import java.io.File
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class LevelBar(
  val defaultIcon: ImageIcon,
  val iconList: List[ImageIcon],
  val gap: Int
) extends JPanel(new GridLayout(1, 5, gap * 2, gap * 2))
     with MouseListener with MouseMotionListener {

  val labelList = List(new JLabel, new JLabel, new JLabel, new JLabel, new JLabel)

  for (l <- labelList) {
    l.setIcon(defaultIcon)
    add(l)
  }
  addMouseListener(this)
  addMouseMotionListener(this)

  private var clicked = -1

  def clear() = {
    clicked = -1
    repaintIcon(clicked)
  }

  def getLevel = clicked

  def setLevel(l: Int) = {
    clicked = l
    repaintIcon(clicked)
  }

  private def getSelectedIconIndex(p: Point) = {
    (
      (
        labelList.zipWithIndex flatMap {
        case (l , i) =>
          val r = l.getBounds()
          r.grow(gap, gap)
          if (r.contains(p)) List(i) else List()
        }
      ) ::: List(-1)
    ).head
  }

  protected def repaintIcon(index: Int) = {
    for ((l, i) <- labelList.zipWithIndex) {
      l.setIcon(if (i <= index) iconList(i) else defaultIcon)
    }
    repaint()
  }

  def mouseMoved(e: MouseEvent) = repaintIcon(getSelectedIconIndex(e.getPoint))
  def mouseEntered(e: MouseEvent) = repaintIcon(getSelectedIconIndex(e.getPoint))
  def mouseClicked(e: MouseEvent) = (clicked = getSelectedIconIndex(e.getPoint))
  def mouseExited(e: MouseEvent) = repaintIcon(clicked)
  def mouseDragged(e: MouseEvent) = ()
  def mousePressed(e: MouseEvent) = ()
  def mouseReleased(e: MouseEvent) = ()

}

object RatingBar {
  val p = new JPanel(new FlowLayout(FlowLayout.LEFT))

  def makeStarRatingPanel(title: String, label: LevelBar) = {
    //p.setBorder(BorderFactory.createTitledBorder(title))
    p.setBorder(new EmptyBorder(1, 1, 1, 1))
    p.add(new JButton(new AbstractAction("clear") {
          def actionPerformed(e: ActionEvent) = label.clear
    }))
    p.add(label)
    p
  }

  def makeStartImageIcon(ip: ImageProducer, filter: List[Float]) = {
    val sif = new SelectedImageFilter(filter)
    new ImageIcon(p.createImage(new FilteredImageSource(ip, sif)))
  }

  def panel() = {
    val url = this.getClass.getClassLoader.getResource("31g.png")
    val defaultIcon = new ImageIcon(url)
    val ip = defaultIcon.getImage.getSource
    val yStar = makeStartImageIcon(ip, List(1f, 1f, 0f))
    val list = List(yStar, yStar, yStar, yStar, yStar)
    makeStarRatingPanel("test", new LevelBar(defaultIcon, list, 1))
  }
}

class SelectedImageFilter(val filter: List[Float]) extends RGBImageFilter {
  canFilterIndexColorModel = true

  def filterRGB(x: Int, y: Int, argb: Int) = {
    val color = new Color(argb, true)
    val array = Array(0f: Float, 0f: Float, 0f: Float, 0f: Float)
    color.getComponents(array)
    new Color(array(0) * filter(0),
              array(1) * filter(1),
              array(2) * filter(2),
              array(3))
    .getRGB()
  }
}
