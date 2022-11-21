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
package net;

import java.util.ArrayList;
import java.util.Vector;

import net.NOC.TOPOLOGY;

public class RoutingAlgo{

	public static enum ALGO{
		WESTFIRST,
		NORTHLAST,
		NEGATIVEFIRST,
		TABLE,
		SIMPLE,
		FATTREE,
		OMEGA,
		BUTTERFLY
	}
	public static enum SELSCHEME{
		STATIC,
		ADAPTIVE
	}
	public static enum DIRECTION{
		UP,
		RIGHT,
		DOWN,
		LEFT
	}
	public static enum ARBITER{
		MATRIX_ARBITER ,
		RR_ARBITER, 
		QUEUE_ARBITER
	}
	
	public RoutingAlgo.DIRECTION nextBank(Vector<Integer> current, Vector<Integer> destination){
		
		// to find next bank ID
		if(current.elementAt(0) < destination.elementAt(0))
			return DIRECTION.DOWN;
		else if(current.elementAt(0) > destination.elementAt(0))
			return DIRECTION.UP;
		else if(current.elementAt(0) == destination.elementAt(0) && current.elementAt(1) < destination.elementAt(1))
			return DIRECTION.RIGHT;
		else if(current.elementAt(0) == destination.elementAt(0) && current.elementAt(1) > destination.elementAt(1))
			return DIRECTION.LEFT;
		return null;
	}
	/************************************************************************
     * Method Name  : XYnextBank
     * Purpose      : implementing XY routing algorithm
     * Parameters   : current id, destination id, topology, number of rows and columns
     * Return       : next direction
     *************************************************************************/
	public Vector<RoutingAlgo.DIRECTION> XYnextBank(ID current, ID destination, 
			NOC.TOPOLOGY topology, int numRows, int numColums)
	{	
		//XYRouting for mesh,torus,ring,bus
		// to find next bank ID
		Vector<RoutingAlgo.DIRECTION> choices = new Vector<RoutingAlgo.DIRECTION>();
		int x1,y1,x2,y2;
		x1 = current.getx();
		y1 = current.gety();
		x2 = destination.getx();
		y2 = destination.gety();
		if(topology == TOPOLOGY.BUS)
		{
			if(x1 < x2 || (x1 == x2 && y1 < y2))
				choices.add(DIRECTION.RIGHT);
			else
				choices.add(DIRECTION.LEFT);
			return choices;
		}
		else if(topology == TOPOLOGY.RING)
		{
			int hop;
			if(x2>x1)
				hop = (x2-x1-1) * (numColums-1) + (numColums - y1 - 1) + (y2) ; //number of hops
			else if(x2<x1)
				hop = (x1-x2-1) * (numColums-1) + (numColums - y2 - 1) + (y1) ; //number of hops
			else
				hop = Math.abs(y2-y1);
			if((numColums * numRows)/2 >= hop)
			{
				if(x1 < x2 || (x1 == x2 && y1 < y2))
					choices.add(DIRECTION.RIGHT);
				else
					choices.add(DIRECTION.LEFT);
				return choices;
			}
			else
			{
				if(x1 < x2 || (x1 == x2 && y1 < y2))
					choices.add(DIRECTION.LEFT);
				else
					choices.add(DIRECTION.RIGHT);
				return choices;
			}
				
		}
		if(x1 < x2){
			if(topology == TOPOLOGY.TORUS){
				if((x2-x1) > Math.ceil(numRows / 2))
					choices.add(DIRECTION.UP);
				else
					choices.add(DIRECTION.DOWN);
			}
			else
				choices.add(DIRECTION.DOWN);
		//	return choices;
		}
		else if(x1 > x2){
			if(topology == TOPOLOGY.TORUS){
				if((x1-x2) > Math.ceil(numRows / 2))
					choices.add(DIRECTION.DOWN);
				else
					choices.add(DIRECTION.UP);
				}
			else
				choices.add(DIRECTION.UP);
		//	return choices;
		}
		if(y1< y2){
			if(topology == TOPOLOGY.TORUS)
				if((y2-y1) > Math.ceil(numColums / 2)){
					choices.add(DIRECTION.LEFT);
					return choices;
				}
			choices.add(DIRECTION.RIGHT);
			return choices;
		}
		else if(y1 >y2){
			if(topology == TOPOLOGY.TORUS)
				if((y1-y2) > Math.ceil(numColums / 2)){
					choices.add(DIRECTION.RIGHT);
					return choices;
				}
			choices.add(DIRECTION.LEFT);
			return choices;
		}
		return choices;
	}
	/************************************************************************
     * Method Name  : WestFirstnextBank
     * Purpose      : implementing WestFirst routing algorithm
     * Parameters   : current id, destination id, topology, number of rows and columns
     * Return       : next direction
     *************************************************************************/
	public Vector<RoutingAlgo.DIRECTION> WestFirstnextBank(ID current, ID destination, 
			NOC.TOPOLOGY topology, int numRows, int numColums)
	{
		Vector<RoutingAlgo.DIRECTION> choices = new Vector<DIRECTION>();
		int y1,y2;
		y1 = current.gety();
		y2 = destination.gety();
		if(y2<y1){
			if(topology == TOPOLOGY.TORUS)
				if((y1-y2) > Math.ceil(numColums / 2)){
					choices.add(DIRECTION.RIGHT);
					return choices;
				}
			choices.add(DIRECTION.LEFT);
			return choices;
		}
		else
			return XYnextBank(current, destination, 
					topology, numRows, numColums);
	}
	/************************************************************************
     * Method Name  : NorthLastnextBank
     * Purpose      : implementing NorthLast routing algorithm
     * Parameters   : current id, destination id, topology, number of rows and columns
     * Return       : next direction
     *************************************************************************/
	public Vector<RoutingAlgo.DIRECTION> NorthLastnextBank(ID current, ID destination, 
			NOC.TOPOLOGY topology, int numRows, int numColums)
	{
		Vector<RoutingAlgo.DIRECTION> choices = new Vector<DIRECTION>();
		int x1,y1,x2,y2;
		x1 = current.getx();
		y1 = current.gety();
		x2 = destination.getx();
		y2 = destination.gety();
		if(x2 < x1){
			if(topology == TOPOLOGY.TORUS)
			{
				if(y2>y1)
				{
					if((y2-y1) > Math.ceil(numColums / 2))
						choices.add(DIRECTION.LEFT);
					else
						choices.add(DIRECTION.RIGHT);
				}
				else if(y1>y2)
				{
					if((y2-y1) > Math.ceil(numColums / 2))
						choices.add(DIRECTION.RIGHT);
					else
						choices.add(DIRECTION.LEFT);
				}
			}
			else if(topology == TOPOLOGY.MESH && y2<y1)
				choices.add(DIRECTION.LEFT);
			else if(topology == TOPOLOGY.MESH && y1>y2)
				choices.add(DIRECTION.RIGHT);
			if(choices.isEmpty())
				return XYnextBank(current, destination,topology, numRows, numColums);
			return choices;
		}
		else
			return XYnextBank(current, destination, 
					topology, numRows, numColums);
	}
	/************************************************************************
     * Method Name  : NegativeFirstnextBank
     * Purpose      : implementing NegativeFirst routing algorithm
     * Parameters   : current id, destination id, topology, number of rows and columns
     * Return       : next direction
     *************************************************************************/
	public Vector<RoutingAlgo.DIRECTION> NegativeFirstnextBank(ID current, ID destination, 
			NOC.TOPOLOGY topology, int numRows, int numColums)
	{
		Vector<RoutingAlgo.DIRECTION> choices = new Vector<DIRECTION>();
		int x1,y1,x2,y2;
		x1 = current.getx();
		y1 = current.gety();
		x2 = destination.getx();
		y2 = destination.gety();
		if(y2 < y1 && x2 < x1){
			if(topology == TOPOLOGY.TORUS )
				if(y1-y2 > Math.ceil(numColums / 2)){
					choices.add(DIRECTION.RIGHT);
					return choices;
				}
			choices.add(DIRECTION.LEFT);
			return choices;
		}
		if(y2 > y1 && x2 > x1){
			if(topology == TOPOLOGY.TORUS)
				if(x2-x1 > Math.ceil(numRows / 2)){
					choices.add(DIRECTION.UP);
					return choices;
				}
			choices.add(DIRECTION.DOWN);
			return choices;
		}
		else
			return XYnextBank(current, destination, 
					topology, numRows, numColums);
	}
	public ArrayList<RoutingAlgo.DIRECTION> XYRouting(Vector<Integer> current, Vector<Integer> destination)
	{
		int x1,y1,x2,y2;
		x1 = current.elementAt(0);
		y1 = current.elementAt(1);
		x2 = destination.elementAt(0);
		y2 = destination.elementAt(1);
		ArrayList<RoutingAlgo.DIRECTION> path = new ArrayList<DIRECTION>();
		while(x1 !=x2 && y1 != y2)
		{
			if(current.elementAt(0) < destination.elementAt(0))
			{
				path.add(DIRECTION.DOWN);
				x1++;
			}
			else if(current.elementAt(0) > destination.elementAt(0))
			{
				path.add(DIRECTION.UP);
				x1--;
			}
			else if(current.elementAt(0) == destination.elementAt(0) && current.elementAt(1) < destination.elementAt(1))
			{
				path.add(DIRECTION.RIGHT);
				y1++;
			}
			else if(current.elementAt(0) == destination.elementAt(0) && current.elementAt(1) > destination.elementAt(1))
			{
				path.add(DIRECTION.LEFT);
				y1--;
			}
			//return null;
		}
		return path;
	}	
}