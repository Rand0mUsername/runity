
package com.runity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

public class SplashScreen extends Activity
{
    private ImageView imageview;
    
    private int SPL_SCR_VIEW_TIME = 3500;
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash_screen );
        
        imageview = ( ImageView ) findViewById( R.id.splashScreen_imageView_placeholder );
        
        Ion.with( imageview ).animateGif( AnimateGifMode.ANIMATE_ONCE ).load( "android.resource://" + getPackageName() + "/" + R.raw.splash_runity );
        
        Handler handler = new Handler();
        final Context mContext = this;
        Runnable r = new Runnable()
        {
            public void run()
            {
                Intent Intent1 = new Intent( mContext, LoginScreen.class );
                startActivity( Intent1 );
                finish();
            }
        };
        
        handler.postDelayed( r, SPL_SCR_VIEW_TIME );
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        imageview = null;
        System.gc();
    }
    
    
}
