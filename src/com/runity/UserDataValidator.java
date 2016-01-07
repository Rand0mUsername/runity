
package com.runity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.os.AsyncTask;

public class UserDataValidator
{
    private static String url_data_exists = "http://" + GV.localIP + "/android_connect/profiles/data_exists.php";
    
    JSONParser jsonParser = new JSONParser();
    
    private static final String TAG_SUCCESS = "success";
    
    Context context;
    
    void getContext( Context Context )
    {
        context = Context.getApplicationContext();
    }
    
    private boolean checkExists( String column, String username )
    {
        try
        {
            String result = new asyncCheckExists().execute( column, username ).get();
            if( result.equals( "0" ) )
                return false;
            return true;
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
    
    class asyncCheckExists extends AsyncTask < String, String, String >
    {
        protected String doInBackground( String... args )
        {
            
            try
            {
                // Building Parameters
                List < NameValuePair > params = new ArrayList < NameValuePair >();
                params.add( new BasicNameValuePair( "data", args[1] ) );
                params.add( new BasicNameValuePair( "column", args[0] ) );
               
                JSONObject json = jsonParser.makeHttpRequest( url_data_exists, "GET", params );
                
                int success = json.getInt( TAG_SUCCESS );
                if( success == 1 )
                {
                    return Integer.toString( json.getInt( "exists" ) );
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
    }
    
    
    public List < String > validate( String username, String name, String surname, String email, String password, int birthyear, String current_city )
    {
        List < String > errors = new ArrayList < String >();
        
        if( username.length() < 6 )
            errors.add( "Username kraći od 6 karaktera.\n" );
        else if( Character.isDigit( username.charAt( 0 ) ) )
            errors.add( "Username ne sme početi brojem.\n" );
        
        if( birthyear < 1900 || birthyear > 2014 )
            errors.add( "Unesite tačan broj.\n" );
        
        if( name.length() < 2 )
            errors.add( "Ime prekratko.\n" );
        if( surname.length() < 2 )
            errors.add( "Prezime prekratko.\n" );
        
        if( password.length() < 8 )
            errors.add( "Password prekratak.\n" );
        if( !password.matches( ".*[a-zA-Z].*" ) )
            errors.add( "Password mora sadržati barem jedno slovo.\n" );
        if( !password.matches( ".*[0-9].*" ) )
            errors.add( "Password mora sadržati barem jedan broj.\n" );
        
        if( !email.matches( "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,5}$" ) )
            errors.add( "Email adresa netačna.\n" );
        else if( checkExists( "email", email ) )
            errors.add( "Email adresa već registrovana.\n" );
        
        if( checkExists( "username", username ) )
            errors.add( "Username već postoji.\n" );
        
        return errors;
    }
}
