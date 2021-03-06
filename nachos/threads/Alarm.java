package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	
	public class Thread {
		private KThread thread;
		private long time;
		
		public Thread(KThread thread, long time) {
			this.thread = thread;
			this.time = time;
		}
		
		public long getTime() {
			return time;
		}
		
	}
	
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
	
		sleepingThreads = new ArrayList<Thread>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	KThread.yield();
		int i = sleepingThreads.size() - 1;
		boolean intStatus = Machine.interrupt().disable();
		while(!sleepingThreads.isEmpty() && 
			sleepingThreads.get(i).getTime() <= Machine.timer().getTime() &&
			i > -1) {
		
			sleepingThreads.get(i).thread.ready();
			sleepingThreads.remove(i);
			i--;
		}
		Machine.interrupt().restore(intStatus);
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
    	long wakeTime = Machine.timer().getTime() + x;
    	boolean intStatus = Machine.interrupt().disable();
    	Thread threadToAppend = new Thread(KThread.currentThread(), wakeTime);
    	sleepingThreads.add(threadToAppend);
    	sortThreads();
	
    	KThread.sleep();
	
    	Machine.interrupt().restore(intStatus);
    }
    
    
    //Added helper function to sort the list of sleeping threads
    public void sortThreads() {
    	long one, two;
    	for (int i = 0; i < sleepingThreads.size() - 2; i++) {
    		one = sleepingThreads.get(i).getTime();
    		two = sleepingThreads.get(i + 1).getTime();
    		
    		if (one < two) 
    			Collections.swap(sleepingThreads, i, i+1);
    		
    	}
    }
    
    
    ArrayList<Thread> sleepingThreads;
}
