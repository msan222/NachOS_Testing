package nachos.threads;
import nachos.machine.*;

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
	Lib.assertTrue(conditionLock.isHeldByCurrentThread()); //does this thread hold the lock
	conditionLock.release();							   //release this lock
	boolean intStatus = Machine.interrupt().disable();	   //set status var
	waitQueue.waitForAccess(KThread.currentThread());	   //Queue is waiting for access to the new current thread
	KThread.sleep();										// thread relinquishes CPU
	Machine.interrupt().restore(intStatus);				   //restore to the earlier status
	conditionLock.acquire();								//automatically reacquire lock 
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());  //does current thread hold associated lock
	boolean inStatus = Machine.interrupt().disable();       //get the status now
	KThread thread = waitQueue.nextThread();				//choosing the next thread to use now that we're awake
	if(thread != null) {									//make sure that there's nothing in it
		thread.ready();
	}
	Machine.interrupt().restore(inStatus);					//restore our original status
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean inStatus = Machine.interrupt().disable();
	while(true) {											//run through and wake all of the threads
		KThread thread = waitQueue.nextThread();
		if(thread == null)break;
		thread.ready();
	}
	Machine.interrupt().restore(inStatus);
    }
    
    

    private Lock conditionLock;
    private ThreadQueue waitQueue;
}
