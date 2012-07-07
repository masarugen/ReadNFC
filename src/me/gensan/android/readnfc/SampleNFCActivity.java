package me.gensan.android.readnfc;

import me.gensan.android.samplenfc.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class SampleNFCActivity extends Activity {
	
	private static final String TAG = "SampleNFCActivity";

	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        setContentView(R.layout.activity_sample_nfc);
        // NFCかどうかActionの判定
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
        ||  NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
        ||  NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
        	Log.d(TAG, "NFC DISCOVERD:" + action);
            // IDmを表示させる
            String idm = getIdm(getIntent());
            if (idm != null) {
            	TextView idmView = (TextView) findViewById(R.id.idm);
            	idmView.setText(idm);
            }
        }
		mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
				new Intent(getApplicationContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sample_nfc, menu);
        return true;
    }

    @Override
	protected void onResume() {
		super.onResume();
		if (mNfcAdapter != null) {
			setNfcIntentFilter(this, mNfcAdapter, mPendingIntent);
		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mNfcAdapter != null) {
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
        String idm = getIdm(intent);
        if (idm != null) {
        	TextView idmView = (TextView) findViewById(R.id.idm);
        	idmView.setText(idm);
        }
	}

	/**
     * IDmを取得する
     * @param intent
     * @return
     */
	private String getIdm(Intent intent) {
		String idm = null;
		StringBuffer idmByte = new StringBuffer();
		byte[] rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
		if (rawIdm != null) {
			for (int i = 0; i < rawIdm.length; i++) {
				idmByte.append(Integer.toHexString(rawIdm[i] & 0xff));
			}
			idm = idmByte.toString();
		}
		return idm;
	}
	
	/**
	 * フォアグラウンドディスパッチシステムで、アプリ起動時には優先的にNFCのインテントを取得するように設定する
	 */
	private void setNfcIntentFilter(Activity activity, NfcAdapter nfcAdapter, PendingIntent seder) {
		// NDEF type指定
		IntentFilter typeNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			typeNdef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			e.printStackTrace();
		}
		// NDEF スキーマ(http)指定
		IntentFilter httpNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		httpNdef.addDataScheme("http");
		IntentFilter[] filters = new IntentFilter[] {
				typeNdef, httpNdef
		};
		// TECH指定
		String[][] techLists = new String[][] {
				new String[] { IsoDep.class.getName() },
				new String[] { NfcA.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcV.class.getName() },
				new String[] { Ndef.class.getName() },
				new String[] { NdefFormatable.class.getName() },
				new String[] { MifareClassic.class.getName() },
				new String[] { MifareUltralight.class.getName() }
			};
		nfcAdapter.enableForegroundDispatch(activity, seder, filters, techLists);
	}
}