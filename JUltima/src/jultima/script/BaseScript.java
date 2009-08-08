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

package jultima.script;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jultima.GameDLL;

/**
 *
 * @author morti
 */
public abstract class BaseScript implements ScriptInterface {

    private GameDLL gameDLL;

    public BaseScript() {
	gameDLL = new GameDLL();
	gameDLL.open();
	setVar("CliNr", 1);
    }

    @Override
    public void finalize() {
	gameDLL.close();
    }

    public BaseScript(GameDLL gameDLL) {
	this.gameDLL = gameDLL;
    }

    public void setGameDLL(GameDLL gameDLL) {
	this.gameDLL = gameDLL;
	setVar("CliNr", 1);
    }

    /* *** Returned Data ****/
    public class Property {
	public String name;
	public String info;
    }

    public class Item {
	public int id;
	public int type;
	public int kind;
	public int contId;
	public int x;
	public int y;
	public int z;
	public int stack;
	public int rep;
	public int color;
    }

    public class Skill {
	public int norm;
	public int real;
	public int cap;
	public int nlock;
    }

    public class JournalInfo {
	public int newRef;
	public int count;
    }

    public class JournalEntry {
	public String line;
	public int color;
    }
    
    /**
     * Executes function directly from the game.dll
     * @param ignoreResult If true - function will not fetch
     * results from the game DLL (its provided for speed)
     * @param name Function name.
     * @param args Arguments to the function.
     * @return Returned objects depends on what you
     * have actually done :) On error - returns null.
     */
    public Object[] executeFunction(boolean ignoreResult, String name, Object[] args)
    {
        gameDLL.setTop(0); // erase stack
        gameDLL.pushStrVal(name); // push func name on the stack

	// push arguments 
        for(Object o : args) {
            /**/ if( o == null ) gameDLL.pushNil();
            else if( o instanceof String ) gameDLL.pushStrVal((String)o);
            else if( o.getClass().equals(Integer.class) )
	        gameDLL.pushInteger( ((Integer)o).intValue() );
            else if( o.getClass().equals(Double.class) )
                gameDLL.pushDouble( ((Integer)o).doubleValue() );
            else if( o.getClass().equals(Boolean.class) ) {
		boolean b = (Boolean)o;
                gameDLL.pushBoolean(b?1:0);
	    }
            else
                gameDLL.pushStrVal(o.toString());
        }

	// execute function
        if(gameDLL.execute() != 0) return null;


        if(ignoreResult)
            return new Object[0];

	// fetch results from the stack
        int cnt = gameDLL.getTop();
        Object[] ret = new Object[cnt];

        for(int i=1; i <= cnt; i++) {
            switch(gameDLL.getType(i))
            {
                case 1: // boolean
                    ret[i-1] = (gameDLL.getBoolean(i) != 0)?true:false;
                    break;
                case 3: // int
                    ret[i-1] = gameDLL.getInteger(i);
                    break;
		case 4: // string
		    ret[i-1] = gameDLL.getString(i);
		    break;
		default:
		    ret[i-1] = null;
		    break;

            }
        }
        return ret;
    }

    /* *** EVENTS ****/

    public void cliDrag(int id) {
	executeFunction(true, "CliDrag", new Object[] { id } );
    }

    public void drag(int id, int amount) {
	executeFunction(true, "Drag", new Object[] { id, amount } );
    }

    public void dropC(int contId) {
	dropC(contId, -1, -1);
    }

    public void dropC(int contId, int x, int y) {
	executeFunction(true, "DropC", new Object[] { contId, x, y } );
    }

    public void dropG(int x, int y, int z) {
	executeFunction(true, "DropG", new Object[] { x, y, z } );
    }

    public void dropPD() {
	executeFunction(true, "DropPD", null);
    }

    public void exMsg(int id, int font, int color, String msg) {
	executeFunction(true, "ExMsg", new Object[] { id, font, color, msg } );
    }

    public void macro(int par1, int par2, String str) {
	executeFunction(true, "Macro", new Object[] {par1, par2, str} );
    }

    public void macro(int par1, String str) {
	macro(par1, 0, str);
    }

    public void macro(int par1, int par2) {
	macro(par1, par2, "");
    }

    public void macro(int par1) {
	macro(par1, 0, "");
    }

    public void pathFind(int x, int y, int z) {
	executeFunction(true, "Pathfind", new Object[] {x, y, z} );
    }
    
    public Property getProperty(int id) {
	Object[] ret = executeFunction(false, "Property", new Object[] {id});
	Property res = new Property();
	res.name = (String)ret[0];
	res.info = (String)ret[1];
	return res;
    }

    public void renamePet(int id, String name) {
	executeFunction(true, "RenamePet", new Object[] {id, name} );
    }

    public void skillLock(String skill, int nlock) {
	executeFunction(true, "SkillLock", new Object[] {nlock} );
    }

    public void statLock(String stat, int nlock) {
	executeFunction(true, "StatLock", new Object[] {nlock} );
    }

    public void sysMessage(String msg) {
	sysMessage(msg, 0);
    }
    
    public void sysMessage(String msg, int color) {
	executeFunction(true, "SysMessage", new Object[] { msg, color });
    }

    /* *** COMMANDS ****/

    public void click(int x, int y, boolean left,
	    boolean down, boolean up, boolean mc) {
	executeFunction(true, "Click",
		new Object[] {x, y, left, down, up, mc} );
    }

    public Item getItem(int index) {
	Object[] o = executeFunction(false, "GetItem", new Object[] {index} );

	Item item = new Item();
	item.id = ((Integer)o[0]).intValue();
	item.type = ((Integer)o[1]).intValue();
	item.kind = ((Integer)o[2]).intValue();
	item.contId = ((Integer)o[3]).intValue();
	item.x = ((Integer)o[4]).intValue();
	item.y = ((Integer)o[5]).intValue();
	item.z = ((Integer)o[6]).intValue();
	item.stack = ((Integer)o[7]).intValue();
	item.rep = ((Integer)o[8]).intValue();
	item.color = ((Integer)o[9]).intValue();

	return item;
    }

    public JournalEntry getJournal(int index) {
	Object[] o = executeFunction(false, "GetJournal", new Object[] {index});
	JournalEntry entry = new JournalEntry();
	entry.line = (String)o[0];
	entry.color = ((Integer)o[1]).intValue();
	return entry;
    }

    public int getPix(int x, int y) {
	Object[] ret = executeFunction(false, "GetPix",
		new Object[] {x, y} );
	return ((Integer)ret[0]).intValue();
    }
   
    public Skill getSkill(String skill) {
	Object[] o = executeFunction(false, "GetSkill", new Object[] {skill});

	Skill s = new Skill();
	s.norm = ((Integer)o[0]).intValue();
	s.real = ((Integer)o[1]).intValue();
	s.cap = ((Integer)o[2]).intValue();
	s.nlock = ((Integer)o[3]).intValue();
	return s;
    }

    public void hideItem(int id) {
	executeFunction(true, "HideItem", new Object[] { id } );
    }

    public void key(String key, boolean ctrl, boolean alt, boolean shift) {
	executeFunction(true, "Key", new Object[] {key, ctrl, alt, shift });
    }

    public void key(String k) {
	key(k, false, false, false);
    }

    public boolean move(int x, int y, int acc, int timeout) {
	Object[] o = executeFunction(false, "Move",
		new Object[] {x, y, acc, timeout} );
	return ((Boolean)o[1]).booleanValue();
    }

    public void msg(String str) {
	executeFunction(true, "Msg", new Object[] {str} );
    }

    public int scanItems(boolean visibleOnly) {
	Object[] o = executeFunction(false, "ScanItems",
		new Object[] { visibleOnly });
	return ((Integer)o[0]).intValue();
    }

    public JournalInfo scanJournal(int oldRef) {
	Object[] o = executeFunction(false, "ScanJournal", new Object[] {oldRef});
	JournalInfo jinfo = new JournalInfo();
	jinfo.newRef = ((Integer)o[0]).intValue();
	jinfo.count = ((Integer)o[1]).intValue();
	return jinfo;
    }
    
    public boolean tileInit(boolean noOverrides) {
	Object[] ret = executeFunction(false, "TileInit", new Object[] {noOverrides});
	return ((Boolean)ret[0]).booleanValue();
    }

    public int tileCnt(int x, int y, int facet) {
	Object[] ret = executeFunction(false, "TileCnt", new Object[] {x, y, facet});
	return ((Integer)ret[0]).intValue();
    }

    // TODO dodac tileGet

    public void contTop(int index) {
	executeFunction(true, "ContTop", new Object[] {index});
    }

    // TODO: getCont

    /* *** ADDITIONAL ****/
    public Object getVar(String name) {
	Object[] o = executeFunction(false, "GetVar", new Object[] {name});
	return o[0];
    }

    public String getVarStr(String name) {
	return (String) getVar(name);
    }

    public int getVarInt(String name) {
	return ((Integer)getVar(name)).intValue();
    }

    public boolean getVarBoolean(String name) {
	return ((Boolean)getVar(name)).booleanValue();
    }

    public void setVar(String name, Object o) {
	executeFunction(true, "SetVar", new Object[] { name, o });
    }

    /**
     * Waits for target cursor
     */
    public void target() {	
	while(!(Boolean)getVar("TargCurs")) {}
    }

    /**
     * Halts script execution
     */
    public void halt() {
	Thread.currentThread().stop();
    }

    public void sleep(int milis) {
	try {
	    Thread.sleep(milis);
	} catch (InterruptedException ex) {
	    Logger.getLogger(BaseScript.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    /* **** VARIABLE ACCESS **** */
    public int getAR() { return getVarInt("AR"); }
    public int getBackpackID() { return getVarInt("BackpackID"); }
    public int getCR() { return getVarInt("CR"); }
    public int getCharDir() { return getVarInt("CharDir"); }
    public int getCharID() { return getVarInt("CharID"); }
    public String getCharName() { return getVarStr("CharName"); }
    public int getCliCnt() { return getVarInt("CliCnt"); }
    public int getCharPosX() { return getVarInt("CharPosX"); }
    public int getCharPosY() { return getVarInt("CharPosY"); }
    public int getCharPosZ() { return getVarInt("CharPosZ"); }
    public String getCharStatus() { return getVarStr("CharStatus"); }
    public int getCharType() { return getVarInt("CharType"); }
    public String getCliLang() { return getVarStr("CliLang"); }
    public int getCliLeft() { return getVarInt("CliLeft"); }
    public boolean getCliLogged() { return getVarBoolean("CliLogged"); }
    public int getCliTop() { return getVarInt("CliTop"); }
    public String getCliVer() { return getVarStr("CliVer"); }
    public int getCliXRes() { return getVarInt("CliXRes"); }
    public int getCliYRes() { return getVarInt("CliYRes"); }
    public int getContID() { return getVarInt("ContID"); }
    public int getContKind() { return getVarInt("ContKind"); }
    public String getContName() { return getVarStr("ContName"); }
    public int getContPosX() { return getVarInt("ContPosX"); }
    public int getContPosY() { return getVarInt("ContPosX"); }
    public int getContSizeX() { return getVarInt("ContSizeX"); }
    public int getContSizeY() { return getVarInt("ContSizeY"); }
    public int getContType() { return getVarInt("ContType"); }
    public int getCursKind() { return getVarInt("CursKind"); }
    public int getDex() { return getVarInt("Dex"); }
    public int getER() { return getVarInt("ER"); }
    public int getEnemyHits() { return getVarInt("EnemyHits"); }
    public int getEnemyID() { return getVarInt("EnemyID"); }
    public int getFR() { return getVarInt("FR"); }
    public int getFollowers() { return getVarInt("Followers"); }
    public int getGold() { return getVarInt("Gold"); }
    public int getHits() { return getVarInt("Hits"); }
    public int getInt() { return getVarInt("Int"); }
    public int getLHandID() { return getVarInt("LHandID"); }
    public int getLLiftedID() { return getVarInt("LLiftedID"); }
    public int getLLiftedKind() { return getVarInt("LLiftedKind"); }
    public int getLObjectID() { return getVarInt("LObjectID"); }
    public int getLObjectType() { return getVarInt("LObjectType"); }
    public int getLShard() { return getVarInt("LShard"); }
    public int getLSkill() { return getVarInt("LSkill"); }
    public int getLSpell() { return getVarInt("LSpell"); }
    public int getLTargetID() { return getVarInt("LTargetID"); }
    public int getLTargetKind() { return getVarInt("LTargetKind"); }
    public int getLTargetTile() { return getVarInt("LTargetTile"); }
    public int getLTargetX() { return getVarInt("LTargetX"); }
    public int getLTargetY() { return getVarInt("LTargetY"); }
    public int getLTargetZ() { return getVarInt("LTargetZ"); }
    public int getLuck() { return getVarInt("Luck"); }
    public int getMana() { return getVarInt("Mana"); }
    public int getMaxFol() { return getVarInt("MaxFol"); }
    public int getMaxHits() { return getVarInt("MaxHits"); }
    public int getMaxMana() { return getVarInt("MaxMana"); }
    public int getMaxStam() { return getVarInt("MaxStam"); }
    public int getMaxStats() { return getVarInt("MaxStats"); }
    public int getMaxWeight() { return getVarInt("MaxWeight"); }
    public int getMinDmg() { return getVarInt("MinDmg"); }
    public int getMaxDmg() { return getVarInt("MaxDmg"); }
    public int getNextCPosX() { return getVarInt("NextCPosX"); }
    public int getNextCPosY() { return getVarInt("NextCPosY"); }
    public int getPR() { return getVarInt("PR"); }
    public int getRHandID() { return getVarInt("RHandID"); }
    public int getSex() { return getVarInt("Sex"); }
    public String getShard() { return getVarStr("Shard"); }
    public int getStamina() { return getVarInt("Stamina"); }
    public int getStr() { return getVarInt("Str"); }
    public String getSysMsg() { return getVarStr("SysMsg"); }
    public int getTP() { return getVarInt("TP"); }
    public boolean getTargCurs() { return getVarBoolean("TargCurs"); }
    public int getWeight() { return getVarInt("Weight"); }

    public void setCliNr(int i) { setVar("CliNr", i); }
    public void setCliLeft(int i) { setVar("CliLeft", i); }
    public void setCliTop(int i) { setVar("CliTop", i); }
    public void setCliXRes(int i) { setVar("CliXRes", i); }
    public void setCliYRes(int i) { setVar("CliYRes", i); }
    public void setContPosX(int i) { setVar("ContPosX", i); }
    public void setContPosY(int i) { setVar("ContPosY", i); }
    public void setLHandID(int i) { setVar("LHandID", i); }
    public void setLObjectID(int i) { setVar("LObjectID", i); }
    public void setLShard(int i) { setVar("LShard", i); }
    public void setLSkill(int i) { setVar("LSkill", i); }
    public void setLSpell(int i) { setVar("LSpell", i); }
    public void setLTargetID(int i) { setVar("LTargetID", i); }
    public void setLTargetKind(int i) { setVar("LTargetKind", i); }
    public void setLTargetTile(int i) { setVar("LTargetTile", i); }
    public void setLTargetX(int i) { setVar("LTargetX", i); }
    public void setLTargetY(int i) { setVar("LTargetY", i); }
    public void setLTargetZ(int i) { setVar("LTargetZ", i); }
    public void setNextCPosX(int i) { setVar("NextCPosX", i); }
    public void setNextCPosY(int i) { setVar("NextCPosY", i); }
    public void setRHandID(int i) { setVar("RHandID", i); }
    public void setTargCurs(boolean b) { setVar("TargCurs", b); }
    
    
    /* **** MY EXTENSIONS **** */
    public Item findItemC(int idOrType, int contId) {
	return findItemC(new int[] {idOrType}, contId);
    }

    public Item findItemC(int[] idsOrTypes, int contId) {
	int count = scanItems(true);
	for(int i=0; i < count; i++) {
	    Item item = getItem(i);
	    for(int idOrType : idsOrTypes)
		if(item.contId == contId &&
		    (item.id == idOrType || item.type == idOrType))
		    return item;
	}
	return null;
    }

    public Item findItemG(int idOrType, int distance) {
	return findItemG(new int[] {idOrType}, distance);
    }

    public Item findItemG(int[] idsOrTypes, int distance) {
	distance = distance * distance;
	int count = scanItems(true);
	int x = getCharPosX();
	int y = getCharPosY();
	int xDiff, yDiff;
	Item item;
	for(int i=0; i < count; i++) {
	    item = getItem(i);
	    xDiff = x - item.x;
	    yDiff = y - item.y;
	    xDiff *= xDiff;
	    yDiff *= yDiff;
	    for(int idOrType : idsOrTypes)
		if(xDiff + yDiff < distance &&
		    (item.id == idOrType || item.type == idOrType))
		    return item;
	}
	return null;
    }

    public Item findItem(int idOrType) {
	return findItem(new int[] {idOrType});
    }

    public Item findItem(int[] idsOrTypes) {
	int count = scanItems(true);
	Item item;
	for(int i=0; i < count; i++) {
	    item = getItem(i);
	    for(int idOrType : idsOrTypes)
		if( item.id == idOrType ||
		    item.type == idOrType)
		    return item;
	}
	return null;
    }

    public Item[] findItems(int idOrType) {
	return findItems(new int[] {idOrType});
    }

    public Item[] findItems(int[] idsOrTypes) {
	HashSet<Item> set = new HashSet<Item>();
	int count = scanItems(true);
	Item item;
	for(int i=0; i < count; i++) {
	    item = getItem(i);
	    for(int idOrType : idsOrTypes)
		if( item.id == idOrType ||
		    item.type == idOrType)
		    set.add(item);
	}
	return set.toArray(new Item [0]);
    }

    public Item[] findItemsC(int idOrType, int contId) {
	return findItemsC(new int[] {idOrType}, contId);
    }
    
    public Item[] findItemsC(int[] idsOrTypes, int contId) {
	HashSet<Item> set = new HashSet<Item>();
	int count = scanItems(true);
	Item item;
	for(int i=0; i < count; i++) {
	    item = getItem(i);
	    for(int idOrType : idsOrTypes)
		if( item.contId == contId &&
		    (item.id == idOrType || item.type == idOrType))
		    set.add(item);
	}
	return set.toArray(new Item [0]);
    }

    public Item[] findItemsG(int idOrType, int distance) {
	return findItemsG(new int[] {idOrType}, distance);
    }
    
    public Item[] findItemsG(int[] idsOrTypes, int distance) {
	HashSet<Item> set = new HashSet<Item>();
	int count = scanItems(true);
	int x = getCharPosX();
	int y = getCharPosY();
	int xDiff, yDiff;
	Item item;
	for(int i=0; i < count; i++) {
	    item = getItem(i);
	    xDiff = x - item.x;
	    yDiff = y - item.y;
	    xDiff *= xDiff;
	    yDiff *= yDiff;
	    for(int idOrType : idsOrTypes)
		if( xDiff + yDiff < distance &&
		    (item.id == idOrType || item.type == idOrType))
		    set.add(item);
	}
	return set.toArray(new Item [0]);
    }
}