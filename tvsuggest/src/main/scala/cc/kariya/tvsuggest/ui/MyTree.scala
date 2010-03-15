package cc.kariya.tvsuggest.ui

import javax.swing.event.{TreeSelectionEvent, TreeSelectionListener}
import java.awt.Component
import javax.swing._
import java.awt.event.{ActionEvent, MouseAdapter, MouseEvent, MouseListener}
import scala.collection.JavaConversions._
import tree._
import cc.kariya.tvsuggest.engine.{ConfigData, Lucene}
import java.beans.{XMLDecoder, XMLEncoder}
import java.io._

/**
 * Created by IntelliJ IDEA.
 * User: kariya
 * Date: 2010/03/14
 * Time: 14:22:40
 * To change this template use File | Settings | File Templates.
 */

class MyTree extends JTree {

  this.getSelectionModel.addTreeSelectionListener(new TreeSelectionListener {
    def valueChanged(e: TreeSelectionEvent): Unit = {
      val buf = new StringBuffer
      for (o <- e.getPath.getPath.tail) {
        val value = o.asInstanceOf[DefaultMutableTreeNode].getUserObject.asInstanceOf[TreeNodeValue]
        if (value.searchable) {
          buf.append(" AND %s".format(value.query))
        }
      }
      if (buf.length == 0) return
      val text = buf.substring(5)
      new MyWorker(UIMain.log_area) {
        var ar: Array[Array[AnyRef]] = null
        def doInBackground() = {
          ar = Lucene.search(text)
          Lucene.close
        }
        override def done() = {
          if (ar.isEmpty) {
            val suggestions = Lucene.spellcheck(text)
            var maybe = ""
            for (s <- suggestions) {
              maybe += " " + s
            }
            JOptionPane.showMessageDialog(null, "No hit! Maybe" + maybe)
          } else {
            val table = new MyTable(UIMain.body, UIMain.log_area, UIMain.desc_area)
            table.setDefaultEditor(classOf[AnyRef], null)
            table.setContents(ar)
            UIMain.body.addTab(text, new JScrollPane(table))
            UIMain.body.setSelectedIndex(UIMain.body.getTabCount - 1)
          }
        }
      }.execute
    }
  })
//  setComponentPopupMenu(new JPopupMenu("myPopup"))

  this.asInstanceOf[Component].addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount == 2) {
        edit(e)
      } else if (e.isPopupTrigger) {
        val path = MyTree.this.getPathForLocation(e.getX(), e.getY())
        MyTree.this.setSelectionPath(path)
        MyTree.this.getModel.reload
      }
    }
    override def mouseReleased(e: MouseEvent): Unit  = {
      if (e.isPopupTrigger) {
        val menu = new JPopupMenu("menu")
        menu.add(new AbstractAction("add") {
          def actionPerformed(e2: ActionEvent): Unit = {
            val path = MyTree.this.getClosestPathForLocation(e.getX, e.getY).getPath
            //if (path.length < 2) return
            val model = MyTree.this.getModel.asInstanceOf[DefaultTreeModel]
            val parent = path(path.length - 1).asInstanceOf[MutableTreeNode]
            val new_node = {
              val dlg = new KeywordDialog(UIMain.main_frame)
              dlg.show
              val label = dlg.data.label
              val query = dlg.data.query
              val seachable = dlg.data.searchable
              dlg.hide
              new DefaultMutableTreeNode(new TreeNodeValue(label, query, "", seachable))
            }
            model.insertNodeInto(new_node, parent, parent.getChildCount)
            model.reload
            visitAll(MyTree.this, new TreePath(path(0)))
            save
          }
        })
        menu.add(new AbstractAction("edit") {
          def actionPerformed(e2: ActionEvent) = {
            edit(e)
          }
        })
        menu.add(new AbstractAction("delete") {
          def actionPerformed(e2: ActionEvent) = {
            val path = MyTree.this.getClosestPathForLocation(e.getX, e.getY).getPath
            val model = MyTree.this.getModel.asInstanceOf[DefaultTreeModel]
            val node = path(path.length - 1).asInstanceOf[MutableTreeNode]
            model.removeNodeFromParent(node)
            model.reload
            visitAll(MyTree.this, new TreePath(path(0)))
            save
          }
        })
        menu.show(e.getComponent, e.getX, e.getY)
      }
    }
  })

  private def edit(e: MouseEvent): Unit = {
    val path0 = MyTree.this.getClosestPathForLocation(e.getX, e.getY)
    val path = path0.getPath
    val model = MyTree.this.getModel.asInstanceOf[DefaultTreeModel]
    val node = path(path.length - 1).asInstanceOf[DefaultMutableTreeNode]
    val data = node.getUserObject.asInstanceOf[TreeNodeValue]
    val new_node_value = {
      val dlg = new KeywordDialog(UIMain.main_frame, data)
      dlg.show
      val label = dlg.data.label
      val query = dlg.data.query
      val seachable = dlg.data.searchable
      dlg.hide
      new TreeNodeValue(label, query, "", seachable)
    }
    model.valueForPathChanged(path0, new_node_value)
    model.reload
    visitAll(MyTree.this, new TreePath(path(0)))
    save
  }

  def visitAll(tree: JTree, parent: TreePath, expand: Boolean): Unit = {
    val node = parent.getLastPathComponent().asInstanceOf[TreeNode]
    if(!node.isLeaf() && node.getChildCount() >= 0) {
      val e = node.children()
      while (e.hasMoreElements()) {
        val n = e.nextElement().asInstanceOf[TreeNode]
        val path = parent.pathByAddingChild(n)
        visitAll(tree, path, expand)
      }
    }
    if (expand) tree.expandPath(parent)
    else        tree.collapsePath(parent)
  }

  def visitAll(tree: JTree, parent: TreePath): Unit = {
    val node = parent.getLastPathComponent().asInstanceOf[TreeNode]
    if(!node.isLeaf() && node.getChildCount() >= 0) {
      val e = node.children()
      while (e.hasMoreElements()) {
        val n = e.nextElement().asInstanceOf[TreeNode]
        val path = parent.pathByAddingChild(n)
        visitAll(tree, path)
      }
    }
    if (node.asInstanceOf[DefaultMutableTreeNode].getUserObject.asInstanceOf[TreeNodeValue].expanded)
      tree.expandPath(parent)
    else
      tree.collapsePath(parent)
  }

  def save = {
    val enc = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(ConfigData.uiInifile)))
    enc.writeObject(this.getModel)
    enc.close
  }
}

object MyTree {
  def load: MyTree = {
    if (new File(ConfigData.uiInifile).exists) {
      val dec = new XMLDecoder(new BufferedInputStream(new FileInputStream(ConfigData.uiInifile)))
      val model = dec.readObject.asInstanceOf[DefaultTreeModel]
      dec.close
      val tree = new MyTree()
      tree.setModel(model)
      return tree
    } else {
      val tree = new MyTree
      val root = new DefaultMutableTreeNode(new TreeNodeValue("KEYWORD", "", "", false))
      val model = new DefaultTreeModel(root)
      tree.setModel(model)
      return tree
    }
  }
}
