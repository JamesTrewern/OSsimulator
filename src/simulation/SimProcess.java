package simulation;

import java.util.ArrayList;
import java.util.List;

import simulation.SimProcess.ProcessStatus;

public class SimProcess {
	private int pid;
	private String name;
	public int priority;	
	private int programCounter;
	private int[][] resourceRequestPoints; // a list of systemRequestPoints  with program counter and resource id
	private int cpuBurst; // next or current cpu burst in clock cycles
	private int lastRunningCycle; // the last cycle the process was in the cpu 
	private int endPoint; // the total amount of cycles before the program is finished 
	private int entryPoint; // the clock cycles at which the process first enters the ready queue
	private Simulation sim;
	private ProcessStatus processStatus;
	private List<Integer> lockedResources;
	private int waitForResourceID;
	private int totalWaitTime;
	
	public SimProcess(int pid, String name, int cyclesRequired, int priority, int[][] resourceRequestPoints, int cpuBurst, int entryPoint, Simulation sim) {
		this.pid = pid;
		this.name = name;
		this.endPoint = cyclesRequired;
		this.priority = priority;
		this.resourceRequestPoints = resourceRequestPoints;
		setCPUBurst(cpuBurst);
		this.lastRunningCycle = entryPoint;
		this.entryPoint = entryPoint;
		this.sim = sim;
		processStatus = ProcessStatus.NOT_STARTED;
		lockedResources = new ArrayList<Integer>();
	}
	
	public boolean nextStep() {	
		boolean wait = isWaitPoint();
		if (!wait) {
			this.programCounter++;
		}
		return wait;
	}
	
	public synchronized boolean  endBurst(int clockCycle) {
		setCPUBurst(cpuBurst);
		lastRunningCycle = clockCycle;
		if(programCounter == endPoint) {
			processStatus = ProcessStatus.FINNISHED;
			for(int resourceID:lockedResources) {
				sim.systemResources.get(resourceID).releaseLock(pid);
				sim.waitQueue.updateQueue(resourceID);
			}
			return true;
		}else {
			if (processStatus != ProcessStatus.WAIT) {
				processStatus = ProcessStatus.READY;
			}
			return false;
		}
	}
	
	private boolean isWaitPoint() {
		boolean wait = false;
		for (int[] requestPoint: this.resourceRequestPoints) {
			if (this.programCounter == requestPoint[0]) {
				if(sim.systemResources.get(requestPoint[1]).requestLock(pid)) {
					wait = true;
					processStatus = ProcessStatus.WAIT;
					this.waitForResourceID = requestPoint[1];
				}
				else {
					lockedResources.add(requestPoint[1]);
				}
			}
		}
		return wait;
	}
	
	private void releaseResource(int id) {
		for(int i = 0;i < lockedResources.size();i++) {
			if (lockedResources.get(i) == id) {
				lockedResources.remove(i);
				sim.systemResources.get(id).releaseLock(pid);
				sim.waitQueue.updateQueue(id);
			}
		}
	}
	
	private void setCPUBurst(int newBurst) {
		cpuBurst = (endPoint < (programCounter + newBurst))? endPoint - programCounter: newBurst;
	}
	
	public void setStatus(ProcessStatus status) {
		this.processStatus = status;
	}
	
	public void addWaitTime(int currentClockCycle) {
		totalWaitTime += currentClockCycle - lastRunningCycle;
	}
	
	public List<Integer> getFutureResourceRequests(){
		List<Integer> futureRequests = new ArrayList<Integer>();
		for (int[] requestPoint: resourceRequestPoints) {
			if(requestPoint[0]> programCounter) {
				futureRequests.add(requestPoint[1]);
			}
		}
		return futureRequests;
	}
	
	public int getPID() {
		return this.pid;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public int getProgramCounter() {
		return this.programCounter;
	}
	
	public int getEntryPoint() {
		return this.entryPoint;
	}
	
	public int getEndPoint() {
		return this.endPoint;
	}
	
	public int getCPUBurst() {
		return this.cpuBurst;
	}
	
	public int getLastRunningCycle() {
		return this.lastRunningCycle;
	}
	
	public int[][] getResourceRequestPoint(){
		return this.resourceRequestPoints;
	}
	
	public ProcessStatus getStatus() {
		return processStatus;
	}
	
	public int getWaitForResourceID() {
		return this.waitForResourceID;
	}
	
	public int getTotalWaitTime() {
		return this.totalWaitTime;
	}
	
	public synchronized List<Integer> getLockList(){
		return this.lockedResources;
	}
	
	public enum ProcessStatus{
		NOT_STARTED,
		RUN,
		WAIT,
		READY,
		FINNISHED;
	}
}

