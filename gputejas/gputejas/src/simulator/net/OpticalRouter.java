package net;

import main.ArchitecturalComponent;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import config.NocConfig;
import config.SystemConfig;

public class OpticalRouter extends Router {
	public OpticalRouter(NocConfig nocConfig, NocInterface reference) {
		super(nocConfig, reference);
		
	}
	int findTime(int x, int y, int i, int j)
	{
		long time = GlobalClock.getCurrentTime();
		int newy=y, newj=j;
		if(y%2==0)
			newy=y-1;
		if(j%2==0)
			newj=j-1;
		long avail = Math.max(ArchitecturalComponent.waitingTimeStation[i/2][newj/2], ArchitecturalComponent.waitingTime[x/2][newy/2]);
		if(avail > time)
		{
			return (int) (avail-time);
		}
		time = ((time/ArchitecturalComponent.reconfInterval + 1)*ArchitecturalComponent.reconfInterval) - time;
		return (int) time;
	}
	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		//Only in case of butterfly
		
		//System.out.println("Handle event has been invoked");
		ID id = this.getID();
		
		if((NocInterface) event.getProcessingElement().getComInterface()==null)
			return;
		ID destinationId = ((NocInterface) event.getProcessingElement().getComInterface()).getId();
		int newsx,newsy,newdx,newdy;
		
		newsx = id.getx();
		newsy = id.gety();
		newdx = destinationId.getx();
		newdy = destinationId.gety();
		
		if(newsy%2 == 0)
			newsy = newsy-1;
		if(newdy%2 == 0)
			newdy = newdy-1;
		
		if(event.getRequestingElement()!=this){
			
		
			if(id.getx()/2 == destinationId.getx()/2 && newsy/2 == newdy/2)
			{
				event.getProcessingElement().getPort().put(event.update(1));//intra cluster
				return;
			}
		}
		
		//System.out.println("Moved out of cluster");
		boolean coherence = false;
		if((newsy/2 == 1 || newsy/2 == 2) && (newdy/2 == 1 || newdy/2 == 2))
			coherence = true;
		
		long time = ArchitecturalComponent.availableTime(id.getx(), newsy ,destinationId.getx(), newdy,coherence);
		ArchitecturalComponent.requestsReceived[newsx/2][newsy/2]++;
		if(time == -1)
		{
			if(event.getRequestingElement()!=this){
				ArchitecturalComponent.pendingEvents++;
				event.update(this, this, event.getRequestingElement(), event.getProcessingElement());
			}
			ArchitecturalComponent.totalReq++;
			int waitTime = findTime(id.getx(), newsy,destinationId.getx(),newdy);
			ArchitecturalComponent.waitTime += waitTime;
			event.setEventTime( waitTime + GlobalClock.getCurrentTime());
			event.getEventQ().addEvent(event);
		}
		else
		{
			ArchitecturalComponent.requestsServiced[newsx/2][newsy/2]++;
			ArchitecturalComponent.TotalStationRequests[id.getx()/2][newsy/2]++;
			ArchitecturalComponent.ideal_Power++;
			ArchitecturalComponent.waitTime += (time-GlobalClock.getCurrentTime());
			ArchitecturalComponent.actualReq++;
			ArchitecturalComponent.totalReq++;
			if(event.getActualProcessingElement() != null)
			{
				ArchitecturalComponent.pendingEvents--;
				event.update(event.getActualRequestingElement(), event.getActualProcessingElement());
			}
			
			//ID sourceId = ((NocInterface) event.getRequestingElement().getComInterface()).getId();
			//ID destinationId = ((NocInterface) event.getProcessingElement().getComInterface()).getId();
			event.getProcessingElement().getPort().put(event.update(time-GlobalClock.getCurrentTime()));
		}	
	}
}
