/*
UNIVERSITY OF SUSSEX
Tactile Graphics Project 2014-19

Released under the MIT license.
If you found this code useful, please let us know.
tactilegraphics@sussex.ac.uk
 */

package uk.ac.sussex.midasLogger;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

public class GatherData extends Activity {
    /** Called when the activity is first created. */

    private TouchView gatherData;
    //ViewGroup vg;
    private static final String TAG = "GatherData";
    private OutputStreamWriter outputStreamWriter;
    private String FILE_NAME,DATA_DIR;
    private Boolean AUTO_EXP;
    public static Boolean SHOW_VIS,PERF_CAL;
    private ToneGenerator tg;
    private static String[] data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FILE_NAME = Start.DATA_FILE;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DATA_DIR = pref.getString("pref_es_savedir", "");
        AUTO_EXP = pref.getBoolean("pref_es_autoexport", false);
        SHOW_VIS = pref.getBoolean("pref_ps_visualiseoutput", false);
        PERF_CAL = pref.getBoolean("pref_ps_calibration", false);

        tg = new ToneGenerator(AudioManager.STREAM_ALARM, 200);

        data = new String[10000000];
        gatherData = new TouchView(this);
        setContentView(gatherData);
    }

    public void requestDisallowInterceptTouchEvent() {

    }

    public static void addData(int i, String s) {
        data[i] = s;
        //  System.out.println("Saving " + i + ": " + data[i]);
    }

    public boolean dispatchTouchEvent() {
        System.out.println("Dispatch fired");
        return false;
    }

    @Override
    public void onBackPressed()
    {
        tg.startTone(ToneGenerator.TONE_SUP_BUSY, 1000);
        try {
           outputStreamWriter = new OutputStreamWriter(this.getApplicationContext().openFileOutput(FILE_NAME, this.getApplicationContext().MODE_APPEND));
            for (int i = 0; i < data.length; i++) {

               if (data[i] != null) {
                   outputStreamWriter.write(data[i]);
                   //System.out.println("Writing " + i + ": " + data[i]);
               }
            }

           outputStreamWriter.flush();
           outputStreamWriter.close();
           //System.out.println("Written to:" + this.getApplicationContext().getFilesDir());

        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }

        //export to external memory
        if(AUTO_EXP) {
            try {
                File src = new File(this.getApplicationContext().getFilesDir(), FILE_NAME);
                File dstdir = new File(Environment.getExternalStorageDirectory(), DATA_DIR);
                dstdir.mkdirs();
                // System.out.println("Create Dir: " + DATA_DIR);
                File dst = new File(Environment.getExternalStorageDirectory()+"/"+DATA_DIR, FILE_NAME);
                FileChannel inChannel = null;
                try {
                    inChannel = new FileInputStream(src).getChannel();
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                }
                FileChannel outChannel = new FileOutputStream(dst).getChannel();
                try
                {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    Toast.makeText(getApplicationContext(), "File '"+ FILE_NAME +"' created", Toast.LENGTH_SHORT).show();
                }
                catch (NullPointerException e) {}
                finally
                {
                    if (inChannel != null)
                        inChannel.close();
                    if (outChannel != null)
                        outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.finish();
    }
}