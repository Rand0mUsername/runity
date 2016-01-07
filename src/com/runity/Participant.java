
package com.runity;

public class Participant
{
    int pid;
    
    String username;
    
    int time;
    
    public Participant( int pid, String username, int time )
    {
        super();
        this.pid = pid;
        this.username = username;
        this.time = time;
    }
    
    public int getPid()
    {
        return pid;
    }
    
    public void setPid( int pid )
    {
        this.pid = pid;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername( String username )
    {
        this.username = username;
    }
    
    public int getTime()
    {
        return time;
    }
    
    public void setTime( int time )
    {
        this.time = time;
    }
}
