package com.goal98.flipdroid.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.goal98.flipdroid.R;
import com.goal98.flipdroid.db.SourceDB;
import com.goal98.flipdroid.model.Source;
import com.goal98.flipdroid.model.SourceRepo;
import com.goal98.flipdroid.util.Constants;
import com.goal98.flipdroid.view.SourceItemViewBinder;

import java.util.List;
import java.util.Map;

public class SourceSelectionActivity extends ListActivity {


    private SourceDB sourceDB;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.source_list);

        sourceDB = new SourceDB(this);

        String type = getIntent().getExtras().getString("type");
        Log.v(this.getClass().getName(), "Account type:" + type);

        List<Map<String, String>> sourceList = new SourceRepo(this).findSourceByType(type);
        Map<String, String> customeSection = SourceDB.buildSource(Constants.TYPE_SINA_WEIBO,
                "Add Custom Source",
                Constants.ADD_CUSTOME_SOURCE,
                "Add any person.", null);

        sourceList.add(customeSection);

        String[] from = new String[]{Source.KEY_SOURCE_NAME, Source.KEY_SOURCE_DESC, Source.KEY_IMAGE_URL};
        int[] to = new int[]{R.id.source_name, R.id.source_desc, R.id.source_image};
        SimpleAdapter adapter = new SimpleAdapter(this, sourceList, R.layout.source_item, from, to);
        adapter.setViewBinder(new SourceItemViewBinder());

        setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Map<String, String> source = (Map<String, String>) l.getItemAtPosition(position);
        String sourceId = source.get(Source.KEY_SOURCE_ID);
        Log.v(this.getClass().getName(), sourceId);

        if (Constants.ADD_CUSTOME_SOURCE.equals(sourceId)) {
            startActivity(new Intent(this, SourceSearchActivity.class));
        } else {
            sourceDB.insert(source);
            startActivity(new Intent(this, IndexActivity.class));
        }
    }
}