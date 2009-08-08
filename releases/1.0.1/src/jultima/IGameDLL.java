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

import com.sun.jna.Library;

/**
 *
 * @author morti
 */
public interface IGameDLL extends Library {
    
    public  int     Version();
    public  int     Open();
    public  void    Close(int hnd);
    public  int     Query(int hnd);
    public  int     Execute(int hnd);
    public  int     GetTop(int hnd);
    public  int     GetType(int hnd, int index);
    public  void    Insert(int hnd, int index);

    public  void    PushNil(int hnd);
    public  void    PushBoolean(int hnd, int value);
    public  void    PushInteger(int hnd, int value);
    public  void    PushDouble(int hnd, double value);
    public  void    PushStrRef(int hnd, String value);
    public  void    PushStrVal(int hnd, String value);
    public  void    PushValue(int hnd, int index);

    public  int    GetBoolean(int hnd, int index);
    public  int     GetInteger(int hnd, int index);
    public  double  GetDouble(int hnd, int index);
    public  String  GetString(int hnd, int index);

    public  void    Remove(int hnd, int index);
    public  void    SetTop(int hnd, int index);
    public  void    Mark(int hnd);
    public  void    Clean(int hnd);
}
