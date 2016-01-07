
package com.runity;

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RouteAdapter extends BaseAdapter
{
    Context context;
    
    protected List < Route > listRoutes;
    
    LayoutInflater inflater;
    
    public RouteAdapter( Context context, List < Route > listRoutes )
    {
        this.listRoutes = listRoutes;
        this.inflater = LayoutInflater.from( context );
        this.context = context;
    }
    
    public int getCount()
    {
        return listRoutes.size();
    }
    
    public Route getItem( int position )
    {
        return listRoutes.get( position );
    }
    
    public View getView( int position, View convertView, ViewGroup parent )
    {
        ViewHolder holder;
        if( convertView == null )
        {
            
            holder = new ViewHolder();
            convertView = this.inflater.inflate( R.layout.layout_route, parent, false );
            
            holder.txtDateTime = ( TextView ) convertView.findViewById( R.id.txt_route_datetime );
            holder.txtDistance = ( TextView ) convertView.findViewById( R.id.txt_route_distance );
            holder.txtRunner = ( TextView ) convertView.findViewById( R.id.txt_route_runner );
            holder.txtAttend = ( TextView ) convertView.findViewById( R.id.txt_route_attend );
            
            convertView.setTag( holder );
        }
        else
        {
            holder = ( ViewHolder ) convertView.getTag();
        }
        
        Route route = listRoutes.get( position );
        holder.txtDateTime.setText( route.getDateTime() );
        
        if( route.dist < 10 )
            holder.txtDistance.setText( Integer.toString( ( int ) ( route.dist * 1000 ) ) + "m od mene." );
        else
            holder.txtDistance.setText( String.format( "%.0f", route.dist ) + "km od mene." ); // Double.toString( route.dist ) + "km od mene." );
            
            
        holder.txtRunner.setText( route.getusername() );
        
        holder.txtAttend.setText( Integer.toString( route.getAttsSize() ) );
        
        if( position == RoutesHub.tableSelected )
        {
            convertView.setBackgroundColor( Color.LTGRAY );
        }
        else
        {
            convertView.setBackgroundColor( Color.DKGRAY ); // Two shades of gray
        }
        
        return convertView;
    }
    
    private class ViewHolder
    {
        TextView txtDateTime;
        
        TextView txtDistance;
        
        TextView txtRunner;
        
        TextView txtAttend;
        
    }
    
    @Override
    public long getItemId( int arg0 )
    {
        return 0;
    }
}
