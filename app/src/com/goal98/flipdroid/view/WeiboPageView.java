package com.goal98.flipdroid.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.goal98.android.WebImageView;
import com.goal98.flipdroid.R;
import com.goal98.flipdroid.activity.FlipdroidApplications;
import com.goal98.flipdroid.activity.PageActivity;
import com.goal98.flipdroid.activity.SinaAccountActivity;
import com.goal98.flipdroid.exception.NoSinaAccountBindedException;
import com.goal98.flipdroid.model.Article;
import com.goal98.flipdroid.util.Constants;
import com.goal98.flipdroid.util.DeviceInfo;
import com.goal98.flipdroid.util.StopWatch;
import weibo4j.WeiboException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class WeiboPageView extends FrameLayout {
    private Page page;

    protected LinearLayout frame;
    protected ExecutorService executor;
    private Animation fadeinArticle;
    private Animation fadeinBoard;
    private LinearLayout content;
    private LayoutInflater inflater;
    protected DeviceInfo deviceInfo;
    private ExpandableArticleView clickedArticleView;

    public LinearLayout getContentLayout() {
        return contentLayout;
    }

    protected LinearLayout contentLayout; //this layout is supposed to be dynamic, depending on the Articles on this smartPage

    protected WeakReference<LinearLayout> enlargedViewWrapperWr;
    //    protected LinearLayout enlargedViewWrapper;
    //    protected ScrollView wrapper;
    protected String sourceName;
    protected String sourceImageURL;
    protected PageActivity pageActivity;
    private LinearLayout wrapperll;
    private LinearLayout loadingView;
    private LinearLayout commentShadowLayer;

    public WebImageView headerImage;


    private boolean rendered;

    public boolean isRendered() {
        return rendered;
    }


    protected boolean loadingNext;

    public void setPage(Page page) {
        this.page = page;

        this.contentLayout.removeAllViews();
        List<Article> articleList = this.page.getArticleList();
        //System.out.println("articleList:" + articleList.size());

        int heightSum = 0;
        for (int i = 0; i < articleList.size(); i++) {
            heightSum += articleList.get(i).getHeight();
        }

        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            boolean isLastArticle = i == articleList.size() - 1;
            article.setHeight((int) ((article.getHeight() * deviceInfo.getDisplayHeight()) / (float) heightSum));
            addArticleView(article, isLastArticle);
        }
    }

    public DeviceInfo getDeviceInfoFromApplicationContext() {
        return DeviceInfo.getInstance((Activity) this.getContext());
    }

    public List<ArticleView> getWeiboViews() {
        return weiboViews;
    }

    public List<LinearLayout> getWrapperViews() {
        return wrapperViews;
    }

    protected List<ArticleView> weiboViews = new ArrayList<ArticleView>();
    protected List<LinearLayout> wrapperViews = new ArrayList<LinearLayout>();

    public LinearLayout addArticleView(Article article, boolean last) {
        WeiboArticleView withoutURLArticleView = new WeiboArticleView(WeiboPageView.this.getContext(), article, this, last, executor);
        weiboViews.add(withoutURLArticleView);


        LinearLayout articleWrapper = new LinearLayout(this.getContext());
        wrapperViews.add(articleWrapper);
        articleWrapper.setBackgroundColor(Constants.LINE_COLOR);//分割线颜色
        articleWrapper.setGravity(Gravity.TOP);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        articleWrapper.addView(withoutURLArticleView, layoutParams);

        LayoutParams wrapperLayoutParam = new LayoutParams(deviceInfo.getWidth(), article.getHeight());

        if (last)//分割线
            articleWrapper.setPadding(0, 0, 0, 0);
        else
            articleWrapper.setPadding(0, 0, 0, 1);

        contentLayout.addView(articleWrapper, wrapperLayoutParam);
        return articleWrapper;
    }

    public WeiboPageView(final PageActivity pageActivity) {
        super(pageActivity);
        this.pageActivity = pageActivity;
        this.sourceName = pageActivity.getSourceName();
        this.sourceImageURL = pageActivity.getSourceImageURL();
        this.executor = pageActivity.getExecutor();
        this.deviceInfo = this.getDeviceInfoFromApplicationContext();
        setDynamicLayout(pageActivity);

        fadeinArticle = AnimationUtils.loadAnimation(this.getContext(), R.anim.fadein);
        fadeinBoard = AnimationUtils.loadAnimation(this.getContext(), R.anim.fadein);

        fadeinArticle.setDuration(400);
        fadeinBoard.setDuration(450);

        fadeinBoard.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                content.setVisibility(VISIBLE);
                articleView.startAnimation(fadeinArticle);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });


        fadeinArticle.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                articleView.setVisibility(VISIBLE);
                WeiboPageView.this.pageActivity.setEnlargedMode(true);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    protected void setDynamicLayout(Context context) {
        this.removeAllViews();

        inflater = LayoutInflater.from(context);

        this.frame = (LinearLayout) inflater.inflate(R.layout.pageview, null);
        this.contentLayout = (LinearLayout) frame.findViewById(R.id.content);
        this.addView(this.frame, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }

    private ArticleView articleView;

    public void enlarge(final ArticleView articleView, final ExpandableArticleView weiboArticleView) {
        StopWatch sw = new StopWatch();
        sw.start("enlarge");
        this.articleView = articleView;
        this.clickedArticleView = weiboArticleView;
        inflater = LayoutInflater.from(WeiboPageView.this.getContext());
        if (enlargedViewWrapperWr == null || enlargedViewWrapperWr.get() == null) {
            enlargedViewWrapperWr = new WeakReference(inflater.inflate(R.layout.enlarged, null));

            wrapperll = (LinearLayout) (enlargedViewWrapperWr.get().findViewById(R.id.wrapperll));

            LinearLayout shadowlayer = (LinearLayout) enlargedViewWrapperWr.get().findViewById(R.id.shadowlayer);
            shadowlayer.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!weiboArticleView.isLoading)
                        closeEnlargedView(weiboArticleView);
                }
            });

        }
        enlargedViewWrapperWr.get().setVisibility(VISIBLE);
        wrapperll.removeAllViews();
        wrapperll.addView(articleView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        articleView.setVisibility(INVISIBLE);
        WeiboPageView.this.removeView(enlargedViewWrapperWr.get());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(deviceInfo.getWidth(), deviceInfo.getHeight());
        WeiboPageView.this.addView(enlargedViewWrapperWr.get(), params);
        sw.stopPrintReset();
        sw.start("enlarge till tool bar");
        setupToolBar(articleView, weiboArticleView, pageActivity.getHeader());
        sw.stopPrintReset();
        content = (LinearLayout) enlargedViewWrapperWr.get().findViewById(R.id.content);
        final LinearLayout contentWrapper = (LinearLayout) enlargedViewWrapperWr.get().findViewById(R.id.contentWrapper);

        content.setVisibility(INVISIBLE);


        contentWrapper.startAnimation(fadeinBoard);

    }

    private void setupToolBar(final ArticleView articleView, final ExpandableArticleView weiboArticleView, HeaderView header) {
        ImageView closeButton = (ImageView) header.findViewById(R.id.close);

        closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                closeEnlargedView(weiboArticleView);
            }
        });

        ImageView retweetButton = (ImageView) header.findViewById(R.id.retweet);

        retweetButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!pageActivity.sinaAlreadyBinded()) {
                    pageActivity.showDialog(PageActivity.PROMPT_OAUTH);
                    return;
                }

                commentShadowLayer = new LinearLayout(WeiboPageView.this.getContext());
                commentShadowLayer.setBackgroundColor(Color.parseColor("#77000000"));
                commentShadowLayer.setPadding(14, 20, 14, 20);
                LinearLayout commentPad = (LinearLayout) inflater.inflate(R.layout.comment_pad, null);
                WebImageView sourceImage = (WebImageView) commentPad.findViewById(R.id.source_image);
                TextView sourceName = (TextView) commentPad.findViewById(R.id.source_name);
                ImageView closeBtn = (ImageView) commentPad.findViewById(R.id.close);
                ImageView sendBtn = (ImageView) commentPad.findViewById(R.id.send);
                closeBtn.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        WeiboPageView.this.removeView(commentShadowLayer);
                    }
                });

                TextView status = (TextView) commentPad.findViewById(R.id.status);
                final EditText commentEditText = (EditText) commentPad.findViewById(R.id.comment);
                commentEditText.addTextChangedListener(new TextWatcher() {
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    public void afterTextChanged(Editable s) {

                    }
                });

                sendBtn.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        if (articleView.getArticle().getSourceType().equals(Constants.TYPE_SINA_WEIBO)) {
                            try {
                                pageActivity.comment(commentEditText.getText().toString(), articleView.getArticle().getStatusId());
                            } catch (WeiboException e) {
                                e.printStackTrace();
                            } catch (NoSinaAccountBindedException e) {
                                pageActivity.startActivity(new Intent(pageActivity, SinaAccountActivity.class));
                            }
                        } else {
                            try {
                                pageActivity.forward(commentEditText.getText().toString(), articleView.getArticle().extractURL());
                            } catch (WeiboException e) {
                                e.printStackTrace();
                            } catch (NoSinaAccountBindedException e) {
                                pageActivity.startActivity(new Intent(pageActivity, SinaAccountActivity.class));
                            }
                        }

                        WeiboPageView.this.removeView(commentShadowLayer);
                    }
                });
                if (articleView.getArticle().getPortraitImageUrl() != null) {
                    sourceImage.setImageUrl(articleView.getArticle().getPortraitImageUrl().toExternalForm());
                    sourceImage.loadImage();
                }

                sourceName.setText(articleView.getArticle().getAuthor());
                status.setText(articleView.getArticle().getStatus());

                commentShadowLayer.addView(commentPad, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                WeiboPageView.this.addView(commentShadowLayer);
            }
        });
    }

    private void closeEnlargedView(ExpandableArticleView weiboArticleView) {
        if (commentShadowLayer != null)
            this.removeView(commentShadowLayer);

        weiboArticleView.getContentView().setVisibility(VISIBLE);
        weiboArticleView.enlargedView = new WeakReference(enlargedViewWrapperWr.get());
        final Animation fadeout = AnimationUtils.loadAnimation(pageActivity, R.anim.fade);
        fadeout.setDuration(150);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                enlargedViewWrapperWr.get().setVisibility(INVISIBLE);
                WeiboPageView.this.removeView(enlargedViewWrapperWr.get());
                //TODO TEST
                pageActivity.setEnlargedMode(false);
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });
        enlargedViewWrapperWr.get().startAnimation(fadeout);

    }

    public void showLoading() {
        if (!loadingNext) {
            LayoutInflater inflater = LayoutInflater.from(WeiboPageView.this.getContext());
            loadingView = (LinearLayout) inflater.inflate(R.layout.loading, null);
            this.addView(loadingView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            this.loadingNext = true;
        }
    }

    public void removeLoadingIfNecessary() {
        if (loadingView != null) {
            loadingNext = false;

            this.removeView(loadingView);
        }
    }

    public void setLoadingNext(boolean loadingNext) {
        this.loadingNext = loadingNext;
    }

    public boolean isLastPage() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public boolean isFirstPage() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public void renderBeforeLayout() {
        rendered = true;
        if (headerImage != null)
            headerImage.loadImage();
    }

    public void releaseResource() {
        for (int i = 0; i < page.getArticleList().size(); i++) {
            Article article = page.getArticleList().get(i);
            System.out.println("release image...");
            article.setImageBitmap(null);
        }
    }

    public void closeEnlargedView() {
        if (this.clickedArticleView != null) {
            this.closeEnlargedView(clickedArticleView);
        }
    }
}
