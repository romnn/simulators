package dram;

import dram.MainMemoryBusPacket.BusPacketType;
import generic.RequestType;


public class BankState {

	public enum CurrentBankState
	{
		IDLE,
		ROW_ACTIVE,
		PRECHARGING,
		REFRESHING,
		POWER_DOWN
	}
	
	CurrentBankState currentBankState;
	int openRowAddress;						
	long nextRead;
	long nextWrite;
	long nextActivate;
	long nextPrecharge;
	long nextPowerUp;

	BusPacketType lastCommand;						
	
	BankState()
	{
		//System.out.println("HI!! Constructing Bank States");
		currentBankState = CurrentBankState.IDLE;
		openRowAddress = 0;						
		nextRead = 0;
		nextWrite = 0;
		nextActivate = 0;
		nextPrecharge = 0;
		nextPowerUp = 0;
		lastCommand = BusPacketType.READ;
	}
	
	public void printState()
	{
		/*
		System.out.println("current Bank State: "+ currentBankState +" "+ openRowAddress +" "						
		+ nextRead +" "+
		nextWrite +" "+
		nextActivate +" "+
		nextPrecharge +" "+
		nextPowerUp +" "+
		lastCommand);
		*/
	}

}
