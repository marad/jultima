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

package jultima;

/**
 *
 * @author morti
 */
public abstract class ScriptListener {

    protected int tabIndex = 0;
    protected String name = null;

    public ScriptListener(int index, String name) {
	tabIndex = index;
	this.name = name;
    }
    
    /**
     * Called when a script finishes his work.
     */
    public void scriptFinished() {}

    /**
     * Called when script throws some exception.
     */
    public void scriptException(Exception e) {}
}
