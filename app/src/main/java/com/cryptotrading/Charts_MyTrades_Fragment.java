package com.crypto_tab;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.binance.api.client.domain.account.Trade;

import java.util.Date;
import java.util.List;


public class Charts_MyTrades_Fragment extends Fragment {

    View vm ;
    public Charts_Main_Fragment Prev_Act ;

    private ProgressBar spinner;
    private List<Trade> OO;

    public static Charts_MyTrades_Fragment My_Trades_Fragment ;

    public SwipeRefreshLayout swipeRefreshLayout;
    private Order_List_Adapter OList_Adapter;

    RecyclerView My_Trades_RV ;

    public Charts_MyTrades_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        My_Trades_Fragment = this  ;

        vm = inflater.inflate(R.layout.charts_mytrades_fragment, container, false);

        My_Trades_RV  = vm.findViewById(R.id.open_order_list);
        My_Trades_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
        My_Trades_RV.setAdapter(new EmptyRecycler());


        FragmentEntered () ;

        return ( vm ) ;


    }

    public static Charts_MyTrades_Fragment getInstance ( )
    {
        return ( My_Trades_Fragment ) ;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        My_Trades_Fragment = null ;
    }

    public void FragmentEntered()
    {
        Prev_Act = (Charts_Main_Fragment) getActivity() ;
        if ( Prev_Act == null )
            return ;

        spinner = vm.findViewById(R.id.pb);

        OO = null ;

        swipeRefreshLayout = vm.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled( true );

        swipeRefreshLayout.setOnRefreshListener(() -> {

            Refresh_OO ( null) ;

            swipeRefreshLayout.setRefreshing(false);
        });


            new Load_Data().execute();
    }


    public void FragmentRefresh()
    {
        Prev_Act = (Charts_Main_Fragment) getActivity() ;
        if ( Prev_Act == null )
            return ;

        Refresh_OO ( null ) ;

    }


    @SuppressLint("NotifyDataSetChanged")
    public void Refresh_OO (  List<Trade> TD )
    {
        if ( Prev_Act == null )
            return ;

        if (Charts_Main_Fragment.Text_coin_RT == null )
            return ;

        if ( TD == null )
            OO = FullscreenActivity.Get_All_Trades (Charts_Main_Fragment.Text_coin_RT) ;
        else
            OO = TD ;

        if(OO !=null)
        {
            if (OList_Adapter == null )
            {
                OList_Adapter = new Order_List_Adapter( OO );
                Prev_Act.runOnUiThread(() -> My_Trades_RV.setAdapter(OList_Adapter));
            }

            OList_Adapter.Update_Order_List (OO) ;
            Prev_Act.runOnUiThread(() -> OList_Adapter.notifyDataSetChanged());

        }
    }


    public class Load_Data extends AsyncTask<Void, Integer, Boolean> {


        private Load_Data()
        {
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            spinner.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {

            OO = FullscreenActivity.Get_All_Trades ( Prev_Act.Text_coin_RT ) ;
            return (true);

        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);

            spinner.setVisibility(View.GONE);

            RecyclerView.LayoutManager layoutManager;

            if ( My_Trades_Fragment == null )
                return ;

            if ( My_Trades_Fragment.getContext() == null )
                return ;

            layoutManager = new LinearLayoutManager(My_Trades_Fragment.getContext());
            My_Trades_RV.setLayoutManager(layoutManager);

            if ( OO != null)
            {
                OList_Adapter = new Order_List_Adapter( OO );
                My_Trades_RV.setAdapter(OList_Adapter);
            }
        }
    }



    public class Order_List_Adapter extends RecyclerView.Adapter<Order_List_Adapter.MyViewHolder>
    {
        private final List<Trade> values;


        class MyViewHolder extends RecyclerView.ViewHolder
        {
            // each data item is just a string in this case
            private CardView cardView;

            private MyViewHolder(CardView v)
            {
                super(v);
                cardView = v;
                cardView.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        private Order_List_Adapter( List<Trade> values )
        {
            this.values = values;
        }

        @NonNull
        @Override
        public Order_List_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.items_trade_chart, parent, false);

            return ( new Order_List_Adapter.MyViewHolder(v));

        }

        private void Update_Order_List ( List<Trade> new_values )
        {
            values.clear();
            values.addAll( new_values ) ;
        }

        @SuppressLint("SetTextI18n")
        public void onBindViewHolder(@NonNull Order_List_Adapter.MyViewHolder holder, int position)
        {
            if ( position > values.size())
                return ;

            if ( ! FullscreenActivity.Use_KuCoin)
                position = values.size()- position - 1  ;

            TextView tpView = holder.cardView.findViewById(R.id.type_id);

            if ( values.get(position).isBuyer())
            {
                tpView.setBackgroundColor(Color.GREEN);
            }
            else {
                tpView.setBackgroundColor( Color.RED );
            }

            TextView tdate = holder.cardView.findViewById(R.id.date);

            Date df = new java.util.Date(values.get(position).getTime());
            String vv = new SimpleDateFormat("MM/dd/yyyy - hh:mm:ss").format(df);
            tdate.setText( vv );

            TextView tamount = holder.cardView.findViewById(R.id.eq_amount);
            tamount.setText( FullscreenActivity.Round_Number(values.get(position).getQty()) + " x ");
            tamount.setTextColor( Color.WHITE );

            String price = values.get(position).getPrice();
            TextView tprice = holder.cardView.findViewById(R.id.eq_price);
            tprice.setText( FullscreenActivity.Round_Number(price ));
            tprice.setTextColor( Color.WHITE );

            android.icu.math.BigDecimal coin_price = new android.icu.math.BigDecimal( price ) ;
            android.icu.math.BigDecimal items      = new BigDecimal( values.get(position).getQty()) ;

            price = coin_price.multiply(  items ).toString();

            TextView teq = holder.cardView.findViewById(R.id.eq_btc);
            teq.setText( "BTC: " + FullscreenActivity.Round_Number(price) );

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount()
        {
            return (values.size());
        }
    }
    private float Get_DP_Width()
    {
        DisplayMetrics displayMetrics = requireActivity().getApplicationContext().getResources().getDisplayMetrics();

        return ( displayMetrics.widthPixels / displayMetrics.density ) ;

    }


}