
package com.runity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


@SuppressLint( "ClickableViewAccessibility" )
public class AddChallenge extends FragmentActivity implements OnClickListener, OnMapClickListener, OnTouchListener
{
    
    private ProgressDialog pDialog;
    
    JSONParser jsonParser = new JSONParser();
    
    CameraUpdate cu = null;
    
    private EditText et_from;
    
    private EditText et_to;
    
    Date today;
    
    private ImageView btn_send, btn_trash;
    
    private DatePickerDialog fromDatePickerDialog;
    
    private DatePickerDialog toDatePickerDialog;
    
    private SimpleDateFormat dateFormatter, dateFormatterSql;
    
    GoogleMap googleMap;
    
    // Current selected challenge markers
    Marker startMarker, endMarker;
    
    private static String url_add_challenge = "http://" + GV.localIP + "/android_connect/challenges/add_challenge.php";
    
    // JSON Tags
    private static final String TAG_SUCCESS = "success";
    
    Date startdate, enddate;
    
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_add_challenge );
        
        dateFormatter = new SimpleDateFormat( "dd-MM-yyyy", Locale.US );
        // Consistency with SQL Date Format
        dateFormatterSql = new SimpleDateFormat( "yyyy-MM-dd", Locale.US );
        
        SupportMapFragment fm = ( SupportMapFragment ) getSupportFragmentManager().findFragmentById( R.id.addChallenge_map );
        googleMap = fm.getMap();
        googleMap.setOnMapClickListener( this );
        
        // First cam animation with preset zoom
        LatLng latLng = new LatLng( GPSTracker.latitude, GPSTracker.longitude );
        cu = CameraUpdateFactory.newLatLngZoom( latLng, 16f );
        googleMap.animateCamera( cu );
        
        btn_send = ( ImageView ) findViewById( R.id.addChallenge_imageView_send );
        btn_trash = ( ImageView ) findViewById( R.id.addChallenge_imageView_trash );
        
        btn_send.setOnTouchListener( this );
        btn_trash.setOnTouchListener( this );
        
        et_from = ( EditText ) findViewById( R.id.addChallenge_editText_fromdate );
        et_from.setInputType( InputType.TYPE_NULL );
        et_from.requestFocus();
        
        et_to = ( EditText ) findViewById( R.id.addChallenge_editText_todate );
        et_to.setInputType( InputType.TYPE_NULL );
        
        
        et_from.setOnClickListener( this );
        et_to.setOnClickListener( this );
        
        Calendar newCalendar = Calendar.getInstance();
        
        fromDatePickerDialog = new DatePickerDialog( this, new OnDateSetListener()
        {
            public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
            {
                Calendar newDate = Calendar.getInstance();
                newDate.set( year, monthOfYear, dayOfMonth );
                et_from.setText( dateFormatter.format( newDate.getTime() ) );
            }
            
        }, newCalendar.get( Calendar.YEAR ), newCalendar.get( Calendar.MONTH ), newCalendar.get( Calendar.DAY_OF_MONTH ) );
        
        toDatePickerDialog = new DatePickerDialog( this, new OnDateSetListener()
        {
            
            public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
            {
                Calendar newDate = Calendar.getInstance();
                newDate.set( year, monthOfYear, dayOfMonth );
                et_to.setText( dateFormatter.format( newDate.getTime() ) );
            }
            
        }, newCalendar.get( Calendar.YEAR ), newCalendar.get( Calendar.MONTH ), newCalendar.get( Calendar.DAY_OF_MONTH ) );
        
        today = new Date();
        today.setDate( new Date().getDate() + 1 );
        et_from.setText( dateFormatter.format( today ) );
        fromDatePickerDialog.updateDate( today.getYear() + 1900, today.getMonth(), today.getDate() );
        
        today.setDate( today.getDate() + 7 );
        et_to.setText( dateFormatter.format( today ) );
        toDatePickerDialog.updateDate( today.getYear() + 1900, today.getMonth(), today.getDate() );
        
        
        today = new Date();
    }
    
    // @Override
    // public boolean onCreateOptionsMenu( Menu menu )
    // {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // // getMenuInflater().inflate( R.menu.add_challenge, menu );
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
    
    
    @Override
    public void onClick( View view )
    {
        if( view == et_from )
        {
            fromDatePickerDialog.show();
        }
        else if( view == et_to )
        {
            toDatePickerDialog.show();
        }
    }
    
    @Override
    public void onMapClick( LatLng l )
    {
        final LatLng loc = l;
        
        // Run dialog to choose if start or end point of challenge
        new AlertDialog.Builder( this ).setTitle( "Odabir" ).setMessage( "Postavi kao:" ).setPositiveButton( "Start", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                if( startMarker != null )
                    startMarker.remove();
                startMarker = googleMap.addMarker( new MarkerOptions().position( loc ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_c_start ) ) );
                cu = CameraUpdateFactory.newLatLng( loc );
                googleMap.animateCamera( cu );
            }
        } ).setNegativeButton( "Cilj", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                if( endMarker != null )
                    endMarker.remove();
                endMarker = googleMap.addMarker( new MarkerOptions().position( loc ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_c_end ) ) );
                cu = CameraUpdateFactory.newLatLng( loc );
                googleMap.animateCamera( cu );
            }
        } ).setIcon( android.R.drawable.ic_dialog_map ).show();
    }
    
    @Override
    public boolean onTouch( View arg0, MotionEvent arg1 )
    {
        if( arg0 == btn_send )
        {
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                // Both markers should be set
                if( startMarker == null || endMarker == null )
                {
                    new AlertDialog.Builder( this ).setTitle( "Gre\u0161ka" ).setMessage( "Niste uneli sve potrebne podatke." ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int which )
                        {
                            // do nothing
                        }
                    } ).setIcon( android.R.drawable.ic_dialog_alert ).show();
                    
                    return true;
                }
                
                startdate = null;
                enddate = null;
                try
                {
                    startdate = dateFormatter.parse( et_from.getText().toString() );
                    enddate = dateFormatter.parse( et_to.getText().toString() );
                }
                catch( ParseException e )
                {
                    e.printStackTrace();
                }
                
                // Check if dates are valid
                if( startdate.compareTo( enddate ) >= 0 || today.compareTo( startdate ) >= 0 )
                {
                    new AlertDialog.Builder( this ).setTitle( "Gre\u0161ka" ).setMessage( "Izazov mo\u017Ee po\u010Deti najranije danas, krajnji datum mora biti posle po\u010Detnog." ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int which )
                        {
                            // do nothing
                        }
                    } ).setIcon( android.R.drawable.ic_dialog_alert ).show();
                    return true;
                }
           
            
            startlat = startMarker.getPosition().latitude;
            startlng = startMarker.getPosition().longitude;
            endlat = endMarker.getPosition().latitude;
            endlng = endMarker.getPosition().longitude;
            new AddChallengeAsync().execute();
            return true;
        }}
        else if( arg0 == btn_trash )
        {
            if( arg1.getAction() == MotionEvent.ACTION_UP )
                finish();
        }
        
        return false;
    }
    
    double startlat, startlng, endlat, endlng;
    
    // Add challenge to SQL Query
    class AddChallengeAsync extends AsyncTask < String, String, String >
    {
        
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( AddChallenge.this );
            pDialog.setMessage( "Kreiranje izazova..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( true );
            pDialog.show();
        }
        
        
        /**
         * Creating product
         * */
        protected String doInBackground( String... args )
        {
            List < NameValuePair > params = new ArrayList < NameValuePair >();
            params.add( new BasicNameValuePair( "startdate", dateFormatterSql.format( startdate ) ) );
            params.add( new BasicNameValuePair( "enddate", dateFormatterSql.format( enddate ) ) );
            params.add( new BasicNameValuePair( "startlat", Double.toString( startlat ) ) );
            params.add( new BasicNameValuePair( "startlng", Double.toString( startlng ) ) );
            params.add( new BasicNameValuePair( "endlat", Double.toString( endlat ) ) );
            params.add( new BasicNameValuePair( "endlng", Double.toString( endlng ) ) );
            params.add( new BasicNameValuePair( "pid", Integer.toString( CurrentUserHandler.pid ) ) );
            
            // Get JSON
            JSONObject json = jsonParser.makeHttpRequest( url_add_challenge, "POST", params );
            
            // Check for success tag
            try
            {
                int success = json.getInt( TAG_SUCCESS );
                
                if( success == 1 )
                {
                    finish();
                }
                else
                {
                    // System.out.println( "Error adding challenge" );
                    // failed to create
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
            }
            
            return null;
        }
        
        // Dismiss the dialog after query
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
    
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        // Get rid of bitmaps
        System.gc();
    }
}
