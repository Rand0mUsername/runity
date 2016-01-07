package com.runity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


@SuppressLint(
{ "SimpleDateFormat", "ClickableViewAccessibility" } )
public class RoutesHub extends FragmentActivity implements OnItemClickListener, OnTouchListener, OnMarkerClickListener, OnInfoWindowClickListener
{
    public class CustomDialog extends DialogFragment
    {
        private String title = null;
        
        private String[] options = null;
        
        private String selected = new String( "All" );
        
        public String getSelected()
        {
            return selected;
        }
        
        public CustomDialog( String title, String[] options )
        {
            super();
            this.title = title;
            this.options = options;
        }
        
        @Override
        public Dialog onCreateDialog( Bundle savedInstanceState )
        {
            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
            builder.setTitle( title ).setItems( options, new DialogInterface.OnClickListener()
            {
                public void onClick( DialogInterface dialog, int position )
                {
                    selected = options[position];
                    getFeedback( selected );
                }
            } );
            
            
            return builder.create();
        }
    }
    
    @SuppressWarnings( "unused" )
    private DrawerLayout drawerLayout;
    
    private ListView drawerList;
    
    private String[] filtersEnum = new String[]
    { "PEOPLE", "TIME", "DISTANCE", "FROM", "TO", "SORT" };
    
    private String[] peopleFilterOptions = null;
    
    private String[] timeFilterOptions = null;
    
    private String[] distanceFilterOptions = null;
    
    private String[] fromFilterOptions = new String[24];
    
    private String[] toFilterOptions = new String[24];
    
    private String[] sortFilterOptions = null;
    
    CustomDialog peopleFilter = null;
    
    CustomDialog timeFilter = null;
    
    CustomDialog distanceFilter = null;
    
    CustomDialog fromFilter = null;
    
    CustomDialog toFilter = null;
    
    CustomDialog sortFilter = null;
    
    private String[] defaultFilters = new String[]
    { "Ljudi: Svi", "Vreme: Sva vremena", "Rastojanje: Sva rastojanja", "Od: 0", "Do: 24", "Sortiraj po: Vreme" };
    
    private static String[] prefString = new String[]
    { "Ljudi: ", "Vreme: ", "Rastojanje: ", "Od: ", "Do: ", "Sortiraj po: " };
    
    static int lastSelection = -1;
    
    static String[] filterResult = new String[]
    { "Svi", "Sva vremena", "Sva rastojanja", "0", "24", "Vreme" };
    
    
    String userUsername;
    
    static int userPid;
    
    ListView table;
    
    ImageView newRoute, seeNotifs;
    
    RoutesHub mContext;
    
    LatLng latLng;
    
    RouteAdapter adapter = null;
    
    CameraUpdate cu = null;
    
    static String feedback = null;
    
    static TextView tw;
    
    Typeface nanosecond;
    
    static int tableSelected = -1;
    
    GoogleMap googleMap;
    
    TextView lblrouter, lblrouted, lblroutet, txtrouter, txtrouted, txtroutet;
    
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_routes_hub );
        
        
        userUsername = CurrentUserHandler.username;
        userPid = CurrentUserHandler.pid;
        table = ( ListView ) findViewById( R.id.routesHub_listView_table );
        newRoute = ( ImageView ) findViewById( R.id.routesHub_imageView_new );
        seeNotifs = ( ImageView ) findViewById( R.id.routesHub_imageView_notifz );
        seeNotifs.setOnTouchListener( this );
        newRoute.setOnTouchListener( this );
        
        drawerLayout = ( DrawerLayout ) findViewById( R.id.drawerLayout );
        drawerList = ( ListView ) findViewById( R.id.drawerList );
        drawerList.setAdapter( new ArrayAdapter < String >( this, android.R.layout.simple_list_item_1, defaultFilters ) );
        drawerList.setOnItemClickListener( this );
        
        
        peopleFilterOptions = new String[]
        { "Svi", "Samo ja", "Samo drugi" };
        timeFilterOptions = new String[]
        { "Sva vremena", "Danas", "Narednih 7 dana", "Narednih 30 dana" };
        distanceFilterOptions = new String[]
        { "Sva rastojanja", "100m od mene", "1km od mene", "5km od mene" };
        fromFilterOptions = new String[24];
        for( int i = 0; i < 24; i++ )
        {
            fromFilterOptions[i] = Integer.toString( i );
        }
        toFilterOptions = new String[24];
        for( int i = 1; i < 25; i++ )
        {
            toFilterOptions[i - 1] = Integer.toString( i );
        }
        sortFilterOptions = new String[]
        { "Vreme", "Rastojanje" };
        
        
        peopleFilter = new CustomDialog( "Izaberi filter", peopleFilterOptions );
        timeFilter = new CustomDialog( "Izaberi vremenski interval", timeFilterOptions );
        distanceFilter = new CustomDialog( "Izaberi razdaljinu", distanceFilterOptions );
        fromFilter = new CustomDialog( "Izaberi pocetak intervala", fromFilterOptions );
        toFilter = new CustomDialog( "Izaberi kraj intervala", toFilterOptions );
        sortFilter = new CustomDialog( "Sortiraj po", sortFilterOptions );
        
        mContext = this;
        
        SupportMapFragment fm = ( SupportMapFragment ) getSupportFragmentManager().findFragmentById( R.id.routesHub_map );
        googleMap = fm.getMap();

        // First cam animation with preset zoom
        LatLng latLng = new LatLng(GPSTracker.latitude, GPSTracker.longitude );
        cu = CameraUpdateFactory.newLatLngZoom( latLng, 16f );
        googleMap.animateCamera( cu );
        
        googleMap.setOnMarkerClickListener( this );
        googleMap.setOnInfoWindowClickListener( this );
        
        Refresh();

        System.out.println( "End" );
        
    }
    
    static double Dist( double lat1, double lng1, double lat2, double lng2 )
    {
        double R = 6372.8; // in km
        double phi1 = Math.toRadians( lat1 );
        double phi2 = Math.toRadians( lat2 );
        double dphi = Math.toRadians( lat2 - lat1 );
        double dl = Math.toRadians( lng2 - lng1 );
        double a = Math.sin( dphi / 2 ) * Math.sin( dphi / 2 ) + Math.cos( phi1 ) * Math.cos( phi2 ) * Math.sin( dl / 2 ) * Math.sin( dl / 2 );
        double c = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );
        double D = R * c;
        return D;
    }
    
    String pplF, timeF, distF, fromF, toF;
    
    int sortCritCode;
    
    ArrayList < Route > goodRoutesList = new ArrayList < Route >();
    
    ArrayList < Route > newGoodRoutesList = new ArrayList < Route >();
    
    ArrayList < Marker > markerList = new ArrayList < Marker >();
    
    
    void ApplyDistFilter()
    {
        
        double tHold = 0;
        if( distF.equals( "Sva rastojanja" ) )
            tHold = -1;
        else if( distF.equals( "100m od mene" ) )
            tHold = 0.1;
        else if( distF.equals( "1km od mene" ) )
            tHold = 1;
        else if( distF.equals( "5km od mene" ) )
            tHold = 5;
        
        for( Route rt : goodRoutesList )
        {
            if( tHold == -1 || rt.dist <= tHold )
            {
                newGoodRoutesList.add( rt );
            }
        }
    }
    
    DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    
    class DistanceComparator implements Comparator < Route >
    {
        public int compare( Route a, Route b )
        {
            return a.dist < b.dist? -1: a.dist == b.dist? 0: 1;
        }
    }
    
    class TimeComparator implements Comparator < Route >
    {
        public int compare( Route a, Route b )
        {
            Date dateA = null, dateB = null;
            try
            {
                dateA = dateFormat.parse( a.dateTime );
            }
            catch( ParseException e )
            {
                e.printStackTrace();
            }
            try
            {
                dateB = dateFormat.parse( b.dateTime );
            }
            catch( ParseException e )
            {
                e.printStackTrace();
            }
            return dateA.compareTo( dateB );
        }
    }
    
    void SortNewList( int code )
    {
        // By distance
        if( code == 1 )
        {
            DistanceComparator dc = new DistanceComparator();
            Collections.sort( newGoodRoutesList, dc );
        }
        // By time
        else
        {
            TimeComparator tc = new TimeComparator();
            Collections.sort( newGoodRoutesList, tc );
        }
    }
    
    
    void Refresh()
    {
        tableSelected = -1;
        
        pplF = filterResult[0];
        timeF = filterResult[1];
        distF = filterResult[2];
        fromF = filterResult[3];
        toF = filterResult[4];
        
        if( filterResult[5].equals( "Vreme" ) )
            sortCritCode = 0;
        else
            sortCritCode = 1;
        if( Integer.parseInt( toF ) < Integer.parseInt( fromF ) )
            return;

        goodRoutesList.clear();
        newGoodRoutesList.clear();
        
        new ApplyFilters().execute();
    }
    
    // Progress Dialog
    private ProgressDialog pDialog;
    
    JSONParser jsonParser = new JSONParser();
    
    private static String url_query_routes = "http://" + GV.localIP + "/android_connect/routes/query_routes.php";
    
    private static String url_query_attendance = "http://" + GV.localIP + "/android_connect/routes/query_attendance.php";
    
    private static String url_add_attend = "http://" + GV.localIP + "/android_connect/routes/add_attend.php";
    
    private static String url_modify_status = "http://" + GV.localIP + "/android_connect/routes/modify_attend_status.php";
    
    private static final String TAG_SUCCESS = "success";
    
    private static final String TAG_ROUTES = "routes";
    
    private static final String TAG_VALID = "valid";
    
    String[] infoBoxStrings1 = new String[]
    { "Niste se pridružili ovde.", "Niste se pridružili ovde.", "Učesvujete ovde već.", "Niste se pridružili ovde.", "Niste se pridružili ovde." };
    
    String[] infoBoxStrings2 = new String[]
    { "Klikni da se pridružiš.", "Odbijen zahtev.", "Klikni da odustaneš.", "Čeka se odgovor.", "Cekamo da skonta da si ga ispalio." };
    
    class ApplyFilters extends AsyncTask < String, String, String >
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( RoutesHub.this );
            pDialog.setMessage( "Učitavanje..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( false );
            pDialog.show();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            int success, valid, suc1, val1;
            
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "pplfilter", pplF ) );
                params1.add( new BasicNameValuePair( "maxdate", timeF ) );
                params1.add( new BasicNameValuePair( "maxdist", distF ) );
                params1.add( new BasicNameValuePair( "mintime", fromF ) );
                params1.add( new BasicNameValuePair( "maxtime", toF ) );
                params1.add( new BasicNameValuePair( "pid", Integer.toString( userPid ) ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_query_routes, "GET", params1 );
                
                success = json.getInt( TAG_SUCCESS );
                valid = json.getInt( TAG_VALID );
                
                if( success == 1 && valid == 1 )
                {
                    JSONArray goodRoutes = json.getJSONArray( TAG_ROUTES ); // JSON Array
                    
                    for( int i = 0; i < goodRoutes.length(); i++ )
                    {
                        JSONObject c = goodRoutes.getJSONObject( i );
                        
                        String dateTime = c.getString( "datetime" );
                        double lat = c.getDouble( "lat" );
                        double lng = c.getDouble( "lng" );
                        int user_id = c.getInt( "pid" );
                        int route_id = c.getInt( "rid" );
                        String r_username = c.getString( "username" );
                        
                        int status = -1;
                        List < NameValuePair > params2 = new ArrayList < NameValuePair >();
                        params2.add( new BasicNameValuePair( "rid", Integer.toString( route_id ) ) );
                        params2.add( new BasicNameValuePair( "pid", Integer.toString( userPid ) ) );
                        JSONObject json2 = jsonParser.makeHttpRequest( url_query_attendance, "GET", params2 );
                        suc1 = json2.getInt( TAG_SUCCESS );
                        ArrayList < Attendance > atts = new ArrayList < Attendance >();
                        
                        if( suc1 == 1 )
                        {
                            val1 = json2.getInt( TAG_VALID );
                            if( val1 == 1 )
                            {
                                JSONArray items = json2.getJSONArray( "items" ); // JSON Array
                                for( int j = 0; j < items.length(); j++ )
                                {
                                    JSONObject c2 = items.getJSONObject( j );
                                    Attendance att = new Attendance( c2.getInt( "pid" ), c2.getString( "username" ), c2.getInt( "status" ) );
                                    atts.add( att );
                                    
                                    if( att.getId() == userPid )
                                    {
                                        status = att.getStatus();
                                    }
                                }
                            }
                            else
                            {
                            }
                        }
                        else
                        {}
                        
                        goodRoutesList.add( new Route( dateTime, lat, lng, user_id, route_id, Dist( GPSTracker.getLatitude(), GPSTracker.getLongitude(), lat, lng ), r_username, atts, status ) );
                    }
                    
                    ApplyDistFilter();
                    
                    SortNewList( sortCritCode );
                    
                    adapter = new RouteAdapter( mContext, newGoodRoutesList );
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
            table.setAdapter( adapter );
            table.setOnItemClickListener( mContext );
            googleMap.clear();
            markerList.clear();

            // First cam animation with preset zoom
            LatLng latLng = new LatLng(GPSTracker.latitude, GPSTracker.longitude );
            cu = CameraUpdateFactory.newLatLngZoom( latLng, 16f );
            googleMap.animateCamera( cu );
            
            for( int i = 0; i < newGoodRoutesList.size(); i++ )
            {
                int status = newGoodRoutesList.get( i ).getUserStatus();
                if( newGoodRoutesList.get( i ).getUser_id() == userPid )
                    markerList.add( googleMap.addMarker( new MarkerOptions().position( new LatLng( newGoodRoutesList.get( i ).lat, newGoodRoutesList.get( i ).lng ) ).title( "Tap to edit" ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_routes ) ) ) );
                else
                    markerList.add( googleMap.addMarker( new MarkerOptions().position( new LatLng( newGoodRoutesList.get( i ).lat, newGoodRoutesList.get( i ).lng ) ).title( infoBoxStrings1[status + 1] ).snippet( infoBoxStrings2[status + 1] ).icon( BitmapDescriptorFactory.fromResource( R.drawable.marker_routes ) ) ) );
                
            }
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
    
    
//    @Override
//    public boolean onCreateOptionsMenu( Menu menu )
//    {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate( R.menu.routes_table, menu );
//        return true;
//    }
//    
//    @Override
//    public boolean onOptionsItemSelected( MenuItem item )
//    {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if( id == R.id.action_settings )
//        {
//            return true;
//        }
//        return super.onOptionsItemSelected( item );
//    }
    
    static Route selectedRoute;
    
    @Override
    public void onItemClick( AdapterView < ? > parent, View view, int position, long id )
    {
        
        if( view != null )
        {
            System.out.println( view.toString() );
            if( view instanceof LinearLayout )
            {
                
                // table.setSelection( position );
                cu = CameraUpdateFactory.newLatLng( markerList.get( position ).getPosition() );
                googleMap.animateCamera( cu );
                markerList.get( position ).showInfoWindow();
                tableSelected = position;
                adapter.notifyDataSetChanged();
            }
            else
            {
                tw = ( TextView ) view;
                lastSelection = position;
                
                if( filtersEnum[position].equals( "PEOPLE" ) )
                {
                    peopleFilter.show( getSupportFragmentManager(), "people" );
                }
                
                if( filtersEnum[position].equals( "TIME" ) )
                {
                    timeFilter.show( getSupportFragmentManager(), "time" );
                }
                
                if( filtersEnum[position].equals( "DISTANCE" ) )
                {
                    distanceFilter.show( getSupportFragmentManager(), "distance" );
                }
                
                if( filtersEnum[position].equals( "FROM" ) )
                {
                    fromFilter.show( getSupportFragmentManager(), "from" );
                }
                
                if( filtersEnum[position].equals( "TO" ) )
                {
                    toFilter.show( getSupportFragmentManager(), "to" );
                }
                
                if( filtersEnum[position].equals( "SORT" ) )
                {
                    sortFilter.show( getSupportFragmentManager(), "sort" );
                }
            }
        }
    }
    
    public void getFeedback( String feedback )
    {
        tw.setText( prefString[lastSelection] + feedback );
        filterResult[lastSelection] = feedback;
        Refresh();
    }
    
    @Override
    public boolean onTouch( View arg0, MotionEvent arg1 )
    {
        if( arg0 == newRoute && arg1.getAction() == MotionEvent.ACTION_UP )
        {
            Intent Intent1 = new Intent( this, EditRoute.class );
            Intent1.putExtra( "mode", 0 );
            // startActivity( Intent1 );
            startActivityForResult( Intent1, 42 );
            
        }
        if( arg0 == seeNotifs && arg1.getAction() == MotionEvent.ACTION_UP )
        {
            Intent Intent1 = new Intent( this, NotifScreen.class );
            startActivityForResult( Intent1, 42 );
        }
        return false;
    }
    
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        
        if( requestCode == 42 )
            Refresh();
    }
    
    @Override
    public void onInfoWindowClick( Marker marker )
    {
        
        for( int i = 0; i < markerList.size(); i++ )
        {
            if( markerList.get( i ).equals( marker ) )
            {
                selectedRoute = newGoodRoutesList.get( i );
                break;
            }
        }
        if( selectedRoute.getUser_id() == userPid )
        {
            Intent Intent1 = new Intent( this, EditRoute.class );
            Intent1.putExtra( "modify", 1 );
            startActivityForResult( Intent1, 42 );
        }
        else if( selectedRoute.getUserStatus() == -1 )
        {
            new AddAttendance().execute();
        }
        else if( selectedRoute.getUserStatus() == 0 )
        {
        }
        else if( selectedRoute.getUserStatus() == 1 )
        {
            new RemoveAttendance().execute();
        }
        else if( selectedRoute.getUserStatus() == 2 )
        {
        }
        
        else if( selectedRoute.getUserStatus() == 3 )
        {
        }
        
    }
    
    class AddAttendance extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( RoutesHub.this );
            pDialog.setMessage( "Dodavanje..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( false );
            pDialog.show();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            int success;
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "rid", Integer.toString( selectedRoute.getId() ) ) );
                params1.add( new BasicNameValuePair( "pid", Integer.toString( userPid ) ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_add_attend, "POST", params1 );
                
                success = json.getInt( TAG_SUCCESS );
                if( success == 1 )
                {
                    // Good
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
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
            
            Refresh();
        }
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
    
    class RemoveAttendance extends AsyncTask < String, String, String >
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( RoutesHub.this );
            pDialog.setMessage( "Brisanje..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( false );
            pDialog.show();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            
            int success;
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "rid", Integer.toString( selectedRoute.getId() ) ) );
                params1.add( new BasicNameValuePair( "pid", Integer.toString( userPid ) ) );
                params1.add( new BasicNameValuePair( "val", Integer.toString( 3 ) ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_modify_status, "POST", params1 );
                
                success = json.getInt( TAG_SUCCESS );
                if( success == 1 )
                {
                    // Good
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
            if( ( pDialog != null ) && pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
            
            Refresh();
        }
    }
    
    
    @Override
    public boolean onMarkerClick( Marker marker )
    {
        marker.showInfoWindow();
        cu = CameraUpdateFactory.newLatLng( marker.getPosition() );
        googleMap.animateCamera( cu );
        for( int i = 0; i < markerList.size(); i++ )
        {
            if( markerList.get( i ).equals( marker ) )
            {
                table.smoothScrollToPosition( i );
                tableSelected = i;
                
                adapter.notifyDataSetChanged();
                
                return true;
            }
        }
        return false;
    }
}