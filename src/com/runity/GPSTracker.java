
package com.runity;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GPSTracker extends Service implements LocationListener
{
    
    private final Context mContext;
    
    // Flag for GPS status
    boolean isGPSEnabled = false;
    
    // Flag for network status
    boolean isNetworkEnabled = false;
    
    // Flag for GPS status
    boolean canGetLocation = false;
    
    static GoogleMap googleMap;
    
    static Location location; // location
    
    static double latitude; // latitude
    
    static double longitude; // longitude
    
    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // meters
    
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1;
    
    protected LocationManager locationManager;
    
    private static float[] mRotationMatrix = new float[16];
    
    private static double mDeclination;
    
    public static double CIRCLE_OFFSET = 0.00005F;
    
    static Marker oldMarker = null;
    
    static Marker newMarker = null;
    
    private final SensorManager mSensorManager;
    
    static SensorEventListener mSensorListener = new SensorEventListener()
    {
        
        
        @Override
        public void onSensorChanged( SensorEvent event )
        {
            if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR )
            {
                SensorManager.getRotationMatrixFromVector( mRotationMatrix, event.values );
                float[] orientation = new float[3];
                SensorManager.getOrientation( mRotationMatrix, orientation );
                float bearing = ( float ) ( Math.toDegrees( orientation[0] ) + mDeclination );
                updateCamera( bearing );
            }
        }
        
        @Override
        public void onAccuracyChanged( Sensor sensor, int accuracy )
        {
        }
    };
    
    public GPSTracker( Context context, GoogleMap googleMap )
    {
        this.mContext = context;
        GPSTracker.googleMap = googleMap;
        
        getLocation();
        
        mSensorManager = ( SensorManager ) mContext.getSystemService( SENSOR_SERVICE );
        mSensorManager.registerListener( mSensorListener, mSensorManager.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR ), SensorManager.SENSOR_DELAY_UI );
    }
    
    public Location getLocation()
    {
        try
        {
            locationManager = ( LocationManager ) mContext.getSystemService( LOCATION_SERVICE );
            
            isGPSEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
            
            isNetworkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
            
            if( !isGPSEnabled && !isNetworkEnabled )
            {
                this.canGetLocation = false;
            }
            else
            {
                this.canGetLocation = true;
                
                // First get location from Network Provider
                if( isNetworkEnabled )
                {
                    locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this );
                    
                    if( locationManager != null )
                    {
                        location = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
                        if( location != null )
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                
                if( isGPSEnabled )
                {
                    if( location == null )
                    {
                        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this );
                        
                        if( locationManager != null )
                        {
                            location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                            if( location != null )
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
            
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        return location;
    }
    
    
    public void stopUsingGPS()
    {
        if( locationManager != null )
        {
            locationManager.removeUpdates( GPSTracker.this );
        }
    }
    
    public static double getLatitude()
    {
        if( location != null )
        {
            latitude = location.getLatitude();
        }
        
        return latitude;
    }
    
    public static double getLongitude()
    {
        if( location != null )
        {
            longitude = location.getLongitude();
        }
        
        return longitude;
    }
    
    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }
    
    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( mContext );
        
        // Setting Dialog Title
        alertDialog.setTitle( "GPS is settings" );
        
        // Setting Dialog Message
        alertDialog.setMessage( "GPS is not enabled. Do you want to go to settings menu?" );
        
        // On pressing Settings button
        alertDialog.setPositiveButton( "Settings", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                Intent intent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                mContext.startActivity( intent );
            }
        } );
        
        // On pressing cancel button
        alertDialog.setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {
                dialog.cancel();
            }
        } );
        
        // Showing Alert Message
        alertDialog.show();
    }
    
    @Override
    public void onLocationChanged( Location location )
    {
        if( this.canGetLocation() )
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            
            GeomagneticField field = new GeomagneticField( ( float ) location.getLatitude(), ( float ) location.getLongitude(), ( float ) location.getAltitude(), System.currentTimeMillis() );
            
            // getDeclination returns degrees
            mDeclination = field.getDeclination();
            
            LatLng latLngMarker = new LatLng( latitude - CIRCLE_OFFSET, longitude );
            
            LatLng latLng = new LatLng( latitude, longitude );
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include( latLng );
            
            
            oldMarker = newMarker;
            newMarker = googleMap.addMarker( new MarkerOptions().position( latLngMarker ).icon( BitmapDescriptorFactory.fromResource( R.drawable.me2 ) ) );
            
            if( oldMarker != null )
            {
                oldMarker.remove();
            }
            
            if( MainMenu.focus_mode )
            {
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom( latLng, 16f );
                googleMap.animateCamera( cu );
            }
            
            if( MainMenu.runMode )
            {
                MainMenu.Update( latLng );
            }
            
            if( MainMenu.cidMode != -1 && MainMenu.runMode && RoutesHub.Dist( MainMenu.endMarker.getPosition().latitude, MainMenu.endMarker.getPosition().longitude, latitude, longitude ) < 0.02 )
            {
                MainMenu.ExitRunMode();
                new SendTime().execute();
            }
        }
        else
        {
            this.showSettingsAlert();
        }
    }
    
    JSONParser jsonParser = new JSONParser();
    
    private static final String TAG_SUCCESS = "success";
    
    private static String url_send_time = "http://" + GV.localIP + "/android_connect/challenges/send_time.php";
    
    class SendTime extends AsyncTask < String, String, String >
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "cid", Integer.toString( MainMenu.cidMode ) ) );
                params1.add( new BasicNameValuePair( "pid", Integer.toString( CurrentUserHandler.pid ) ) );
                params1.add( new BasicNameValuePair( "time", Integer.toString( ( int ) MainMenu.currentRun.timeElapsed ) ) );
                JSONObject json = jsonParser.makeHttpRequest( url_send_time, "POST", params1 );
                
                
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
            
        }
    }
    
    private static void updateCamera( float bearing )
    {
        CameraPosition oldPos = googleMap.getCameraPosition();
        
        CameraPosition pos = CameraPosition.builder( oldPos ).bearing( bearing ).build();
        googleMap.moveCamera( CameraUpdateFactory.newCameraPosition( pos ) );
    }
    
    @Override
    public void onProviderDisabled( String provider )
    {
    }
    
    @Override
    public void onProviderEnabled( String provider )
    {
    }
    
    @Override
    public void onStatusChanged( String provider, int status, Bundle extras )
    {
    }
    
    @Override
    public IBinder onBind( Intent intent )
    {
        return null;
    }
    
}
