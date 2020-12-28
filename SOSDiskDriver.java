/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

8/17 SOSDiskDriver.java
Simulated Disk Driver Implementation
*/

import java.util.*;

class SOSDiskDriver
{
  //Disk Setup
  static final int DiskBlockSize     = 128;
  static final int DiskNumber        =   0; //disc zero is the initial disk (1 disc)
  static final int NumberOfDiskPages =  32;
  static final int SizeOfDiskPage    =  64;
	

  //Creates IO request in request queue then wakes the disk scheduler. Called from interrupt Handler
  void DiskIO(int command, int disk_block, int buffer_address)
  {
    SIM.Trace(SIM.TraceSOSDisk, "Queue Disk IO request: block=" + disk_block + ", buffer=" + buffer_address );
		
    //Create a new disk request and fill fields
    int cur_proc = SIM.sosData.current_process;
    SOSDiskRequest req = new SOSDiskRequest();
    req.command = command;
    req.disk_block = disk_block;
    req.buffer_address //absolute address
      = buffer_address + SIM.sosData.pd[cur_proc].base_register;
    req.pid = cur_proc;
		
    //Put address into queue
    SIM.sosData.disk_queue.addElement(req);
    SIM.sosData.pd[cur_proc].state = SOSProcessManager.Blocked;
		
    //Wake up the disk scheduler
    ScheduleDisk();
  }
	
  //Called when the disk is busy
  void ScheduleDisk()
  {
		
    //If the disk is already in use...
    if(DiskBusy())
    {
      return;
    }
		
    //Return if there is no disk service...
    Vector queue = SIM.sosData.disk_queue;
    if(queue.isEmpty())
    {
      return;
    }
		
    //Get first request from queue
    SOSDiskRequest req = (SOSDiskRequest)queue.firstElement();
    queue.removeElementAt(0);
		
    //recall which process is in queue
    SIM.sosData.pending_disk_request = req;
		
    //issue the read or write, with disk interrupt enabled
    if(req.command == SOSSyscallIntHandler.DiskReadSystemCall)
    {
      IssueDiskRead( req.disk_block, DiskBlockSize, 1 );
    } 
    
    else
    {//copy the block from MIPS memory into an OS buffer
      SOSSyscallIntHandler.MemoryCopy(req.buffer_address,SIM.SystemDiskBuffer, DiskBlockSize);
      IssueDiskWrite(req.disk_block, SIM.SystemDiskBuffer, 1);
    }
  }
	
	
  //Returns Disk Status
  boolean DiskBusy()
  {
    int status_reg = SIM.hw.GetCellUnmappedAsInt(SIM.DiskStatusRegister);
    SIM.Trace(SIM.TraceSOSDisk, "DiskBusy, status reg = " + Integer.toBinaryString(status_reg));
    return status_reg != 0;
  }
	
  //Disk Read
  void IssueDiskRead(int block_number, int buffer_address, int enable_disk_interrupt)
  {
    SIM.Trace(SIM.TraceSOSDisk,"Disk Read -- block=" + block_number + ", buffer_address=" + buffer_address );
    SIM.hw.SetCellUnmapped(SIM.DiskAddressRegister, buffer_address);
    SIM.hw.SetCellUnmapped(SIM.DiskControlRegister, (enable_disk_interrupt<<30) + (block_number<<10));
  }
	
  //Disk Write
  void IssueDiskWrite(int block_number, int buffer_address, int enable_disk_interrupt)
  {
    SIM.Trace(SIM.TraceSOSDisk, "Disk Write -- block=" + block_number + ", buffer_address=" + buffer_address);
    SIM.hw.SetCellUnmapped(SIM.DiskAddressRegister, buffer_address);
    SIM.hw.SetCellUnmapped(SIM.DiskControlRegister,(1<<31) + (enable_disk_interrupt<<30) + (block_number<<10));
  }
}
