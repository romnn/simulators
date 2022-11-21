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

/*
 * represents a circular buffer of type E
 * circular buffer is implemented as a linked list
 * starts of with size bufferSize, specified in constructor
 * if isGrowable is set, then upon exhaustion of the buffer, new objects are created and added; bufferSize is duly incremented
 * (idea is to support a higher level pool class)
 */

public class GenericCircularBuffer<E> {
	
	@SuppressWarnings("rawtypes")
	Class type;
	Element<E> head;
	Element<E> tail;
	int minBufferSize;
	int maxBufferSize;
	int currentMaxBufferSize; // ensures that we do not return more objects than we gave out
	boolean isGrowable;
	int currentSize;
		
	@SuppressWarnings("rawtypes")
	public GenericCircularBuffer(Class E, int minBufferSize, int maxBufferSize,
			boolean isGrowable)
	{
		this.type = E;
		this.minBufferSize = minBufferSize;
		this.maxBufferSize = maxBufferSize;
		this.currentMaxBufferSize = minBufferSize;
		this.currentSize = minBufferSize;
		
		tail = new Element<E>(E, null);
		
		Element<E> temp = tail;
		for(int i = 0; i < minBufferSize - 1; i++)
		{
			temp = new Element<E>(E, temp);
		}
		
		head = temp;
		tail.next = head;
		
		this.isGrowable = isGrowable;
	}
	
	public boolean append(E newObject)
	{
		if(isFull())
		{
			return false;
		}
		
		tail = tail.next;
		tail.object = newObject;
		
		currentSize++;
		
		return true;
	}
	
	public E removeObjectAtHead()
	{
		if(isEmpty() && !isGrowable)
		{
			return null;
		}
		
		else if(!isEmpty())
		{
			E toBeReturned = head.object;
			head = head.next;
			
			currentSize--;
			
			return toBeReturned;
		}
		
		else
		{

			// When we have to increment by dynamic number of elements
			Element<E> temp = head.next;
			int numElementsAdded = (int)(0.2*minBufferSize); // 0.2 -> 20% 
			
			if((currentMaxBufferSize+numElementsAdded) > maxBufferSize) {
				misc.Error.showErrorAndExit("pool overflow !!");
			}
			
			for(int i = 0; i < numElementsAdded ; i++) 
			{
				temp = new Element<E>(this.type, temp);
			}
			
			head.next=temp;
			
			currentMaxBufferSize += numElementsAdded;
			currentSize += numElementsAdded;
			
			return removeObjectAtHead();
		}
	}
	
	public boolean isFull()
	{
		if(currentSize == currentMaxBufferSize)
		{
			return true;
		}
		return false;
	}
	
	public boolean isEmpty()
	{
		if(currentSize == 2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int size()
	{
		return currentSize;
	}

	public int getPoolCapacity() {
		return currentMaxBufferSize;
	}
}

@SuppressWarnings("unchecked")
class Element<E> {
	
	E object;
	Element<E> next;
	
	@SuppressWarnings("rawtypes")
	Element(Class E, Element<E> next)
	{
		try {
			object = (E) E.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		this.next = next;
	}
}