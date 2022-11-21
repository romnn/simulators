package net;

import main.ArchitecturalComponent;
import dram.MainMemoryDRAMController;
import generic.CommunicationInterface;
import generic.Event;
import java.util.*;
import config.MainMemoryConfig;
import config.SystemConfig;
public class BusInterface implements CommunicationInterface{

	Bus bus;
	public BusInterface(Bus bus) {
		super();
		this.bus = bus;
	}
	
	/*
	 * Messages are coming from simulation elements(cores, cache banks) in order to pass it to another
	 * through electrical snooping Bus.
	 */
	//@Override
	public void sendMessage(Event event) {
		bus.sendBusMessage(event);		
	}

	public MainMemoryDRAMController getNearestMemoryController() {
		// TODO Auto-generated method stub

		Random rand = new Random();
		int n=rand.nextInt(SystemConfig.mainMemoryConfig.numChans);
		return ArchitecturalComponent.getMainMemoryDRAMController(this,n);
	}
}
