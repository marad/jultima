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

import jsyntaxpane.syntaxkits.JavaSyntaxKit;

/**
 *
 * @author morti
 */
public class MyEditorKit extends JavaSyntaxKit {

    public MyEditorKit() {
	super();
	setProperty("Action.parenthesis", "jultima.actions.DummyAction");
	setProperty("Action.quotes", "jultima.actions.DummyAction");
	setProperty("Action.double-quotes", "jultima.actions.DummyAction");
	setProperty("Action.brackets", "jultima.actions.DummyAction");
	setProperty("Action.indent", "jultima.actions.DummyAction");
	setProperty("Action.jindent", "jultima.actions.DummyAction");
	setProperty("Action.close-curly","jultima.actions.DummyAction");
    }

}
