
package com.runity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint( "ClickableViewAccessibility" )
public class SignUp extends Activity implements OnTouchListener
{
    ImageView button_signUp;
    
    EditText et_username, et_name, et_surname, et_email, et_password, et_birthyear;
    
    //Spinner spinner_currentcity;
    
    String username, name, surname, email, password;
    
    int birthyear;
    
    String current_city;
    
    List < String > errors = new ArrayList < String >();
    
    UserDataValidator validator = new UserDataValidator();
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        validator.getContext( getApplicationContext() );
        super.onCreate( savedInstanceState );
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView( R.layout.activity_signup );
        /*
        String[] array_spinner = new String[7];
        array_spinner[0] = "Beograd";
        array_spinner[1] = "Novi Sad";
        array_spinner[2] = "Nis";
        array_spinner[3] = "Kragujevac";
        array_spinner[4] = "Cacak";
        array_spinner[5] = "Subotica";
        array_spinner[6] = "Other";
        spinner_currentcity = ( Spinner ) findViewById( R.id.signup_spinner_currentcity );
        ArrayAdapter < Object > adapter = new ArrayAdapter < Object >( this, android.R.layout.simple_spinner_item, array_spinner );
        spinner_currentcity.setAdapter( adapter );
        */
        // register all other UI elements
        button_signUp = ( ImageView ) findViewById( R.id.signup_imageView_signup );
        button_signUp.setOnTouchListener( this );
        
        et_username = ( EditText ) findViewById( R.id.signup_editText_username );
        et_name = ( EditText ) findViewById( R.id.signup_editText_name );
        et_surname = ( EditText ) findViewById( R.id.signup_editText_surname );
        et_email = ( EditText ) findViewById( R.id.signup_editText_email );
        et_password = ( EditText ) findViewById( R.id.signup_editText_password );
        et_birthyear = ( EditText ) findViewById( R.id.signup_editText_birthyear );
    }
    
    
    public static String bytesToHex( byte[] bytes )
    {
        StringBuffer result = new StringBuffer();
        for( byte byt : bytes )
            result.append( Integer.toString( ( byt & 0xff ) + 0x100, 16 ).substring( 1 ) );
        return result.toString();
    }
    
    public static String SHA256( String password )
    {
        MessageDigest md = null;
        
        try
        {
            md = MessageDigest.getInstance( "SHA-256" );
        }
        catch( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
        
        try
        {
            md.update( password.getBytes( "UTF-8" ) );
        }
        catch( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        
        
        byte[] digest = md.digest();
        String hashVal = bytesToHex( digest );
        
        return hashVal;
    }
    
    @SuppressWarnings( { "unused" } )
    @Override
    public boolean onTouch( View arg0, MotionEvent arg1 )
    {
        if( arg0.getId() == button_signUp.getId() )
        {
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                username = et_username.getText().toString();
                name = et_name.getText().toString();
                surname = et_surname.getText().toString();
                email = et_email.getText().toString();
                password = et_password.getText().toString();
                try
                {
                    Date date = new Date();
                    birthyear = Integer.parseInt( et_birthyear.getText().toString() );
                }
                catch( Exception NumberFormatException )
                {
                    birthyear = 0;
                }
                
                current_city = "Beograd"; //We don't really need cities at this point
                
                errors = validator.validate( username, name, surname, email, password, birthyear, current_city );
                
                password = SHA256( password );
                
                if( errors.isEmpty() )
                {
                    new CreateNewProfile().execute();
                }
                else
                {
                    StringBuilder userErrorLog = new StringBuilder();
                    for( String err : errors )
                    {
                        userErrorLog.append( err );
                    }
                    new AlertDialog.Builder( this ).setTitle( "Gre\u0161ka pri registraciji" ).setMessage( userErrorLog.toString() ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int which )
                        {
                            // do nothing
                        }
                    } ).setIcon( android.R.drawable.ic_dialog_alert ).show();
                }
            }
        }
        return false;
    }
    
    private ProgressDialog pDialog;
    
    JSONParser jsonParser = new JSONParser();
    
    private static String url_create_product = "http://" + GV.localIP + "/android_connect/profiles/create_profile.php";
    
    // JSON Tag
    private static final String TAG_SUCCESS = "success";
    
    class CreateNewProfile extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog( SignUp.this );
            pDialog.setMessage( "Kreiranje naloga..." );
            pDialog.setIndeterminate( false );
            pDialog.setCancelable( true );
            pDialog.show();
        }
        
        protected String doInBackground( String... args )
        {
            // Building Parameters
            List < NameValuePair > params = new ArrayList < NameValuePair >();
            params.add( new BasicNameValuePair( "username", username ) );
            params.add( new BasicNameValuePair( "name", name ) );
            params.add( new BasicNameValuePair( "surname", surname ) );
            params.add( new BasicNameValuePair( "email", email ) );
            params.add( new BasicNameValuePair( "password", password ) );
            params.add( new BasicNameValuePair( "birthyear", Integer.toString( birthyear ) ) );
            params.add( new BasicNameValuePair( "current_city", current_city ) );
            
            // Get JSON
            Log.d( "NESTO", params.toString() );
            
            JSONObject json = jsonParser.makeHttpRequest( url_create_product, "POST", params );
            
            try
            {
                int success = json.getInt( TAG_SUCCESS );
                
                if( success == 1 )
                {
                    Intent i = new Intent( getApplicationContext(), LoginScreen.class );
                    startActivity( i );
                    
                    finish();
                }
                else
                {
                    Toast.makeText( getBaseContext(), "Greška", Toast.LENGTH_SHORT ).show();
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
            finish();
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
}
