package net;

import java.io.FileWriter;
import java.util.Vector;

import config.EnergyConfig;

//import config.EnergyConfig;

import generic.CommunicationInterface;

public abstract class InterConnect {
	public Vector<CommunicationInterface> networkElements;

	public InterConnect() {
		super();
		this.networkElements = new Vector<CommunicationInterface>();
	}

	public abstract EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String name);
}
