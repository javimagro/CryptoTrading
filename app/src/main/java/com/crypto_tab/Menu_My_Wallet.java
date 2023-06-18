package com.crypto_tab;

import android.app.Activity;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

public class Menu_My_Wallet extends AppCompatActivity {
    RecyclerView recyclerView_Items;
    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView.Adapter Items;

    private ProgressBar spinner;
    PieChart Pie_Chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_mywallet);

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(Color.BLACK);


        Items = null ;

        spinner = findViewById(R.id.wallet_pb);

        ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark) ;


        Switch sw_id = findViewById(R.id.hide_dust_id);

        sw_id.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recyclerView_Items = (RecyclerView) findViewById(R.id.items_id);
                if (recyclerView_Items != null) {
                    recyclerView_Items.getAdapter().notifyDataSetChanged();
                    recyclerView_Items.requestLayout();
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Account account = null;

                account = Refresh_Account();
                Refresh_Pie_Data(account);
                Show_Table_Data ( account ) ;

                if ( Items != null )
                {
                    Items_Adapter lmp ;

                    lmp = ( Items_Adapter) Items ;

                    lmp.Refresh_Content ( account.getBalances() );
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });

            new Load_Data(this).execute();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    public class Load_Data extends AsyncTask<Void, Integer, Boolean> {
        Account account = null;

        private Load_Data(Activity activity) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);

            Pie_Chart = findViewById(R.id.Pie_Chart);

            Pie_Chart.getDescription().setEnabled(false);
            Pie_Chart.getLegend().setEnabled(true);
            Pie_Chart.setHighlightPerTapEnabled(false);
            Pie_Chart.setNoDataText("");
            Pie_Chart.setUsePercentValues(false);
            Pie_Chart.getDescription().setEnabled(false);
            Pie_Chart.setDragDecelerationFrictionCoef(0.95f);
            Pie_Chart.setDrawHoleEnabled(true);
            Pie_Chart.setHoleColor(Color.TRANSPARENT);
            Pie_Chart.setTransparentCircleColor(Color.RED);
            Pie_Chart.setTransparentCircleAlpha(110);
            Pie_Chart.setHoleRadius(45f);
            Pie_Chart.setTransparentCircleRadius(57f);
            Pie_Chart.setDrawCenterText(false);
            Pie_Chart.setRotationAngle(0);
            Pie_Chart.setRotationEnabled(true);
            Pie_Chart.setHighlightPerTapEnabled(false);

            Legend l = Pie_Chart.getLegend();
            l.setEnabled(false);

        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            account = Refresh_Account();

            return (true);

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (account != null) {
                Refresh_Pie_Data(account);


                Show_Table_Data( account );

                if ( account != null )
                {
                    RecyclerView.LayoutManager layoutManager_Items;

                    recyclerView_Items =(RecyclerView) findViewById(R.id.items_id);
                    recyclerView_Items.setHasFixedSize(false);

                    layoutManager_Items =new LinearLayoutManager(getApplicationContext() );
                    recyclerView_Items.setLayoutManager(layoutManager_Items);

                // specify an adapter (see also next example)
                    Items =new Items_Adapter(account.getBalances());

                    recyclerView_Items.setAdapter(Items);
                    recyclerView_Items.getAdapter().notifyDataSetChanged();
                    recyclerView_Items.requestLayout();
                }
            }

            spinner.setVisibility(View.GONE);
        }
    }

    public class NonZeroChartValueFormatter extends DefaultValueFormatter {

        public NonZeroChartValueFormatter(int digits) {
            super(digits);
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {
            if (value > 0) {
                return mFormat.format(value);
            } else {
                return "";
            }
        }
    }

    public void Show_Table_Data ( Account account )
    {
        String Curr_Price;

        if ( account == null )
            return ;

        String usdt_free = Get_Total_USDT (account.getBalances());

        TextView btc_funds = findViewById(R.id.btc_funds_id);
        TextView dollar_funds = findViewById(R.id.dolar_funds_id);

        Curr_Price = Find_RT_Data( "BTC" , "USDT" ) ;

        dollar_funds.setText(FullscreenActivity.Round_Number(usdt_free , 2 ));
        btc_funds.setText(FullscreenActivity.Round_Number(Float.toString(Float.parseFloat(usdt_free)/Float.parseFloat(Curr_Price)),6));

    }

    public static String Find_RT_Data ( String Coin )
    {
        int pos_symbol  ;

        if ((pos_symbol = FullscreenActivity.Find_List_Data( Coin )) >= 0)
        {
            return (  FullscreenActivity.CList_Data.get(pos_symbol).getLastPrice() ) ;
        }
        return ( "0" );
    }

    public static String Find_RT_Data ( String Coin , String Base )
    {
        int pos_symbol  ;
        String CoinName ;

        if ( FullscreenActivity.Use_KuCoin )
            CoinName = Coin + "-" + Base ;
        else
            CoinName = Coin + Base ;

        if ((pos_symbol = FullscreenActivity.Find_List_Data( CoinName )) >= 0)
        {
            return (  FullscreenActivity.CList_Data.get(pos_symbol).getLastPrice() ) ;
        }
        return ( "0" );
    }


    public void Refresh_Pie_Data ( Account account )
    {
        if ( account == null )
            return ;

        ArrayList<PieEntry> yvalues = get_Account_Coins (account.getBalances());

        if ( Pie_Chart != null )
            Pie_Chart.clear(); ;

        PieDataSet dataSet = new PieDataSet(yvalues, "");

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);

        data.setValueTextSize(13f);
        data.setValueTextColor(Color.DKGRAY);
        data.setValueFormatter( new NonZeroChartValueFormatter(2));

        Pie_Chart.setData(data);
        Pie_Chart.animateXY(500, 500);

    }




    public Account Refresh_Account (  ) {

        Switch sw_id;
        List<AssetBalance> ABal;
        Account account ;

        sw_id = findViewById(R.id.hide_dust_id);

        account = FullscreenActivity.Get_Account();

        if (account != null)
        {
            sw_id.setEnabled(true);

            ABal = account.getBalances();

            for (int idx = 0; idx < ABal.size(); ++idx) {
                if (Float.parseFloat(ABal.get(idx).getFree()) == 0 && Float.parseFloat(ABal.get(idx).getLocked()) == 0) {
                    ABal.remove(idx);
                    --idx;
                }
            }
            return ( account ) ;
        }
        else
        {
            Pie_Chart = findViewById(R.id.Pie_Chart);

            Pie_Chart.setNoDataText("Unable to get Account data from Exchange");
            Pie_Chart.setNoDataTextColor(Color.WHITE);

            Pie_Chart.invalidate();

            sw_id.setEnabled(false);
        }

        return ( null ) ;
    }

    public ArrayList<PieEntry> get_Account_Coins ( List<AssetBalance> ABal )
    {
        ArrayList<PieEntry> ar ;

        ar = new  ArrayList<PieEntry>();

        for ( int idx = 0 ; idx <  ABal.size() ; ++ idx )
        {
            BigDecimal bg ;

            bg  = new BigDecimal( ABal.get(idx ).getFree()).add( new BigDecimal( ABal.get(idx ).getLocked() )) ;

            String btc_price = Get_Price_Value ( ABal.get(idx ).getAsset() , bg ) ;

            if ( Float.parseFloat( btc_price ) > 0.001f )
                ar.add( new PieEntry( Float.parseFloat( btc_price ) ,  ABal.get(idx ).getAsset()   ) ) ;
        }

        return ( ar  );
    }

    static public String Get_Total_USDT ( List<AssetBalance> ABal )
    {
        BigDecimal Final_Price ;
        String btc_price ;

        Final_Price = new BigDecimal( "0");

        for ( int idx = 0 ; idx < ABal.size() ; ++ idx )
        {
            BigDecimal bg ;

            bg  = new BigDecimal( ABal.get(idx ).getFree()).add( new BigDecimal( ABal.get(idx ).getLocked() )) ;

            btc_price = Get_Price_Value ( ABal.get(idx ).getAsset() , bg ) ;

            Final_Price = Final_Price.add ( new BigDecimal( btc_price )) ;

        }

        return ( Final_Price.toString() );
    }

    static public String Get_Price_Value ( String Coin_Nm , BigDecimal qty ) {
        String BTC_Price;
        String USDT_Price;
        String Chg_Price;
        BigDecimal Temp_Nm;

        Chg_Price = Find_RT_Data("BTC", "USDT");

        if (Coin_Nm.equals("USDT"))
            return (String.valueOf(qty));

        if (Coin_Nm.equals("BTC")) {
            return (qty.multiply(new BigDecimal(Chg_Price)).toString());
        }

        if (Coin_Nm.equals("ETH")) {
            BTC_Price = Find_RT_Data("ETH", "USDT");
            return (qty.multiply(new BigDecimal(BTC_Price)).toString());
        }

        USDT_Price = Find_RT_Data(Coin_Nm, "USDT");
        if (USDT_Price.equals("0"))
        {
            BTC_Price  = Find_RT_Data(Coin_Nm, "BTC");
            USDT_Price = new BigDecimal( BTC_Price ).multiply ( new BigDecimal( Chg_Price ) ).toString() ;
        }

        return ( qty.multiply( new BigDecimal( USDT_Price )).toString());

    }

    public class Items_Adapter extends RecyclerView.Adapter<Items_Adapter.MyViewHolder> {
        List<AssetBalance> mDataset;
        int Text_Color;


        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public CardView cardView;

            public MyViewHolder(CardView v) {
                super(v);
                cardView = v;
                cardView.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        public Items_Adapter( List<AssetBalance> ABal ) {

            Refresh_Content( ABal );

        }

        public void Refresh_Content ( List<AssetBalance> ABal )
        {
            AssetBalance elem ;

            elem = new AssetBalance();

            elem.setAsset  ( "Coin Name" ) ;
            elem.setFree   ( "Available" ) ;
            elem.setLocked ( "Total" ) ;

            mDataset = ABal ;
            mDataset.add ( 0 , elem );
            notifyDataSetChanged () ;
        }

        public void setColor(int rColor) {
            Text_Color = rColor;
        }

        @Override
        public Items_Adapter.MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.items_mywallet_table, parent, false);

            Items_Adapter.MyViewHolder vh = new Items_Adapter.MyViewHolder(v);
            return vh;

        }

        public void onBindViewHolder(Items_Adapter.MyViewHolder holder, int position )
        {
            BigDecimal total ;

            TextView v  = holder.cardView.findViewById(R.id.coinname);
            TextView v2 = holder.cardView.findViewById(R.id.coin_qty);
            TextView v3 = holder.cardView.findViewById(R.id.coin_total);
            TextView v4 = holder.cardView.findViewById(R.id.btc_eq);


            if ( position == 0 )
            {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                holder.itemView.setBackgroundColor( ContextCompat.getColor ( getApplicationContext() , R.color.MYBLUE  ) );

                v.setTextAlignment (View.TEXT_ALIGNMENT_CENTER);
                v2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                v3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                v4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                v.setTextColor ( Color.BLACK);
                v2.setTextColor( Color.BLACK);
                v3.setTextColor( Color.BLACK);
                v4.setTextColor( Color.BLACK);

                v.setText ( "Coin Name" );
                v2.setText( "Available" );
                v3.setText( "Total" );
                v4.setText( "USDT" );

            }
            else
            {
                String Coin_Dta ;
                String btc_price ;

                v.setText( mDataset.get(position).getAsset());
                v2.setText(FullscreenActivity.Round_Number ( mDataset.get(position).getFree()));

                total = new BigDecimal(( mDataset.get(position).getFree())).add ( new BigDecimal( mDataset.get(position).getLocked())) ;


                btc_price = Get_Price_Value ( mDataset.get(position).getAsset() , total ) ;

                Switch sw_id = findViewById(R.id.hide_dust_id);
                if ( sw_id.isChecked() == true && new BigDecimal( btc_price ).compareTo( new BigDecimal( "0.001")) == -1 )
                {
                    holder.itemView.setVisibility( View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

                    return ;
                }
                else
                {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    holder.itemView.setBackgroundColor( ContextCompat.getColor ( getApplicationContext() , R.color.GRAY_DARK  ) );
                }

                v3.setText( FullscreenActivity.Round_Number( total.toString()  ));
                v4.setText( FullscreenActivity.Round_Number( btc_price , 2  ));

                v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                v2.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                v3.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                v4.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                v.setTextColor( Color.LTGRAY);
                v2.setTextColor( Color.LTGRAY);
                v3.setTextColor( Color.LTGRAY);
                v4.setTextColor( Color.LTGRAY);

            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return (mDataset.size());
        }
    }

}




