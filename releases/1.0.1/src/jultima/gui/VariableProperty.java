/*
    This file is part of JUltima.

    JUltima is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JUltima is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JUltima.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jultima.gui;

import com.l2fprod.common.propertysheet.AbstractProperty;

/**
 *
 * @author morti
 */
public class VariableProperty extends AbstractProperty {

    private String category;
    private String name;
    private Class type;
    private String description="";
    private boolean editable = false;

    public VariableProperty(String category, String name, Class type, String desc) {
	this.category = category;
	this.name = name;
	this.type = type;
	this.description = desc;
    }

    public VariableProperty(String category, String name, Class type, String desc, boolean editable) {
	this.category = category;
	this.name = name;
	this.type = type;
	this.description = desc;
	this.editable = editable;
    }

    public String getName() {
	return name;
    }

    public String getDisplayName() {
	return name;
    }

    public String getShortDescription() {
	return description;
    }

    public Class getType() {
	return type;
    }

    public boolean isEditable() {
	return true;
    }

    public String getCategory() {
	return category;
    }

    public void readFromObject(Object arg0) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeToObject(Object arg0) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
