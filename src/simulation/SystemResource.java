package simulation;

import java.util.ArrayList;
import java.util.List;

public class SystemResource {
	private int id;
	private int instanceCount; //the amount of possible instances of this resource 
	private int[] locks;// an array of process IDs that have a lock on an instance of the resource. -1 implies no current lock;
	private Simulation sim;
	
	public SystemResource(int id, int intanceCount,Simulation sim) {
		super();
		this.id = id;
		this.instanceCount = intanceCount;
		this.sim = sim;
		this.locks = new int[intanceCount];
		for(int i = 0; i< instanceCount;i++) {
			locks[i] = -1;
		}
	}
	
	public boolean requestLock(int pid) {
		boolean declined=false;
		if (instanceCount > 0) {
			if (this.getAvailableInstanceCount() == 1) {
				for (int lockHolder:locks) {
					if(lockHolder != -1) {
						declined = (checkForCycle(lockHolder,this.id))?true:declined;
					}		
				}
				declined = (checkForCycle(pid,this.id))?true:declined;
			}			
		}
		else {
			declined = checkForCycle(pid,this.id);
		}
		
		if(declined) {
			sim.simTrace.printToTrace("process " + pid + " denied lock on resource " + id + " to prevent deadlock",sim.cycle);
		}
		else{
			declined = true;
			for(int i = 0;i< instanceCount;i++) {
				if (locks[i] == -1) {
					locks[i] = pid;
					i = instanceCount;
					declined = false;	
					sim.simTrace.printToTrace("process " + pid + " given lock on resource " + id ,sim.cycle);
				}
			}
		}
		return declined;
	}
	
	private boolean checkForCycle(int pid,int resourceID) {
		List<Integer> futureRequests = sim.processes.get(pid).getFutureResourceRequests();
		boolean cycleFound = false;
		for(int request:futureRequests) {
			if(request == resourceID) {
				return true;
			}
			if(sim.systemResources.get(request).getAvailableInstanceCount() == 0) {
				int [] lockHolders = sim.systemResources.get(request).getLockList();
				for (int lockHolder:lockHolders) {
					cycleFound = (checkForCycle(lockHolder,resourceID))?true:cycleFound;
				}
			}
		}
		return cycleFound;
	}
	
	public void releaseLock(int pid) {
		boolean valid = false;
		System.out.println("Process "+pid+" released resource "+id);
		for(int i = 0;i< instanceCount;i++) {
			if (locks[i] == pid) {
				locks[i] = -1;
				valid = true;
				sim.simTrace.printToTrace("process " + pid + " released lock on resource " + id ,sim.cycle);
				break;
			}
		}
		if (valid = false) {
			throw new IllegalArgumentException("Process :" + pid + "did not have lock");
		}
	}
	
	public int getID() {
		return id;
	}
	
	public int getInstanceCount() {
		return instanceCount;
	}
	
	public int[] getLockList() {
		return locks;
	}
	
	public int getAvailableInstanceCount() {
		int lockCount = 0;
		for (int lock:locks) {
			if (lock != -1) {
				lockCount++;
			}
		}
		return instanceCount - lockCount;
	}
}

