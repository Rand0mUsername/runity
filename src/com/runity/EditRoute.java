package com.runity;
 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
 
 
@SuppressLint(
{ "SimpleDateFormat", "ClickableViewAccessibility" } )
public class EditRoute extends FragmentActivity implements OnTouchListener, OnMapClickListener
{
   
    SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
   
    Route sRoute;
   
    int modify;
   
    TimePicker tPick;
   
    DatePicker dPick;
   
    ImageView send, trash;
   
    double lat = 0, lng = 0;
   
    String datetime;
   
    GoogleMap googleMap;
   
    // Progress Dialog
    private ProgressDialog pDialog;
   
    JSONParser jsonParser = new JSONParser();
   
    // URLs for SQL queries
    private static String url_new_route = "http://" + GV.localIP + "/android_connect/routes/new_route.php";
   
    private static String url_modify_route = "http://" + GV.localIP + "/android_connect/routes/modify_route.php";
   
    private static String url_delete_route = "http://" + GV.localIP + "/android_connect/routes/delete_route.php";
   
    // JSON Tags
    private static final String TAG_SUCCESS = "success";
   
    Date date = null;
   
    Marker marker = null;
   
    LatLng latLng = null;
   
    CameraUpdate cu = null;
   
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit_route );
        sRoute = RoutesHub.selectedRoute;
        Bundle extras = getIntent().getExtras();
       
        SupportMapFragment fm = ( SupportMapFragment ) getSupportFragmentManager().findFragmentById( R.id.editRoute_map );
        googleMap = fm.getMap();
        googleMap.setOnMapClickListener( this );
        // First cam animation with preset zoom
        latLng = new LatLng(GPSTracker.latitude, GPSTracker.longitude );
        cu = CameraUpdateFactory.newLatLngZoom( latLng, 16f );
        googleMap.animateCamera( cu );
       
        tPick = ( TimePicker ) findViewById( R.id.editRoute_timePicker );
        tPick.setIs24HourView( true );
       
        dPick = ( DatePicker ) findViewById( R.id.editRoute_datePicker );
       
        send = ( ImageView ) findViewById( R.id.editRoute_imageView_send );
        send.setOnTouchListener( this );
       
        trash = ( ImageView ) findViewById( R.id.editRoute_imageView_trash );
        trash.setOnTouchListener( this );
       
        // Get intent extras
        if( extras != null )
        {
            modify = extras.getInt( "modify" );
        }
       
        // Check modify attribute
        if( modify == 1 )
        {
            try
            {
                date = dateFormat.parse( sRoute.dateTime );
            }
            catch( ParseException e )
            {
                e.printStackTrace();
            }
           
            lat = sRoute.lat;
            lng = sRoute.lng;
           
            tPick.setCurrentHour( date.getHours() );
            tPick.setCurrentMinute( date.getMinutes() );
            dPick.updateDate( date.getYear() + 1900, date.getMonth(), date.getDate() );
        }
        else
        {
            // Get current time
            date = new Date();
           
            // Current pos for map
            lat = GPSTracker.latitude;
            lng = GPSTracker.longitude;
           
            tPick.setCurrentHour( date.getHours() + 1 );
            tPick.setCurrentMinute( date.getMinutes() );
            dPick.updateDate( date.getYear() + 1900, date.getMonth(), date.getDate() );
        }
       
        latLng = new LatLng( lat, lng );
        cu = CameraUpdateFactory.newLatLngZoom( latLng, 15f );
        googleMap.animateCamera( cu );
       
        marker = googleMap.addMarker( new MarkerOptions().position( latLng ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_routes ) ) );
    }
   
    // @Override
    // public boolean onCreateOptionsMenu( Menu menu )
    // {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate( R.menu.new_route, menu );
    // return true;
    // }
    //
    // @Override
    // public boolean onOptionsItemSelected( MenuItem item )
    // {
    // // Handle action bar item clicks here. The action bar will
    // // automatically handle clicks on the Home/Up button, so long
    // // as you specify a parent activity in AndroidManifest.xml.
    // int id = item.getItemId();
    // if( id == R.id.action_settings )
    // {
    // return true;
    // }
    // return super.onOptionsItemSelected( item );
    // }
   
    @SuppressWarnings( "deprecation" )
    @Override
    public boolean onTouch( View arg0, MotionEvent arg1 )
    {
        if( arg0 == send && arg1.getAction() == MotionEvent.ACTION_UP )
        {
            // Deparse
            lat = latLng.latitude;
            lng = latLng.longitude;
           
            date.setYear( dPick.getYear() - 1900 );
            date.setMonth( dPick.getMonth() );
            date.setDate( dPick.getDayOfMonth() );
            date.setHours( tPick.getCurrentHour() );
            date.setMinutes( tPick.getCurrentMinute() );
            date.setSeconds( 0 );
            datetime = dateFormat.format( date );
           
            if( modify == 1 )
            {
                new ModifyRoute().execute();
            }
            else
            {
                new CreateNewRoute().execute();
            }
            return true;
        }
        else if( arg0 == trash && arg1.getAction() == MotionEvent.ACTION_UP )
        {
            if( modify == 1 )
            {
                new DeleteRoute().execute();
            }
            else
            {
                finish();
            }
            return true;
        }
        return false;
    }
   
    @Override
    public void onPause()
    {
        super.onPause();
        if( ( pDialog != null ) && pDialog.isShowing() )
            pDialog.dismiss();
        pDialog = null;
    }
   
    @Override
    public void onStop()
    {
        super.onStop();
        if( ( pDialog != null ) && pDialog.isShowing() )
            pDialog.dismiss();
        pDialog = null;
    }
   
    // Modify route SQL query
    class ModifyRoute extends AsyncTask < String, String, String >
    {
       
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( EditRoute.this );
            pDialog.setMessage( "Ruta se pamti..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( true );
            pDialog.show();
        }
       
       
        protected String doInBackground( String... args )
        {
            String rid = Integer.toString( sRoute.getId() );
           
            // Building Parameters
            List < NameValuePair > params = new ArrayList < NameValuePair >();
            params.add( new BasicNameValuePair( "rid", rid ) );
            params.add( new BasicNameValuePair( "lat", Double.toString( lat ) ) );
            params.add( new BasicNameValuePair( "lng", Double.toString( lng ) ) );
            params.add( new BasicNameValuePair( "datetime", datetime ) );
            System.out.println( params );
           
            // Get JSON
            JSONObject json = jsonParser.makeHttpRequest( url_modify_route, "POST", params );
           
            // Check JSON success tag
            try
            {
                int success = json.getInt( TAG_SUCCESS );
               
                if( success == 1 )
                {
                    finish();
                }
                else
                {
                    // failed to create
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
           
            return null;
        }
       
       
        // Dismiss dialog
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
   
   
    // Delete route SQL query
    class DeleteRoute extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( EditRoute.this );
            pDialog.setMessage( "Brisanje rute..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( true );
            pDialog.show();
        }
       
        protected String doInBackground( String... args )
        {
            String rid = Integer.toString( sRoute.getId() );
           
            // Building Parameters
            List < NameValuePair > params = new ArrayList < NameValuePair >();
            params.add( new BasicNameValuePair( "rid", rid ) );
            System.out.println( params );
           
            // Get JSON
            JSONObject json = jsonParser.makeHttpRequest( url_delete_route, "POST", params );
           
            // Check succes tag from JSON
            try
            {
                int success = json.getInt( TAG_SUCCESS );
               
                if( success == 1 )
                {
                    finish();
                }
                else
                {
                    // failed to create
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
           
            return null;
        }
       
       
        // Dismiss dialog
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
   
   
    // Create route SQL query
    class CreateNewRoute extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( EditRoute.this );
            pDialog.setMessage( "Kreiranje rute..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( true );
            pDialog.show();
        }
       
        protected String doInBackground( String... args )
        {
            String pid = Integer.toString( RoutesHub.userPid );
           
            // Building Parameters
            List < NameValuePair > params = new ArrayList < NameValuePair >();
            params.add( new BasicNameValuePair( "pid", pid ) );
            params.add( new BasicNameValuePair( "lat", Double.toString( lat ) ) );
            params.add( new BasicNameValuePair( "lng", Double.toString( lng ) ) );
            params.add( new BasicNameValuePair( "datetime", datetime ) );
 
            // Get JSON
            JSONObject json = jsonParser.makeHttpRequest( url_new_route, "POST", params );
           
           
            // Check JSON success tag
            try
            {
                int success = json.getInt( TAG_SUCCESS );
               
                if( success == 1 )
                {
                    finish();
                }
                else
                {
                    // failed to create
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
           
            return null;
        }
       
        // Dismiss dialog
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
   
    @Override
    public void onMapClick( LatLng loc )
    {
        latLng = loc;
       
        marker.remove();
        marker = googleMap.addMarker( new MarkerOptions().position( loc ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_routes ) ) );
       
        cu = CameraUpdateFactory.newLatLng( loc );
        googleMap.animateCamera( cu );
    }
   
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
       
        // Get rid of bitmaps for memory
        System.gc();
    }
   
}