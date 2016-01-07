
package com.runity;

public class Attendance
{
    int id;
    
    String username;
    
    int status;
    
    public Attendance( int id, String username, int status )
    {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    public int getId()
    {
        return id;
    }

    public String username()
    {
        return username;
    }

    public int getStatus()
    {
        return status;
    }
}
