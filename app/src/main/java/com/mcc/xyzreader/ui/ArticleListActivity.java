package com.mcc.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcc.xyzreader.R;
import com.mcc.xyzreader.adapter.ArticleAdapter;
import com.mcc.xyzreader.data.ArticleLoader;
import com.mcc.xyzreader.data.ItemsContract;
import com.mcc.xyzreader.data.UpdaterService;
import com.mcc.xyzreader.model.ArticleModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.toString();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ArticleAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private ArrayList<ArticleModel> arrayList;

    private Parcelable mListState;
    private final String LIST_STATE_KEY = "list_state";
    private final String LIST_DATA_KEY = "list_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            //getSupportActionBar().setIcon(R.drawable.logo);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        arrayList = new ArrayList<>();
        adapter = new ArticleAdapter(ArticleListActivity.this, arrayList);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        layoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter.setItemClickListener(new ArticleAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(arrayList.get(position).getId())));
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stopRefresh();
                startRefresh();
            }
        });


        getLoaderManager().initLoader(0, null, this);

    }

    private void startRefresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    private void stopRefresh() {
        stopService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                boolean mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI(mIsRefreshing);
            }
        }
    };

    private void updateRefreshingUI(boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if(refresh) {
            Snackbar.make(mRecyclerView, getString(R.string.loading), Snackbar.LENGTH_INDEFINITE).show();
        } else {
            Snackbar.make(mRecyclerView, getString(R.string.loaded), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {

            ArrayList<ArticleModel> articleModels = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(ArticleLoader.Query._ID);
                    String title = cursor.getString(ArticleLoader.Query.TITLE);
                    String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                    String author = cursor.getString(ArticleLoader.Query.AUTHOR);
                    String thumbUrl = cursor.getString(ArticleLoader.Query.THUMB_URL);
                    float thumbAspectRatio = cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);

                    articleModels.add(new ArticleModel(id, title, date, author, thumbUrl, thumbAspectRatio));
                } while (cursor.moveToNext());
            }

            refreshData(articleModels);
            updateRefreshingUI(false);
        } else {
            startRefresh();
        }
    }

    private void refreshData(ArrayList<ArticleModel> newData) {
        arrayList.clear();
        arrayList.addAll(newData);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }


    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = layoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, mListState);
        state.putParcelableArrayList(LIST_DATA_KEY, arrayList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state != null) {
            ArrayList<ArticleModel> restoredList = state.getParcelableArrayList(LIST_DATA_KEY);
            refreshData(restoredList);

            mListState = state.getParcelable(LIST_STATE_KEY);
            layoutManager.onRestoreInstanceState(mListState);
        }
    }
}
