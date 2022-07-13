# NachOS_Testing
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The goal of Nachos is to introduce students to concepts in operating system design and implementation by 
requiring them to implement significant pieces of functionality within the Nachos system.


"NachOS Machine - Nachos simulates a machine that roughly approximates the MIPS architecture. The machine has registers, memory and a cpu. 
The Nachos/MIPS machine is implemented by the Machine object, an instance of which is created when Nachos starts up. 
It contains methods like Run, ReadRegister, WriteRegister, etc. It also defines an interrupt object to handle interrupts. 
Timer and statistics are also implemented in this.

NachOS Threads - In NachOS a thread class has been defined. A thread has an associated state with it which maybe ready, running, blocked or just created. 
The thread object has various methods like PutThreadToSleep, YieldCPU, ThreadFork, ThreadStackAllocate, etc. Each thread runs at a virtual address space.

NachOS UserPrograms - Nachos runs user programs in their own private address space. 
Nachos can run any MIPS binary, assuming that it restricts itself to only making system calls that Nachos understands." 

1 - First, I implemented the wake(), sleep(), and wakeAll() methods using a threadQueue and locks.

2 - In order for KThread to work with my implementation, I implemented another ThreadQueue in the KThread class 
that is compatible with locks and the condition class. 

3 - To complete the Alarm class, I created a WaitingThread class containing a KThread, along with a 
corresponding wakeTime. I used a PriorityQueue of these WaitingThreads so that the timerInterrupt() can 
force a context switch when needed, given that the wakeTime is less than the machine time. 
In the waitUntil() method, I get the current thread, and add the waitTime parameter (x) to the MachineTime 
in order to get the wakeTime of the current thread. It then finishes by creating a WaitingThread, and adds 
that WaitingThread to the PriorityQueue of WaitingThreads.

4 - To complete the Communicator class, I created the speaker and listener condition variables from the
Condition2 class. I implemented these condition variables in the speak() and listen() methods.
Both the speaker and listener cannot be waiting at the same time. For the speak() method, when a thread
is listened to, data (int word) is transferred to the listener. In listen(), when a thread speaks through 
this communicator, word is returned to speak(). 

5 - In order to complete the priority scheduler, I first implemented a new subclass of ThreadQueue named
newThreadQueue that would receive the signal to transfer priorities. In that class, I also implemented 
the methods getPriority(), getEffectivePriority(), setPriority(), increasePriority(), decreasePriority(), 
getThreadState(), and ThreadPriorityQueue() (which contained the following:
A constructor, a method telling the machine to waitForAccess(),  acquire(), nextThread(), 
updateThreadState(), pickNextThread(). And using a tying class that allows for it to compare and act upon the
time, and also a HashMap to keep track of the thread states along with an instance of the class that 
controlled donation). The trickiest part involved making another class, Donator, to control the donation 
properties, with methods to find the maximum priorities, set them, and transfer them. Then the scheduling 
state of the threads was implemented under all the classes in ThreadState which involved using what was 
just defined in the previous class, all using a HashMap to keep track of the donations, and a HashSet to 
keep track of the priorities as inherited by the parents of all threads.






