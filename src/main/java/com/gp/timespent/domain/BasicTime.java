/*
    Time Spent
    Copyright (C) 2010-2013, Gary Paduana, gary.paduana@gmail.com
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.gp.timespent.domain;

import com.gp.timespent.domain.Timeable;

public class BasicTime implements Timeable{
	private int time = 0;
	private boolean enabled = true;
	private String name;
	
	public BasicTime(String name){
		this(0, name);
	}
	
	public BasicTime(int time, String name){
		this.time = time;
		this.name = name;
	}
	
	public BasicTime(){
		this(0, "Unnamed");
	}
	
	public int getTime(){
		return time;
	}
	
	public void setTime(int time){
		this.time = time;
	}
	
	public void reset(){
		time = 0;
	}
	
	public int increment(){
		if(enabled){
			time++;
		}
		return time;
	}
	
	public void pause(){
		enabled = false;
	}
	
	public void resume(){
		enabled = true;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setEnabled(boolean isEnabled){
		this.enabled = isEnabled;
	}
}
