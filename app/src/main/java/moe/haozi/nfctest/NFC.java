package moe.haozi.nfctest;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class NFC extends AppCompatActivity {
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    private NfcAdapter mAdapter;
    private  TextView tv ;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        tv = ((TextView)findViewById(R.id.textView3));

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        //拦截系统级的NFC扫描，例如扫描蓝牙
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        ((Button) findViewById(R.id.exitNFC)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NFC.this, "返回主Activity", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    public void onPause() {
        super.onPause();
        //反注册 mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            readFromTag(getIntent());
        }
    }

    private boolean readFromTag(Intent intent){
//        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_TAG);
//        NdefMessage mNdefMsg = (NdefMessage)rawArray[0];
//        NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
//        Tag  tagMessage = (Tag)rawArray[0];
//        try {
//            if(mNdefRecord != null){
//                ((TextView)findViewById(R.id.textView3)).setText( new String(mNdefRecord.getPayload(),"UTF-8"));
//                return true;
//            }
//        }
//        catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        };

        ArrayList<String> sj = new ArrayList<String>();
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for(String tech :tagFromIntent.getTechList())
            sj.add(tech);
        boolean auth = false;

        MifareClassic mfc = MifareClassic.get(tagFromIntent);

        try{
            String metaInfo = "";
            mfc.connect();
            int type = mfc.getType();
            int sectorCount = mfc.getSectorCount();
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";

            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);

                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            tv.setText(metaInfo);

        }catch (Exception e){
            Toast.makeText(this,"boom",Toast.LENGTH_LONG);
        }


        return false;
    }
    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
    private int num = 0;
    private int[] hex = {16,256,4096,65565,1048576,16777216};

    byte[] getKey (){

        int[] key = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        int m = num;

        for(int i = 0;i<hex.length;i++) {
            key[i] = num % hex[i];

        }

        byte[] PREAMBLE_KEY = {(byte) key[0], (byte) key[1], (byte) key[2], (byte) key[3], (byte) key[4], (byte) key[5]};
        return PREAMBLE_KEY;
    }
}
