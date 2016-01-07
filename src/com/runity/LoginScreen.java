
package com.runity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;

public class LoginScreen extends Activity implements OnTouchListener
{
    
    private ProgressDialog pDialog2;
    
    private static String url_check_loginpass = "http://" + GV.localIP + "/android_connect/profiles/check_loginpass.php";
    
    private static String url_check_alreadyloggedin = "http://" + GV.localIP + "/android_connect/profiles/check_alreadyloggedin.php";
    
    private static String url_setLoggedIn = "http://" + GV.localIP + "/android_connect/profiles/set_loggedin.php";
    
    JSONParser jsonParser = new JSONParser();
    
    private static final String TAG_SUCCESS = "success";
    
    ImageView signUp;
    
    ImageView logIn;
    
    EditText usernameField, passwordField;
    
    String username, password;

    String errorMsg = "";
    
    int pid = -1; // 0th came before the 1st joke makes no sense anymore, I'm lost :/
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView( R.layout.activity_login_screen );
        
        CurrentUserHandler.setContextAndInit( this );
        CurrentUserHandler.readFromDatabase();
        
        // If pid is set
        if( CurrentUserHandler.pid != -1 )
        {
            Intent Intent1 = new Intent( this, MainMenu.class );
            startActivity( Intent1 );
            finish();
        }
       
        signUp = ( ImageView ) findViewById( R.id.login_imageView_signup );
        signUp.setOnTouchListener( this );
        logIn = ( ImageView ) findViewById( R.id.login_imageView_login );
        logIn.setOnTouchListener( this );
        
        usernameField = ( EditText ) findViewById( R.id.login_editText_username );
        passwordField = ( EditText ) findViewById( R.id.login_editText_password );
    }
    
    private boolean areValid( String username, String password )
    {
        try
        {
            String result1 = new checkLoginPass().execute( username, password ).get();
            
            if( result1.equals( "0" ) )
            {
                errorMsg = "Neta\u010Dna kombinacija korisni\u010Dkog imena i lozinke.";
                return false;
            }
            else
            {
                String result2 = new checkAlreadyLoggedIn().execute( username, password ).get();
                if( result2.equals( "1" ) )
                {
                    errorMsg = "Korisnik je ve\u0107 ulogovan.";
                    return false;
                }
                else
                    return true;
                
            }
            
        }
        catch( InterruptedException e )
        {
            e.printStackTrace();
        }
        catch( ExecutionException e )
        {
            e.printStackTrace();
        }
        return true;
    }
    
    class checkAlreadyLoggedIn extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        
        protected String doInBackground( String... args )
        {
            
            try
            {
                // Building Parameters
                List < NameValuePair > params = new ArrayList < NameValuePair >();
                params.add( new BasicNameValuePair( "username", args[0] ) );
                
                JSONObject jsonn = jsonParser.makeHttpRequest( url_check_alreadyloggedin, "GET", params );
                
                int success = jsonn.getInt( TAG_SUCCESS );
                
                if( success == 1 )
                {
                    if( jsonn.getInt( "already" ) == 1 )
                    {
                        return "1";
                    }
                    else
                    {
                        return "0";
                    }
                }
                else
                {
                    return "0";
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
                return "0";
            }
        }
        
        protected void onPostExecute( String file_url )
        {
        }
    }
    

    // Dismiss dialogs when leaving activity
    @Override
    public void onPause()
    {
        super.onPause();
        
        if( ( pDialog2 != null ) && pDialog2.isShowing() )
            pDialog2.dismiss();
        pDialog2 = null;
    }
    
    // Dismiss dialogs
    @Override
    public void onStop()
    {
        super.onStop();
        
        if( ( pDialog2 != null ) && pDialog2.isShowing() )
            pDialog2.dismiss();
        pDialog2 = null;
    }
    
    @SuppressLint( "ClickableViewAccessibility" )
    @Override
    public boolean onTouch( View arg0, MotionEvent arg1 )
    {
        if( arg0.getId() == signUp.getId() )
        {
            if( arg1.getAction() == MotionEvent.ACTION_DOWN )
            {
                Intent Intent1 = new Intent( this, SignUp.class );
                startActivity( Intent1 );
                return true;
            }
        }
        else if( arg0.getId() == logIn.getId() )
        {
            if( arg1.getAction() == MotionEvent.ACTION_UP )
            {
                username = usernameField.getText().toString();
                
                // Use SHA256 encryption
                password = SignUp.SHA256( passwordField.getText().toString() );
                
                if( areValid( username, password ) )
                {
                    new setLoggedIn().execute();
                    CurrentUserHandler.setUserData( pid, username );
                    Intent Intent1 = new Intent( this, MainMenu.class );
                    startActivity( Intent1 );
                    finish();
                }
                else
                {
                    new AlertDialog.Builder( this ).setTitle( "Gre\u0161ka pri ulasku" ).setMessage( errorMsg ).setNeutralButton( android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick( DialogInterface dialog, int which )
                        {
                            // do nothing
                        }
                    } ).setIcon( android.R.drawable.ic_dialog_alert ).show();
                }
                return true;
            }
        }
        return false;
    }
    
    
    class checkLoginPass extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        
        protected String doInBackground( String... args )
        {
            
            try
            {
                // Building Parameters
                List < NameValuePair > params = new ArrayList < NameValuePair >();
                params.add( new BasicNameValuePair( "username", args[0] ) );
                params.add( new BasicNameValuePair( "password", args[1] ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_check_loginpass, "GET", params );
                
                int success = json.getInt( TAG_SUCCESS );
                if( success == 1 )
                {
                    if( json.getInt( "valid" ) == 1 )
                    {
                        pid = json.getInt( "pid" );
                        return "1";
                    }
                    else
                    {
                        return "0";
                    }
                }
                else
                {
                    return "0";
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
                return "0";
            }
        }
        
        protected void onPostExecute( String file_url )
        {
        }
    }
    
    class setLoggedIn extends AsyncTask < String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog2 = new ProgressDialog( LoginScreen.this );
            pDialog2.setMessage( "Potvrda..." );
            pDialog2.setIndeterminate( false );
            pDialog2.setCancelable( true );
            pDialog2.show();
        }
        
        @Override
        protected String doInBackground( String... params )
        {
            
            try
            {
                // Building Parameters
                List < NameValuePair > params1 = new ArrayList < NameValuePair >();
                params1.add( new BasicNameValuePair( "username", username ) );
                
                JSONObject json = jsonParser.makeHttpRequest( url_setLoggedIn, "POST", params1 );
                
                int success = json.getInt( TAG_SUCCESS );
                if( success == 1 )
                {
                    return null;
                }
                else
                {
                    return null;
                }
            }
            catch( JSONException e )
            {
                e.printStackTrace();
                return null;
            }
        }
        
        protected void onPostExecute( String file_url )
        {
            if( ( pDialog2 != null ) && pDialog2.isShowing() )
            {
                pDialog2.dismiss();
            }
        }
    }
}
