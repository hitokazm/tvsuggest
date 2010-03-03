/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cc.kariya.tvsuggest.util

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

object DateUtil {

  val formatter = new SimpleDateFormat()
  val cal = new GregorianCalendar()

  def format(s:String, from: String, to: String) = {
    formatter.applyPattern(from)
    val date = formatter.parse(s)
    cal.setTime(date)
    formatter.applyPattern(to)
    formatter.format(cal.getTime)
  }
}
