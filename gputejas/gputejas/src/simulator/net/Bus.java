package net;

import java.io.FileWriter;

import config.EnergyConfig;
//import config.EnergyConfig;
import config.SystemConfig;
import generic.Event;

public class Bus extends InterConnect {
	 BusArbiter busArbiter;
	 long hopCounter;
	 public Bus()
	 {
		 busArbiter = new BusArbiter();
		 hopCounter=0;
	 }
	 
	 public void sendBusMessage(Event event)
	 {
		 //TODO : Add code for arbiter here
		 hopCounter++;
		 event.addEventTime(SystemConfig.busConfig.getLatency());
		 event.getProcessingElement().getPort().put(event);
//		 System.out.println(event.getProcessingElement());
	 }
	 
	 public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter,
				String name) {
			EnergyConfig energyConfig = new EnergyConfig(0, 0);
			//TODO : Add bus energy
			energyConfig.add(calculateEnergy());
			return energyConfig;
		}
		public EnergyConfig calculateEnergy() {
			if(hopCounter == 0)
			{
				return new EnergyConfig(0, 0);
			}
			EnergyConfig power = new EnergyConfig(SystemConfig.busEnergy, hopCounter);
			return power;
		}

}
