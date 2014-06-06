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

package com.gp.timespent.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.gp.timespent.exception.ExceptionHandler;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ObjectPersister{
		
	public static final String SAVE_STRUCTURE = System.getProperty("user.dir") + "/resources/save/";
	public static final String XML_EXTENSION = ".xml";
	
	public static void persistState(Persistable p) throws IOException{
		persistState(derivePath(p), p);
	}
	
	public static Object restore(Persistable p) throws IOException{
		return restore(derivePath(p));
	}
	
	public static void persistState(String filePath, Object object){
		File file = new File(filePath);
		XStream xstream = new XStream();
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(file));
			xstream.toXML(object, bw);
		}
		catch(Exception ex){
			ExceptionHandler.handleException(ex, true);
		}
		finally{
			try {
				if(bw != null){
					bw.close();
				}
			} catch (IOException e) {
				ExceptionHandler.handleException(e, true);
			}
		}		
	}
	
	public static Object restore(String filePath){
		File file = new File(filePath);
		XStream xstream = null;
		BufferedReader br = null;
		
		try{
			xstream = new XStream(new DomDriver());
			br = new BufferedReader(new FileReader(file));
			return xstream.fromXML(br);
		}
		catch(Exception ex){
			ExceptionHandler.handleException(ex, true);
			return null;
		}
		finally{
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				ExceptionHandler.handleException(e, true);
			}
		}
	}
	
	private static String derivePath(Persistable p){
		return derivePath(p.getName());
	}
	
	public static String derivePath(String name){
		return SAVE_STRUCTURE + name + XML_EXTENSION;
	}
	
	public static void validateSaveStructure() throws IOException{
		File file = new File(SAVE_STRUCTURE);
		if(!file.exists()){
			file.mkdirs();
		}
	}
}
