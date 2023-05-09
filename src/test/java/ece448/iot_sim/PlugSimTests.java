package ece448.iot_sim;

import static org.junit.Assert.*;

//import org.junit.Test;
import org.junit.jupiter.api.Test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.PlugSim.Observer;
import java.util.ArrayList;


public class PlugSimTests {

	private final ArrayList<Observer> observers = new ArrayList<>();

	@Test
	public void testInit() {
		PlugSim plug = new PlugSim("a");
		assertFalse(plug.isOn());
	}

	@Test
	public void testisOn() {
		PlugSim plug = new PlugSim("a");
		// isOn() is a getter for the 'on' boolean variable. 
		// initially the state of 'on' is set to false: PlugSim.java:12
		// test to make sure its set to this state before switchOn() method is run.
		boolean off = false;
		assertEquals(off, plug.isOn());
		// test to make sure it pulls back changed state on 'on' if we toggle it.
		plug.switchOn();
		boolean on = true;
		assertEquals(on, plug.isOn()); // returns current state of 'true' after we switch on.
	}

	@Test
	public void testinitPower() {
		PlugSim plug = new PlugSim("a");
		double initPower = 0;
		double p = plug.getPower(); // getPower is a getter for our power variable PlugSim.java:13
		assertEquals(initPower, p, 0.01);
	}

	@Test
	public void testgetName() {
		PlugSim plug = new PlugSim("a");
		String name = plug.getName();
		assertTrue(name == "a");
	}

	@Test
	public void testSwitchOn() {
		PlugSim plug = new PlugSim("a");;
		assertFalse(plug.isOn()); // first make sure we confirm its off.
		plug.switchOn();
		assertTrue(plug.isOn());
	}

	@Test public void testisOff() {
		PlugSim plug = new PlugSim("a");
		assertFalse(plug.isOn());
	}

	@Test
	public void testSwitchOff() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn(); // make sure we first turn it on.
		assertTrue(plug.isOn());
		plug.switchOff(); // ok now test to make sure its off.
		assertFalse(plug.isOn());
	}

	// toggle on, then toggle off. test isOn() return boolean values after each toggle.
	@Test
	public void testtoggle() {
		PlugSim plug = new PlugSim("a");

		plug.toggle();
		assertTrue(plug.isOn());

		plug.toggle();
		assertFalse(plug.isOn());
	}

	@Test
	public void testmeasurePower() {
		PlugSim plug = new PlugSim("a");

		// start by testing if our initial value is 0, it should be if isOn() is F
		plug.switchOff();
		plug.measurePower();
		double p = plug.getPower();
		assertEquals(0, p, 0.01);

		// turn it on; get the power value; test to make sure onPowerInit is greater than p/0.
		//PluginSim.java:69-72
		plug.switchOn();
		plug.measurePower();
		double onPowerInit = plug.getPower();
		assertTrue(onPowerInit > p);

		// now to test each additional conditional of measurePower()
		// PluginSim.java:80
		plug.updatePower(99);
		double power99 = plug.getPower();
		plug.measurePower();
		double powerOver100 = plug.getPower();
		assertTrue(powerOver100 > power99);

		//PluginSim.java:84
		plug.updatePower(301);
		double power301 = plug.getPower();
		plug.measurePower();
		// since the result of power ends up being a random value
		// lets just make sure they are different from eachother
		double newPowerV = plug.getPower();
		assertTrue(power301 != newPowerV);

		//PluginSim.java:88
		plug.updatePower(200);
		double power200 = plug.getPower();
		// if power is not < 100 or > 300
		plug.measurePower();
		// again make sure the numbers are different since power
		// ends in a random walk
		double finalPowerP = plug.getPower();
		assertTrue(power200 != finalPowerP);
	}

	@Test
	public void testUpdatePower() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		double initPower = 10;
		plug.updatePower(initPower);
		double currentPower = plug.getPower();
		assertTrue(initPower == currentPower);
		
		// test to make sure the observer has the power values
		String expected = "power: "+ currentPower;
		for (Observer observer: observers) {
			assertEquals(expected, observer.toString());
		}
		
	}

	@Test
	public void testaddObserver() {
		// first we need to create plugSim and do something to trigger state change.
		// keep a running count of the observers as well.
		PlugSim plug = new PlugSim("a");

		plug.switchOn();
		Integer observers_count = 0;

		for (Observer observer: observers) {
			observers_count += 1;
			plug.addObserver(observer);
		}

		Integer observer_size = observers.size();
		assertTrue(observer_size==observers_count);
	}

	@Test
	public void testupdateState() {
		PlugSim plug = new PlugSim("a");
		plug.updateState(true);
		Boolean on = plug.isOn();

		String expected = "state: "+ on;
		for (Observer observer: observers) {
			assertEquals(expected, observer.toString());
		}
		assertTrue(on);

		plug.updateState(false);
		Boolean off = plug.isOn();
		assertFalse(off);
		String expected2 = "state: "+ off;

		// test to make sure the observer has the power values
			for (Observer observer: observers) {
				assertEquals(expected2, observer.toString());
			}
	}

	@Test
	public void testLogging() {
		// PlugSim.java:96
		// updatePower() uses logging. 
		// we dont have the slf4-test library, but we can still test to make sure our LoggerFactory works
		logger.debug("This tests to make sure the logging plugin slf4j logs in debug mode.");
	}

	private static final Logger logger = LoggerFactory.getLogger(PlugSimTests.class);

}
