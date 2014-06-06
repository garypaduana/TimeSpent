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

import com.gp.timespent.persistence.*;

public class ChargeCode implements Persistable{
	private Timeable activeTime;
	private int idleThreshold = 300;
	private String name;
	
	public ChargeCode(String name, Timeable activeTime){
		this.name = name;
		this.activeTime = activeTime;
	}
	
	public ChargeCode(){
		this.name = "Undefined";
		this.activeTime = new BasicTime(); 
	}
	
	public Timeable getTime(){
		return activeTime;
	}
	
	public void setTime(Timeable activeTime){
		this.activeTime = activeTime;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getIdleThreshold(){
		return idleThreshold;
	}
	
	public void setIdleThreshold(int idleThreshold){
		this.idleThreshold = idleThreshold;
	}
}
