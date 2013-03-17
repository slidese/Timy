package se.slide.timy;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Color;

import java.util.List;

public class ProjectActivity extends FragmentActivity {
    
    ImageView mCheckmark;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        DatabaseManager.init(this);
        
        setContentView(R.layout.project_dialog);
        
        List<Color> colors = DatabaseManager.getInstance().getColors();
        
        /*
        ListView list = (ListView) findViewById(R.id.colorlist);
        list.setAdapter(new ColorAdapter(this, colors));
        */

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ColorAdapter mAdapter = new ColorAdapter(this, colors);
        gridview.setAdapter(mAdapter);
        
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mCheckmark != null)
                    mCheckmark.setVisibility(View.INVISIBLE);
                
                //Toast.makeText(HelloGridView.this, "" + position, Toast.LENGTH_SHORT).show();
                mCheckmark = (ImageView) v.findViewById(R.id.checkmark);
                mCheckmark.setVisibility(View.VISIBLE);
                
            }
        });
       
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                // TODO: If Settings has multiple levels, Up should navigate up
                // that hierarchy.
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public class ColorAdapter extends BaseAdapter {
        private Context mContext;
        private List<Color> mColors;

        public ColorAdapter(Context c, List<Color> colors) {
            mContext = c;
            mColors = colors;
        }

        public int getCount() {
            return mColors.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            //ImageView imageView;
            View view;
            
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                
                view = getLayoutInflater().inflate(R.layout.color_item, null);
                FrameLayout frame = (FrameLayout) view.findViewById(R.id.color);
              
                frame.setBackgroundColor(android.graphics.Color.parseColor(mColors.get(position).getBackgroundColor()));
                
                //view = new TextView(mContext);
                
            } else {
                //imageView = (ImageView) convertView;
                
                view = convertView;
            }

            //imageView.setImageResource(mThumbIds[position]);
            //return imageView;
            
            //view.setText(mColors.get(position).getBackgroundColor());
            
            return view;
        }
        
    }
}
