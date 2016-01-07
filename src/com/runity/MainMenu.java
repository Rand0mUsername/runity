
package com.runity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract.Colors;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.runity.R.color;

@SuppressLint(
{ "ClickableViewAccessibility", "UseSparseArrays" } )
public class MainMenu extends FragmentActivity implements OnTouchListener, OnMarkerClickListener, OnInfoWindowClickListener, OnMapClickListener
{
    static ImageView runityGo;
    
    static boolean focus_mode;
    
    ImageView routesHub;
    
    ImageView logOut;
    
    ImageView focus;
    
    GoogleMap googleMap;
    
    SupportMapFragment fm;
    
    String username;
    
    int pid;
    
    GPSTracker gps;
    
    TextView tw_username;
    
    Typeface nanosecond;
    
    static boolean runMode;
    
    static int cidMode;
    
    static TextView infoTime, infoKcal, infoMeters;
    
    private SimpleDateFormat dateFormatter, dateFormatterSql;
    
    static Context mContext2;
    
    private static final int AVG_MASS = 70; // You can't ask a lady
    
    static Drawable runityRed, runityGray;
    
    ImageView addChallenge;
    
    static Marker endMarker;
    
    static Run currentRun;
    
    int deltaScore;
    
    static Challenge currC;
    
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );
        focus_mode = true;
        mContext2 = this;
        
        username = CurrentUserHandler.username;
        pid = CurrentUserHandler.pid;
        
        runityRed = getResources().getDrawable( R.drawable.runity_red );
        runityGray = getResources().getDrawable( R.drawable.runity_gray );
        
        runMode = false;
        focus = ( ImageView ) findViewById( R.id.main_imageView_center );
        focus.setOnTouchListener( this );
        CurrentUserHandler.setContextAndInit( this );
        
        routesHub = ( ImageView ) findViewById( R.id.main_imageView_routesHub );
        routesHub.setOnTouchListener( this );
        
        addChallenge = ( ImageView ) findViewById( R.id.main_imageView_addChallenge );
        addChallenge.setOnTouchListener( this );
        
        
        infoTime = ( TextView ) findViewById( R.id.main_textView_infoTime );
        infoKcal = ( TextView ) findViewById( R.id.main_textView_infoKcal );
        infoMeters = ( TextView ) findViewById( R.id.main_textView_infoMeters );
        
        runityGo = ( ImageView ) findViewById( R.id.main_imageView_runity );
        runityGo.setOnTouchListener( this );
        
        logOut = ( ImageView ) findViewById( R.id.main_imageView_logout );
        logOut.setOnTouchListener( this );
        
        fm = ( SupportMapFragment ) getSupportFragmentManager().findFragmentById( R.id.main_map );
        googleMap = fm.getMap();
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom( new LatLng(GPSTracker.latitude,GPSTracker.longitude), 16f );
        googleMap.animateCamera( cu );
        gps = new GPSTracker( this, googleMap );
        googleMap.getUiSettings().setZoomControlsEnabled( false );
        googleMap.getUiSettings().setMyLocationButtonEnabled( false );
        googleMap.setOnMarkerClickListener( this );
        googleMap.setOnInfoWindowClickListener( this );
        googleMap.setOnMapClickListener( this );
        GPSTracker.newMarker = googleMap.addMarker( new MarkerOptions().position( new LatLng( GPSTracker.latitude - GPSTracker.CIRCLE_OFFSET, GPSTracker.longitude ) ).icon( BitmapDescriptorFactory.fromResource( R.drawable.me2 ) ) );
        
        nanosecond = Typeface.createFromAsset( getAssets(), "nsecthck.TTF" );
        infoTime.setTypeface( nanosecond );
        infoKcal.setTypeface( nanosecond );
        infoMeters.setTypeface( nanosecond );
        Display display = getWindowManager().getDefaultDisplay();
        @SuppressWarnings( "unused" )
        double ScrWidth = display.getWidth();
        double ScrHeight = display.getHeight();
        // double resolution = ScrWidth * ScrHeight;
        infoTime.setTextSize( ( int ) ( 0.03 * ScrHeight ) );
        infoKcal.setTextSize( ( int ) ( 0.03 * ScrHeight ) );
        infoMeters.setTextSize( ( int ) ( 0.03 * ScrHeight ) );
        infoTime.setTextColor( Color.parseColor( "#7d7d7d" ) );
        infoKcal.setTextColor( Color.parseColor( "#7d7d7d" ) );
        infoMeters.setTextColor( Color.parseColor( "#7d7d7d" ) );
        
        dateFormatter = new SimpleDateFormat( "dd-MM-yyyy", Locale.US );
        dateFormatterSql = new SimpleDateFormat( "yyyy-MM-dd", Locale.US );
        
        
        infoTime.setText( "00:00:00" );
        infoKcal.setText( "0kcal" );
        infoMeters.setText( "0m" );
        
        new DrawChallenges().execute();
    }
    
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        
        System.gc();
        
        gps.stopUsingGPS();
        
        if( ( pDialog != null ) && pDialog.isShowing() )
            pDialog.dismiss();
        pDialog = null;
        
        if( ( pDialog1 != null ) && pDialog1.isShowing() )
            pDialog1.dismiss();
        pDialog1 = null;
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        if( ( pDialog != null ) && pDialog.isShowing() )
            pDialog.dismiss();
        pDialog = null;
        
        
        if( ( pDialog1 != null ) && pDialog1.isShowing() )
            pDialog1.dismiss();
        pDialog1 = null;
    }
    
    Handler handler = new Handler();
    
    final Context mContext = this;
    
    // Tick each second
    Runnable r = new Runnable()
    {
        public void run()
        {
            if( runMode )
            {
                currentRun.timeElapsed++;
                infoTime.setText( Challenge.TimeFormat( ( int ) currentRun.timeElapsed ) );
                handler.postDelayed( r, 1000 );
            }
        }
    };
    
    public void onResume()
    {
        super.onResume();
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom( new LatLng(GPSTracker.latitude,GPSTracker.longitude), 16f );
        googleMap.animateCamera( cu );
        gps = new GPSTracker( mContext, googleMap );
        focus_mode=true;
    }
    
    JSONParser jsonParser = new JSONParser();
    
    private ProgressDialog pDialog, pDialog1;
    
    private static final String TAG_SUCCESS = "success";
    
    private static final String TAG_VALID = "valid";
    
    private static String url_setLoggedOut = "http://" + GV.localIP + "/android_connect/profiles/set_loggedout.php";
    
    private static String url_get_all_challenges = "http://" + GV.localIP + "/android_connect/challenges/get_all_challenges.php";
    
    
    class setLoggedOut extends AsyncTask < String, String, String >
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( MainMenu.this );
            pDialog.setMessage( "Izlazak..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( true );
            pDialog.show();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "username", username ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_setLoggedOut, "POST", params1 );
                
                @SuppressWarnings( "unused" )
                int success = json.getInt( TAG_SUCCESS );
                return null;
            }
            catch( JSONException e )
            {
                e.printStackTrace();
                return null;
            }
        }
        
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
    
    @Override
    public boolean onTouch( View arg0, MotionEvent arg1 )
    {
        if( arg0.getId() == routesHub.getId() )
        {
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                if( cidMode != -1 )
                {
                    cidMode = -2;
                }
                if( runMode )
                {
                    ExitRunMode();
                }
                
                Intent Intent1 = new Intent( this, RoutesHub.class );
                startActivity( Intent1 );
                return true;
            }
        }
        
        if( arg0.getId() == runityGo.getId() )
        {
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                if( runMode )
                {
                    if( cidMode != -1 )
                    {
                        cidMode = -2;
                    }
                    ExitRunMode();
                }
                else
                {
                    EnterRunMode( -1 );
                }
                return true;
            }
            
        }
        
        if( arg0.getId() == logOut.getId() )
        {
            if( arg1.getAction() == MotionEvent.ACTION_DOWN )
            {
                if( cidMode != -1 )
                {
                    cidMode = -2;
                }
                if( runMode )
                {
                    ExitRunMode();
                }
                
                new setLoggedOut().execute();
                CurrentUserHandler.setUserData( -1, "/" );
                
                Intent Intent1 = new Intent( this, LoginScreen.class );
                startActivity( Intent1 );
                finish();
                
                return true;
            }
            
        }
        
        if( arg0.getId() == addChallenge.getId() )
        {
            
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                if( cidMode != -1 )
                {
                    cidMode = -2;
                }
                if( runMode )
                {
                    ExitRunMode();
                }
                
                Intent Intent1 = new Intent( this, AddChallenge.class );
                startActivityForResult( Intent1, 420 );
                
                return true;
            }
        }
        if( arg0.getId() == focus.getId() )
        {
            
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                focus_mode = true;
                System.out.println("a");
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom( new LatLng(GPSTracker.latitude,GPSTracker.longitude), 16f );
                googleMap.animateCamera( cu );
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        
        if( requestCode == 420 )
        {
            new DrawChallenges().execute();
        }
    }
    
    
    Map < Integer, Challenge > allChallengesMap = new HashMap < Integer, Challenge >();
    
    Map < Marker, Integer > allMarkers = new HashMap < Marker, Integer >();
    
    class DrawChallenges extends AsyncTask < String, String, String >
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog1 = new ProgressDialog( MainMenu.this );
            pDialog1.setMessage( "Crtanje izazova..." );
            pDialog1.setIndeterminate( false );
            pDialog1.setCancelable( false );
            pDialog1.show();
        }
        
        
        @Override
        protected String doInBackground( String... params )
        {
            // Check for success tag
            int success, valid;
            try
            {
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                JSONObject json = jsonParser.makeHttpRequest( url_get_all_challenges, "GET", params1 );
                success = json.getInt( TAG_SUCCESS );
                valid = json.getInt( TAG_VALID );
                if( success == 1 && valid == 1 )
                {
                    JSONArray allRows = json.getJSONArray( "rows" ); // JSON Array
                    allChallengesMap.clear();
                    
                    for( int i = 0; i < allRows.length(); i++ )
                    {
                        JSONObject c = allRows.getJSONObject( i );
                        int cid = c.getInt( "cid" );
                        String startDate = c.getString( "startdate" );
                        String endDate = c.getString( "enddate" );
                        try
                        {
                            startDate = dateFormatter.format( dateFormatterSql.parse( startDate ) );
                            endDate = dateFormatter.format( dateFormatterSql.parse( endDate ) );
                        }
                        catch( ParseException e )
                        {
                            e.printStackTrace();
                        }
                        LatLng startLoc = new LatLng( c.getDouble( "startlat" ), c.getDouble( "startlng" ) );
                        LatLng endLoc = new LatLng( c.getDouble( "endlat" ), c.getDouble( "endlng" ) );
                        Participant currParticipant = new Participant( c.getInt( "ppid" ), c.getString( "pusername" ), c.getInt( "ptime" ) );
                        
                        if( !allChallengesMap.containsKey( cid ) )
                        {
                            Challenge currChall = new Challenge( cid, startDate, endDate, startLoc, endLoc );
                            allChallengesMap.put( cid, currChall );
                        }
                        
                        allChallengesMap.get( cid ).addToParticipants( currParticipant );
                    }
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
            for( Challenge currChall : allChallengesMap.values() )
            {
                currChall.sortParticipants();
                
                MarkerOptions markerOptions = new MarkerOptions().position( currChall.getStartPosition() ).title( "Izazov" ).snippet( "Klik za info" ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_c_start ) );
                Marker marker = googleMap.addMarker( markerOptions );
                
                allMarkers.put( marker, currChall.getCid() );
            }
            if( ( pDialog1 != null ) && pDialog1.isShowing() )
            {
                pDialog1.dismiss();
            }
        }
        
    }
    
    @SuppressWarnings( "deprecation" )
    void EnterRunMode( int cid )
    {
        
        
        handler.postDelayed( r, 0 );
        runityGo.setImageDrawable( runityRed );
        infoTime.setText( "00:00:00" );
        infoKcal.setText( "0kcal" );
        infoTime.setText( "0m" );
        
        infoTime.setTextColor( Color.parseColor( "#ff0000" ) );
        infoKcal.setTextColor( Color.parseColor( "#ff0000" ) );
        infoMeters.setTextColor( Color.parseColor( "#ff0000" ) );
        runMode = true;
        cidMode = cid;
        currentRun = new Run( 0, 0, 0, new Date().getSeconds(), new LatLng( GPSTracker.latitude, GPSTracker.longitude ) );
    }
    
    public static void Update( LatLng latLng )
    {
        if( runMode )
        {
            double deltaKm = RoutesHub.Dist( latLng.latitude, latLng.longitude, currentRun.lastPosition.latitude, currentRun.lastPosition.longitude );
            
            Log.d( "GPS", currentRun.lastPosition.toString() + " " + latLng.toString() );
            Log.d( "GPS", currentRun.distanceCovered + " " + deltaKm );
            
            currentRun.distanceCovered += deltaKm;
            currentRun.lastPosition = latLng;
            currentRun.caloriesBurned = ( ( 3.5 + 0.2 * ( ( currentRun.distanceCovered * 1000.0 ) / ( currentRun.timeElapsed / 60.0 ) ) ) / 3.5 ) * AVG_MASS * currentRun.timeElapsed / 3600.0;
            
            infoKcal.setText( ( int ) ( currentRun.caloriesBurned ) + "kcal" );
            if( currentRun.distanceCovered >= 1 )
            {
                infoMeters.setText( String.format( "%.2fkm", currentRun.distanceCovered ) );
            }
            else
            {
                infoMeters.setText( ( int ) ( currentRun.distanceCovered * 1000 ) + "m" );
            }
            
        }
        
    }
    
    
    static void ExitRunMode()
    {
        infoTime.setTextColor( Color.parseColor( "#7d7d7d" ) );
        infoKcal.setTextColor( Color.parseColor( "#7d7d7d" ) );
        infoMeters.setTextColor( Color.parseColor( "#7d7d7d" ) );
        runityGo.setImageDrawable( runityGray );
        runMode = false;
        
        if( cidMode == -2 )
        {
            new AlertDialog.Builder( mContext2 ).setTitle( "Poruka" ).setMessage( "Odustali ste od izazova." ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick( DialogInterface dialog, int which )
                {
                    // do nothing
                }
            } ).setIcon( android.R.drawable.ic_dialog_info ).show();
            
            infoTime.setText( "00:00:00" );
            infoKcal.setText( "0kcal" );
            infoMeters.setText( "0m" );
        }
        else if( cidMode == -1 )
        {
            new AlertDialog.Builder( mContext2 ).setTitle( "Uspešno završeno trčanje!" ).setMessage( "Trčali ste " + infoTime.getText() + " i izgubili " + infoKcal.getText() + "." ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick( DialogInterface dialog, int which )
                {
                    // do nothing
                }
            } ).setIcon( android.R.drawable.ic_dialog_info ).show();
            
            
            infoTime.setText( "00:00:00" );
            infoKcal.setText( "0kcal" );
            infoMeters.setText( "0m" );
            
        }
        else
        {
            new AlertDialog.Builder( mContext2 ).setTitle( "Uspešno završen izazov!" ).setMessage( "Vaše vreme je " + infoTime.getText() + "." ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick( DialogInterface dialog, int which )
                {
                    // do nothing
                }
            } ).setIcon( android.R.drawable.ic_dialog_info ).show();
            
            
            infoTime.setText( "00:00:00" );
            infoKcal.setText( "0kcal" );
            infoMeters.setText( "0m" );
            
            // deltaScore = (int)(currentRun.caloriesBurned*0.1);
            // new UpdateScore().execute();
            
            int time = ( int ) currentRun.timeElapsed;
            
            Participant curr = new Participant( CurrentUserHandler.pid, CurrentUserHandler.username, time );
            currC.Update( curr );
        }
        
    }
    
    boolean InRange( LatLng start )
    {
        double dist = RoutesHub.Dist( start.latitude, start.longitude, GPSTracker.latitude, GPSTracker.longitude );
        if( dist < 0.02 )
            return true;
        return false;
    }
    
    
    @Override
    public void onInfoWindowClick( Marker marker )
    {
        final int cid = allMarkers.get( marker );
        final Challenge currChall = allChallengesMap.get( cid );
        
        currC = allChallengesMap.get( cid );
        
        String info = currChall.daysLeft() + "\n" + currChall.getFirst() + "\n" + currChall.getSecond() + "\n" + currChall.getThird() + "\n" + currChall.getMyPos( CurrentUserHandler.pid );
        new AlertDialog.Builder( this ).setTitle( "Informacije o izazovu" ).setMessage( info ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                // do nothing
            }
        } ).setPositiveButton( "Trči!", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                if( InRange( currChall.startPos ) && !runMode )
                {
                    EnterRunMode( cid );
                }
                else if( runMode )
                {
                    Toast.makeText( getBaseContext(), "Završite trenutnu sesiju.", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    Toast.makeText( getBaseContext(), "Priđite bliže izazovu.", Toast.LENGTH_SHORT ).show();
                }
            }
        } ).setIcon( android.R.drawable.ic_dialog_info ).show();
        marker.hideInfoWindow();
    }
    
    @Override
    public boolean onMarkerClick( Marker marker )
    {
        if( endMarker != null )
        {
            endMarker.remove();
        }
        if( !allMarkers.containsKey( marker ) || runMode )
        {
            return false;
        }
        
        int cid = allMarkers.get( marker );
        Challenge currChall = allChallengesMap.get( cid );
        MarkerOptions markerOptions = new MarkerOptions().position( currChall.getEndPosition() ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_c_end ) );
        
        endMarker = googleMap.addMarker( markerOptions );
        marker.showInfoWindow();
        
        return false;
    }
    
    @Override
    public void onMapClick( LatLng arg0 )
    {
        focus_mode = false;
        if( runMode == false && endMarker != null )
        {
            endMarker.remove();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        googleMap.clear();
        googleMap = null;
        System.gc();
    }
    
}
