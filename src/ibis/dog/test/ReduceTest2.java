package ibis.dog.test;

import java.util.Arrays;

import jorus.operations.CxRedOpAddByteArray;
import jorus.parallel.PxSystem;

public class ReduceTest2 {

    public static void main(String [] args) { 

        int count = 2000;
        int repeat = 10;

        if (args.length != 3) { 
            System.err.println("Usage: ReduceTest <poolname> <poolsize> <arraysize>");
            System.exit(1);
        }

        int arraySize = Integer.parseInt(args[2]);

        byte [] array = new byte[arraySize];

        Arrays.fill(array, (byte) 1);

        try {
            PxSystem px = PxSystem.init(args[0], args[1]);
            
            final CxRedOpAddByteArray op = new CxRedOpAddByteArray();

            for (int r=0;r<repeat;r++) { 

                long start = System.currentTimeMillis();

                for (int c=0;c<count;c++) { 
                    px.reduceArrayToAll(array, op);                                   
                }

                long end = System.currentTimeMillis();                          

                System.out.println(count + " reduceArrayToAll took " + (end-start) + " ms.");

                px.printStatistics();
            }

            px.exitParallelSystem();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
