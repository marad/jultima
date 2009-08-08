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
import java.io.FilenameFilter;
import java.util.HashSet;

/**
 *
 * @author morti
 */
public class Workspace {
    /**
     * Workspace path
     */
    private String path;

    /**
     * Workspace file array
     */
    private File[] files;

    /**
     * Listeners
     */
    private HashSet<WorkspaceListener> listeners = new HashSet<WorkspaceListener>();

    /**
     * Allows openning only *.java files
     */
    final private FilenameFilter javaFilter = new FilenameFilter() {
	public boolean accept(File dir, String name) {
	    if(name.endsWith(".java")) return true;
	    return false;
	}
    };

    public Workspace(String path) {
	this.path = path;
	File dir = new File(path);
	scanFiles();
    }

    /**
     * Adds new listeners
     * @param l
     */
    public void addListener(WorkspaceListener l) {
	listeners.add(l);
	for(WorkspaceListener list : listeners) {
		list.fileListChange(files);
	}
    }

    /**
     * Removes listener
     * @param l
     */
    public void removeListener(WorkspaceListener l) {
	listeners.remove(l);
    }

    /**
     * Scans workspace folder and finds all files.
     */
    public void scanFiles() {
	File dir = new File(path);
	if(dir.exists() && dir.isDirectory()) {
	    files = dir.listFiles(javaFilter);

	    for(WorkspaceListener l : listeners) {
		l.fileListChange(files);
	    }
	}
    }

    /**
     *
     * @return Returns workspace path.
     */
    public String getPath() {
	return path;
    }
}
