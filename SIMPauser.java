/*
Corinne Gaines

COSC 414 - Simple Operating System (SOS)

6/17 SIMPauser.java
If the user choosed to pause the SOS, it will stop the calling thread if the system is paused.
*/

class SIMPauser
{
  private boolean systemIsPaused = false;
  public synchronized void pause()
  {
    systemIsPaused = true;
  }
  
  public synchronized void unpause()
  {
    systemIsPaused = false;
    notifyAll();
  }
  
  public synchronized boolean isPaused()
  {
    return systemIsPaused;
  }
  
  public synchronized void checkIfPaused()
  {
    while(systemIsPaused)
    {
      try
      {
	      wait();
      }
      
      catch (InterruptedException e)
      {}
    }
  }
}
