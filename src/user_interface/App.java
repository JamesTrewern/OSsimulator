package user_interface;

import simulation.Simulation;

public class App {

	public static void main(String[] args) {
		Simulation sim = new Simulation();
		AppGui gui = new AppGui(sim);
	}
	

}
