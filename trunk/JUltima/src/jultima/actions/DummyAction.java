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

package jultima.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.actions.DefaultSyntaxAction;

/**
 * This action Does nothing but its used to turn off
 * some bothering features in JSyntaxPane.
 * @author morti
 */
public class DummyAction extends DefaultSyntaxAction {

    public DummyAction() {
	super("DUMMY-ACTION");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc, int dot, ActionEvent e) {
	super.actionPerformed(target, sDoc, dot, e);
	System.out.println("Dummy action");
    }


}
