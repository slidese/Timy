package se.slide.timy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AcknowledgementsActivity extends FragmentActivity {

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        setContentView(R.layout.activity_acknowledgements);
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.acknowledgements);
        
        TextView line;
        line = new TextView(this);
        line.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        line.setTextAppearance(this, android.R.attr.textAppearanceMedium);
        
        // Loop string array with HTML formatted strings
        
        line.setText("Used frame work version 1.0");
        
        layout.addView(line);
    }

}
