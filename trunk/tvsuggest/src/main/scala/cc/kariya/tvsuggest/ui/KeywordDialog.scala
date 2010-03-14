  /*
   * To change this template, choose Tools | Templates
   * and open the template in the editor.
   */

  package cc.kariya.tvsuggest.ui

  import cc.kariya.tvsuggest.engine.ConfigData
  import com.jgoodies.binding.PresentationModel
  import com.jgoodies.binding.adapter.BasicComponentFactory
  import com.jgoodies.forms.layout.CellConstraints
  import com.jgoodies.forms.layout.FormLayout
  import java.io.Serializable
  import xml.XML
  import java.awt.{Component, Window, Frame, Label}
  import java.util.EventObject
  import java.awt.event._
  import javax.swing._
  import java.beans.{PropertyChangeEvent, PropertyChangeListener}
  import com.jgoodies.binding.value.Trigger
  import cc.kariya.tvsuggest.util.BoundPropertyBean

  class KeywordDialog(val owner: Frame, val data: TreeNodeValue) extends JDialog(owner, "キーワード", true) {

    def this(owner: Frame) = {
      this(owner, new TreeNodeValue("", "", "",  true))
    }

    {
      val trigger = new Trigger
      val adapter = new PresentationModel(data, trigger)

      val layout = new FormLayout(
        "3dlu, right:pref, 6dlu, 50dlu, 4dlu, default",  // columns
        "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" // rows
      )
      val cc = new CellConstraints

      this.setLayout(layout)
      this.add(new Label("ラベル"), 	      cc.xy(2, 2))
      this.add(BasicComponentFactory.createTextField(adapter.getBufferedModel("label")),
                                              cc.xyw(4, 2, 3))
      this.add(new Label("条件"),           cc.xy(2, 4))
      this.add(BasicComponentFactory.createTextField(adapter.getBufferedModel("query")),
                                              cc.xyw(4, 4, 3))
      //this.add(new Label("検索対象"),       cc.xy(2, 4))
      this.add(BasicComponentFactory.createCheckBox(adapter.getBufferedModel("searchable"), "検索対象"),
                                              cc.xy(4, 6))
      val _ = {
        val buttonOK = new JButton("OK")
        val buttonNG = new JButton("Cancel")
        buttonOK.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) = {
            KeywordDialog.this.hide
            trigger.triggerCommit
          }
        })
        buttonNG.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) = {
            KeywordDialog.this.hide
            trigger.triggerFlush
          }
        })
        this.add(buttonNG,                     cc.xy(4, 8));
        this.add(buttonOK,                     cc.xy(6, 8));
      }


      this.setLocationRelativeTo(null)
      this.setSize(320, 240)

      this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      
    }
  }
