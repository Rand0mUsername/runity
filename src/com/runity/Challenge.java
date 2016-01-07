
package com.runity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import android.annotation.SuppressLint;
import com.google.android.gms.maps.model.LatLng;

public class Challenge
{
    int cid;
    
    String startDate, endDate;
    
    LatLng startPos;
    
    LatLng endPos;
    
    
    ArrayList < Participant > participants = new ArrayList < Participant >();
    
    
    private SimpleDateFormat dateFormatter = new SimpleDateFormat( "dd-MM-yyyy", Locale.US );
    
    public Challenge( int cid, String startDate, String endDate, LatLng startPos, LatLng endPos )
    {
        super();
        this.cid = cid;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public void addToParticipants( Participant p )
    {
        this.participants.add( p );
    }
    
    public int getCid()
    {
        return cid;
    }
    
    public String getStartDate()
    {
        return startDate;
    }
    
    public String getEndDate()
    {
        return endDate;
    }
    
    public LatLng getStartPosition()
    {
        return startPos;
    }
    
    public LatLng getEndPosition()
    {
        return endPos;
    }
    
    private class TimeComparator implements Comparator < Participant >
    {
        public int compare( Participant a, Participant b )
        {
            return a.time < b.time? -1: a.time == b.time? 0: 1;
        }
    }
    
    public void sortParticipants()
    {
        TimeComparator dc = new TimeComparator();
        Collections.sort( participants, dc );
    }
    
    public static String TimeFormat( int secs )
    {
        String h = Integer.toString( secs / 3600 );
        String m = Integer.toString( ( secs % 3600 ) / 60 );
        String s = Integer.toString( ( secs % 3600 ) % 60 );
        
        if( s.length() < 2 )
            s = "0" + s;
        if( m.length() < 2 )
            m = "0" + m;
        if( h.length() < 2 )
            h = "0" + h;
        
        return h + ":" + m + ":" + s;
    }
    
    public String getFirst()
    {
        if( participants.size() < 2 )
            return "1. N/A";
        
        return String.format( "1. %s %s", participants.get( 1 ).getUsername(), TimeFormat( participants.get( 1 ).getTime() ) );
    }
    
    
    public String getSecond()
    {
        if( participants.size() < 3 )
            return "2. N/A";
        return String.format( "2. %s %s", participants.get( 2 ).getUsername(), TimeFormat( participants.get( 2 ).getTime() ) );
    }
    
    
    public String getThird()
    {
        if( participants.size() < 4 )
            return "3. N/A";
        return String.format( "3. %s %s", participants.get( 3 ).getUsername(), TimeFormat( participants.get( 3 ).getTime() ) );
    }
    
    @SuppressLint( "DefaultLocale" )
    public String getMyPos( int pid )
    {
        for( int i = 1; i < participants.size(); i++ )
        {
            if( participants.get( i ).getPid() == pid )
            {
                if( i <= 3 )
                {
                    return "Ti si u top 3!";
                }
                else
                {
                    return String.format( "%d. %s %s", i, participants.get( i ).getUsername(), TimeFormat( participants.get( i ).getTime() ) );
                }
            }
        }
        return "Nisi učestvovao u ovom izazovu.";
    }
    
    public String daysLeft()
    {
        Date endDateParsed = null;
        try
        {
            endDateParsed = dateFormatter.parse( endDate );
        }
        catch( ParseException e )
        {
            e.printStackTrace();
        }
        Date today = new Date();
        
        int diff = ( ( int ) ( ( endDateParsed.getTime() / ( 24 * 60 * 60 * 1000 ) ) - ( int ) ( today.getTime() / ( 24 * 60 * 60 * 1000 ) ) ) ) + 1;
        if( diff == 0 )
            return "Izazov ističe danas.";
        return "Izazov ističe za " + Integer.toString( diff ) + " dana.";
    }
    
    public void Update( Participant curr )
    {
        
        for( int i = 1; i < participants.size(); i++ )
        {
            if( participants.get( i ).getPid() == curr.getPid() )
            {
                if( participants.get( i ).getTime() > curr.getTime() )
                    participants.get( i ).setTime( curr.getTime() );
                return;
            }
        }
        participants.add( curr );
        
        sortParticipants();        
    }
}
