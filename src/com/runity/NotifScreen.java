
package com.runity;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class NotifScreen extends Activity implements OnItemClickListener
{
    
    ListView table;
    
    ArrayList < String > allNotifStrings;
    
    ArrayList < Notif > allNotifs;
    
    
    // Progress Dialog
    private ProgressDialog pDialog3;
    
    private ProgressDialog pDialog2;
    
    private ProgressDialog pDialog1;
    
    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();
    
    // Notifs SQL URL
    private static String url_get_notifs = "http://" + GV.localIP + "/android_connect/routes/get_notifs.php";
    
    String userUsername;
    
    int userPid;
    
    NotifScreen mContext;
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_notif_screen );
        
        
        userUsername = CurrentUserHandler.username;
        userPid = CurrentUserHandler.pid;
        
        table = ( ListView ) findViewById( R.id.notifs_listView_table );
        
        mContext = this;
        
        // Populate with notifss
        new PopulateList().execute();
    }
    
    
    class Notif
    {
        private int status;
        
        private int pid;
        
        private int rid;
        
        private String datetime;
        
        private String username;
        
        public Notif( int pid, int rid, int status, String datetime, String username )
        {
            this.pid = pid;
            this.rid = rid;
            this.status = status;
            this.datetime = datetime;
            this.username = username;
        }
        
        public String getString()
        {
            String ret = "";
            if( status == 2 )
            {
                ret = "Korisnik " + username + " bi \u017Eeleo da Vam se pridru\u017Ei u tr\u010Danju zakazanom za " + datetime;
            }
            else if( status == 3 )
            {
                ret = "Korisnik " + username + " je odustao od tr\u010Danja zakazanog za " + datetime;
            }
            return ret;
        }
        
        public int getStatus()
        {
            return status;
        }
        
        public String getUsername()
        {
            return username;
        }
        
        public int getRid()
        {
            return rid;
        }
        
        public int getPid()
        {
            return pid;
        }
    }
    
    class PopulateList extends AsyncTask < String, String, String >
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog1 = new ProgressDialog( NotifScreen.this );
            pDialog1.setMessage( "Ucitavanje..." );
            pDialog1.setIndeterminate( false );
            pDialog1.setCancelable( false );
            pDialog1.show();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            allNotifStrings = new ArrayList < String >();
            allNotifs = new ArrayList < Notif >();
            
            int success, valid;
            try
            {
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "pid", Integer.toString( userPid ) ) );
                JSONObject json = jsonParser.makeHttpRequest( url_get_notifs, "GET", params1 );
                success = json.getInt( "success" );
                valid = json.getInt( "valid" );
                if( success == 1 )
                {
                    // Is good
                    if( valid == 1 )
                    {
                        // There are notifs
                        JSONArray jAllNotifs = json.getJSONArray( "items" );
                        
                        // Loop through all
                        for( int i = 0; i < jAllNotifs.length(); i++ )
                        {
                            JSONObject c = jAllNotifs.getJSONObject( i );
                            
                            int status = c.getInt( "status" );
                            int pid = c.getInt( "pid" );
                            int rid = c.getInt( "rid" );
                            String datetime = c.getString( "datetime" );
                            String username = c.getString( "username" );
                            Notif n = new Notif( pid, rid, status, datetime, username );
                            String s = n.getString();
                            allNotifStrings.add( s );
                            allNotifs.add( n );
                        }
                        
                    }
                    else
                    {
                        // No notifs, poor social skills :/
                        allNotifStrings.add( "Nema obave\u0161tenja." );
                    }
                }
                else
                {
                    // fail
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
            return null;
        }
        
        protected void onPostExecute( String file_url )
        {
            
            table.setAdapter( new ArrayAdapter < String >( mContext, android.R.layout.simple_list_item_1, allNotifStrings ) );
            table.setOnItemClickListener( mContext );
            
            if( ( pDialog1 != null ) && pDialog1.isShowing() )
            {
                pDialog1.dismiss();
            }
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if( ( pDialog3 != null ) && pDialog3.isShowing() )
            pDialog3.dismiss();
        pDialog3 = null;
        
        if( ( pDialog1 != null ) && pDialog1.isShowing() )
            pDialog1.dismiss();
        pDialog1 = null;
        
        if( ( pDialog2 != null ) && pDialog2.isShowing() )
            pDialog2.dismiss();
        pDialog2 = null;
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        if( ( pDialog3 != null ) && pDialog3.isShowing() )
            pDialog3.dismiss();
        pDialog3 = null;
        
        if( ( pDialog1 != null ) && pDialog1.isShowing() )
            pDialog1.dismiss();
        pDialog1 = null;
        
        if( ( pDialog2 != null ) && pDialog2.isShowing() )
            pDialog2.dismiss();
        pDialog2 = null;
    }
    
    
    @Override
    public void onItemClick( AdapterView < ? > parent, View view, int position, long id )
    {
        if( allNotifStrings.get( 0 ).equals( "Nema obave\u0161tenja." ) )
            return;
        
        final Notif n = allNotifs.get( position );
        if( n.getStatus() == 2 )
        {
            
            AlertDialog alertDialog = new AlertDialog.Builder( this ).create();
            alertDialog.setTitle( "Potvrda" );
            alertDialog.setMessage( "Da li \u017Eelite da korisnik " + n.getUsername() + " tr\u010Di sa Vama?" );
            alertDialog.setButton( Dialog.BUTTON_POSITIVE, "Da", new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    new ModifyStatus().execute( Integer.toString( n.getRid() ), Integer.toString( n.getPid() ), Integer.toString( 1 ) );
                }
            } );
            alertDialog.setButton( Dialog.BUTTON_NEGATIVE, "Ne", new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    new ModifyStatus().execute( Integer.toString( n.getRid() ), Integer.toString( n.getPid() ), Integer.toString( 0 ) );
                }
            } );
            alertDialog.show();
            
        }
        else if( n.getStatus() == 3 )
        {
            AlertDialog alertDialog = new AlertDialog.Builder( this ).create();
            alertDialog.setTitle( "Potvrda" );
            // Request denied, but we're still being polite
            alertDialog.setMessage( "Poruka nije dospela do korisnika." );
            alertDialog.setButton( Dialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    new DeleteAttend().execute( Integer.toString( n.getRid() ), Integer.toString( n.getPid() ) );
                }
            } );
            alertDialog.show();
            new PopulateList().execute();
        }
    }
    
    private static String url_modify_status = "http://" + GV.localIP + "/android_connect/routes/modify_attend_status.php";
    
    private static String url_delete_attend = "http://" + GV.localIP + "/android_connect/routes/delete_attend.php";
    
    class ModifyStatus extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog2 = new ProgressDialog( NotifScreen.this );
            pDialog2.setMessage( "Ucitavanje..." );
            pDialog2.setIndeterminate( false );
            pDialog2.setCancelable( false );
            pDialog2.show();
        }
        
        @Override
        protected String doInBackground( String... args )
        {
            
            // Check for success tag
            int success;
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "rid", args[0] ) );
                params1.add( new BasicNameValuePair( "pid", args[1] ) );
                params1.add( new BasicNameValuePair( "val", args[2] ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_modify_status, "POST", params1 );
                
                success = json.getInt( "success" );
                if( success == 1 )
                {
                    // Is good
                }
                else
                {
                    // Fail
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
            return null;
        }
        
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog2 != null ) && pDialog2.isShowing() )
            {
                pDialog2.dismiss();
            }
            
            new PopulateList().execute();
        }
    }
    
    
    class DeleteAttend extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog3 = new ProgressDialog( NotifScreen.this );
            pDialog3.setMessage( "Loading..." );
            pDialog3.setIndeterminate( false );
            pDialog3.setCancelable( false );
            pDialog3.show();
        }
        
        /**
         * Getting product details in background thread
         * */
        @Override
        protected String doInBackground( String... args )
        {
            
            // Check for success tag
            int success;
            try
            {
                System.out.println( "AAA" );
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "rid", args[0] ) );
                params1.add( new BasicNameValuePair( "pid", args[1] ) );
                
                System.out.println( "AAXA" + params1 );
                // getting product details by making HTTP request
                // Note that product details url will use GET request
                JSONObject json = jsonParser.makeHttpRequest( url_delete_attend, "POST", params1 );
                
                System.out.println( "susha gej" );
                success = json.getInt( "success" );
                if( success == 1 )
                {
                    // GOOD
                    
                }
                else
                {
                    // fail
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
            return null;
        }
        
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog3 != null ) && pDialog3.isShowing() )
            {
                pDialog3.dismiss();
            }
            
            new PopulateList().execute();
        }
    }
}
