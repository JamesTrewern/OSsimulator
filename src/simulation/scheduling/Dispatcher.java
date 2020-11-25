package simulation.scheduling;

import simulation.SimProcess;
import simulation.Simulation;

public class Dispatcher implements Runnable {

	private ReadyQueue readyQueue;
	private RunningBuffer[] runningBuffers;
	private Simulation sim;
	
	public Dispatcher(ReadyQueue readyQueue, RunningBuffer[] runningBuffers,Simulation sim) {
		this.readyQueue = readyQueue;
		this.runningBuffers = runningBuffers;
		this.sim = sim;
	}
	
	@Override
	public void run() {
		while (sim.run) {
			boolean wait = true;
			for (RunningBuffer runningBuffer: runningBuffers) {
				if (!runningBuffer.full) {
					wait = false;
					int pid = readyQueue.getNext();
					if (pid!= -1) {
						runningBuffer.add(pid);
					}	
				}
			}
			if (wait) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
