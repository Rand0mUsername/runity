
package com.runity;

import android.content.Context;
import android.content.SharedPreferences;

public final class CurrentUserHandler
{
    static Context mContext;
    
    static int pid;
    
    static String username;
    
    static SharedPreferences spReader;
    
    static SharedPreferences.Editor spEditor;
    
    public static void setContextAndInit( Context context )
    {
        mContext = context;
        // .apls = andruljid pls
        spReader = mContext.getSharedPreferences( "data.apls", 0 );
        spEditor = spReader.edit();
    }
    
    // Set shared preferences
    static void setUserData( int pid, String username )
    {
        CurrentUserHandler.pid = pid;
        CurrentUserHandler.username = username;
        spEditor.putString( "pid", Integer.toString( pid ) );
        spEditor.putString( "username", username );
        spEditor.commit();
    }
    
    static void readFromDatabase()
    {
        CurrentUserHandler.pid = Integer.parseInt( spReader.getString( "pid", "-1" ) );
        CurrentUserHandler.username = spReader.getString( "username", "/" );
    }
}
