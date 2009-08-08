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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import jultima.script.BaseScript;

/**
 *
 * @author morti
 */
public class ScriptRunner extends Thread {

    String classpath;
    String scriptName;

    private HashSet<ScriptListener> listeners = new HashSet<ScriptListener>();

    public ScriptRunner(String classpath, String script) {
	this.classpath = classpath;
	scriptName = script;
    }

    public void addListener(ScriptListener l) {
	listeners.add(l);
    }
    
    @Override
    public void run() {
	try {
	    URL[] urls = new URL[] { new File(classpath).toURI().toURL() };
	    URLClassLoader loader = new URLClassLoader(urls);

	    BaseScript script = (BaseScript)
		    loader.loadClass(scriptName).newInstance();
	    script.run();

	    for(ScriptListener l : listeners)
		l.scriptFinished();
	}
	catch( Exception e ) {
	    for(ScriptListener l : listeners)
		l.scriptException(e);
	    e.printStackTrace();
	}
    }
}
