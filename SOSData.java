/*
Corinne Gaines
Joelle Mason
Cymone McDowell

COSC 414 - Simple Operating System (SOS)

7/17 SOSData.java
This class contains all the SOS global data
*/

import java.util.*;

class SOSData
{
  public int current_process;
  public int next_proc;
  public SOSProcessDescriptor[] pd;
  public int TimeQuantum;
  public int initialProcessNumber = 2;
  public int free_message_buffer; 
  public boolean[] message_queue_allocated;
  public Vector[] message_queue;
  public Vector[] wait_queue;

   //Disk Data
  public SOSDiskRequest pending_disk_request = new SOSDiskRequest();
	
  //Disk Requests
  public Vector disk_queue;

  //Handles Objects
  public SOSDiskDriver diskDriver;
  public SOSProcessManager processManager;
}
