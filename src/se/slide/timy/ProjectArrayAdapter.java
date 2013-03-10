package se.slide.timy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import se.slide.timy.model.Project;

public class ProjectArrayAdapter extends ArrayAdapter<Project> {
    Context context;

    public ProjectArrayAdapter(Context context, int textViewResourceId, List<Project> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }
    
    

    /* (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Project project = getItem(position);
        
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);

            holder = new ViewHolder();
            holder.message = (TextView) convertView.findViewById(android.R.id.text1);
            //holder.time = (TextView) convertView.findViewById(R.id.alertTime);
            //holder.frameLayout = (FrameLayout) convertView.findViewById(R.id.frameLayout);
            //holder.icon = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        
        holder.message.setText(project.getName());
        
        return convertView;
    }



    /* private view holder class */
    private class ViewHolder {
        TextView message;
        TextView time;
        FrameLayout frameLayout;
        ImageView icon;
    }
}
