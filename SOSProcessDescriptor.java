/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

11/17 SOSProcessDescriptor.java
Records process state
*/

import java.awt.*;

class SOSProcessDescriptor
{
  public boolean slotAllocated;  //Free or used
  public int timeLeft;       //Time left in ms
  public int state;          //status - ready, running, or blocked
  public int base_register;
  public int limit_register;
}

