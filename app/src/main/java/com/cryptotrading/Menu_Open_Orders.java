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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.binance.api.client.domain.account.Order;

import java.util.List;


public class Menu_Open_Orders extends AppCompatActivity {

    public static SwipeRefreshLayout swipeRefreshLayout;

    private ProgressBar spinner;
    private List<Order> OO;
    Activity my_activity ;

    private RecyclerView.Adapter Order_List_Adapter;

    private RecyclerView rcvview ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_open_orders);

        my_activity = this ;

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(Color.BLACK);


        spinner = findViewById(R.id.mpg_progressBar);

        swipeRefreshLayout = null ;
        OO = null ;

        ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark) ;

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled( true );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                OO = FullscreenActivity.Get_Open_Orders ( null ) ;
                if ( OO != null )
                {
                    Order_List_Adapter = new Order_List_Adapter( OO);
                    rcvview.setAdapter(Order_List_Adapter);
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });

            new Load_Data(this).execute();

    }

    public class Load_Data extends AsyncTask<Void, Integer, Boolean> {
        private Activity activity;


        private Load_Data(Activity activity) {
            this.activity = activity;
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

            OO = FullscreenActivity.Get_Open_Orders ( null ) ;
            return (true);

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            RecyclerView.LayoutManager layoutManager;

            rcvview = findViewById(R.id.open_order_list);

            if ( rcvview == null )
                return ;

            if ( my_activity == null )
                return ;

            layoutManager = new LinearLayoutManager(my_activity);
            rcvview.setLayoutManager(layoutManager);

            new SwipeHelper(getBaseContext(), rcvview) {
                @Override
                public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons)
                {
                    underlayButtons.add(new SwipeHelper.UnderlayButton(
                            "",
                            R.drawable.delete2,
                            Color.parseColor("#e53935"),
                            new SwipeHelper.UnderlayButtonClickListener() {
                                @Override
                                public void onClick(int pos)
                                {
                                    if ( OO != null ) {

                                        FullscreenActivity.Cancel_Open_Order( OO.get ( pos ) );
                                        OO.remove(pos);
                                        Order_List_Adapter.notifyDataSetChanged();

                                        swipeRefreshLayout.setEnabled( true );
                                    }
                                }
                            }
                    ));
                }
            };

            if ( OO != null)
            {
                Order_List_Adapter = new Order_List_Adapter ( OO );
                rcvview.setAdapter(Order_List_Adapter);

            }
            spinner.setVisibility(View.GONE);
        }
    }



    public class Order_List_Adapter extends RecyclerView.Adapter<Menu_Open_Orders.Order_List_Adapter.MyViewHolder>
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

            @Override
            public Menu_Open_Orders.Order_List_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                CardView v = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.items_open_order, parent, false);

                return ( new Menu_Open_Orders.Order_List_Adapter.MyViewHolder(v));

            }

            public void onBindViewHolder(Menu_Open_Orders.Order_List_Adapter.MyViewHolder holder, int position)
            {
                if ( position > values.size())
                    return ;

                TextView textView = holder.cardView.findViewById(R.id.coinname);
                textView.setText(values.get(position).getSymbol());
                textView.setTextColor( Color.WHITE );


                String side = values.get(position).getSide().toString() ;

                TextView textView2 = holder.cardView.findViewById(R.id.type_id3);

                if ( side.contains( "SELL"))
                    textView2.setBackgroundColor( Color.RED );
                else
                    textView2.setBackgroundColor( Color.GREEN );

                    textView = holder.cardView.findViewById(R.id.eq_amount);
                    textView.setText( FullscreenActivity.Round_Number(values.get(position).getOrigQty()));
                    textView.setTextColor( Color.WHITE );

                    String price = values.get(position).getPrice();
                    textView = holder.cardView.findViewById(R.id.eq_price);
                    textView.setText( FullscreenActivity.Round_Number(price ));
                    textView.setTextColor( Color.WHITE );

                    android.icu.math.BigDecimal coin_price = new android.icu.math.BigDecimal( price ) ;
                    android.icu.math.BigDecimal items      = new BigDecimal( values.get(position).getOrigQty()) ;

                    price = coin_price.multiply(  items ).toString();

                    textView = holder.cardView.findViewById(R.id.eq_btc);
                    textView.setText( FullscreenActivity.Round_Number(price) );

            }

                // Return the size of your dataset (invoked by the layout manager)
                @Override
            public int getItemCount()
            {
                    return (values.size());
            }
    }

}


