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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.gp.timespent.domain.BasicTime;
import com.gp.timespent.domain.ChargeCode;
import com.gp.timespent.domain.Timeable;
import com.gp.timespent.exception.ExceptionHandler;
import com.gp.timespent.persistence.ObjectPersister;
import com.gp.timespent.util.FileUtil;

public class TimeSpentGui {
	private static final int ONE_SECOND = 1000;
	private static final String DEFAULT	= "Default";
	private static final String SINCE_LAST_MOVEMENT_TIME = "sinceLastMovement";
	private static final String IDLE_TIME = "idleTime";
	
	private Timer movementTimer;
	private Timer elapsedTimer;
	
	private Map<String, Timeable> coreTimes = new HashMap<String, Timeable>();
	private Map<String, ChargeCode> chargeCodes = new HashMap<String, ChargeCode>();
	
	private String activeChargeCode = DEFAULT;
	private TimedClickListener clinClickListener;

	private JLabel sinceMovementDescLabel = new JLabel();
	private JLabel sinceMovementLabel = new JLabel();
	
	private JLabel idleTimeDescLabel = new JLabel();
	private JLabel idleTimeLabel = new JLabel();
	
	private JLabel sinceStartDescLabel = new JLabel();
	private JLabel sinceStartLabel = new JLabel();
	
	private JLabel idleThresholdDescLabel = new JLabel();
	private JTextArea idleThresholdTextArea = new JTextArea();
		
	private JComboBox clinComboBox = new JComboBox();
	private JLabel clinTimeLabel = new JLabel();
	
	private JFrame timespentFrame = new JFrame();
	private JPanel mainPanel = new JPanel();
	
	public static final String ZERO_TIME = "00:00:00";
	public static final String PROJECT_NAME = "TimeSpent";
	
	private static Insets defaultInsets = new Insets(3,3,3,3);
	
	private enum Title {TIME, NAME};
	private Title windowTitle = Title.NAME;
	
	private boolean showTips = false;
	private int persistInterval = 30;
	
	public TimeSpentGui(){
		ResourceBundle config = ResourceBundle.getBundle("config");
		
		if(config.containsKey("title")){
			if(config.getString("title").equals("time")){
				windowTitle = Title.TIME;
			}
		}
		
		if(config.containsKey("showtips")){
			if(Boolean.valueOf(config.getString("showtips"))){
				showTips = true;
			}
		}
		
		if(config.containsKey("persist_interval")){
			try{
				persistInterval = Integer.valueOf(config.getString("persist_interval"));
			}catch(NumberFormatException e){
				Throwable n = new Throwable("Invalid integer in configuration file for key: persist_interval", e);
				ExceptionHandler.handleException(n, true);
			}
			if(persistInterval < 10){
				persistInterval = 10;
			}
		}
	}
	
	public void launchGui(){
		setDefaultChargeCode();
		setDefaultCoreTimes();
		
		try {
			ObjectPersister.validateSaveStructure();
		} catch (IOException e) {
			ExceptionHandler.handleException(e, true);
		}
		
		buildGui();
    	restorePreviousSession();
    	registerListeners();
    	show();
    	if(showTips){
    		try {
				showTips();
			} catch (IOException e) {
				ExceptionHandler.handleException(e, true);
			}
    	}
	}
	
	private void showTips() throws IOException {
		List<String> tips = FileUtil.getTextFromFile("./resources/usagetips.txt");
		if(tips.size() == 0){
			return;
		}
		
		Collections.shuffle(tips);
		JOptionPane.showMessageDialog(null, tips.get(0), "Usage Tips", JOptionPane.INFORMATION_MESSAGE, null);
	}
	
	private void setDefaultCoreTimes(){
		coreTimes.put(SINCE_LAST_MOVEMENT_TIME, new BasicTime(0, SINCE_LAST_MOVEMENT_TIME));
		coreTimes.put(IDLE_TIME, new BasicTime(0, IDLE_TIME));
	}
	
	private void setDefaultChargeCode(){
		chargeCodes.put(DEFAULT, new ChargeCode(DEFAULT, new BasicTime(0, "defaultTime")));
	}
			
	@SuppressWarnings({ "unchecked" })
	private void restorePreviousSession(){
		chargeCodes = (HashMap<String, ChargeCode>) ObjectPersister.restore(ObjectPersister.derivePath("chargeCodes"));
		
		if(chargeCodes == null){
			chargeCodes = new HashMap<String, ChargeCode>();
			setDefaultChargeCode();
		}
		
		List<String> names = new ArrayList<String>();
		for(Object name : chargeCodes.keySet().toArray()){
			names.add((String) name);
		}
		clinComboBox.setModel(new DefaultComboBoxModel(names.toArray()));
		
		coreTimes = (HashMap<String, Timeable>) ObjectPersister.restore(ObjectPersister.derivePath("coreTimes"));
		
		if(coreTimes == null){
			coreTimes = new HashMap<String, Timeable>();
			setDefaultCoreTimes();
		}
	}
	
	private void buildGui(){
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(windowTitle.equals(Title.NAME)){
					timespentFrame.setTitle(PROJECT_NAME);
				}
				else{
					timespentFrame.setTitle("");
				}
				
				timespentFrame.setSize(new Dimension(900,500));
				timespentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				timespentFrame.setLocationRelativeTo(null);
				timespentFrame.setLayout(new BorderLayout());
				ImageIcon img = new ImageIcon("./resources/images/icon.png");
				timespentFrame.setIconImage(img.getImage());
				
				timespentFrame.add(mainPanel);
				
				mainPanel.setLayout(new GridBagLayout());
				
				idleThresholdDescLabel.setText("Idle Threshold:");
				mainPanel.add(idleThresholdDescLabel, getDefaultGridBagConstraints(0, 0));
				
				idleThresholdTextArea.setText("300");
				idleThresholdTextArea.setBorder(BorderFactory.createLineBorder(Color.black));
				idleThresholdTextArea.setMinimumSize(new Dimension(50,20));
				idleThresholdTextArea.setPreferredSize(new Dimension(50,20));
				mainPanel.add(idleThresholdTextArea, getDefaultGridBagConstraints(1, 0));
				
				sinceMovementDescLabel.setText("Since Last Movement:");
				mainPanel.add(sinceMovementDescLabel, getDefaultGridBagConstraints(0, 1));
				
				sinceMovementLabel.setText(ZERO_TIME);
				sinceMovementLabel.setBorder(BorderFactory.createLineBorder(Color.orange));
				mainPanel.add(sinceMovementLabel, getDefaultGridBagConstraints(1, 1));
				
				idleTimeDescLabel.setText("Total Idle Time:");
				mainPanel.add(idleTimeDescLabel, getDefaultGridBagConstraints(0, 2));
				
				idleTimeLabel.setText(ZERO_TIME);
				idleTimeLabel.setBorder(BorderFactory.createLineBorder(Color.red));
				mainPanel.add(idleTimeLabel, getDefaultGridBagConstraints(1, 2));
				
				sinceStartDescLabel.setText("Total Active Time:");
				mainPanel.add(sinceStartDescLabel, getDefaultGridBagConstraints(0, 3));
				
				sinceStartLabel.setText(ZERO_TIME);
				sinceStartLabel.setBorder(BorderFactory.createLineBorder(Color.green));
				mainPanel.add(sinceStartLabel, getDefaultGridBagConstraints(1, 3));
								
				clinComboBox.setMinimumSize(new Dimension(120,20));
				clinComboBox.setPreferredSize(new Dimension(120,20));
				mainPanel.add(clinComboBox, getDefaultGridBagConstraints(0, 4));
				
				clinTimeLabel.setText(ZERO_TIME);
				clinTimeLabel.setBorder(BorderFactory.createLineBorder(Color.green));
				mainPanel.add(clinTimeLabel, getDefaultGridBagConstraints(1, 4));
								
				timespentFrame.add(mainPanel, BorderLayout.CENTER);
				
				clinComboBox.setModel(new DefaultComboBoxModel(chargeCodes.keySet().toArray()));
				timespentFrame.setResizable(false);
		    }
		});
		
	}
	
	private GridBagConstraints getDefaultGridBagConstraints(int x, int y){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.insets = defaultInsets;
		return c;
	}
	
	private void changeClinClickListener(){
		clinClickListener = new TimedClickListener(chargeCodes.get(activeChargeCode).getTime());
		clinTimeLabel.addMouseListener(clinClickListener);
	}
	
	private void chargeCodeChanged(){
		clinTimeLabel.removeMouseListener(clinClickListener);
		idleThresholdTextArea.setText(Integer.toString(chargeCodes.get(activeChargeCode).getIdleThreshold()));
		changeClinClickListener();
	}
	
	private void registerListeners(){
		sinceStartLabel.addMouseListener(new SummaryClickListener());
		idleTimeLabel.addMouseListener(new TimedClickListener(coreTimes.get(IDLE_TIME)));
		sinceMovementLabel.addMouseListener(new TimedClickListener(coreTimes.get(SINCE_LAST_MOVEMENT_TIME)));
		clinClickListener = new TimedClickListener(chargeCodes.get(activeChargeCode).getTime());
		clinTimeLabel.addMouseListener(clinClickListener);
		clinComboBox.addMouseListener(new ChargeCodeClickListener());
		
		clinComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				activeChargeCode = ((JComboBox) e.getSource()).getSelectedItem().toString();
				chargeCodeChanged();				
			}
		});
		
		MouseObserver mo = new MouseObserver(timespentFrame);
		mo.addMouseMotionListener(new MouseMotionListener(){
				public void mouseMoved(MouseEvent e){
					coreTimes.get(SINCE_LAST_MOVEMENT_TIME).setTime(0);
					
					if(!movementTimer.isRunning()){
						movementTimer.start();
					}
				}

				public void mouseDragged(MouseEvent e) {
					// This case is not important
				}
		});
		mo.start();
		
		ActionListener movementTaskPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				sinceMovementLabel.setText(generateTimerLabel(coreTimes.get(SINCE_LAST_MOVEMENT_TIME).getTime()));
				coreTimes.get(SINCE_LAST_MOVEMENT_TIME).increment();
			}
		};
		movementTimer = new Timer(ONE_SECOND, movementTaskPerformer);
		
		ActionListener elapsedTaskPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				sinceStartLabel.setText(generateSummaryTimerLabel());
				idleTimeLabel.setText(generateTimerLabel(coreTimes.get(IDLE_TIME).getTime()));
				clinTimeLabel.setText(generateTimerLabel(chargeCodes.get(activeChargeCode).getTime().getTime()));
				
				if(windowTitle.equals(Title.TIME)){
					timespentFrame.setTitle(generateSummaryTimerLabel());
				}
				
				try{
					int newThreshold = Integer.valueOf(idleThresholdTextArea.getText());
					chargeCodes.get(activeChargeCode).setIdleThreshold(newThreshold);
					idleThresholdTextArea.setToolTipText("");
				}
				catch(Exception ex){
					idleThresholdTextArea.setToolTipText("Using: " + Integer.toString(chargeCodes.get(activeChargeCode).getIdleThreshold()));
				}
				
				if(coreTimes.get(SINCE_LAST_MOVEMENT_TIME).getTime() <= chargeCodes.get(activeChargeCode).getIdleThreshold()){
					chargeCodes.get(activeChargeCode).getTime().increment();
					
					if(chargeCodes.get(activeChargeCode).getTime().getTime() % 10 == 0){
						persistAll();
					}
				}
				else{
					coreTimes.get(IDLE_TIME).increment();
					if(coreTimes.get(IDLE_TIME).getTime() % 10 == 0){
						persistAll();
					}
				}
			}
		};
		elapsedTimer = new Timer(ONE_SECOND, elapsedTaskPerformer);		
	}
	
	private void persistAll(){
		ObjectPersister.persistState(ObjectPersister.derivePath("coreTimes"), coreTimes);
		ObjectPersister.persistState(ObjectPersister.derivePath("chargeCodes"), chargeCodes);
	}
	
	private String generateSummaryTimerLabel(){
		int secondsElapsed = 0;
		
		for(String s : chargeCodes.keySet()){
			secondsElapsed += chargeCodes.get(s).getTime().getTime();
		}
		
		return generateTimerLabel(secondsElapsed);
	}
		
	private String generateTimerLabel(int secondsElapsed){
		int hours = secondsElapsed / 3600;
		int minutes = (secondsElapsed % 3600) / 60;
		int seconds = (secondsElapsed % 60) / 1;
		
		boolean negative = false;
		if(hours < 0 || minutes < 0 || seconds < 0){
			negative = true;
		}
		
		String value = padLeft(Integer.toString(hours).replace("-", ""), 2, '0') +
				 ":" + padLeft(Integer.toString(minutes).replace("-", ""), 2, '0') +
				 ":" + padLeft(Integer.toString(seconds).replace("-", ""), 2, '0');
		
		if(negative){
			value = "-" + value;
		}
		
		return value;
	}
	
	private String padLeft(String s, int length, char c){
		StringBuilder sb = new StringBuilder(s);
		while(sb.length() < length){
			sb.insert(0, c);
		}
		return sb.toString();
	}
	
	public void show(){
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	timespentFrame.pack();
				timespentFrame.setVisible(true);
				movementTimer.start();
				elapsedTimer.start();
		    }
		});
	}
	
	private void pauseAll(){
		for(String s : chargeCodes.keySet()){
			chargeCodes.get(s).getTime().pause();
		}
	}
	
	private void resetAll(){
		for(String s : chargeCodes.keySet()){
			chargeCodes.get(s).getTime().reset();
		}
	}
	
	private void resumeAll(){
		for(String s : chargeCodes.keySet()){
			chargeCodes.get(s).getTime().resume();
		}
	}
	
	public class TimedClickListener extends MouseAdapter{
		private Timeable basicTime;
		
		public TimedClickListener(Timeable basicTime){
			this.basicTime = basicTime;
		}
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent){
			
			final MouseEvent e = mouseEvent;
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					if(e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3){
				
						JPopupMenu popup = new JPopupMenu();

						JMenuItem pauseMenuItem = new JMenuItem("Pause");
						JMenuItem resumeMenuItem = new JMenuItem("Resume");
						JMenuItem resetMenuItem = new JMenuItem("Reset");
						JMenuItem adjustTimeMenuItem = new JMenuItem("Adjust");
						
						pauseMenuItem.setEnabled(basicTime.isEnabled());
						resumeMenuItem.setEnabled(!basicTime.isEnabled());
						
						pauseMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								basicTime.pause();
								persistAll();
							}
						});
						
						resumeMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								basicTime.resume();
								persistAll();
							}
						});
						
						resetMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								int response = JOptionPane.showConfirmDialog(null, "Are you sure?");
								if(response == JOptionPane.YES_OPTION){
									basicTime.reset();
									persistAll();
								}
							}
						});
						
						adjustTimeMenuItem.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent arg0) {
								String adjustment = JOptionPane.showInputDialog("Enter the adjustment (in seconds):");
								
								int adjust = 0;
								try{
									adjust = Integer.valueOf(adjustment);
									basicTime.setTime(basicTime.getTime() + adjust);
								}
								catch(Exception ex){
									JOptionPane.showMessageDialog(null, "Invalid adjustment value.");
								}
							}
						});
						
						popup.add(pauseMenuItem);
						popup.add(resumeMenuItem);
						popup.addSeparator();
						popup.add(resetMenuItem);
						popup.add(adjustTimeMenuItem);
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
	}
	
	public class SummaryClickListener extends MouseAdapter{
		
		public SummaryClickListener(){
			
		}
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent){
			
			final MouseEvent e = mouseEvent;
			
			SwingUtilities.invokeLater(new Runnable(){
				
				public void run(){
					if(e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3){
						
						JPopupMenu popup = new JPopupMenu();
						
						JMenuItem pauseMenuItem = new JMenuItem("Pause All Charge Codes");
						JMenuItem resumeMenuItem = new JMenuItem("Resume All Charge Codes");
						JMenuItem resetMenuItem = new JMenuItem("Reset All Charge Codes");
											
						pauseMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								pauseAll();
								persistAll();
							}
						});
						
						resumeMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								resumeAll();
								persistAll();
							}
						});
						
						resetMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								int response = JOptionPane.showConfirmDialog(null, "Are you sure?");
								if(response == JOptionPane.YES_OPTION){
									resetAll();						
									persistAll();
								}
							}
						});
						
						popup.add(pauseMenuItem);
						popup.add(resumeMenuItem);
						popup.addSeparator();
						popup.add(resetMenuItem);
						popup.show(e.getComponent(), e.getX(), e.getY());
						
					}
				}
			});
		}
	}
	
	public class ChargeCodeClickListener extends MouseAdapter{
		
		public ChargeCodeClickListener(){
			
		}
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent){
			
			final MouseEvent e = mouseEvent;
			
			SwingUtilities.invokeLater(new Runnable(){
			
				public void run(){
					if(e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3){
					
						JPopupMenu popup = new JPopupMenu();
						
						JMenuItem deleteMenuItem = new JMenuItem("Delete");
						JMenuItem addNewMenuItem = new JMenuItem("Add New...");
						
						deleteMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								removeCurrentChargeCode();
								persistAll();
							}
						});
						
						addNewMenuItem.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent actionEvent){
								addNew();
								persistAll();
							}
						});
						
						popup.add(addNewMenuItem);					
						popup.add(deleteMenuItem);
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
	}
	
	private void addNew(){
		String name = JOptionPane.showInputDialog("Enter the project name:");
		
		if(name.length() <= 0){
			JOptionPane.showMessageDialog(null, "Invalid name! Please try again.");
			return;
		}
		
		if(chargeCodes.containsKey(name)){
			int selection = JOptionPane.showConfirmDialog(null, name + " already exists! Overwrite and reset time for this project?");
			if(selection == JOptionPane.NO_OPTION || selection == JOptionPane.CANCEL_OPTION){
				return;
			}
		}
		
		chargeCodes.put(name, new ChargeCode(name, new BasicTime(name)));
		activeChargeCode = name;
		clinComboBox.setModel(new DefaultComboBoxModel(chargeCodes.keySet().toArray()));
		clinComboBox.setSelectedIndex(findElementLocation(name));
	}
	
	private int findElementLocation(String name){
		
		for(int i = 0; i < clinComboBox.getModel().getSize(); i++){
			if(clinComboBox.getModel().getElementAt(i).equals(name)){
				return i;
			}
		}
		return 0;
	}
	
	private void removeCurrentChargeCode(){
		String name = clinComboBox.getSelectedItem().toString();
		
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + name + "?");
		
		if(response == JOptionPane.YES_OPTION){
		
			if(!name.equals(DEFAULT)){
				chargeCodes.remove(name);
				clinComboBox.setModel(new DefaultComboBoxModel(chargeCodes.keySet().toArray()));
				activeChargeCode = (String)chargeCodes.keySet().toArray()[0];
				chargeCodeChanged();
			}
			else{
				JOptionPane.showMessageDialog(null, "Cannot remove default charge code!");
			}
		}
	}
}
