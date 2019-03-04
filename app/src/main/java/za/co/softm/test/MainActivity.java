package za.co.softm.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Spinner privSp1;
    private TextView privTV;
    private Socket skt;
    private String sIP = "13.81.64.104";
    private int nPN = 30021;
    private String all_mtn = ",R2,R5,R15,R30,R60,R180";
    private String prm;
    private String msg0;
    private String msg1;
    private String msg2;
    private Thread thr;
    private Timer tmr1;

    private int Encaps2(byte[] b,int j)
    {	int i,n;
        for (i = j + 1; i > 1; --i)
        {	b[i] = b[i-2];	}
        n = j; b[1] = (byte)(n % 256);
        n = n / 256; b[0] = (byte)(n % 256);
        return (j+2);
    }

    private int TalkTo()
    {	int i,j0,j1;
        byte[] bb;
        byte[] buf0;
        byte[] buf1;
        msg1 = "";
        if (msg0.length() == 0) return 0;
        j0 = msg0.length();
        bb = msg0.getBytes();
        buf0 = new byte[1024];
        for (i = 0; i < j0; ++i) buf0[i] = bb[i];
        j0 = Encaps2(buf0,j0);
        try
        {	OutputStream nos = skt.getOutputStream();
            DataOutputStream dos = new DataOutputStream(nos);
            dos.write(buf0,0,j0);
            dos.flush();
            //----------
            InputStream nis = skt.getInputStream();
            DataInputStream dis = new DataInputStream(nis);
            buf1 = new byte[1024];
            j1 = dis.read(buf1,0,1024);
            if (j1 >= 2) msg1 = new String(buf1,2,j1-2);
            else msg1 = "No Response!";
        } catch (Exception ex) {msg1 = "No " + ex.toString();}
        i = msg1.length();
        return i;
    }

    public class ClientThread implements Runnable
    {	int j;
        String sg, tg;
        public void run()
        {
            if (prm.compareTo("") == 0) return;
            try {
                skt = new Socket(sIP,nPN);
                if (skt.isConnected()) {
                    msg0 = "Echo Hi";
                    j = TalkTo(); if (j == 0) { skt.close(); return; }
                    sg = msg1.substring(0, 2);
                    if (sg.compareTo("Ok") != 0) { skt.close(); return; }
                    /* * */
                    msg0 = "Echo Vend " + prm;
                    j = TalkTo(); if (j == 0) { skt.close(); return; }
                    sg = msg1.substring(0, 2);
                    if (sg.compareTo("Ok") != 0) { skt.close(); return; }
                    tg = msg1;
                    /* * */
                    msg0 = "Echo Bye";
                    j = TalkTo(); if (j == 0) { skt.close(); return; }
                    sg = msg1.substring(0, 2);
                    if (sg.compareTo("Ok") != 0) { skt.close(); return; }
                    /* * */
                    skt.close();
                    msg2 = tg;

                } else msg1 = "No Not connected";
            } catch (Exception ex) {
                msg1 = "No " + ex.toString();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* * * * * * * * * * * * * * * * * * * * * * * * */
        int i, j;
        String sg;
        privTV = (TextView) findViewById(R.id.textView);
        String[] part1 = all_mtn.split(",");
        List<String> list1 = new ArrayList<String>();
        j = part1.length;
        for (i = 0; i < j; ++i)
        {	sg = part1[i];
            list1.add(sg);
        }
        privSp1 = (Spinner) findViewById(R.id.mtn_Spinner);
        ArrayAdapter<String> dataAd1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list1);
        dataAd1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privSp1.setAdapter(dataAd1);

        privSp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                msg2 = "";
                prm = privSp1.getSelectedItem().toString();
                thr = new Thread(new ClientThread());
                thr.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {
                prm = "";
            }
        });
        /* * * * * * * * * * * * * * * * * */
        tmr1 = new Timer();
        tmr1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (msg2.compareTo("") != 0) {
                            privTV.setText(msg2); msg2 = "";
                        }
                    }
                });
            }
        },1500,500);
    }

}
