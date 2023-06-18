package com.crypto_tab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Menu_CoinList extends AppCompatActivity {

    private ListViewAdapter Adapter;
    GridView mGrid;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu_coinlist);

        getWindow().setNavigationBarColor(Color.BLACK);

        spinner = findViewById(R.id.progress_clist_id);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        getWindow().setNavigationBarColor(Color.TRANSPARENT);


        mGrid = findViewById(R.id.open_order_list);
        mGrid.setNumColumns(Calculate_Columns());

        final SearchView sv = findViewById(R.id.search_coin);


        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                if (!sv.isIconified()) {
                    sv.setIconified(true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (Adapter != null) {

                    if (TextUtils.isEmpty(s)) {
                        Adapter.filter("");
                        mGrid.clearTextFilter();
                    } else {
                        Adapter.filter(s);
                    }
                }
                return false;
            }

        });

            new Load_Data().execute();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @SuppressLint("StaticFieldLeak")
    public class Load_Data extends AsyncTask<Void, Integer, Boolean> {

        Load_Data() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            Add_Coin_Names();
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override

        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);

            mGrid.setAdapter(Adapter);
            spinner.setVisibility(View.GONE);
        }
    }


    private float Get_DP_Width() {
        float dpWidth;

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        return (dpWidth);
    }


    private int Calculate_Columns() {

        return (int) (Get_DP_Width() / (150));
    }


    private void Add_Coin_Names() {

        if (FullscreenActivity.CList_Data.size() <= 0) {
            return;
        }

        Adapter = new ListViewAdapter(this, R.layout.menu_coin_list_item, FullscreenActivity.CList_Data);


    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        if (checked) {
            FullscreenActivity.Add_Coin_Name((String) ((CheckBox) view).getText());
        } else {
            FullscreenActivity.Delete_Coin_Name((String) ((CheckBox) view).getText());
        }

    }


    public static class ListViewAdapter extends ArrayAdapter<String> {

        private Activity activity;
        private List<String> friendList;
        private List<String> searchList;

        ListViewAdapter(Activity context, int resource, List<FullscreenActivity.Coin_Data> objects) {
            super(context, resource);
            this.activity = context;
            this.friendList = new ArrayList<>();
            this.searchList = new ArrayList<>();

            for (int idx = 0; idx < objects.size(); ++idx) {
                friendList.add(objects.get(idx).getSymbol());
            }

            this.searchList.addAll(friendList);
        }

        @Override
        public int getCount() {
            return friendList.size();
        }

        @Override
        public String getItem(int position) {
            return friendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            // If holder not exist then locate all view from UI file.
            if (convertView == null) {
                // inflate UI from XML file
                convertView = inflater.inflate(R.layout.menu_coin_list_item, parent, false);
                // get all UI view
                holder = new ViewHolder(convertView);
                // set tag for holder
                convertView.setTag(holder);
            } else {
                // if holder created, get tag from view
                holder = (ViewHolder) convertView.getTag();
            }

            holder.friendName.setText(getItem(position));

            if (FullscreenActivity.Config_Data.Coin_Names.indexOf(getItem(position)) >= 0)
                holder.friendName.setChecked(true);
            else
                holder.friendName.setChecked(false);

            return convertView;
        }

        // Filter method
        void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            friendList.clear();
            if (charText.length() == 0) {
                friendList.addAll(searchList);
            } else {
                for (String s : searchList) {
                    if (s.toLowerCase(Locale.getDefault()).contains(charText)) {
                        friendList.add(s);
                    }
                }
            }
            notifyDataSetChanged();
        }

        private static class ViewHolder {
            private CheckBox friendName;

            ViewHolder(View v) {
                friendName = v.findViewById(R.id.checkBox);
            }
        }
    }

}