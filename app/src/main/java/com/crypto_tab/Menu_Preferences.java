package com.crypto_tab;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;


public class Menu_Preferences extends AppCompatActivity
    {

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark) ;

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new PrefsFragment())
                    .commit();
        }

        public static class PrefsFragment extends PreferenceFragmentCompat {
            @Override
            public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
            {
                setPreferencesFromResource(R.xml.preferences, rootKey);

                ListPreference pr_value ;
                SwitchPreference sp;
                Preference eP;

                pr_value =  getPreferenceManager().findPreference("filter_coin");
                if ( pr_value != null ) {
                    pr_value.setValue(FullscreenActivity.Config_Data.Filter_Coin);
                    pr_value.setSummary(FullscreenActivity.Config_Data.Filter_Coin );
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.Filter_Coin = newValue.toString();
                        preference.setSummary( FullscreenActivity.Config_Data.Filter_Coin )  ;
                        return true;
                    });
                }

                pr_value =  getPreferenceManager().findPreference("stop_loss_sell_margin");
                if ( pr_value != null ) {
                    pr_value.setValue(FullscreenActivity.Config_Data.Percent_From_Sell_Stop_Loss);
                    pr_value.setSummary(FullscreenActivity.Config_Data.Percent_From_Sell_Stop_Loss + "%%");
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.Percent_From_Sell_Stop_Loss = newValue.toString();
                        preference.setSummary( FullscreenActivity.Config_Data.Percent_From_Sell_Stop_Loss + "%%" )  ;
                        return true;
                    });
                }

                pr_value =  getPreferenceManager().findPreference("Topten");
                if ( pr_value != null ) {
                    pr_value.setValue(FullscreenActivity.Config_Data.Topten.toString());
                    pr_value.setSummary(FullscreenActivity.Config_Data.Topten.toString());
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.Topten = Integer.valueOf( newValue.toString()) ;
                        preference.setSummary( FullscreenActivity.Config_Data.Topten.toString() )  ;
                        return true;
                    });
                }


                sp = getPreferenceManager().findPreference("save_charts");
                assert sp != null;
                sp.setChecked( FullscreenActivity.Config_Data.Save_Charts );
                sp.setOnPreferenceChangeListener((preference, newValue) -> {
                    FullscreenActivity.Config_Data.Save_Charts = (boolean)newValue;
                    return ( true ) ;
                });

                sp =  getPreferenceManager().findPreference("notif_sent");
                assert sp != null;
                sp.setChecked( FullscreenActivity.Config_Data.Notif_Sent );
                sp.setOnPreferenceChangeListener((preference, newValue) -> {
                    FullscreenActivity.Config_Data.Notif_Sent = (boolean)newValue;
                    return ( true ) ;
                });

                sp = getPreferenceManager().findPreference("notif_cancel");
                assert sp != null;
                sp.setChecked( FullscreenActivity.Config_Data.Notif_Cancel );
                sp.setOnPreferenceChangeListener((preference, newValue) -> {
                    FullscreenActivity.Config_Data.Notif_Cancel = (boolean)newValue;
                    return ( true ) ;
                });

                sp = getPreferenceManager().findPreference("notif_part");
                assert sp != null;
                sp.setChecked( FullscreenActivity.Config_Data.Notif_Part_Filled );
                sp.setOnPreferenceChangeListener((preference, newValue) -> {
                    FullscreenActivity.Config_Data.Notif_Part_Filled =  (boolean)newValue;
                    return ( true ) ;
                });

                sp = getPreferenceManager().findPreference("notif_filled");
                assert sp != null;
                sp.setChecked( FullscreenActivity.Config_Data.Notif_Filled );
                sp.setOnPreferenceChangeListener((preference, newValue) -> {
                    FullscreenActivity.Config_Data.Notif_Filled =  (boolean)newValue;
                    return ( true ) ;
                });

                pr_value= getPreferenceManager().findPreference("con_types");
                Set_Conn_Mode ( pr_value ) ;
                assert pr_value != null;
                pr_value.setSingleLineTitle ( false ) ;

                pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                    ListPreference itm = getPreferenceManager().findPreference("con_types");
                    assert itm != null;
                    Set_Config_Mode ( itm.findIndexOfValue( newValue.toString() ) ) ;
                    Set_Conn_Mode   ( itm ) ;

                    return true;
                });

                pr_value= getPreferenceManager().findPreference("chart_types");

                assert pr_value != null;
                pr_value.setSingleLineTitle ( false ) ;
                pr_value.setValue( FullscreenActivity.Config_Data.Chart_Interval );
                Set_Chart_Mode ( pr_value ) ;

                pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                    ListPreference itm = getPreferenceManager().findPreference("chart_types");
                    FullscreenActivity.Config_Data.Chart_Interval = newValue.toString() ;
                    assert itm != null;
                    Set_Chart_Mode  ( itm) ;
                    return true;
                });


                eP = getPreferenceManager().findPreference("alert_id");

                assert eP != null;
                eP.setOnPreferenceClickListener((pref) ->{

                     Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                     intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName() );
                     intent.putExtra(Settings.EXTRA_CHANNEL_ID, FullscreenActivity.Get_Alerts_Channel_ID()) ;
                     startActivity(intent);

                    return (true);
                });

                eP = getPreferenceManager().findPreference("sound_id");
                assert eP != null;
                eP.setOnPreferenceClickListener((pref) -> {

                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, FullscreenActivity.Get_Channel_ID());
                    startActivity(intent);

                    return (true);
                });

                eP = getPreferenceManager().findPreference("sound_pump_id");
                assert eP != null;
                eP.setOnPreferenceClickListener((pref) ->{

                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName() );
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, FullscreenActivity.Get_Channel_Pump_ID()) ;
                    startActivity(intent);

                    return (true);
                });


                pr_value =  getPreferenceManager().findPreference("price_increase");
                if ( pr_value != null )
                {
                    pr_value.setValue( FullscreenActivity.Config_Data.price_increase);
                    pr_value.setSummary(FullscreenActivity.Config_Data.price_increase + " %%");
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.price_increase = newValue.toString();
                        preference.setSummary(FullscreenActivity.Config_Data.price_increase);
                        return true;
                    });
                }

                pr_value =  getPreferenceManager().findPreference("price_decrease");
                if ( pr_value != null )
                {
                    pr_value.setValue( FullscreenActivity.Config_Data.price_decrease);
                    pr_value.setSummary(FullscreenActivity.Config_Data.price_decrease + " %%");
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.price_decrease = newValue.toString();
                        preference.setSummary(FullscreenActivity.Config_Data.price_decrease);
                        return true;
                    });
                }

                pr_value =  getPreferenceManager().findPreference("volume_variation");
                if ( pr_value != null )
                {
                    pr_value.setValue( FullscreenActivity.Config_Data.volume_variation);
                    pr_value.setSummary(FullscreenActivity.Config_Data.volume_variation + " %%");
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.volume_variation = newValue.toString();
                        preference.setSummary(FullscreenActivity.Config_Data.volume_variation);
                        return true;
                    });
                }

                pr_value = getPreferenceManager().findPreference("vol_amount");
                if ( pr_value != null )
                {
                    pr_value.setValue( FullscreenActivity.Config_Data.Min_Volume_Pump);
                    pr_value.setSummary(FullscreenActivity.Config_Data.Min_Volume_Pump);
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.Min_Volume_Pump = newValue.toString();
                        preference.setSummary(FullscreenActivity.Config_Data.Min_Volume_Pump);
                        return true;
                    });
                }


                pr_value = getPreferenceManager().findPreference("min_price");
                if ( pr_value != null )
                {
                    pr_value.setValue( FullscreenActivity.Config_Data.min_price);
                    pr_value.setSummary(FullscreenActivity.Config_Data.min_price);
                    pr_value.setSingleLineTitle ( false ) ;

                    pr_value.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.min_price = newValue.toString();
                        preference.setSummary(FullscreenActivity.Config_Data.min_price);
                        return true;
                    });
                }

                sp = getPreferenceManager().findPreference("detect_vol_var");
                if ( sp != null ) {
                    sp.setChecked(FullscreenActivity.Config_Data.detect_volume_variation);
                    sp.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.detect_volume_variation = (boolean)newValue ;
                        return (true);
                    });
                }

                sp = getPreferenceManager().findPreference("detect_price_increase");
                if ( sp != null ) {
                    sp.setChecked(FullscreenActivity.Config_Data.detect_price_increases);
                    sp.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.detect_price_increases = (boolean)newValue ;
                        return (true);
                    });
                }

                sp = getPreferenceManager().findPreference("detect_price_decrease");
                if ( sp != null ) {
                    sp.setChecked(FullscreenActivity.Config_Data.detect_price_decreases);
                    sp.setOnPreferenceChangeListener((preference, newValue) -> {
                        FullscreenActivity.Config_Data.detect_price_decreases = (boolean ) newValue ;
                        return (true);
                    });
                }
            }

            private void Set_Config_Mode(int value)
            {
                if ( value == 0 )
                    FullscreenActivity.Config_Data.Connection_Type = "ALW" ;
                else if ( value == 1 )
                    FullscreenActivity.Config_Data.Connection_Type = "PART" ;
                else
                    FullscreenActivity.Config_Data.Connection_Type = "NEVER" ;
            }


            private void Set_Conn_Mode ( ListPreference my ) {
                if (FullscreenActivity.Config_Data.Connection_Type.contains("ALW")) {
                    my.setSummary("Always use real time data (websockets). This mode uses huge ammount of data.");
                    my.setValueIndex(0);
                    my.setSingleLineTitle ( false ) ;
                } else if (FullscreenActivity.Config_Data.Connection_Type.contains("PART")) {
                    my.setSummary("Use real time data (websockets) only when you are connected to a WIFI access point.");
                    my.setValueIndex(1);
                    my.setSingleLineTitle ( false ) ;
                } else if (FullscreenActivity.Config_Data.Connection_Type.contains("NEVER")) {
                    my.setSummary("Never use real time data (websockets). Data will be updated each few seconds.");
                    my.setValueIndex(2);
                    my.setSingleLineTitle ( false ) ;

                }

            }

            private void Set_Chart_Mode ( ListPreference my )
            {
                my.setSummary( FullscreenActivity.Config_Data.Chart_Interval );
                my.setSingleLineTitle ( false ) ;

            }


        }

    }
