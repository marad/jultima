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
import jultima.gui.MainFrame;

/**
 *
 * @author morti
 */
public class enterKeyListener implements KeyListener {

    String indent = "\t";
    JEditorPane edit;

    public enterKeyListener(JEditorPane edit) {
	this.edit = edit;
    }
  
    public void keyTyped(KeyEvent e) {
	if(e.getKeyChar() != '\n') return;
	Document doc = edit.getDocument();
	try {

	    doc.insertString(edit.getCaretPosition(), indent, null);
	} catch (BadLocationException ex) {
	    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public void keyPressed(KeyEvent e) {
	if(e.getKeyChar() != '\n') return;
	indent = "";
	int pos = edit.getCaretPosition();

	// get the document text
	String text="";
	try {
	    text = edit.getDocument().getText(0, edit.getDocument().getLength());
	} catch (BadLocationException ex) {
	    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	}

	// find current line and select split it
	// into pre and post cursor parts
	int begin = text.lastIndexOf('\n', pos-1)+1;
	int end = text.indexOf('\n', pos);
	String preCursor = text.substring(begin, pos);
	String postCursor = text.substring(pos, end);

	// find current indent
	int index=0;
	while(index < preCursor.length()) {
	    char c = preCursor.charAt(index++);

	    if(c == '\t' || c == ' ') indent += c;
	    else break;
	}

	preCursor = preCursor.trim();
	if(preCursor.endsWith("{")) { 
//	    if(postCursor.isEmpty()) {
//		try {
//		    edit.getDocument().insertString(pos, "\n" + indent + "}", null);
//		    edit.setCaretPosition(pos);
//		} catch (BadLocationException ex) {
//		    Logger.getLogger(enterKeyListener.class.getName()).log(Level.SEVERE, null, ex);
//		}
//	    }
	    indent += '\t';
	}
    }

    public void keyReleased(KeyEvent e) {
    }

}
