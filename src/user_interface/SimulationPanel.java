package user_interface;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import simulation.SimProcess;
import simulation.SimProcess.ProcessStatus;
import simulation.Simulation;
import simulation.SystemResource;

public class SimulationPanel extends JPanel implements Runnable {

	private Simulation sim;
	private int coreCount;
	private int resourceCount;
	private JButton pauseButton;
	private DefaultTableModel currentProcessModel;
	private JTable currentProcessTable;
	private DefaultTableModel processListModel;
	private JTable processListTable;
	private JScrollPane scrollPane;
	private JTextArea simTrace;
	
	public SimulationPanel(Simulation sim) {
		this.sim = sim;	
		this.setLayout(new GridBagLayout());
		coreCount = sim.cores.length;
		resourceCount = sim.systemResources.size();
		
		currentProcessModel = getCurrentProcessModel();
		processListModel = getProcessListModel();
		currentProcessTable = new JTable(currentProcessModel);
		processListTable = new JTable(processListModel);
		
		simTrace = new JTextArea();
		simTrace.setEditable(false);
		simTrace.append("SIMULATION TRACE\n--------------------------\n");
		GridBagConstraints gbc = new GridBagConstraints();
		
		scrollPane = new JScrollPane(currentProcessTable);
		scrollPane.setPreferredSize(new Dimension(200,200));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 2;
		gbc.weighty = 2;
		this.add(scrollPane,gbc);
		
		//currentCycleLabel = new JLabel("current Cycle: 0");

		scrollPane = new JScrollPane(simTrace);
		scrollPane.setPreferredSize(new Dimension(400,200));
		gbc.gridx = 2;
		gbc.weightx = 4;
		gbc.weighty = 2;
		this.add(scrollPane,gbc);
		
		scrollPane = new JScrollPane(processListTable);
		scrollPane.setPreferredSize(new Dimension(600,200));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 6;
		gbc.weighty = 2;	
		this.add(scrollPane,gbc);	
		
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sim.pauseUnpause();
				JButton button = (JButton)e.getSource();
				if(sim.pause) {		
					button.setText("Unpause");
				}
				else {
					button.setText("pause");
				}
			}
			
		});
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 1;
		gbc.weighty = 1;
		this.add(pauseButton,gbc);
		
		this.setSize(1000,500);
	}
	
	
	@Override
	public void run() {
		while (sim.run) {	
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			updateCurrentProcessModel();
			updateProcessListModel();
			updateSimTrace();
			this.repaint();
		}
	}
	
	private DefaultTableModel getCurrentProcessModel() {	
		String[] headers = {"Core","Current Process"};
		String[][] data = new String[coreCount][2];
		for (int i = 0; i < coreCount;i++) {
			data[i][0] = Integer.toString(i); 
			data[i][1] = Integer.toString(sim.cores[i].getCurrentProcess());
			
			data[i][1] = (data[i][1].equals("-1")) ? "N/A" : data[i][1];
			
		}
		DefaultTableModel model = new DefaultTableModel(data,headers);
		return model;
	}
	
	private void updateCurrentProcessModel() {
		for (int i = 0; i < coreCount;i++) {
			String data = Integer.toString(sim.cores[i].getCurrentProcess());		
			data = (data.equals("-1")) ? "N/A" : data;
			currentProcessModel.setValueAt(data, i, 1);
		}
	}
	
	private DefaultTableModel getProcessListModel() {
		String[] headers = {"Process ID","Process Name", "Priority", "CPU Usage","Status","Resource Locks"};
		Object[][] data = new Object[0][0];
		DefaultTableModel model = new DefaultTableModel(data,headers);
		return model;
	}
	
	private void updateProcessListModel() {		
		for (SimProcess process:sim.processes) {
			if (process.getEntryPoint() < sim.cycle) {
				int rowNum = findRowInModel(process.getPID());
				if (process.getStatus() == ProcessStatus.FINNISHED && rowNum != -1) {
					processListModel.removeRow(rowNum);
				}
				if (!(process.getStatus() == ProcessStatus.FINNISHED)) {
					if (rowNum == -1) {
						Object data[] = new Object[6];
						data[0] = process.getPID();
						data[1] = process.getName();
						data[2] = process.getPriority();
						data[3] = (float)process.getProgramCounter()/(sim.cycle*coreCount);
						data[4] = process.getStatus().toString();
						StringBuffer buffer = new StringBuffer();
						Object[] resources = process.getLockList().toArray();
						for (Object resource:resources) {
							buffer.append(resource);
							buffer.append(", ");
						}
						data[5] = buffer.toString();
						processListModel.addRow(data);
					}
					else {
						float ratio = (float)process.getProgramCounter()/(sim.cycle*coreCount);
						int percentage = (int) (ratio * 100);
						processListModel.setValueAt(percentage + "%", rowNum, 3);
						processListModel.setValueAt(process.getStatus().toString(), rowNum, 4);
					}
				}
			}
		}
	}
	
	private int findRowInModel(int pid) {
		int rowNum = 0;
		for (Object rowObject : processListModel.getDataVector()){
			Vector row = (Vector) rowObject;
			if ((int)row.get(0) == pid) {
				return rowNum;
			}
			rowNum++;
		}
		return -1;
	}

	private void updateSimTrace() {
		String line;
		line = sim.simTrace.getNextLine();
		while(line != null) {
			simTrace.append(line);
			line = sim.simTrace.getNextLine();
		}
	}
}
