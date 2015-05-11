// -----------------------------------------------------------------------------
// ---------------------------- Written by Josh Brunner ------------------------
// ----------------------- for CSS 430 HW4/pt.2  Assignment --------------------
// -------------------------- Last modified: 5/28/2014 -------------------------
// --------------------------------- Test4.java --------------------------------
/*
 * PURPOSE OF FILE
 * This files serves to allow the user to determine what kind of increases are
 * provided when disk caching is enabled in an operating system. Essentially, 
 * this file performs user-level tests using read and write methods. After each 
 * test, the arrays of data (writeBlock and readBlock) are compared to ensure 
 * validity between the operations.
 *
 * Upon loading Test4, the user must satisfy two arguments. The first, argv[0], 
 * represents a String object signifying caching enabled/disabled. The second,
 * argv[1], represents what test is to be performed. The user may choose from
 * any of the following four tests:
 *          1 - Random Accesses
 *          2 - Localized Accesses
 *          3 - Mixed Accesses
 *          4 - Adversary Accesses
 *
 * RUNNING
 * In order to run Test4 properly, you must follow the convention from ThreadOS'
 * load screen (-->). Running a localized access test with cache enabled would 
 * look like this: "l Test4 enabled 2"
 *
 * BRIEF NOTE
 * The data written and read within this file is purely for testing purposes.
 * There's nothing significant about the randomly created data. A brief 
 * discussion on the algorithm of each function is noted above the
 * function names.
 *
 * ASSUMPTIONS
 * 1. It is assumed that the user is running this file in ThreadOS's directory. 
 */

import java.util.*;
import java.io.*;

public class Test4 extends Thread {

    private final static int blockSize = 512;
    private final static int adverseSize = 1000;
    private final static int arraySize = 200; 
    private boolean diskCaching = false;				//disk cache defaulted 
    private long write_start;                           //Keep track of writes
    private long write_stop;                            //Keep track of writes
    private long read_start;                            //Keep track of reads
    private long read_stop;                             //Keep track of reads
    private int currTest = 0;                           //Keep track of the test
    private byte[] writeBlock;                          //Instantiate writeBlock
    private byte[] readBlock;                           //Instantiate readBlock
    private Random rando;    
    private String testName = "dummy";         
    private String enableStat = "disabled";   

    // -------------------------------------------------------------------------
    // Constructor 
    /*
     * SUMMARY
     * This is the constructor for Test4. It starts out by initializing some 
     * global variables to be used throughout the program's execution tests. An 
     * important thing to note is that it is within this function that the data 
     * to be contained by writeBlock gets placed within that block using a neat
     * nextBytes() function that I found online.
     */ 
	public Test4(String[] args){ 
		currTest = Integer.parseInt(args[1]);           //Capture desired test
        writeBlock = new byte[blockSize];               //initialize writeBlock
        readBlock = new byte[blockSize];                //initialize readBlock
        rando = new Random();                           //Create a Random obj
        rando.nextBytes(writeBlock);                    //Generate random data
        if(args[0].equals("enabled")){
            diskCaching = true;
            enableStat = "enabled";
        }              
	}

    // -------------------------------------------------------------------------
    // run 
    /*
     * SUMMARY
     * This function is automatically called because this class inherits from 
     * the Thread class. It starts out by flushing out the cache block from
     * any previous data in it. Then, it looks at what test the user has desired
     * to be run and executes the appropriate test's code. 
     */ 
    public void run( ) {
    	SysLib.flush();									//Clean up cache before	
    	switch(currTest){                               //Look at user entry
    		case 1:	 randomAccess(); 	break;          //1 = randomAccess()
    		case 2:  localizedAccess(); break;          //2 = localizedAccess()
    		case 3:  mixedAccess(); 	break;          //3 = mixedAccess()
    		case 4:  adversaryAccess(); break;          //4 = adversaryAccess()
    		default: SysLib.cout("threadOS: Invalid test identifier.\n");   			  			  			  			  			
    	}
        sync();                                         //Sync back with DISK
		SysLib.exit( );                                 //Exit out of thread
    }

    // -------------------------------------------------------------------------
    // randomAccess 
    /*
     * SUMMARY
     * This function is responsible for testing reads and writes to random 
     * locations across the disk. It utilizes a for loop to load up an array 
     * with random locations (up to 512) to be used in random writes and reads. 
     * At the end of this function, a method is used to ensure that all blocks 
     * that were written were read properly and match up. Then, a performance
     * output is displayed.
     */     
    private void randomAccess(){
    	testName = "Random Accesses";                       //Record test name
        int[] randLoc = new int[arraySize];                 //Initialize array
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            randLoc[i] = randomInt(512);                    //Random locations
        }        
        write_start = captureTime();                        //Start WRITE timer
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            write(randLoc[i], writeBlock);                  //Write those blocks
        }
        write_stop = captureTime();                         //End WRITE timer
        read_start = captureTime();                         //Start READ timer
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            read(randLoc[i], readBlock);                    //read those blocks
        }    
        read_stop = captureTime();                          //End READ timer
        validateBlocks();                                   //Validate I/O
        calculatePerformance();                             //Report results
    }

    // -------------------------------------------------------------------------
    // localizedAccess 
    /*
     * SUMMARY
     * This function is responsible for testing localized accesses to the disk
     * or cache depending on what the user has selected in order to get high 
     * cache hits. It uses the preexisting array of byte data and write it to
     * small sections. It then reads this data into another byte array. At the 
     * end of this function, a method is used to ensure that all blocks that
     * were written were read properly and match up. Then, a performance
     * output is displayed.
     */       
    private void localizedAccess(){
    	testName = "Localized Accesses";                    //Record test name
        write_start = captureTime();                        //Start WRITE timer
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            for(int j = 0; j < 10; j++){                    //Loop locally
                write(j, writeBlock);                       //Write those blocks
            }
        }
        write_stop = captureTime();                         //End WRITE timer
        read_start = captureTime();                         //Start READ timer
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            for(int j = 0; j < 10; j++){                    //Loop locally
                read(j, readBlock);                         //Read those blocks
            }
        }        
        read_stop = captureTime();                          //End READ timer
        validateBlocks();                                   //Validate I/O
        calculatePerformance();                             //Report results
    }

    // -------------------------------------------------------------------------
    // mixedAccess 
    /*
     * SUMMARY
     * This function is responsible for testing operational tests between 
     * localized and random accesses. The distribution for this is 90% of the 
     * total disk operations should be localized accesses and 10% should be
     * random accesses. At the end of this function, a method is used to 
     * ensure that all blocks that were written were read properly and match
     * up. Then, a performance output is displayed.
     */      
    private void mixedAccess(){
    	testName = "Mixed Accesses";                        //Record test name
        int[] mixedValues = new int[arraySize];             //Initialize array
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            if(randomInt(10) < 9){
                mixedValues[i] = randomInt(10);             //Localized Accesses
            } else {            
                mixedValues[i] = randomInt(512);            //Random Accesses
            }            
        }
        write_start = captureTime();                        //Start WRITE timer
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            write(mixedValues[i], writeBlock);              //Write those blocks
        }
        write_stop = captureTime();                         //End WRITE timer
        read_start = captureTime();                         //Start READ timer
        for(int i = 0; i < arraySize; i++){                 //Loop 200 times
            read(mixedValues[i], readBlock);                //Read those blocks
        }    
        read_stop = captureTime();                          //End READ timer
        validateBlocks();                                   //Validate I/O
    	calculatePerformance();                             //Report results
    }

    // -------------------------------------------------------------------------
    // adversaryAccess 
    /*
     * SUMMARY
     * This function is responsible for testing adversary accesses by generating
     * disk accesses that do not make good use of the disk cache at all. In 
     * fact, my testing has shown that this function actually performs worse 
     * when cache is enabled. At the end of this function, a method is used to 
     * ensure that all blocks that were written were read properly and match
     * up. Then, a performance output is displayed.
     */      
    private void adversaryAccess(){
    	testName = "Adversary Accesses";                    //Record test name
        write_start = captureTime();                        //Start WRITE timer
        for (int i = 0; i < blockSize; i++) {               //Loop 512 times
            write(i, writeBlock);                           //Write all blocks
        }        
        write_stop = captureTime();                         //End WRITE timer
        read_start = captureTime();                         //Start READ timer
        for (int i = 0; i < blockSize; i++) {               //Loop 512 times
            read(i, readBlock);                             //Read all blocks
        }        
        read_stop = captureTime();                          //End READ timer
        validateBlocks();                                   //Validate I/O
        calculatePerformance();                             //Report results
    }

    // -------------------------------------------------------------------------
    // sync, read, and write 
    /*
     * SUMMARY
    * The following set of functions are used to eliminate repetive code 
    * that would check to see if disck caching has been enabled or not. 
    * Basically, if disk caching is enabled, the Cache's functions [sync(), 
    * read(), and write()] will be used. Otherwise, the disk's sync(), 
    * rawwrite(), and rawread() will be used.
     */      
    private void sync(){
        if(diskCaching){ SysLib.csync(); } 
        else { SysLib.sync(); }
    }

    private void write(int blockId, byte[] buffer){
        if(diskCaching){ SysLib.cwrite(blockId, buffer); } 
        else { SysLib.rawwrite(blockId, buffer); }        
    }

    private void read(int blockId, byte[] buffer){
        if(diskCaching){ SysLib.cread(blockId, buffer); } 
        else { SysLib.rawread(blockId, buffer); }
    }

    // -------------------------------------------------------------------------
    // randomInt 
    /*
     * SUMMARY
     * This function does nothing more than return a random int rounded to 
     * the desired sized as per determined by maxSize.
     */  
    private int randomInt(int maxSize){
        return (Math.abs(rando.nextInt()) % maxSize);
    }

    // -------------------------------------------------------------------------
    // validateBlocks 
    /*
     * SUMMARY
     * This function is responsible for running over each byte array and
     * verifying that the contents within each array match up. In the event 
     * of data not matching up, the error statement will be thrown to alert 
     * the user of the problem.
     */      
    private void validateBlocks(){
        if(!(Arrays.equals(readBlock, writeBlock))){
            SysLib.cout("threadOS: readBlock and writeBlock are not equal.\n");
        } 
    }

    // -------------------------------------------------------------------------
    // calculatePerformance 
    /*
     * SUMMARY
     * This function is responsible for outputing the results performed by a 
     * specific test. An example of what the output looks like can is here:
     * TEST_1: Random Accesses(cache enabled): avg WRITE(35 msec), READ(37 msec)
     */  
    private void calculatePerformance(){
        SysLib.cout("TEST_" + currTest + ": " + testName);
        SysLib.cout("(cache " + enableStat + "): ");
        SysLib.cout("avg WRITE(" + averageWrite() + " msec)");
        SysLib.cout(", READ(" + averageRead() + " msec)\n");
    }

    // -------------------------------------------------------------------------
    // averageWrite, averageRead, and captureTime 
    /*
     * SUMMARY
     * The functions are simply in place to clean up repetitive/confusing code. 
     */  
    private long averageWrite(){ return ((write_stop-write_start)/arraySize); }
    private long averageRead(){  return ((read_stop-read_start)/arraySize); }    
    private long captureTime(){  return (new Date().getTime()); }
}
