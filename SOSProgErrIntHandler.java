/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

13/17 SOSErrIntHandler.java
Called when an exception occurs
*/

class SOSProgErrIntHandler implements SIMIntHandler
{
  public void HandleInterrupt(int error_code)
  {
    int cur_proc = SIM.sosData.current_process;

    SIM.Trace(SIM.TraceSOSPM,"Program Error, pid=" +  cur_proc + ", error code is "+ error_code);
    SIM.hw.SetTimer(0); //cancel the timer interval
    SIM.sosData.pd[cur_proc].slotAllocated = false;
    SIM.sosData.processManager.Dispatcher();
  }
}
