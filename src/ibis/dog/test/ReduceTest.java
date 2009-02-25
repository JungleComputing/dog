package ibis.dog.test;

import java.util.Arrays;

import jorus.operations.CxRedOpAddDoubleArray;
import jorus.operations.CxRedOpArray;
import jorus.parallel.PxSystem;

public class ReduceTest {

	private static final void testAlg1(final int count, final double [] array, final CxRedOpArray op)
		throws Exception { 
		
		for (int c=0;c<count;c++) { 
			PxSystem.reduceArrayToAllOFT(array, op);					
		}
	}

	private static final void testAlg2(final int count, final double [] array, final CxRedOpArray op)
		throws Exception { 

		for (int c=0;c<count;c++) { 
			PxSystem.reduceArrayToAllOFT_JASON(array, op);					
		}
	}

	private static final void testAlg3(final int count, final double [] array, final CxRedOpArray op) 
		throws Exception { 

		for (int c=0;c<count;c++) { 
			PxSystem.reduceArrayToAllOFT_JASON2(array, op);					
		}
		
	}
	
	private static final void testAlg4(final int count, final double [] array, final CxRedOpArray op) 
		throws Exception { 

		for (int c=0;c<count;c++) { 
			PxSystem.reduceArrayToAllOFT_JASON3(array, op);					
		}

	}

	
	public static void main(String [] args) { 

		int count = 100;
		int repeat = 10;
		
		if (args.length != 4) { 
			System.err.println("Usage: ReduceTest <poolname> <poolsize> <arraysize> <algorithm>");
			System.exit(1);
		}
		
		int arraySize = Integer.parseInt(args[2]);
		int algorithm = Integer.parseInt(args[3]);
		
		double [] array = new double[arraySize];
	
		Arrays.fill(array, 1.0);
		
		try {
			PxSystem.initParallelSystem(args[0], args[1]);

			final CxRedOpAddDoubleArray op = new CxRedOpAddDoubleArray();
			
			for (int r=0;r<repeat;r++) { 
				
				long start = System.currentTimeMillis();

				switch (algorithm) { 
				case 1: 
					testAlg1(count, array, op);
					break;
				case 2: 
					testAlg2(count, array, op);
					break;
				case 3: 
					testAlg3(count, array, op);
					break;
				case 4: 
					testAlg4(count, array, op);
					break;	
				default: 
					System.err.println("Unknown algorithm: "+ algorithm);
					return;
				}
				
				long end = System.currentTimeMillis();				
			
				System.out.println(count + " reduceArrayToAll took " + (end-start) + " ms.");
	
				//PxSystem.printStatistics();
			}
		
			PxSystem.exitParallelSystem();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
