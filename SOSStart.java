/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

14/17 SOSProcessManager
System Initialization - Main Program of the SOS - Called only once
*/

import java.util.*;

class SOSStart implements Runnable
{
  public void run()
  {
    SIM.Trace(SIM.TraceSOSStart, "Initialize SOS (Simple Operating System)");
    SIM.sosData.TimeQuantum = SOSProcessManager.TimeQuantum;
		
    //Initialize the interrupt vectors
    SIM.hw.SetCellUnmapped(SIM.SyscallIntVector,
		  (Object)new SOSSyscallIntHandler());
    SIM.hw.SetCellUnmapped(SIM.TimerIntVector,
		  (Object)new SOSTimerIntHandler());
    SIM.hw.SetCellUnmapped(SIM.DiskIntIntVector,
		  (Object)new SOSDiskIntHandler());
    SIM.hw.SetCellUnmapped(SIM.ProgErrIntIntVector,
		  (Object)new SOSProgErrIntHandler());
		
    //Initialize the subsystems
    //InitializeMemorySystem( 0, 128*64 );
    InitializeIOSystem();
    InitializeProcessSystem();
		
    //Create the initial process
    SIM.sosData.processManager.CreateProcessSysProc(SIM.sosData.initialProcessNumber, 0);
		
    //Start processes
    SIM.sosData.processManager.Dispatcher();
    SIM.Trace(SIM.TraceSOSStart, "Suspending the startup thread (for good)");
    Thread.currentThread().suspend();
  }
	
  //Initialize process management subsystem.
  private void InitializeProcessSystem()
  {
    SIM.Trace(SIM.TraceSOSStart, "Initialize Process System");
    int i;
    //Create the necessary arrays
    SIM.sosData.pd = new SOSProcessDescriptor[SOSProcessManager.NumberOfProcesses];
    for(i = 0; i < SOSProcessManager.NumberOfProcesses; ++i)
    {
      SIM.sosData.pd[i] = new SOSProcessDescriptor();
    }
		
    //Process Descriptors - Process 0 is reserved for the system, never dispatch it.
    SIM.sosData.pd[0].slotAllocated = true;
    SIM.sosData.pd[0].state = SOSProcessManager.Blocked;
		
    //Other process slots start free
    for(i = 1; i < SOSProcessManager.NumberOfProcesses; ++i)
      SIM.sosData.pd[i].slotAllocated = false;
		
    //Set up message buffers - linking each one to the next one
    for(i = 0; i < (SOSProcessManager.NumberOfMessageBuffers-1); ++i)
      SIM.hw.SetCellUnmapped(SIM.MessageBufferArea + SOSProcessManager.MessageSize*i, new Integer(i+1));
		
    //Last message buffer has a marker for the end of the list
    SIM.hw.SetCellUnmapped(SIM.MessageBufferArea + SOSProcessManager.MessageSize * (SOSProcessManager.NumberOfMessageBuffers-1),
      new Integer(-1));
    SIM.sosData.free_message_buffer = 0;
		
    SIM.sosData.message_queue_allocated = new boolean[SOSProcessManager.NumberOfMessageQueues];
		
    SIM.sosData.message_queue = new Vector[SOSProcessManager.NumberOfMessageQueues];
		
    SIM.sosData.wait_queue = new Vector[SOSProcessManager.NumberOfMessageQueues];
		
    //All message queues start unallocated
    for(i = 0; i < SOSProcessManager.NumberOfMessageQueues; ++i)
      SIM.sosData.message_queue_allocated[i] = false;
		
    SIM.sosData.current_process = -1;
		
    //Wrap around to process one
    SIM.sosData.next_proc = SOSProcessManager.NumberOfProcesses;

    //Create process manager
    SIM.sosData.processManager = new SOSProcessManager();
  }
	
  //SOS Initialization - I/O
  private void InitializeIOSystem()
  {
    SIM.Trace(SIM.TraceSOSStart, "Initialize IO System");
    SIM.sosData.pending_disk_request = null;
    SIM.sosData.diskDriver = new SOSDiskDriver();
    SIM.sosData.disk_queue = new Vector();
  }
}

