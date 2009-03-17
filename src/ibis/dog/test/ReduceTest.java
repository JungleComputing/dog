package ibis.dog.test;

import java.util.Arrays;

import jorus.operations.CxRedOpAddDoubleArray;
import jorus.parallel.PxSystem;

public class ReduceTest {

	public static void main(String [] args) { 

		int count = 2000;
		int repeat = 10;
		
		if (args.length != 3) { 
			System.err.println("Usage: ReduceTest <poolname> <poolsize> <arraysize>");
			System.exit(1);
		}
		
		int arraySize = Integer.parseInt(args[2]);
		
		double [] array = new double[arraySize];
	
		Arrays.fill(array, 1.0);
		
		try {
			PxSystem.initParallelSystem(args[0], args[1]);

			final CxRedOpAddDoubleArray op = new CxRedOpAddDoubleArray();
			
			for (int r=0;r<repeat;r++) { 
				
				long start = System.currentTimeMillis();

                                for (int c=0;c<count;c++) { 
                                        PxSystem.reduceArrayToAllOFT(array, op);                                   
                                }
                                
				long end = System.currentTimeMillis();				
			
				System.out.println(count + " reduceArrayToAll took " + (end-start) + " ms.");
				
				PxSystem.printStatistics();
			}
		
			PxSystem.exitParallelSystem();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
