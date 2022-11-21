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

import java.util.Comparator;

/**
 *events firstly sorted in increasing order of event time
 *secondly, by event type
 *		- denoted by priority : higher the priority, earlier it is scheduled
 *thirdly, by tie-breaker
 *      - in some cases, a relative ordering, between events of the same type,
 *        that are scheduled at the same time, is desired.
 *      - this is enforced by having a third parameter for sorting, i.e, tie-breaker.
 *      - smaller the tie-breaker, earlier it is scheduled
 */

public class EventComparator implements Comparator<Event> 
{
	public int compare(Event newEvent0, Event newEvent1)
	{
		if(newEvent0.getEventTime() < newEvent1.getEventTime())
		{
			return -1;
		}

		else if(newEvent0.getEventTime() > newEvent1.getEventTime())
		{
			return 1;
		}
		
		else
		{
			if(newEvent0.getPriority() > newEvent1.getPriority())
			{
				return -1;
			}
			else if(newEvent0.getPriority() < newEvent1.getPriority())
			{
				return 1;
			}
			else
			{
					return 0;
			}
		}
	}
}