package se.slide.timy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
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
        
        String[] ackList = getResources().getStringArray(R.array.ack_list);
        for (String ack : ackList) {
            TextView line = createTextView();
            line.setText(Html.fromHtml(ack));
            layout.addView(line, layout.getChildCount());
        }
        
        
    }

    private TextView createTextView() {
        TextView line = new TextView(this);
        line.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        line.setTextAppearance(this, android.R.attr.textAppearanceMedium);
        line.setPadding(0, 10, 0, 10);
        
        return line;
    }
}
