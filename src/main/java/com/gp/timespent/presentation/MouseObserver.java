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

package com.gp.timespent.presentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

class MouseObserver {
	/* the resolution of the mouse motion */
	private static final int DELAY = 100;

	private Component component;
	private Timer timer;
	private Set<MouseMotionListener> mouseMotionListeners;
	private Point lastPoint = null;
	private PointerInfo pointerInfo = null;

	protected MouseObserver(Component component){
		if (component == null){
			throw new IllegalArgumentException("Null component not allowed.");
		}

		this.component = component;

		/* poll mouse coordinates at the given rate */
		timer = new Timer(DELAY, new ActionListener(){
				
				/* called every DELAY milliseconds to fetch the
				 * current mouse coordinates */
				public synchronized void actionPerformed(ActionEvent e){
					pointerInfo = MouseInfo.getPointerInfo();
					if(pointerInfo != null){
						Point point = pointerInfo.getLocation();

						if (!point.equals(lastPoint)){
							fireMouseMotionEvent(point);
						}

						lastPoint = point;
					}
				}
			});
		mouseMotionListeners = new HashSet<MouseMotionListener>();
	}

	public Component getComponent(){
		return component;
	}

	public void start(){
		timer.start();
	}

	public void stop(){
		timer.stop();
	}

	public void addMouseMotionListener(MouseMotionListener listener){
		synchronized (mouseMotionListeners){
			mouseMotionListeners.add(listener);
		}
	}

	public void removeMouseMotionListener(MouseMotionListener listener){
		synchronized (mouseMotionListeners){
			mouseMotionListeners.remove(listener);
		}
	}

	protected void fireMouseMotionEvent(Point point){
		synchronized (mouseMotionListeners){
			for(final MouseMotionListener listener : mouseMotionListeners){
				final MouseEvent event = new MouseEvent(component, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, (int)point.x, (int)point.y, 0, false);

				SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							listener.mouseMoved(event);
						}
				});
			}
		}
	}
}
