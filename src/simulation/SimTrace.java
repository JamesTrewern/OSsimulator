package simulation;

import java.util.ArrayList;
import java.util.List;

public class SimTrace {
	private List<String> buffer;
	private int index;
	
	public SimTrace() {
		buffer = new ArrayList<String>();
		index = 0;
	}
	
	public void printToTrace(String line,int clockCycle) {
		buffer.add("Clock Cycle ("+clockCycle+"): "+line+ "\n");
	}
	
	public String getNextLine() {
		try {
			String line = buffer.get(index) ;
			index++;
			return line;
		}
		catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public List<String> getLines() {
		return buffer;
	}
}
