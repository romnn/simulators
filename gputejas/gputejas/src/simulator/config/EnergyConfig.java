package config;

import generic.GlobalClock;
import generic.Statistics;

import java.io.FileWriter;
import java.io.IOException;

public class EnergyConfig {
	public double leakageEnergy;
	public double dynamicEnergy;
	public long numAccesses = 0;
	
	public EnergyConfig(double leakagePower, double dynamicPower) {
		this.leakageEnergy = leakagePower;
		this.dynamicEnergy = dynamicPower;
	}
	
	public EnergyConfig(EnergyConfig power, long numAccesses) {
		
		this.leakageEnergy = power.leakageEnergy * (GlobalClock.getCurrentTime());
		this.dynamicEnergy = power.dynamicEnergy*numAccesses;
		this.numAccesses = numAccesses;
	}
	
	public String toString()
	{
		return " " + leakageEnergy
				+ "\t" + dynamicEnergy
				+ "\t" + (leakageEnergy + dynamicEnergy);
	}
	
	public void add(EnergyConfig a, EnergyConfig b)
	{
		leakageEnergy = a.leakageEnergy + b.leakageEnergy;
		dynamicEnergy = a.dynamicEnergy + b.dynamicEnergy;
	}
	
	public void add(EnergyConfig a)
	{
		leakageEnergy += a.leakageEnergy;
		dynamicEnergy += a.dynamicEnergy;
	}

	public void printEnergyStats(FileWriter outputFileWriter, String componentName) throws IOException {
		outputFileWriter.write(componentName + "\t" + Statistics.formatDouble(leakageEnergy) + "\t" + Statistics.formatDouble(dynamicEnergy) 
				+ "\t" + Statistics.formatDouble((leakageEnergy + dynamicEnergy)) + "\t" + numAccesses + "\n");
	}
}
