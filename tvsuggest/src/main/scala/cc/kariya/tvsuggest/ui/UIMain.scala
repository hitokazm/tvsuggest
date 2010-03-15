/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.ui


import cc.kariya.tvsuggest.engine.Lucene
import cc.kariya.tvsuggest.engine.Util
import cc.kariya.tvsuggest.engine.ConfigData
import cc.kariya.tvsuggest.grabber.IPlugin
import cc.kariya.tvsuggest.grabber.sp3.Grab
import com.jidesoft.swing.ButtonStyle
import com.jidesoft.swing.JideSplitButton
import com.jidesoft.swing.JideTabbedPane
import java.awt.Dialog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import java.awt.{Dimension, BorderLayout, Dialog}

object UIMain {

  val plugins: List[IPlugin] = List(Grab)

  val main_frame = new JFrame
  val main_panel = new JPanel
  val header = new JPanel
  val body = new JideTabbedPane
  val footer = new JideTabbedPane
  val left = new JPanel
  val menu_bar = new JMenuBar
  val status_bar = new StatusBar

  val log_area = new JTextArea(80, 10)
  val desc_area = new JTextArea(80, 10)
  val search_text = new JTextField
  val update_button = new JideSplitButton("更新")
  val scroll_pane_log = new JScrollPane
  val scroll_pane_desc = new JScrollPane
  val tree = MyTree.load

  def main(args: Array[String]): Unit = {
    search_text.addActionListener(new ActionListener {
        def actionPerformed(ev: ActionEvent) = {
          new MyWorker(log_area) {
            var ar: Array[Array[AnyRef]] = null
            def doInBackground() = {
              ar = Lucene.search(search_text.getText)
              Lucene.close
            }
            override def done() = {
              println(ar.toString)
              if (ar.isEmpty) {
                val suggestions = Lucene.spellcheck(search_text.getText)
                var maybe = ""
                for (s <- suggestions) {
                  maybe += " " + s
                }
                JOptionPane.showMessageDialog(null, "No hit! Maybe" + maybe)
              } else {
                val table = new MyTable(body, log_area, desc_area)
                //table.setRowSelectionAllowed(false)
                //table.setRowHeight(35)
                table.setDefaultEditor(classOf[AnyRef], null)
                table.setContents(ar)
                body.addTab(search_text.getText, new JScrollPane(table))
                body.setSelectedIndex(body.getTabCount - 1)
              }
            }
          }.execute
        }
      })

    /*
    val root = new DefaultMutableTreeNode(new TreeNodeValue("KEYWORD", "", ""))
    val node1 = new DefaultMutableTreeNode(new TreeNodeValue("ドラマ", "ドラマ", ""))
    val node2 = new DefaultMutableTreeNode(new TreeNodeValue("海外", "海外", "", false))
    val node3 = new DefaultMutableTreeNode(new TreeNodeValue("!韓", "NOT 韓", ""))
    root.add(node1)
    node1.add(node2)
    node2.add(node3)
    root.add(new DefaultMutableTreeNode(new TreeNodeValue("SF", "SF", "")))
    val model = new DefaultTreeModel(root)
    tree.setModel(model)
    */

    update_button.addActionListener(new ActionListener {
        def actionPerformed(ev: ActionEvent) = {
          new MyWorker(log_area) {
            def doInBackground() = Util.fetch(Grab)
            override def done() = {
            }
          }.execute
        }
      })

    update_button.setButtonStyle(ButtonStyle.TOOLBOX_STYLE)
    for (plugin <- plugins) {
      update_button.add(new JMenuItem(plugin.getPluginName))
    }
    update_button.add(new JSeparator)
    update_button.add(new AbstractAction("設定") {
      def actionPerformed(e: ActionEvent) = {
        val dlg = new ConfigDialog(main_frame)
        dlg.show
        dlg.asInstanceOf[Dialog].dispose
      }
    })

    body.setShowCloseButton(true)
    body.setShowCloseButtonOnTab(true)

    main_frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    log_area.setRows(10)
    scroll_pane_log.setViewportView(log_area)
    desc_area.setRows(10)
    desc_area.setLineWrap(true)
    scroll_pane_desc.setViewportView(desc_area)

    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS))
    header.add(new JLabel("Search"))
    header.add(search_text)
    header.add(update_button)
    //header.add(new RatingBar)

    footer.addTab("ログ", scroll_pane_log)
    footer.addTab("詳細", scroll_pane_desc)
    footer.setTabPlacement(SwingConstants.BOTTOM)

    tree.setPreferredSize(new Dimension(160, 360))
    left.add(new JScrollPane(tree))

    main_frame.setLayout(new BorderLayout)
    main_frame.add(main_panel, BorderLayout.CENTER)
    main_frame.add(status_bar, BorderLayout.SOUTH)

    main_panel.setLayout(new BorderLayout)
    main_panel.add(header, BorderLayout.NORTH)
    main_panel.add(body, BorderLayout.CENTER)
    main_panel.add(footer, BorderLayout.SOUTH)
    main_panel.add(left, BorderLayout.WEST)

    menu_bar.add(new JMenuItem("File"))
    
    main_frame.setJMenuBar(menu_bar)

    status_bar.setMainLeftComponent(new JLabel("welcome"))
    status_bar.addRightComponent(new JLabel("DB 1.9MB"), 40)
    status_bar.addRightComponent(new JLabel("IDX 3.8MB"), 40)
    status_bar.addRightComponent(new JLabel("2010/02/26"), 40)
    status_bar.addRightComponent(new JLabel("11:59 PM"), 30)
    
    main_frame.setSize(ConfigData.x, ConfigData.y)
    main_frame.setLocationRelativeTo(null)

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    SwingUtilities.updateComponentTreeUI(main_frame)

    main_frame.setVisible(true)
  }
}
