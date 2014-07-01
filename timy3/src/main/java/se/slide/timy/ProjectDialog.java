
package se.slide.timy;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Color;

import java.util.List;

public class ProjectDialog extends DialogFragment implements OnEditorActionListener {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_HINT = "hint";

    public static final int TYPE_PROJECT = 0;
    public static final int TYPE_CATEGORY = 1;

    private String mTitle = "About";

    private EditText mInput;

    private int mType = 0;

    public interface EditNameDialogListener {
        void onFinishEditDialog(String text, int type, int icon);
    }

    public ProjectDialog() {

    }

    public static final ProjectDialog newInstance(String title, int type)
    {
        ProjectDialog fragment = new ProjectDialog();
        Bundle bdl = new Bundle(2);
        bdl.putString(EXTRA_TITLE, title);
        bdl.putInt(EXTRA_HINT, type);
        fragment.setArguments(bdl);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.project_dialog, container);

        Button btnOk = (Button) view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mInput.getText() == null || mInput.getText().length() < 1)
                    return;

                EditNameDialogListener activity = (EditNameDialogListener) getActivity();
                activity.onFinishEditDialog(mInput.getText().toString(), mType, -1);
                dismiss();
            }
        });

        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

            }
        });

        List<Color> colors = DatabaseManager.getInstance().getColors();
        // GridView gridview = (GridView) view.findViewById(R.id.gridview);
        // gridview.setAdapter(new ColorAdapter(getActivity(), colors));

        mTitle = getArguments().getString(EXTRA_TITLE);
        mType = getArguments().getInt(EXTRA_HINT);

        getDialog().setTitle(mTitle);

        mInput = (EditText) view.findViewById(R.id.input);

        if (mType == TYPE_PROJECT)
            mInput.setHint(getString(R.string.hint_add_project));
        else if (mType == TYPE_CATEGORY)
            mInput.setHint(getString(R.string.hint_add_category));

        mInput.setOnEditorActionListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
            activity.onFinishEditDialog(mInput.getText().toString(), mType, -1);
            this.dismiss();
            return true;
        }
        return false;
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
            // ImageView imageView;
            TextView view;

            if (convertView == null) { // if it's not recycled, initialize some
                                       // attributes
                /*
                 * imageView = new ImageView(mContext);
                 * imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                 * imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                 * imageView.setPadding(8, 8, 8, 8);
                 */

                view = new TextView(mContext);

            } else {
                // imageView = (ImageView) convertView;

                view = (TextView) convertView;
            }

            // imageView.setImageResource(mThumbIds[position]);
            // return imageView;

            view.setText(mColors.get(position).getBackgroundColor());

            return view;
        }

    }
}
