package com.jseo.earthquakelist.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jseo.earthquakelist.DataRetriever;
import com.jseo.earthquakelist.EarthquakeDataRetriever;
import com.jseo.earthquakelist.R;
import com.jseo.earthquakelist.data.EarthquakesSummary;
import com.jseo.earthquakelist.dummy.DummyContent;

import java.io.IOException;
import java.net.URL;
import java.util.List;


/**
 * An activity representing a list of Earthquakes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EarthquakeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EarthquakeListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private EarthquakeRecyclerViewAdapter mAdapter;

    private MagnitudeFilter mMagnitudeFilter = MagnitudeFilter.MAG_45;
    public enum MagnitudeFilter {
        ALL(0),
        MAG_10(1),
        MAG_25(2),
        MAG_45(3),
        SIG(4);

        private int mIndex;

        MagnitudeFilter(int filter) {
            mIndex = filter;
        }
    }

    private static int URL_RESOURCES[] = {
            R.string.quakes_feed_all_day_url,
            R.string.quakes_feed_mag_1_0_day_url,
            R.string.quakes_feed_mag_2_5_day_url,
            R.string.quakes_feed_mag_4_5_day_url,
            R.string.quakes_feed_significant_day_url
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        View recyclerView = findViewById(R.id.earthquake_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.earthquake_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            gatherEarthquakeData();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void gatherEarthquakeData() throws IOException {

        DataRetriever dataRetriever = new EarthquakeDataRetriever();
        String urlString = getString(URL_RESOURCES[mMagnitudeFilter.mIndex]);
        try {
            URL url = new URL(urlString);
            dataRetriever.setUrlString(urlString);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "invalid URL set", Toast.LENGTH_SHORT).show();
            return;
        }
        DataRetriever.OnRetrieveCompleteListener onRetrieveCompleteListener =
                new DataRetriever.OnRetrieveCompleteListener() {
                    @Override
                    public void onRetrieveComplete(final boolean isSuccess, final Object retrievedData) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TODO use retrievedData to update list
                                if (isSuccess && retrievedData instanceof EarthquakesSummary) {
                                    EarthquakesSummary earthquakesSummary =
                                            (EarthquakesSummary)retrievedData;
                                    mAdapter.setEarthquakeData(earthquakesSummary.getEarthquakeList());
                                    mAdapter.notifyDataSetChanged();
                                }
                                Toast.makeText(getApplicationContext(),
                                        "retrieve completed - is success? " + isSuccess,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
        dataRetriever.setOnRetrieveCompleteListener(onRetrieveCompleteListener);

        dataRetriever.retrieve();

    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new EarthquakeRecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);
//        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.earthquake_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EarthquakeDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        EarthquakeDetailFragment fragment = new EarthquakeDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.earthquake_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EarthquakeDetailActivity.class);
                        intent.putExtra(EarthquakeDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
