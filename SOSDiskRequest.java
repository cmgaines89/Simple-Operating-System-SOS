/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

10/17 SOSDiskRequest.java
Struct for Disk Requests
*/

class SOSDiskRequest
{
  public int command;     //read or write to disk block
  public int disk_block;  //block number
  public int buffer_address; //address
  public int pid;         //process id of request
}

