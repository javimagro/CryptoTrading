package com.crypto_tab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.atomic.AtomicBoolean;


public class Main_Favs_Fragment extends Fragment {

    private View vm;
    private GridLayout mGrid;
    private NestedScrollView mScrollView;
    private FloatingActionButton fab;
    private Point lastTouch;
    private ColorStateList LastFBColor;
    private ValueAnimator mAnimator;
    private final AtomicBoolean mIsScrolling = new AtomicBoolean(false);

    public Main_Favs_Fragment()
    {
        MyDebug( "Main_Favs_Fragment" , "Constructor");

        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MyDebug( "Main_Favs_Fragment" , "OnCreate Method");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        MyDebug( "Main_Favs_Fragment" , "OnCreate_View");

        vm = inflater.inflate(R.layout.favs_panel , container, false);

        FragmentVisible () ;

        return (vm);
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        MyDebug( "Main_Favs_Fragment" , "OnAttach");


    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        MyDebug( "Main_Favs_Fragment" , "OnDettach");

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        FullscreenActivity.Favs_Fragment = null ;
        MyDebug( "Main_Favs_Fragment" , "OnDestroy.");

    }

    private void FragmentVisible()
    {
        MyDebug( "Main_Favs_Fragment" , "Mixed fragment is visible..");

        FullscreenActivity.Favs_Fragment = this;

        mGrid = vm.findViewById(R.id.grid_layout);
        mGrid.setOnDragListener(new DragListener());

        mScrollView = vm.findViewById(R.id.nested_scroll_view);
        mScrollView.setSmoothScrollingEnabled(true);

        fab = vm.findViewById(R.id.fab);
        fab.setTag("");
        fab.setOnClickListener(view -> {

            if (!FullscreenActivity.Global_Socket_Connection)
                return;

            view.setEnabled(false);

            startActivityForResult(new Intent( requireActivity() , Menu_CoinList.class), FullscreenActivity.COIN_CODE);
            requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            view.setEnabled(true);
        });

        fab.setOnDragListener(new DragListener());

        mScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                fab.hide();
            } else {
                fab.show();
            }
        });

        Show_Favourites();

    }

    public void Show_Favourites()
    {
          MyDebug("Main_Favs_Fragment data", "Show favourites.");

            int item;
            int idx_coin;

            mGrid.removeAllViews();
            mGrid.setColumnCount(Calculate_Columns());

            final LayoutInflater inflater = LayoutInflater.from(requireActivity());

            for (idx_coin = 0; idx_coin < FullscreenActivity.Config_Data.Coin_Names.size(); idx_coin++) {

                final View itemView = inflater.inflate(R.layout.items_coin_data_main, mGrid, false);

                final TextView text = itemView.findViewById(R.id.text);
                text.setText(FullscreenActivity.Config_Data.Coin_Names.get(idx_coin));

                final TextView text_value = itemView.findViewById(R.id.value);
                final TextView text_change = itemView.findViewById(R.id.change);

                FullscreenActivity.Show_Volume_Data ( getActivity() , itemView );

                if ((item = FullscreenActivity.Find_List_Data(FullscreenActivity.Config_Data.Coin_Names.get(idx_coin))) >= 0) {
                    FullscreenActivity.Paint_Coin_Values( requireActivity(), itemView, FullscreenActivity.CList_Data.get(item).getLastPrice(), FullscreenActivity.CList_Data.get(item).getPriceChangePercent(), FullscreenActivity.CList_Data.get(item).getVolume());
                } else {
                    text_value.setText("--");
                    text_change.setText("- %");
                }


                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

                    itemView.setLayoutParams(new CardView.LayoutParams(Get_Pixels(Get_Width()), Get_Pixels(80)));

                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                    itemView.setLayoutParams(new CardView.LayoutParams( Get_Pixels(Get_Width()) , Get_Pixels(65)));

                }

                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();

                layoutParams.setMargins( get_width_margin(), get_width_margin(), get_width_margin(), get_width_margin());


                itemView.setOnClickListener(view -> {

                    if (!FullscreenActivity.Global_Socket_Connection)
                        return;

                    view.setEnabled(false);

                    Intent intent = new Intent("data_between_activities");
                    intent.putExtra("Need_Save", "true");
                    LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);

                    FullscreenActivity.Current_Binance_Interval = FullscreenActivity.Config_Data.Chart_Interval;

                    MyDebug("Activity_Creation", "Context [" + this + "] Class [" + Charts_Main_Fragment.class + "]");

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
                    id.putExtra("Change", Pr_Chg );

                    startActivity(id);

                    requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                    view.setEnabled(true);
                });

                itemView.setOnTouchListener(new onTouchEvent());
                itemView.setOnLongClickListener(new LongPressListener());

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

    private void startScrolling(int from, int to) {

        if (from != to && mAnimator == null) {
            mIsScrolling.set(true);
            mAnimator = new ValueAnimator();
            mAnimator.setInterpolator(new OvershootInterpolator());
            mAnimator.setDuration(Math.abs(to - from));
            mAnimator.setIntValues(from, to);
            mAnimator.addUpdateListener(valueAnimator -> mScrollView.smoothScrollTo(0, (int) valueAnimator.getAnimatedValue()));
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsScrolling.set(false);
                    mAnimator = null;
                }
            });
            mAnimator.start();
        }
    }

    private void stopScrolling() {

        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    private void Rebuild_Coin_Names()
    {
        FullscreenActivity.Config_Data.Coin_Names.clear();

        for (int idx = 0; idx < mGrid.getChildCount(); ++idx)
        {
            View itemView = mGrid.getChildAt(idx);
            TextView text = itemView.findViewById(R.id.text);

            FullscreenActivity.Config_Data.Coin_Names.add(text.getText().toString());
        }
    }


    class DragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {

            fab.show();

            final View view = (View) event.getLocalState();

            switch (event.getAction())
            {

                case DragEvent.ACTION_DRAG_LOCATION:

                    if (v == fab)
                    {
                        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.GRAY_DARK)));
                        return (true);
                    }

                    if (view == v)
                        return true;

                    final int index = calculateNewIndex(event.getX(), event.getY());

                    final int scrollY = mScrollView.getScrollY();
                    final Rect rect = new Rect();
                    mScrollView.getHitRect(rect);

                    if (event.getY() - scrollY > mScrollView.getBottom() - 250) {
                        startScrolling(scrollY, mGrid.getHeight());
                    } else if (event.getY() - scrollY < mScrollView.getTop() + 250) {
                        startScrolling(scrollY, 0);
                    } else {
                        stopScrolling();
                    }

                    mGrid.removeView(view);
                    mGrid.addView(view, index);

                    Rebuild_Coin_Names();

                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.RD)));
                    break;

                case DragEvent.ACTION_DROP:

                    if (v == fab) {
                        if (mGrid.getChildCount() > 1) {
                            mGrid.removeView(view);
                            Rebuild_Coin_Names();

                            Intent intent = new Intent("data_between_activities");
                            intent.putExtra("Need_Save", "true" );
                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
                            return (true);
                        }
                    }

                    view.setVisibility(View.VISIBLE);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:



                    if (!event.getResult()) {
                        view.setVisibility(View.VISIBLE);
                    }

                    fab.hide();
                    fab.setImageResource(android.R.drawable.ic_input_add);
                    fab.setBackgroundTintList(LastFBColor);
                    fab.setTag("");
                    fab.show();

                    break;
            }
            return true;
        }


        private int calculateNewIndex(float x, float y)
        {
            final int cellWidth = mGrid.getWidth() / mGrid.getColumnCount();
            final int column = (int) (x / cellWidth);

            final int cellHeight = mGrid.getHeight() / mGrid.getRowCount();
            final int row = (int) Math.floor(y / cellHeight);

            int index = row * mGrid.getColumnCount() + column;
            if (index >= mGrid.getChildCount()) {
                index = mGrid.getChildCount() - 1;
            }

            return index;
        }

    }

    class onTouchEvent implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            lastTouch = new Point((int) event.getX(), (int) event.getY());
            return (false);
        }
    }

    class LongPressListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {

            ClipData data = ClipData.newPlainText("", "");

            View.DragShadowBuilder shadowBuilder = new CustomDragShadowBuilder(view, lastTouch);

            view.startDrag(data, shadowBuilder , view, 0);
            view.setVisibility(View.INVISIBLE);

            fab.setTag("NR");
            LastFBColor = fab.getBackgroundTintList();
            fab.hide();
            fab.setImageResource(R.drawable.delete);
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.RD)));
            fab.show();

            return true;

        }
    }

    public static class CustomDragShadowBuilder extends View.DragShadowBuilder {

        private final Point _offset;

        private CustomDragShadowBuilder(View view, Point offset) {

            super(view);

            _offset = offset;
        }

        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            size.set(getView().getWidth(), getView().getHeight());
            touch.set(_offset.x, _offset.y);
        }
    }

    private int Get_Pixels(int dpvalue) {

        return (dp2px(requireActivity(), dpvalue));
    }

    private static int dp2px(Context ctx, float dp) {

        final float scale = ctx.getResources().getDisplayMetrics().density;

        return (int) (dp * scale + 0.5f);

    }

    public void Update_Coin_Label(  )
    {
        Update_Coin_Label( "" );
    }

    public void Update_Coin_Label( String Coin )
    {
        if ( FullscreenActivity.CList_Data == null)
            return;


        for (int idx = 0; idx < mGrid.getChildCount(); ++idx)
        {
              View view = mGrid.getChildAt(idx);
              if (view != null)
              {
                    TextView text = view.findViewById(R.id.text);
                    String CoinNm = text.getText().toString();

                    if ( Coin.length() > 0 && ! Coin.equals(CoinNm) )
                        continue ;

                    int coin_idx = FullscreenActivity.Find_List_Data( CoinNm );
                    if ( coin_idx >= 0 )
                    {
                        FullscreenActivity.Coin_Data CD ;

                        CD = FullscreenActivity.CList_Data.get ( coin_idx ) ;

                        String percent = CD.getPriceChangePercent();
                        String price   = CD.getLastPrice();
                        String Volume  = CD.getVolume();

                        FullscreenActivity.Paint_Coin_Values(requireActivity(), view, price, percent , Volume );
                    }
              }
        }
    }

    public void Config_Changed ( )
    {
        Show_Favourites();
    }
}

