package simulation.scheduling;

public class RunningBuffer {
	public int pid;
	public boolean full;
	
	public RunningBuffer() {
		full = false;
	}
	
	public synchronized void add(int pid) {
		this.pid = pid;
		full = true;
	}
	
	public synchronized int remove(){
	full = false;
	return this.pid;
	}
}
