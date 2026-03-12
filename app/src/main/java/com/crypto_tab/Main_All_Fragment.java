package com.crypto_tab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;


public class Main_All_Fragment extends Fragment {

    private View vm;

    private GridLayout mGrid;
    private FloatingActionButton fab;

    private int Get_Max_Top_Items ( )
    {
        return ( FullscreenActivity.Config_Data.Topten) ;
    }


    public Main_All_Fragment()
    {
        MyDebug( "Main_All_Fragment" , "Constructor");

        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MyDebug( "Main_All_Fragment" , "OnCreate Method");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        MyDebug( "Main_All_Fragment" , "OnCreate_View");

        vm = inflater.inflate(R.layout.all_panel , container, false);

        FragmentVisible () ;

        return (vm);
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        MyDebug( "Main_All_Fragment" , "OnAttach");


    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        MyDebug( "Main_All_Fragment" , "OnDettach");

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        FullscreenActivity.Alls_Fragment = null ;

        MyDebug( "Main_All_Fragment" , "OnDestroy.");

    }


    private void FragmentVisible()
    {
        MyDebug( "Main_All_Fragment" , "Mixed fragment is visible..");

        FullscreenActivity.Alls_Fragment = this;

        mGrid = vm.findViewById(R.id.grid_layout);


        NestedScrollView mScrollView = vm.findViewById(R.id.nested_scroll_view);
        mScrollView.setSmoothScrollingEnabled(true);

        fab = vm.findViewById(R.id.fab);
        fab.setTag("");
        fab.setOnClickListener(view -> {

            if ( FullscreenActivity.Sort_Mode == 0 )
            {
                fab.setImageResource( R.drawable.flecha ) ;
                FullscreenActivity.Sort_Mode = 1 ;
                FullscreenActivity.CList_Data.sort(Collections.reverseOrder());
            }
            else
            {
                fab.setImageResource(R.drawable.flechabajo ) ;
                FullscreenActivity.Sort_Mode = 0 ;
                Collections.sort( FullscreenActivity.CList_Data );

            }
        });


        mScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                fab.hide();
            } else {
                fab.show();
            }
        });


        Show_All_Coins ();

    }

    public void Show_All_Coins  ()
    {
        Activity Act ;

        Act = getActivity() ;

        if ( Act == null )
            return ;

        Act.runOnUiThread(() -> {

            Show_All_Coins ( false ) ;
        });
    }
    public void Show_All_Coins( boolean force )
    {
        int idx_coin;

        if ( FullscreenActivity.CList_Data == null )
            return ;

        MyDebug("Main_All_Fragment", "Show AllCoins.");

        mGrid.removeAllViews();
        mGrid.setColumnCount(Calculate_Columns());

        final LayoutInflater inflater = LayoutInflater.from(requireActivity());

        int found_item = 0 ;

        BigDecimal vl2 = new BigDecimal(  FullscreenActivity.Config_Data. Min_Volume_Pump )  ;

        for (idx_coin = 0; idx_coin < FullscreenActivity.CList_Data.size(); idx_coin++)
        {

            if ( ! FullscreenActivity.Config_Data.Filter_Coin.equals( "NONE"))
                if (!FullscreenActivity.CList_Data.get(idx_coin).getSymbol().endsWith(FullscreenActivity.Config_Data.Filter_Coin))
                    continue ;

            BigDecimal vl1 = new BigDecimal( FullscreenActivity.CList_Data.get(idx_coin).getVolume()) ;


            if ( vl1.compareTo ( vl2 )< 0 )
                continue ;

            ++ found_item ;

            if ( found_item > Get_Max_Top_Items() )
                break ;

            final View itemView = inflater.inflate(R.layout.items_coin_data_main, mGrid, false);

            LinearLayout idata = itemView.findViewById(R.id.qty_data);
            idata.setVisibility(View.GONE);
            TextView data = itemView.findViewById(R.id.Fav_ID);
            data.setVisibility(View.GONE);

            FullscreenActivity.Show_Volume_Data ( requireActivity() , itemView );

            final TextView text = itemView.findViewById(R.id.text);
            text.setText(FullscreenActivity.CList_Data.get(idx_coin).getSymbol());

            final TextView text_value = itemView.findViewById(R.id.value);
            final TextView text_change = itemView.findViewById(R.id.change);

            FullscreenActivity.Paint_Coin_Values( requireActivity() , itemView, FullscreenActivity.CList_Data.get(idx_coin).getLastPrice(), FullscreenActivity.CList_Data.get(idx_coin).getPriceChangePercent() , FullscreenActivity.CList_Data.get(idx_coin).getVolume());


            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                itemView.setLayoutParams(new CardView.LayoutParams(Get_Pixels(Get_Width()), Get_Pixels(80)));

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                itemView.setLayoutParams(new CardView.LayoutParams(Get_Pixels(Get_Width()), Get_Pixels(65)));
            }

            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();

            layoutParams.setMargins(8, 8, 8, 8);

            itemView.setOnClickListener(view -> {

                if (!FullscreenActivity.Global_Socket_Connection)
                    return;

                view.setEnabled(false);

                Intent intent = new Intent("data_between_activities");
                intent.putExtra("Need_Save", "true" );
                LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);

                FullscreenActivity.Current_Binance_Interval = FullscreenActivity.Config_Data.Chart_Interval;

                MyDebug( "Activity_Creation" , "Context [" + this + "] Class [" + Charts_Main_Fragment.class +"]" );

                Intent id = new Intent(requireActivity(), Charts_Main_Fragment.class);

                TextView text1 = view.findViewById(R.id.text);
                TextView text_value1 = view.findViewById(R.id.value);

                int idx = FullscreenActivity.Find_List_Data(text1.getText().toString());
                String Pr_Chg ;
                if (idx >= 0) {
                    Pr_Chg = FullscreenActivity.CList_Data.get(idx).getPriceChangePercent();
                } else {
                    Pr_Chg = "";
                }


                id.putExtra("CoinName", text1.getText());
                id.putExtra("Value", text_value1.getText());
                id.putExtra("Change", Pr_Chg);

                startActivity(id);

                requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                view.setEnabled(true);
            });

            itemView.requestLayout();

            mGrid.addView(itemView);
            mGrid.requestLayout();
        }
    }



    private int get_width_margin()
    {
        return (8);
    }

    private float Get_DP(int width) {
        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
        return (width / displayMetrics.density);
    }

    private float Get_DP_Width() {
        float dpWidth;

        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (dpWidth);
    }

    private int Get_Width() {

        return ((int) (((Get_DP_Width()) / Calculate_Columns()) - Get_DP((get_width_margin() * 2))));

    }

    private int Calculate_Columns() {
        return ((int) (Get_DP_Width() / (300 + get_width_margin() * 2)));
    }


    private void MyDebug ( String Type , String Message )
    {
        FullscreenActivity.MyDebug( Type , Message ) ;
    }

    private int Get_Pixels(int dpvalue)
    {
        return (dp2px( requireActivity(), dpvalue));
    }

    private static int dp2px(Context ctx, float dp) {

        final float scale = ctx.getResources().getDisplayMetrics().density;

        return (int) (dp * scale + 0.5f);

    }

    public void Config_Changed (  )
    {
        Show_All_Coins();
    }


    public void Update_Coin_Label ( )
    {
        Update_Coin_Label( "" );
    }

    
    public void Update_Coin_Label ( String Coin )
    {
        Activity Act ;

        Act = getActivity()  ;
        if ( Act == null )
            return ;

        if ( FullscreenActivity.CList_Data == null )
            return;

        if ( mGrid.getChildCount() == 0 )
        {
            Show_All_Coins ( ) ;
        }

        int found_item = 0 ;
        for (int x = 0; x < FullscreenActivity.CList_Data.size(); x++)
        {
            String symbol  = FullscreenActivity.CList_Data.get(x).getSymbol();
            String percent = FullscreenActivity.CList_Data.get(x).getPriceChangePercent();
            String price   = FullscreenActivity.CList_Data.get(x).getLastPrice();
            String Volume  = FullscreenActivity.CList_Data.get(x).getVolume();

            if ( ! FullscreenActivity.Config_Data.Filter_Coin.equals( "NONE"))
                if ( ! symbol.endsWith(FullscreenActivity.Config_Data.Filter_Coin) )
                    continue ;

            BigDecimal LPrice = new BigDecimal((price));

            if (LPrice.compareTo(new BigDecimal(FullscreenActivity.Config_Data.min_price)) <= 0)
                continue;

            ++ found_item ;

            if ( found_item > Get_Max_Top_Items() )
                break ;

            View view = mGrid.getChildAt(found_item-1);

            if ( view == null ) {
                MyDebug( "Application" , "Items out of bounds..");
                return;
            }

            TextView text = view.findViewById(R.id.text);
            if ( ! text.getText().equals(symbol))
            {
                FullscreenActivity.Shine_Item ( Act , text , symbol ) ;
            }

            FullscreenActivity.Paint_Coin_Values( Act , view, price, percent , Volume );

        }
    }


    private void Start_Flash_Button ( View Label_Coin_Data )
    {
        if ( Label_Coin_Data == null )
            return ;

        final ShimmerFrameLayout container;

        container = Label_Coin_Data.findViewById(R.id.shimmer_view_container1);

        container.showShimmer( true );
    }

}

