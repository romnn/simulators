package generic;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import config.SystemConfig;

public class GlobalClock {
	 static long currentTime;
	 static int stepSize;

	public static long getCurrentTime() {
		return currentTime;
	}

	public static void setCurrentTime(long currenttime) {
		currentTime = currenttime;
	}
	
	public static void incrementClock()
	{
		currentTime += stepSize;
	}

	public static int getStepSize() {
		return stepSize;
	}

	public static void setStepSize(int stepsize) {
		stepSize = stepsize;
	}
	public static EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		double leakagePower = SystemConfig.globalClockPower.leakageEnergy;
		double dynamicPower = SystemConfig.globalClockPower.dynamicEnergy;
		
		EnergyConfig power = new EnergyConfig(leakagePower, dynamicPower);
		
		power.printEnergyStats(outputFileWriter, componentName);
		
		return power;
	}
	
}
