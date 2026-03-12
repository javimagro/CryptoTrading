package com.crypto_tab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.market.AggTrade;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.kucoin.sdk.model.enums.PublicChannelEnum;
import com.kucoin.sdk.websocket.event.KucoinEvent;
import com.kucoin.sdk.websocket.event.MatchExcutionChangeEvent;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Charts_Mixed_Data_Fragment extends Fragment {

    public Charts_Main_Fragment Prev_Act;


    private View vm;

    private RecyclerView Order_Book_RV;
    private RecyclerView Trades_RV;
    private String Text_coin_RT = null ;

    private long Last_Trade_Time;
    private BigDecimal Candle_Buy_Volume;
    private BigDecimal Candle_Sell_Volume;

    private float OrderBook_Sell_Volume;
    private float OrderBook_Buy_Volume;
    private ProgressBar Global_Percent_PG;

    private final int MAX_ITEMS = 100;

    private static final String BIDS = "BIDS";
    private static final String ASKS = "ASKS";

    private long lastUpdateId;

    private Closeable ws_orderbook_closeable;
    private Closeable ws_trades_closeable;
    private ProgressBar spinner;

    private OrderBook orderBook;

    private Timer Update_Data_Timer;

    private ConcurrentMap<String, NavigableMap<BigDecimal, BigDecimal>> depthCache;

    public static Integer MAX_TRADES = 100;

    public List<AggTrade> CacheTrades;

    public Charts_Mixed_Data_Fragment() {
        MyDebug("Mixed_Fragment", "Constructor");

        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyDebug("Mixed_Fragment", "OnCreate Method");

        Text_coin_RT = Charts_Main_Fragment.Text_coin_RT ;
        Global_Percent_PG = null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MyDebug("Mixed_Fragment", "OnCreate_View");

        vm = inflater.inflate(R.layout.charts_mixed_data_fragment, container, false);


        Trades_RV = vm.findViewById(R.id.orders);
        Order_Book_RV = vm.findViewById(R.id.orderbook);

        Trades_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
        Trades_RV.setAdapter(new EmptyRecycler());

        Order_Book_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
        Order_Book_RV.setAdapter(new EmptyRecycler());

        FragmentVisible();

        return (vm);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        MyDebug("Mixed_Fragment", "OnAttach");


    }

    @Override
    public void onDetach() {
        super.onDetach();

        MyDebug("Mixed_Fragment", "OnDettach");

    }

    private void FragmentVisible()
    {

        Prev_Act = (Charts_Main_Fragment) getActivity();
        if (Prev_Act == null)
            return;

        MyDebug("Mixed_Fragment", "Mixed fragment is visible..");

        Prev_Act.Mixed_Fragment = this;
        FullscreenActivity.Chart_Mixed_F = this;

        Init_Global_Varibs();

        new Load_Data().execute();

        Set_Trades_Timer();

    }

    public void FragmentRefresh() {
        MyDebug("Mixed_Fragment", "Mixed fragment Refresh.");

        Clear_Fragment_Data();

        Prev_Act = (Charts_Main_Fragment) getActivity();
        if (Prev_Act == null)
            return;

        Init_Global_Varibs();

            new Load_Data().execute();
        Set_Trades_Timer();

    }

    private void Set_Trades_Timer() {

        MyDebug("Mixed_Fragment", "Start Timer.");

        Update_Data_Timer = new Timer();
        Update_Data_Timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (spinner != null)
                {
                    if (spinner.getVisibility() == View.GONE)
                    {
                        if (!Allow_RealTime_Sockets()) {
                            Update_Orders_View_Data();
                        }
                        else
                        {
                            if (!Ping_Public_Websocket())
                            {
                                Enable_Trades_Stream();
                                Enable_OrderBook_Stream();
                            }
                        }
                    }
                }

            }
        }, 0, FullscreenActivity.Get_Timer_Refresh_Period());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        MyDebug("Mixed_Fragment", "OnDestroy.");

        FullscreenActivity.Chart_Mixed_F = null ;

        Clear_Fragment_Data();

    }

    private void Close_WS_OrderBook() {

        if (FullscreenActivity.Use_KuCoin)
        {
            try {
                MyDebug ( "Connection" , "Unsubscribe.. OB :  " + Text_coin_RT) ;

                FullscreenActivity.kucoinPublicWSClient.unsubscribe(PublicChannelEnum.LEVEL2, Text_coin_RT);

            } catch (Exception e) {
                MyDebug("Connection", "CLose_Kucoin_OB: " + e.getMessage());
            }
        }

        if (ws_orderbook_closeable != null) {
            try {
                ws_orderbook_closeable.close();

            } catch (Exception e) {
                MyDebug("Connection", "CLose_WS_OB: " + e.getMessage());
            }
        }

        ws_orderbook_closeable = null;

    }

    private void Close_WS_Trades() {

        if (FullscreenActivity.Use_KuCoin)
        {
            try {
                MyDebug ( "Connection" , "Unsubscribe.. MATCH :  " + Text_coin_RT) ;

                FullscreenActivity.kucoinPublicWSClient.unsubscribe(PublicChannelEnum.MATCH, Text_coin_RT);

            } catch (Exception e) {
                MyDebug("Connection", "CLose_Kucoin_OB: " + e.getMessage());
            }
        }

        if (ws_trades_closeable != null) {
            try {
                ws_trades_closeable.close();
                ws_trades_closeable = null;
            } catch (Exception e) {
                MyDebug("Connection", "CLose_WS_Trades: " + e.getMessage());
                ws_trades_closeable = null;
            }
        }
    }

    private void Clear_Fragment_Data() {

        MyDebug("Main_Fragment", "Clear_Fragment_Data");

        if (Update_Data_Timer != null)
            Update_Data_Timer.cancel();

        Update_Data_Timer = null;

        Close_WS_OrderBook();
        Close_WS_Trades();

        Clear_Trades();
        Clear_OrderBook();

        Text_coin_RT = null ;

    }


    private void Init_Global_Varibs() {
        if (getActivity() == null)
            return;

        spinner = vm.findViewById(R.id.mpg_progressBar);

        ws_orderbook_closeable = null;
        ws_trades_closeable = null;

        depthCache  = null;
        CacheTrades = null;

        orderBook = null;

        Update_Data_Timer = null;

    }


    private void Update_Orders_View_Data() {
        MyDebug("Chart_Activity Timer", "Timer count reached....");

        if (CacheTrades != null)
            Get_Trades();


        if (depthCache != null) {
            Get_Order_Book(false);
            Get_Order_Book_Data();
        }

        if (getActivity() == null)
            return;

        getActivity().runOnUiThread(() -> {

            if ( Trades_RV.getAdapter() != null )
                Trades_RV.getAdapter().notifyDataSetChanged();

            if ( Order_Book_RV.getAdapter() != null )
                Order_Book_RV.getAdapter().notifyDataSetChanged();

            if ( Prev_Act.Trades_Fragment.Trades_RV.getAdapter() != null )
                Prev_Act.Trades_Fragment.Trades_RV.getAdapter().notifyDataSetChanged();

        });

    }

    public class Load_Data extends AsyncTask<Void, Integer, Boolean> {

        private Load_Data() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MyDebug("Mixed_Fragment", "Start Painting Screen");

            spinner.setVisibility(View.VISIBLE);

            Paint_Screen();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            if (getActivity() == null)
                return (true);

            if (Get_Order_Book(true)) {
                Get_Order_Book_Data();
                publishProgress(1);
            }

            if (Get_Trades())
                publishProgress(2);

            return (true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values[0] == 1) {
                Update_Order_Book();
                Enable_OrderBook_Stream();
            } else if (values[0] == 2) {
                Update_Trades();
                Enable_Trades_Stream();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Post_Data_Paint();

            spinner.setVisibility(View.GONE);

            if (Prev_Act.Trades_Fragment != null)
                Prev_Act.Trades_Fragment.Update_Trades();

            MyDebug("Mixed_Fragment", "End Load_Data.");

        }
    }

    public void Enable_Trades_Stream() {


        if (CacheTrades == null)
            return;

        Close_WS_Trades();

        if ( ! Allow_RealTime_Sockets()  )
            return ;

        MyDebug( "Connection"  , "Enable Tradessss");

        Candle_Buy_Volume = BigDecimal.ZERO;
        Candle_Sell_Volume = BigDecimal.ZERO;

        if (FullscreenActivity.Use_KuCoin)
        {
            MyDebug( "Connection" , "Registering Match....");
            FullscreenActivity.kucoinPublicWSClient.onMatchExecutionData (response -> {

                if ( getActivity() == null )
                {
                    MyDebug( "FAIL ACTIVITY.", "Coin: " + response.getData().getSymbol()) ;
                    FullscreenActivity.kucoinPublicWSClient.unsubscribe(PublicChannelEnum.MATCH, response.getData().getSymbol());
                    return ;
                }

                if ( ! response.getData().getSymbol().equals( Text_coin_RT )) {
                    MyDebug( "INVALID COIN" , "Coin: " + response.getData().getSymbol() ) ;
                    FullscreenActivity.kucoinPublicWSClient.unsubscribe(PublicChannelEnum.MATCH, response.getData().getSymbol());
                    return;
                }


                if (!Allow_RealTime_Sockets()) {
                    Close_WS_Trades();
                    return;
                }

                Show_Connection_State_TR(1);

                AggTrade AT = new AggTrade();
                AT.setPrice(response.getData().getPrice().toString());
                AT.setQuantity(response.getData().getSize().toString());
                AT.setTradeTime(response.getData().getTime() / 1000000);
                if (response.getData().getSide().equals("buy"))
                    AT.setBuyerMaker(false);
                else
                    AT.setBuyerMaker(true);

                Process_Trade ( AT ) ;

                Paint_Candle_From_Trade ( response ) ;


            }, Text_coin_RT);

        } else {
            ws_trades_closeable = FullscreenActivity.client_ws.onAggTradeEvent(Text_coin_RT.toLowerCase(), new BinanceApiCallback<AggTradeEvent>() {

                @Override
                public void onResponse(final AggTradeEvent response) {

                    if (!Allow_RealTime_Sockets()) {
                        Close_WS_Trades();
                        return;
                    }

                    Show_Connection_State_TR(1);

                    Process_Trade(response);

                }

                @Override
                public void onFailure(final Throwable cause) {
                    My_Toast("[Trades] Failed internet connection....");
                    Close_WS_Trades();
                    Show_Connection_State_TR(0);
                }
            });
        }
    }

    private void Process_Trade ( AggTrade response )
    {
        if ( CacheTrades == null )
            return ;

        Activity Act = getActivity();

        if((response.getTradeTime()/1000)<=(Last_Trade_Time +3))
        {
            BigDecimal TT;

            TT = new BigDecimal(response.getQuantity()).multiply(new BigDecimal(response.getPrice()));
            if (!response.isBuyerMaker())
                Candle_Buy_Volume = Candle_Buy_Volume.add(TT);
            else
                Candle_Sell_Volume = Candle_Sell_Volume.add(TT);
        } else
        {
            Candle_Buy_Volume = BigDecimal.ZERO;
            Candle_Sell_Volume = BigDecimal.ZERO;
            Last_Trade_Time = (response.getTradeTime() / 1000);
        }

        CacheTrades.add(response);

        if(Act !=null)
        {
            Act.runOnUiThread(() ->
            {
                if (CacheTrades == null)
                    return;

                if (CacheTrades.size() > MAX_TRADES)
                    CacheTrades.remove(0);

                if (FullscreenActivity.CList_Data == null)
                    return;

                String percent = Prev_Act.Show_Symbol_Percent(Text_coin_RT);
                Prev_Act.Paint_Coin_Values(Text_coin_RT, response.getPrice(), percent);

                Objects.requireNonNull(Trades_RV.getAdapter()).notifyDataSetChanged();
                Objects.requireNonNull(Prev_Act.Trades_Fragment.Trades_RV.getAdapter()).notifyDataSetChanged();

            });
        }
    }

    public void Enable_OrderBook_Stream()
    {

        Close_WS_OrderBook();

        if ( ! Allow_RealTime_Sockets()  )
            return ;

        Global_Percent_PG     = null ;

        if ( FullscreenActivity.Use_KuCoin )
        {
            if ( FullscreenActivity.kucoinPublicWSClient == null )
                return ;

            if ( Text_coin_RT == null )
                return ;

            FullscreenActivity.kucoinPublicWSClient.onLevel2Data(response -> {

                if ( Text_coin_RT == null )
                    return ;

                if ( getActivity() == null )
                {
                    MyDebug( "FAIL ACTIVITY.", "Coin: " + response.getData().getSymbol()) ;
                    FullscreenActivity.kucoinPublicWSClient.unsubscribe(PublicChannelEnum.LEVEL2, response.getData().getSymbol());
                    return ;
                }

                if ( ! response.getData().getSymbol().equals( Text_coin_RT ))
                {
                    FullscreenActivity.kucoinPublicWSClient.unsubscribe(PublicChannelEnum.LEVEL2, response.getData().getSymbol());
                    return;
                }

                if (!Allow_RealTime_Sockets()) {
                    Close_WS_OrderBook();
                    return;
                }

                if (getActivity() == null)
                    return;

                DepthEvent DE = new DepthEvent();

                DE.setFinalUpdateId( response.getData().getSequenceEnd() );
                DE.setFirstUpdateId( response.getData().getSequenceStart());
                DE.setSymbol( response.getData().getSymbol());

                List<OrderBookEntry> OBA = new ArrayList<>();
                List<OrderBookEntry> OBB = new ArrayList<>();

                for ( int idx = 0 ; idx < response.getData().getChanges().getAsks().size() ; ++ idx )
                {
                    OrderBookEntry OBES = new OrderBookEntry();

                    OBES.setPrice( response.getData().getChanges().getAsks().get(idx).get(0));
                    OBES.setQty  ( response.getData().getChanges().getAsks().get(idx).get(1));

                    OBA.add( OBES) ;

                }
                for ( int idx = 0 ; idx < response.getData().getChanges().getBids().size() ; ++ idx ) {

                    OrderBookEntry OBES = new OrderBookEntry();

                    OBES.setPrice( response.getData().getChanges().getBids().get(idx).get(0));
                    OBES.setQty  ( response.getData().getChanges().getBids().get(idx).get(1));

                    OBB.add( OBES ) ;

                }

                DE.setAsks( OBA );
                DE.setBids( OBB );

                Show_Connection_State_OB(1);

                Update_OB_Data ( DE ) ;

            }, Text_coin_RT);

        }
        else {

            ws_orderbook_closeable = FullscreenActivity.client_ws.onDepthEvent(Text_coin_RT.toLowerCase(), new BinanceApiCallback<DepthEvent>() {

                @Override
                public void onResponse(final DepthEvent response) {

                    if (!Allow_RealTime_Sockets()) {
                        Close_WS_OrderBook();
                        return;
                    }

                    if (getActivity() == null)
                        return;

                    Show_Connection_State_OB(1);

                    Update_OB_Data ( response ) ;

                }

                @Override
                public void onFailure(final Throwable cause) {

                    My_Toast("[OrderBook] Failed internet connection....");
                    Close_WS_OrderBook();
                    Show_Connection_State_OB(0);

                }
            });
        }
    }

    private void Update_OB_Data ( DepthEvent response )
    {
        BigDecimal last_Ask ;
        BigDecimal last_Bid ;

        if (response.getFinalUpdateId() >= lastUpdateId)
        {
            lastUpdateId = response.getFinalUpdateId();

            updateOrderBook(getAsks(), response.getAsks(), 0);
            updateOrderBook(getBids(), response.getBids(), 1);

            try {
                last_Ask = Objects.requireNonNull(getAsks()).firstKey();
                last_Bid = Objects.requireNonNull(getBids()).firstKey();
            }
            catch ( Exception E )
            {
                MyDebug ( "Exception" , "Invalid Ask..." ) ;
                return ;
            }

            if (last_Ask.compareTo(last_Bid) < 0) {
                if (getAsks().containsKey(last_Ask)) {
                    MyDebug("LAST_ASK Found...", "Removing LasBID.");
                    OrderBook_Sell_Volume -= Objects.requireNonNull(getBids()).firstEntry().getValue().floatValue();
                    getBids().remove(last_Bid);

                } else if (getBids().containsKey(last_Bid)) {
                    MyDebug("LAST_BID Found...", "Removing LastAsk.");
                    OrderBook_Buy_Volume -= Objects.requireNonNull(getAsks()).firstEntry().getValue().floatValue();
                    getAsks().remove(last_Ask);
                }
            }
        } else {
            MyDebug("Trades", "Unknow Lastupdate ID : " + lastUpdateId);
//                    My_Toast( "Unknow Lastupdate ID : " + lastUpdateId );
        }

        if (Global_Percent_PG != null) {
//                    Set_Trades_Percent_Value ( Global_Percent_PG ) ;
        }

        getActivity().runOnUiThread(() -> Objects.requireNonNull(Order_Book_RV.getAdapter()).notifyDataSetChanged());
    }

    private void Paint_Screen()
    {
        Paint_Trades    ( ) ;
        Paint_OrderBook ( ) ;

    }


    private boolean Get_Order_Book ( boolean init_trds  )
    {
        NavigableMap<BigDecimal, BigDecimal> asks ;
        NavigableMap<BigDecimal, BigDecimal> bids ;

        if (getActivity()==null)
            return (false );

        if ( init_trds  )
        {
            depthCache = new ConcurrentHashMap<>();

            asks = new TreeMap<>(Comparator.naturalOrder());
            bids  = new TreeMap<>(Comparator.reverseOrder());

            depthCache.put(ASKS, asks);
            depthCache.put(BIDS, bids);

        }
        else
        {
            asks = getAsks();
            if ( asks != null )
                asks.clear( );
            bids = getBids();
            if ( bids != null)
                bids.clear( );
        }


        return ( true ) ;
    }


    private boolean Get_Order_Book_Data (  )
    {
        NavigableMap<BigDecimal, BigDecimal> asks ;
        NavigableMap<BigDecimal, BigDecimal> bids ;

        OrderBook_Buy_Volume  = 0 ;
        OrderBook_Sell_Volume = 0 ;

        if (getActivity()==null)
            return ( false );

        orderBook = FullscreenActivity.Get_OrderBook_Data(Text_coin_RT, MAX_ITEMS);

        if (orderBook == null) {
            Show_Connection_State_OB( 0 );
            return (false);
        }

        if (orderBook.getAsks() == null) {
            Show_Connection_State_OB( 0 );
            return (false);
        }
        Show_Connection_State_OB( 1 );

        lastUpdateId = orderBook.getLastUpdateId();

        asks = getAsks();
        bids = getBids();

        if ( asks == null )
            return ( false );

        if ( bids == null )
            return ( false );


        for (OrderBookEntry ask : orderBook.getAsks()) {
            asks.put(new BigDecimal(ask.getPrice()), new BigDecimal(ask.getQty()));
            OrderBook_Buy_Volume += Float.parseFloat( ask.getQty());
        }

        depthCache.put(ASKS, asks);

        for (OrderBookEntry bid : orderBook.getBids()) {
            bids.put(new BigDecimal(bid.getPrice()), new BigDecimal(bid.getQty()));
            OrderBook_Sell_Volume += Float.parseFloat( bid.getQty());
        }

        depthCache.put(BIDS, bids);


        return ( true ) ;
    }

    private boolean Get_Trades (  )
    {
        List <AggTrade> Trades ;

        if (getActivity()==null)
            return ( false );

        Trades = FullscreenActivity.Get_Trades_Data(Text_coin_RT);

        if ( Trades == null ) {
            Show_Connection_State_TR(0);
            return (false);
        }

        Show_Connection_State_TR( 1 );

        if ( CacheTrades != null )
        {
            CacheTrades.clear();
            CacheTrades.addAll(Trades);
        }
        else
        {
            CacheTrades = Trades;
        }

        return ( true  );
    }

    private void Paint_Trades()
    {
        if ( getActivity () == null )
            return  ;

        if ( Trades_RV == null )
            return ;

        RecyclerView.LayoutManager layoutManager_Trades;

        Trades_RV.setHasFixedSize(false);

        layoutManager_Trades = new LinearLayoutManager(getActivity());
        Trades_RV.setLayoutManager(layoutManager_Trades);

    }

    private void Paint_OrderBook ()
    {
        if ( getActivity () == null )
            return  ;

        if ( Order_Book_RV == null )
            return ;

        Order_Book_RV.setHasFixedSize(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        Order_Book_RV.setItemAnimator(new DefaultItemAnimator());
        Order_Book_RV.setLayoutManager(layoutManager);

    }


    private void Post_Data_Paint ()
    {
        LinearLayoutManager layoutManager ;

        if ( Order_Book_RV == null )
            return ;


        if ( getActivity() == null )
            return ;


        layoutManager = (LinearLayoutManager) Order_Book_RV.getLayoutManager();
        if ( layoutManager == null )
            return ;

        int num_items = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if ( num_items <= 0 )
        {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                num_items = Order_Book_RV.getHeight() / 25 ;

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                num_items = Order_Book_RV.getHeight() / 52 ;
            }
        }


        int position = ( (MAX_ITEMS/4) + 1 - (num_items/2));

        MyDebug ( "Order_Book positions.." , "num_items [ " + num_items + "] - [" + MAX_ITEMS + "]" + " - [" + layoutManager.findLastCompletelyVisibleItemPosition() + "] - [" + layoutManager.findFirstCompletelyVisibleItemPosition() + "] [" + position + "]" ) ;

        ((LinearLayoutManager) Order_Book_RV.getLayoutManager()).scrollToPositionWithOffset(position, 0);


    }

    private void Clear_Trades ()
    {
        if ( CacheTrades == null )
            return ;

        CacheTrades.clear();

        Show_Connection_State_TR ( 0 );

        Update_Trades ();

    }
    private void Clear_OrderBook ()
    {
        if ( depthCache == null )
            return ;

        NavigableMap<BigDecimal, BigDecimal> Trd = getAsks();
        if (Trd != null )
            Trd.clear();

        Trd = getBids();
        if (Trd != null )
            Trd.clear();

        Show_Connection_State_OB ( 0 );

        Update_Order_Book ();
    }


    private void Update_Order_Book()
    {
        final RecyclerView.Adapter mAdapter;

        if ( getActivity () == null )
            return  ;

        if ( depthCache == null )
            return ;

        if ( Order_Book_RV == null )
            return ;

        mAdapter = new Order_Book_Adapter(getAsks() , getBids() , 0 );

        getActivity().runOnUiThread(() ->
        {
            Order_Book_RV.setAdapter(mAdapter);

            mAdapter.notifyDataSetChanged();
            Order_Book_RV.requestLayout();

        });
    }

    private void Update_Trades()
    {
        RecyclerView.Adapter mAdapter_Trades;

        if ( getActivity () == null )
            return  ;

        if ( CacheTrades == null )
            return ;

        if ( Trades_RV == null )
            return ;

        mAdapter_Trades = new Order_Book_Adapter(CacheTrades, 2);

        getActivity().runOnUiThread(() ->
        {
            Trades_RV.setAdapter(mAdapter_Trades);

            mAdapter_Trades.notifyDataSetChanged();
            Trades_RV.requestLayout();
        });
    }

    private void My_Toast ( String Msg )
    {
        FullscreenActivity.My_Toast( Msg);
    }




    private boolean Allow_RealTime_Sockets ( )
    {
        return ( FullscreenActivity.Allow_RealTime_Sockets( ) );
    }

    private boolean checkWifiOnAndConnected() {

        if ( getActivity() == null )
            return ( false ) ;

        return ( FullscreenActivity.checkWifiOnAndConnected ( getContext())) ;

    }


    private float Get_DP_Width()
    {
        if ( getActivity () == null )
            return ( 0 ) ;

        DisplayMetrics displayMetrics = getActivity().getApplicationContext().getResources().getDisplayMetrics();

        return ( displayMetrics.widthPixels / displayMetrics.density ) ;

    }

    public int Find_Limit_Line_By_Value ( String value  )
    {
        CombinedChart Chart_CS;

        if (getActivity()==null)
            return ( -1 );


        Chart_CS = getActivity().findViewById(R.id.Candle_Chart);
        YAxis RAxis = Chart_CS.getAxisRight();

        if ( RAxis == null )
            return ( -1 ) ;

        List<LimitLine> LL ;

        if (( LL = RAxis.getLimitLines() ) == null )
            return ( -1 );

        for ( int idx = 0 ; idx < LL.size() ; ++ idx  )
        {
            if ( LL.get(idx).getLimit() == Float.parseFloat(value ) && LL.get(idx).getTag().equals("ORDER"))
                return ( idx ) ;
        }

        return ( -1 );
    }


    public boolean Find_Alert_Value ( String value  )
    {

        if (getActivity()==null)
            return ( false );


        if ( FullscreenActivity.Find_Alert_Data (  Text_coin_RT , value ))
            return ( true ) ;

        return (false );
    }


    public class Order_Book_Adapter extends RecyclerView.Adapter<Order_Book_Adapter.MyViewHolder> {

        List<AggTrade> mDataset;
        NavigableMap<BigDecimal, BigDecimal> mDataset_sell;
        NavigableMap<BigDecimal, BigDecimal> mDataset_buy;

        int Text_Color;
        int Mode;

        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            private final CardView cardView;

            private MyViewHolder(CardView v) {
                super(v);
                cardView = v;
                cardView.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                cardView.setOnClickListener(new View.OnClickListener()
                {
                    @Override public void onClick(View v) {
                        // item clicked
                        TextView v1;
                        BigDecimal fprice;

                        try {

                            if (Mode == 2)
                                v1 = v.findViewById(R.id.order_trades);
                            else
                                v1 = v.findViewById(R.id.order_items);

                            if (v1 != null) {
                                fprice = new BigDecimal((v1.getText().toString()));
                                Show_Buy_Dialog(fprice.toString());
                            }
                        } catch (Exception E) {
                            MyDebug("Exception", "Got Exception: " + E.getMessage());
                        }
                    }
                });

            }

        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


        private Order_Book_Adapter(List<AggTrade> myDataset, int mode) {
            mDataset = myDataset;
            Mode = mode;
        }

        private Order_Book_Adapter(NavigableMap<BigDecimal, BigDecimal> myDataset_sell, NavigableMap<BigDecimal, BigDecimal> myDataset_buy, int mode) {
            mDataset_sell = myDataset_sell;
            mDataset_buy = myDataset_buy;
            Mode = mode;
        }

        public void setColor(int rColor) {
            Text_Color = rColor;
        }

        @NonNull
        @Override
        public Order_Book_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.items_order_book, parent, false);

            return (new Order_Book_Adapter.MyViewHolder(v));

        }


        private void Test_Buy_Order_Price(String price, CardView v) {
            if (getActivity() == null)
                return;

            if (Find_Limit_Line_By_Value(price) >= 0) {
                v.setBackgroundColor(Color.parseColor("#494949"));
            } else {
                v.setBackgroundColor( Color.TRANSPARENT);
            }
        }

        @SuppressLint("DefaultLocale")
        public void onBindViewHolder(Order_Book_Adapter.MyViewHolder holder, int position) {

            TextView v = holder.cardView.findViewById(R.id.order_items);
            TextView v2 = holder.cardView.findViewById(R.id.order_trades);
            ProgressBar pgb = holder.cardView.findViewById(R.id.trades_percent_id);
            ProgressBar pgb2 = holder.cardView.findViewById(R.id.volumes_percent_id);

            String Price_Value;

            if (getActivity() == null)
                return;

            try {

                if (Mode == 2) {

                    pgb.setVisibility(View.GONE);
                    pgb2.setVisibility(View.GONE);

                    if (position >= mDataset.size())
                        return;

                    position = mDataset.size() - position - 1;

                    Calendar Cl = Calendar.getInstance();
                    Cl.setTimeInMillis(mDataset.get(position).getTradeTime());

                    v.setText(String.format("%02d:%02d:%02d", Cl.get(Calendar.HOUR_OF_DAY), Cl.get(Calendar.MINUTE), Cl.get(Calendar.SECOND)));


                    Price_Value = mDataset.get(position).getPrice();

                    if (mDataset.get(position).isBuyerMaker()) {
                        v.setTextColor(Color.WHITE);
                        if ( Find_Alert_Value ( Price_Value ) ) {
                            v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.LKRD  ));
                        }
                        else {
                            v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.RD));
                        }
                    } else {
                        v.setTextColor(Color.WHITE);

                        if ( Find_Alert_Value ( Price_Value ) ) {
                            v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.LKGR));
                        }
                        else {
                            v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.GR));
                        }
                    }

                    v2.setText(FullscreenActivity.Round_Price_Number(  Text_coin_RT , Price_Value));


                    Test_Buy_Order_Price(Price_Value, holder.cardView);

                } else {

                    if (position < (MAX_ITEMS / 4)) {

                        pgb.setVisibility(View.GONE);
                        pgb2.setVisibility(View.GONE);

                        v.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.RD));
                        v2.setTextColor(Color.WHITE);

                        int new_pos = ((MAX_ITEMS / 4) - 1) - position;

                        if (new_pos < mDataset_sell.size() && new_pos >= 0) {
                            Object[] ar;

                            try {
                                ar = mDataset_sell.keySet().toArray();
                            } catch (Exception e) {
                                return;
                            }


                            if (ar != null) {
                                BigDecimal key = (BigDecimal) ar[new_pos];
                                BigDecimal value = mDataset_sell.get(key);

                                if (value != null) {

                                    v.setText(FullscreenActivity.Round_Price_Number( Text_coin_RT , key.toString()));
                                    v2.setText(FullscreenActivity.Qty_Round ( Text_coin_RT , value.toString()));

                                    Price_Value = key.toString();

                                    Test_Buy_Order_Price(Price_Value, holder.cardView);
                                    if ( Find_Alert_Value ( Price_Value ) )
                                        v.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.LKRD));
                                }
                            }
                        } else {
                            v.setText(" ");
                            v2.setText(" ");
                        }
                    } else if (position == (MAX_ITEMS / 4)) {
                        v.setText(" ");
                        v2.setText(" ");

                        Global_Percent_PG = pgb2;

                        pgb.setVisibility(View.VISIBLE);
                        Global_Percent_PG.setVisibility(View.VISIBLE);

                        Set_Trades_Percent(pgb);
                        Set_Trades_Percent_Value(Global_Percent_PG);


                    } else {

                        pgb.setVisibility(View.GONE);
                        pgb2.setVisibility(View.GONE);

                        v.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.GR));
                        v2.setTextColor(Color.WHITE);

                        int new_pos = position - ((MAX_ITEMS / 4) + 1);


                        if (new_pos < mDataset_buy.size()) {
                            Object[] ar;

                            try {
                                ar = mDataset_buy.keySet().toArray();
                            } catch (Exception e) {
                                return;
                            }


                                BigDecimal key = (BigDecimal) ar[new_pos];
                                BigDecimal value = mDataset_buy.get(key);

                                if (value != null) {
                                    v.setText(FullscreenActivity.Round_Price_Number( Text_coin_RT , key.toString()));
                                    v2.setText(FullscreenActivity.Qty_Round ( Text_coin_RT , value.toString()));

                                    Price_Value = key.toString();

                                    Test_Buy_Order_Price(Price_Value, holder.cardView);
                                    if ( Find_Alert_Value ( Price_Value ) )
                                        v.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.DKGR));
                                }

                        } else {
                            v.setText(" ");
                            v2.setText(" ");
                        }

                    }
                }
/*
                if ( position != (MAX_ITEMS / 4) || Mode == 2 ) {
                    holder.cardView.setOnClickListener(view -> {

                        MyDebug ( "JAVI","XXXXXXXXXXXXXXXXXXX") ;
                        TextView v1;
                        BigDecimal fprice;


                        if (Mode == 2) {
                            v1 = view.findViewById(R.id.order_trades);
                        } else {
                            v1 = view.findViewById(R.id.order_items);
                        }

                        fprice = new BigDecimal((v1.getText().toString()));
                        Show_Buy_Dialog(fprice.toString());
                    });
                                    }
 */

            } catch (Exception ex)
            {
                MyDebug("Exception", ex.getMessage());
            }
        }

        private void Set_Trades_Percent( ProgressBar pbg )
        {

            float asks =  Count_Order_Books ( getAsks() ) ;
            float bids =  Count_Order_Books ( getBids() ) ;

            float total = asks + bids ;

            if ( total == 0)
                return ;


            total = ( asks * 100 ) / total ;

            pbg.setProgress( (int)total );


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount()
        {
            if ( Mode == 0 )
                return ( (MAX_ITEMS/2) + 1 ) ;
            else
                return (mDataset.size());
        }
    }

    private Float  Count_Order_Books (NavigableMap<BigDecimal, BigDecimal> OrderBookEntries)
    {
        BigDecimal total ;

        if ( OrderBookEntries == null )
        {
            Log.i( "Main_Fragment" , "Found 0 orderbooks." ) ;
            return ((float)0);
        }

        total = BigDecimal.ZERO ;

        try {
            NavigableSet<BigDecimal> ns = OrderBookEntries.navigableKeySet();
            for (BigDecimal n : ns)
            {
                total = total.add( OrderBookEntries.get(n) ) ;
            }
        } catch (Exception e)
        {
            Log.i ( "Main_Fragment" , "Concurrent exception in count data.");
        }
        /*
        for( BigDecimal value: OrderBookEntries.values())
        {
            total = total.add( value ) ;
        }
*/

        return ( Float.parseFloat( total.toString() )) ;
    }

    private void updateOrderBook(NavigableMap<BigDecimal, BigDecimal> lastOrderBookEntries, List<OrderBookEntry> orderBookDeltas , int mode ) {

        for (OrderBookEntry orderBookDelta : orderBookDeltas)
        {
            BigDecimal price = new BigDecimal(orderBookDelta.getPrice());
            BigDecimal qty   = new BigDecimal(orderBookDelta.getQty());

            if (qty.compareTo(BigDecimal.ZERO) == 0)
            {
                BigDecimal my_qty = lastOrderBookEntries.remove(price);

                if ( my_qty != null ) {
                    if (mode == 0) {
                        OrderBook_Buy_Volume -= my_qty.longValue();
                    } else {
                        OrderBook_Sell_Volume -= my_qty.longValue();
                    }
                }

            } else
            {
                lastOrderBookEntries.put(price, qty);
                if ( mode == 0) {
                    OrderBook_Buy_Volume += qty.floatValue();
                }
                else {
                    OrderBook_Sell_Volume += qty.floatValue();
                }
            }

//            Objects.requireNonNull(getActivity()).runOnUiThread(() -> Objects.requireNonNull(Order_Book_RV.getAdapter()).notifyDataSetChanged());
        }

        if ( mode == 0 )
        {
            while (lastOrderBookEntries.size() > ( MAX_ITEMS/2))
            {
                OrderBook_Buy_Volume -= lastOrderBookEntries.lastEntry().getValue().floatValue();
                lastOrderBookEntries.remove(lastOrderBookEntries.lastEntry().getKey());
            }


        }
        else if ( mode == 1 )
        {
            while (lastOrderBookEntries.size() > (MAX_ITEMS/2)) {
                OrderBook_Sell_Volume -= lastOrderBookEntries.lastEntry().getValue().floatValue();
                lastOrderBookEntries.remove(lastOrderBookEntries.lastEntry().getKey());
            }
        }

    }

    private NavigableMap<BigDecimal, BigDecimal> getAsks()
    {
        if ( depthCache == null )
            return ( null) ;

        return depthCache.get(ASKS);
    }

    private NavigableMap<BigDecimal, BigDecimal> getBids()
    {

        if ( depthCache == null )
            return ( null) ;

        return depthCache.get(BIDS);
    }

    public void Show_Buy_Dialog ( String Price )
    {
        final AlertDialog dialogBuilder ;

        if ( Float.parseFloat( Price ) == 0 )
            return ;

        if ( FullscreenActivity.Max_Buy_Reached ( ) )
        {
            if (!FullscreenActivity.Get_Subscription_Buys())
            {
                My_Toast( "Maximum buy/sell operations has been reached in this free version.\n\nYou need subscribe this functionality in Google Play Store.");
                FullscreenActivity.Buy_Subscription( requireActivity() );
                return;
            }
        }

        FullscreenActivity.New_Buy_Selected ( ) ;

        dialogBuilder = new AlertDialog.Builder(getActivity() ).create();

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.buy_dialog, null , false );

        final EditText fprice = dialogView.findViewById(R.id.buy_price);
        fprice.setText( FullscreenActivity.Round_Price_Number( Text_coin_RT  , Price));

        final TextView cprice = dialogView.findViewById(R.id.current_price);
        cprice.setText( FullscreenActivity.Round_Price_Number( Text_coin_RT  , Prev_Act.Get_Current_Price( ) ));

        final TextView citems = dialogView.findViewById(R.id.sell_items);
        citems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Prev_Act.Get_Current_Items( )) );

        fprice.setSelection( fprice.getText().length() );

        fprice.setOnClickListener(view -> fprice.setSelection( fprice.getText().length() ));

        cprice.setOnClickListener(view -> {
            fprice.setText ( FullscreenActivity.Round_Price_Number( Text_coin_RT , Prev_Act.Get_Current_Price( ) ));
            cprice.setText ( FullscreenActivity.Round_Price_Number( Text_coin_RT , Prev_Act.Get_Current_Price( ) ));
        });

        String Balance = Prev_Act.Get_Label_Balance () ;
        BigDecimal BTC_commision = new BigDecimal( Balance ).multiply( BigDecimal.valueOf(0.2)).divide( BigDecimal.valueOf(100)) ;
        BigDecimal Qty = new BigDecimal( Balance ).subtract(BTC_commision);

        BigDecimal Qty_Items = Qty.divide( new BigDecimal( ( fprice.getText().toString()) ) , BigDecimal.ROUND_DOWN) ;

        String Final_Qty = Qty_Items.setScale( FullscreenActivity.Get_Qty_Precision ( Qty_Items.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();


        final TextView bitems = dialogView.findViewById(R.id.buy_items);
        bitems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty) );

        Button tid4 =  dialogView.findViewById(R.id.total_id4);
        tid4.setOnClickListener(view -> {
            BigDecimal Qty1 = new BigDecimal( Prev_Act.Get_Current_Items( ) );
            String Final_Qty1 = Qty1.setScale( FullscreenActivity.Get_Qty_Precision ( Qty1.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();
            citems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty1) );
        });
        Button tid5 = dialogView.findViewById(R.id.total_id5);
        tid5.setOnClickListener(view -> {
            BigDecimal Qty12 = new BigDecimal( Prev_Act.Get_Current_Items( ) );
            BigDecimal Qty_Items1 = Qty12.divide( new BigDecimal( 2 ) , BigDecimal.ROUND_DOWN) ;
            String Final_Qty12 = Qty_Items1.setScale( FullscreenActivity.Get_Qty_Precision ( Qty_Items1.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();
            citems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty12) );
        });
        Button tid6 = dialogView.findViewById(R.id.total_id6);
        tid6.setOnClickListener(view -> {
            BigDecimal Qty14 = new BigDecimal( Prev_Act.Get_Current_Items( ) );
            BigDecimal Qty_Items13 = Qty14.divide( new BigDecimal( 4 ) , BigDecimal.ROUND_DOWN) ;
            String Final_Qty14 = Qty_Items13.setScale( FullscreenActivity.Get_Qty_Precision ( Qty_Items13.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();
            citems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty14) );
        });

        Button btid4 =dialogView.findViewById(R.id.total_id);
        btid4.setOnClickListener(view -> {
            String Balance1 = Prev_Act.Get_Label_Balance () ;
            BigDecimal BTC_commision1 = new BigDecimal(Balance1).multiply( BigDecimal.valueOf(0.2)).divide( BigDecimal.valueOf(100)) ;
            BigDecimal Qty13 = new BigDecimal(Balance1).subtract(BTC_commision1);

            BigDecimal Qty_Items12 = Qty13.divide( new BigDecimal( ( fprice.getText().toString()) ) , BigDecimal.ROUND_DOWN ) ;

            String Final_Qty13 = Qty_Items12.setScale( FullscreenActivity.Get_Qty_Precision ( Qty_Items12.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();

            bitems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty13) );
        });
        Button btid5 = dialogView.findViewById(R.id.total_id2);
        btid5.setOnClickListener(view -> {
            String Balance12 = Prev_Act.Get_Label_Balance () ;
            BigDecimal BTC_commision12 = new BigDecimal(Balance12).multiply( BigDecimal.valueOf(0.2)).divide( BigDecimal.valueOf(100)) ;
            BigDecimal Qty15 = new BigDecimal(Balance12).subtract(BTC_commision12);

            BigDecimal Qty_Items14 = Qty15.divide( new BigDecimal( ( fprice.getText().toString())) , BigDecimal.ROUND_DOWN ).divide( new BigDecimal( 2 ), BigDecimal.ROUND_DOWN) ;

            String Final_Qty15 = Qty_Items14.setScale( FullscreenActivity.Get_Qty_Precision ( Qty_Items14.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();

            bitems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty15) );
        });
        Button btid6 = dialogView.findViewById(R.id.total_id3);
        btid6.setOnClickListener(view -> {
            String Balance13 = Prev_Act.Get_Label_Balance () ;
            BigDecimal BTC_commision13 = new BigDecimal(Balance13).multiply( BigDecimal.valueOf(0.2)).divide( BigDecimal.valueOf(100)) ;
            BigDecimal Qty16 = new BigDecimal(Balance13).subtract(BTC_commision13);

            BigDecimal Qty_Items15 = Qty16.divide( new BigDecimal( ( fprice.getText().toString()) ), BigDecimal.ROUND_DOWN).divide( new BigDecimal( 4 ) , BigDecimal.ROUND_DOWN) ;

            String Final_Qty16 = Qty_Items15.setScale( FullscreenActivity.Get_Qty_Precision ( Qty_Items15.toString() ).scale(), BigDecimal.ROUND_DOWN ).toString();

            bitems.setText( FullscreenActivity.Qty_Round ( Text_coin_RT  , Final_Qty16) );
        });

        Button button1 =  dialogView.findViewById(R.id.sell_id);
        Button button2 =  dialogView.findViewById(R.id.buy_id);
        Button button3 =  dialogView.findViewById(R.id.limit_sell_id);
        Button alert   =  dialogView.findViewById(R.id.alert_id);
        Button alert_rep =  dialogView.findViewById(R.id.alert_repeat_id);

        alert.setOnClickListener(view -> {
            dialogBuilder.dismiss();
            FullscreenActivity.Put_Alert ( Text_coin_RT, (fprice.getText().toString()), (cprice.getText().toString()) ,false ) ;


            if (  Prev_Act != null )
                Prev_Act.Refresh_All_Information ( ) ;

        });

        alert_rep.setOnClickListener(view -> {
            dialogBuilder.dismiss();
            FullscreenActivity.Put_Alert ( Text_coin_RT, (fprice.getText().toString()), (cprice.getText().toString()) , true ) ;


            if (  Prev_Act != null )
                Prev_Act.Refresh_All_Information ( ) ;

        });

        button2.setOnClickListener(view -> {
            dialogBuilder.dismiss();
            FullscreenActivity.Send_Buy_Order (Text_coin_RT, (fprice.getText().toString()) , (bitems.getText ( ).toString())) ;

            if (  Prev_Act != null )
                Prev_Act.Refresh_All_Information ( ) ;

        });

        button1.setOnClickListener(view -> {

            dialogBuilder.dismiss();
            FullscreenActivity.Sell_Limit (Text_coin_RT, (fprice.getText().toString()) , (citems.getText ( ).toString() )) ;

            if (  Prev_Act != null )
                Prev_Act.Refresh_All_Information ( ) ;

        });

        button3.setOnClickListener(view -> {

            dialogBuilder.dismiss();
            FullscreenActivity.Sell_StopLoss (Text_coin_RT, (fprice.getText().toString()) , (citems.getText ( ).toString() )) ;

            if (  Prev_Act != null )
                Prev_Act.Refresh_All_Information ( ) ;

        });

        Objects.requireNonNull(dialogBuilder.getWindow()).setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    private int Get_Chart_Selected ( CharSequence [] Types )
    {
        for ( int idx =0 ; idx < Types.length ; ++ idx )
        {
            if ( Types[idx].toString().equals( FullscreenActivity.Current_Binance_Interval ) )
            {
                return ( idx ) ;
            }
        }

        return ( -1 );
    }

    void Change_Chart_Period()
    {

        CharSequence[] grpname = getResources().getStringArray(R.array.Chart_Types) ;
        int selected_item ;

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getContext());

        alt_bld.setTitle("Select Chart time period");

        selected_item = Get_Chart_Selected ( grpname ) ;

        alt_bld.setSingleChoiceItems(grpname, selected_item , (dialog, item) -> {
            FullscreenActivity.Current_Binance_Interval = grpname [ item ].toString() ;
            Prev_Act.Paint_All_Data();
            dialog.dismiss();
        });

        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    private void MyDebug ( String Type , String Message )
    {
        FullscreenActivity.MyDebug( Type , Message ) ;
    }

    private void Set_Trades_Percent_Value ( ProgressBar pbg )
    {
        if ( pbg == null )
            return ;

        if ( Candle_Buy_Volume == null || Candle_Sell_Volume == null  ) {
            return;
        }

        float asks = Candle_Buy_Volume.floatValue() ;
        float bids = Candle_Sell_Volume.floatValue() ;

        float total = asks + bids ;

        if ( total == 0)
            return ;

        pbg.setVisibility(View.VISIBLE);

        total = ( bids * 100 ) / total ;

        pbg.setProgress( (int)total );


    }

    private void Show_Connection_State_OB ( int mode )
    {
        if ( getActivity() == null || Order_Book_RV == null )
            return ;

        if ( mode == 1 )
            Set_Scroll_Color( Order_Book_RV, R.drawable.scrollbar);
        else
            Set_Scroll_Color ( Order_Book_RV , R.color.RD ) ;

    }

    private void Show_Connection_State_TR ( int mode )
    {
        if ( getActivity() == null || Trades_RV == null )
            return ;

        if ( mode == 1 )
            Set_Scroll_Color( Trades_RV, R.drawable.scrollbar );
        else
            Set_Scroll_Color ( Trades_RV , R.color.RD ) ;

    }


    public void Set_Scroll_Color ( RecyclerView scr  , int colordrawable  ) {
/*
        try {
            Field mScrollCacheField = View.class.getDeclaredField("mScrollCache");
            mScrollCacheField.setAccessible(true);
            Object mScrollCache = mScrollCacheField.get(scr); // scr is your Scroll View

            Field scrollBarField = mScrollCache.getClass().getDeclaredField("scrollBar");
            scrollBarField.setAccessible(true);
            Object scrollBar = scrollBarField.get(mScrollCache);

            Method method = scrollBar.getClass().getDeclaredMethod("setVerticalThumbDrawable", Drawable.class);
            method.setAccessible(true);

            // Set your drawable here.
            method.invoke(scrollBar, getResources().getDrawable(colordrawable));
        } catch (Exception e) {
            e.printStackTrace();
        }

        */
    }

    public boolean Ping_Public_Websocket ( )
    {
        if (! FullscreenActivity.Use_KuCoin)
        {
            return ws_trades_closeable != null && ws_orderbook_closeable != null;
        }

        return ( true ) ;
    }

    private void Paint_Candle_From_Trade (KucoinEvent<MatchExcutionChangeEvent> data )
    {
        java.util.Map.Entry<Long, Candlestick> EC  = FullscreenActivity.candlesticksCache.lastEntry();
        java.util.Map.Entry<Long, Candlestick> FC  = FullscreenActivity.candlesticksCache.firstEntry();

        if (FC==null )
            return ;
        if (EC == null)
            return;

        Candlestick updateCandlestick = EC.getValue();

        if (updateCandlestick == null)
            return;

        long period = FC.getValue().getCloseTime() - FC.getValue().getOpenTime() ;

        CandlestickEvent response = new CandlestickEvent();

        response.setSymbol(data.getData().getSymbol());

        String price = data.getData().getPrice().toString() ;

        if ( System.currentTimeMillis() >= (updateCandlestick.getOpenTime() + period))
        {
            response.setOpenTime(updateCandlestick.getOpenTime() + period);
            response.setHigh(price);
            response.setLow(price);
            response.setOpen(price);
            response.setVolume(data.getData().getSize().toString());
        }
        else
        {
            response.setOpenTime(updateCandlestick.getOpenTime());
            response.setOpen(updateCandlestick.getOpen());
            if (new BigDecimal(price).compareTo(new BigDecimal(updateCandlestick.getHigh())) > 0)
            {
                response.setHigh(price);
            } else
            {
                response.setHigh(updateCandlestick.getHigh());
            }
            if (new BigDecimal(price).compareTo(new BigDecimal(updateCandlestick.getLow())) < 0) {
                response.setLow(price);
            } else {
                response.setLow(updateCandlestick.getLow());
            }

            BigDecimal volume = new BigDecimal( updateCandlestick.getVolume() ).add( new BigDecimal(data.getData().getSize()) );
            response.setVolume( volume.toString());
        }

        response.setClose(price);

        try
        {
            Prev_Act.Paint_New_Candle_Line(response, response.getOpenTime());
        } catch (Exception E) {
            FullscreenActivity.MyDebug("Error", "Candle.... " + E.getMessage());
        }

    }

}

