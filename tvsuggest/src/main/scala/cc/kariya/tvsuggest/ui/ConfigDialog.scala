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

  class ConfigDialog(val owner: Frame) extends JDialog(owner, "設定", true) {

    {
      val trigger = new Trigger
      val adapter = new PresentationModel(ConfigData, trigger)

      val layout = new FormLayout(
        "3dlu, right:pref, 6dlu, 50dlu, 4dlu, default, default",  // columns
        "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" // rows
      )
      val cc = new CellConstraints

      this.setLayout(layout)
      this.add(new JSeparator,        	      cc.xyw(2, 2, 5))
      this.add(new Label("データベース"), 	cc.xy(2, 4))
      this.add(BasicComponentFactory.createTextField(adapter.getBufferedModel("db")),
                                              cc.xy(4, 4))
      this.add(new Label("インデックス"),   cc.xy(2, 6))
      this.add(BasicComponentFactory.createTextField(adapter.getBufferedModel("index")),
                                              cc.xy(4, 6))
      this.add(new JSeparator,        	      cc.xyw(2, 8, 5))
      this.add(new Label("X"), 			        cc.xy(2, 10))
      this.add(BasicComponentFactory.createIntegerField(adapter.getBufferedModel("x")),
                                              cc.xy(4, 10))
      this.add(new Label("Y"),   			      cc.xy(2, 12))
      this.add(BasicComponentFactory.createIntegerField(adapter.getBufferedModel("y")),
                                              cc.xy(4, 12))
      this.add(new JSeparator,        	      cc.xyw(2, 14, 5))
      val _ = {
        val buttonOK = new JButton("OK")
        val buttonNG = new JButton("Cancel")
        buttonOK.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) = {
            ConfigDialog.this.hide
            trigger.triggerCommit
            XML.save(ConfigData.inifile, ConfigData.toXML, "UTF-8", true, null)
          }
        })
        buttonNG.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) = {
            ConfigDialog.this.hide
            trigger.triggerFlush
          }
        })
        this.add(buttonNG,                     cc.xy(6, 16));
        this.add(buttonOK,                     cc.xy(7, 16));
      }

      adapter.addBeanPropertyChangeListener(new PropertyChangeListener {
        def propertyChange(e: PropertyChangeEvent) = {
          if (e.getPropertyName == "x") {
            val x = e.getNewValue.asInstanceOf[Int]
            UIMain.main_frame.setSize(x, UIMain.main_frame.getSize.getHeight.toInt)
          } else if (e.getPropertyName == "y") {
            val y = e.getNewValue.asInstanceOf[Int]
            UIMain.main_frame.setSize(UIMain.main_frame.getSize.getWidth.toInt, y)
          }
        }
      })

      this.setLocationRelativeTo(null)
      this.setSize(320, 240)

      this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
      
    }
  }
