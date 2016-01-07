
package com.runity;

import java.util.ArrayList;
import android.util.Log;

public class Route
{
    String dateTime = "";
    
    double lat = 0;
    
    double lng = 0;
    
    int user_id = 0;
    
    int id = 0;
    
    double dist = 0;
    
    String username;
    
    int userStatus;
    
    ArrayList < Attendance > atts = new ArrayList < Attendance >();
    
    public Route( String dateTime, double lat, double lng, int user_id, int id, double dist, String username, ArrayList < Attendance > atts, int myStatus )
    {
        super();
        this.dateTime = dateTime;
        this.lat = lat;
        this.lng = lng;
        this.user_id = user_id;
        this.id = id;
        this.dist = dist;
        this.username = username;
        Log.d( "kraj", "--" + atts.size() );
        this.atts = atts;
        Log.d( "kraj", "--" + this.atts.size() );
        this.userStatus = myStatus;
    }
    
    public String getDateTime()
    {
        return dateTime;
    }
    
    public String getusername()
    {
        return username;
    }
    
    public double getDist()
    {
        return dist;
    }
    
    public void setDist( double dist )
    {
        this.dist = dist;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername( String username )
    {
        this.username = username;
    }
    
    public ArrayList < Attendance > getAtts()
    {
        return atts;
    }
    
    public void setAtts( ArrayList < Attendance > atts )
    {
        this.atts = atts;
    }
    
    public void setDateTime( String dateTime )
    {
        this.dateTime = dateTime;
    }
    
    public double getLat()
    {
        return lat;
    }
    
    public void setLat( double lat )
    {
        this.lat = lat;
    }
    
    public double getLng()
    {
        return lng;
    }
    
    public void setLng( double lng )
    {
        this.lng = lng;
    }
    
    public int getUser_id()
    {
        return user_id;
    }
    
    public void setUser_id( int user_id )
    {
        this.user_id = user_id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public void setId( int id )
    {
        this.id = id;
    }
    
    public double getdist()
    {
        return dist;
    }
    
    public int getAttsSize()
    {
        int ret = 0;
        for( Attendance at : atts )
        {
            if( at.status == 1 )
                ret++;
        }
        return ret;
    }
    
    public int getUserStatus()
    {
        return userStatus;
    }
    
}
