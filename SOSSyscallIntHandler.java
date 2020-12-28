/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

15/17 SOSSyscallIntHandler.java
*/

import java.util.*;

class SOSSyscallIntHandler implements SIMIntHandler
{//Arbitrary System Call Numbers
  static final int CreateProcessSystemCall =       1;
  static final int ExitProcessSystemCall =         2;
  static final int CreateMessageQueueSystemCall =  3;
  static final int SendMessageSystemCall =         4;
  static final int ReceiveMessageSystemCall =      5;
  static final int DiskReadSystemCall =            6;
  static final int DiskWriteSystemCall =           7;
  
  //Interrupt Handler
  public void HandleInterrupt(int addr)
  {
    int cur_proc = SIM.sosData.current_process;
    SOSProcessManager.SaveProcessState(cur_proc);
    SIM.sosData.pd[cur_proc].timeLeft = SIM.hw.SetTimer(0);
    SIM.sosData.pd[cur_proc].state = SOSProcessManager.Ready;
		
    int base_reg = SIM.sosData.pd[cur_proc].base_register;
    int system_call_number = SIM.hw.GetCellUnmappedAsInt(addr + 1 + base_reg);
		
    int block_number, to_q, from_q, disk_block, return_code;
    int buffer;
    int user_msg;
    int msg_no;

    switch(system_call_number)
    {
			case CreateProcessSystemCall:
         int app_number = SIM.hw.GetCellUnmappedAsInt(addr + 2 + base_reg);
         SIM.Trace(SIM.TraceSOSSyscall, "Create system call: pid=" + cur_proc + ", application number=" + app_number);
			
         int arg = SIM.hw.GetCellUnmappedAsInt(addr + 3 + base_reg);
         int ret = SIM.sosData.processManager.CreateProcessSysProc(app_number, arg);
         SIM.hw.SetCellUnmapped(addr + base_reg, ret);
         break;
			
         case ExitProcessSystemCall:
         return_code = SIM.hw.GetCellUnmappedAsInt(addr + base_reg);
         SIM.Trace(SIM.TraceSOSSyscall, "Exit system call -- pid=" + cur_proc + ", return code=" + return_code );
         SIM.sosData.pd[cur_proc].slotAllocated = false;
         SIM.sosData.pd[cur_proc].state = SOSProcessManager.Blocked;
         break;
			
         case CreateMessageQueueSystemCall:
         int i;
         for(i = 0; i < SOSProcessManager.NumberOfMessageQueues; ++i)
         {
	         if(!SIM.sosData.message_queue_allocated[i])
            {
               break;
            }
         }
      
      if(i >= SOSProcessManager.NumberOfMessageQueues)
      {
	      SIM.hw.SetCellUnmapped(addr + base_reg, -1);
	      break;
      }
      
      SIM.Trace(SIM.TraceSOSSyscall, "Create message queue system call: pid=" + cur_proc + ", mqid=" + i );
      SIM.sosData.message_queue_allocated[i] = true;
      SIM.sosData.message_queue[i] = new Vector();
      SIM.sosData.wait_queue[i] = new Vector();
      SIM.hw.SetCellUnmapped(addr + base_reg, i);
      break;
			
      case SendMessageSystemCall:
      user_msg = SIM.hw.GetCellUnmappedAsInt(addr + 2 + base_reg);
      user_msg += base_reg;
      to_q = SIM.hw.GetCellUnmappedAsInt(addr + 3 + base_reg);
      SIM.Trace(SIM.TraceSOSSyscall, "Send message system call: pid=" + cur_proc + ", mqid=" + to_q + ", user message address=" + user_msg );
			
      if(!SIM.sosData.message_queue_allocated[to_q])
      {
         SIM.hw.SetCellUnmapped(addr + base_reg, -1);
	      break;
      }
      
      msg_no = GetMessageBuffer();

      if(msg_no == -1)
      {
         SIM.hw.SetCellUnmapped(addr + base_reg, -2);
	      break;
      }

      MemoryCopy(user_msg, SIM.MessageBufferArea + SOSProcessManager.MessageSize*msg_no, SOSProcessManager.MessageSize);
      Vector wq = SIM.sosData.wait_queue[to_q];
      
      if(!wq.isEmpty())
      {
	      Object oitem = wq.firstElement();
	      wq.removeElementAt(0);
	      SOSWaitQueueItem wqitem = (SOSWaitQueueItem)oitem;
	      TransferMessage( msg_no, wqitem.buffer_address );
	      SIM.sosData.pd[wqitem.pid].state = SOSProcessManager.Ready;
      }
      
      else
      {
	      SIM.sosData.message_queue[to_q].addElement(new Integer(msg_no));
      }
      
      SIM.hw.SetCellUnmapped(addr + base_reg, 0);
      break;
			
      case ReceiveMessageSystemCall:
      user_msg = SIM.hw.GetCellUnmappedAsInt(addr + 2 + base_reg);
      user_msg += base_reg;
      from_q = SIM.hw.GetCellUnmappedAsInt(addr + 3 + base_reg);
      SIM.Trace(SIM.TraceSOSSyscall,"Receive message system call, pid=" + cur_proc + ", mqid=" + from_q + ", user msg addr=" + user_msg );
			
      if(!SIM.sosData.message_queue_allocated[from_q])
      {
         SIM.hw.SetCellUnmapped(addr + base_reg, -1);
	      break;
      }
      
      if(SIM.sosData.message_queue[from_q].isEmpty())
      {
	      SIM.sosData.pd[SIM.sosData.current_process].state = SOSProcessManager.Blocked;
	      SOSWaitQueueItem item = new SOSWaitQueueItem();
	      item.pid = SIM.sosData.current_process;
	      item.buffer_address = user_msg;
	      SIM.sosData.wait_queue[from_q].addElement(item);
      } 
      
      else
      {
	      msg_no = ((Integer)
		   SIM.sosData.message_queue[from_q].
		   firstElement()).intValue();
	      SIM.sosData.message_queue[from_q].removeElementAt(0);
	      TransferMessage(msg_no, user_msg);
      }
      
      SIM.hw.SetCellUnmapped(addr + base_reg, 0);
      break;
			
      case DiskReadSystemCall:
      case DiskWriteSystemCall:
      buffer = SIM.hw.GetCellUnmappedAsInt(addr + 2 + base_reg);
      buffer += base_reg;
      disk_block = SIM.hw.GetCellUnmappedAsInt(addr + 3 + base_reg);
      SIM.Trace(SIM.TraceSOSSyscall, "Disk IO system call: pid=" + cur_proc + ", block=" + disk_block + ", user buffer address=" + buffer );
      SIM.sosData.diskDriver.DiskIO(system_call_number, disk_block, buffer);
      SIM.hw.SetCellUnmapped(addr + base_reg, 0);
      break;
    }
    SIM.sosData.processManager.Dispatcher();
  }

  //Send message to trhe reciever process and free the message buffer
  private void TransferMessage(int msg_no, int user_msg)
  {
    MemoryCopy(SIM.MessageBufferArea + SOSProcessManager.MessageSize*msg_no, user_msg, SOSProcessManager.MessageSize );
    FreeMessageBuffer( msg_no );
  }
  
  //allocate message buffers
  private int GetMessageBuffer()
  {
    int msg_no = SIM.sosData.free_message_buffer;
    if(msg_no != -1)
    {
      SIM.sosData.free_message_buffer = SIM.hw.GetCellUnmappedAsInt(SIM.MessageBufferArea + SOSProcessManager.MessageSize*msg_no);
    }
    return msg_no;
  }
	
  private void FreeMessageBuffer(int msg_no)
  {
    SIM.hw.SetCellUnmapped(SIM.MessageBufferArea + SOSProcessManager.MessageSize*msg_no, new Integer(SIM.sosData.free_message_buffer));
    SIM.sosData.free_message_buffer = msg_no;
  }
	
	//Thread Simulation
  static public void MemoryCopy(int from, int to, int len)
  {
    SIM.Trace(SIM.TraceSOSSyscall,"MemoryCopy from=" + from + ", to=" + to + ", len=" + len);
    for(int i = 0; i < len; ++i)
    {
      SIM.hw.SetCellUnmapped(to+i, SIM.hw.GetCellUnmapped(from+i));
    }
  }
}
