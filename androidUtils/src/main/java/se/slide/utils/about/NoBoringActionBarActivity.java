package se.slide.utils.about;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import se.slide.utils.R;

import java.util.ArrayList;
import java.util.List;

public abstract class NoBoringActionBarActivity extends Activity {

    private static final String TAG = "NoBoringActionBarActivity";
    private int mActionBarTitleColor;
    private int mActionBarHeight;
    private int mHeaderHeight;
    private int mMinHeaderTranslation;
    private ListView mListView;
    protected KenBurnsView mHeaderPicture;
    private ImageView mHeaderLogo;
    private View mHeader;
    private View mPlaceHolderView;
    private AccelerateDecelerateInterpolator mSmoothInterpolator;

    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();

    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
    private SpannableString mSpannableString;

    private TypedValue mTypedValue = new TypedValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSmoothInterpolator = new AccelerateDecelerateInterpolator();
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mMinHeaderTranslation = -mHeaderHeight + getActionBarHeight();

        setContentView(R.layout.activity_noboringactionbar);

        mListView = (ListView) findViewById(R.id.listview);
        mHeader = findViewById(R.id.header);
        mHeaderPicture = (KenBurnsView) findViewById(R.id.header_picture);
        //mHeaderPicture.setResourceIds(R.drawable.picture0, R.drawable.picture1);
        
        mHeaderLogo = (ImageView) findViewById(R.id.header_logo);

        mActionBarTitleColor = getResources().getColor(R.color.actionbar_title_color);

        mSpannableString = new SpannableString(getString(R.string.noboringactionbar_title));
        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(mActionBarTitleColor);

        setupActionBar();
        setupListView();
        setupLogo(mHeaderLogo);
        
        // Lastly, set up the custom views
        ImageView[] imageView = setupKenBurnsView(mHeaderPicture);
        
        mHeaderPicture.setImageView(imageView);
        mHeaderPicture.setResourceIds(getDrawableResources());
    }

    private void setupListView() {
        /*
        ArrayList<String> FAKES = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            FAKES.add("entry " + i);
        }
        */
        
        List<AboutLines> listOfAboutLines = getAboutLines();
        
        AboutLines empty = new AboutLines();
        empty.row1 = "";
        empty.row2 = "";
        listOfAboutLines.add(empty);
        
        mPlaceHolderView = getLayoutInflater().inflate(R.layout.view_header_placeholder, mListView, false);
        mListView.addHeaderView(mPlaceHolderView);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);
        //mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FAKES));
        mListView.setAdapter(new AboutAdapter(this, R.layout.list_item_aboutlines, listOfAboutLines));
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int scrollY = getScrollY();
                //sticky actionbar
                mHeader.setTranslationY(Math.max(-scrollY, mMinHeaderTranslation));
                //header_logo --> actionbar icon
                float ratio = clamp(mHeader.getTranslationY() / mMinHeaderTranslation, 0.0f, 1.0f);
                interpolate(mHeaderLogo, getActionBarIconView(), mSmoothInterpolator.getInterpolation(ratio));
                //actionbar title alpha
                //getActionBarTitleView().setAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
                //---------------------------------
                //better way thanks to @cyrilmottier
                setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
            }
        });
    }
    
    private void setTitleAlpha(float alpha) {
        mAlphaForegroundColorSpan.setAlpha(alpha);
        mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(mSpannableString);
    }

    public static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, min), max);
    }

    private void interpolate(View view1, View view2, float interpolation) {
        getOnScreenRect(mRect1, view1);
        getOnScreenRect(mRect2, view2);

        float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
        float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
        float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
        float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));

        view1.setTranslationX(translationX);
        view1.setTranslationY(translationY - mHeader.getTranslationY());
        view1.setScaleX(scaleX);
        view1.setScaleY(scaleY);
    }

    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    public int getScrollY() {
        View c = mListView.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mPlaceHolderView.getHeight();
        }

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_transparent);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //getActionBarTitleView().setAlpha(0f);
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

    private ImageView getActionBarIconView() {
        return (ImageView) findViewById(android.R.id.home);
    }

    /*
    private TextView getActionBarTitleView() {
        int id = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        return (TextView) findViewById(id);
    }
    */

    public int getActionBarHeight() {
        if (mActionBarHeight != 0) {
            return mActionBarHeight;
        }
        getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
        return mActionBarHeight;
    }
    
    protected abstract List<AboutLines> getAboutLines();
    protected abstract ImageView[] setupKenBurnsView(KenBurnsView mHeaderPicture);
    protected abstract int[] getDrawableResources();
    protected abstract void setupLogo(ImageView logo);
    
    /*
    public List<AboutLines> getAboutLines() {
        ArrayList<AboutLines> FAKES = new ArrayList<AboutLines>();
        
        AboutLines a = new AboutLines();
        a.row1 = "Version";
        a.row2 = "1.4.2";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "By";
        a.row2 = "slide.se";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "Note";
        a.row2 = "I wrote this app when I had my baby boy in early 2013 and wanted a way to keep track of when to feed him :)";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "";
        a.row2 = "";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "android-HoloCircle";
        a.row2 = "http://www.dn.se";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "NotBoringActionBar";
        a.row2 = "https://github.com/flavienlaurent/NotBoringActionBar";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "NotBar";
        a.row2 = "https://github.com/flavienlaurent/NotBoringActionBar";
        FAKES.add(a);
        
        a = new AboutLines();
        a.row1 = "Yeahjo";
        a.row2 = "https://github.com/flavienlaurent/Ydhsjk";
        FAKES.add(a);
                
        return FAKES;
    }
    
    private ImageView[] setupKenBurnsView() {
        View view = View.inflate(this, R.layout.view_kenburns, mHeaderPicture);
        
        ImageView[] mImageViews = new ImageView[2];
        mImageViews[0] = (ImageView) view.findViewById(R.id.image0);
        mImageViews[1] = (ImageView) view.findViewById(R.id.image1);
        
        return mImageViews;
    }
    
    public int[] getDrawableResources() {
        //return new int[] { R.drawable.picture0, R.drawable.picture1 };
        return new int[] { R.drawable.black_wood, R.drawable.black_wood };
    }
    
    public void setupLogo(ImageView logo) {
        logo.setImageResource(R.drawable.ic_launcher_nodpi);
    }
    */
}