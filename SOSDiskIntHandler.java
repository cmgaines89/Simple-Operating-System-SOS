/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

9/17 SIMInt.java
SOSDiskIntHandler.java - Disk Interrupt Handler
*/

class SOSDiskIntHandler implements SIMIntHandler
{
  //Disk Intrerupt Handler-called after disk operation completes
  public void HandleInterrupt(int arg)
  {
    int pid;
    SIM.Trace(SIM.TraceSOSDisk, "DiskInterruptHandler called");
		
    int cur_proc = SIM.sosData.current_process;
    if(cur_proc > 0)
    {
      SOSProcessManager.SaveProcessState(cur_proc);
      //cancel time interval and record time
      SIM.sosData.pd[cur_proc].timeLeft = SIM.hw.SetTimer(0);
      SIM.sosData.pd[cur_proc].state = SOSProcessManager.Ready;
      SIM.Trace(SIM.TraceSOSDisk, "Disk Interrupt, current process=" + cur_proc + ", time left=" + SIM.sosData.pd[cur_proc].timeLeft );
    }

    //If disk read transfer blosk into memory
    SOSDiskRequest dr = SIM.sosData.pending_disk_request;
    if(dr != null)
    {
      if(dr.command == SOSSyscallIntHandler.DiskReadSystemCall)
      {
	      SOSSyscallIntHandler.MemoryCopy(SIM.SystemDiskBuffer, dr.buffer_address,SOSDiskDriver.DiskBlockSize);
      }
			
      //process ready for waiting disk
      pid = dr.pid;
      SIM.sosData.pd[pid].state = SOSProcessManager.Ready;
      SIM.sosData.pending_disk_request = null;
    }

    //wake disj
    SIM.sosData.diskDriver.ScheduleDisk();
		
    //call disk dispatcher
    SIM.sosData.processManager.Dispatcher( );
  }
}
