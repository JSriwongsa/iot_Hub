package ece448.iot_sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Simulate a smart plug with power monitoring.
 */
public class PlugSim {

	private final String name;
	private boolean on = false;
	private double power = 0; // in watts
	private final ArrayList<Observer> observers = new ArrayList<>();

	public PlugSim(String name) {
		this.name = name;
	}

	// Observer pattern implementation - Lecture 10
	public static interface Observer {
		void update (String name, String key, String value);
	}

	// Observer pattern implementation - Lecture 10
	synchronized public void addObserver(Observer observer) {
		observers.add(observer);
		observer.update(name, "state", on? "on":"off");
		observer.update(name, "power", String.format("%.3f", power));
	}

	/**
	 * No need to synchronize if read a final field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Switch the plug on.
	 */
	synchronized public void switchOn() {
		// P1: add your code here
		// boolean logic: if on==false, set it to true
		//if (!on){
		//	on = true;
		//	return;
		//}
		updateState(true);
	}

	// Observer pattern implementation - Lecture 10
	protected void updateState(boolean o) {
		on = o;
		logger.info("Plug {}: state {}", name, on? "on": "off");
		for (Observer observer: observers) {
			observer.update(name, "state", on? "on": "off");
		}
	}

	// Observer pattern implementation - Lecture 10	
	protected void updatePower(double p) {
		power = p;
		logger.debug("Plug {}: power {}", name, power);
		for (Observer observer: observers) {
			observer.update(name, "power", String.format("%.3f", power));
		}
	}
	/**
	 * Switch the plug off.
	 */
	synchronized public void switchOff() {
		// P1: add your code here
		// boolean logic: if on==true, set it to false
		//if (on) {
		//	on = false;
		//	return; 
		//}
		updateState(false);
	}

	/**
	 * Toggle the plug.
	 */
	synchronized public void toggle() {
		// P1: add your code here
		// this needs to utilize the isOn() function to determine state and then call switchOff() or switchOn() depending if isOn() is T/F
		if (isOn()) {
			switchOff();
			return;
		} else {
			switchOn();
			return;
		}
	}

	/**
	 * Measure power.
	 */
	synchronized public void measurePower() {
		if (!on) {
			updatePower(0);
			return;
		}

		// a trick to help testing
		if (name.indexOf(".") != -1)
		{
			updatePower(Integer.parseInt(name.split("\\.")[1]));
		}
		// do some random walk
		else if (power < 100)
		{
			updatePower(power + Math.random() * 100);
		}
		else if (power > 300)
		{
			updatePower(power - Math.random() * 100);
		}
		else
		{
			updatePower(power + Math.random() * 40 - 20);
		}
	}

	/**
	 * Getter: current state
	 */
	synchronized public boolean isOn() {
		return on;
	}

	/**
	 * Getter: last power reading
	 */
	synchronized public double getPower() {
		return power;
	}

	private static final Logger logger = LoggerFactory.getLogger(PlugSim.class);
}
