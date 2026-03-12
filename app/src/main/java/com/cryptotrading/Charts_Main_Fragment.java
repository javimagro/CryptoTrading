package com.crypto_tab;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.icu.math.BigDecimal;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.google.android.material.tabs.TabLayout;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class Charts_Main_Fragment extends AppCompatActivity {

    public static String Text_coin_RT = "" ;
    long First_TimeStampt;

    Charts_Mixed_Data_Fragment Mixed_Fragment;
    Charts_Trades_Fragment Trades_Fragment;

    boolean Main_Thread_Still_Running ;

    static public Timer Update_Data_Timer;

    static View Label_Coin_Data;

    private Closeable ws_candlestick_closeable;

    static public boolean Global_Socket_Connection;

    private AsyncTask Paint_Task ;


    private CombinedChart Chart_CS;
    private BarChart Bar_CS;

    private ProgressBar spinner;
    private static Activity Charts_Main_Activity ;



    List<Trade> TD;

    private void Clear_All_Fragments( )
    {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        if (fragmentList == null) {
            // code that handles no existing fragments
        }

        for (Fragment frag : fragmentList) {
            getSupportFragmentManager().beginTransaction().remove(frag).commit();

        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        MyDebug( "Main_Fragment" , "onNewIntent()");

        MyDebug( "Main_Fragment" , "Destroing new data..") ;

        if ( Paint_Task != null )
            Paint_Task.cancel(true) ;

        Paint_Task = null ;
        Clear_All_Fragments ( ) ;
        Clear_Data ( ) ;

        setIntent( intent ) ;

        setContentView(R.layout.charts_main_fragment);
        getWindow().setNavigationBarColor(Color.BLACK);

        Mixed_Fragment = null;
        Main_Thread_Still_Running = false ;
        Charts_Main_Activity = this ;

        MyDebug( "Main_Fragment" , "Creating new data..") ;


        Bundle b = intent.getExtras();
        if (b != null)
            Text_coin_RT = b.getString("CoinName");
        else
            Text_coin_RT = "";

        Paint_All_Data();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        Paint_Task = null ;
        Clear_All_Fragments ( ) ;

        MyDebug( "Main_Fragment" , "onCreate()");

        setContentView(R.layout.charts_main_fragment);

        getWindow().setNavigationBarColor(Color.BLACK);

        Mixed_Fragment = null;
        Main_Thread_Still_Running = false ;
        Charts_Main_Activity = this ;


        Bundle b = getIntent().getExtras();
        if (b != null)
            Text_coin_RT = b.getString("CoinName");
        else
            Text_coin_RT = "";

        Paint_All_Data();

    }

    public void Start_Fragments()
    {

        MyDebug( "Main_Fragment" , "Enable Fragments ");

        TabsPagerAdapter sectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setEnabled(false);
        viewPager.setCurrentItem(1);

        ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark);

        TabLayout Main_Tabs_Layout = findViewById(R.id.tabs);
        Main_Tabs_Layout.setupWithViewPager(viewPager);

        viewPager.setPageTransformer(false, (page, position) -> {
            // do transformation here
            final float normalizedposition = Math.abs(Math.abs(position) - 1);
            page.setScaleX(normalizedposition / 2 + 0.5f);
            page.setScaleY(normalizedposition / 2 + 0.5f);
        });
    }

    public void
    Paint_All_Data()
    {
        Init_Global_Varibs();

            Paint_Task = new Load_Data().execute();
    }

    private void Set_Chart_Timer()
    {

        MyDebug ( "Main_Fragment" , "Enable Chart_Timer"); ;

        Update_Data_Timer = new Timer();

        Update_Data_Timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (spinner != null)
                {
                    if (spinner.getVisibility() == View.GONE) {

                        if (!Allow_RealTime_Sockets())
                        {
                            Update_Chart_View_Data();
                        }
                        else
                        {
                            if (!Ping_Public_Websocket())
                            {
                                Enable_Candle_Stream(Text_coin_RT);
                            }
                        }

                        Refresh_All_Information();

                    }
                }
            }

        }, 0, FullscreenActivity.Get_Timer_Refresh_Period());

    }

    public void Refresh_All_Information() {

        new Refresh_Async_Mode ().execute();;

    }

    class Refresh_Async_Mode extends AsyncTask<Void, Integer, Boolean>
    {
        private Refresh_Async_Mode ( )
        {
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            Update_Coin_Balance( );

            List<Order> OO = Update_Chart_Open_Orders(false);
            List<Trade> MTD = Update_Chart_Trades();

            Update_Chart_Alerts( false );

            MyDebug( "Main_Fragment" , "Refresh Main Info.");

            Charts_Open_Orders_Fragment Orders_Act = Charts_Open_Orders_Fragment.getInstance();
            if (Orders_Act != null) {
                Orders_Act.Refresh_OO( OO );
            }

            Charts_MyTrades_Fragment MyTrades_Act = Charts_MyTrades_Fragment.getInstance();
            if (MyTrades_Act != null) {
                MyTrades_Act.Refresh_OO( MTD );
            }

            Charts_Alerts_Fragment Alerts_Act = Charts_Alerts_Fragment.getInstance();
            if (Alerts_Act != null) {
                Alerts_Act.Refresh_Alerts();
            }

            return ( true ) ;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
        }
    }



    public class TabsPagerAdapter extends FragmentPagerAdapter {

        private TabsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int index) {

            switch (index) {
                case 0:
                    return (new Charts_Trades_Fragment());
                case 1:
                    return (new Charts_Mixed_Data_Fragment());
                case 2:
                    return new Charts_Open_Orders_Fragment();
                case 3:
                    return new Charts_Alerts_Fragment();
                case 4:
                    return new Charts_MyTrades_Fragment();
            }

            return (null);
        }

        @Override
        public int getCount() {
            return 5;
        }

    }

    private void Close_WS_CandleStick() {


        if (ws_candlestick_closeable != null)
        {
            try
            {
                ws_candlestick_closeable.close();
            } catch (Exception e)
            {
                MyDebug("Connection" , "Close_WS: " +  e.getMessage());
            }
        }

        ws_candlestick_closeable =null;
    }

    void Clear_Data ( )
    {
        if (Update_Data_Timer != null)
        {
            MyDebug( "Clear_Data" , "Destroy timer...");
            Update_Data_Timer.cancel();
        }

        Update_Data_Timer = null;

        Close_WS_CandleStick ( ) ;
        Show_Connection_State ( false );
        Text_coin_RT = "" ;
/*
        if (Chart_CS != null)
            Chart_CS.clear();

        if (Bar_CS != null)
            Bar_CS.clear();

        FullscreenActivity.candlesticksCache.clear();

 */
    }

    @Override
    public void onDestroy()
    {
        MyDebug( "Main_Fragment" , "onDestroy()");

        super.onDestroy();

        if ( Paint_Task != null )
            Paint_Task.cancel(true) ;


        Clear_Data ( ) ;

        //QUITAR ??
//        Charts_Main_Activity = null ;
    }


    private void Init_Global_Varibs()
    {

        spinner = findViewById(R.id.mpg_progressBar);

        if ( FullscreenActivity.candlesticksCache != null )
            FullscreenActivity.candlesticksCache.clear();
        else
            FullscreenActivity.candlesticksCache = new TreeMap<>();

        Label_Coin_Data = null;

        if (Update_Data_Timer != null)
            Update_Data_Timer.cancel();

        Update_Data_Timer = null;

        Close_WS_CandleStick ( ) ;

        if ( Chart_CS !=null )
        {
//            Chart_CS.clearValues();
            Chart_CS.clear();
        }
        if ( Bar_CS !=null )
        {
//            Bar_CS.clearValues();
            Bar_CS.clear();

        }

        if (  TD !=null )
            TD.clear();

        Chart_CS = null;
        Bar_CS   = null;
        TD       = null;
    }

    private boolean Update_CandleStick()
    {

        Long offsettime = 0L;

        if (FullscreenActivity.candlesticksCache == null)
            return (false);

        if (FullscreenActivity.Get_Ticket_Data(Text_coin_RT, true) == 1)
        {

            for (Map.Entry<Long, Candlestick> entry : FullscreenActivity.candlesticksCache.entrySet()) {
                offsettime = entry.getKey();
            }

            if (offsettime > 0L)
            {
                Paint_Line_Chart(offsettime);
            }

            Show_Connection_State ( true );

            return (true);
        }

        Show_Connection_State ( false );

        return false;
    }

    private void Update_Chart_View_Data()
    {

        MyDebug("Chart_Activity Timer", "Timer count reached....");

        if (FullscreenActivity.CList_Data == null)
            return;

        String text_change = Show_Symbol_Percent(Text_coin_RT);
        String text_value  = Show_Symbol_Price  (Text_coin_RT);

        Paint_Coin_Values(Text_coin_RT, text_value, text_change);

        if (Update_CandleStick())
        {
            Refresh_Charts();
        }
    }

    public String Show_Symbol_Percent(String Text_coin_RT)
    {
        int idx ;

        idx = FullscreenActivity.Find_List_Data( Text_coin_RT ) ;
        if ( idx < 0 )
            return ("0");

        return (FullscreenActivity.CList_Data.get(idx ).getPriceChangePercent());
    }

    private String Show_Symbol_Price(String Text_coin_RT) {
        if (FullscreenActivity.CList_Data == null)
            return ("0");

        for (int x = 0; x < FullscreenActivity.CList_Data.size(); x++) {
            String symbol = FullscreenActivity.CList_Data.get(x).getSymbol();

            if (symbol.equals(Text_coin_RT)) {

                return (FullscreenActivity.CList_Data.get(x).getLastPrice());
            }
        }

        return ("0");
    }

    private void Check_And_Paint_In_Favourites ( String CoinName )
    {
        if ( Check_In_Favorites( CoinName))
            Paint_In_Favorites( true );
        else
            Paint_In_Favorites( false );

    }

    private boolean Check_In_Favorites ( String CoinName )
    {
        for (int idx_coin = 0; idx_coin < FullscreenActivity.Config_Data.Coin_Names.size(); idx_coin++) {

            if ( FullscreenActivity.Config_Data.Coin_Names.get(idx_coin).equals(CoinName))
            {
                return ( true ) ;
            }
        }

        return ( false ) ;
    }

    private void Paint_In_Favorites ( boolean mode )
    {
        if ( mode )
        {
            TextView idata = Label_Coin_Data.findViewById(R.id.Fav_ID);
            idata.setVisibility(View.VISIBLE);
            idata.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.GR));

        }
        else
        {
            TextView idata = Label_Coin_Data.findViewById(R.id.Fav_ID);
            idata.setVisibility(View.VISIBLE);
            idata.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.RD));
        }
    }

    private static void Hide_Amount_Items( Activity Act , boolean enable) {

        if (Label_Coin_Data == null)
            return;

        LinearLayout idata = Label_Coin_Data.findViewById(R.id.qty_data);

        Act.runOnUiThread(() -> {
            if (enable)
                idata.setVisibility(View.GONE);
            else
                idata.setVisibility(View.VISIBLE);
        });

    }

    private static String Append_Base_Asset ( String Coin )
    {
        if ( Coin.endsWith( "BTC"))
            return ( "BTC" ) ;

        if ( Coin.endsWith( "ETH"))
            return ("ETH");

        if ( Coin.endsWith("USDT"))
            return ("USDT");

        return ( "BTC" ) ;

    }

    public static void Update_Coin_Balance (  )
    {
        if ( Charts_Main_Activity == null )
            return ;

        Update_Coin_Balance( Charts_Main_Activity ) ;
    }

    public static void Update_Coin_Balance ( Activity Act )
    {

        List<AssetBalance> ABal;
        Account account;

        if (Label_Coin_Data == null)
            return;

        final TextView idata = Label_Coin_Data.findViewById(R.id.item_data);
        final TextView idata2 = Label_Coin_Data.findViewById(R.id.item_data_sell);

        account = FullscreenActivity.Get_Account();

        if (account != null) {
            ABal = account.getBalances();

            if ( ABal == null)
                return ;

            for (int idx = 0; idx < ABal.size(); ++idx)
            {
                String Asset = ABal.get(idx).getAsset();

                if (FullscreenActivity.Use_KuCoin)
                    Asset += "-";

                Asset += Append_Base_Asset ( Text_coin_RT ) ;

                if (Asset.equals(Text_coin_RT)) {
                    final String Amount_free;
                    final String Amount_locked;

                    Amount_free = ABal.get(idx).getFree();
                    Amount_locked = ABal.get(idx).getLocked();

                    if (Float.parseFloat(Amount_free) == 0 && Float.parseFloat(Amount_locked) == 0) {
                        Hide_Amount_Items( Act ,true);
                        return;
                    }

                    Act.runOnUiThread(() -> {

                        Set_TV_Text ( Act , idata , FullscreenActivity.Qty_Round( Charts_Main_Fragment.Text_coin_RT , Amount_free));
                        idata.setTextColor(Color.parseColor("#9ccc65"));

                        Set_TV_Text ( Act, idata2 , FullscreenActivity.Qty_Round( Charts_Main_Fragment.Text_coin_RT , Amount_locked));
                        idata2.setTextColor(Color.parseColor("#FF8F00"));

                        Hide_Amount_Items( Act,false);
                    });

                    return;
                }
            }
        } else {
            Act.runOnUiThread(() -> Hide_Amount_Items( Act,true));
        }
    }

    public String Get_Label_Balance() {
        if (Text_coin_RT.endsWith("USDT"))
            return (Get_Coin_Balance("USDT"));
        else if (Text_coin_RT.endsWith("BTC"))
            return (Get_Coin_Balance("BTC"));
        else if (Text_coin_RT.endsWith("ETH"))
            return (Get_Coin_Balance("ETH"));
        else if (Text_coin_RT.endsWith("BNB"))
            return (Get_Coin_Balance("BNB"));

        return ("0.00");
    }

    public String Get_Coin_Balance(String Fud) {
        List<AssetBalance> ABal;
        Account account;

        if (Label_Coin_Data == null)
            return ("0.00");

        account = FullscreenActivity.Get_Account();
        if (account != null) {
            ABal = account.getBalances();

            for (int idx = 0; idx < ABal.size(); ++idx) {
                String Asset = ABal.get(idx).getAsset();

                if (Asset.equals(Fud)) {
                    return (ABal.get(idx).getFree());
                }
            }
        }

        return ("0.00");
    }


    public String Get_Current_Price() {
        if (Label_Coin_Data == null)
            return ("0.00");

        TextView idata = Label_Coin_Data.findViewById(R.id.value);

        return (Get_Tv_Text ( idata));

    }

    public String Get_Current_Items()
    {
        if (Label_Coin_Data == null)
            return ("0");

        LinearLayout idata = Label_Coin_Data.findViewById(R.id.qty_data);

        if (idata.getVisibility() != View.VISIBLE)
            return ("0");

        TextView didata = Label_Coin_Data.findViewById(R.id.item_data);

        return  (Get_Tv_Text(didata) ) ;


    }

    private void Start_Flash_Button (  )
    {
        if ( Label_Coin_Data == null )
            return ;

        final ShimmerFrameLayout container;

        container = Label_Coin_Data.findViewById(R.id.shimmer_view_container1);

        container.showShimmer( true );
    }

    private void Stop_Flash_Button (  )
    {
        if ( Label_Coin_Data == null )
            return ;

        final ShimmerFrameLayout container;

        container = Label_Coin_Data.findViewById(R.id.shimmer_view_container1);

        container.stopShimmer();
        container.hideShimmer();
    }

    private void Paint_Label()
    {
        Bundle b = getIntent().getExtras();

        MyDebug("Main_Fragment", "Paint_Label... ");

        if (b == null)
            return;

        String text_name    = b.getString("CoinName");
        String text_value   = b.getString("Value");
        String change_value = b.getString("Change");

        MyDebug ( "Main_Fragment" , "New label : " + text_name );

            LayoutInflater inflater = LayoutInflater.from(this);
            FrameLayout mGrid = findViewById(R.id.Coin_Table);

            Label_Coin_Data = inflater.inflate(R.layout.items_coin_data, mGrid, false);

            Label_Coin_Data.setOnLongClickListener(view -> {

                if ( Check_In_Favorites( text_name )) {
                    FullscreenActivity.Delete_Coin_Name(text_name);
                    Paint_In_Favorites(false);


                }
                else {
                    FullscreenActivity.Add_Coin_Name(text_name);
                    Paint_In_Favorites(true);
                }

                Intent intent = new Intent("data_between_activities");
                intent.putExtra("Need_Reload", "true");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                return ( true );
            });

            Check_And_Paint_In_Favourites ( text_name ) ;

            Hide_Amount_Items( this ,true);

            runOnUiThread(() ->
            {
                Paint_Coin_Values(text_name, text_value, change_value);

//                Start_Flash_Button ( ) ;

                Label_Coin_Data.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                Label_Coin_Data.requestLayout();
                mGrid.addView(Label_Coin_Data);
                mGrid.requestLayout();

            });

            Update_Coin_Balance( this );
    }



    private float Get_DP_Width() {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        return (displayMetrics.widthPixels / displayMetrics.density);

    }

    public void Paint_Coin_Values(String symbol, final String price, final String percent) {

        if ( getIntent() == null )
            return ;

        if (Label_Coin_Data == null)
            return;

        getIntent().putExtra("CoinName", symbol);
        getIntent().putExtra("Value", price);
        getIntent().putExtra("Change", percent);


        final TextView text = Label_Coin_Data.findViewById(R.id.text);

        Set_TV_Text ( this , text , symbol);

        final TextView text_value = Label_Coin_Data.findViewById(R.id.value);

        FullscreenActivity.Set_Text_With_Flash( this , text_value, FullscreenActivity.Round_Number(price));

        if (percent.length() > 0) {
            final TextView text_change = Label_Coin_Data.findViewById(R.id.change);

                String tv = FullscreenActivity.Round_Number(percent) ;
                Set_TV_Text( this , text_change , tv + " %");

                if ( Float.parseFloat( percent ) <= 0)
                    text_change.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.RD));
                else
                    text_change.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GR));
        }
    }

    private void Update_Candle_Chart()
    {
        long offsettime ;

        if (FullscreenActivity.candlesticksCache == null)
            return;

        Iterator<Map.Entry<Long, Candlestick>> entries = FullscreenActivity.candlesticksCache.entrySet().iterator();

        try {
            while (entries.hasNext()) {
                Map.Entry<Long, Candlestick> entry = entries.next();
                offsettime = entry.getKey();
                Paint_Line_Chart(offsettime);
            }
        } catch ( Exception cm)
        {
            MyDebug ( "Concurrent Exception" , "Got iterator problem...may be double click..." + cm.getMessage() ) ;

        }

        Paint_Line_Trade(TD);

        float off_r = Chart_CS.getViewPortHandler().offsetRight();
        float off_b = Chart_CS.getViewPortHandler().offsetBottom();

        Chart_CS.setViewPortOffsets (5f, 1f, off_r, off_b);
        Bar_CS.setViewPortOffsets   (5f, 0f, off_r, 0f);

        Chart_CS.fitScreen();
        Chart_CS.invalidate();
        Chart_CS.zoom( FullscreenActivity.Config_Data.Chart_Scale_X , 1f , 0 , 0 );

        if ( Chart_CS.getData() != null )
            Chart_CS.moveViewToX(Chart_CS.getData().getEntryCount());
        if ( Bar_CS.getData() != null )
            Bar_CS.moveViewToX(Bar_CS.getData().getEntryCount());

        Refresh_Charts();

        CoupleChartGestureListener cs = (CoupleChartGestureListener) Chart_CS.getOnChartGestureListener();
        cs.syncCharts();

    }

    public void Paint_Line_Trade(List<Trade> LT)
    {
        CombinedData CBData;
        ScatterData data1;

        if ( LT == null )
            return ;

        List<Entry> Values;
        List<Entry> Values2;

        Values = new ArrayList<>();
        Values2 = new ArrayList<>();

        for (Trade TiN : LT)
        {
            if (TiN.getTime() < First_TimeStampt)
                continue;

            int item_index = (int) (TiN.getTime() - First_TimeStampt) / Get_Time_Period();

            if (TiN.isBuyer())
                Values.add(new Entry(item_index, Float.parseFloat(TiN.getPrice())));
            else
                Values2.add(new Entry(item_index, Float.parseFloat(TiN.getPrice())));
        }

        Chart_CS = findViewById(R.id.Candle_Chart);

        CBData = Chart_CS.getCombinedData();
        if (CBData != null)
        {
            ScatterDataSet set1 = new ScatterDataSet(Values, "");
            ScatterDataSet set2 = new ScatterDataSet(Values2, "");

            Set_Line_Mode(set1, true);
            Set_Line_Mode(set2, false);

            data1 = new ScatterData(set1);
            data1.addDataSet(set2);

            CBData.setData(data1);
//            data1.notifyDataChanged(); //

            Chart_CS.setData(CBData);

//            Chart_CS.notifyDataSetChanged(); //
//            Chart_CS.invalidate(); //
        }
    }


    public List<Order>  Update_Chart_Open_Orders(boolean force) {
        List<Order> OO;

        if (spinner.getVisibility() != View.GONE && !force)
            return ( null );

        OO = FullscreenActivity.Get_Open_Orders(Text_coin_RT);

        if (OO != null) {
            runOnUiThread(() -> Paint_Open_Orders(OO));
        }

        return ( OO ) ;
    }

    private LimitLine Find_Limit_Line(YAxis RX, String StrVal) {

        int idx;

        List<LimitLine> LN = RX.getLimitLines();

        for (idx = 0; idx < LN.size(); ++idx) {
            LimitLine Ln = LN.get(idx);
            String Lbl = Ln.getTag();
            if (Lbl.equals(StrVal)) {
                return (Ln);
            }
        }

        return (null);
    }

    private void Remove_Limit_Line_Orders(YAxis RX) {
        Remove_Limit_Lines(RX, "ORDER");
    }

    private void Remove_Limit_Line_Alerts(YAxis RX) {
        Remove_Limit_Lines(RX, "ALERT");
    }

    private void Remove_Limit_Lines(YAxis RX, String Label) {
        LimitLine Ln;

        while ((Ln = Find_Limit_Line(RX, Label)) != null)
            RX.removeLimitLine(Ln);

    }

    private void Paint_Open_Orders(List<Order> OO) {
        Chart_CS = findViewById(R.id.Candle_Chart);
        YAxis RAxis = Chart_CS.getAxisRight();

        Remove_Limit_Line_Orders(RAxis);

        if (OO.size() <= 0)
            return;


        for (int idx = 0; idx < OO.size(); ++idx)
        {
            LimitLine lml = new LimitLine(Float.parseFloat(OO.get(idx).getPrice()),  FullscreenActivity.Round_Number( OO.get(idx).getOrigQty() , 2 ) , "ORDER");

            if (OO.get(idx).getSide().toString().contains("SELL")) {
                lml.setLineColor(Color.RED);
            } else {
                lml.setLineColor(Color.GREEN);
            }

            lml.setLineWidth(FullscreenActivity.px2dp(this, 2f));
            lml.enableDashedLine(20f, 5f, 0f);
            lml.setTextColor(Color.WHITE);
            lml.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP );


            RAxis.addLimitLine(lml);
        }

        Refresh_Charts();

    }

    private void Paint_Open_Alerts ( )
    {
        Chart_CS = findViewById(R.id.Candle_Chart);
        YAxis RAxis = Chart_CS.getAxisRight();

        Remove_Limit_Line_Alerts(RAxis);

        if ( FullscreenActivity.Alerts_List == null )
            return ;

        if (FullscreenActivity.Alerts_List.size() <= 0)
            return;


        for (int idx = 0; idx < FullscreenActivity.Alerts_List.size(); ++idx)
        {
            if ( FullscreenActivity.Alerts_List.get(idx).Label.equals( Text_coin_RT )  ) {
                LimitLine lml = new LimitLine(Float.parseFloat(FullscreenActivity.Alerts_List.get(idx).Alert_Price), "",  "ALERT");

                lml.setLineColor(Color.BLUE);

                lml.setLineWidth(FullscreenActivity.px2dp(this, 2f));
                lml.disableDashedLine();
                lml.setTextColor(Color.TRANSPARENT);

                RAxis.addLimitLine(lml);
            }
        }
        Refresh_Charts();
    }


    private boolean Get_CandleStick()
    {
        MyDebug( "Main_Fragment" , "Get_CandleStick ");

        if (FullscreenActivity.Get_Ticket_Data(Text_coin_RT, false) == 1)
        {
            MyDebug( "Main_Fragment" , "Get_Trades ");

            TD = FullscreenActivity.Get_Trades(Text_coin_RT);
            return (true);
        }
        return false;
    }


    private void Paint_Chart() {

        MyDebug( "Main_Fragment" , "Paint_Chart... ");

        runOnUiThread(() ->
        {

            Chart_CS = findViewById(R.id.Candle_Chart);
            Chart_CS.setNoDataText("");

            Chart_CS.getLegend().setEnabled(false);
            Chart_CS.setAutoScaleMinMaxEnabled(true);

            Chart_CS.setDoubleTapToZoomEnabled(false);
            Chart_CS.setScaleEnabled(false);
            Chart_CS.setScaleXEnabled(true);
            Chart_CS.setScaleYEnabled(false);

            Chart_CS.setExtraBottomOffset(5);
            Chart_CS.setExtraTopOffset(5);
            Chart_CS.setExtraRightOffset(5);

            Chart_CS.setHighlightPerDragEnabled(false);
            Chart_CS.setHighlightPerTapEnabled(false);

            Chart_CS.setBackgroundColor(Color.TRANSPARENT);
            Chart_CS.setNoDataText("");

            XAxis xAxis = Chart_CS.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawGridLines(true);
            xAxis.setGridColor(Color.DKGRAY);

            xAxis.setTextSize( 12f );

            xAxis.setAxisLineColor(Color.DKGRAY);
            xAxis.setSpaceMax(1.5f);
            xAxis.setEnabled(true);


            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    long new_ts;

                    new_ts = ((long) value * Get_Time_Period()) + (First_TimeStampt);

                    Date date = new Date(new_ts);

                    SimpleDateFormat prettyFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    prettyFormat.setTimeZone(TimeZone.getDefault());
                    return (prettyFormat.format(date));
                }
            });

            YAxis leftAxis = Chart_CS.getAxisLeft();
            leftAxis.setEnabled(false);

            YAxis rightAxis = Chart_CS.getAxisRight();

            rightAxis.setLabelCount(9, false);
            rightAxis.setDrawGridLines(true);
            rightAxis.setDrawAxisLine(true);
            rightAxis.setAxisLineColor(Color.LTGRAY);
            rightAxis.setTextColor(Color.WHITE);

            rightAxis.setTextSize( 12f );

/*
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                rightAxis.setTextSize(Get_DP_Width() / FullscreenActivity.MIN_LITTLE_FONT_LANDSCAPE);

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                rightAxis.setTextSize(Get_DP_Width() / FullscreenActivity.MIN_LITTLE_FONT_PORTRAIT);
            }
*/
            rightAxis.setGridColor(Color.DKGRAY);
            rightAxis.setAxisLineColor(Color.DKGRAY);

            rightAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {

                    return (FullscreenActivity.Round_Price_Number( Text_coin_RT , new BigDecimal(value).toString() ) + "    ");
                }
            });

            /* Volume */

            Bar_CS = findViewById(R.id.Volume_Chart);

            Bar_CS.getDescription().setEnabled(false);
            Bar_CS.getLegend().setEnabled(false);
            Bar_CS.setAutoScaleMinMaxEnabled(true);
            Bar_CS.setTouchEnabled(false);
            Bar_CS.setNoDataText("");
            Bar_CS.setBackgroundColor(Color.TRANSPARENT);
            Bar_CS.setExtraBottomOffset(5);
            Bar_CS.setExtraTopOffset(5);
            Bar_CS.setExtraRightOffset(5);

            xAxis = Bar_CS.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawGridLines(true);

            rightAxis.setTextSize( 12f );
/*
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                xAxis.setTextSize(Get_DP_Width() / FullscreenActivity.MIN_LITTLE_FONT_LANDSCAPE);

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                xAxis.setTextSize(Get_DP_Width() / FullscreenActivity.MIN_LITTLE_FONT_PORTRAIT);
            }
*/
            xAxis.setGridColor(Color.DKGRAY);
            xAxis.setDrawAxisLine(true);
            xAxis.setAxisLineColor(Color.DKGRAY);
            xAxis.setSpaceMax(1.5f);
            xAxis.setEnabled(false);

            leftAxis = Bar_CS.getAxisLeft();
            leftAxis.setEnabled(false);

            rightAxis = Bar_CS.getAxisRight();
            rightAxis.setLabelCount(5, false);

            rightAxis.setDrawGridLines(true);
            rightAxis.setDrawAxisLine(true);
            rightAxis.setTextColor(Color.WHITE);
/*
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                rightAxis.setTextSize(Get_DP_Width() / FullscreenActivity.MIN_LITTLE_FONT_LANDSCAPE);

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                rightAxis.setTextSize(Get_DP_Width() / FullscreenActivity.MIN_LITTLE_FONT_PORTRAIT);
            }
*/
            rightAxis.setGridColor(Color.DKGRAY);
            rightAxis.setAxisLineColor(Color.DKGRAY);

            Show_Data_Period();

            Chart_CS.setOnChartGestureListener(new CoupleChartGestureListener(
                    Chart_CS, new Chart[]{Bar_CS}));

        });
    }

    private void Show_Data_Period(  )
    {

        Chart_CS.getDescription().setText( "Interval: " + FullscreenActivity.Current_Binance_Interval);
        Chart_CS.getDescription().setPosition(getResources().getDisplayMetrics().density * 10f, getResources().getDisplayMetrics().density * 17f);
        Chart_CS.getDescription().setTextAlign( Paint.Align.LEFT );
        Chart_CS.getDescription().setTextColor(Color.WHITE);
        Chart_CS.getDescription().setTextSize( 12f);
        Chart_CS.getDescription().setEnabled(true);

    }


    private int Get_Time_Period ( )
    {
        String[] some_array = getResources().getStringArray(R.array.Chart_Types);

        if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[0]) )
            return ( 60000  ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[1])  )
            return ( 60000 * 3  ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[2])  )
            return ( 60000 * 5   ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[3]) )
            return ( 60000 * 15  ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[4])  )
            return (60000 * 30  ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[5]) )
            return ( 60000 * 60 ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[6]) )
            return ( 60000 * 120 ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[7]) )
            return ( 60000 * 240 ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[8]) )
            return ( 60000 * 480 ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[9]) )
            return ( 60000 * 720 ) ;
        else if ( FullscreenActivity.Current_Binance_Interval.equals( some_array[10]) )
            return ( 60000 * 1440 ) ;

        return ( 60000 * 5   ) ;

    }

    private void Set_Line_Mode(ScatterDataSet St, boolean mode)
    {
        St.setAxisDependency(YAxis.AxisDependency.RIGHT);

        if (mode)
        {
            St.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            St.setColor(ContextCompat.getColor(getApplicationContext(), R.color.GR), 0);
            St.setScatterShapeHoleColor(ContextCompat.getColor(getApplicationContext(), R.color.DKGR));
        }
        else
        {
            St.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            St.setColor(ContextCompat.getColor(getApplicationContext(), R.color.RD), 0);
            St.setScatterShapeHoleColor(ContextCompat.getColor(getApplicationContext(), R.color.DKRD));
        }

        St.setScatterShapeHoleRadius(FullscreenActivity.px2dp  ( this , 6f )) ;
        St.setScatterShapeSize(FullscreenActivity.px2dp(this, 3f));
        St.setDrawValues(false);
    }


    private void Paint_Line_Chart(long opentime)
    {
        CombinedData CBData;
        CandleData   data1;

        if ( FullscreenActivity.candlesticksCache == null )
            return ;

        try
        {
            Chart_CS = findViewById(R.id.Candle_Chart);

            if ( Chart_CS == null )
                return ;

            ArrayList<CandleEntry> values = new ArrayList<>();
            List<BarEntry> values_vol     = new ArrayList<>();

            Candlestick cvalues = FullscreenActivity.candlesticksCache.get(opentime);

            if ( cvalues == null )
                return ;

            CBData = Chart_CS.getCombinedData();
            if (CBData != null)
            {
                data1 = Chart_CS.getCombinedData().getCandleData();
            } else {
                CBData = new CombinedData();
                data1  = null;
            }

            BarData data2 = Bar_CS.getData();

            if (data1 != null)
            {
                int item_index = (int) (opentime - First_TimeStampt) / Get_Time_Period();

                if (data1.getEntryCount() > item_index)
                   data1.removeEntry(item_index, 0);

                data1.notifyDataChanged();//

                data1.addEntry(
                      new CandleEntry(
                              item_index, Float.parseFloat(cvalues.getHigh()),
                              Float.parseFloat(cvalues.getLow()),
                              Float.parseFloat(cvalues.getOpen()),
                              Float.parseFloat(cvalues.getClose())), 0);

                data1.notifyDataChanged();//

                if (data2.getEntryCount() > item_index)
                    data2.removeEntry(item_index, 0);


                data2.notifyDataChanged();//

                data2.addEntry(
                      new BarEntry(item_index, Float.parseFloat(cvalues.getVolume())), 0);

                data2.notifyDataChanged();//


            }
            else {

                First_TimeStampt = opentime;

                values.add(new CandleEntry(
                        0, Float.parseFloat(cvalues.getHigh()),
                              Float.parseFloat(cvalues.getLow()),
                              Float.parseFloat(cvalues.getOpen()),
                              Float.parseFloat(cvalues.getClose())));

                CandleDataSet set1 = new CandleDataSet(values, "");

                set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set1.setColor(Color.rgb(80, 80, 80));
                set1.setShadowColor(Color.LTGRAY);
                set1.setShadowWidth(0f);
                set1.setIncreasingColor(ContextCompat.getColor(getApplicationContext(), R.color.GR));
                set1.setIncreasingPaintStyle(Paint.Style.FILL);
                set1.setDecreasingColor(ContextCompat.getColor(getApplicationContext(), R.color.RD));
                set1.setDecreasingPaintStyle(Paint.Style.FILL);
                set1.setNeutralColor(Color.LTGRAY);
                set1.setBarSpace(0.2f);
                set1.setValueTextColor(Color.TRANSPARENT);


                data1 = new CandleData(set1);
                data1.notifyDataChanged();//

                CBData.setData(data1);

                Chart_CS.setData(CBData);

                values_vol.add(new BarEntry(0, Float.parseFloat(cvalues.getVolume())));

                BarDataSet set2 = new BarDataSet(values_vol, "Time series");

                set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set2.setColor(Color.rgb(180, 180, 180));
                set2.setValueTextColor(Color.TRANSPARENT);

                data2 = new BarData(set2);
                data2.setBarWidth(0.5f);
                data2.notifyDataChanged();//

                Bar_CS.setData(data2);

            }
        }
        catch ( Exception e )
        {
            Log.i ( "Error Paint_Line_Chart " , e.getMessage()) ;
            //My_Toast( ( e.getMessage()));
        }
    }

    public void Paint_New_Candle_Line (CandlestickEvent response , Long openTime )
    {
        if ( FullscreenActivity.candlesticksCache == null )
            return ;

        if ( FullscreenActivity.candlesticksCache.size() <= 0 )
            return ;

        if ( ! response.getSymbol().equals(  Text_coin_RT ) ) {
            MyDebug ( "Candle Data" , "doesnt match : " + response.getSymbol() + " - " + Text_coin_RT ) ;
            return;
        }
        Candlestick updateCandlestick = FullscreenActivity.candlesticksCache.get(openTime);

        if (updateCandlestick == null) {
            updateCandlestick = new Candlestick();
        }

        updateCandlestick.setOpenTime(response.getOpenTime());
        updateCandlestick.setOpen(response.getOpen());
        updateCandlestick.setLow(response.getLow());
        updateCandlestick.setHigh(response.getHigh());
        updateCandlestick.setClose(response.getClose());
        updateCandlestick.setCloseTime(response.getCloseTime());
        updateCandlestick.setVolume(response.getVolume());
        updateCandlestick.setNumberOfTrades(response.getNumberOfTrades());
        updateCandlestick.setQuoteAssetVolume(response.getQuoteAssetVolume());
        updateCandlestick.setTakerBuyQuoteAssetVolume(response.getTakerBuyQuoteAssetVolume());
        updateCandlestick.setTakerBuyBaseAssetVolume(response.getTakerBuyQuoteAssetVolume());

        FullscreenActivity.candlesticksCache.put(openTime, updateCandlestick);

        Paint_Line_Chart(openTime);

        Refresh_Charts();
    }

    private void Refresh_Charts ( )
    {
        runOnUiThread(() ->
        {
            if ( Chart_CS != null )
            {
                Chart_CS.notifyDataSetChanged();
                Chart_CS.invalidate();
            }
            if ( Bar_CS !=  null )
            {
                Bar_CS.notifyDataSetChanged();
                Bar_CS.invalidate();
            }
        });
    }



    private void Enable_Candle_Stream(String coin_name)
    {

        Close_WS_CandleStick ( ) ;

        if ( ! Allow_RealTime_Sockets()  )
            return ;

        if ( FullscreenActivity.Use_KuCoin )
        {

        }
        else
        {

            CandlestickInterval CS = FullscreenActivity.Get_Binance_Interval();

            ws_candlestick_closeable = FullscreenActivity.client_ws.onCandlestickEvent(coin_name.toLowerCase(), CS, new BinanceApiCallback<CandlestickEvent>() {

                @Override
                public void onResponse(final CandlestickEvent response) {

                    if (Allow_RealTime_Sockets())
                    {
                        Show_Connection_State(true );

                        Long openTime = response.getOpenTime();
                        Paint_New_Candle_Line(response, openTime);

                    } else {
                        Close_WS_CandleStick();
                    }
                }

                @Override
                public void onFailure(final Throwable cause) {

                    My_Toast("[CandleStream] Failed internet connection....");

                    Close_WS_CandleStick();
                    Show_Connection_State(false );
                }
            });
        }
    }

    private void Add_Method_Chart( BigDecimal Value , int mode )
    {
        Chart_CS = findViewById(R.id.Candle_Chart);

        if ( Chart_CS == null )
            return ;

        YAxis RAxis = Chart_CS.getAxisRight();

        if ( mode == 0 )
        {
            Remove_Limit_Lines ( RAxis , "NEWORDER");
            LimitLine lml = new LimitLine( Value.floatValue() , Value.toString() , "NEWORDER");
            lml.setLineWidth(1f);
            lml.setLineColor(Color.YELLOW);
            lml.enableDashedLine(10f, 10f, 0f);
            lml.setTextColor(Color.WHITE);
            lml.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP );

            RAxis.addLimitLine(lml);
            Chart_CS.invalidate();

        }
        else if ( mode == 2 || mode == 4  )
        {
            LimitLine lml = Find_Limit_Line ( RAxis, "NEWORDER") ;
            if ( lml != null )
            {
                Remove_Limit_Lines ( RAxis , "NEWORDER");

                lml = new LimitLine(Value.floatValue(), Value.toString(), "NEWORDER");
                lml.setLineWidth(1f);
                lml.setLineColor(Color.YELLOW);
                lml.enableDashedLine(10f, 10f, 0f);
                lml.setTextColor(Color.WHITE);
                lml.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP );

                RAxis.addLimitLine(lml);
                Chart_CS.invalidate();

                if ( mode == 2 )
                {
                    if ( Mixed_Fragment != null )
                        Mixed_Fragment.Show_Buy_Dialog( Value.toString() );

                    Remove_Limit_Lines ( RAxis , "NEWORDER");

                }
            }
        }
    }

    private class CoupleChartGestureListener implements OnChartGestureListener {

        private Chart srcChart;
        private Chart[] dstCharts;
        boolean State_Pressed;


        private CoupleChartGestureListener(Chart srcChart, Chart[] dstCharts) {
            this.srcChart = srcChart;
            this.dstCharts = dstCharts;
        }


        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {


        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
        {
            State_Pressed = false;

            float tappedX = me.getX();
            float tappedY = me.getY();


            MPPointD point = Chart_CS.getTransformer(YAxis.AxisDependency.RIGHT).getValuesByTouchPoint(tappedX, tappedY);

            try {
                BigDecimal FP = new BigDecimal(point.y);
                Add_Method_Chart(new BigDecimal(Round_Price_Number(Text_coin_RT, FP.toString())), 2);
            } catch (Exception E) {
                MyDebug("Exception", "Eval: " + E.getMessage());
            }
        }

        @Override
        public void onChartLongPressed(MotionEvent me)
        {
            float tappedX = me.getX();
            float tappedY = me.getY();

            float xoff = Chart_CS.getViewPortHandler().contentRight() ;

            if( me.getRawX() >= xoff )
            {
                MPPointD point = Chart_CS.getTransformer(YAxis.AxisDependency.RIGHT).getownValuesByTouchPoint(true, tappedX, tappedY);

                try {
                    BigDecimal FP = new BigDecimal(point.y);
                    String RPrice = Round_Price_Number(Text_coin_RT, FP.toString());

                    Add_Method_Chart(new BigDecimal(RPrice), 0);

                    State_Pressed = true;

                }
                catch (Exception E)
                {
                    MyDebug("Exception", "Eval: " + E.getMessage());
                }
            }

        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

            if ( Mixed_Fragment != null )
                Mixed_Fragment.Change_Chart_Period ( );

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            syncCharts();

            if ( FullscreenActivity.Config_Data.Save_Charts )
                FullscreenActivity.Config_Data.Chart_Scale_X = Chart_CS.getViewPortHandler().getScaleX() ;
        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY)
        {
            if ( Chart_CS == null )
                return ;

            syncCharts();

            if (!State_Pressed)
                return;

            float mvalue = Chart_CS.getWidth() - (Chart_CS.getAxisRight().getMaxWidth() + (Chart_CS.getViewPortHandler().offsetRight() / 2));
            float tappedX = me.getX();

            if (me.getRawX() >= mvalue) {
                float tappedY = me.getY();

                MPPointD point = Chart_CS.getTransformer(YAxis.AxisDependency.RIGHT).getownValuesByTouchPoint(false, tappedX, tappedY);

                try {
                    BigDecimal FP = new BigDecimal(point.y);
                    String RPrice = Round_Price_Number(Text_coin_RT, FP.toString());


                    Add_Method_Chart( new BigDecimal(RPrice), 4);

                } catch (Exception E) {
                    MyDebug("Exception", "Eval: " + E.getMessage());
                }
            }

        }

        private void syncCharts()
        {

            if ( Chart_CS == null )
                return ;
            Matrix srcMatrix;
            float[] srcVals = new float[9];
            Matrix dstMatrix;
            float[] dstVals = new float[9];

            // get src chart translation matrix:
            srcMatrix = srcChart.getViewPortHandler().getMatrixTouch();
            srcMatrix.getValues(srcVals);

            // apply X axis scaling and position to dst charts:
            for (Chart dstChart : dstCharts) {
                if (dstChart.getVisibility() == View.VISIBLE) {
                    dstMatrix = dstChart.getViewPortHandler().getMatrixTouch();
                    dstMatrix.getValues(dstVals);
                    dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X];
                    dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X];
                    dstMatrix.setValues(dstVals);
                    dstChart.getViewPortHandler().refresh(dstMatrix, dstChart, true);
                }
            }
        }

    }

    public  List<Trade> Update_Chart_Trades ( )
    {
        TD = FullscreenActivity.Get_Trades(Text_coin_RT);
        if ( TD != null )
        {
            runOnUiThread(() -> Paint_Line_Trade(TD));
        }

        Refresh_Charts ( ) ;

        return ( TD ) ;

    }

    public void Update_Chart_Alerts ( boolean force )
    {
        if (spinner.getVisibility() != View.GONE && !force)
            return;

        runOnUiThread(() -> Paint_Open_Alerts());
    }


    private class Load_Data extends AsyncTask<Void, Integer, Boolean>
    {
        private Load_Data()
        {
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            MyDebug( "Main_Fragment" , "onPreexecute()");

            Main_Thread_Still_Running = true ;
            spinner.setVisibility(View.VISIBLE);

        }


        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            try {

                MyDebug( "Main_Fragment" , "doInBackground()");

                if(isCancelled())
                    return ( false ) ;

                Paint_Label ( ) ;
                Paint_Chart ( ) ;

                if ( Get_CandleStick (  ) )
                {
                    publishProgress(3);
                }

                MyDebug ( "Main_Fragment" , "End Load_Data") ;
                return ( true ) ;
            }
            catch ( Exception E )
            {
                FullscreenActivity.MyDebug( "Exception" , "In load_data.... " + E.getMessage());
                return ( false ) ;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            try {

                if (isCancelled())
                    return;

                if (values[0] == 3) {
                    Update_Candle_Chart();
                    Update_Chart_Open_Orders(true);
                    Update_Chart_Alerts(true);
                    Enable_Candle_Stream(Text_coin_RT);
                }
            } catch (Exception E) {
                FullscreenActivity.MyDebug("Exception", "In load_data.... " + E.getMessage());
                return ;
            }
        }

            @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);

            try {

                if(isCancelled())
                    return ;

                MyDebug( "Main_Fragment" , "onPostExecute()");

                spinner.setVisibility(View.GONE);

                Main_Thread_Still_Running = false ;

                Start_Fragments();
                Set_Chart_Timer();

            } catch (Exception E) {
                FullscreenActivity.MyDebug("Exception", "In load_data.... " + E.getMessage());
                return ;
            }

        }
    }

    private boolean Allow_RealTime_Sockets ( )
    {
        return ( FullscreenActivity.Allow_RealTime_Sockets( ) );
    }

    private boolean checkWifiOnAndConnected()
    {
        return ( FullscreenActivity.checkWifiOnAndConnected ( getApplicationContext() ) ) ;

    }


    private void My_Toast ( String Msg )
    {

        FullscreenActivity.My_Toast( Msg);
    }



    private void MyDebug ( String Type , String Message )
    {
        FullscreenActivity.MyDebug( Type , Message ) ;
    }

    private void Show_Connection_State ( boolean mode )
    {
        TabLayout Main_Tabs_Layout ;

        if (Global_Socket_Connection == mode ) //Optimize
            return ;

        Global_Socket_Connection = mode ;

        Main_Tabs_Layout = findViewById(R.id.tabs);

        if ( Main_Tabs_Layout == null )
            return ;

        runOnUiThread(() -> {

            if (mode)
                Main_Tabs_Layout.setSelectedTabIndicatorColor(Color.parseColor("#73D0F4"));
            else
                Main_Tabs_Layout.setSelectedTabIndicatorColor(Color.RED);

            ViewPager viewPager = findViewById(R.id.Panels_ID);
            if (viewPager != null && viewPager.getAdapter() != null) {
                viewPager.getAdapter().notifyDataSetChanged();
            }

        });

    }

    public static void Set_TV_Text ( Activity Act, TextView Vc , String Text )
    {
        Act.runOnUiThread(() -> {
            Vc.setText(Text);
        });
    }

    public static String Get_Tv_Text ( TextView Vc )
    {
        return FullscreenActivity.Get_Tv_Text( Vc ) ;
    }

    private static String Round_Price_Number(String Coin, String number) {
        return ( FullscreenActivity.Round_Price_Number(Coin, number));
    }

    public boolean Ping_Public_Websocket ( )
    {
        if (! FullscreenActivity.Use_KuCoin)
        {
            return ws_candlestick_closeable != null;
        }

        if ( FullscreenActivity.kucoinPublicWSClient != null)
        {
            MyDebug( "Websocket" , "Ping to Public WS");

            if ( FullscreenActivity.kucoinPublicWSClient.ping("PublicWSClient") == null)
            {
                MyDebug("Websocket", "Failed Public Websocket");
                return ( false );
            }
        }
        else
        {
            return ( false ) ;
        }

        return ( true ) ;
    }
}
