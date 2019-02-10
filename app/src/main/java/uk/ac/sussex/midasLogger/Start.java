/*
UNIVERSITY OF SUSSEX
Tactile Graphics Project 2014-19

Released under the MIT license.
If you found this code useful, please let us know.
tactilegraphics@sussex.ac.uk
 */

package uk.ac.sussex.midasLogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import uk.ac.sussex.midasLogger.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Start extends Activity {

    public static String DATA_FILE;
    private String DATA_DIR,ARCHIVE_DIR,SD_MOUNT;
    private Boolean SHOW_VIS,ADV_OPT,ARCH_LOGS,PERF_CAL;
    private Boolean AUTO_EXP,EMAIL_ZIP;
    private String EMAIL_RECIP;
    private String PART_ID,STIM_ID;
    private SharedPreferences pref;
    SimpleDateFormat filedate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if done properly, default values should be declared separately from the XML
        // ignore the error marker below for now
        PreferenceManager.setDefaultValues(this, R.layout.settings, false);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // GET DEFAULT PARTICIPANT
        PART_ID = pref.getString("pref_ps_partid", "default");
        STIM_ID = pref.getString("pref_ps_stimid", "default");

        //DB_SERVER = pref.getString("pref_db_server", "");
        //DB_USER = pref.getString("pref_db_user", "");
       // DB_PASS = pref.getString("pref_db_password", "");
        // file name end
        DATA_DIR = pref.getString("pref_es_savedir", "");
        ARCHIVE_DIR = pref.getString("pref_es_archivedir", "");
        SD_MOUNT = pref.getString("pref_es_sdmountpt", "");
        ARCH_LOGS = pref.getBoolean("pref_es_archivefiles", false);
        PERF_CAL = pref.getBoolean("pref_ps_calibration", false);

        ADV_OPT = pref.getBoolean("pref_ps_addDataOptions", false);

        DATA_FILE = "";
        //DATA_DIR = (getApplicationContext().getSharedPreferences("pref_es_SaveDir", 0)).toString();


        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // advanced options
        toggleAdvancedOptions();
        // archive logs
        if(ARCH_LOGS)
            archiveAllLogsButtonOnClick(findViewById(R.id.mainWindowView));

        // NOT IN USE
        //System.out.println("SD: "+checkSdCardPresent());

    }

    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        //System.out.println("Resumed");
        DATA_DIR = pref.getString("pref_es_savedir", "");
        ARCHIVE_DIR = pref.getString("pref_es_archivedir", "");
        SD_MOUNT = pref.getString("pref_es_sdmountpt", "");
        PERF_CAL = pref.getBoolean("pref_ps_calibration", false);
        toggleAdvancedOptions();
    }

    private void toggleAdvancedOptions() {
        ADV_OPT = pref.getBoolean("pref_ps_addDataOptions", false);
        if(!ADV_OPT) {
            findViewById (R.id.button2).setVisibility(View.GONE);
            findViewById (R.id.button3).setVisibility(View.GONE);
            findViewById (R.id.button4).setVisibility(View.GONE);
            findViewById (R.id.button6).setVisibility(View.GONE);
            findViewById (R.id.button7).setVisibility(View.GONE);

        }
        else {
            findViewById (R.id.button2).setVisibility(View.VISIBLE);
            findViewById (R.id.button3).setVisibility(View.VISIBLE);
            findViewById (R.id.button4).setVisibility(View.VISIBLE);
            findViewById (R.id.button6).setVisibility(View.VISIBLE);
            findViewById (R.id.button7).setVisibility(View.VISIBLE);
        }
    }

    // NOT IN USE
    public Boolean checkSdCardPresent() {
        try {
            FileInputStream fs = new FileInputStream("/proc/mounts");
            DataInputStream in = new DataInputStream(fs);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String s;
            while ((s = br.readLine()) != null)   {
                if(s.contains(SD_MOUNT))
                    return true;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void gatherRawData(View v) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.enter_part_id, null);
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        popupBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);
        userInput.setText(PART_ID);
        final EditText stimInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogStimInput);
        stimInput.setText(STIM_ID);
        stimInput.selectAll();
        userInput.selectAll();
        popupBuilder
                    .setCancelable(false)
                    .setPositiveButton("Start Experiment",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                        PART_ID = userInput.getText().toString();
                                        STIM_ID = stimInput.getText().toString();
                                    if(PERF_CAL)
                                        setContentView(R.layout.start_calibration);
                                    else
                                        setContentView(R.layout.start_experiment);


                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

            AlertDialog enterPartIdPopup = popupBuilder.create();
            enterPartIdPopup.show();
    }

    public void startExperiment(View v) {
        clearMemory();
        // set file name: the date in the file name is always the start date of the experiment!
        Date now = new Date();
        filedate = new SimpleDateFormat ("yyyy-MM-dd - HH-mm-ss");
        DATA_FILE = filedate.format(now)+" - "+ PART_ID +" - "+ STIM_ID + " - midasLogger Data.txt";
        Intent myIntent = new Intent(Start.this, GatherData.class);
        //Start.this.startActivity(myIntent);
        startActivity(myIntent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem i) {

        switch (i.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, Settings.class));
                return true;
            case R.id.help:
                // maybe open browser to show help manual / web page?
                //startActivity(new Intent(this, Help.class));
                return true;
            case R.id.about:
                showAbout();
                //startActivity(new Intent(this, About.class));
                return true;

            default:
                return super.onOptionsItemSelected(i);
        }

    }

    public void readDatafile(View v) throws FileNotFoundException {

        String FILE_NAME = DATA_FILE;
        String line;
        TextView tv = new TextView(Start.this);
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(this.getApplicationContext().openFileInput(FILE_NAME));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
           // line = bufferedReader.readLine();
           // if (line == null) return;
           // else
           //     System.out.println(line);

            while ((line = bufferedReader.readLine()) != null)
                    tv.append(line + "\n");


            tv.setMovementMethod(ScrollingMovementMethod.getInstance());
            setContentView(tv);
            inputStreamReader.close();
            bufferedReader.close();

        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), "No Data Log Found", Toast.LENGTH_SHORT).show();
        }

    }

    public void exportRawdata(View v) throws Exception {

        File src = new File(this.getApplicationContext().getFilesDir(), DATA_FILE);
        File dstdir = new File(Environment.getExternalStorageDirectory(), DATA_DIR);

        if (!dstdir.exists())
            dstdir.mkdirs();

        File dst = new File(Environment.getExternalStorageDirectory()+"/"+DATA_DIR, DATA_FILE);
        FileChannel inChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
        } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            Toast.makeText(getApplicationContext(), "File '"+ DATA_FILE +"' created", Toast.LENGTH_SHORT).show();
        }
        catch (NullPointerException e) {}
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

    }

    public void clearMemory() {

        File file = new File(this.getApplicationContext().getFilesDir(), DATA_FILE);
        Boolean deleted = file.delete();
    }

    // SD Copy function disabled as no longer supported in Android >4.x
/*
    public void copyLogsToSDButtonOnClick(View v) {

        // THIS IS BUGGY DUE TO A NEW RESTRICTION IN KITKAT

        if(checkSdCardPresent()) {
            boolean success = false;
            File fileDir = new File(Environment.getExternalStorageDirectory()+"/"+ARCHIVE_DIR);
            String[] flist = fileDir.list();
            int count = 0;
            for (String fileName : flist) {
                File src = new File(Environment.getExternalStorageDirectory()+"/"+ARCHIVE_DIR+"/"+fileName);
                File dst = new File("/mnt/extSdCard/Android/data/com.ac.ronaldgrau.midasLogger/"+fileName);
                System.out.println("Copy '"+src.toString()+"' to '"+dst.toString()+"'");
                try {
                    copyFile(src,dst);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
                //}
            }
            if(success)
                Toast.makeText(getApplicationContext(), "Data Logs Copied to SD-Card", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "No Data Logs Found or Operation Failed", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "No SD-Card present", Toast.LENGTH_SHORT).show();

        // if(deleted)
        //    Toast.makeText(getApplicationContext(), "Memory cleared", Toast.LENGTH_SHORT).show();
        // else
        //    Toast.makeText(getApplicationContext(), "Memory ready", Toast.LENGTH_SHORT).show();

    }

    public Boolean copyFile(File src, File dst) throws Exception {
        dst.mkdirs();
        FileChannel inChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "No Data to Copy", Toast.LENGTH_SHORT).show();
            return false;
        }
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cannot copy to destination", Toast.LENGTH_SHORT).show();
            return false;
        }
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Copying Failed", Toast.LENGTH_SHORT).show();
            return false;
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
        return true;
    }

*/
    public void sendDataButtonOnClick(View v) {

        EMAIL_RECIP = pref.getString("pref_email_recipient", "");
        EMAIL_ZIP = pref.getBoolean("pref_email_zipfiles", false);

        File fileSend;
        File file = new File(Environment.getExternalStorageDirectory()+"/"+DATA_DIR, DATA_FILE);
        String filepath = file.getPath(); // = (this.getApplicationContext().getFilesDir()).toString()+DATA_FILE;
        //System.out.println("Path:"+filepath);

        if(DATA_FILE != "") {

            if (EMAIL_ZIP) {
                String[] flist = new String[1];
                flist[0] = filepath;
                String zipfile = filepath + ".zip";
                try {
                    zip(flist, zipfile);
                } catch (IOException e) {
                    //e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "No Data Log Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileSend = new File(Environment.getExternalStorageDirectory() + "/" + DATA_DIR, DATA_FILE + ".zip");
            } else
                fileSend = file;

            Intent sendIntent = new Intent(Intent.ACTION_SEND);

            //sendIntent.setType("plain/text");
            sendIntent.setType("message/rfc822");
            sendIntent.setData(Uri.parse(EMAIL_RECIP));
            sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "See data attached.");

            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileSend));
// the mail subject
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "midasLogger Experiment Data");
            startActivity(sendIntent);
//        startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }
        else
            Toast.makeText(getApplicationContext(), "No Data Log Found", Toast.LENGTH_SHORT).show();
    }

    public void sendAllDataButtonOnClick(View v) {

        EMAIL_RECIP = pref.getString("pref_email_recipient", "");
        EMAIL_ZIP = pref.getBoolean("pref_email_zipfiles", false);

        File fileSend;

        Date now = new Date();
        filedate = new SimpleDateFormat ("yyyy-MM-dd - HH-mm-ss");
        //File file = new File(this.getApplicationContext().getFilesDir(), DATA_FILE);
        File fileDir = new File(Environment.getExternalStorageDirectory()+"/"+DATA_DIR);
        String filepath = fileDir.getPath(); // = (this.getApplicationContext().getFilesDir()).toString()+DATA_FILE;
        //System.out.println("Path:"+filepath);

        String[] flist = fileDir.list();

        if(flist.length == 0) {
            Toast.makeText(getApplicationContext(), "No Data Logs Found", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            String[] flistOut = new String[flist.length];
            int count = 0;
            for (String fileName : flist) {
                //if (fileName.toLowerCase().endsWith(".txt") || fileName.toLowerCase().endsWith(".zip")) {
                //System.out.println(filepath + "/" + fileName);
                flistOut[count] = filepath + "/" + fileName;
                count++;
                //}
            }

            //System.out.println("Files: " + flistOut.length);

            String zipfile = filedate.format(now) + " - midasLogger Data (Multiple).txt.zip";
            try {
                zip(flistOut, filepath + "/" + zipfile);
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }

            fileSend = new File(Environment.getExternalStorageDirectory() + "/" + DATA_DIR, zipfile);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("message/rfc822");
            sendIntent.setData(Uri.parse(EMAIL_RECIP));
            sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

            sendIntent.putExtra(Intent.EXTRA_TEXT, "See data attached.");

            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileSend));
// the mail subject
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "midasLogger Experiment Data");
            //startActivity(Intent.createChooser(sendIntent, "Choose an Email client :"));
            startActivity(sendIntent);
        }
    }

    public void archiveAllLogsButtonOnClick(View v) {
        boolean success = false;
        String filestring = "";
        File archdir = new File(Environment.getExternalStorageDirectory()+"/"+ARCHIVE_DIR);
        if (!archdir.exists())
                archdir.mkdirs();

        File fileDir = new File(Environment.getExternalStorageDirectory()+"/"+DATA_DIR);
        if (!fileDir.exists())
            fileDir.mkdirs();

        String[] flist = fileDir.list();
        int count = 0;
        for (String fileName : flist) {
            File temp = new File(Environment.getExternalStorageDirectory()+"/"+DATA_DIR+"/"+fileName);
            File arch = new File(Environment.getExternalStorageDirectory()+"/"+ARCHIVE_DIR+"/"+fileName);
            success = temp.renameTo(arch);
            //System.out.println("File: " + Environment.getExternalStorageDirectory()+"/"+ARCHIVE_DIR+"/"+fileName);
            count++;
            //}
        }
        if(count == 1)
            filestring = " File";
        if(count > 1)
            filestring = " Files";
        if(success)
            Toast.makeText(getApplicationContext(), count+filestring+" Archived", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Archiving: No Data Logs Found", Toast.LENGTH_SHORT).show();
    }

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        int BUFFER_SIZE = 1000;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }

    protected void showAbout() {
        // Inflate message
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        // When linking text, force to always use the default color. This works
        // around a pressed color state bug
        TextView textView = (TextView) messageView.findViewById(R.id.about_text);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    public void quitMTL(View v) {
        finish();
        System.exit(0);

    }

    public void onBackPressed()
    {
        // on returning from the start experiment button
        setContentView(R.layout.activity_main);
        toggleAdvancedOptions();
    }

}

