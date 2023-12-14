package com.crypto_tab;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.icu.math.BigDecimal;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.event.AccountUpdateEvent;
import com.binance.api.client.domain.event.AllMarketTickersEvent;
import com.binance.api.client.domain.event.OrderTradeUpdateEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.AggTrade;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.binance.api.client.domain.market.TickerStatistics;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.kucoin.sdk.KucoinClientBuilder;
import com.kucoin.sdk.KucoinPrivateWSClient;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.KucoinRestClient;
import com.kucoin.sdk.rest.request.OrderCreateApiRequest;
import com.kucoin.sdk.rest.response.AccountBalancesResponse;
import com.kucoin.sdk.rest.response.AllTickersResponse;
import com.kucoin.sdk.rest.response.MarketTickerResponse;
import com.kucoin.sdk.rest.response.OrderBookResponse;
import com.kucoin.sdk.rest.response.OrderCreateResponse;
import com.kucoin.sdk.rest.response.OrderResponse;
import com.kucoin.sdk.rest.response.Pagination;
import com.kucoin.sdk.rest.response.SymbolResponse;
import com.kucoin.sdk.rest.response.TradeHistoryResponse;

import java.io.Closeable;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class FullscreenActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    public static final String PREFS_NAME = "UData";
    public static final String ALERTS_NAME = "UData_Alerts";
    public static final int MAX_ALERTS_ALLOWED = 3 ;

    public static final int LITTLE_FONT = 0x01;
    public static final int MED_FONT = 0x02;
    public static final int BIG_FONT = 0x03;
    public static final int MID_BIG_FONT = 0x04 ;
    public static final int MID_MED_FONT = 0x05 ;

    public static final int BIG_FONT_LANDSCAPE = 30;
    public static final int BIG_FONT_PORTRAIT  = 32;

    public static final int MID_BIG_FONT_LANDSCAPE = 24;
    public static final int MID_BIG_FONT_PORTRAIT  = 21;

    public static final int MID_MED_FONT_LANDSCAPE = 18;
    public static final int MID_MED_FONT_PORTRAIT  = 16;

    public static final int MED_FONT_LANDSCAPE = 22;
    public static final int MED_FONT_PORTRAIT  = 19;

    public static final int LITTLE_FONT_LANDSCAPE     = 15;
    public static final int LITTLE_FONT_PORTRAIT      = 14;

    NavigationView navigationView ;
    SwitchCompat pdetect_drawerSwitch = null ;
    SwitchCompat kucoin_drawerSwitch = null ;


    boolean First_Time_Connect ;

    private static String[] Chart_Types_Array;

    public static boolean In_Pause_Mode = false;

    public static String Current_Binance_Interval;
    public static int  Sort_Mode ;

    int Times_To_Check = 0 ;
    public static boolean Use_KuCoin ;

    public static KucoinRestClient kucoinRestClient ;
    public static KucoinClientBuilder kucoin_builder ;
    public KucoinPrivateWSClient kucoinPrivateWSClient ;
    public static KucoinPublicWSClient kucoinPublicWSClient ;

    /// Subscriptions...

    public static BillingClient billingClient;

    public static boolean BUY_SELL_SUBSCRIPTION;
    public static SkuDetails SKUDetails_Compras;
    private final String Compras_SKU = "compras_001" ;

    static int Max_Buy_Times ;
    static long Toast_interval;

    static Toast MyToaster ;
    int last_kucoin_update ;

    public static class Alerts
    {
        String Label ;
        String  Alert_Price ;
        boolean WhengoesUp ;
        boolean repeat ;
    }

    public static List<Alerts> Alerts_List ;

    public static class Config_Data {
        public static class Keys_Data {
            String Private_Key;
            String Public_Key;
            String Kucoin_Private_Key;
            String Kucoin_Public_Key;
            String Kucoin_passPhrase;

            private void Clear() {
                Private_Key = "";
                Public_Key = "";
                Kucoin_Private_Key = "";
                Kucoin_Public_Key = "";
                Kucoin_passPhrase = "" ;
            }
        }

        public static ArrayList<String> Coin_Names = new ArrayList<String>();

        public static Keys_Data Config_Keys = new Keys_Data();

        public static String Min_Volume_Pump ;
        public static String Connection_Type;
        public static String Chart_Interval;

        public static boolean Save_Charts;
        public static boolean Notif_Sent;
        public static boolean Notif_Cancel;
        public static boolean Notif_Part_Filled;
        public static boolean Notif_Filled;

        public static String Filter_Coin ;

        public static String Notif_Sound;
        public static String Notif_Pump_Sound;

        public static String Percent_From_Sell_Stop_Loss;

        public static String price_increase;
        public static String price_decrease;
        public static String volume_variation;
        public static String min_price;

        public static boolean enable_pump_detection;

        public static boolean detect_price_increases;
        public static boolean detect_price_decreases;
        public static boolean detect_volume_variation;

        public static Integer Topten;

        public static float Chart_Scale_X;
    }

    public static class Coin_Data implements Comparable<Coin_Data> {

        public String symbol;
        public String priceChangePercent;
        public String lastPrice;
        public String volume;
        public String openPrice ;

        public String Price_Precision ;
        public String Qty_Precision   ;


        public Coin_Data ( String got_symbol , String got_lastPrice , String got_Volume , String got_priceChangePercent , String got_openPrice)
        {
            symbol = got_symbol ;
            lastPrice = got_lastPrice ;
            volume = got_Volume ;
            priceChangePercent = got_priceChangePercent ;
            if ( got_lastPrice.length() > 0 )
                openPrice = got_openPrice ;
        }


        public void setPriceChangePercent ( String Percent ) { priceChangePercent = Percent ;}
        public void setLastPrice ( String Price ) { lastPrice = Price ;}
        public void setVolume ( String Volume ) { volume = Volume ;}
        public void setOpenPrice ( String oPrice ) { openPrice = oPrice ;}


        public String getSymbol() {
            return (symbol);
        }
        public String getLastPrice() {
            return (lastPrice);
        }
        public String getVolume() {
            return (volume);
        }
        public String getOpenPrice() {
            return (openPrice);
        }
        public String getPriceChangePercent() {
            return (priceChangePercent);
        }

        @Override
        public int compareTo(@NonNull Coin_Data o) {
            return Float.valueOf(this.priceChangePercent).compareTo(Float.valueOf(o.priceChangePercent));
        }
    }

    public static List<Coin_Data> CList_Data = null ;


    static BinanceApiClientFactory factory;
    static BinanceApiRestClient client;
    static BinanceApiWebSocketClient client_ws;
    static String listenKey;

    static public boolean Global_Socket_Connection;


    static Main_Favs_Fragment Favs_Fragment;
    static Main_All_Fragment Alls_Fragment;
    static Charts_Mixed_Data_Fragment Chart_Mixed_F ;

    public static OrderBook OB;
    public static List<AggTrade> AT;
    public static List<Trade> LT;
    public static List<Order> OO;
    public static Account AC;

    public static NavigableMap<Long, Candlestick> candlesticksCache;


    private Closeable user_data_closeable;
    private Closeable ticket_data_closeable;


    public static final int PREFS_CODE = 0xe110;
    public static final int COIN_CODE = 0xe111;
    public static final int KEYS_CODE = 0xe112;

    private Timer Update_Data_Timer;

    /********* MENUS *****/
    private ActionBarDrawerToggle mDrawerToggle;
    Toolbar toolbar;


    public static Context App_Context;

    private void Set_Spinner ( int V )
    {
        ProgressBar spinner = findViewById(R.id.Main_ProgressBar);
        if ( spinner != null ) {
            spinner.setVisibility(V);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        setTheme(R.style.FullscreenTheme);

        DrawerLayout mDrawerLayout;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.fullscreenactivity);

        Set_Spinner ( View.GONE  ) ;

        getWindow().setNavigationBarColor(Color.BLACK);

        Clear_Varibs();

        Chart_Types_Array = getResources().getStringArray(R.array.Chart_Types);
        App_Context = getApplicationContext();

        MyDebug("Application", "onCreate method.");

        //ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        if ( navigationView != null ) {
            pdetect_drawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.pump_detect_id).getActionView();
            kucoin_drawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.use_kucoin_id).getActionView();
        }

        navigationView.setNavigationItemSelectedListener(item -> {

            Intent id;

            switch (item.getItemId()) {

                case R.id.kucoin_key_mn:

                    id = new Intent(this, Menu_Config_Kucoin_Keys.class);
                    startActivityForResult(id, KEYS_CODE);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                    break;

                case R.id.key_mn:

                    id = new Intent(this, Menu_Config_Keys.class);
                    startActivityForResult(id, KEYS_CODE);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                    break;


                case R.id.wallet_mn:

                    id = new Intent(this, Menu_My_Wallet.class);
                    startActivity(id);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;

                case R.id.open_order_mn:

                    id = new Intent(this, Menu_Open_Orders.class);
                    startActivity(id);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;

                case R.id.alerts_mn:

                    id = new Intent(this, Menu_Alerts.class);
                    startActivity(id);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;

                case R.id.settings_mn:

                    startActivityForResult(new Intent(this, Menu_Preferences.class), PREFS_CODE);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;

                case R.id.privacyPolicyButton:
                    id = new Intent(this, PrivacyPolicyActivity.class);
                    startActivity(id);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;

                case R.id.Subscribe:
                    if (!Get_Subscription_Buys(  )) {
                        Buy_Subscription( this );
                    }
                    break;

            }

            mDrawerLayout.closeDrawers();
            return true;
        });

        if ( pdetect_drawerSwitch != null ) {
            pdetect_drawerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Enable_Pump_Detection(true, false);
                } else {
                    Enable_Pump_Detection(false, false);
                }
                Save_Config_Data();
            });
        }

        MyDebug("Stating application...", "OnCreate mode.. ");

        Show_Connection_Status(false);

        Load_Config_Data ( ) ;
        Load_Alerts      ( ) ;

//        SwitchCompat KuCoindrawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.use_kucoin_id).getActionView();
        if ( kucoin_drawerSwitch != null ) {
            kucoin_drawerSwitch.setChecked(Use_KuCoin);
            kucoin_drawerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

                Save_Alerts();
                Save_Favourites();
                Enable_KuCoin(isChecked);
                Load_Favourites();
                Load_Alerts();
                Save_Config_Data();
                ReStart_Fragments();
            });
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(tempReceiver, new IntentFilter("data_between_activities"));

        Max_Buy_Times = 0;

        Connect_Billing();

        Register_All_Notification_Channels();

        Load_Exchange_Keys ( ) ;
        Begin_Binance_Work ( ) ;
        Start_Fragments    ( ) ;
        Start_Timer        ( ) ;

    }

    private void Start_Timer ( )
    {
        Update_Data_Timer = new Timer();

        Update_Data_Timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if ( !Allow_RealTime_Sockets ( ) )
                {
                     Update_Data ( ) ;
                }
                else
                {
                    if  ( ! Ping_Public_Websocket  ( ) )
                    {
                       Enable_Websockets ( ) ;
                       if ( Chart_Mixed_F !=null )
                       {
                           Chart_Mixed_F.Enable_Trades_Stream();
                           Chart_Mixed_F.Enable_OrderBook_Stream();
                       }
                    }
                }

                if ( ! Ping_Private_Websocket ( ) )
                {
                    User_Streams_Thread ( ) ;
                }

                ++ Times_To_Check ;

                if ( ( Times_To_Check % 2 ) == 0 && listenKey != null )
                {
                    MyDebug("Connection", "KeepAlive UserDataSTream ");
                    try
                    {
                        FullscreenActivity.client.keepAliveUserDataStream(listenKey);
                    }
                    catch (Exception e)
                    {
                        MyDebug("Keepalive", "Listenkey is not valid [" + e.getMessage() + "-");
                        Close_User_Data_Websocket();
                    }
                }

                if ( Times_To_Check > 10 )
                {
                    Check_Subscriptions();
                    Times_To_Check = 0;
                }

            }
        }, 0, Get_Timer_Refresh_Period());
    }

    public boolean Ping_Private_Websocket ( )
    {
        if (! Use_KuCoin)
        {
            return user_data_closeable != null;
        }

        if ( kucoinPrivateWSClient != null )
        {
            MyDebug( "Websocket" , "Ping to Private WS");

            if (kucoinPrivateWSClient.ping("PublicWSClient") == null)
            {
                MyDebug("Websocket", "Failed Private Websocket");
                return ( false ) ;
            }
        }
        else
        {
            MyDebug("Websocket", "Private Socket not connected.");
            return ( false ) ;
        }

        return ( true ) ;
    }

    public boolean Ping_Public_Websocket ( ) {

        if (! Use_KuCoin)
        {
            return ticket_data_closeable != null ;
        }

        if (kucoinPublicWSClient != null)
        {
            MyDebug( "Websocket" , "Ping to Public WS");

            if (kucoinPublicWSClient.ping("PublicWSClient") == null)
            {
                MyDebug("Websocket", "Failed Public Websocket");
                return ( false );
            }
        }
        else
        {
            MyDebug("Websocket", "Public Socket not connected.");
            return ( false ) ;
        }
        return ( true ) ;

    }



    private void Clear_Varibs ( )
    {
        Sort_Mode = 0 ;

        MyToaster = null ;

        Favs_Fragment = null ;
        Alls_Fragment = null ;
        listenKey = null;
        Toast_interval = - 1 ;
        Alerts_List = new ArrayList<>();
        CList_Data  = null ;
        Use_KuCoin    = false ;
        kucoinPrivateWSClient = null ;
        kucoinPublicWSClient  = null ;
        Chart_Mixed_F = null;
    }

    private void Enable_KuCoin ( boolean Enab )
    {

        Close_User_Data_Websocket   ( );
        Close_User_Data_Stream      ( );
        Close_Ticket_Data_Websocket ( );

        Clear_Varibs ( ) ;

        Use_KuCoin = Enab ;

    }

    private final BroadcastReceiver tempReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            MyDebug( "Application" , "BroadcastReceived");

            Bundle b = intent.getExtras();
            if (b != null)
            {
                if (Objects.equals(b.getString("Need_Save"), "true"))
                {
                    Save_Config_Data();
                    Save_Alerts ();
                }
                if (Objects.equals(b.getString("Need_Reload"), "true"))
                {
                    Save_Config_Data();
                    Save_Alerts ();
                    if ( Favs_Fragment != null )
                    {
                        Favs_Fragment.Show_Favourites ( ) ;
                    }
                }
            }
        }
    };

    @Override
    protected void onRestart()
    {
        super.onRestart();

        MyDebug( "Application" , "onRestart");

    }



    public static int Get_Timer_Refresh_Period() {
        return (10000);
    }

    private void Update_Data()
    {
        MyDebug( "Timer" , "Update timer reached... Get data.");

        Markets_Thread ( ) ;

        if (CList_Data == null)
        {
            MyDebug( "Connection" , "Update Data Failed. ");
            Show_Connection_Status(false );
            return;
        } else if ( CList_Data.size() == 0 )
        {
            Show_Connection_Status(false );
            return;
        }
        else
        {
            Show_Connection_Status(true );
        }

        MyDebug("Timer Timer", "Timer count reached....");
        Update_Coin_Label();
    }

    private void Update_Coin_Label()
    {
        if (Favs_Fragment != null)
        {
                  Favs_Fragment.Update_Coin_Label();
        }
        if (Alls_Fragment != null)
        {
                  Alls_Fragment.Update_Coin_Label();
        }
    }

    private static boolean checkWifiOnAndConnected( )
    {
        return checkWifiOnAndConnected (App_Context.getApplicationContext() ) ;
    }

    public static boolean checkWifiOnAndConnected( Context ctx ) {

        if ( ctx == null)
            return ( false ) ;

        ConnectivityManager cm =
                (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if ( activeNetwork == null)
            return ( false ) ;

        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

        return ( !isMobile) ;

        /*
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) {

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            return wifiInfo.getLinkSpeed() != -1;
        } else {
            return false;
        }
*/
    }

    private void Enable_Pump_Detection(boolean enable, boolean setview) {
        Config_Data.enable_pump_detection = enable;

        if (setview && pdetect_drawerSwitch != null )
        {
            pdetect_drawerSwitch.setChecked(enable);
        }
    }

    private void Show_Connection_Status( boolean mode)
    {

//        if (Global_Socket_Connection == mode ) //Optimize
//            return ;

        TabLayout Main_Tabs_Layout = findViewById(R.id.Main_TABS);

        if ( Main_Tabs_Layout == null )
            return ;

        Global_Socket_Connection = mode ;


        runOnUiThread(() -> {

            if (! mode ) {
                if (Main_Tabs_Layout != null) {
                    Main_Tabs_Layout.setSelectedTabIndicatorColor(Color.RED);
                }
            } else {
                if (Main_Tabs_Layout != null) {
                    Main_Tabs_Layout.setSelectedTabIndicatorColor(Color.parseColor("#73D0F4"));
                }
            }

            ViewPager viewPager = findViewById(R.id.Panels_ID);
            if(viewPager != null && viewPager.getAdapter() != null)
            {
                viewPager.getAdapter().notifyDataSetChanged();
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PREFS_CODE || requestCode == KEYS_CODE)
        {
            Save_Config_Data();
            if ( Alls_Fragment != null )
            {
                Alls_Fragment.Show_All_Coins ( ) ;
            }
        } else if (requestCode == COIN_CODE) {
            Save_Config_Data();
            if ( Favs_Fragment != null )
            {
                Favs_Fragment.Show_Favourites ( ) ;
            }
        } else {
            Save_Config_Data();
            if ( Favs_Fragment != null )
            {
                Favs_Fragment.Show_Favourites ( ) ;
            }
        }
    }

    private void Close_User_Data_Websocket()
    {
        if ( ! Use_KuCoin )
        {
            if (user_data_closeable != null) {
                MyDebug("Connection", "Closing User_Data_WebSocket");

                try {
                    user_data_closeable.close();
                    user_data_closeable = null ;

                    if (listenKey != null)
                        Close_User_Data_Stream();

                    listenKey = null ;

                } catch (Exception e) {
                    MyDebug("Connection", "Close user data websocket: " + e.getMessage());
                }
            }
        }
        else
        {
            try
            {
                MyDebug( "Close_User_Websocket" , "Closing Private websocket..");
                if ( kucoinPrivateWSClient != null )
                {
                    kucoinPrivateWSClient.close();
                }
                kucoinPrivateWSClient = null ;

            } catch (Exception e)
            {
                MyDebug( "Close_User_Websocket" , "Esception: " + e.getMessage() );
            }
        }

        kucoinPrivateWSClient = null ;
        user_data_closeable = null;
    }

    private void Close_Ticket_Data_Websocket( boolean force_kucoin )
    {
        if ( force_kucoin )
        {
            try
            {
                if ( kucoinPublicWSClient != null )
                {
                    MyDebug( "Close_User_Websocket" , "Closing Public websocket..");
                    kucoinPublicWSClient.close();
                }
                kucoinPublicWSClient = null ;

            } catch (Exception e)
            {
                MyDebug( "Close_User_Websocket" , "Exception: " + e.getMessage() );
                kucoinPublicWSClient  = null ;
            }
        }
        else
        {
              if (ticket_data_closeable != null) {
                MyDebug("Connection", "Not null... Closing Ticket_Data_WebSocket");

                try {
                    ticket_data_closeable.close();
                    ticket_data_closeable = null;

                } catch (Exception e) {
                    MyDebug("Connection", "Close Ticket data websocket: " + e.getMessage());
                    ticket_data_closeable = null;
                }
            }
        }
    }

    private void Close_Ticket_Data_Websocket()
    {
        MyDebug("Connection", "Closing Ticket_Data_WebSocket");

        Show_Connection_Status(false );

        if ( ! Use_KuCoin )
        {
            Close_Ticket_Data_Websocket ( false ) ;
        }
        else
        {
            Close_Ticket_Data_Websocket ( true ) ;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationManager notificationManager;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(tempReceiver);

        MyDebug("Application", "onDestroy.... ");

        if (Update_Data_Timer != null)
            Update_Data_Timer.cancel();

        Close_User_Data_Websocket();
        Close_Ticket_Data_Websocket();

        Save_Config_Data();
        Save_Alerts ();

    }

    public void onPause() {
        super.onPause();
        MyDebug( "Application" , "onPause");

        Set_Pause(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        MyDebug( "Application" , "onResume");

        Set_Pause(false);
    }



    public static boolean Is_In_Pause_Mode() {
        return (In_Pause_Mode);
    }


    void Set_Pause(boolean enable) {
        In_Pause_Mode = enable;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);


        if ( Favs_Fragment != null )
        {
            Favs_Fragment.Config_Changed( ) ;
        }
        if ( Alls_Fragment != null )
        {
            Alls_Fragment.Config_Changed( ) ;
        }
    }

    public static void Add_Coin_Name(String CoinName)
    {
        if (CoinName == null)
            return;

        if (CoinName.length() <= 0)
            return;

        if (CoinName.indexOf('/') >= 0)
            return;


        if (Config_Data.Coin_Names.indexOf(CoinName) < 0) {
            Config_Data.Coin_Names.add(CoinName);
        }
    }

    public static void Delete_Coin_Name(String CoinName) {
        if (CoinName == null)
            return;

        if (CoinName.length() <= 0)
            return;

        if (CoinName.indexOf('/') >= 0)
            return;

        Config_Data.Coin_Names.remove(CoinName);
    }

    private void Load_Favourites_Kucoin ( )
    {
        String dta;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        Config_Data.Coin_Names.clear();
        for (int i = 0; i < settings.getInt("StringArrayLength_KuCoin", 0); ++i) {
            dta = settings.getString("StringArrayElement_KuCoin" + i, "");

            Add_Coin_Name(dta);
        }

        if (Config_Data.Coin_Names.size() == 0) {
            Config_Data.Coin_Names.add("BTC-USDT");
        }

    }

    private void Load_Favourites_Binance ( )
    {
        String dta;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        Config_Data.Coin_Names.clear();
        for (int i = 0; i < settings.getInt("StringArrayLength", 0); ++i) {
            dta = settings.getString("StringArrayElement" + i, "");

            Add_Coin_Name(dta);
        }

        if (Config_Data.Coin_Names.size() == 0) {
            Config_Data.Coin_Names.add("BTCUSDT");
        }
    }

    private void Save_Favourites_Binance ( )
    {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        for (int i = 0; i < Config_Data.Coin_Names.size(); i++)
        {
            sharedPreferencesEditor.putString("StringArrayElement" + i, Config_Data.Coin_Names.get(i));
        }
        sharedPreferencesEditor.putInt("StringArrayLength", Config_Data.Coin_Names.size());
        sharedPreferencesEditor.apply();
    }

    private void Save_Favourites_Kucoin ( )
    {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        for (int i = 0; i < Config_Data.Coin_Names.size(); i++)
        {
            sharedPreferencesEditor.putString("StringArrayElement_KuCoin" + i, Config_Data.Coin_Names.get(i));
        }
        sharedPreferencesEditor.putInt("StringArrayLength_KuCoin", Config_Data.Coin_Names.size());
        sharedPreferencesEditor.apply();
    }

    private void Save_Favourites ( )
    {
        if ( Use_KuCoin )
            Save_Favourites_Kucoin ( ) ;
        else
            Save_Favourites_Binance ( ) ;
    }

    private void Load_Favourites ( )
    {
        if ( Use_KuCoin )
            Load_Favourites_Kucoin ( ) ;
        else
            Load_Favourites_Binance ( ) ;
    }


    private void Load_Config_Data()
    {
        String dta;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        Config_Data.Config_Keys.Clear();

        Config_Data.Config_Keys.Private_Key = settings.getString("Private_Key", "");
        Config_Data.Config_Keys.Public_Key = settings.getString("Public_Key", "");


        Config_Data.Config_Keys.Kucoin_Private_Key = settings.getString("Kucoin_Private_Key", "");
        Config_Data.Config_Keys.Kucoin_Public_Key  = settings.getString("Kucoin_Public_Key", "");
        Config_Data.Config_Keys.Kucoin_passPhrase  = settings.getString("Kucoin_PassPhrase", "");

/*
        Config_Data.Config_Keys.Kucoin_Public_Key =  "1ca3cfbf-1394-4d11-9ebc-f6314c5c1fe6";
        Config_Data.Config_Keys.Kucoin_Private_Key  =  "60468858de3a710006d74619";
        Config_Data.Config_Keys.Kucoin_passPhrase  ="javi1313";
*/

        Config_Data.Min_Volume_Pump = settings.getString("Min_Volume_Pump", "100000");

        Config_Data.Filter_Coin = settings.getString("Filter_Coin", "NONE");
        Config_Data.Connection_Type = settings.getString("Connection_Type", "PART");
        Config_Data.Chart_Interval = settings.getString("Chart_Interval", "5 minutes");

        Config_Data.Notif_Sound = settings.getString("Notif_Sound", "");
        Config_Data.Notif_Pump_Sound = settings.getString("Notif_Pump_Sound", "");

        Config_Data.Notif_Sent = settings.getBoolean("Notif_Sent", false);
        Config_Data.Save_Charts = settings.getBoolean("Save_Charts", true);
        Config_Data.Notif_Cancel = settings.getBoolean("Notif_Cancel", false);
        Config_Data.Notif_Part_Filled = settings.getBoolean("Notif_Part_Filled", true);
        Config_Data.Notif_Filled = settings.getBoolean("Notif_Filled", true);

        Config_Data.Percent_From_Sell_Stop_Loss = settings.getString("Percent_From_Sell_Stop_Loss", "0");

        Config_Data.price_increase = settings.getString("Price_Increase", "2");
        Config_Data.price_decrease = settings.getString("Price_Decrease", "-4");
        Config_Data.volume_variation = settings.getString("Volume_Variation", "5");
        Config_Data.min_price = settings.getString("Min_Price", "0.0000009");

        Config_Data.detect_price_increases = settings.getBoolean("Detect_Price_Increases", true);
        Config_Data.detect_price_decreases = settings.getBoolean("Detect_Price_Decreases", true);
        Config_Data.detect_volume_variation = settings.getBoolean("Detect_Volume_Variation", false);

        Config_Data.Topten = settings.getInt("TopTen", 10 );

        Config_Data.enable_pump_detection = settings.getBoolean("Enable_Pump_Detection", true);

        Config_Data.Chart_Scale_X = settings.getFloat("Chart_Scale_X", 5);

        Use_KuCoin = settings.getBoolean( "Use_KuCoin", false ) ;

        Load_Favourites();

        Enable_Pump_Detection(Config_Data.enable_pump_detection, true);

    }

    private void Load_Binance_Alerts ()
    {
        int i ;

        MyDebug ( "Alerts" , "Loading Alerts...") ;

        SharedPreferences settings = getSharedPreferences(ALERTS_NAME, 0);

        for ( i = 0; i < settings.getInt("Alerts_Size", 0); ++i)
        {
            Alerts dta = new Alerts();

            dta.Label = settings.getString("Alert_Coin_Name" + i, "");

            if ( dta.Label!= null )
            {
                dta.Alert_Price = settings.getString("Alert_Price" + i, "");
                dta.WhengoesUp = settings.getBoolean("Alert_WhengoesUp" + i, true);
                dta.repeat = settings.getBoolean("Alert_Repeat" + i, false );

                Alerts_List.add( dta ) ;

                MyDebug( "Alerts" , "Add Alert for " + dta.Label + " - Price: " + dta.Alert_Price + " - Up:" + dta.WhengoesUp );
            }
        }
    }

    private void Load_Alerts ()
    {
        if (Use_KuCoin)
           Load_KuCoin_Alerts ( );
        else
           Load_Binance_Alerts ( ) ;
    }


    public void Save_KuCoin_Alerts()
    {

        MyDebug ( "Alerts" , "Save Kucoin Alerts... [" + Alerts_List.size() + "]") ;

        SharedPreferences sharedPreferences = getSharedPreferences(ALERTS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putInt("Alerts_Kucoin_Size", Alerts_List.size());

        for (int i = 0; i < Alerts_List.size(); i++)
        {
            Alerts dta = Alerts_List.get(i) ;
            if ( dta != null )
            {
                MyDebug( "Alerts" , "Save Alert for Kucoin: " + dta.Label + " - Price: " + dta.Alert_Price + " - Up:" + dta.WhengoesUp );

                sharedPreferencesEditor.putString("Alert_Kucoin_Coin_Name" + i, dta.Label);
                sharedPreferencesEditor.putString("Alert_Kucoin_Price" + i,dta.Alert_Price);
                sharedPreferencesEditor.putBoolean("Alert_Kucoin_WhengoesUp" + i, dta.WhengoesUp);
                sharedPreferencesEditor.putBoolean("Alert_Kucoin_Repeat" + i, dta.repeat);
            }
        }


        sharedPreferencesEditor.apply();

    }
    private void Load_KuCoin_Alerts ()
    {
        int i ;

        MyDebug ( "Alerts" , "Loading Kucoin Alerts...") ;

        SharedPreferences settings = getSharedPreferences(ALERTS_NAME, 0);

        Alerts_List.clear();

        for ( i = 0; i < settings.getInt("Alerts_Kucoin_Size", 0); ++i)
        {
            Alerts dta = new Alerts();

            dta.Label = settings.getString("Alert_Kucoin_Coin_Name" + i, "");

            if ( dta.Label!= null )
            {
                dta.Alert_Price = settings.getString("Alert_Kucoin_Price" + i, "");
                dta.WhengoesUp = settings.getBoolean("Alert_Kucoin_WhengoesUp" + i, true);
                dta.repeat = settings.getBoolean("Alert_Kucoin_Repeat" + i, false );

                Alerts_List.add( dta ) ;

                MyDebug( "Alerts" , "Add Alert for Kucoin: " + dta.Label + " - Price: " + dta.Alert_Price + " - Up:" + dta.WhengoesUp );
            }
        }
    }



    public void Save_Binance_Alerts()
    {

        MyDebug ( "Alerts" , "Save Alerts... [" + Alerts_List.size() + "]") ;

        SharedPreferences sharedPreferences = getSharedPreferences(ALERTS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putInt("Alerts_Size", Alerts_List.size());

        for (int i = 0; i < Alerts_List.size(); i++)
        {
            Alerts dta = Alerts_List.get(i) ;
            if ( dta != null )
            {
                MyDebug( "Alerts" , "Save Alert for " + dta.Label + " - Price: " + dta.Alert_Price + " - Up:" + dta.WhengoesUp );

                sharedPreferencesEditor.putString("Alert_Coin_Name" + i, dta.Label);
                sharedPreferencesEditor.putString("Alert_Price" + i,dta.Alert_Price);
                sharedPreferencesEditor.putBoolean("Alert_WhengoesUp" + i, dta.WhengoesUp);
                sharedPreferencesEditor.putBoolean("Alert_Repeat" + i, dta.repeat);
            }
        }


        sharedPreferencesEditor.apply();

    }


    private void Save_Alerts ()
    {
        if (Use_KuCoin)
           Save_KuCoin_Alerts ( );
        else
           Save_Binance_Alerts ( ) ;
    }



    public void Save_Config_Data()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putString("Private_Key", Config_Data.Config_Keys.Private_Key);
        sharedPreferencesEditor.putString("Public_Key", Config_Data.Config_Keys.Public_Key);

        sharedPreferencesEditor.putString("Kucoin_Private_Key", Config_Data.Config_Keys.Kucoin_Private_Key);
        sharedPreferencesEditor.putString("Kucoin_Public_Key", Config_Data.Config_Keys.Kucoin_Public_Key);
        sharedPreferencesEditor.putString("Kucoin_PassPhrase", Config_Data.Config_Keys.Kucoin_passPhrase);

        sharedPreferencesEditor.putString("Filter_Coin", Config_Data.Filter_Coin);

        sharedPreferencesEditor.putString("Min_Volume_Pump", Config_Data.Min_Volume_Pump);
        sharedPreferencesEditor.putString("Connection_Type", Config_Data.Connection_Type);
        sharedPreferencesEditor.putString("Chart_Interval", Config_Data.Chart_Interval);
        sharedPreferencesEditor.putString("Notif_Sound", Config_Data.Notif_Sound);
        sharedPreferencesEditor.putString("Notif_Pump_Sound", Config_Data.Notif_Pump_Sound);

        sharedPreferencesEditor.putBoolean("Save_Charts", Config_Data.Save_Charts);
        sharedPreferencesEditor.putBoolean("Notif_Sent", Config_Data.Notif_Sent);
        sharedPreferencesEditor.putBoolean("Notif_Cancel", Config_Data.Notif_Cancel);
        sharedPreferencesEditor.putBoolean("Notif_Part_Filled", Config_Data.Notif_Part_Filled);
        sharedPreferencesEditor.putBoolean("Notif_Filled", Config_Data.Notif_Filled);

        sharedPreferencesEditor.putString("Percent_From_Sell_Stop_Loss", Config_Data.Percent_From_Sell_Stop_Loss);

        sharedPreferencesEditor.putString("Price_Increase", Config_Data.price_increase);
        sharedPreferencesEditor.putString("Price_Decrease", Config_Data.price_decrease);
        sharedPreferencesEditor.putString("Volume_Variation", Config_Data.volume_variation);
        sharedPreferencesEditor.putString("Min_Price", Config_Data.min_price);

        sharedPreferencesEditor.putBoolean("Detect_Price_Increases", Config_Data.detect_price_increases);
        sharedPreferencesEditor.putBoolean("Detect_Price_Decreases", Config_Data.detect_price_decreases);
        sharedPreferencesEditor.putBoolean("Detect_Volume_Variation", Config_Data.detect_volume_variation);

        sharedPreferencesEditor.putInt("TopTen", Config_Data.Topten);

        sharedPreferencesEditor.putFloat("Chart_Scale_X", Config_Data.Chart_Scale_X);

        sharedPreferencesEditor.putBoolean("Enable_Pump_Detection", Config_Data.enable_pump_detection);
        sharedPreferencesEditor.putBoolean("Use_KuCoin", Use_KuCoin );

        sharedPreferencesEditor.apply();

        Save_Favourites () ;
    }



    static public int Find_List_Data(String Coin_Name)
    {

        if (CList_Data == null)
            return (-1);

        for (int idx = 0; idx < CList_Data.size(); ++idx)
        {
            if (CList_Data.get(idx).getSymbol().equals(Coin_Name))
            {
                return (idx);
            }
        }

        return (-1);
    }

    public void Update_List_Data_Price(String Coin_Name, String Price, String Percent, String Volume, boolean check_pump , long Dtime )
    {
        Coin_Data CD ;

        if ( CList_Data == null )
            return ;

        int idx = Find_List_Data(Coin_Name);

        if ( idx < 0 )
        {
            CD = new Coin_Data( Coin_Name , Price , Volume , Percent , "" );
            CList_Data.add( CD );
            idx = Find_List_Data(Coin_Name);
        }

        if (idx >= 0)
        {
            CD = CList_Data.get(idx) ;

            CD.setPriceChangePercent(Percent);

            if (check_pump)
            {
//                if ( Charts_Main_Fragment.Update_Data_Timer == null )
                {
                    Check_Pump_Percent(Coin_Name, Price, CD.getLastPrice(), Volume, CD.getVolume(), Dtime);
                }
            }

            Check_Alert ( Coin_Name , Price ) ;

            CD.setLastPrice(Price);
            CD.setVolume(Volume);

        }
    }

    public static void Put_Alert ( String Coin_Name , String Alert_Price , String Current_Price , boolean Repeat )
    {
        Alerts Alr;
        BigDecimal pr1 ;
        BigDecimal pr2 ;

        if ( !FullscreenActivity.Get_Subscription_Buys() )
        {
            if ( Alerts_List.size() > MAX_ALERTS_ALLOWED )
            {
                My_Toast( "Only three alerts are allowed in the free version.\n\nPlease register it.");
                return ;
            }
        }

        if ( ! Find_Alert_Data (Coin_Name , Alert_Price )) {
            Alr = new Alerts();

            Alr.Label = Coin_Name;
            Alr.repeat = false;
            Alr.Alert_Price = Alert_Price;

            pr1 = new BigDecimal(Alert_Price);
            pr2 = new BigDecimal(Current_Price);

            if (pr1.compareTo(pr2) > 0)
                Alr.WhengoesUp = true;
            else
                Alr.WhengoesUp = false;

            Alr.repeat = Repeat;

            Alerts_List.add(Alr);

            My_Toast ( "Alert has been created.") ;

            MyDebug("Alerts", "Creating new alert for " + Coin_Name + " - Alert Price: " + Alr.Alert_Price + " - Current Price: " + Current_Price + " - Total: " + Alerts_List.size() + " - Type: " + Alr.WhengoesUp);
        }
        else
        {
            My_Toast ( "Alert not created. Already exists");
        }
    }

    static boolean Find_Alert_Data ( String Coin_Name , String Alert_Price )
    {
        for ( int idx = 0 ; idx < Alerts_List.size () ; ++ idx )
        {
            if ( Alerts_List.get(idx).Label.equals( Coin_Name ) )
            {
                BigDecimal bg1 = new BigDecimal( Alert_Price ) ;
                BigDecimal bg2 = new BigDecimal( Alerts_List.get(idx).Alert_Price ) ;

//                if ( bg1.divide( bg2 ).compareTo( BigDecimal.valueOf ( 1) ) ==0 )
                 if ( bg1.compareTo( bg2 ) == 0 )
                    return (true);
            }
        }

        return ( false ) ;

    }

    private void Check_Alert ( String Coin_Name , String Price )
    {
        int idx =  0 ;

        for ( idx = 0 ; idx < Alerts_List.size() ; ++ idx )
        {
            if ( !FullscreenActivity.Get_Subscription_Buys() )
            {
                if ( idx >= MAX_ALERTS_ALLOWED )
                {
                    break ;
                }
            }

            Alerts Al = Alerts_List.get(idx ) ;
            if ( Al.Label.equals( Coin_Name ) )
            {
                BigDecimal Pr = new BigDecimal(Price) ;
                BigDecimal Al_Pr = new BigDecimal( Al.Alert_Price) ;

                if ( ( Al.WhengoesUp && Pr.compareTo( Al_Pr ) >= 0 ) || ( ! Al.WhengoesUp && Pr.compareTo( Al_Pr ) <= 0 ) )
                {
                    Play_Alert_Sound ( Coin_Name , Price , Al.WhengoesUp ) ;
                    if ( ! Al.repeat )
                    {
                        Alerts_List.remove(idx);
                    }
                    else
                    {
                        Alerts_List.get(idx).WhengoesUp = !Alerts_List.get(idx).WhengoesUp ;
                    }
                }
            }
        }
    }

    private void Play_Alert_Sound ( String Coin_Name , String Price , boolean Mode_Up )
    {
        MyDebug( "Alerts" , "Play Alert Sound for " + Coin_Name + " - Price: " + Price + " - Mode: " + Mode_Up );

        Send_Alert_Notification ( Coin_Name, Round_Number(Price) );

    }

    public void Check_Pump_Percent(String Coin_Name, String Price, String Old_Price, String Volume, String Old_Volume , long DTime )
    {

        if ( ! FullscreenActivity.Config_Data.Filter_Coin.equals( "NONE")) {
            if (!Coin_Name.endsWith(FullscreenActivity.Config_Data.Filter_Coin)) {
                return;
            }
        }

        if ( Charts_Main_Fragment.Text_coin_RT.length() > 0 /*&&  Charts_Main_Fragment.Text_coin_RT.equals( ( Coin_Name ))*/ )
        {
            return;
        }

        BigDecimal LPrice = new BigDecimal((Price));
        BigDecimal OPrice = new BigDecimal((Old_Price));

        if (LPrice.compareTo(new BigDecimal(Config_Data.min_price)) <= 0)
            return;

        BigDecimal LVolume = new BigDecimal((Volume));
        BigDecimal OVolume = new BigDecimal((Old_Volume));

        if (LPrice.compareTo(BigDecimal.ZERO) == 0)
            return;
        if (OPrice.compareTo(BigDecimal.ZERO) == 0)
            return;
        if (LVolume.compareTo(BigDecimal.ZERO) == 0)
            return;
        if (OVolume.compareTo(BigDecimal.ZERO) == 0)
            return;

        if ( LVolume.compareTo( new BigDecimal( Config_Data.Min_Volume_Pump )) < 0 && (!new BigDecimal(Config_Data.Min_Volume_Pump).equals(new BigDecimal("-1"))))
            return ;


        BigDecimal Percent_Price_Change = ((LPrice.multiply(BigDecimal.valueOf(100))).divide(OPrice)).subtract(BigDecimal.valueOf(100));
        BigDecimal Percent_Volume_Change = ((LVolume.multiply(BigDecimal.valueOf(100))).divide(OVolume)).subtract(BigDecimal.valueOf(100));

        Percent_Price_Change = Percent_Price_Change.setScale(2, BigDecimal.ROUND_UP);
        Percent_Volume_Change = Percent_Volume_Change.setScale(2, BigDecimal.ROUND_UP);

        if (Percent_Price_Change.compareTo(new BigDecimal(Config_Data.price_increase)) >= 0) {
            if (Config_Data.detect_price_increases) {
                Send_Pump_Notification(0, Coin_Name, Round_Number(Price), Round_Number(Old_Price), Round_Number(Percent_Price_Change.toString()), Round_Number(Volume), Round_Number(Old_Volume), Round_Number(Percent_Volume_Change.toString()) , DTime );
            }
        } else if (Percent_Price_Change.compareTo(new BigDecimal(Config_Data.price_decrease)) <= 0) {
            if (Config_Data.detect_price_decreases) {
                Send_Pump_Notification(1, Coin_Name, Round_Number(Price), Round_Number(Old_Price), Round_Number(Percent_Price_Change.toString()), Round_Number(Volume), Round_Number(Old_Volume), Round_Number(Percent_Volume_Change.toString()) , DTime );
            }

        } else if (Percent_Volume_Change.compareTo(new BigDecimal(Config_Data.volume_variation)) >= 0) {
            if (Config_Data.detect_volume_variation) {
                Send_Pump_Notification(2, Coin_Name, Round_Number(Price), Round_Number(Old_Price), Round_Number(Percent_Price_Change.toString()), Round_Number(Volume), Round_Number(Old_Volume), Round_Number(Percent_Volume_Change.toString()) , DTime );
            }
        }

    }

    static public String Find_RT_Data(String Coin_Name) {
        int idx = Find_List_Data(Coin_Name);
        if (idx >= 0) {
            return (CList_Data.get(idx).getLastPrice());
        }

        return ("0");
    }

    public static int dp2px(Context ctx, float dp) {

        final float scale = ctx.getResources().getDisplayMetrics().density;

        return (int) (dp * scale + 0.5f);

    }

    public static int px2dp(Context ctx, float px) {
        final float scale = ctx.getResources().getDisplayMetrics().density;

        return (int) (px / scale + 0.5f);
    }

    public static Context Get_App_Context() {
        return (App_Context);
    }

/*
    public enum Toaster {
        INSTANCE;

        private final Handler handler = new Handler(Looper.getMainLooper());

        private void postMessage(final String message) {
            handler.post(
                    () ->
                    {
                        Toast toast = Toast.makeText(Get_App_Context(), message, Toast.LENGTH_LONG);
                        TextView v  = toast.getView().findViewById(android.R.id.message);
                        if( v != null) v.setGravity(Gravity.CENTER);
                        toast.show();
                    }
            );
        }
    }
*/
    public enum Toaster {

        INSTANCE;

        private final Handler handler = new Handler(Looper.getMainLooper());

        public void postMessage(final String message)
        {
            handler.post(() -> {
                try {
                    if (MyToaster != null) {
                        MyToaster.cancel();
                    }
                    MyToaster = Toast.makeText(Get_App_Context(), message, Toast.LENGTH_LONG);
                    MyToaster.show();
                }
                catch ( Exception E )
                {
                    MyDebug( "Toast Exception" , "E: " + E.getMessage());
                }
            });
        }
    }

    public static void My_Toast_Filtered(String Message, Exception e)
    {
        /*
        BinanceApiError Err = e.getError();

        if (Err != null) {
            if (Err.getCode() == -2014 && !Is_Keys_Enabled())
                return;
        }
        */

        My_Toast(Message + e.getMessage());

    }

    public static void My_Toast(String Message)
    {
        long tm = SystemClock.uptimeMillis()/1000;

        if ( tm <= ( Toast_interval + 1 ) )
        {
            Toast_interval = tm ;
            MyDebug( "Toast" , Message + "- Repeated message - " + tm + " - " + Toast_interval );
            return ;
        }

        Toast_interval = tm ;

        Toaster.INSTANCE.postMessage(Message);
    }

    public static String Round_Precision_Price_Number ( String number , int PosCoin )
    {
        String []  Price_Inc ;

        if ( CList_Data == null )
            return ( Round_Number ( number , 8 ) ) ;

        if ( CList_Data.get(PosCoin).Price_Precision == null )
            return ( Round_Number ( number , 8 ) ) ;

        Price_Inc = CList_Data.get(PosCoin).Price_Precision.split("\\.") ;


        if ( Price_Inc.length > 1 )
            return ( Round_Number ( number , Price_Inc[1].length() )) ;
        else
            return ( Round_Number ( number )) ;


    }

    public static String Round_Precision_Qty_Number ( String number , int PosCoin )
    {
        String []  Qty_Inc ;

        if ( CList_Data == null )
            return ( Round_Number ( number , 8 ) ) ;

        if ( CList_Data.get(PosCoin).Qty_Precision == null )
            return ( Round_Number ( number , 8 ) ) ;

        Qty_Inc = CList_Data.get(PosCoin).Qty_Precision.split("\\.") ;

        if ( Qty_Inc.length > 1 )
            return ( Round_Number ( number , Qty_Inc[1].length() )) ;
        else
            return ( Round_Number ( number )) ;

    }

    public static BigDecimal Get_Price_Precision ( String coin )
    {
        int coin_pos ;

        if ( CList_Data == null )
            return ( new BigDecimal( "0.00000001" ) ) ;

        if ( CList_Data.size() <= 0)
            return ( new BigDecimal( "0.00000001" ) ) ;

        coin_pos = Get_Round_Position ( coin ) ;
        if ( coin_pos >= 0 )
        {
            return ( new BigDecimal( CList_Data.get(coin_pos).Price_Precision )) ;
        }

        return ( new BigDecimal( "0.00000001" ) ) ;
    }

    public static BigDecimal Get_Qty_Precision ( String coin )
    {
        int coin_pos ;

        if ( CList_Data == null )
            return ( new BigDecimal( "0.00000001" ) ) ;

        if ( CList_Data.size() <= 0)
            return ( new BigDecimal( "0.00000001" ) ) ;

        coin_pos = Get_Round_Position ( coin ) ;
        if ( coin_pos >= 0 )
        {
            if ( CList_Data.get(coin_pos).Qty_Precision != null)
                return ( new BigDecimal( CList_Data.get(coin_pos).Qty_Precision )) ;
        }

        return ( new BigDecimal( "0.00000001" ) ) ;
    }

    public static int Get_Round_Position  ( String Coin )
    {
        int coin_pos ;

        if ( ( coin_pos = Find_List_Data ( Coin )) >= 0 )
            return coin_pos ;

        return ( -1 ) ;
    }

    public static String Round_Price_Number ( String Coin , String number )
    {
        int coin_pos ;

        if ( ( coin_pos = Get_Round_Position ( Coin ) ) >= 0 )
        {
            return ( Round_Precision_Price_Number ( number , coin_pos ) ) ;
        }

        return ( Round_Number( number )) ;
    }

    public static String Round_Qty_Number ( String Coin , String number )
    {
        int coin_pos ;

        if ( ( coin_pos = Get_Round_Position ( Coin ) ) >= 0 )
        {
            return ( Round_Precision_Qty_Number ( number , coin_pos ) ) ;
        }

        return ( Round_Number( number )) ;
    }


    public static String Round_Number ( String number )
    {
        return ( Round_Number ( number, 8 )) ;
    }

    public static String Round_Number(String number , int digits )
    {
        java.math.BigDecimal BD ;

        if ( number == null )
            return ( "0") ;

        try
        {
            BD = new java.math.BigDecimal(number).setScale(digits, RoundingMode.HALF_UP);

            if (BD.compareTo(java.math.BigDecimal.ZERO) == 0)
                return ("0");

           return ( BD.stripTrailingZeros().toPlainString());

//            return (BD.toString().replaceFirst("\\.0*$|(\\.\\d*?)0+$", "$1"));
        }
        catch ( Exception E )
        {
            MyDebug( "Exception" , "Round: " + E.getMessage());
            return ( "0") ;
        }
//          return ( BD.toString().replaceAll("()\\.0+$|(\\..+?)0+$", "$2"));
//          return ( BD.toString() ) ;

    }
    /*

    public static String Round_Number(String number)
    {

        if (number.indexOf(",") > 0)
        {
            number = number.replaceAll(",", "'");
            number = number.replaceAll("[.]", "");
            number = number.replaceAll("'", ".");
        }

        number = !number.contains(".") ? number : number.replaceAll("0*$", "").replaceAll("\\.$", "");

        NumberFormat nf_in = NumberFormat.getNumberInstance(Locale.US);
        double val = 0;
        try {
            val = nf_in.parse(number).doubleValue();
        } catch (Exception e) {
            MyDebug("Number convertion.", "Exception : " + e.getMessage());
        }

        NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.ITALY);
        nf_out.setMaximumFractionDigits(8);

        return (nf_out.format(val));
    }
   */

    public void Enable_Websockets_Kucoin_Private (  )
    {
        try
        {
            if ( kucoinPrivateWSClient == null )
            {
                MyDebug( "Websocket", "Enable Websocket Private connection....");
                kucoinPrivateWSClient = kucoin_builder.buildPrivateWSClient();
                MyDebug ( "Websocket", "Connected." ) ;
            }
        }
        catch ( Exception E )
        {
            kucoinPrivateWSClient = null ;
            My_Toast (  E.getMessage() );
            MyDebug ( "Enable_Websockets_Kucoin_Private", "Exception in kucoin... " + E.getMessage() ) ;
        }
    }


    public void Enable_User_Data_Streams()
    {
        if (!Is_Keys_Enabled())
        {
            return;
        }

        MyDebug("Application", "Enable User Data Stream.... ");

        Close_User_Data_Websocket();

        try
        {
            if (!Use_KuCoin )
            {
                listenKey = FullscreenActivity.client.startUserDataStream();
            }

        } catch (Exception e)
        {
            MyDebug("UserStreamData", e.getMessage());
            listenKey = null;
            return;
        }

        if (Use_KuCoin)
        {
            MyDebug("UserDataStream", "Enable... Kucoin Websockets Private.. ");

            Enable_Websockets_Kucoin_Private();

            if (kucoinPrivateWSClient != null) {
                kucoinPrivateWSClient.onOrderChange(response -> {

                    MyDebug("UserDataStream", "Got private data: " + response.toString());

                    if (response.getData().getType().equals("open")) {
                        Send_Notification(0, response.getData().getSymbol(), response.getData().getSide(), Round_Number(response.getData().getPrice().toString() ));
                        Charts_Main_Fragment.Update_Coin_Balance(  );
                        My_Toast("New order sent...");
                    } else if (response.getData().getType().equals("match")) {
                        Send_Notification(1, response.getData().getSymbol(), response.getData().getSide(), response.getData().getPrice().toString(), response.getData().getFilledSize().toString(), response.getData().getSize().toString());
                        My_Toast("Order partially filled...");
                    } else if (response.getData().getType().equals("filled")) {
                        Send_Notification(2, response.getData().getSymbol(), response.getData().getSide(), response.getData().getPrice().toString(), response.getData().getFilledSize().toString(), response.getData().getSize().toString());
                        Charts_Main_Fragment.Update_Coin_Balance(  );
                        My_Toast("Order filled...");
                    } else if (response.getData().getType().equals("canceled")) {
                        Send_Notification(3, response.getData().getSymbol(), response.getData().getSide(), response.getData().getPrice().toString());
                        Charts_Main_Fragment.Update_Coin_Balance(  );
                        My_Toast("Order canceled...");
                    }
                });
            }
        } else {
            user_data_closeable = client_ws.onUserDataUpdateEvent(listenKey, new BinanceApiCallback<UserDataUpdateEvent>() {
                public void onResponse(final UserDataUpdateEvent response) {
                    if (response.getEventType() == UserDataUpdateEvent.UserDataUpdateEventType.ACCOUNT_UPDATE) {
                        runOnUiThread(() -> {

                            AccountUpdateEvent accountUpdateEvent = response.getAccountUpdateEvent();

                            String btc_free = Menu_My_Wallet.Get_Total_USDT(accountUpdateEvent.getBalances());

                            TextView btc_funds = findViewById(R.id.btc_funds_id);
                            TextView dollar_funds = findViewById(R.id.dolar_funds_id);

                            String Curr_Price = Find_RT_Data("BTCUSDT");

                            if (btc_funds != null)
                                Set_TV_Text ( btc_funds , FullscreenActivity.Round_Number(btc_free));
                            if (dollar_funds != null)
                                Set_TV_Text ( dollar_funds , FullscreenActivity.Round_Number(Float.toString(Float.parseFloat(Curr_Price) * Float.parseFloat(btc_free))));
                        });

                    } else if (response.getEventType() == UserDataUpdateEvent.UserDataUpdateEventType.ORDER_TRADE_UPDATE) {
                        runOnUiThread(() -> {

                            OrderTradeUpdateEvent orderTradeUpdateEvent = response.getOrderTradeUpdateEvent();

                            if (orderTradeUpdateEvent.getOrderStatus() == OrderStatus.NEW) {
                                Send_Notification(0, orderTradeUpdateEvent.getSymbol(), orderTradeUpdateEvent.getSide().toString(), orderTradeUpdateEvent.getPrice());
                                Charts_Main_Fragment.Update_Coin_Balance(  );
                                My_Toast("New order sent...");
                            } else if (orderTradeUpdateEvent.getOrderStatus() == OrderStatus.PARTIALLY_FILLED) {
                                Send_Notification(1, orderTradeUpdateEvent.getSymbol(), orderTradeUpdateEvent.getSide().toString(), orderTradeUpdateEvent.getPrice(), orderTradeUpdateEvent.getAccumulatedQuantity(), orderTradeUpdateEvent.getOriginalQuantity());
                                My_Toast("Order partially filled...");
                            } else if (orderTradeUpdateEvent.getOrderStatus() == OrderStatus.FILLED) {
                                Send_Notification(2, orderTradeUpdateEvent.getSymbol(), orderTradeUpdateEvent.getSide().toString(), orderTradeUpdateEvent.getPrice(), orderTradeUpdateEvent.getAccumulatedQuantity(), orderTradeUpdateEvent.getOriginalQuantity());
                                Charts_Main_Fragment.Update_Coin_Balance(  );
                                My_Toast("Order filled...");
                            } else if (orderTradeUpdateEvent.getOrderStatus() == OrderStatus.CANCELED) {
                                Send_Notification(3, orderTradeUpdateEvent.getSymbol(), orderTradeUpdateEvent.getSide().toString(), orderTradeUpdateEvent.getPrice());
                                Charts_Main_Fragment.Update_Coin_Balance(  );
                                My_Toast("Order cancelled...");
                            }
                        });

                    }
                }

                public void onFailure(final Throwable cause) {
                    MyDebug("Connection", "Lost UserData connection.");
                    Close_User_Data_Websocket();
                }
            });
        }
    }

    private boolean Need_Check_Pump()
    {
        boolean check_pump = false;

        if ( First_Time_Connect )
            return ( false );

        if (pdetect_drawerSwitch != null)
        {
                if (pdetect_drawerSwitch.isChecked())
                    check_pump = true;
        }

        return (check_pump);
    }

    public void Enable_Websockets_Kucoin_Public (  )
    {
        Thread thread = new Thread(() -> {
            try
            {
                if ( kucoinPublicWSClient == null )
                {
                    MyDebug( "Websocket", "Enable Websocket Public connection....");
                    kucoinPublicWSClient = kucoin_builder.buildPublicWSClient();
                    MyDebug ( "Websocket", "Connected." ) ;
                }
            }
            catch ( Exception E )
            {
                kucoinPublicWSClient = null ;
                MyDebug ( "Enable_Websockets_Kucoin_Public", "Exception in kucoin... " + E.getMessage() ) ;
            }
        });

        thread.start();

        try
        {
            thread.join();
        } catch (Exception e)
        {
            MyDebug ( "Enable_Websockets_Kucoin_Public", "Exception in kucoin... " + e.getMessage() ) ;
        }
    }



    private void Enable_Websockets()
    {
        MyDebug("Connection", "Enabling websockets for markets.");

        First_Time_Connect = true;

        Close_Ticket_Data_Websocket();

        if ( ! Allow_RealTime_Sockets () )
        {
            First_Time_Connect = false;
            return;
        }

        if (CList_Data == null)
        {
            MyDebug("Data", "Markets data is null... Reading markets data....");

            Get_Markets_Data();

            First_Time_Connect = false;

            if (CList_Data == null)
                return;
        }

        if ( Use_KuCoin)
            Enable_Kucoin_WSockets();
        else
            Enable_Binance_WSockets();

    }
    private void Enable_Kucoin_WSockets ( )
    {
        last_kucoin_update = (int) SystemClock.uptimeMillis()/1000;

        Enable_Websockets_Kucoin_Public ( ) ;

        if ( kucoinPublicWSClient != null )
        {
            Show_Connection_Status(true );

            try
            {
/*
                kucoinPublicWSClient.onTicker( response -> {

                    if ( ! Use_KuCoin)
                    {
                        Close_Ticket_Data_Websocket( true );
                        return;
                    }

                    if ( CList_Data == null )
                        return ;

                    if (!Allow_RealTime_Sockets())
                    {
                        Close_Ticket_Data_Websocket( true );
                        return;
                    }

                    if ( ! Check_Coin_Valid ( response.getSubject()))
                        return ;

                    String Change_Percent ;
                    String Full_Volume ;

                    int idx = Find_List_Data(response.getSubject());
                    if ( idx < 0 )
                    {
                        return ;
                    }

                    Coin_Data CD = CList_Data.get(idx) ;
                    Change_Percent = new BigDecimal( response.getData().getPrice()).multiply( new BigDecimal( 100 )).divide( new BigDecimal(CD.openPrice )).subtract( new BigDecimal( 100 )). setScale( 2 , 2 ).toString();

//                    Full_Volume = new BigDecimal( CD.getVolume()).add( new BigDecimal( response.getData().getPrice()).multiply( new BigDecimal( response.getData().getSize()))).toString();
                    Full_Volume = new BigDecimal( CD.getVolume()).add( new BigDecimal(  response.getData().getSize())).toString();

                    Update_List_Data_Price(response.getSubject(),
                            response.getData().getPrice().toString(),
                            Change_Percent,
                            Full_Volume, Need_Check_Pump() , Long.parseLong( response.getData().getSequence()));


                    First_Time_Connect = false ;
                    if ( ( last_kucoin_update + 1 ) < (int)( SystemClock.uptimeMillis()/1000 ) ) //update each  second.
                    {
                        last_kucoin_update = (int) SystemClock.uptimeMillis() / 1000;

                        if (Sort_Mode == 0 )
                        {
                            msort(CList_Data);
                        } else {
                            Collections.sort(CList_Data);
                        }

                        Update_Coin_Label();

                    }
                } , "all") ;
*/

                kucoinPublicWSClient.onSnapshot ( response -> {

                    if ( ! Use_KuCoin)
                    {
                        Close_Ticket_Data_Websocket( true );
                        return;
                    }

                    if ( CList_Data == null )
                        return ;

                    if (!Allow_RealTime_Sockets())
                    {
                        Close_Ticket_Data_Websocket( true );
                        return;
                    }

                    if ( ! Check_Coin_Valid ( response.getData().getData().getSymbol() ))
                        return ;


                    Update_List_Data_Price(response.getData().getData().getSymbol(),
                                            response.getData().getData().getLastTradedPrice().toString(),
                                            response.getData().getData().getChangeRate().multiply( java.math.BigDecimal.valueOf(100)) .toString(),
                                            response.getData().getData().getVol().toString(), Need_Check_Pump() , response.getData().getData().getDatetime());


                    First_Time_Connect = false ;
                    if ( ( last_kucoin_update + 1 ) < (int)( SystemClock.uptimeMillis()/1000 ) ) //update each  second.
                    {
                        last_kucoin_update = (int) SystemClock.uptimeMillis() / 1000;

                        if (Sort_Mode == 0 )
                        {
                            msort(CList_Data);
                        } else {
                            Collections.sort(CList_Data);
                        }

                        Update_Coin_Label();

                    }
//                  Show_Connection_Status(true );
                } , "USDS,BTC,ALTS" );



            }
            catch ( Exception E )
            {
                MyDebug("Onticket exception", E.getMessage());
                Close_Ticket_Data_Websocket( true );
            }
        }
        else
        {
            Show_Connection_Status(false );
        }
    }

    private void Enable_Binance_WSockets ( )
    {

        ticket_data_closeable = client_ws.onAllMarketTickersEvent(new BinanceApiCallback<List<AllMarketTickersEvent>>()
        {
            public void onResponse(final List<AllMarketTickersEvent> response)
            {
                if ( Use_KuCoin)
                {
                    Close_Ticket_Data_Websocket( false );
                    return;
                }

                if (!Allow_RealTime_Sockets())
                {
                    Close_Ticket_Data_Websocket ( false );
                    return;
                }

                for (int idx = 0; idx < response.size(); ++idx)
                {
                    if ( new BigDecimal( response.get(idx).getBestAskPrice()).compareTo(BigDecimal.ZERO) == 0 )
                    {
                        continue ;
                    }

                    if ( ! Check_Coin_Valid ( response.get(idx).getSymbol() ))
                        continue ;

                    Update_List_Data_Price(response.get(idx).getSymbol(), response.get(idx).getCurrentDaysClosePrice(), response.get(idx).getPriceChangePercent(), response.get(idx).getTotalTradedBaseAssetVolume(), Need_Check_Pump() , response.get(idx).getEventTime());
                }

                First_Time_Connect = false ;

                if ( Sort_Mode == 0 )
                {
                    msort(CList_Data);
                }
                else
                {
                    Collections.sort(CList_Data);
                }

                Show_Connection_Status(true );

                Update_Coin_Label ( ) ;

            }

            public void onFailure(final Throwable cause)
            {
                MyDebug("Connection", ("Markets Web socket failed: [" + cause.getMessage() + "]"));
                My_Toast ( "Connection Lost: " + cause.getMessage () ) ;
                Close_Ticket_Data_Websocket( false );
            }
        });
    }

    private static <T extends Comparable<? super T>> void msort(List<T> list) {
        Object[] a = list.toArray();
        Arrays.sort(a);
        ListIterator<T> i = list.listIterator();
        for (int j=a.length; j>0 ; j--) {
            i.next();
            i.set((T)a[j-1]);
        }
    }


    public void Send_Notification(int type, String Symbol, String Side, String Price)
    {
        Send_Notification(type, Symbol, Side, Price, "", "");

    }

    public void Send_Notification(int type, String Symbol, String Side, String Price, String filled, String total) {
        String Message;

        Price = Round_Number ( Price );

        switch (type) {
            case 0:
                if (Config_Data.Notif_Sent) {
                    Message = "Order sent...";
                    Send_Notificacion(Symbol, Side, Price, Message);
                }
                break;

            case 1:
                if (Config_Data.Notif_Part_Filled) {
                    Message = "Partially filled (" + FullscreenActivity.Round_Number(filled) + "/" + FullscreenActivity.Round_Number(total) + ")";

                    Send_Notificacion(Symbol, Side, Price, Message);
                }
                break;

            case 2:
                if (Config_Data.Notif_Filled) {
                    Message = "Order filled (" +  FullscreenActivity.Round_Number(total) + ")";
                    Send_Notificacion(Symbol, Side, Price, Message);
                }

                break;

            case 3:
                if (Config_Data.Notif_Cancel) {
                    Message = "Order cancelled...";
                    Send_Notificacion(Symbol, Side, Price, Message);
                }
                break;

        }
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }


    public void Send_Notificacion(String Symbol, String Type, String Price, String message) {
        RemoteViews contentView;
        Notification notification;
        NotificationManager notificationManager;
        NotificationCompat.Builder mBuilder;
        int NotificationID = 1005;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, Get_Channel_ID());

        Uri defaultSoundUri = Uri.parse(Config_Data.Notif_Sound);

        contentView = new RemoteViews(getPackageName(), R.layout.notifications_layout);

        contentView.setTextViewText(R.id.title, Symbol);
        contentView.setTextViewText(R.id.type_order, Type);
        contentView.setTextViewText(R.id.price_order, Price);
        contentView.setTextViewText(R.id.result_id, message);
        contentView.setImageViewResource(R.id.image_id, R.mipmap.ic_launcher);

        mBuilder.setSmallIcon(R.drawable.bitcoin_trans_bw);
        mBuilder.setContentTitle(Symbol);
        mBuilder.setContentText(Type + " Price: " + Price + " - " + message);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        mBuilder.setSound(defaultSoundUri);


        mBuilder.setOnlyAlertOnce(false);
        mBuilder.build().flags = NotificationManager.IMPORTANCE_HIGH;
        mBuilder.setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_SMALL);
        mBuilder.setContent(contentView);

        Intent resultIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        resultIntent.setPackage(null); // The golden row !!!
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent pendingIntent = PendingIntent.getActivity( this , 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pendingIntent);  //builder is the notificationBuilder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(Get_Channel_ID());
        }

        notification = mBuilder.build();
        notification.bigContentView = contentView;
        notificationManager.notify(NotificationID, notification);

    }

    public static String Get_Channel_Pump_ID() {
        return ("CryptoPanel_Notif_Channel" + "PMP");

    }

    public static String Get_Channel_ID() {
        return ("CryptoPanel_Notif_Channel");

    }
    public static String Get_Alerts_Channel_ID() {
        return ("CryptoPanel_Alerts_Notif_Channel");

    }

     public void Send_Pump_Notification(int type, String Symbol, String Price, String Old_Price, String Price_Change, String Volume, String Old_Volume, String Volume_Change  , long DTime )
    {

        RemoteViews contentView;
        Notification notification;
        NotificationManager notificationManager;
        NotificationCompat.Builder mBuilder;
        String Pr_Chg;

        int NotificationID = (int) SystemClock.uptimeMillis();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this, Get_Channel_Pump_ID());

        Uri defaultSoundUri = Uri.parse(Config_Data.Notif_Pump_Sound);

        contentView = new RemoteViews(getPackageName(), R.layout.notifications_pump_layout);

        Current_Binance_Interval = Config_Data.Chart_Interval;

        int idx = Find_List_Data(Symbol);
        if (idx >= 0) {
            Pr_Chg = CList_Data.get(idx).getPriceChangePercent();
        } else {
            Pr_Chg = Price_Change;

        }

        MyDebug( "Activity_Creation" , "Context [" + this + "] Class [" + Charts_Main_Fragment.class +"]" );

        Intent id = new Intent(getApplicationContext(), Charts_Main_Fragment.class);


        id.putExtra("CoinName", Symbol);
        id.putExtra("Value", Price);
        id.putExtra("Change", Pr_Chg);


//        final PendingIntent pendingIntent = PendingIntent.getActivities( getApplicationContext(), NotificationID,  new Intent[]{backIntent, id}, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE );

        PendingIntent pendingIntent =  PendingIntent.getActivity( getApplicationContext() , NotificationID, id, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE );

        contentView.setImageViewResource(R.id.image_id, android.R.drawable.star_on);

        Date timeD = new Date(DTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        String Time = sdf.format(timeD);//Timestamp to HH:mm:ss format


        contentView.setTextViewText(R.id.title, Symbol);
        if (type == 0) {
            contentView.setTextViewText(R.id.type, "Possible pump by price increase.");
            contentView.setTextViewText(R.id.price_id, "Price: " + Price);
            contentView.setTextViewText(R.id.old_price_id, "Old Price: " + Old_Price);
            contentView.setTextViewText(R.id.result_id, Price_Change + " % - " + Time );
            contentView.setTextColor(R.id.result_id, ContextCompat.getColor(getApplicationContext(), R.color.GR));
        } else if (type == 1) {
            contentView.setTextViewText(R.id.type, "Possible pump by price decrease.");
            contentView.setTextViewText(R.id.price_id, "Price: " + Price);
            contentView.setTextViewText(R.id.old_price_id, "Old Price: " + Old_Price);
            contentView.setTextViewText(R.id.result_id, Price_Change + " % - " + Time ) ;
            contentView.setTextColor(R.id.result_id, ContextCompat.getColor(getApplicationContext(), R.color.RD));
        } else if (type == 2) {
            contentView.setTextViewText(R.id.type, "Possible pump by volume variation.");
            contentView.setTextViewText(R.id.price_id, "Volume: " + Volume);
            contentView.setTextViewText(R.id.old_price_id, "Old Volume: " + Old_Volume);
            contentView.setTextViewText(R.id.result_id, Volume_Change + " % Price Change: " + Price_Change + " % - " + Time);
            contentView.setTextColor(R.id.result_id, Color.BLACK);
        }


        mBuilder.setSmallIcon(R.drawable.bitcoin_trans_bw);

        mBuilder.setContentTitle(Symbol);
        mBuilder.setContentText("Possible Pump detected Price: " + Price + " - " + Price_Change + " %");
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        mBuilder.setSound(defaultSoundUri);
        mBuilder.setOnlyAlertOnce(false);
        mBuilder.build().flags = NotificationManager.IMPORTANCE_HIGH;
        mBuilder.setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_SMALL);
        mBuilder.setContent(contentView);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setCustomBigContentView( contentView );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(Get_Channel_Pump_ID());
        }

        notification = mBuilder.build();
        notification.bigContentView = contentView;
        notificationManager.notify(NotificationID, notification);

    }

    public void Send_Alert_Notification( String Symbol, String Price )
    {
        RemoteViews contentView;
        Notification notification;
        NotificationManager notificationManager;
        NotificationCompat.Builder mBuilder;
        String Pr_Chg;

        int NotificationID = (int) SystemClock.uptimeMillis();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this, Get_Alerts_Channel_ID());

        Uri defaultSoundUri = Uri.parse(Config_Data.Notif_Pump_Sound);

        contentView = new RemoteViews(getPackageName(), R.layout.notifications_alert);

        Current_Binance_Interval = Config_Data.Chart_Interval;

        int idx = Find_List_Data(Symbol);
        if (idx >= 0) {
            Pr_Chg = CList_Data.get(idx).getPriceChangePercent();
        } else {
            Pr_Chg = "";
        }

        Intent id = new Intent(this, Charts_Main_Fragment.class);

        id.putExtra("CoinName", Symbol);
        id.putExtra("Value", Price);
        id.putExtra("Change", Pr_Chg);

        PendingIntent pendingIntent =  PendingIntent.getActivity( this , NotificationID, id, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE );
        contentView.setImageViewResource(R.id.image_id, android.R.drawable.star_on);

        contentView.setTextViewText(R.id.title, Symbol);
        contentView.setTextViewText(R.id.type, "Price Alert has been detected.");
        contentView.setTextViewText(R.id.alert_price_id, "Price: " + Price );
        contentView.setImageViewResource(R.id.image_id, android.R.drawable.btn_star_big_off);

        mBuilder.setSmallIcon(R.drawable.bitcoin_trans_bw);

        mBuilder.setContentTitle(Symbol);
        mBuilder.setContentText("Price Alert has been detected.");
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        mBuilder.setSound(defaultSoundUri);
        mBuilder.setOnlyAlertOnce(false);
        mBuilder.build().flags = NotificationManager.IMPORTANCE_HIGH;
        mBuilder.setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_SMALL);
        mBuilder.setContent(contentView);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setCustomBigContentView( contentView );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(Get_Alerts_Channel_ID());
        }

        notification = mBuilder.build();
        notification.bigContentView = contentView;
        notificationManager.notify( Symbol, NotificationID, notification);

    }

    static public List<AggTrade> Get_Trades_Data(final String Coin) {
        AT = null;

        Thread thread = new Thread(() -> {
            try {
                    if ( Use_KuCoin )
                    {
                        try {

                            List<TradeHistoryResponse> TH = kucoinRestClient.historyAPI().getTradeHistories( Coin ) ;
                            AT = new ArrayList<>();

                            for ( int idx = 0 ; idx < TH.size() ; ++ idx ) {

                                AggTrade AGT = new AggTrade();

                                AGT.setPrice( TH.get(idx).getPrice().toString());
                                AGT.setQuantity( TH.get(idx).getSize().toString());
                                AGT.setTradeTime( TH.get(idx).getTime()/1000000);
                                AGT.setAggregatedTradeId( Long.parseLong(TH.get(idx).getSequence()));
                                if ( TH.get(idx).getSide().equals("buy"))
                                    AGT.setBuyerMaker( false );
                                else
                                    AGT.setBuyerMaker( true  );

                                AT.add( AGT ) ;
                            }
                        } catch (Exception E) {

                            MyDebug( "Connection" , "Error: " + E.getMessage());
                            My_Toast("Trades: " + E.getMessage());
                            AT=  null ;
                        }
                    }
                    else
                    {
                         AT = client.getAggTrades(Coin, null, 100, null, null);
                    }
            } catch (Exception e)
            {
                MyDebug( "Connection" , "Error: " + e.getMessage());
                My_Toast("Trades: " + e.getMessage());
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "Get_Trades_Exception : " + e.getMessage());
        }

        return (AT);
    }

    static public void Cancel_Open_Order ( Order OID )
    {
            if ( Use_KuCoin)
            {
                Cancel_Open_Order(new CancelOrderRequest(OID.getSymbol(),OID.getClientOrderId()));
            }
            else
            {
                Cancel_Open_Order(new CancelOrderRequest(OID.getSymbol(),OID.getOrderId()));
            }
    }


    static public List<Order> Get_Open_Orders(final String Coin) {

        OO = null;

        if (!Is_Keys_Enabled())
            return (OO);

        Thread thread = new Thread(() -> {
            try {

                if (!Use_KuCoin)
                {
                    OO = client.getOpenOrders(new OrderRequest(Coin));
                }
                else
                {
                    Pagination<OrderResponse> PO = kucoinRestClient.orderAPI().listOrders  ( Coin , null, null , "TRADE", "active", null , null , 10 , 1 );

                    if ( PO != null )
                    {
                        /*XXX**/
                        OO = new ArrayList<>() ;

                        for ( int idx = 0 ; idx < PO.getItems().size() ; ++ idx )
                        {
                            Order Abl  ;

                            Abl = new Order();
                            Abl.setClientOrderId( PO.getItems().get(idx).getId());
                            Abl.setPrice ( PO.getItems().get(idx).getPrice().toString());
                            Abl.setOrigQty( PO.getItems().get(idx).getSize().toString()) ;

                            if (PO.getItems().get(idx).getSide().toUpperCase(Locale.ROOT).equals( "BUY" ))
                                Abl.setSide  ( OrderSide.BUY ) ;
                            else
                                Abl.setSide  ( OrderSide.SELL ) ;

                            Abl.setSymbol( PO.getItems().get(idx).getSymbol());

                            OO.add( Abl ) ;
                        }
                    }
                    else {
                        MyDebug ( "Open Orders" , "No Open Orders") ;
                        OO = null ;
                    }
                }
            } catch (Exception e) {
//                My_Toast_Filtered("Open Orders: ", e);
                MyDebug("Connection", "getOpenOrders:" + e.getMessage());
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "Get_Open_Orders_Exception : " + e.getMessage());
        }

        return (OO);
    }

    static public List<Trade> Get_All_Trades(final String Coin) {
        return (Get_Trades(Coin, 50));
    }

    static public void Cancel_Open_Order(CancelOrderRequest Rq)
    {
        Thread thread = new Thread(() -> {
            try {
                if (!Use_KuCoin) {
                    client.cancelOrder(Rq);
                }
                else
                {
                    kucoinRestClient.orderAPI().cancelOrder  (Rq.getOrigClientOrderId() );
                }

            } catch (Exception e)
            {
                My_Toast_Filtered("Cancel Order: ", e );
                MyDebug("Connection", "CancelOrder:" + e.getMessage());
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "Cancel_Orders_Exception : " + e.getMessage());
        }

    }

    static public Account Get_Account() {
        AC = null;

        if (!Is_Keys_Enabled())
            return (AC);

        Thread thread = new Thread(() -> {
            try {
                if (!Use_KuCoin)
                {
                    AC = client.getAccount();
                }
                else
                {
                    /*XXX**/
                    List<AccountBalancesResponse> ABRL = kucoinRestClient.accountAPI().listAccounts  ( "" , "trade" );

                    if ( ABRL != null && ABRL.size ( ) > 0 )
                    {
                        List<AssetBalance> ABal ;
                        AC = new Account();

                        ABal = new ArrayList<AssetBalance>();

                        for ( int idx = 0 ; idx < ABRL.size() ; ++ idx )
                        {
                            AssetBalance Abl  ;

                            Abl = new AssetBalance();
                            Abl.setAsset( ABRL.get(idx).getCurrency());
                            Abl.setFree( ABRL.get(idx).getAvailable().toString());
                            Abl.setLocked( ABRL.get(idx).getHolds().toString() );
                            ABal.add( Abl ) ;
                        }
                        AC.setBalances( ABal );
                    }
                    else {
                        MyDebug ( "Account" , "No account data ") ;
                        AC = null ;
                    }
                }
            } catch (Exception e) {
                My_Toast_Filtered("Account: ", e);
                MyDebug("Connection", "getAccount:" + e.getMessage());
                My_Delay ( 2 ) ;
                AC= null ;
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "GetAccount_Exception : " + e.getMessage());
        }

        return (AC);
    }


    static public List<Trade> Get_Trades(final String Coin) {
        return (Get_Trades(Coin, 100));
    }

    static public List<Trade> Get_Trades(final String Coin, int Limit) {
        LT = null;

        if (!Is_Keys_Enabled())
            return (LT);

        MyDebug( "Trades.." , "Getting trades [" + Coin + "]");

        Thread thread = new Thread(() -> {
            try {
                if (!Use_KuCoin) {
                    LT = client.getMyTrades(Coin, Limit);
                }
                else
                {
                    /*XXX**/
                    Pagination<OrderResponse> PO = kucoinRestClient.orderAPI().listOrders  ( Coin , null, null , "TRADE", "done", null , null , 50 , 1 );

                    if ( PO != null && PO.getItems().size ( ) > 0 )
                    {
                        LT = new ArrayList<>() ;

                        for ( int idx = 0 ; idx < PO.getItems().size() ; ++ idx )
                        {
                            Trade Abl  ;

                            if ( PO.getItems().get(idx ).getDealSize().compareTo(java.math.BigDecimal.ZERO) == 0 )
                                continue ;

                            Abl = new Trade();
                            Abl.setPrice ( PO.getItems().get(idx).getDealFunds().divide(PO.getItems().get(idx).getDealSize(),  BigDecimal.ROUND_DOWN).toString());
                            Abl.setSymbol( PO.getItems().get(idx).getSymbol());
                            Abl.setQty( PO.getItems().get(idx).getDealSize().toString());
                            Abl.setTime( PO.getItems().get(idx).getCreatedAt().getTime());
                            if (PO.getItems().get(idx).getSide().toUpperCase(Locale.ROOT).equals( "BUY" ))
                                Abl.setBuyer( true);
                            else
                                Abl.setBuyer( false );

                            LT.add( Abl ) ;
                        }
                    }
                    else {
                        MyDebug ( "Closed Orders" , "No Closed Orders") ;
                        LT = null ;
                    }
                }

            } catch (Exception e) {
                My_Toast_Filtered("MyTrades: ", e);
                MyDebug("Connection", "getMyTrades:" + e.getMessage());
                LT = null ;
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "GetMyTrades_Exception : " + e.getMessage());
        }

        return (LT);
    }


    private void Close_User_Data_Stream() {

        if (!Is_Keys_Enabled())
            return ;

        Thread thread = new Thread(() -> {
            try {
                if ( listenKey != null)
                {
                    client.closeUserDataStream(listenKey);
                    listenKey = null;
                }
            } catch (Exception e) {
                MyDebug("Connection", "CloseUserDataStream_Exception : " + e.getMessage());
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "CloseUserDataStream_Exception : " + e.getMessage());
        }
    }

    static public OrderBook Get_OrderBook_Data(final String Coin, int max_items) {
        OB = null;


        Thread thread = new Thread(() -> {
            try {

                if ( Use_KuCoin )
                {
                    try
                    {
                        List<OrderBookEntry> OBA = new ArrayList<>();
                        List<OrderBookEntry> OBB = new ArrayList<>();

                        OrderBookResponse OR = kucoinRestClient.orderBookAPI().getTop100Level2OrderBook( Coin ) ;
                        OB = new OrderBook() ;
                        for ( int idx = 0 ; idx < OR.getAsks().size() ; ++ idx ) {

                            OrderBookEntry OBES = new OrderBookEntry();

                            OBES.setPrice( OR.getAsks().get(idx).get(0));
                            OBES.setQty ( OR.getAsks().get(idx).get(1));

                            OBA.add( OBES) ;

                        }
                        for ( int idx = 0 ; idx < OR.getBids().size() ; ++ idx ) {

                            OrderBookEntry OBES = new OrderBookEntry();

                            OBES.setPrice( OR.getBids().get(idx).get(0));
                            OBES.setQty ( OR.getBids().get(idx).get(1));

                            OBB.add( OBES ) ;

                        }

                        OB.setAsks( OBA );
                        OB.setBids( OBB) ;


                    } catch (Exception E)
                    {
                        My_Toast("OrderBook: " + E.getMessage());
                        MyDebug("Connection", "getOrderBook:" + E.getMessage());
                        OB = null ;
                    }
                }
                else {
                    OB = client.getOrderBook(Coin, max_items);
                }
            } catch (Exception e) {
                My_Toast("OrderBook: " + e.getMessage());
                MyDebug("Connection", "getOrderBook:" + e.getMessage());
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "OrderBook_Exception : " + e.getMessage());
        }

        return (OB);
    }

    static int Get_Ticket_Data(final String Coin, boolean only_last)
    {
        int ret ;

        ret = Get_Ticket_Data( Coin , only_last , false ) ;
        if ( candlesticksCache.isEmpty()) {
            MyDebug( "Candle" , "Candle Retry");
            ret = Get_Ticket_Data(Coin, only_last, false);
        }

        return ( ret )  ;
    }

    static int Get_Ticket_Data(final String Coin, boolean only_last , boolean cook )
    {
        Thread thread = new Thread(() -> {

            List<Candlestick> klines;

            if (!only_last)
                candlesticksCache.clear();

            if ( Use_KuCoin )
            {
                try
                {
                    List<List<String>> Candle;

                    Candle = kucoinRestClient.historyAPI().getHistoricRates ( Coin , Get_Kucoin_Max_Candle(), System.currentTimeMillis()/1000, Get_Kucoin_Interval() );

                    for ( int idx = 0 ; idx < Candle.size() ; ++ idx )
                    {
                        Candlestick CLK = new Candlestick();

                        CLK.setOpenTime  ( Long.parseLong( Candle.get(idx).get(0))*1000);
                        CLK.setCloseTime ( CLK.getOpenTime() + ( Get_Kucoin_Interval_InSeconds()*1000) );
                        CLK.setOpen      ( Candle.get(idx).get(1) );
                        CLK.setClose     ( Candle.get(idx).get(2) );
                        CLK.setHigh      ( Candle.get(idx).get(3));
                        CLK.setLow       ( Candle.get(idx).get(4));
                        CLK.setVolume    ( Candle.get(idx).get(5));

                        candlesticksCache.put( Long.parseLong( Candle.get(idx).get(0))*1000 , CLK );
                    }

                    MyDebug( "Candle" , "Candle data Size: " + Candle.size() + " [" + Candle.get(0).get(0) + "]") ;

                } catch (Exception e)
                {
                    MyDebug( "Candle" , "Exception " + e.getMessage() );

                }

            }
            else {
                try {

                    if (only_last)
                        klines = client.getCandlestickBars(Coin.toUpperCase(), Get_Binance_Interval(), 1, null, null);
                    else
                        klines = client.getCandlestickBars(Coin.toUpperCase(), Get_Binance_Interval(), Get_Max_Candle(), null, null);

                    for (Candlestick candlestickBar : klines) {
                        candlesticksCache.put(candlestickBar.getOpenTime(), candlestickBar);
                    }

                } catch (Exception e) {
                    MyDebug("Connection", "CandleStickBars_Exception : " + e.getMessage());
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            MyDebug("Connection", "GetTicket_Data_Exception : " + e.getMessage());
            return (0);
        }

        return (1);
    }

    static public long Get_Kucoin_Max_Candle ( )
    {
        switch ( Get_Kucoin_Interval())
        {
            case "1min" :
                return ((System.currentTimeMillis()/1000)-(60*400)) ;
            case "3min" :
                return ((System.currentTimeMillis()/1000)-(180*400)) ;
            case "5min" :
                return ((System.currentTimeMillis()/1000)-(300*400)) ;
            case "15min" :
                return ((System.currentTimeMillis()/1000)-(60*15*400)) ;
            case "30min" :
                return ((System.currentTimeMillis()/1000)-(60*30*400)) ;
            case "60min" :
                return ((System.currentTimeMillis()/1000)-(60*60*400)) ;
            default :
                return ((System.currentTimeMillis()/1000)-(300*400)) ;
        }

    }

    static public int Get_Max_Candle() {

        if (Current_Binance_Interval.equals(Chart_Types_Array[0]))
            return (400);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[1]))
            return (400);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[2]))
            return (400);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[3]))
            return (400);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[4]))
            return (400);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[5]))
            return (400);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[6]))
            return (200);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[7]))
            return (150);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[8]))
            return (150);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[9]))
            return (100);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[10]))
            return (100);


        return (200);

    }

    static public boolean Is_Keys_Enabled() {

        if ( !Use_KuCoin ) {
            if (Config_Data.Config_Keys.Public_Key == null || Config_Data.Config_Keys.Private_Key == null)
                return (false);

            return (Config_Data.Config_Keys.Public_Key.length() != 0 && Config_Data.Config_Keys.Private_Key.length() != 0);
        }
        else
        {
            if (Config_Data.Config_Keys.Kucoin_Public_Key == null || Config_Data.Config_Keys.Kucoin_Private_Key == null || Config_Data.Config_Keys.Kucoin_passPhrase == null )
                return (false);

            if ( Config_Data.Config_Keys.Kucoin_Public_Key.length() == 0 || Config_Data.Config_Keys.Kucoin_Private_Key.length() == 0 || Config_Data.Config_Keys.Kucoin_passPhrase.length() == 0 )
                return ( false ) ;

            return ( true );
        }
    }


    public static CandlestickInterval Get_Binance_Interval()
    {
        if (Current_Binance_Interval == null) {
            MyDebug("Binance", "Current binance Interval = null, setting default");
            Current_Binance_Interval = Config_Data.Chart_Interval;
        }

        if (Current_Binance_Interval.equals(Chart_Types_Array[0]))
            return (CandlestickInterval.ONE_MINUTE);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[1]))
            return (CandlestickInterval.THREE_MINUTES);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[2]))
            return (CandlestickInterval.FIVE_MINUTES);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[3]))
            return (CandlestickInterval.FIFTEEN_MINUTES);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[4]))
            return (CandlestickInterval.HALF_HOURLY);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[5]))
            return (CandlestickInterval.HOURLY);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[6]))
            return (CandlestickInterval.TWO_HOURLY);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[7]))
            return (CandlestickInterval.FOUR_HOURLY);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[8]))
            return (CandlestickInterval.EIGHT_HOURLY);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[9]))
            return (CandlestickInterval.TWELVE_HOURLY);
        else if (Current_Binance_Interval.equals(Chart_Types_Array[10]))
            return (CandlestickInterval.DAILY);

        return (CandlestickInterval.FIVE_MINUTES);

    }

    static public long Get_Kucoin_Interval_InSeconds ( )
    {
        switch ( Get_Kucoin_Interval())
        {
            case "1min" :
                return (60) ;
            case "3min" :
                return (180) ;
            case "5min" :
                return (300) ;
            case "15min" :
                return (900) ;
            case "30min" :
                return (1800) ;
            case "60min" :
                return (3600) ;
        }

        return ( 300 ) ;
    }


    public static String Get_Kucoin_Interval()
    {
        if (Current_Binance_Interval == null) {
            MyDebug("Binance", "Current binance Interval = null, setting default");
            Current_Binance_Interval = Config_Data.Chart_Interval;
        }

        if (Current_Binance_Interval.equals(Chart_Types_Array[0]))
            return ("1min");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[1]))
            return ("3min");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[2]))
            return ("5min");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[3]))
            return ("15min");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[4]))
            return ("30min");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[5]))
            return ("1hour");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[6]))
            return ("2hour");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[7]))
            return ("4hour");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[8]))
            return ("6hour");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[9]))
            return ("8hour");
        else if (Current_Binance_Interval.equals(Chart_Types_Array[10]))
            return ("1day");

        return ("5min" ) ;
    }

    private BigDecimal Process_BigDecimal ( java.math.BigDecimal Value )
    {
        if ( Value == null)
            return ( new BigDecimal(0));

        return ( new BigDecimal(Value.toString()) ) ;
    }


    public void Get_Markets_Data()
    {

        long offset_time;
        List<TickerStatistics> CList_Data_Backup = null ;
        AllTickersResponse ATR;

        if (Use_KuCoin)
        {
            MyDebug("Markets", "Get Markets_Data");

            CList_Data_Backup = new ArrayList<>();

            try
            {
                List<MarketTickerResponse> LMR;
                ATR = kucoinRestClient.symbolAPI().getAllTickers();

                if (ATR != null) {

                    LMR = ATR.getTicker();

                    for (int idx = 0; idx < LMR.size(); ++idx)
                    {

                        TickerStatistics TS = new TickerStatistics();

                        TS.setSymbol(LMR.get(idx).getSymbol());
                        TS.setVolume(Process_BigDecimal(LMR.get(idx).getVol()).toString());
                        TS.setLastPrice(Process_BigDecimal(LMR.get(idx).getLast()).toString());
                        TS.setAskPrice(Process_BigDecimal(LMR.get(idx).getBuy()).toString());

                        BigDecimal chg_price = Process_BigDecimal(LMR.get(idx).getChangePrice());
                        BigDecimal last = Process_BigDecimal(LMR.get(idx).getLast());
                        BigDecimal Chg_Rate = Process_BigDecimal(LMR.get(idx).getChangeRate());

                        TS.setOpenPrice(last.subtract(chg_price).toString());
                        TS.setPriceChangePercent(Chg_Rate.multiply(BigDecimal.valueOf(100)).toString());

                        CList_Data_Backup.add(TS);
                    }
                }
            } catch (Exception E)
            {
                MyDebug("24Statistics", E.getMessage());

                if (CList_Data != null)
                    CList_Data.clear();
                CList_Data = null;
            }
        }
        else
        {

            try {

                offset_time = client.getServerTime();
                offset_time -= System.currentTimeMillis();
                client.Set_Time_Offset(offset_time);

                CList_Data_Backup = client.getAll24HrPriceStatistics();

            } catch (Exception e)
            {
                MyDebug("Connection", "getAll24HrPriceStatistics:" + e.getMessage());

                if (CList_Data != null)
                    CList_Data.clear();
                CList_Data = null;
            }
        }

        if ( CList_Data_Backup == null )
            return ;

        if (CList_Data == null)
            CList_Data = new ArrayList<>();

        for (int idx = 0; idx < CList_Data_Backup.size(); ++idx)
        {
            if (new BigDecimal(CList_Data_Backup.get(idx).getAskPrice()).compareTo(BigDecimal.ZERO) == 0)
            {
                continue;
            }

            if ( ! Check_Coin_Valid ( CList_Data_Backup.get(idx).getSymbol() ) )
               continue ;

            Update_List_Data_Price(CList_Data_Backup.get(idx).getSymbol(), CList_Data_Backup.get(idx).getLastPrice(), CList_Data_Backup.get(idx).getPriceChangePercent(), CList_Data_Backup.get(idx).getVolume(), Need_Check_Pump() , 0 );
            int idr = Find_List_Data( CList_Data_Backup.get(idx).getSymbol()) ;
            if ( idr >=0 )
            {
                CList_Data.get(idr).setOpenPrice( CList_Data_Backup.get(idx).getOpenPrice());
                CList_Data.get(idr).setVolume ( CList_Data_Backup.get(idx).getVolume());
            }
        }

        if ( Sort_Mode == 0 )
        {
            msort(CList_Data);
//                    CList_Data.msort(Collections.reverseOrder());
        }
        else
            Collections.sort(CList_Data);

        Get_Precision_Coins ( ) ;
    }

    private boolean Check_Coin_Valid ( String Coin )
    {
        if ( Coin.contains( "DOWNUSDT"))
            return ( false ) ;
        if ( Coin.contains( "UPUSDT"))
            return ( false )  ;
        if ( Coin.contains( "3L-"))
            return ( false )  ;
        if ( Coin.contains( "3S-"))
            return ( false )  ;


        return ( true );

    }

    private void Get_Precision_Coins ( )
    {
        if ( Use_KuCoin )
            Get_Precision_KuCoin_Coins ( ) ;
        else
            Get_Precision_Binance_Coins () ;
    }

    private void Get_Precision_Binance_Coins ( )
    {
        int pos_symbol ;

        if ( CList_Data == null )
            return ;

        try
        {
            List<SymbolInfo> LS  ;

            LS = client.getExchangeInfo().getSymbols() ;
            for ( int idx = 0 ; idx < LS.size() ; ++ idx )
            {
                if ( ( pos_symbol = Find_List_Data ( LS.get(idx).getSymbol())) >= 0 )
                {
                    for ( int idx2 = 0 ; idx2 < LS.get(idx).getFilters().size() ; ++ idx2 )
                    {
                        SymbolFilter Tp = LS.get(idx).getFilters().get(idx2);
                        if ( Tp != null )
                        {
                            if ( Tp.getFilterType().toString().equals("PRICE_FILTER"))
                            {
                                CList_Data.get(pos_symbol).Price_Precision = Tp.getTickSize();
                            }
                            else if ( Tp.getFilterType().toString().equals("LOT_SIZE"))
                            {
                                CList_Data.get(pos_symbol).Qty_Precision = Tp.getStepSize();
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception E )
        {
            MyDebug ( "GetSymbols" , "Exception: " + E.getMessage());
        }

    }

    private void Get_Precision_KuCoin_Coins ( )
    {
        int pos_symbol ;

        if ( CList_Data == null )
            return ;

        try
        {
            List<SymbolResponse> LS  ;

            LS = kucoinRestClient.symbolAPI().  getSymbols( ) ;
            for ( int idx = 0 ; idx < LS.size() ; ++ idx )
            {
                if ( ( pos_symbol = Find_List_Data ( LS.get(idx).getSymbol())) >= 0 )
                {
                    CList_Data.get(pos_symbol).Price_Precision = LS.get(idx).getPriceIncrement().toPlainString();
                    CList_Data.get(pos_symbol).Qty_Precision = LS.get(idx).getBaseIncrement().toPlainString();
                }
            }
        }
        catch ( Exception E )
        {
            MyDebug ( "GetSymbols" , "Exception: " + E.getMessage());
        }
    }

    private void Markets_Thread()
    {
        MyDebug("Connection", "Reading Market data... ");

        Thread thread = new Thread(() -> {

            if ( ! Use_KuCoin )
            {
                if (listenKey != null )
                {
                    MyDebug("Connection", "KeepAlive UserDataSTream ");

                    try
                    {
                        FullscreenActivity.client.keepAliveUserDataStream(listenKey);
                    }
                    catch (Exception e)
                    {
                        MyDebug("Connection", "Listenkey is not valid [" + e.getMessage() + "-");
                        Close_User_Data_Websocket();
                        return;
                    }
                }
            }

            Get_Markets_Data();
        });

        thread.start();

        try {

            thread.join();

        } catch (InterruptedException e) {

            My_Toast("Unable to connect with Binance..." + e.getMessage());
        }

    }

    private void User_Streams_Thread()
    {

        MyDebug("Connection", "Starting user data stream...");

        Thread thread = new Thread(this::Enable_User_Data_Streams);

        thread.start();

        try {
            thread.join();

        } catch (InterruptedException e) {
            MyDebug("Connection", "User_Streams_Data_Exception : " + e.getMessage());
        }
    }

    private void Begin_Binance_Work() {

        MyDebug("Application", "Starting Binance_Works");

        Markets_Thread();

        if (CList_Data == null) {
            My_Toast("Unable to connect with Binance...");
            Show_Connection_Status(false );
            return ;
        }

        User_Streams_Thread ( ) ;
        Enable_Websockets   ( ) ;

    }


    public static void Load_Exchange_Keys()
    {
        MyDebug ( "Loading Keys..." , "Loading private Keys.. ") ;

        if ( client != null && listenKey != null )
        {
            client.closeUserDataStream( listenKey );
            client = null ;
        }

        if ( ! Use_KuCoin )
        {
            Load_Binance_Keys () ;
        }
        else
        {
//            kucoin_builder   = new KucoinClientBuilder().withBaseUrl("https://openapi-v2.kucoin.com").withApiKey("60468858de3a710006d74619", "1ca3cfbf-1394-4d11-9ebc-f6314c5c1fe6" , "javi1313");
            kucoin_builder   = new KucoinClientBuilder().withBaseUrl("https://openapi-v2.kucoin.com").withApiKey( Config_Data.Config_Keys.Kucoin_Private_Key , Config_Data.Config_Keys.Kucoin_Public_Key , Config_Data.Config_Keys.Kucoin_passPhrase);
            kucoinRestClient = kucoin_builder.buildRestClient();
        }
    }

    static public void Load_Binance_Keys() {

        factory = BinanceApiClientFactory.newInstance(Config_Data.Config_Keys.Public_Key, Config_Data.Config_Keys.Private_Key);
        client = factory.newRestClient();

        Restart_WebSockets();

    }

    static private void Restart_WebSockets()
    {
        MyDebug("Connection", "Restarting Websockets.");

        client_ws = factory.newWebSocketClient();

        if (client_ws == null)
            MyDebug("Connection", "Websockets: Error creating handle.");

    }

    public static boolean Allow_RealTime_Sockets()
    {
        if (Config_Data.Connection_Type.contains("NEVER"))
            return (false);

        if (Config_Data.Connection_Type.contains("PART"))
            return (checkWifiOnAndConnected());

        return Config_Data.Connection_Type.contains("ALW");

    }

    static public String Qty_Round ( String Coin , String Qty )
    {
        BigDecimal Final_Qty ;

        Final_Qty = new BigDecimal(Qty).subtract(new BigDecimal(Qty).remainder(Get_Qty_Precision(Coin)));
        Final_Qty = new BigDecimal(Round_Qty_Number(Coin, Final_Qty.toString()));

        return ( Final_Qty.toString()) ;
    }

    static public void Sell_Limit(final String Coin, final String Price, final String Qty)
    {
        MyDebug ( "Selling... " , Coin + " [" + Price + "][" + Qty + "]") ;

        BigDecimal Final_Price ;
        BigDecimal Final_Qty   ;

        Final_Qty   = new BigDecimal ( Qty ).subtract ( new BigDecimal( Qty ).remainder( Get_Qty_Precision( Coin )));
        Final_Price = new BigDecimal ( Price ).subtract ( new BigDecimal( Price ).remainder( Get_Price_Precision( Coin )));

        Final_Qty   = new BigDecimal( Round_Qty_Number   ( Coin , Final_Qty.toString())) ;
        Final_Price = new BigDecimal( Round_Price_Number ( Coin , Final_Price.toString())) ;

        MyDebug ( "Create Order" , "Price: " + Final_Price +  " - Qty: " + Final_Qty ) ;

        new Buy_Async_Mode ( 2 ,  Coin , "" , Final_Qty.toString() ,  Final_Price.toString() ).execute();;
    }
    private static String Get_Percent_Stop_Loss_Sell() {
        return (Config_Data.Percent_From_Sell_Stop_Loss);
    }
    public static void Sell_StopLoss(final String Coin, final String Price, final String Qty)
    {
        MyDebug ( "Selling... " , Coin + " [" + Price + "][" + Qty + "]") ;

        BigDecimal Final_Price ;
        BigDecimal Final_Qty   ;
        BigDecimal Stop_Price  ;

        Final_Qty  = new BigDecimal ( Qty ).subtract ( new BigDecimal( Qty ).remainder( Get_Qty_Precision( Coin )));
        Stop_Price = new BigDecimal ( Price ).subtract ( new BigDecimal( Price ).remainder( Get_Price_Precision( Coin )));


        BigDecimal SLM = Stop_Price.multiply(new BigDecimal(Get_Percent_Stop_Loss_Sell())).divide(BigDecimal.valueOf(100));
        Final_Price = SLM.add(Stop_Price);
        Final_Price = Final_Price.subtract ( Final_Price.remainder( Get_Price_Precision( Coin )));

        Final_Qty   = new BigDecimal( Round_Qty_Number   ( Coin , Final_Qty.toString())) ;
        Stop_Price  = new BigDecimal( Round_Price_Number ( Coin , Stop_Price.toString())) ;
        Final_Price = new BigDecimal( Round_Price_Number ( Coin , Final_Price.toString())) ;

        new Buy_Async_Mode ( 1 ,  Coin , Stop_Price.toString() , Final_Qty.toString() ,  Final_Price.toString()).execute();;

    }


    public static void Send_Buy_Order(final String Coin, final String Price, final String Btc_amount )
    {

        MyDebug ( "Buy.." , "Coin: "+  Coin + " - Price: " + Price + " - Amount: " + Btc_amount ) ;

        BigDecimal Final_Price ;
        BigDecimal Final_Qty   ;

        Final_Price = new BigDecimal ( Price ).subtract ( new BigDecimal( Price ).remainder( Get_Price_Precision( Coin )));
        Final_Price = new BigDecimal( Round_Price_Number ( Coin , Final_Price.toString())) ;

        BigDecimal BTC_commision = new BigDecimal(Btc_amount).multiply(BigDecimal.valueOf(0.4)).divide(BigDecimal.valueOf(100)) ;
        BigDecimal Qty = new BigDecimal( Btc_amount ).subtract(BTC_commision);

        Final_Qty   = Qty.subtract ( Qty.remainder( Get_Qty_Precision( Coin )));
        Final_Qty   = new BigDecimal( Round_Qty_Number   ( Coin , Final_Qty.toString())) ;

        MyDebug ( "Buy.." , "Coin: "+  Coin + " - Price: " + Final_Price.toString() + " - Amount: " + Btc_amount + " - Qty: " + Final_Qty.toString() ) ;

        new Buy_Async_Mode ( 0 ,  Coin , "" , Final_Qty.toString() ,  Final_Price.toString()).execute();;

    }

    public static void MyDebug(String Type, String Message) {
        Log.i(Calendar.getInstance().get(Calendar.HOUR) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND) + " - " + Thread.currentThread() + "-" + " - " + Type, Message);
    }

    private void Register_All_Notification_Channels() {
        Register_Notification_Channel();
        Register_Notification_Pump_Channel();
        Register_Alerts_Pump_Channel();
    }

    public void Register_Notification_Channel() {

        NotificationManager notificationManager;

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Get_Channel_ID(),
                    "Buy/Sell order executed.",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }
    }

    public void Register_Notification_Pump_Channel() {

        NotificationManager notificationManager;

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(Get_Channel_Pump_ID(),
                    "Possible Pump Detected.",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }
    }

    public void Register_Alerts_Pump_Channel() {

        NotificationManager notificationManager;

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(Get_Alerts_Channel_ID(),
                    "Price Alerts.",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    void handlePurchase(Purchase purchase) {

        List<String> Skus = new ArrayList<>( );

        Skus = purchase.getSkus() ;
        for ( int idx = 0 ; idx < Skus.size() ; ++ idx )
        {
            if (Compras_SKU.equals( Skus.get(idx)))
            {
                Enable_Subscription_Buys ( true ) ;
                break;
            }
        }

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                My_Toast("Subscription has been acknowledged" );
            }

        };

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases)
    {

        MyDebug( "Subscription" , "Purchases updated!!" ) ;

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null)
        {
            for (Purchase purchase : purchases)
            {
                MyDebug( "Subscription" , "Purchase updated for sku [" + purchase.getPackageName() + "]" ) ;
                handlePurchase(purchase);
            }

        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {

            My_Toast("Subscription Cancelled.");

            MyDebug( "Subscription" , "Subscription Canceled. [" + billingResult.getDebugMessage() + "]" ) ;

            // Handle an error caused by a user cancelling the purchase flow.
        }
        else {
            MyDebug( "Subscription" , "Subscription Failed. [" + billingResult.getResponseCode() + "]" ) ;

            My_Toast("OnPurchases Updated Fail.  [" + billingResult.getResponseCode()  + "]");
        }
    }

    private void Init_Subscriptions()
    {
        Enable_Subscription_Buys ( false ) ;
        SKUDetails_Compras    = null;
    }

    public static boolean  Get_Subscription_Buys ( )
    { //JAVI
       return ( true ) ;  // JUST FOR TEST.
//        return ( BUY_SELL_SUBSCRIPTION ) ;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    private void Paint_Logo_Subscription ( boolean activated )
    {
        if ( navigationView != null )
        {
            final ImageView fimage = navigationView.findViewById(R.id.registered_logo_id);
            if ( fimage != null)
            {
                runOnUiThread(() ->
                {
                    Menu menuNav = navigationView.getMenu();
                    MenuItem itm = menuNav.findItem( R.id.Subscribe);
                    if ( itm != null) {
                        itm.setVisible( !activated ) ;
                    }

                    if ( activated ) {
                        fimage.setImageResource(R.drawable.bitcoin_trans_registered);


                    }
                    else {
                        fimage.setImageResource(R.drawable.bitcoin_trans);
                    }
                });
            }
        }
    }

    private void Enable_Subscription_Buys ( boolean activate )
    {
        Paint_Logo_Subscription ( activate ) ;
        BUY_SELL_SUBSCRIPTION = activate ;
    }

    private void Connect_Billing()
    {

        MyDebug( "Subscription" , "Connecting to Billing service.." ) ;

        Init_Subscriptions();

        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                {

                    MyDebug( "Subscription" , "Connected OK." ) ;

                    Check_Subscriptions ( ) ;

                    // The BillingClient is ready. You can query purchases here.
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED)
                {
                    MyDebug( "Subscription" , "Connected USER_CANCELED." ) ;

                } else {
                    MyDebug( "Subscription" , "Connected error [" + billingResult.getDebugMessage() + "]" ) ;
                }
            }

            @Override
            public void onBillingServiceDisconnected()
            {
                MyDebug( "Subscription" , "Lost Billing connection." ) ;
            }
        });
    }

    private void Check_Subscriptions ( )
    {
        Get_Subscription_Availables();
        Get_Subscription_Enabled();
    }

    private void Get_Subscription_Enabled()
    {
        MyDebug( "Subscription" , "Checking Subscritions enabled....");

        final Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.SUBS);

        final List<Purchase> purchases = result.getPurchasesList();

        BUY_SELL_SUBSCRIPTION = false  ;
        if (purchases != null)
        {
            MyDebug( "Subscription" , "Checking Subscritions enabled [" + purchases.size() + "]");
            for (Purchase purchase : purchases)
            {
                MyDebug( "Subscription" , "Subscription enabled for [" + purchase.getPackageName() + "]");

                handlePurchase(purchase);
            }
        }

        if ( !BUY_SELL_SUBSCRIPTION )
            Enable_Subscription_Buys ( false ) ;

    }

    private void Get_Subscription_Availables()
    {

        MyDebug( "Subscription" , "Checking Subscritions state");

        if ( billingClient == null )
            return ;

        if ( ! billingClient.isReady()  )
            return ;

        SKUDetails_Compras = null ;

        List<String> skuList = new ArrayList<>();

        skuList.add(Compras_SKU);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);

        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {

                        MyDebug( "Subscription" , "Got Subscription List [" + billingResult.getDebugMessage() + "]");

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null)
                        {

                            MyDebug( "Subscription" , "Got Subscription items [" + skuDetailsList.size()  + "]");

                            for (SkuDetails skuDetails : skuDetailsList)
                            {
                                String sku = skuDetails.getSku();

                                MyDebug( "Subscription" , "Checking Subscription item [" + skuDetails.getTitle()  + "]" + "[" + sku + "]" ) ;

                                if (Compras_SKU.equals(sku))
                                {
                                    SKUDetails_Compras = skuDetails;
                                }
                            }
                        } else
                        {
                            MyDebug( "Subscription" , "Error Subscription List [" + billingResult.getDebugMessage() + "]");
                        }

                        // Process the result.
                    }
                });
    }

    public static void Buy_Subscription( Activity Act )
    {
            MyDebug( "Subscription" , "Trying to buy new subscription....");

            if ( ! billingClient.isReady()  || SKUDetails_Compras == null )
            {
                MyDebug( "Subscription" , "Trying follow Flow Params.  BillingClient not ready.");
                return;
            }

            MyDebug( "Subscription" , "Trying follow Flow Params. [" + SKUDetails_Compras.getTitle() + "]");

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(SKUDetails_Compras)
                    .build();

            billingClient.launchBillingFlow( Act , flowParams);
    }

    public static boolean Max_Buy_Reached ( )
    {
        if ( Use_KuCoin )
            return ( true ) ;

        if(  Max_Buy_Times >= 3 )
            return ( true );

        return ( false ) ;
    }

    public static void New_Buy_Selected ( )
    {
        ++ Max_Buy_Times ;
    }


    private static class Buy_Async_Mode extends AsyncTask<Void, Integer, Boolean>
    {
        String Coin ;
        String Final_Qty ;
        String Final_Price ;
        String Stop_Price ;
        int Buy_Mode ;

        private Buy_Async_Mode( int Mode , String CoinName , String SPrice , String Qty , String Price )
        {
            Buy_Mode = Mode ;

            Coin = CoinName ;
            Final_Qty = Qty ;
            Final_Price = Price ;
            Stop_Price = SPrice ;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            if ( Buy_Mode == 0)
            {
                MyDebug("Buying", Coin + "  Price [" + Final_Price + "]  Qty_Items [" + Final_Qty + "]");

                try {

                    if (!Use_KuCoin)
                    {
                        client.newOrder(NewOrder.limitBuy(Coin, TimeInForce.GTC, Final_Qty , Final_Price ));
                    }
                    else {
                        OrderCreateApiRequest request = OrderCreateApiRequest.builder()
                                .price(new java.math.BigDecimal(Final_Price.toString()))
                                .size(new java.math.BigDecimal(Final_Qty.toString()))
                                .side("buy")
                                .symbol(Coin)
                                .type("limit")
                                .tradeType("TRADE")
                                .clientOid(UUID.randomUUID().toString()).build();

                        OrderCreateResponse CR = kucoinRestClient.orderAPI().createOrder(request);
                        MyDebug("Create Order", "Result: " + CR.toString());
                    }

                } catch ( Exception e)
                {
                     My_Toast_Filtered("Buy Orders: ", e);
                     MyDebug("Connection", "Buy Orders: " + e.getMessage());
                }
            }
            else if ( Buy_Mode == 1)
            {
                MyDebug("Selling", Coin + " [" + Final_Price + "][" + Stop_Price + "][" + Final_Qty + "]");

                try {
                    if (!Use_KuCoin)
                    {
                        NewOrder order = new NewOrder(Coin, OrderSide.SELL, OrderType.STOP_LOSS_LIMIT, TimeInForce.GTC, Final_Qty, Final_Price);
                        client.newOrder(order.stopPrice(Stop_Price));

                    } else {

                        OrderCreateApiRequest request = OrderCreateApiRequest.builder()
                                .price(new java.math.BigDecimal(Final_Price))
                                .size(new java.math.BigDecimal(Final_Qty))
                                .stopPrice(new java.math.BigDecimal(Stop_Price))
                                .side("sell")
                                .symbol(Coin)
                                .type("limit")
                                .stop("loss")
                                .tradeType("TRADE")
                                .clientOid(UUID.randomUUID().toString()).build();

                        OrderCreateResponse CR = kucoinRestClient.orderAPI().createOrder(request);

                        MyDebug("Create Order", "Result: " + CR.toString() + " - Price: " + Final_Price + " - Qty: " + Final_Qty);

                    }
                }
                catch ( Exception e )
                {
                    My_Toast_Filtered("Sell Orders: ", e);
                    MyDebug("Connection", "Sell Orders: " + e.getMessage());
                }
            }
            else if ( Buy_Mode == 2)
            {
                MyDebug("Selling", Coin + " [" + Final_Price + "][" + Final_Qty + "]");

                try {
                    if (!Use_KuCoin) {
                        client.newOrder(NewOrder.limitSell(Coin, TimeInForce.GTC, Final_Qty, Final_Price));
                    } else {
                        OrderCreateApiRequest request = OrderCreateApiRequest.builder()
                                .price(new java.math.BigDecimal(Final_Price))
                                .size(new java.math.BigDecimal(Final_Qty))
                                .side("sell")
                                .symbol(Coin)
                                .type("limit")
                                .tradeType("TRADE")
                                .clientOid(UUID.randomUUID().toString()).build();

                        OrderCreateResponse CR = kucoinRestClient.orderAPI().createOrder(request);

                        MyDebug("Create Order", "Result: " + CR.toString() + " - Price: " + Final_Price + " - Qty: " + Final_Qty);

                    }
                }
                catch ( Exception e )
                {
                    My_Toast_Filtered("Sell Orders: ", e);
                    MyDebug("Connection", "Sell Orders: " + e.getMessage());
                }
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

    private class Restart_App_Async_Mode extends AsyncTask<Void, Integer, Boolean>
    {
        private Restart_App_Async_Mode(  )
        {
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            Set_Spinner ( View.VISIBLE  ) ;
        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            Close_User_Data_Websocket   ( );
            Close_User_Data_Stream      ( );
            Close_Ticket_Data_Websocket ( );

            Load_Exchange_Keys ( ) ;
            Begin_Binance_Work ( ) ;

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

            Start_Fragments    ( ) ;
            Start_Timer        ( ) ;

            Set_Spinner ( View.GONE  ) ;
        }
    }

    public void ReStart_Fragments()
    {
        if (Update_Data_Timer != null)
            Update_Data_Timer.cancel();

        new Restart_App_Async_Mode ( ).execute();;
    }

    public void Start_Fragments()
    {

        MyDebug( "Application" , "Enable Fragments ");

        TabsPagerAdapter sectionsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.Panels_ID);

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setEnabled(false);
        viewPager.setCurrentItem(0);

        ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark);

        TabLayout Main_Tabs_Layout = findViewById(R.id.Main_TABS);
        Main_Tabs_Layout.setupWithViewPager(viewPager);

        viewPager.setPageTransformer(false, (page, position) -> {
            // do transformation here
            final float normalizedposition = Math.abs(Math.abs(position) - 1);
            page.setScaleX(normalizedposition / 2 + 0.5f);
            page.setScaleY(normalizedposition / 2 + 0.5f);
        });
    }

    public static class TabsPagerAdapter extends FragmentPagerAdapter {

        private TabsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int index) {

            switch (index) {
                case 0:
                    return (new Main_Favs_Fragment());
                case 1:
                    return (new Main_All_Fragment());
            }

            return (null);
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    public static void My_Delay ( int seconds )
    {
        try
        {
            Thread.sleep(seconds * 1_000L); //1000 milliseconds is one second.

        } catch (Exception e)
        {
            MyDebug( "My_Delay" , "Exception: " + e.getMessage() );
        }
    }

    public void Set_TV_Text ( TextView Vc , String Text )
    {
        runOnUiThread(() -> {
            Vc.setText(Text);
        });
    }

    public static String Get_Tv_Text ( TextView Vc ){

        String Tx  ;

        Tx = Vc.getText ( ).toString() ;

        return (Tx ) ;
    }

    public static String Get_Base_Volume ( String Coin_Volume , String Coin_Price )
    {

        String Base_Vol =  new BigDecimal( Coin_Volume).multiply( new BigDecimal( Coin_Price )).toString() ;

        NumberFormat nf_out = NumberFormat.getNumberInstance(Locale.getDefault());
        nf_out.setMaximumFractionDigits(2);
        Base_Vol = nf_out.format ( Float.parseFloat(Base_Vol) ) ;

        return ( Base_Vol ) ;
        //return ( Round_Number ( Base_Vol , 0 ) ) ;
    }

    public  static void Show_Volume_Data ( Activity Act, View itemView )
    {
        LinearLayout idata = itemView.findViewById(R.id.qty_data);
        idata.setVisibility(View.VISIBLE);

        TextView data = itemView.findViewById(R.id.Fav_ID);
        data.setVisibility(View.GONE);

        TextView item_data = itemView.findViewById(R.id.item_data);
        item_data.setText( "");
        item_data.setTextColor ( ContextCompat.getColor( Act , R.color.MGRAY ) );
        item_data.setTextAlignment( View.TEXT_ALIGNMENT_TEXT_START);


    }

    public static void Paint_Coin_Values( Activity Act , View view, final String price, final String percent , String Volume )
    {

        final TextView text_value = view.findViewById(R.id.value);
        TextView item_data = view.findViewById(R.id.item_data);

        Act.runOnUiThread(() -> {

            Set_Text_With_Flash( Act, text_value, FullscreenActivity.Round_Number(price));

            final TextView text_change = view.findViewById(R.id.change);

            String vl = FullscreenActivity.Round_Number(percent).concat(" %");

            text_change.setText(vl);

            if (Float.parseFloat(percent) <= 0)
                text_change.setTextColor(ContextCompat.getColor(Act, R.color.RD));
            else
                text_change.setTextColor(ContextCompat.getColor(Act, R.color.GR));

            item_data.setText( Get_Base_Volume ( Volume , price ) );
        });

    }

    public static void Shine_Item ( Activity Act,  TextView text , String symbol )
    {
        Act.runOnUiThread(() -> {

            Drawable  mDrawable;

            text.setText(symbol);

            mDrawable = ContextCompat.getDrawable(Act, R.drawable.border_price_green);
            text.setBackground(mDrawable);
            TransitionDrawable transition = (TransitionDrawable) text.getBackground();
            transition.startTransition(500);
            transition.reverseTransition(500);
        });
    }

    public static void Set_Text_With_Flash( Activity Act, TextView txt_v, String data)
    {
        String old_text;

        old_text = txt_v.getText().toString();

        if (old_text.equals(data))
            return;

        Act.runOnUiThread(() -> {

            Drawable mDrawable;

            txt_v.setText(data);
            txt_v.setTextColor(Color.WHITE);

            try {
                BigDecimal old_number = new BigDecimal(old_text);
                BigDecimal new_number = new BigDecimal(data);

                if (new_number.compareTo(old_number) > 0) {
                   mDrawable = ContextCompat.getDrawable(Act, R.drawable.border_price_green);
                } else {

                   mDrawable = ContextCompat.getDrawable(Act, R.drawable.border_price_red);
                }

            } catch (Exception e) {
              mDrawable = ContextCompat.getDrawable(Act, R.drawable.border_price);
            }

            txt_v.setBackground(mDrawable);
            TransitionDrawable transition = (TransitionDrawable) txt_v.getBackground();
            transition.startTransition(240);
            transition.reverseTransition(240);

        });
    }

}



