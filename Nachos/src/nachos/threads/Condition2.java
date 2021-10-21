package nachos.threads;
import nachos.machine.*;
import java.util.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    
	private Lock conditionLock;
    private ThreadQueue waitQueue;
    private int countVal;
	
	public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());			
	conditionLock.release();
	boolean machstat = Machine.interrupt().disable();
	countVal++;
	waitQueue.waitForAccess(KThread.currentThread());
	conditionLock.acquire();
	KThread.sleep();
	Machine.interrupt().restore(machstat);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean machstat = Machine.interrupt().disable();
	KThread thread = waitQueue.nextThread();				//grabbing that next thread
	if(thread != null) {									//as long as it isn't empty
		countVal--;
		thread.ready();										//now it's awake
	}
	Machine.interrupt().restore(machstat);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean machstat = Machine.interrupt().disable();
	while(true) {
		KThread thread = waitQueue.nextThread();
		if(thread == null)break;						//since we have to keep going to the next thread use a break statement to finish once its empty
		countVal = 0;
		thread.ready();
	}
	Machine.interrupt().restore(machstat);
    }
    
    public int getThreadCount() {
    	return countVal;
    }
}
