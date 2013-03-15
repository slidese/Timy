package se.slide.timy;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import se.slide.timy.model.Project;

import java.util.List;

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
            //convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            convertView = mInflater.inflate(R.layout.project_listview_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            //holder.time = (TextView) convertView.findViewById(R.id.alertTime);
            //holder.frameLayout = (FrameLayout) convertView.findViewById(R.id.frameLayout);
            //holder.icon = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
            
            
        } else
            holder = (ViewHolder) convertView.getTag();
        
        holder.name.setText(project.getName());
        
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        animation.setDuration(300);
        animation.setStartOffset(position * 100);
        
        convertView.startAnimation(animation);
        
        return convertView;
    }



    /* private view holder class */
    private class ViewHolder {
        TextView name;
        TextView time;
        FrameLayout frameLayout;
        ImageView icon;
    }
}
