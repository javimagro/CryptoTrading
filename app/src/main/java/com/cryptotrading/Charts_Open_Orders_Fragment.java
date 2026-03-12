package com.crypto_tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.binance.api.client.domain.account.Order;

import java.util.List;
import java.util.Objects;


public class  Charts_Open_Orders_Fragment extends Fragment {
    public Charts_Main_Fragment Prev_Act;

    private ProgressBar spinner;
    private List<Order> OO;


    public static Charts_Open_Orders_Fragment Open_Orders_Activity  ;

    public static SwipeRefreshLayout swipeRefreshLayout;

    private Order_List_Adapter OList_Adapter;

    View vm;

    RecyclerView Open_Orders_RV ;

    public Charts_Open_Orders_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
                super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Open_Orders_Activity = this ;


        MyDebug ( "Open_Orders_Fragments" , "On create.... ") ;

        vm = inflater.inflate(R.layout.charts_open_orders_fragment, container, false);

        Open_Orders_RV  = vm.findViewById(R.id.open_order_list);
        Open_Orders_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
        Open_Orders_RV.setAdapter(new EmptyRecycler());

        FragmentEntered();

        return (vm);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        MyDebug ( "Open_Orders_Fragments" , "On destroy.... ") ;

        Open_Orders_Activity = null ;
    }

    public static Charts_Open_Orders_Fragment getInstance ( )
    {
        return ( Open_Orders_Activity ) ;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void FragmentEntered()
    {

        MyDebug ( "Open_Orders_Fragments" , "Enter in fragment.... ") ;


        Prev_Act = (Charts_Main_Fragment) getActivity();

        spinner = vm.findViewById(R.id.pb);

        swipeRefreshLayout = null;
        OO = null;

        swipeRefreshLayout = vm.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(() -> {

             Refresh_OO( null );

            swipeRefreshLayout.setRefreshing(false);
        });

            new Load_Data().execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void Refresh_OO ( List<Order> OldOrders )
    {
        if ( Prev_Act == null )
            return ;

        if (Prev_Act.Text_coin_RT == null )
            return ;

        if ( OldOrders == null )
            OO =FullscreenActivity.Get_Open_Orders(Prev_Act.Text_coin_RT );
        else
            OO = OldOrders ;

        if(OO !=null)
        {
            if (OList_Adapter == null )
            {
                OList_Adapter = new Order_List_Adapter( OO );
                Prev_Act.runOnUiThread(() -> Open_Orders_RV.setAdapter(OList_Adapter));
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

            MyDebug ( "Open_Orders_Fragments" , "Load_Data()") ;


            spinner.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            MyDebug ( "Open_Orders_Fragments" , "DoInBackground") ;

            OO = FullscreenActivity.Get_Open_Orders ( Prev_Act.Text_coin_RT ) ;
            return (true);

        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);

            spinner.setVisibility(View.GONE);

            MyDebug ( "Open_Orders_Fragments" , "onPostExecute") ;

            RecyclerView.LayoutManager layoutManager;

            if ( Open_Orders_Activity == null )
                return ;

            if ( Open_Orders_Activity.getContext() == null )
                return ;

            layoutManager = new LinearLayoutManager(Open_Orders_Activity.getContext());
            Open_Orders_RV.setLayoutManager(layoutManager);

            if ( OO != null)
            {
                OList_Adapter = new Order_List_Adapter( OO );
                Open_Orders_RV.setAdapter(OList_Adapter);
            }

        }

    }

    public class Order_List_Adapter extends RecyclerView.Adapter<Order_List_Adapter.MyViewHolder>
    {
        private final List<Order> values;

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

        private Order_List_Adapter( List<Order> values )
        {
            this.values = values;
        }

        @NonNull
        @Override
        public Order_List_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.items_open_order_chart, parent, false);

            return ( new Order_List_Adapter.MyViewHolder(v));

        }

        private void Update_Order_List ( List<Order> new_values )
        {
            values.clear();
            values.addAll( new_values ) ;
        }

        @SuppressLint("SetTextI18n")
        public void onBindViewHolder(@NonNull Order_List_Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position)
        {
            if ( position > values.size())
                return ;

            TextView textView = holder.cardView.findViewById(R.id.type_id);

            String side = values.get(position).getSide().toString() ;

            if ( side.equals( "SELL"))
                textView.setBackgroundColor( Color.RED );
            else
                textView.setBackgroundColor( Color.GREEN );

            textView = holder.cardView.findViewById(R.id.eq_amount);
            textView.setText( FullscreenActivity.Round_Number(values.get(position).getOrigQty()) + " x ");
            textView.setTextColor( Color.WHITE );

            String price = values.get(position).getPrice();
            TextView tprice = holder.cardView.findViewById(R.id.eq_price);
            tprice.setText( FullscreenActivity.Round_Number(price ));
            tprice.setTextColor( Color.WHITE );

            android.icu.math.BigDecimal coin_price = new android.icu.math.BigDecimal( price ) ;
            android.icu.math.BigDecimal items      = new BigDecimal( values.get(position).getOrigQty()) ;

            price = coin_price.multiply(  items ).toString();

            TextView tbtc = holder.cardView.findViewById(R.id.eq_btc);
            tbtc.setText( "BTC: " + FullscreenActivity.Round_Number(price) );



            ImageButton ib = holder.cardView.findViewById(R.id.cancel_id);
            ib.setOnClickListener(v -> {

                FullscreenActivity.Cancel_Open_Order( values.get (position ) );

                values.remove(position);
                notifyDataSetChanged();
            });
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
        DisplayMetrics displayMetrics = Objects.requireNonNull(getActivity()).getApplicationContext().getResources().getDisplayMetrics();

        return ( displayMetrics.widthPixels / displayMetrics.density ) ;

    }

    private void MyDebug ( String Type , String Message )
    {
        FullscreenActivity.MyDebug( Type , Message ) ;
    }
}
