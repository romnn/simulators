package main;


import generic.CustomInstructionPool;

public class CustomObjectPool {
	
	private static CustomInstructionPool instructionPool;
	
	
	public static void initCustomPools(int maxApplicationThreads, int staticInstructionPoolSize) {
		
		// Create Pools of Instructions, Operands and AddressCarryingEvents
//		int runTimePoolPerAppThread =  RunnableThread.INSTRUCTION_THRESHOLD;
//		int staticTimePool = staticInstructionPoolSize;
//		
//		// best case -> single threaded application
//		int minInstructionPoolSize = staticTimePool + runTimePoolPerAppThread;
//		int maxInstructionPoolSize = staticTimePool + runTimePoolPerAppThread * maxApplicationThreads * 2;
				
		/* custom pool */
		System.out.println("creating instruction pool..");
	//	setInstructionPool(new CustomInstructionPool(minInstructionPoolSize, maxInstructionPoolSize));
		
	
	}

	public static CustomInstructionPool getInstructionPool() {
		return instructionPool;
	}

	public static void setInstructionPool(CustomInstructionPool instructionPool) {
		CustomObjectPool.instructionPool = instructionPool;
	}

	
}
