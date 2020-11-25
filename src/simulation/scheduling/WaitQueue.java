package simulation.scheduling;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulation.Simulation;
import simulation.SimProcess.ProcessStatus;

public class WaitQueue {
	Map<Integer,Integer> waitQueue;
	ReadyQueue readyQueue;
	Simulation sim;
	public WaitQueue (Simulation sim, ReadyQueue readyQueue) {
		this.waitQueue = new HashMap<Integer,Integer>(); 
		this.readyQueue = readyQueue;
		this.sim = sim;
	}
	
	public void updateQueue(int resourceID) {
		List<Integer> removeKeys = new ArrayList<Integer>();
		
		for (Map.Entry<Integer, Integer> entry:waitQueue.entrySet()) {
			if(entry.getValue() == resourceID) {
				readyQueue.addToQueue(entry.getKey());
				removeKeys.add(entry.getKey());
				sim.processes.get(entry.getKey()).setStatus(ProcessStatus.READY);;
			}
		}
		
		for (int key:removeKeys) {
			waitQueue.remove(key);
		}
	}
	
	public void addToQueue(int pid, int resourceID) {
		waitQueue.put(pid,resourceID);
	}
}
