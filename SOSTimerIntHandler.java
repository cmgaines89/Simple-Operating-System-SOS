/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

16/17 SOSTimerIntHandler.java
Manages Timer
*/

class SOSTimerIntHandler implements SIMIntHandler
{
  public void HandleInterrupt(int arg)
  {//arguments are ignored for timer interrupts
    int cur_proc = SIM.sosData.current_process;
    SIM.Trace(SIM.TraceSOSPM, "Timer Interupt -- current process=" + cur_proc);
    SOSProcessManager.SaveProcessState(cur_proc);

    SIM.Trace(SIM.TraceSOSPM, "Suspending pid " + cur_proc + " : " + SIM.hw.ProcessThread[cur_proc].toString());
    //Suspend the current process
    SIM.hw.ProcessThread[cur_proc].suspend();
    SIM.sosData.pd[cur_proc].timeLeft = 0;
    SIM.sosData.pd[cur_proc].state = SOSProcessManager.Ready;
    //Run another process
    SIM.sosData.processManager.Dispatcher();
  }
}
