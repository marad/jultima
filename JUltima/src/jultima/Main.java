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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import jultima.gui.MainFrame;

/**
 *
 * @author morti
 */
public class Main {

    public static Properties config = new Properties();
    public static MainFrame mainFrame = null;

    public static String getConfigName() {
	return "jultima.config";
    }

    public static boolean loadConfig() {
	if(!new File(getConfigName()).exists()) return false;
	FileInputStream fis = null;
	try {
	    fis = new FileInputStream(getConfigName());
	    config.load(fis);
	} catch(IOException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    try { fis.close(); }
	    catch (IOException e) { e.printStackTrace(); }
	}
	return true;
    }

    public static boolean saveConfig() {
	FileOutputStream fos = null;
	try {
	    fos = new FileOutputStream(getConfigName());
	    config.store(fos, "JUltima config file");
	} catch(IOException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    try { fos.close(); }
	    catch (IOException e) { e.printStackTrace(); }
	}

	return true;
    }

    /**
     * Returns path to the Java Runtime Environment
     * @return Path to JRE
     */
    private static String findJRE() {
	File java = new File("C:/Program Files/Java");
	if(java.exists()) {
	    String[] folders = java.list(new FilenameFilter() {
		public boolean accept(File dir, String name) {
		    return dir.isDirectory() && name.contains("jre");
		}
	    });

	    if(folders != null && folders.length > 0) {
		
		String path;
		if(folders.length > 1) {
		    int i = JOptionPane.showOptionDialog(null,
			"Choose JRE to use:\n" +
			"Usualy it's the best to use\nthe newest one" +
			"(with the highest number)",
			"Chosing JRE", JOptionPane.OK_OPTION, 
			JOptionPane.QUESTION_MESSAGE, null, folders, null);
		    path = java.getAbsolutePath() + "\\" + folders[i];
		} else path = java.getAbsolutePath() + "\\" + folders[0];
		return path;
	    }
	}

	// TODO nie znalazlem JRE to pokaz okno wyboru
	JOptionPane.showMessageDialog(null,
	    "JUltima tried to find Java Runtime Environment (JRE) path\n" +
	    "but it couldn't find it. You'll be asked now to choose JRE directory.");
	JFileChooser chooser = new JFileChooser();
	chooser.setDialogTitle("Choose JRE directory");
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	chooser.setAcceptAllFileFilterUsed(false);

	while(true) switch(chooser.showOpenDialog(null)) {
	    case JFileChooser.APPROVE_OPTION:
		File f = chooser.getSelectedFile();
		File rt = new File(f.getAbsolutePath() + "\\lib\\rt.jar" );
		if(!f.exists()) {
		    JOptionPane.showMessageDialog(null,
			"This directory does not exist.");
		    break;
		}
		if(!rt.exists()) {
		    JOptionPane.showMessageDialog(null,
			"It's not valid JRE directory.");
		    break;
		}
		return chooser.getSelectedFile().getAbsolutePath();
		
	    default:
		return null;
	}
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//	    if(!loadConfig()) {
//		String jrePath = findJRE();
//		if(jrePath != null) {
//		    config.setProperty("jrePath", jrePath);
//		    saveConfig();
//		}
//		else {
//		    JOptionPane.showMessageDialog(null,
//			"JRE was not found.\nProgram will terminate.",
//			"JRE not found", JOptionPane.ERROR_MESSAGE);
//		    return;
//		}
//	    }
	    
	    mainFrame = new MainFrame(args);
	    mainFrame.setVisible(true);	  
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
