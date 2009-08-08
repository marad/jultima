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

/*
 * MainFrame.java
 *
 * Created on 2009-07-30, 21:47:20
 */

package jultima.gui;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jultima.GameDLL;
import jultima.MyEditorKit;
import jultima.ScriptListener;
import jultima.ScriptRunner;
import jultima.Workspace;
import jultima.WorkspaceListener;
import jultima.gui.editKeyListeners.enterKeyListener;
import jultima.gui.editKeyListeners.rightBraceKeyListener;

/**
 *
 * @author morti
 */
public class MainFrame extends javax.swing.JFrame {
   
    /**
     * Loads startup config and auto runs scripts
     * @param args
     */
    private void loadStartup(String[] args) {
	if(args.length < 1) return;

	File f = new File(args[0]);
	if(!f.exists()) return;

	Properties conf = new Properties();
	FileInputStream fis = null;
	try {
	    fis = new FileInputStream(f);
	    conf.load(fis);
	} catch(IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		fis.close();
	    } catch (IOException ex) {
		Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	
	String wsPath = conf.getProperty("Workspace");
	String autoRunString = conf.getProperty("AutoRun");

	if(wsPath != null && !wsPath.isEmpty()) {
	    workspace = new Workspace(wsPath);

	    if(autoRunString == null || autoRunString.isEmpty()) return;
	    autoRunString = autoRunString.trim();
	    String[] autoRun = autoRunString.split("\\s*,\\s*");
	    for(String script : autoRun) {
		openScript(script);
		runScript(script);
	    }
	}
    }

    private void createEditTab(String name, String code) {
	final JEditorPane edit = new JEditorPane();
	JScrollPane scroll = new JScrollPane(edit);
	edit.setContentType("text/java");
	edit.setEditorKit(editorKit);
	edit.setText(code);
	edit.getDocument().addDocumentListener(changeListener);
	edit.addKeyListener(new enterKeyListener(edit));
	edit.addKeyListener(new rightBraceKeyListener(edit));
	jTabbedPane1.addTab(name, scroll);
	jTabbedPane1.setTabComponentAt(jTabbedPane1.getTabCount()-1, new TabComponent(name));
	jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount()-1);
	
	for(ScriptEventListener sel : scriptEventListeners)
	    sel.scriptOpened(name);
    }

    /**
     * Opens script in a new Tab
     * @param name Name of the script file
     */
    private void openScript(String name) {
	String fileName = name + ".java";

	for(int i=0; i < jTabbedPane1.getTabCount(); i++)
	    if( jTabbedPane1.getTitleAt(i).equals(name)) {
		jTabbedPane1.setSelectedIndex(i);
		return;
	    }

	String code = "";
	try {
	    FileInputStream in = new FileInputStream(workspace.getPath() + "/" + fileName);

	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    String line;
	    while((line = reader.readLine()) != null) {
		code += line + System.getProperty("line.separator");
	    }
	    reader.close();
	    in.close();
	} catch(Exception e) {
	    System.err.println("Read error: " + e.getLocalizedMessage());
	    return;
	}

	createEditTab(name, code);
    }

    /**
     * Saves script opened at specified tab
     * @param selectedIndex Tab index
     */
    private void saveScript(int selectedIndex) {
	FileOutputStream out = null;
	try {
	    String path = workspace.getPath();
	    JScrollPane scroll = (JScrollPane) jTabbedPane1.getComponentAt(selectedIndex);
	    JEditorPane edit = (JEditorPane) scroll.getViewport().getView();

	    String name = jTabbedPane1.getTitleAt(selectedIndex);
	    out = new FileOutputStream(path + "/" + name + ".java");
	    PrintWriter writer = new PrintWriter(out);
	    writer.write(edit.getText());
	    writer.close();
	    workspace.scanFiles();

	    TabComponent label = (TabComponent)
		    jTabbedPane1.getTabComponentAt(selectedIndex);
	    label.setText(name);
	    label.setSaved(true);
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	} finally {
	    try {
		if(out != null) out.close();
	    } catch (IOException ex) {
		Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    int getTabIndex(String scriptName) {
	for(int i=0; i < jTabbedPane1.getTabCount(); i++) {
	    if(jTabbedPane1.getTitleAt(i).equals(scriptName)) {
		return i;
	    }
	}
	return -1;
    }

    void runScript(String scriptName) {
	for(int i=0; i < jTabbedPane1.getTabCount(); i++) {
	    if(jTabbedPane1.getTitleAt(i).equals(scriptName)) {
		runScript(i);
		break;
	    }
	}
    }

    /**
     * Runs script opened at specified tab
     * @param selectedIndex Tab number containig the script
     */
    void runScript(final int selectedIndex) {
	TabComponent tabComp = (TabComponent)
		jTabbedPane1.getTabComponentAt(selectedIndex);
	if(tabComp.thread != null) {
	    if(tabComp.getState() == TabComponent.PAUSED) {
		tabComp.thread.resume();
		tabComp.setState(TabComponent.RUNNING);
	    }
	    else {
		compileOut.setText("");
		compileOut.append("Script is already running...");
	    }
	    return;
	}
	if(!tabComp.compiled) {
	    compileScript(selectedIndex, true);
	    return;
	}

	try {
	    String scriptName = jTabbedPane1.getTitleAt(selectedIndex);

	    ScriptListener scriptListener =
		    new ScriptListener(selectedIndex, scriptName) {
		@Override
		public void scriptFinished() {
		    TabComponent tabComp = (TabComponent)
			    jTabbedPane1.getTabComponentAt(tabIndex);
		    tabComp.thread = null;
		    tabComp.setState(TabComponent.STOPPED);
		}

		@Override
		public void scriptException(Exception e) {
		    compileOut.setText("Exception in " + name + ":\n");
		    compileOut.append(e.getMessage());
		    scriptFinished();
		}
	    };

	    ScriptRunner runner = new ScriptRunner(workspace.getPath(), scriptName);

	    tabComp.thread = runner;
	    tabComp.setState(TabComponent.RUNNING);

	    runner.addListener(scriptListener);
	    runner.start();
	}
	catch (Exception ex) {
	    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    /**
     * Compiles script opened at specified tab
     * @param selectedIndex Tab index
     * @param  run Tells if you want to run script after compilation
     */
    private void compileScript(final int selectedIndex, final boolean run) {
	TabComponent tabComp = (TabComponent)
		jTabbedPane1.getTabComponentAt(selectedIndex);
	if(!tabComp.saved)
	    saveScript(selectedIndex);

	String compiler = "jikes\\bin\\jikes.exe ";
	String fileName = jTabbedPane1.getTitleAt(selectedIndex) + ".java";
	String classPath = ".;./classpath/cp.jar;" + workspace.getPath();
	String bootCP = System.getProperty("java.home") + "\\lib\\rt.jar";
	String cmd = compiler + " " +
		"-classpath \""+ classPath +"\" " +
		"-bootclasspath \"" + bootCP + "\" " +
		"\"" + workspace.getPath() + "/" + fileName + "\"";

	Runtime rt = Runtime.getRuntime();
	try {
	    compileOut.setText("");
	    compileOut.append("Compiling...\n");
	    final Process p = rt.exec(cmd);
	    ProcessInputReader inReader = new ProcessInputReader(p.getInputStream());
	    ProcessInputReader errReader = new ProcessInputReader(p.getErrorStream());

	    inReader.start();
	    errReader.start();

	    Thread th = new Thread() {
		@Override
		public void run() {
		    try {
			p.waitFor();
			compileOut.append("Done\n");
			if( run ) {
			    runScript(selectedIndex);
			}
		    } catch (InterruptedException ex) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    };

	    th.start();
	    tabComp.compiled = true;
	} catch (IOException ex) {
	    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    void stopScript(int selectedIndex) {
	TabComponent tabComp = (TabComponent)
		jTabbedPane1.getTabComponentAt(selectedIndex);
	if(tabComp.thread != null) {
	    tabComp.thread.stop();
	    tabComp.thread = null;
	}
	tabComp.setState(TabComponent.STOPPED);
    }

    void pauseScript(int selectedIndex) {
	TabComponent tabComp = (TabComponent)
		jTabbedPane1.getTabComponentAt(selectedIndex);
	tabComp.thread.suspend();
	tabComp.setState(TabComponent.PAUSED);
    }

    private void initVariables() {
	propSheet.addProperty(new VariableProperty("Last Action", "LObjectID", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Last Action", "LObjectType", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LTargetID", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LTargetKind", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LTargetTile", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LTargetX", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LTargetY", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LTargetZ", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LLiftedID", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LLiftedKind", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LSkill", int.class, ""));
	propSheet.addProperty(new VariableProperty("Last Action", "LSpell", int.class, ""));
	
	propSheet.addProperty(new VariableProperty("Character", "CharPosX", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharPosY", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharPosZ", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharDir", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharID", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharName", String.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharStatus", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "CharType", int.class, ""));
	propSheet.addProperty(new VariableProperty("Character", "BackpackID", int.class, ""));
	
	propSheet.addProperty(new VariableProperty("Statusbar", "Str", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Dex", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Int", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Sex", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Hits", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Stamina", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Mana", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxHits", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxStam", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxMana", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Weight", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxWeight", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MinDmg", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxDmg", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxStats", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Gold", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Luck", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "Followers", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "MaxFol", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "AR", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "FR", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "CR", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "PR", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "ER", int.class, ""));
	propSheet.addProperty(new VariableProperty("Statusbar", "TP", int.class, ""));

	propSheet.addProperty(new VariableProperty("Container", "ContID", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Container", "ContName", String.class, ""));
	propSheet.addProperty(new VariableProperty("Container", "ContPosX", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Container", "ContPosY", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Container", "ContSizeX", int.class, ""));
	propSheet.addProperty(new VariableProperty("Container", "ContSizeY", int.class, ""));
	propSheet.addProperty(new VariableProperty("Container", "ContKind", int.class, ""));
	propSheet.addProperty(new VariableProperty("Container", "ContType", int.class, ""));
	propSheet.addProperty(new VariableProperty("Container", "NextCPosX", int.class, ""));
	propSheet.addProperty(new VariableProperty("Container", "NextCPosY", int.class, ""));

	propSheet.addProperty(new VariableProperty("Client", "CliCnt", int.class, ""));
	propSheet.addProperty(new VariableProperty("Client", "CliNr", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Client", "CliTop", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Client", "CliLang", String.class, ""));
	propSheet.addProperty(new VariableProperty("Client", "CliLogged", int.class, ""));
	propSheet.addProperty(new VariableProperty("Client", "CliXRes", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Client", "CliYRes", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Client", "CliLeft", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Client", "CliVer", int.class, "", true));

	propSheet.addProperty(new VariableProperty("Miscallenous", "TargCurs", boolean.class, ""));
	propSheet.addProperty(new VariableProperty("Miscallenous", "CursKind", int.class, ""));
	propSheet.addProperty(new VariableProperty("Miscallenous", "Shard", String.class, ""));
	propSheet.addProperty(new VariableProperty("Miscallenous", "LShard", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Miscallenous", "EnemyHits", int.class, ""));
	propSheet.addProperty(new VariableProperty("Miscallenous", "EnemyID", int.class, ""));
	propSheet.addProperty(new VariableProperty("Miscallenous", "LHandID", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Miscallenous", "RHandID", int.class, "", true));
	propSheet.addProperty(new VariableProperty("Miscallenous", "SysMsg", String.class, ""));

	Thread th = new Thread() {
	    PropertySheetPanel prop = propSheet;
	    @Override
	    public void run() {
		while(true) {

		    Property[] pArr = (Property[]) prop.getProperties();
		    GameDLL gameDLL = new GameDLL();
		    gameDLL.open();

		    gameDLL.setTop(0);
		    gameDLL.pushStrVal("SetVar");
		    gameDLL.pushStrVal("CliNr");
		    gameDLL.pushInteger(1);
		    gameDLL.execute();

		    for(Property p : pArr) {
			gameDLL.setTop(0);
			gameDLL.pushStrVal("GetVar");
			gameDLL.pushStrVal(p.getName());
			if(gameDLL.execute() != 0) {
			    System.out.println("Error fetching " + p.getName());
			    continue;
			}

			if(p.getType() == String.class) {
			    String s = gameDLL.getString(1);
			    if(!s.equals(p.getValue())) p.setValue(s);
			} else if (p.getType() == int.class) {
			    int i = gameDLL.getInteger(1);
			    if(p.getValue() == null) p.setValue(i);
			    else if(i != ((Integer)p.getValue()).intValue()) p.setValue(i);
			}
		    }
		    gameDLL.close();
		    try {
			Thread.sleep(100);
		    } catch (InterruptedException ex) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    }
	};
	th.setDaemon(true);
	th.start();
    }

    void addScriptEventListener(ScriptEventListener scrEvtLis) {
	scriptEventListeners.add(scrEvtLis);
    }

    /**
     * Component viewed at tabs. 
     * Contains file name loaded in corresponding
     * editor and information about save and 
     * compilation.
     */
    public class TabComponent extends JLabel {

	final public static int RUNNING = 0;
	final public static int PAUSED = 1;
	final public static int STOPPED = 2;

	/**
	 * Script execution thread
	 */
	Thread thread = null;

	boolean compiled = false;
	private boolean saved = false;

	private int state = STOPPED;

	private String fileName;

	/**
	 * Called on new tab
	 * @param text
	 */
	public TabComponent(String text) {
	    fileName = text;
	    File code = new File(workspace.getPath() + "\\" + text + ".java");
	    File bin = new File(workspace.getPath() + "\\" + text + ".class");
	    if(code.exists()) { 
		setSaved(true);
		if(bin.exists()) {
		    if(code.lastModified() > bin.lastModified()) {
			compiled = false;
		    } else compiled = true;
		}
	    } else setSaved(false);
	}

	public void setState(int state) {
	    this.state = state;
	    switch(state) {
		case TabComponent.RUNNING:
		    for(ScriptEventListener sel : scriptEventListeners)
			sel.scriptRun(fileName);
		    jTabbedPane1StateChanged(null);
//		    runToolbar.setEnabled(false);
//		    pauseToolbar.setEnabled(true);
//		    stopToolbar.setEnabled(true);
		    setForeground(new Color(0x00AA00)); // bit darker green
		    break;
		case TabComponent.PAUSED:
		    for(ScriptEventListener sel : scriptEventListeners)
			sel.scriptPaused(fileName);
		    jTabbedPane1StateChanged(null);
//		    runToolbar.setEnabled(true);
//		    pauseToolbar.setEnabled(false);
//		    stopToolbar.setEnabled(true);
		    setForeground(new Color(0x888800)); // dark yellow
		    break;
		case TabComponent.STOPPED:
		    for(ScriptEventListener sel : scriptEventListeners)
			sel.scriptStopped(fileName);
		    jTabbedPane1StateChanged(null);
//		    runToolbar.setEnabled(true);
//		    pauseToolbar.setEnabled(false);
//		    stopToolbar.setEnabled(false);
		    setForeground(Color.BLACK);
		    break;
	    }
	}

	public int getState() { return state; }

	public void setSaved(boolean saved) {
	    this.saved = saved;
	    if(saved)
		setText(fileName);
	    else
		setText("<html><i>"+ fileName +"*</i></html>");
	}

	public boolean getSaved() { return saved; }

	public String getScriptName() {
	    return fileName;
	}
	
    }    

    public class ProcessInputReader extends Thread {
	BufferedReader reader;
	public ProcessInputReader(InputStream in) {
	    reader = new BufferedReader(new InputStreamReader(in));
	}

	@Override
	public void run() {
	    try {
		String line;
		while ((line = reader.readLine()) != null) {
		    compileOut.append(line + "\n");
		}
	    } catch (IOException ex) {
		Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }
    
    /**
     * Curent working space
     */
    private Workspace workspace = null;

    /**
     * Holds UO variables
     */
    PropertySheetPanel propSheet;

    /**
     * Shows opened scripts with play/pause/stop buttons
     */
    RunFrame runFrame=null;

    /**
     * Script event listeners set
     */
    HashSet<ScriptEventListener> scriptEventListeners = new HashSet<ScriptEventListener>();

    /**
     * Editor kit used for syntax higlight ant stuff :)
     */
    MyEditorKit editorKit = new MyEditorKit();

    final private WorkspaceListener workspaceListener = new WorkspaceListener() {
	@Override
	public void fileListChange(File[] files) {
	    if(files == null || files.length < 1) return;
	    DefaultListModel model = (DefaultListModel) fileList.getModel();
	    model.clear();
	    for(File f : files) {		
		model.addElement(f.getName().substring(0, f.getName().indexOf('.')));
	    }
	}
    };

    /**
     * Listens for changes in scripts
     */
    final DocumentListener changeListener = new DocumentListener() {

	public void insertUpdate(DocumentEvent e) {changedUpdate(e);}

	public void removeUpdate(DocumentEvent e) {
	    changedUpdate(e);
	}

	public void changedUpdate(DocumentEvent e) {

//	    String name = jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex());
	    TabComponent label = (TabComponent)
		    jTabbedPane1.getTabComponentAt(jTabbedPane1.getSelectedIndex());
	    label.setSaved(false);
	    label.compiled = false;
	}
    };
       
    /**
     * Creates new form MainFrame
     * @param args Command line parameters
     */
    public MainFrame(String[] args) {
	runFrame = new RunFrame(this);
        initComponents();

	propSheet = new PropertySheetPanel();
	propSheet.setMode(PropertySheet.VIEW_AS_CATEGORIES);
	propSheet.setDescriptionVisible(false);
	propSheet.setSortingCategories(false);
	propSheet.setSortingProperties(false);
	propSheet.setRestoreToggleStates(true);
	propSheet.setToolBarVisible(true);

	jSplitPane2.setRightComponent(propSheet);

	initVariables();

	loadStartup(args);

	if(workspace == null) {
	    String path = System.getProperty("user.dir") + "\\workspaces\\default";
	    File file = new File(path);
	    if(!file.exists()) file.mkdirs();
	    System.out.println(path);
	    workspace = new Workspace(path);
	}

	if(workspace != null) workspace.addListener(workspaceListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        fileListPopup = new javax.swing.JPopupMenu();
        deleteFileMenu = new javax.swing.JMenuItem();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        compileOut = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList();
        jToolBar1 = new javax.swing.JToolBar();
        newToolbar = new javax.swing.JButton();
        saveToolbar = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        compileToolbar = new javax.swing.JButton();
        runToolbar = new javax.swing.JButton();
        pauseToolbar = new javax.swing.JButton();
        stopToolbar = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newScriptMenu = new javax.swing.JMenuItem();
        saveMenu = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        closeFileMenu = new javax.swing.JMenuItem();
        exitMenu = new javax.swing.JMenuItem();
        controlMenu = new javax.swing.JMenu();
        compileMenu = new javax.swing.JMenuItem();
        runMenu = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        pauseMenu = new javax.swing.JMenuItem();
        stopMenu = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        deleteFileMenu.setText("Delete");
        deleteFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteFileMenuActionPerformed(evt);
            }
        });
        fileListPopup.add(deleteFileMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JUltima Online");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(100);

        jSplitPane2.setDividerLocation(450);
        jSplitPane2.setResizeWeight(1.0);

        jSplitPane3.setDividerLocation(380);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setResizeWeight(1.0);

        compileOut.setColumns(20);
        compileOut.setEditable(false);
        compileOut.setFont(new java.awt.Font("Monospaced", 0, 10));
        compileOut.setLineWrap(true);
        compileOut.setRows(5);
        compileOut.setTabSize(4);
        compileOut.setWrapStyleWord(true);
        jScrollPane2.setViewportView(compileOut);

        jSplitPane3.setRightComponent(jScrollPane2);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jSplitPane3.setTopComponent(jTabbedPane1);

        jSplitPane2.setLeftComponent(jSplitPane3);

        jSplitPane1.setRightComponent(jSplitPane2);

        fileList.setModel(new DefaultListModel());
        fileList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileList.setComponentPopupMenu(fileListPopup);
        fileList.setMinimumSize(new java.awt.Dimension(33, 80));
        fileList.setPreferredSize(new java.awt.Dimension(33, 80));
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileListMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(fileList);

        jSplitPane1.setLeftComponent(jScrollPane3);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        newToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/document-new.png"))); // NOI18N
        newToolbar.setFocusable(false);
        newToolbar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newToolbar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newScriptMenuActionPerformed(evt);
            }
        });
        jToolBar1.add(newToolbar);

        saveToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/media-floppy.png"))); // NOI18N
        saveToolbar.setFocusable(false);
        saveToolbar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveToolbar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        jToolBar1.add(saveToolbar);
        jToolBar1.add(jSeparator2);

        compileToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/system-run.png"))); // NOI18N
        compileToolbar.setFocusable(false);
        compileToolbar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        compileToolbar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        compileToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileMenuActionPerformed(evt);
            }
        });
        jToolBar1.add(compileToolbar);

        runToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/media-playback-start.png"))); // NOI18N
        runToolbar.setFocusable(false);
        runToolbar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runToolbar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runMenuActionPerformed(evt);
            }
        });
        jToolBar1.add(runToolbar);

        pauseToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/media-playback-pause.png"))); // NOI18N
        pauseToolbar.setEnabled(false);
        pauseToolbar.setFocusable(false);
        pauseToolbar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pauseToolbar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pauseToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseMenuActionPerformed(evt);
            }
        });
        jToolBar1.add(pauseToolbar);

        stopToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/media-playback-stop.png"))); // NOI18N
        stopToolbar.setEnabled(false);
        stopToolbar.setFocusable(false);
        stopToolbar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopToolbar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stopToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopMenuActionPerformed(evt);
            }
        });
        jToolBar1.add(stopToolbar);
        jToolBar1.add(jSeparator4);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/applications-games.png"))); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        fileMenu.setText("File");

        newScriptMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newScriptMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/small-document-new.png"))); // NOI18N
        newScriptMenu.setText("New Script");
        newScriptMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newScriptMenuActionPerformed(evt);
            }
        });
        fileMenu.add(newScriptMenu);

        saveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/small-media-floppy.png"))); // NOI18N
        saveMenu.setText("Save");

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, saveToolbar, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), saveMenu, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        saveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenu);
        fileMenu.add(jSeparator5);

        jMenuItem3.setText("Change Workspace");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem3);

        jMenuItem1.setText("Export Auto Run");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem1);
        fileMenu.add(jSeparator1);

        closeFileMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeFileMenu.setText("Close File");
        closeFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeFileMenuActionPerformed(evt);
            }
        });
        fileMenu.add(closeFileMenu);

        exitMenu.setText("Exit");
        exitMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenu);

        jMenuBar1.add(fileMenu);

        controlMenu.setText("Control");

        compileMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        compileMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/cog.png"))); // NOI18N
        compileMenu.setText("Compile File");
        compileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileMenuActionPerformed(evt);
            }
        });
        controlMenu.add(compileMenu);

        runMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        runMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/control_play.png"))); // NOI18N
        runMenu.setText("Run");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, runToolbar, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), runMenu, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        runMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runMenuActionPerformed(evt);
            }
        });
        controlMenu.add(runMenu);
        controlMenu.add(jSeparator3);

        pauseMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        pauseMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/control_pause.png"))); // NOI18N
        pauseMenu.setText("Pause");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, pauseToolbar, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), pauseMenu, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        pauseMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseMenuActionPerformed(evt);
            }
        });
        controlMenu.add(pauseMenu);

        stopMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        stopMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gfx/control_stop.png"))); // NOI18N
        stopMenu.setText("Stop");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, stopToolbar, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), stopMenu, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        stopMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopMenuActionPerformed(evt);
            }
        });
        controlMenu.add(stopMenu);

        jMenuBar1.add(controlMenu);

        jMenu1.setText("Help");

        jMenuItem2.setText("About");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newScriptMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newScriptMenuActionPerformed
	String name = JOptionPane.showInputDialog("Choose script name:");
	if(name == null || name.isEmpty()) return;
	File f = new File(workspace.getPath() + "\\" + name + ".java");
	if(f.exists()) {
	    JOptionPane.showMessageDialog(this, "This file already exists!");
	    return;
	}
	String sep = System.getProperty("line.separator");
	String code = "import jultima.script.BaseScript;" + sep + sep +
		"public class " + name + " extends BaseScript {" + sep +
		"\tpublic void run() throws Exception {" + sep +
		"\t\t" + sep + "\t}" + sep + "}";

	createEditTab(name, code);
    }//GEN-LAST:event_newScriptMenuActionPerformed

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuActionPerformed
	if(jTabbedPane1.getSelectedIndex() == -1) return;
	saveScript(jTabbedPane1.getSelectedIndex());	
    }//GEN-LAST:event_saveMenuActionPerformed

    private void fileListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileListMouseClicked
	if( evt.getClickCount() < 2 ) return;	
	String name = (String) fileList.getSelectedValue();
	openScript(name);
    }//GEN-LAST:event_fileListMouseClicked

    private void closeFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeFileMenuActionPerformed
	int index = jTabbedPane1.getSelectedIndex();
	TabComponent comp = (TabComponent) jTabbedPane1.getTabComponentAt(index);
	if(!comp.saved) {
	    switch(JOptionPane.showConfirmDialog(this, comp.getScriptName() + " is not saved.\n" +
		"Do you want to save it?")) {
		case JOptionPane.YES_OPTION:
		    saveScript(index);
		    break;
		case JOptionPane.CANCEL_OPTION:
		    return;
	    }
	}
	stopScript(index);
	jTabbedPane1.remove(index);
	for(ScriptEventListener sel : scriptEventListeners)
	    sel.scriptClosed(comp.getScriptName());
    }//GEN-LAST:event_closeFileMenuActionPerformed

    private void compileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileMenuActionPerformed
	if(jTabbedPane1.getSelectedIndex() == -1) return;
	compileScript(jTabbedPane1.getSelectedIndex(), false);
    }//GEN-LAST:event_compileMenuActionPerformed

    private void runMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runMenuActionPerformed
	if(jTabbedPane1.getSelectedIndex() == -1) return;
	runScript(jTabbedPane1.getSelectedIndex());	
    }//GEN-LAST:event_runMenuActionPerformed

    private void deleteFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFileMenuActionPerformed
	if(fileList.getSelectedIndex() == -1) {
	    JOptionPane.showMessageDialog(this, "You have not selected any file!");
	}
	int result = JOptionPane.showConfirmDialog(this, "Do you really want to delete this permanently?", "Confirmation", JOptionPane.YES_NO_OPTION);
	
	switch(result) {
	    case JOptionPane.NO_OPTION:
		return;
	    case JOptionPane.YES_OPTION:
	}

	String code = workspace.getPath() + "/" + fileList.getModel().getElementAt(fileList.getSelectedIndex()) + ".java";
	String bin = workspace.getPath() + "/" + fileList.getModel().getElementAt(fileList.getSelectedIndex()) + ".class";
	File file = new File(code);
	file.delete();

	file = new File(bin);
	if(file.exists()) file.delete();
	workspace.scanFiles();
    }//GEN-LAST:event_deleteFileMenuActionPerformed

    private void exitMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuActionPerformed
	System.exit(0);
    }//GEN-LAST:event_exitMenuActionPerformed

    private void stopMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopMenuActionPerformed
	if(jTabbedPane1.getSelectedIndex() == -1) return;
	stopScript(jTabbedPane1.getSelectedIndex());
    }//GEN-LAST:event_stopMenuActionPerformed

    private void pauseMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseMenuActionPerformed
	if(jTabbedPane1.getSelectedIndex() == -1) return;
	pauseScript(jTabbedPane1.getSelectedIndex());
    }//GEN-LAST:event_pauseMenuActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
	if(jTabbedPane1.getSelectedIndex() == -1) return;
	TabComponent tabComp = (TabComponent)
		jTabbedPane1.getTabComponentAt(jTabbedPane1.getSelectedIndex());

	if(tabComp == null) return;
	switch(tabComp.getState()) {
	    case TabComponent.RUNNING:
		runToolbar.setEnabled(false);
		pauseToolbar.setEnabled(true);
		stopToolbar.setEnabled(true);
		break;
	    case TabComponent.PAUSED:
		runToolbar.setEnabled(true);
		pauseToolbar.setEnabled(false);
		stopToolbar.setEnabled(true);
		break;
	    case TabComponent.STOPPED:
		runToolbar.setEnabled(true);
		pauseToolbar.setEnabled(false);
		stopToolbar.setEnabled(false);
		break;
	}
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
	// TODO pytanie o nazwe pliku
	JFileChooser chooser = new JFileChooser();
	chooser.setMultiSelectionEnabled(false);
	chooser.showSaveDialog(this);

	String fileName = chooser.getSelectedFile().getAbsolutePath();
	System.out.println(fileName);
	
	Properties p = new Properties();
	p.setProperty("Workspace", workspace.getPath());
	String scripts="";
	for(int i=0; i < jTabbedPane1.getTabCount(); i++) {
	    TabComponent comp = (TabComponent) jTabbedPane1.getTabComponentAt(i);
	    if(comp.getState() == TabComponent.RUNNING) {
		if(i != 0) scripts += ", ";
		scripts += comp.getScriptName();
	    }
	}
	p.setProperty("AutoRun", scripts);

	FileOutputStream fos = null;
	try {
	    fos = new FileOutputStream(fileName);
	    p.store(fos, "Auto Run script for JUltima");
	} catch(IOException e) {
	    e.printStackTrace();
	} finally {
	    try { fos.close(); }
	    catch(IOException e) { e.printStackTrace(); }
	}

    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
	// TODO add your handling code here:
	JOptionPane.showMessageDialog(this,
	    "<html><font size=6>JUltima Online</font</html>\n" +
	    "Project is licensed under GPLv3, but it's allowed\n" +
	    "to use closed source Cheffe's library gmae.dll.\n\n" +
	    "Libraries used in project: \n" +
	    "* JSyntaxPane\n" +
	    "* Java Native Acces\n" +
	    "* L2FProd Property Sheet\n" +
	    "* Cheffe's game.dll\n\n" +
	    "I have also make a use of BtbN's Ultima Sharp source code."
	    ,
	    "About JUltima", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
	runFrame.setVisible(!runFrame.isVisible());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
	// TODO add your handling code here:
	JOptionPane.showMessageDialog(this, "This feature is not implemented yet!");
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem closeFileMenu;
    private javax.swing.JMenuItem compileMenu;
    private javax.swing.JTextArea compileOut;
    private javax.swing.JButton compileToolbar;
    private javax.swing.JMenu controlMenu;
    private javax.swing.JMenuItem deleteFileMenu;
    private javax.swing.JMenuItem exitMenu;
    private javax.swing.JList fileList;
    private javax.swing.JPopupMenu fileListPopup;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem newScriptMenu;
    private javax.swing.JButton newToolbar;
    private javax.swing.JMenuItem pauseMenu;
    private javax.swing.JButton pauseToolbar;
    private javax.swing.JMenuItem runMenu;
    private javax.swing.JButton runToolbar;
    private javax.swing.JMenuItem saveMenu;
    private javax.swing.JButton saveToolbar;
    private javax.swing.JMenuItem stopMenu;
    private javax.swing.JButton stopToolbar;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
