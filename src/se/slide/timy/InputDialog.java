package se.slide.timy;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class InputDialog extends DialogFragment implements OnEditorActionListener {
    
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";
    
    public static final int TYPE_PROJECT = 0;
    public static final int TYPE_CATEGORY = 1;
    
    private String mTitle = "About";
    
    private EditText mInput;
    
    private int mCategoryId = 0;
    private String mCategoryName = "";

    public interface EditNameDialogListener {
        void onFinishEditDialog(String text, int categoryId, int icon);
    }
    
    public InputDialog() {
        
    }
    
    public static final InputDialog newInstance(String title, int categoryId, String categoryName)
    {
        InputDialog fragment = new InputDialog();
        Bundle bdl = new Bundle(2);
        bdl.putString(EXTRA_TITLE, title);
        bdl.putInt(EXTRA_CATEGORY_ID, categoryId);
        bdl.putString(EXTRA_CATEGORY_NAME, categoryName);
        fragment.setArguments(bdl);
        
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.input_dialog, container);
        
        Button btnOk = (Button) view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mInput.getText() == null || mInput.getText().toString().trim().length() < 1)
                    return;
                
                EditNameDialogListener activity = (EditNameDialogListener) getActivity();
                activity.onFinishEditDialog(mInput.getText().toString(), mCategoryId, -1);
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
        
        mTitle = getArguments().getString(EXTRA_TITLE);
        mCategoryId = getArguments().getInt(EXTRA_CATEGORY_ID);
        mCategoryName = getArguments().getString(EXTRA_CATEGORY_NAME);
        
        getDialog().setTitle(mTitle);
        
        mInput = (EditText) view.findViewById(R.id.input);
        mInput.setHint(getString(R.string.hint_add_category));
        mInput.setOnEditorActionListener(this);
        
        if (mCategoryName != null && mCategoryName.length() > 0)
            mInput.setText(mCategoryName);
        
        return view;
    }
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
            activity.onFinishEditDialog(mInput.getText().toString(), mCategoryId, -1);
            this.dismiss();
            return true;
        }
        return false;
    }

}
