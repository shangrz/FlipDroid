package com.goal98.flipdroid.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import com.goal98.flipdroid.R;
import com.goal98.flipdroid.anim.AnimationFactory;
import com.goal98.flipdroid.exception.NoMorePageException;
import com.goal98.flipdroid.model.ContentRepo;
import com.goal98.flipdroid.model.FakeArticleSource;
import com.goal98.flipdroid.model.Page;
import com.goal98.flipdroid.model.SimplePagingStrategy;
import com.goal98.flipdroid.model.sina.SinaArticleSource;
import com.goal98.flipdroid.util.GestureUtil;
import com.goal98.flipdroid.view.PageView;

public class PageActivity extends Activity {

    static final private int CONFIG_ID = Menu.FIRST;

    private boolean animationStarted = false;
    private boolean mToggleIndeterminate = false;
    private boolean forward = false;

    private ViewGroup container;
    private View current;
    private View next;

    private ContentRepo repo;
    private SharedPreferences preferences;

    private SimplePagingStrategy simplePagingStrategy;

    private String deviceId;
    private int currentPageIndex = -1;
    private Page currentPage;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        TelephonyManager tManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tManager.getDeviceId();

        String repoStr = (String) getIntent().getExtras().get("repo");

        setContentView(R.layout.main);

        container = (ViewGroup) findViewById(R.id.pageContainer);
        current = findViewById(R.id.currentPage);
        next = findViewById(R.id.nextPage);

        repo = new ContentRepo();

        String userId = "13774256612";
        String password = "541116";
        String sourceUserId = null;

        if ("weibo".equals(repoStr)) {
            sourceUserId = null;
            repo.setArticleSource(new SinaArticleSource(false, userId, password, sourceUserId));
        } else if ("helianbobo".equals(repoStr)) {
            sourceUserId = "1702755335";
            repo.setArticleSource(new SinaArticleSource(false, userId, password, sourceUserId));
        } else if ("fake".equals(repoStr)) {
            repo.setArticleSource(new FakeArticleSource());

        }
        simplePagingStrategy = new SimplePagingStrategy();
        repo.setPagingStrategy(simplePagingStrategy);
    }

    @Override
    protected void onStart() {
        super.onStart();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        simplePagingStrategy.setArticlePerPage(getArticlePerPageFromPreference());

        flipPage(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (event.getHistorySize() > 0 && !animationStarted) {
                    if (GestureUtil.flipRight(event))
                        flipPage(false);
                    else if (GestureUtil.flipLeft(event))
                        flipPage(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (deviceId.startsWith("0000")) {
                    float middle = container.getWidth() / 2.0f;
                    boolean rightHalf = event.getX() > middle;
                    if (rightHalf) {
                        flipPage(true);
                    } else {
                        flipPage(false);
                    }
                }
                break;
            default:
                break;
        }
        return true;

    }

    private void flipPage(final boolean forward) {

        this.forward = forward;

        if (!forward)
            decreasePageNo();

        if (currentPageIndex + 1 >= 0) {

            try {
                currentPage = repo.getPage(currentPageIndex + 1);
                processCurrentPage();
            } catch (NoMorePageException e) {
                new FetchRepoTask().execute(currentPageIndex + 1);
            }


        } else {
            currentPageIndex++;
            startActivity(new Intent(this, IndexActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, R.anim.fade);
        }
    }

    private void decreasePageNo() {
        currentPageIndex--;
    }

    private Animation buildAnimation(final boolean forward) {
        final float centerY = container.getHeight() / 2.0f;
        final float centerX = 0;
        long duration = getFlipDurationFromPreference();

        AnimationFactory animationFactory = new AnimationFactory();
        Animation rotation = animationFactory.buildFlipAnimation(forward, duration, centerX, centerY);

        rotation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                current.setVisibility(View.GONE);
                animationStarted = false;

                switchViews(forward);

                current.setAnimation(null);
                next.setAnimation(null);
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });

        return rotation;
    }

    private long getFlipDurationFromPreference() {
        String key = getString(R.string.key_anim_flip_duration_preference);
        return Long.parseLong(preferences.getString(key, "500"));
    }

    private int getArticlePerPageFromPreference() {
        String key = getString(R.string.key_article_per_page_preference);
        return Integer.parseInt(preferences.getString(key, "5"));
    }

    private void switchViews(boolean forward) {
        View tmp = current;
        current = next;
        next = tmp;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, CONFIG_ID, 0, R.string.config);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONFIG_ID:
                startActivity(new Intent(this, ConfigActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void noMorePage() {
        //TODO: Popup "No More Page" notification

    }

    private class FetchRepoTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        protected Integer doInBackground(Integer... integers) {

            repo.refresh();
            return integers[0];
        }

        protected void onPostExecute(Integer pageIndex) {

            try {
                currentPage = repo.getPage(pageIndex);
            } catch (NoMorePageException e1) {
                noMorePage();
            }

            processCurrentPage();
            setProgressBarIndeterminateVisibility(false);
        }


    }

    private void processCurrentPage() {
        container.removeAllViews();

        next.setVisibility(View.VISIBLE);

        if (currentPage != null) {
            ((PageView) next).setPage(currentPage);

            Animation rotation = buildAnimation(forward);

            if (forward) {
                currentPageIndex++;
                container.addView(next);
                container.addView(current);
                current.startAnimation(rotation);
            } else {
                decreasePageNo();
                container.addView(current);
                container.addView(next);
                next.startAnimation(rotation);
            }

            animationStarted = true;
        }
    }

}
