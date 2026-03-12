package com.crypto_tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binance.api.client.domain.market.AggTrade;

import java.util.List;


public class Charts_Trades_Fragment extends Fragment
{

    public Charts_Main_Fragment Prev_Act ;

    RecyclerView Trades_RV ;

    public Charts_Trades_Fragment()
    {
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
        View vm ;

        vm = inflater.inflate(R.layout.charts_trades_fragment, container, false);

        Trades_RV  = vm.findViewById(R.id.orders);
        Trades_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
        Trades_RV.setAdapter(new EmptyRecycler());

        FragmentEntered();

        return ( vm ) ;

    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    public void FragmentEntered()
    {
        Prev_Act = (Charts_Main_Fragment) getActivity();
        if (Prev_Act == null)
            return;

        Prev_Act.Trades_Fragment =  this ;

    }

    public void FragmentRefresh()
    {
        Prev_Act = (Charts_Main_Fragment) getActivity() ;
        if ( Prev_Act == null )
            return ;

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    public void Update_Trades ()
    {
        Trades_Adapter mAdapter ;

        if ( getActivity() == null )
            return ;


        Trades_RV.setHasFixedSize(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        Trades_RV.setLayoutManager(layoutManager);
        mAdapter = new Trades_Adapter(Prev_Act.Mixed_Fragment.CacheTrades);
        Trades_RV.setAdapter(mAdapter);

        Trades_RV.requestLayout();

    }

    public class Trades_Adapter extends RecyclerView.Adapter<Trades_Adapter.MyViewHolder>
    {
        List<AggTrade> mDataset;

        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            private CardView cardView;

            private MyViewHolder(CardView v) {
                super(v);
                cardView = v;
                cardView.setLayoutParams(new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        private Trades_Adapter( List<AggTrade> myDataset  )
        {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public Trades_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.items_trades_extended, parent, false);

            return ( new Trades_Adapter.MyViewHolder(v))  ;

        }

        private void Test_Buy_Order_Price ( String price , CardView v )
        {
            if ( getActivity () == null )
                return ;

            if ( Prev_Act.Mixed_Fragment == null )
                return ;

            if ( Prev_Act.Mixed_Fragment.Find_Limit_Line_By_Value( price ) >= 0) {
                v.setBackgroundColor(Color.parseColor( "#494949"));
            } else {
                v.setBackgroundColor( Color.TRANSPARENT);
            }
        }



        @SuppressLint("DefaultLocale")
        public void onBindViewHolder(Trades_Adapter.MyViewHolder holder, int position)
        {
            try {

                TextView v = holder.cardView.findViewById(R.id.trade_date);
                TextView v2 = holder.cardView.findViewById(R.id.trade_price);
                TextView v3 = holder.cardView.findViewById(R.id.trade_items);
                TextView v4 = holder.cardView.findViewById(R.id.trades_btc);

                if (getActivity() == null)
                    return;

                if (position >= mDataset.size())
                    return;

                position = mDataset.size() - position - 1;

                Calendar Cl = Calendar.getInstance();
                Cl.setTimeInMillis(mDataset.get(position).getTradeTime());
                v.setText(String.format("%02d:%02d:%02d", Cl.get(Calendar.HOUR_OF_DAY), Cl.get(Calendar.MINUTE), Cl.get(Calendar.SECOND)));

                String price = mDataset.get(position).getPrice();
                String items = mDataset.get(position).getQuantity();
                float btc = Float.parseFloat(price) * Float.parseFloat(items);

                if (mDataset.get(position).isBuyerMaker()) {
                    v.setTextColor(Color.WHITE);
                    if (  Prev_Act.Mixed_Fragment.Find_Alert_Value ( price ) )
                        v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.LKRD));
                    else
                        v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.RD));
                } else {
                    v.setTextColor(Color.WHITE);

                    if (  Prev_Act.Mixed_Fragment.Find_Alert_Value ( price ) )
                        v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.LKGR));
                    else
                        v2.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.GR));
                }

                v2.setText(FullscreenActivity.Round_Number(price));
                v3.setText(FullscreenActivity.Qty_Round ( Charts_Main_Fragment.Text_coin_RT , items));
                v4.setText(FullscreenActivity.Round_Number(String.valueOf(btc)));

                Test_Buy_Order_Price(price, holder.cardView);

                holder.cardView.setOnClickListener(view -> {

                    TextView v1;
                    BigDecimal fprice;

                    v1 = view.findViewById(R.id.trade_price);

                    fprice = new BigDecimal((v1.getText().toString()));

                    if (Prev_Act.Mixed_Fragment != null)
                        Prev_Act.Mixed_Fragment.Show_Buy_Dialog(fprice.toString());
                });
            } catch (  Exception e )
            {
                MyDebug(  "Exception" , e.getMessage() );
            }
        }

    // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount()
        {
            if ( mDataset == null )
                return ( 0 ) ;

             return (mDataset.size());
        }
    }

    private float Get_DP_Width()
    {
        if ( getActivity () == null )
            return ( 0 ) ;

        DisplayMetrics displayMetrics = getActivity().getApplicationContext().getResources().getDisplayMetrics();

        return ( displayMetrics.widthPixels / displayMetrics.density ) ;

    }

    private void MyDebug ( String Type , String Message )
    {
        FullscreenActivity.MyDebug( Type , Message ) ;
    }

}
