package cc.kariya.tvsuggest.ui

import reflect.BeanProperty
import cc.kariya.tvsuggest.util.BoundPropertyBean

/**
 * Created by IntelliJ IDEA.
 * User: kariya
 * Date: 2010/03/13
 * Time: 23:38:09
 * To change this template use File | Settings | File Templates.
 */

class TreeNodeValue extends BoundPropertyBean
{
  //@BeanProperty
  var label: String = "";
  //@BeanProperty
  var query: String = "";
  @BeanProperty
  var rank: String = "";
  //@BeanProperty
  var searchable: Boolean = true;
  @BeanProperty
  var expanded: Boolean = true

  def this(label: String, query: String, rank: String, searchable: Boolean, expanded: Boolean = true) = {
    this()
    this.label = label
    this.query = query
    this.rank = rank
    this.searchable = searchable
    this.expanded = expanded
  }

  override def toString = if (label != "") label else  query

  def getLabel = label
  def setLabel(newvalue: String) = {
    val oldvalue = label
    label = newvalue
    firePropertyChange("label", oldvalue, newvalue)
  }

  def getQuery = query
  def setQuery(newvalue: String) = {
    val oldvalue = query
    query = newvalue
    firePropertyChange("query", oldvalue, newvalue)
  }

  def getSearchable = searchable
  def setSeachable(newvalue: Boolean) = {
    val oldvalue = searchable
    searchable = newvalue
    firePropertyChange("searchable", oldvalue, newvalue)
  }

}
