
package se.slide.timy;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AcknowledgementsActivity extends ListActivity {

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        String[] listOfAcks = getResources().getStringArray(R.array.ack_list);

        setListAdapter(new ArrayAdapter<String>(this, R.layout.ack_list_item, listOfAcks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;

                if (null == convertView) {
                    row = getLayoutInflater().inflate(R.layout.ack_list_item, null);
                } else {
                    row = convertView;
                }

                TextView tv = (TextView) row.findViewById(R.id.ack_message_text);
                tv.setText(Html.fromHtml(getItem(position)));
                tv.setAutoLinkMask(Linkify.WEB_URLS);

                return row;
            }
        });

    }
}
