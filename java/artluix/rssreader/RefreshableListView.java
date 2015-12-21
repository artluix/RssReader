package artluix.rssreader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class RefreshableListView extends ListView implements AbsListView.OnScrollListener {
    private final int HEADER_HEIGHT = 60;
    private final int HEADER_TOP = 10;
    private final int STATE_PULL_TO_REFRESH = 0;
    private final int STATE_RELEASE_TO_UPDATE = 1;
    private ImageView arrowImage;
    private int currentState;
    private float deltaY;
    private LinearLayout headerRelativeLayout;
    private TextView headerTextView;
    private boolean isLoading;
    private boolean isLoadingMore;
    private TextView lastUpdateDateTextView;
    private ProgressBar progressBar;
    private RefreshableInterface refreshDelegate;
    private RotateAnimation reverseRotateAnimation;
    private RotateAnimation rotateAnimation;
    private float startY;


    public RefreshableListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        headerRelativeLayout = ((LinearLayout)inflate(context, R.layout.refresh_header_view, null));
        arrowImage = ((ImageView)headerRelativeLayout.findViewById(R.id.head_arrowImageView));
        progressBar = ((ProgressBar)headerRelativeLayout.findViewById(R.id.head_progressBar));
        headerTextView = ((TextView)headerRelativeLayout.findViewById(R.id.head_tipsTextView));
        headerTextView.setText(R.string.pull_to_refresh);
        lastUpdateDateTextView = ((TextView)headerRelativeLayout.findViewById(R.id.head_lastUpdatedDateTextView));
        lastUpdateDateTextView.setText("");
        headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), -1 * HEADER_HEIGHT, 0, headerRelativeLayout.getPaddingBottom());
        addHeaderView(headerRelativeLayout, null, false);
        isLoadingMore = false;
        currentState = STATE_PULL_TO_REFRESH;
        setOnScrollListener(this);
        rotateAnimation = new RotateAnimation(0.0f, -180f, 1, 0.5f, 1, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(250);
        rotateAnimation.setFillAfter(true);
        reverseRotateAnimation = new RotateAnimation(-180.0f, 0.0f, 1, 0.5f, 1, 0.5f);
        reverseRotateAnimation.setInterpolator(new LinearInterpolator());
        reverseRotateAnimation.setDuration(1);
        reverseRotateAnimation.setFillAfter(true);
    }

    public void onLoadingMoreComplete() {
        isLoadingMore = false;
    }

    public void onRefreshComplete() {
        progressBar.setVisibility(View.GONE);
        arrowImage.setVisibility(View.VISIBLE);
        arrowImage.startAnimation(reverseRotateAnimation);
        headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), -1 * HEADER_HEIGHT, 0, headerRelativeLayout.getPaddingBottom());
        String str = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date());
        lastUpdateDateTextView.setText("Last Updated: " + str);
        isLoading = false;
    }

    public void onRefreshStart() {
        headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), HEADER_TOP, 0, headerRelativeLayout.getPaddingBottom());
        headerTextView.setText(R.string.loading);
        progressBar.setVisibility(View.VISIBLE);
        arrowImage.setVisibility(View.GONE);
        isLoading = true;
        if (refreshDelegate != null) {
            refreshDelegate.startFresh();
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLoading) {
                    break;
                }
                deltaY = ev.getY() - startY;
                headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), -1 * HEADER_HEIGHT + (int)deltaY, 0, headerRelativeLayout.getPaddingBottom());
                if (headerRelativeLayout.getPaddingTop() >= HEADER_HEIGHT && currentState == STATE_PULL_TO_REFRESH) {
                    currentState = STATE_RELEASE_TO_UPDATE;
                    arrowImage.clearAnimation();
                    arrowImage.startAnimation(rotateAnimation);
                    headerTextView.setText(R.string.release_to_refresh);
                    break;
                }
                if (headerRelativeLayout.getPaddingTop() < HEADER_HEIGHT && currentState == STATE_RELEASE_TO_UPDATE) {
                    currentState = STATE_PULL_TO_REFRESH;
                    arrowImage.clearAnimation();
                    arrowImage.startAnimation(reverseRotateAnimation);
                    headerTextView.setText(R.string.pull_to_refresh);
                    break;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isLoading) {
                    break;
                }
                if (headerRelativeLayout.getPaddingTop() < HEADER_HEIGHT) {
                    headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), -1 * HEADER_HEIGHT, 0, headerRelativeLayout.getPaddingBottom());
                    break;
                }
                headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), HEADER_TOP, 0, headerRelativeLayout.getPaddingBottom());
                headerTextView.setText(R.string.loading);
                progressBar.setVisibility(View.VISIBLE);
                arrowImage.clearAnimation();
                arrowImage.setVisibility(View.GONE);
                isLoading = true;
                if (refreshDelegate != null) {
                    refreshDelegate.startFresh();
                    break;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setOnRefresh(RefreshableInterface refreshableInterface) {
        refreshDelegate = refreshableInterface;
    }
}
