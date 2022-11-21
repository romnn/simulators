/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
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

	Contributors:  Eldhose Peter
*****************************************************************************/

package config;

import generic.PortType;
import net.RoutingAlgo;
import net.NOC.CONNECTIONTYPE;
import net.NOC.TOPOLOGY;
import net.RoutingAlgo.ARBITER;
import net.RoutingAlgo.SELSCHEME;

public class NocConfig
{
	public long operatingFreq;
	public TOPOLOGY topology;
	public int latency;
	public PortType portType;
	public int accessPorts;
	public int portOccupancy;
	public int numberOfBuffers;
	public RoutingAlgo.ALGO rAlgo;
	public int latencyBetweenNOCElements;
	public SELSCHEME selScheme;
	public ARBITER arbiterType;
	public int technologyPoint;
	public CONNECTIONTYPE ConnType;
	public int numberOfColumns;
	public int numberOfRows;
	public String NocTopologyFile; 
	
	public int getLatency() {
		return latency;
	}

	public int getAccessPorts() {
		return accessPorts;
	}

	public int getPortOccupancy() {
		return portOccupancy;
	}
	public void setLatency(int latency) {
		this.latency = latency;
	}

	public void setAccessPorts(int accessPorts) {
		this.accessPorts = accessPorts;
	}

	public void setPortOccupancy(int portOccupancy) {
		this.portOccupancy = portOccupancy;
	}
	public void setTopology(TOPOLOGY topology)
	{
		this.topology = topology;
	}
	
	public TOPOLOGY getTopology()
	{
		return this.topology;
	}
	public int getNumberOfBankRows(){
		return this.numberOfRows;
	}
	public int getNumberOfBankColumns(){
		return this.numberOfColumns;
	}

	public EnergyConfig  power;
}