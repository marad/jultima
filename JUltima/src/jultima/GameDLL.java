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

import com.sun.jna.Native;

/**
 *
 * @author morti
 */
public class GameDLL {

    private static IGameDLL game;
    private int handle;

//    private static GameDLL gameDLL = new GameDLL();

//    /**
//     * Private constructor.
//     * To use this class use static get() method.
//     */
//    private GameDLL() {
//    }

    static {
	game = (IGameDLL) Native.loadLibrary("game", IGameDLL.class);
//	gameDLL.init();
//	gameDLL.open();
    }

    /**
     *
     * @return Zwraca wersje pliku game.dll
     */
    public synchronized  int version() {
        return game.Version();
    }

    /**
     * Otwiera polaczenie z klientem
     */
    public synchronized  void open() {
        handle = game.Open();
    }

    /**
     * Zamyka polaczenie z klientem
     */
    public synchronized  void close() {
        game.Close(handle);
    }

    /**
     * Odpytuje klienta
     * @return Status. 0 - jest ok.
     */
    public synchronized  int query() {
        return game.Query(handle);
    }

    public synchronized  int execute() {
        return game.Execute(handle);
    }

    public synchronized  int getTop() {
        return game.GetTop(handle);
    }

    public synchronized  int getType(int index) {
        return game.GetType(handle, index);
    }

    public synchronized  void insert(int index) {
        game.Insert(handle, index);
    }

    public synchronized  void pushNil() {
        game.PushNil(handle);
    }

    public synchronized  void pushBoolean(int value) {
        game.PushBoolean(handle, value);
    }

    public synchronized  void pushInteger(int value) {
        game.PushInteger(handle, value);
    }

    public synchronized  void pushDouble(double value) {
        game.PushDouble(handle, value);
    }

    public synchronized  void pushStrRef(String value) {
        game.PushStrRef(handle, value);
    }

    public synchronized  void pushStrVal(String value) {
        game.PushStrVal(handle, value);
    }

    public synchronized  void pushValue(int index) {
        game.PushValue(handle, index);
    }

    public synchronized  int getBoolean(int index) {
        return game.GetBoolean(handle, index);
    }

    public synchronized  int getInteger(int index) {
        return game.GetInteger(handle, index);
    }

    public synchronized  double getDouble(int index) {
        return game.GetDouble(handle, index);
    }

    public synchronized  String getString(int index) {
        return game.GetString(handle, index);
    }

    public synchronized  void remove(int index) {
        game.Remove(handle, index);
    }

    public synchronized  void setTop(int index) {
        game.SetTop(handle, index);
    }

    public synchronized  void mark() {
        game.Mark(handle);
    }

    public synchronized  void clean() {
        game.Clean(handle);
    }
}
