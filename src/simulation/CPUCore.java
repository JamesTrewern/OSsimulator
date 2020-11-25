package simulation;
import java.util.TimerTask;

import simulation.scheduling.WaitQueue;
import simulation.SimProcess.ProcessStatus;
import simulation.scheduling.ReadyQueue;
import simulation.scheduling.RunningBuffer;

public class CPUCore extends TimerTask{
	private ReadyQueue readyQueue;
	private RunningBuffer runningBuffer;
	private WaitQueue ioQueue;
	private int coreID;
	private int pid;
	private int clockCycles;
	private int processTimeLeft;
	private Simulation sim;
	public CPUCore(int coreID,ReadyQueue readyQueue, RunningBuffer runningBuffer, WaitQueue ioQueue,Simulation sim) {
		this.coreID = coreID;
		this.readyQueue = readyQueue;
		this.runningBuffer = runningBuffer;
		this.ioQueue = ioQueue;
		this.sim = sim;
		this.pid = -1;
		this.processTimeLeft = 0;
	}
	
	
	/**Simulates a clock cycle for the CPU by running the next step method of a Process
	 * until processTimeLeft = 0 when the core will change the current running process to 
	 * the process in the running buffer. 
	*/
	@Override
	public void run() {
		if (!sim.pause|| clockCycles<sim.cores[0].clockCycles) {
			if (this.processTimeLeft == 0) {
				if (pid != -1) {
					if (this.sim.processes.get(pid).endBurst(clockCycles)) {
						sim.finishedProcesses.add(pid);
						sim.simTrace.printToTrace("Core" + coreID + " added " + pid + " to finsished process",clockCycles);
					}else if (sim.processes.get(pid).getStatus() != ProcessStatus.WAIT){
						readyQueue.addToQueue(pid);
					}
					
					pid = -1;			
				}
				if (this.runningBuffer.full) {
					this.pid = this.runningBuffer.remove();
					sim.simTrace.printToTrace("Core " + coreID + " switched to process " + pid,clockCycles);
					this.sim.processes.get(this.pid).addWaitTime(clockCycles);
					this.processTimeLeft = this.sim.processes.get(this.pid).getCPUBurst();
					this.sim.processes.get(this.pid).setStatus(ProcessStatus.RUN);
				}
			}
			else {
				this.processTimeLeft--;
				if(this.sim.processes.get(this.pid).nextStep()) {
					this.ioQueue.addToQueue(this.pid,this.sim.processes.get(this.pid).getWaitForResourceID());
					sim.processes.get(pid).setStatus(ProcessStatus.WAIT);
					this.processTimeLeft = 0;
				}
			}
			clockCycles++;
		}

	}
	
	public int getCurrentProcess() {
		return pid;
	}
	
	public int getClockCycle() {
		return clockCycles;
	}
}
