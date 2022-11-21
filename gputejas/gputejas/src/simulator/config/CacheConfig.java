/*****************************************************************************
				GPUTejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2014] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Seep Goel, Geetika Malhotra, Harinder Pal
*****************************************************************************/
package config;

import memorysystem.Cache.CoherenceType;
import memorysystem.NucaCache.NucaType;
import memorysystem.NucaCache.Mapping;
import memorysystem.Cache;
import generic.PortType;
import generic.MultiPortingType;

public class CacheConfig 
{
	public WritePolicy writePolicy;
	public Cache.CacheType levelFromTop;
	public boolean isLastLevel;
	public String nextLevel;
	public int blockSize;
	public int numEntries;
	public int assoc;
	public int size;
	public int latency;
	
	public PortType portType;
	public NucaType nucaType;
	public int accessPorts;
	public int portOccupancy;
	public MultiPortingType multiportType;
	public CoherenceType coherence;
	public int numberOfBuses;
	public int busOccupancy;
	public Mapping mapping;
	public String cacheName;
	public int mshrSize;
	public CacheEnergyConfig power;
	
	public String nextLevelId;
	
	public boolean isDirectory = false;
	
	public static enum WritePolicy{
		WRITE_BACK, WRITE_THROUGH
	}

	//Getters and setters
	
	public WritePolicy getWritePolicy() {
		return writePolicy;
	}

	public boolean isLastLevel() {
		return isLastLevel;
	}

	public String getNextLevel() {
		return nextLevel;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getAssoc() {
		return assoc;
	}

	public int getSize() {
		return size;
	}

	public int getLatency() {
		return latency;
	}

	public int getAccessPorts() {
		return accessPorts;
	}

	public int getPortOccupancy() {
		return portOccupancy;
	}

	public MultiPortingType getMultiportType() {
		return multiportType;
	}
	
	protected void setWritePolicy(WritePolicy writePolicy) {
		this.writePolicy = writePolicy;
	}

	protected void setLastLevel(boolean isLastLevel) {
		this.isLastLevel = isLastLevel;
	}

	protected void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}

	protected void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	protected void setAssoc(int assoc) {
		this.assoc = assoc;
	}

	protected void setSize(int size) {
		this.size = size;
	}

	protected void setLatency(int latency) {
		this.latency = latency;
	}

	protected void setAccessPorts(int accessPorts) {
		this.accessPorts = accessPorts;
	}

	protected void setPortOccupancy(int portOccupancy) {
		this.portOccupancy = portOccupancy;
	}

	protected void setMultiportType(MultiPortingType multiportType) {
		this.multiportType = multiportType;
	}

	public Cache.CacheType getLevelFromTop() {
		return levelFromTop;
	}

	protected void setLevelFromTop(Cache.CacheType levelFromTop) {
		this.levelFromTop = levelFromTop;
	}

	public CoherenceType getCoherence() {
		return coherence;
	}

	public void setCoherence(CoherenceType coherence) {
		this.coherence = coherence;
	}

	public int getNumberOfBuses() {
		return numberOfBuses;
	}

	public int getBusOccupancy() {
		return busOccupancy;
	}

	public void setBusOccupancy(int busOccupancy) {
		this.busOccupancy = busOccupancy;
	}
		
}
