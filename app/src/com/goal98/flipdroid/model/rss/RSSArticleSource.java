package com.goal98.flipdroid.model.rss;

import com.goal98.flipdroid.exception.NoNetworkException;
import com.goal98.flipdroid.model.Article;
import com.goal98.flipdroid.model.OnSourceLoadedListener;
import com.goal98.flipdroid.model.cachesystem.CacheToken;
import com.goal98.flipdroid.model.cachesystem.CacheableArticleSource;
import com.goal98.flipdroid.model.cachesystem.SourceCacheObject;
import com.goal98.flipdroid.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 6/6/11
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSArticleSource implements CacheableArticleSource {
    private String contentUrl;
    private String sourceName;
    private boolean loaded;
    private String sourceImage;
    private LinkedList list = new LinkedList<Article>();
    private InputStream content;
    private OnSourceLoadedListener listener;

    public RSSArticleSource(String contentUrl, String sourceName, String sourceImage) {
        this.contentUrl = contentUrl;
        this.sourceName = sourceName;
        this.sourceImage = sourceImage;
    }

    public RSSArticleSource(InputStream content, String sourceName, String sourceImage) {
        this.content = content;
        this.sourceName = sourceName;
        this.sourceImage = sourceImage;
    }

    public Date lastModified() {
        return new Date();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Article> getArticleList() {
        return list;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static long cachedTime = -1;

    public boolean loadMore() {
        RssParser rp;
        if (content == null) {
            rp = new RssParser(this.contentUrl);
            if (listener != null) {
                rp.addOnLoadListener(listener);
            }
        } else
            rp = new RssParser(this.content);

        try {
            rp.parse();
        } catch (Exception e) {
            return false;
        }
        RssParser.RssFeed feed = rp.getFeed();
        ArrayList<RssParser.Item> items = feed.getItems();
        for (int i = 0; i < items.size(); i++) {
            RssParser.Item item = items.get(i);
            //System.out.println("item" + item.title);
            Article article = new Article();
            if (item.author == null || item.author.trim().length() == 0)
                article.setAuthor(sourceName);
            else {
                String[] authors = item.author.split(" ");
                article.setAuthor(authors[0]);
            }

            if (sourceImage != null) {
                try {
                    article.setPortraitImageUrl(new URL(sourceImage));
                } catch (MalformedURLException e) {

                }
            }
            article.setTitle(item.title);
            article.setStatus(item.link);
            //System.out.println("item.pubDate " + item.pubDate);
            Date date = new Date();
            if (item.pubDate != null) {
                item.pubDate = preFormat(item.pubDate);
                try {
                    date = new SimpleDateFormat("dd MM yyyy HH:mm:ss").parse(item.pubDate);
                } catch (ParseException e) {
                    try {
                        date = new SimpleDateFormat("dd MM yyyy HH:mm").parse(item.pubDate);
                    } catch (ParseException e1) {
                        try {
                            date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(item.pubDate);
                        } catch (ParseException e2) {
                            try {
                                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.pubDate);
                            } catch (ParseException e3) {
                                try {
                                    date = new SimpleDateFormat("yyyy-MM-dd").parse(item.pubDate);
                                } catch (ParseException e4) {

                                }
                            }
                        }
                    }
                }
            }
            article.setCreatedDate(date);
            article.setSourceType(Constants.TYPE_RSS);
            list.add(article);
        }
        loaded = true;
        ////System.out.println("LOADED" + loaded);
        return true;
    }

    private String preFormat(String pubDate) {
        return pubDate.replace("Jan", "01")
                .replace("Feb", "02")
                .replace("Mar", "03")
                .replace("Apr", "04")
                .replace("May", "05")
                .replace("Jun", "06")
                .replace("Jul", "07")
                .replace("Aug", "08")
                .replace("Sep", "09")
                .replace("Oct", "10")
                .replace("Nov", "11")
                .replace("Dec", "12")
                .replace("Sun, ", "")
                .replace("Mon, ", "")
                .replace("Tue, ", "")
                .replace("Wen, ", "")
                .replace("Thu, ", "")
                .replace("Fri, ", "")
                .replace("Sat, ", "");
    }

    public boolean isNoMoreToLoad() {
        ////System.out.println("loaded--:" + loaded);
        if (loaded)
            return true;
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getForceMagzine() {
        return true;
    }

    public boolean reset() {
        list.clear();
        loaded = false;
        return true;
    }

    public CacheToken getCacheToken() {
        CacheToken token = new CacheToken();
        token.setType(Constants.TYPE_RSS);
        token.setToken(this.contentUrl);
        return token;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void fromCache(SourceCacheObject cachedObject) {
        this.content = new ByteArrayInputStream(cachedObject.getContent().getBytes());
    }

    public void registerOnLoadListener(OnSourceLoadedListener listener) {
        this.listener = listener;
    }

    public boolean loadLatestSource() throws NoNetworkException {
        RssParser rp = new RssParser(this.contentUrl);
        try {
            final String rpContent = rp.getContent();
            if(!rpContent.equals(content)){
               this.content = rpContent;

               return true;
           }
        } catch (IOException e) {
            throw new NoNetworkException();
        }

    }
}
