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

package jultima.gui.editKeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author morti
 */
public class rightBraceKeyListener implements KeyListener {

    JEditorPane edit;

    public rightBraceKeyListener(JEditorPane edit) {
	this.edit = edit;
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
	if(e.getKeyChar() != '}') return;
	Document doc = edit.getDocument();
	try {
	    String text = doc.getText(0, doc.getLength());
	    int pos = edit.getCaretPosition();
	    int begin = text.lastIndexOf('\n', pos-1)+1;
	    if(pos - begin == 0) return;

	    if(text.charAt(pos-1) == '\t') {
		doc.remove(pos-1, 1);
	    }

	} catch (BadLocationException ex) {
	    Logger.getLogger(rightBraceKeyListener.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public void keyReleased(KeyEvent e) {
    }

}
