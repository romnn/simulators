/*
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

package memorysystem;

import java.util.Stack;
import java.util.Vector;

import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;

public class SignalWavelengthEvent  extends AddressCarryingEvent{

	private int wavelength;
	public SignalWavelengthEvent(EventQueue eventQ, 
			long eventTime, SimulationElement requestingElement, SimulationElement processingElement,
			RequestType requestType, long address, int wavelength, Vector<Integer> sourceBankId, 
			Vector<Integer> destinationBankId, Vector<Integer> oldSourceBankId,
			SimulationElement oldReqElement, Stack<SimulationElement> simStack, Stack<RequestType> reqStack, int tpcId, int smId) {
		
		super(eventQ, eventTime, requestingElement, processingElement, requestType, address);
		
		this.wavelength = wavelength; 
		this.setSourceId(sourceBankId);
		this.setDestinationId(destinationBankId);
		this.tpcId = tpcId;
		this.smId = smId;	
	}
	
	public void setWavelength(int wavelength){
		this.wavelength = wavelength;
	}
	public int getWavelength(){
		return this.wavelength;
	}
}
