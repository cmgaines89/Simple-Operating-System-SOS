/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

12/17 SOSProcessManager.java
Manages Process within the Simple Operating System
*/

import java.util.*;

class SOSProcessManager
{
  //Constants
  static final int MessageSize            =  8;   //8 words=32 bytes
  static final int NumberOfProcesses      =  8;
  static final int NumberOfMessageQueues  =  4;
  static final int NumberOfMessageBuffers = 50;
  static final int TimeQuantum            = 5000;
  
  //All process states
  static final int Ready   = 1;
  static final int Running = 2;
  static final int Blocked = 3;

  //called for every interript and exception to have the state of the interrupted process
  static public void SaveProcessState(int pid)
  {
    if(pid > 0)
    {
      SIM.Trace(SIM.TraceSOSPM, "Save State -- pid=" + pid);
    }
  }
	
  //internal procedure for creating a process, called during system initialization and when CreateProcess system call is made
  int CreateProcessSysProc(int app_number, int arg)
  {//runs process in sequential file on disk
    int pid;

    //find free slot in process table
    for(pid = 1; pid < NumberOfProcesses; ++pid)
    {
      if(!(SIM.sosData.pd[pid].slotAllocated))
	   break;
    }
    
    if(pid >= NumberOfProcesses)
    {//if no free slot found
      SIM.Trace(SIM.TraceAlways, "Create process failed -- no slots" );
      return -1; //error handler
    }
		
    SIM.sosData.pd[pid].slotAllocated = true; //mark the slot as allocated.
		
    //setup memory mapping for process
    SIM.sosData.pd[pid].base_register = pid * 1000;
    SIM.sosData.pd[pid].limit_register = 1000;
		
    SIM.hw.CreateProcess(app_number, arg, pid);
    //processes start out ready
    SIM.sosData.pd[pid].state = Ready;

    return pid;
  }

  //finds a process and runs it
  void Dispatcher()
  {
    SIM.sosData.current_process = SelectProcessToRun();
    RunProcess(SIM.sosData.current_process);
  }
	
  //find the process that needs to be run next
  int SelectProcessToRun()
  {	
    int cur_proc = SIM.sosData.current_process;
    if(cur_proc > 0 && SIM.sosData.pd[cur_proc].slotAllocated && SIM.sosData.pd[cur_proc].state == Ready && SIM.sosData.pd[cur_proc].timeLeft > 0)
   {
      SIM.Trace(SIM.TraceSOSPM, "Run same process again, pid=" + cur_proc + ":" + SIM.hw.ProcessThread[cur_proc].toString() + " for " + SIM.sosData.pd[cur_proc].timeLeft + " ms");
      return cur_proc;
    }

	//start at current value and wrap around, count number of iterations until entire array has been gone through
    for(int i = 1; i < NumberOfProcesses; ++i)
    {
      if(++SIM.sosData.next_proc >= NumberOfProcesses)
	   SIM.sosData.next_proc = 1;
      int next_proc = SIM.sosData.next_proc;
      
      if(SIM.sosData.pd[next_proc].slotAllocated && SIM.sosData.pd[next_proc].state == Ready)
      {//pick up process if ready
	      SIM.sosData.pd[next_proc].timeLeft = SIM.sosData.TimeQuantum;
	      SIM.sosData.pd[next_proc].state = Running;
	      SIM.Trace(SIM.TraceSOSPM, "Run new process, pid=" + next_proc + ":" + SIM.hw.ProcessThread[next_proc].toString() + " for " + SIM.sosData.TimeQuantum + " ms");
	      return next_proc;
      }
    }
    return -1;
  }
	
	//starts user focused process
  void RunProcess(int pid)
  {
    if(pid >= 0)
    {//start process
      SIM.hw.SetTimer(SIM.sosData.pd[pid].timeLeft);
      SIM.Trace(SIM.TraceSOSPM, "Dispatching pid " + pid + " :" + SIM.hw.ProcessThread[pid].toString());
      //set  memory maps
      SIM.hw.MemoryBaseRegister = SIM.sosData.pd[pid].base_register;
      SIM.hw.MemoryLimitRegister = SIM.sosData.pd[pid].limit_register;
      SIM.hw.RunProcess(pid);
    } 
    
    else 
    {
      SIM.Trace(SIM.TraceSOSPM, "No ready processes, idle" );
      SIM.hw.WaitForInterrupt();
    }
  }
}

