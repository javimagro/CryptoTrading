package com.crypto_tab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.kucoin.sdk.KucoinClientBuilder;
import com.kucoin.sdk.KucoinRestClient;
import com.kucoin.sdk.rest.response.AccountBalancesResponse;

import org.json.JSONObject;

import java.util.List;

public class Menu_Config_Kucoin_Keys extends AppCompatActivity
{
    public static KucoinRestClient kucoinRestClient ;
    public static KucoinClientBuilder kucoin_builder ;

    int thread_back_mode ;
    boolean tested_keys = false ;



    public boolean Save_Keys ()
    {
        if ( ! tested_keys  )
        {
            Log.i ( "Check_keys" , "Keys not tested");
            My_Toast ( (String)"You need test the keys before." );

            FullscreenActivity.Config_Data.Config_Keys.Kucoin_Private_Key = "" ;
            FullscreenActivity.Config_Data.Config_Keys.Kucoin_Public_Key  = "" ;
            FullscreenActivity.Config_Data.Config_Keys.Kucoin_passPhrase  = "" ;

            return ( false )  ;
        }

        TextView Private_Key;
        TextView Public_Key;
        TextView PassPhrase ;

        Private_Key = findViewById(R.id.privateKEY);
        Public_Key = findViewById(R.id.publicKEY);
        PassPhrase = findViewById(R.id.PassPhrase);

        FullscreenActivity.Config_Data.Config_Keys.Kucoin_Private_Key = Private_Key.getText().toString();
        FullscreenActivity.Config_Data.Config_Keys.Kucoin_Public_Key  = Public_Key.getText().toString();
        FullscreenActivity.Config_Data.Config_Keys.Kucoin_passPhrase  = PassPhrase.getText().toString();

        return ( true ) ;
    }


    public int Test_Keys () {

        TextView Private_Key ;
        TextView Public_Key ;
        TextView PassPhrase ;
        String PrKey ;
        String PbKey ;
        String PssKey ;

        tested_keys = false ;

        thread_back_mode = 0 ;

        Private_Key = findViewById(R.id.privateKEY);
        Public_Key  = findViewById(R.id.publicKEY);
        PassPhrase  = findViewById(R.id.PassPhrase);

        PrKey  = Private_Key.getText().toString() ;
        PbKey  = Public_Key.getText().toString() ;
        PssKey = PassPhrase.getText().toString() ;

        if ( PrKey.length() == 0 )
        {
            Log.i ( "Check_keys" , "Invalid private key");
            My_Toast ( (String)"Invalid Private Key" );
            return ( 0 );
        }

        if ( PbKey.length() == 0 )
        {
            Log.i ( "Check_keys" , "Invalid public key");
            My_Toast ( (String)"Invalid Public Key");
            return ( 0 );
        }

        if ( PssKey.length() == 0 )
        {
            Log.i ( "Check_keys" , "Invalid PassPhrase.");
            My_Toast ( (String)"Invalid PassPhrase");
            return ( 0 );
        }


        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    kucoin_builder   = new KucoinClientBuilder().withBaseUrl("https://openapi-v2.kucoin.com").withApiKey(PrKey , PbKey , PssKey);
                    kucoinRestClient = kucoin_builder.buildRestClient();

                    List<AccountBalancesResponse> ABRL = kucoinRestClient.accountAPI().listAccounts  ( "" , "trade" );

                    if (ABRL != null)
                    {
                        Log.i("Check_keys", "Keys are valid. you have " + ABRL.get(0).getBalance() + " " +  ABRL.get(0).getCurrency());
                        My_Toast("Keys are valid. you have " + ABRL.get(0).getBalance() + " "  + ABRL.get(0).getCurrency() );
                        thread_back_mode = 0;
                        tested_keys = true;
                    }
                    else
                    {
                        My_Toast( "Invalid keys...");
                    }

                    thread_back_mode = 1 ;

                } catch (Exception e)
                {
                    Log.i("Check_keys", e.getMessage());
                    My_Toast( e.getMessage());

                    tested_keys = false ;
                    thread_back_mode = 0;
                }
            }
        });

        thread.start();
        try {
            thread.join ();
        } catch (Exception e) {
            Log.i ( "Interrup Config_KEYS" , e.getLocalizedMessage()) ;
        }

        return ( thread_back_mode ) ;
    }

    protected  void Show_Keys ( ) {
        TextView privateKey;
        TextView publicKey;
        TextView passPhrase;

        privateKey = findViewById(R.id.privateKEY);
        publicKey = findViewById(R.id.publicKEY);
        passPhrase = findViewById(R.id.PassPhrase);

        publicKey.setText(FullscreenActivity.Config_Data.Config_Keys.Kucoin_Public_Key);
        privateKey.setText(FullscreenActivity.Config_Data.Config_Keys.Kucoin_Private_Key);
        passPhrase.setText(FullscreenActivity.Config_Data.Config_Keys.Kucoin_passPhrase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Button QRPublic ;
        View Check ;
        Button Save ;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_config_kucoin_keys);

        getWindow().setNavigationBarColor(Color.BLACK);

        tested_keys = false ;

        ContextCompat.getColor(getApplicationContext(), android.R.color.background_dark) ;

        QRPublic = findViewById(R.id.scan_PubKeys_ID);
        QRPublic.setOnClickListener(
            new Button.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.i ( "Fragment config_keys" , "Scan Public Key for Kucoin ");

                IntentIntegrator scanIntegrator = new IntentIntegrator(Menu_Config_Kucoin_Keys.this);
                scanIntegrator.setPrompt("Scan Public Key");
                scanIntegrator.setBeepEnabled(true);
                //The following line if you want QR code
                scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                scanIntegrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.setBarcodeImageEnabled(true);

                startActivityForResult(scanIntegrator.createScanIntent(), 0x01 ) ;

            }
        });

//        Show_Keys(  ) ;



        QRPublic = findViewById(R.id.scan_PrKeys_ID);

        QRPublic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.i ( "Fragment config_keys" , "Scan Private Key for Kucoin");

                IntentIntegrator scanIntegrator = new IntentIntegrator(Menu_Config_Kucoin_Keys.this );
                scanIntegrator.setPrompt("Scan Private Key");
                scanIntegrator.setBeepEnabled(true);

                //The following line if you want QR code
                scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                scanIntegrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.setBarcodeImageEnabled(true);

                startActivityForResult(scanIntegrator.createScanIntent(), 0x02 ) ;

            }
        });

        Check = findViewById(R.id.check_Keys_ID);

        Check.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.i ( "Fragment config_keys" , "Checking keys");
                Test_Keys ( );
            }
        });
        Save = findViewById(R.id.save_keys_ID);
        Save.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.i ( "Fragment config_keys" , "Save keys");
                if (Save_Keys())
                {
                    FullscreenActivity.Load_Exchange_Keys();
                    onBackPressed();
                }
            }
        });
    }

    private void My_Toast ( String Msg )
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FullscreenActivity.My_Toast( Msg );
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK )
        {
            String scanContent = data.getStringExtra("SCAN_RESULT");

            TextView P_Key;

            if ( scanContent.contains("{\"apiKey\":"))
            {
                String Public  ;
                String Private ;


                JSONObject jObject ;

                try {
                    jObject = new JSONObject(scanContent);

                    Private     = jObject.getString("apiKey");
                    Public      = jObject.getString("secretKey");

                    if (Private!=null) {
                        P_Key = findViewById(R.id.privateKEY);
                        P_Key.setText(Public);
                    }

                    if (Public!=null) {
                        P_Key = findViewById(R.id.publicKEY);
                        P_Key.setText(Private);
                    }


                } catch (Exception e) {
                    Log.i("JSON Parser", "Error parsing data " + e.toString());
                }

            }
            else {
                if (requestCode == 0x02) {

                    P_Key = findViewById(R.id.privateKEY);
                    P_Key.setText(scanContent);
                } else {
                    P_Key = findViewById(R.id.publicKEY);
                    P_Key.setText(scanContent);
                }
            }

        }
    }
}