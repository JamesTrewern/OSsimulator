package user_interface;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simulation.Simulation;

public class AppGui{

	private JFrame frame;
	private JPanel panel;
	JButton runButton;
	JTextField textField;
	JComboBox comboBox;
	private ButtonListener buttonListener;
	private Simulation sim;
	int coreCount;
	int clockSpeed;
	int cpuBurst;
	public AppGui(Simulation sim) {
		this.sim = sim;
		buttonListener = new ButtonListener(this,sim);
		setSchedulePanel();
		if (frame != null) {
			frame.setVisible(false);
		}	
		frame = new JFrame();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("OS simulator");
		frame.pack();
		frame.setVisible(true);
		this.coreCount = 1;
		this.clockSpeed = 20;
		this.cpuBurst = 20;
	}
	
	public void setSchedulePanel() {
		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel label = new JLabel("Input File: ");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.ipadx = 60;
		panel.add(label,gbc);
		
		textField = new JTextField();
		textField.setSize(100, 20);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		panel.add(textField,gbc);
		
		label = new JLabel("Core count: ");
		gbc.gridy = 1;
		gbc.gridx = 0;
		panel.add(label,gbc);
		
		comboBox = new JComboBox(new String[]{"1","2","4"});
		comboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox sender = (JComboBox) e.getSource();
				switch(sender.getSelectedIndex()) {
				case 0:
					coreCount = 1;
					break;
				case 1:
					coreCount = 2;
					break;
				case 2:
					coreCount = 4;
					break;
				}
			}});
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		panel.add(comboBox,gbc);
	
		label = new JLabel("Clock speed(ms): ");
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		panel.add(label,gbc);
		
		comboBox = new JComboBox(new String[]{"20","50", "100", "200", "500"});
		comboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox sender = (JComboBox) e.getSource();
				switch(sender.getSelectedIndex()) {
				case 0:
					clockSpeed = 20;
					break;
				case 1:
					clockSpeed = 50;
					break;
				case 2:
					clockSpeed = 100;
					break;
				case 3:
					clockSpeed = 200;
					break;
				case 4:
					clockSpeed = 500;
					break;
				}
			}});
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		panel.add(comboBox,gbc);
		
		label = new JLabel("CPU burst(quantum): ");
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		panel.add(label,gbc);
		
		JSlider slider = new JSlider(10,100,20);
		Hashtable labelTable = new Hashtable();
		labelTable.put( new Integer( 20 ), new JLabel("20"));
		labelTable.put( new Integer( 40 ), new JLabel("40"));
		labelTable.put( new Integer( 60 ), new JLabel("60"));
		labelTable.put( new Integer( 80 ), new JLabel("80"));
		labelTable.put( new Integer( 100 ), new JLabel("100"));
		slider.setLabelTable(labelTable);
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(10);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				cpuBurst = slider.getValue();
			}});
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		panel.add(slider,gbc);
		
		
		runButton = new JButton("Run");	
		runButton.addActionListener(buttonListener);
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridx = 2;
		panel.add(runButton,gbc);
	}

	
	public void setSimulationPanel() {
		frame.setVisible(false);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("OS simulator");
		
		SimulationPanel simPanel = new SimulationPanel(sim);
		this.panel = simPanel;
		frame.add(panel);
		frame.setSize(1020,500);
		frame.setVisible(true);
		Thread simPanelThread = new Thread(simPanel);
		simPanelThread.start();
		
	}
	

	
	private class ButtonListener implements ActionListener {

		private AppGui gui;
		private Simulation sim;
		
		public ButtonListener(AppGui gui,Simulation sim) {
			this.gui = gui;
			this.sim = sim;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton) e.getSource();
			if (source.equals(gui.runButton)) {
				writeToSettings();
				sim.startSetInterval(gui.textField.getText());
				gui.setSimulationPanel();			
			}			
		}
		
		private void writeToSettings() {
			try {
				FileWriter fw = new FileWriter("settings.txt",false);
				fw.write(Integer.toString(gui.clockSpeed));
				fw.write("\n");
				fw.write(Integer.toString(gui.coreCount));
				fw.write("\n");
				fw.write(Integer.toString(gui.cpuBurst));
				fw.write("\n");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		}

	}
	
}
