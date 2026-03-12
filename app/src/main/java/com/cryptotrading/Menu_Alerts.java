package com.crypto_tab;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;


public class Menu_Alerts extends AppCompatActivity
{

    public static SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView.Adapter AAdapter;

    private RecyclerView rcvview ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        RecyclerView.LayoutManager layoutManager;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_open_alerts);

        getWindow().setNavigationBarColor(Color.BLACK);

        ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark) ;

        AAdapter = new Alerts_Adapter( FullscreenActivity.Alerts_List );
        layoutManager = new LinearLayoutManager(this);

        rcvview = findViewById(R.id.alerts_list);
        rcvview.setAdapter(AAdapter);
        rcvview.setLayoutManager(layoutManager);

        new SwipeHelper(getBaseContext(), rcvview)
        {
                @Override
                public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons)
                {
                    underlayButtons.add(new UnderlayButton(
                            "",
                            R.drawable.delete2,
                            Color.parseColor("#e53935"),
                            new UnderlayButtonClickListener() {
                                @Override
                                public void onClick(int pos)
                                {
                                    if ( FullscreenActivity.Alerts_List.size () > 0 )
                                    {
                                        String Symbol ;
                                        long   OrderID ;
                                        FullscreenActivity.Alerts_List.remove( pos ) ;
                                        AAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                    ));
                }
        };

        if ( !FullscreenActivity.Get_Subscription_Buys() )
        {
            if ( FullscreenActivity.Alerts_List.size() >= FullscreenActivity.MAX_ALERTS_ALLOWED )
            {
                My_Toast( "Only three alerts are allowed in the free version.\n\nPlease register it.");
            }
        }

    }

    public class Alerts_Adapter extends RecyclerView.Adapter<Menu_Alerts.Alerts_Adapter.MyViewHolder>
    {
            private final List<FullscreenActivity.Alerts> values;

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

            private Alerts_Adapter( List<FullscreenActivity.Alerts> values )
            {
                this.values = values;
                MyDebug( "Alerts" , "Set Alert values.. " + values.size() );
            }

            @Override
            public Menu_Alerts.Alerts_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                CardView v = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.items_alert, parent, false);

                return ( new Menu_Alerts.Alerts_Adapter.MyViewHolder(v));

            }

            public void onBindViewHolder(Menu_Alerts.Alerts_Adapter.MyViewHolder holder, int position)
            {
                if ( position > values.size())
                    return ;

                if ( !FullscreenActivity.Get_Subscription_Buys() )
                {
                    if ( position >= FullscreenActivity.MAX_ALERTS_ALLOWED )
                    {
                        holder.cardView.setVisibility( ViewGroup.GONE );
                        return ;
                    }
                }
                holder.cardView.setVisibility(ViewGroup.VISIBLE );

                TextView textView = holder.cardView.findViewById(R.id.coinname);
                TextView textView2 = holder.cardView.findViewById(R.id.type_id3);
                textView.setText(values.get(position).Label);

                if ( values.get(position).repeat) {
                    textView2.setBackgroundColor( Color.CYAN );
                }
                else {
                    textView2.setBackgroundColor( Color.LTGRAY );
                }

                String price = values.get(position).Alert_Price;
                textView = holder.cardView.findViewById(R.id.eq_price);
                textView.setText( FullscreenActivity.Round_Number(price ));

            }

                // Return the size of your dataset (invoked by the layout manager)
            @Override
            public int getItemCount()
            {
                    return (values.size());
            }
    }

    private void MyDebug ( String Type , String Message )
    {
        FullscreenActivity.MyDebug( Type , Message ) ;
    }

    private void My_Toast ( String Msg )
    {
        FullscreenActivity.My_Toast( Msg);
    }

}


