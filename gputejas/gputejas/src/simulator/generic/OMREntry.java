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
package generic;

import java.util.ArrayList;
import memorysystem.AddressCarryingEvent;

public class OMREntry {
	public ArrayList<AddressCarryingEvent> outStandingEvents;
	public AddressCarryingEvent eventToForward;
	
	public OMREntry(ArrayList<AddressCarryingEvent> outStandingEvent, AddressCarryingEvent eventToForward)
	{
		this.outStandingEvents = outStandingEvent;
		this.eventToForward = eventToForward;
	}
	
	public boolean containsWriteToAddress(long addr)
	{
		if(eventToForward != null && eventToForward.getRequestType() == RequestType.Cache_Write &&
				eventToForward.getAddress() == addr)
		{
			return true;
		}
		
		for(int i = 0; i < outStandingEvents.size(); i++)
		{
			if(outStandingEvents.get(i).getRequestType() == RequestType.Cache_Write &&
					outStandingEvents.get(i).getAddress() == addr)
			{
				return true;
			}
		}
		
		return false;
	}
}
