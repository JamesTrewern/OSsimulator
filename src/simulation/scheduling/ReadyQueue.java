package simulation.scheduling;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import simulation.Simulation;

public class ReadyQueue {
	private List<Integer>[] readyQueue;
	private Simulation sim;
	public ReadyQueue(Simulation sim) {
		readyQueue = new List[3];
		for (int i = 0;i<3;i++) {
			readyQueue[i] = new ArrayList<Integer>();
		}
		this.sim = sim;
	}
	
	public synchronized int getNext() {
		int pid = -1;
		int highestResponseRatio = 0;
		int index = 0;
		BiFunction<Integer,Integer,Integer> ratioFunc = (waitTime,processTimeLeft) -> (waitTime + processTimeLeft)/processTimeLeft;
		for (int i=0;i<3;i++) {
			try {
				if (pid == -1) {
					int a = 0;
					for (int process:readyQueue[i]) {
						
						int waitTime = sim.cycle - sim.processes.get(process).getLastRunningCycle();
						int processTimeLeft = sim.processes.get(process).getEndPoint() - sim.processes.get(process).getProgramCounter();
						if (ratioFunc.apply(waitTime,processTimeLeft)> highestResponseRatio) {
							pid = process;
							index = a;
							highestResponseRatio = ratioFunc.apply(waitTime,processTimeLeft);
						}
						a++;
					}
				}
			}catch (IndexOutOfBoundsException | NullPointerException e) {
				pid = -1;
			}
		}		
		if(pid != -1) {
			readyQueue[sim.processes.get(pid).priority].remove(index);
		}
		return pid;
	}
	
	public synchronized void addToQueue(int pid) {
		readyQueue[sim.processes.get(pid).priority].add(pid);
	}
	
	
}
