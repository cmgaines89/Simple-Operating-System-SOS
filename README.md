# Simple-Operating-System-SOS

This Project was written for a school project for my Operating Systems course in Spring of 2019.

A Simple Operating System that provides processes that use threads, a simulation of memory by using an array of objects, a timer which uses a thread and sleep statements, a disk also using thread and sleep statements, and hardware services such as creating processes, running processes, system calls, access to memory, and access to the disk.

The simulation is comprised of a group of files which are all required to run the Operating System. To run the Simple Operating System all files must be compiled into “.class” files. Then you may run the operating system by running the “SIM.java” class which will bring up a GUI interface for the user to interact with the operating system (it is best to run this program as an applet). When running the Operating System there will be several options to choose from:
  
  1.	Choose an application amongst the buttons at the top of the window:
	
    a.	GUI Apps – Starts two GUI counter apps with counters of 5 seconds
    b.	Msg App – Starts two Processes that send a created message to a queue. The sender sends 10 messages then exits. There is no GUI for this process, but message traces will         be generated.
    c.	Disk App – Starts a process that writes to the virtual disk and reads what was written back from the virtual disk. There is no GUI for this process, but message traces           will be generated.
  
  2.	Choose which objects you would like traced during execution. Trace messages will appear in the list boxes within the simulation window:
	
    a.	App – Shows Application trace messages
    b.	Hardware – shows trace messages relating to hardware simulation
    c.	Simulation – Shows trace messages relating to the software simulation
    d.	SystemCall – Shows messages generated by the system call interrupt handler
    e.	ProcessManager – Shows messages generated by the disk subsystem of the SOS
 
 3.	Start the SOS by clicking “START”
 
 4.	All trace messages will go to the list box below the top row of buttons. You may pause and resume the SOS at your leisure with the “Pause SOS” and “Resume SOS” buttons.
 
 5.	You may exit the simulator at any time by clicking the “Exit SOS” button, and closing the window.
 
 6.	The Process box (Proc0) is below the message trace box. This box shows the trace messages divided up based on the current process that is was running when the trace message was created. There is a separate list for each process in the system. You will only be able to view one process box at a time. There can be between 0-4 process boxes       available, which show the current user threads and interrupt times.
