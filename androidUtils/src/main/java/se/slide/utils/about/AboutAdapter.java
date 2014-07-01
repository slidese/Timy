package se.slide.utils.about;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import se.slide.utils.R;

import java.util.List;

public class AboutAdapter extends ArrayAdapter<AboutLines> {
    
    private LayoutInflater mInflater;

    public AboutAdapter(Context context, int resource, List<AboutLines> objects) {
        super(context, resource, objects);
        
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_aboutlines, null);
            
            holder = new ViewHolder();
            
            holder.row1 = (TextView) convertView.findViewById(R.id.row1);
            holder.row2 = (TextView) convertView.findViewById(R.id.row2);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        AboutLines line = getItem(position);
        
        holder.row1.setText(line.row1);
        holder.row2.setText(line.row2);
        
        return convertView;
    }
    
    public class ViewHolder {
        TextView row1;
        TextView row2;
    }
    
}
