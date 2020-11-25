package simulation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulation.SimProcess.ProcessStatus;
import simulation.scheduling.Dispatcher;
import simulation.scheduling.WaitQueue;
import simulation.scheduling.ReadyQueue;
import simulation.scheduling.RunningBuffer;

public class Simulation implements Runnable{
	RunningBuffer[] runningBuffers;
	public CPUCore[] cores;
	WaitQueue waitQueue;
	public ReadyQueue readyQueue;
	Dispatcher dispatcher;
	Timer timer;
	public List<SimProcess> processes;
	public List<SystemResource> systemResources;
	List<Integer> finishedProcesses;
	public SimTrace simTrace;
	public int cycle;
	public boolean run;
	public boolean pause;
	private int clockSpeed;
	
	public Simulation() {
		this.readyQueue = new ReadyQueue(this);
		this.waitQueue = new WaitQueue(this,readyQueue);
		this.processes = new ArrayList<SimProcess>();
		this.systemResources = new ArrayList<SystemResource>();
		this.finishedProcesses = new ArrayList<Integer>();
		this.simTrace = new SimTrace();
	}

	public void startSetInterval(String fileName) {
		int[] settings = getSettings();
		this.clockSpeed = settings[0];
		int coreCount = settings[1];
		int CPUBurst = settings[2];
		run = true;
		coreCount = (coreCount>4)? 4:coreCount;
		cores = new CPUCore[coreCount];
		runningBuffers = new RunningBuffer[coreCount];
		loadSimulationFile(fileName,CPUBurst);
		timer = new Timer();
		for (int i = 0;i<coreCount;i++) {
			this.runningBuffers[i] = new RunningBuffer();
			cores[i] = new CPUCore(i,readyQueue, runningBuffers[i], waitQueue,this);
		}
		for (int i = 0;i<coreCount;i++) {
			timer.scheduleAtFixedRate(cores[i],10, clockSpeed);
		}
		this.dispatcher = new Dispatcher(this.readyQueue, this.runningBuffers,this);
		Thread dispatcherThread = new Thread(dispatcher);
		Thread simulationThread = new Thread(this);
		dispatcherThread.start();	
		simulationThread.start();
	}

	@Override
	public void run() {
		while (run) {
			if (!pause) {
				if (cycle != cores[0].getClockCycle()) {
					
					for (SimProcess process:processes) {
						if (process.getEntryPoint() == cycle) {
							readyQueue.addToQueue(process.getPID());
							process.setStatus(ProcessStatus.READY);
							simTrace.printToTrace("Process " + process.getPID() + " started", cycle);
						}
					}
					cycle = cores[0].getClockCycle();
				}
				if(processes.size() == finishedProcesses.size()) {
					run = false;
				}
				try {
					Thread.sleep(clockSpeed/2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		simTrace.printToTrace("simulation finished",cycle);
		writeReportToFile();
		timer.cancel();
	}
	
	public void pauseUnpause() {
		pause = !pause;
	}
	
	
	private void getProcessListFromJSON(JSONObject file,int defaultCPUBurst) {
		JSONArray jsonArray = file.getJSONArray("processes");
		for (int i = 0; i < jsonArray.length();i++) {
			JSONObject json = jsonArray.getJSONObject(i);
			String name = json.getString("name");
			int cyclesRequired = json.getInt("cyclesRequired");
			int priority = json.getInt("priority");
			priority = (priority < 0)?0:priority;
			priority = (priority > 2)?2:priority;
			int cpuBurst = defaultCPUBurst;
			int entryPoint = json.getInt("entryPoint");
			
			JSONArray jsonRequestPoints = json.getJSONArray("resourceRequestPoints");
			int[][] requestPoints = new int[jsonRequestPoints.length()][2];
			for (int j = 0; j < jsonRequestPoints.length(); j++) {
				requestPoints[j][0] = jsonRequestPoints.getJSONObject(j).getInt("programCounter");
				requestPoints[j][1] = jsonRequestPoints.getJSONObject(j).getInt("resourceID");
			}
			
			
			processes.add(new SimProcess (i,name,cyclesRequired,priority,requestPoints, cpuBurst,entryPoint,this));
			}
	}
	
	private void getSystemResourcesFromJSON(JSONObject file) {
		JSONArray resourceArray = file.getJSONArray("systemResources");
		for (int i =0;i<resourceArray.length();i++) {
			systemResources.add(new SystemResource(i,resourceArray.getJSONObject(i).getInt("intanceCount"),this));
		}
	}

	private void loadSimulationFile(String fileName,int defaultCPUBurst) {
		File file = new File("input\\" +fileName + ".json");
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JSONTokener tokener = new JSONTokener(is);
		JSONObject json = new JSONObject(tokener);
		
		getProcessListFromJSON(json,defaultCPUBurst);
		getSystemResourcesFromJSON(json);
	}
	
 	public int getRunningProcessCount() {
		int result = 0;
		for(SimProcess process:processes) {
			if (!(process.getStatus() == ProcessStatus.FINNISHED)) {
				result++;
			}
		}
		return result; 
	}
 	
 	private void writeReportToFile() {
 		List<String> lines = simTrace.getLines();
 		lines.add("Simulation finished after " + cycle + " clock cycles\n");
 		int totalWaitTime = 0;
 		for (SimProcess process:processes) {
 			totalWaitTime += process.getTotalWaitTime();
 			lines.add("Process "+process.getPID() + " total wait time: " + process.getTotalWaitTime()+ "\n");
 		}
 		lines.add("Total wait time: " + totalWaitTime + "\n");
 		try {
 			 SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH-mm-ss");
 			 File file = new File("output\\" + formatter.format(new Date())+".txt");
 			 file.createNewFile();
 			 FileWriter fw = new FileWriter(file);
 			 int[] settings = getSettings();
 			 fw.write("Simulation Settings: time slice: " + settings[2]+", CPU Core Count: " + settings[1] + "\n");
 			 for(String line:lines) {
 				 fw.write(line);
 			 }
 			 fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
 	}
 	
 	private int[] getSettings() {
 		int[] settings = new int[3];
 		try {
 			BufferedReader br = new BufferedReader(new FileReader("settings.txt"));
			String line = br.readLine();
			int i = 0;
			while (line!= null) {
				settings[i] = Integer.parseInt(line);
				i++;
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
 		return settings;
 	}
}
