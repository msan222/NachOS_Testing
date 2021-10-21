package nachos.threads;
import nachos.machine.*;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
//I chose these because they're familiar for me to work with


/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new ThreadPriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;
	else
		setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	else 
		setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	
    	if (thread.schedulingState == null)
    		thread.schedulingState = new ThreadState(thread);

    	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class ThreadPriorityQueue extends ThreadQueue { //making our subclass of ThreadQueue
	
    	ThreadPriorityQueue(boolean transferPriority) {
	    this.donationController = new Donater(queue);
	    this.transferPriority = transferPriority;
    	}

    	public void waitForAccess(KThread thread) {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		Lib.assertTrue(!threadStates.containsKey(getThreadState(thread)));
	    
    		threadStates.put(getThreadState(thread), new ThreadTie(getThreadState(thread)));
    		queue.add(threadStates.get(getThreadState(thread)));

    		getThreadState(thread).waitForAccess(this);

    		if(transferPriority)
            donationController.transferPriority(getThreadState(thread));
    	}

    	public void acquire(KThread thread) {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		Lib.assertTrue(!threadStates.containsKey(getThreadState(thread)));
    		getThreadState(thread).acquire(this);
	    
    		if(transferPriority)
    			donationController.findTarget(getThreadState(thread));
    	}

    	public KThread nextThread() {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		// implement me
    		if(queue.isEmpty())
    			return null;
    		ThreadTie tying = queue.poll();
    		threadStates.remove(tying.threadsstate);
    		if(transferPriority)
    			donationController.resetMaximumPriority(tying.threadsstate);
    		acquire(tying.threadsstate.thread);
    		return tying.threadsstate.thread;
    	}

    	public void updateThreadState(ThreadState s) {
    		if(threadStates.containsKey(s)) {
    			ThreadTie tying = threadStates.get(s);
    			queue.remove(tying);
    			queue.add(tying);
    		}
    	}
	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
    	protected ThreadState pickNextThread() {
    		return queue.peek().threadsstate;
    	}
	
    	public void print() {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		// implement me (if you want)

    	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	
	public boolean transferPriority;
	public HashMap<ThreadState, ThreadTie> threadStates = new HashMap<ThreadState, ThreadTie>();
    public java.util.PriorityQueue<ThreadTie> queue = new java.util.PriorityQueue<ThreadTie>();
    public Donater donationController;
	
	

    protected class ThreadTie implements Comparable{

    	public ThreadState threadsstate;
        public long timelngth;
        
        public int compareTo(Object x) {
            Lib.assertTrue(x instanceof ThreadTie);
            ThreadTie s = (ThreadTie) x;
            return (threadsstate.getEffectivePriority() == s.threadsstate.getEffectivePriority()) ? (int)(timelngth - s.timelngth) : (s.threadsstate.getEffectivePriority() - threadsstate.getEffectivePriority());
        }
        
        public ThreadTie(ThreadState s) {
            threadsstate = s;
            timelngth = Machine.timer().getTime();
        }

    }
} // end of PriorityQueue
    
    protected class Donater{

        public Donater(java.util.PriorityQueue<ThreadPriorityQueue.ThreadTie> queue){ //here we implement priority inversion by switching the high and the low
        	this.queue = queue;
        	this.maximumPriority = priorityMinimum;
        	this.target = null;
        }

        public void findTarget(ThreadState t) {
            if(target != null)
                target.retractDonatedPriority(this);
            target = t;
            target.donatePriority(this, maximumPriority);
        }

        public void resetMaximumPriority(ThreadState t) {
            if(t.getEffectivePriority() == maximumPriority) {
                maximumPriority = priorityMinimum;
                for(ThreadPriorityQueue.ThreadTie tying : queue)
                    maximumPriority = Math.max(maximumPriority, tying.threadsstate.getEffectivePriority());
            }
        }

        public void transferPriority(ThreadState t) {
            maximumPriority = Math.max(t.getEffectivePriority(), maximumPriority);
            if(target != null && maximumPriority > target.getEffectivePriority()) {
                target.donatePriority(this, maximumPriority);
            }
        }
        
        protected java.util.PriorityQueue<ThreadPriorityQueue.ThreadTie> queue;
        protected ThreadState target;
        protected int maximumPriority;
        
    }
    
    
    
    
    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
    public ThreadState() {
    	
    }
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    return effectivePriority.getEffectivePriority();
	}
	
	public void donatePriority(Donater x, int donation) {
		effectivePriority.donate(x, donation);
        updateP();
	}
	
	public void retractDonatedPriority(Donater x) {
        effectivePriority.remover(x);
        updateP();
    }

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;
	    
	    this.priority = priority;
	    
	    // implement me
	    effectivePriority.setPriority(priority);
	    updateP();
	}
	
	public void updateP() {
        for(ThreadPriorityQueue x : parents) {
            x.updateThreadState(this);
        }
    }
	
	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	threadPriorityQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(ThreadPriorityQueue waitQueue) {
	    parents.add(waitQueue);
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(ThreadPriorityQueue waitQueue) {
	    parents.remove(waitQueue);
	}	

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	protected HashSet<ThreadPriorityQueue> parents = new HashSet<ThreadPriorityQueue>(); //I could use another data type but I'm familiar with this and like it
	protected PriorityController effectivePriority = new PriorityController(0);
	
	protected class PriorityController {
        PriorityController(int priority) {
            this.priority = priority;
            this.max_donation = 0;
        }
        
        void donate(Donater q, int priority) {
            this.donations.put(q, priority);
            max_donation = Math.max(max_donation, priority);
        }
        
        void remover(Donater x) {
            if(donations.containsKey(x)) {
                int tracker = donations.get(x);
                donations.remove(x);
                if(max_donation == tracker) {
                    max_donation = 0;
                    for(Map.Entry<Donater, Integer> e : donations.entrySet()) {
                        max_donation = Math.max(max_donation, e.getValue());
                    }
                }
            }
        }

        void setPriority(int priority) {
            this.priority = priority;
        }

        int getEffectivePriority() {
            return Math.max(priority, max_donation);
        }

        protected int priority, max_donation;
        protected HashMap<PriorityScheduler.Donater, Integer> donations = new HashMap<Donater, Integer>();
    }
	
    }
}
