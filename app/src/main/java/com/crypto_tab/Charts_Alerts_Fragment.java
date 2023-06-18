package com.crypto_tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class Charts_Alerts_Fragment extends Fragment {

    public Charts_Main_Fragment Prev_Act;
    public static Charts_Alerts_Fragment Alerts_Activity  ;

    private Alerts_Adapter AAdapter;

    View vm;

    RecyclerView Alerts_RV ;

    public Charts_Alerts_Fragment() {
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
        Alerts_Activity = this ;

        vm = inflater.inflate(R.layout.charts_open_alerts, container, false);

        Alerts_RV  = vm.findViewById(R.id.alerts_list);
        Alerts_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
        Alerts_RV.setAdapter(new EmptyRecycler());

        FragmentEntered();

        return (vm);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        Alerts_Activity = null ;
    }

    public static Charts_Alerts_Fragment getInstance ( )
    {
        return ( Alerts_Activity ) ;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void FragmentEntered() {

            Full_Refresh_Alerts();

    }

    public void Refresh_Alerts ()
    {
        if ( Prev_Act == null )
            return ;

        if ( AAdapter == null )
            return ;

        if (Charts_Main_Fragment.Text_coin_RT == null )
            return ;

        AAdapter.Update_Order_List ( Get_Filtered ( ) ) ;
        Prev_Act.runOnUiThread(() -> AAdapter.notifyDataSetChanged());

    }

    private List<FullscreenActivity.Alerts> Get_Filtered ( )
    {
        List<FullscreenActivity.Alerts> Alts_List ;

        if ( FullscreenActivity.Alerts_List == null )
            return ( null ) ;

        Alts_List = new ArrayList<>();

        for ( int idx = 0 ; idx < FullscreenActivity.Alerts_List.size() ; ++ idx )
        {
            if (!FullscreenActivity.Get_Subscription_Buys())
            {
                if (idx >= FullscreenActivity.MAX_ALERTS_ALLOWED)
                {
                    break;
                }
            }

            if (Charts_Main_Fragment.Text_coin_RT.equals(FullscreenActivity.Alerts_List.get(idx).Label))
            {
                Alts_List.add(FullscreenActivity.Alerts_List.get(idx));
            }
        }
        return ( Alts_List );

    }


    public void Full_Refresh_Alerts ()
    {
        RecyclerView.LayoutManager layoutManager;

        Prev_Act = (Charts_Main_Fragment) getActivity();
        layoutManager = new LinearLayoutManager(Alerts_Activity.getContext());

        AAdapter = new Alerts_Adapter( Get_Filtered ( ) );
        Alerts_RV.setAdapter(AAdapter);
        Alerts_RV.setLayoutManager(layoutManager);

    }

    public class Alerts_Adapter extends RecyclerView.Adapter<Alerts_Adapter.MyViewHolder>
    {
        private final List<FullscreenActivity.Alerts> values;

        class MyViewHolder extends RecyclerView.ViewHolder
        {
            // each data item is just a string in this case
            private final CardView cardView;

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
        }

        @NonNull
        @Override
        public Alerts_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.items_alert_fragment, parent, false);

            return ( new Alerts_Adapter.MyViewHolder(v));

        }

        private void Update_Order_List ( List<FullscreenActivity.Alerts> new_values )
        {
            if ( values !=null )
            {
                values.clear();
                values.addAll(new_values);
            }
        }

        @SuppressLint("SetTextI18n")
        public void onBindViewHolder(@NonNull Alerts_Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position)
        {
            if ( values == null )
                return ;

            if ( position > values.size())
                return ;

            if ( !FullscreenActivity.Get_Subscription_Buys() )
            {
                if ( position >= FullscreenActivity.MAX_ALERTS_ALLOWED )
                {
                    holder.cardView.setVisibility(View.INVISIBLE);
                    return ;
                }
            }

            holder.cardView.setVisibility(ViewGroup.VISIBLE );

            TextView textView = holder.cardView.findViewById(R.id.coinname);
            if ( values.get(position).repeat)
            {
               textView.setText("Repeating Alert");
               textView.setTextColor(Color.CYAN);
            }
            else {
               textView.setText("Normal Alert");
               textView.setTextColor(Color.GREEN);
            }


            final String price = values.get(position).Alert_Price;
            TextView textView2 = holder.cardView.findViewById(R.id.eq_price);
            TextView textView3 = holder.cardView.findViewById(R.id.price);
            textView2.setText( FullscreenActivity.Round_Number(price ));

            ImageView ib = holder.cardView.findViewById(R.id.cancel_id);
            ib.setOnClickListener(v ->
            {
                values.remove(position);
                Remove_From_Main ( price , Charts_Main_Fragment.Text_coin_RT );
                notifyDataSetChanged();
            });
        }

        void Remove_From_Main ( String Price , String Label )
        {
            if ( FullscreenActivity.Alerts_List == null )
                return ;

            for ( int idx = 0 ; idx < FullscreenActivity.Alerts_List.size() ; ++ idx )
            {
                if ( Label.equals(FullscreenActivity.Alerts_List.get(idx).Label) && Price.equals(FullscreenActivity.Alerts_List.get(idx).Alert_Price))
                {
                    FullscreenActivity.Alerts_List.remove( idx ) ;
                }
            }
        }


        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount()
        {
            if ( values == null )
                return ( 0 ) ;

            return (values.size());
        }
    }

    private float Get_DP_Width()
    {
        if ( getActivity () == null )
            return ( 0 ) ;

        DisplayMetrics displayMetrics = getActivity().getApplicationContext().getResources().getDisplayMetrics();

        return ( displayMetrics.widthPixels / displayMetrics.density ) ;

    }


}
