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
import java.awt.Frame
import java.awt.Label
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JSeparator
import javax.swing.JTextField

class ConfigDialog(val owner: Frame) extends JDialog(owner, "設定", true) {

  {
    val adapter = new PresentationModel(ConfigData)

    val layout = new FormLayout(
      "3dlu, right:pref, 6dlu, 50dlu, 4dlu, default",  // columns
      "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"             // rows
    )
    val cc = new CellConstraints

    this.setLayout(layout)
    this.add(new JSeparator,             cc.xyw(2, 2, 5))
    this.add(new Label("データベース"),   cc.xy(2, 4))
    this.add(BasicComponentFactory.createTextField(adapter.getModel("db")),
                                         cc.xy(4, 4))
    this.add(new JTextField,             cc.xy(4, 4))
    this.add(new Label("インデックス"),   cc.xy(2, 6))
    this.add(new JTextField,             cc.xy(4, 6))
    this.add(new JButton("\u2026"),      cc.xy(6, 8));

    this.setLocationRelativeTo(null)
    this.setSize(320, 240)
  }
}
