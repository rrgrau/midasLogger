/*
UNIVERSITY OF SUSSEX
Tactile Graphics Project 2014-19

Released under the MIT license.
If you found this code useful, please let us know.
tactilegraphics@sussex.ac.uk
 */

package uk.ac.sussex.midasLogger;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
//import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TouchView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "MTView";

    private static final int MAX_TOUCHPOINTS = 11;
    private static final String START_TEXT = "Ready";
    private Boolean SHOW_VIS,PERF_CAL;

    private static Paint textPaint = new Paint();
    private static Paint touchPaints[] = new Paint[MAX_TOUCHPOINTS];
    private static int colors[] = new int[MAX_TOUCHPOINTS];
    private static int[][] histCoords = new int[10][2];
    private static int histBlocks;
    private static int eventcount, action, x, y, id, t, i, zerotime;

    private static int width, height; //, eventcount;
    private static float scale = 1.0f;
    private static Context context;
    private ToneGenerator tg;
    private Canvas c;


    public TouchView(Context context) {
        super(context);
        this.context = context;

        PERF_CAL = GatherData.PERF_CAL;
        SHOW_VIS = GatherData.SHOW_VIS;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setFocusable(true); // make sure we get key events
        setFocusableInTouchMode(true); // make sure we get touch events

        init();
    }

    private void storeDataLine(int i, String data) {
        GatherData.addData(i, data);
    }



    private void init() {
        textPaint.setColor(Color.WHITE);
        colors[0] = Color.BLUE;
        colors[1] = Color.RED;
        colors[2] = Color.GREEN;
        colors[3] = Color.YELLOW;
        colors[4] = Color.CYAN;
        colors[5] = Color.MAGENTA;
        colors[6] = Color.DKGRAY;
        colors[7] = Color.WHITE;
        colors[8] = Color.LTGRAY;
        colors[9] = Color.GRAY;
        colors[10] = Color.rgb(255,100,100);
        for (int i = 0; i < MAX_TOUCHPOINTS; i++) {
            touchPaints[i] = new Paint();
            touchPaints[i].setColor(colors[i]);
        }
        eventcount = 0;
        zerotime=0;
        histBlocks = 0;
        x = y = 100; // must not be 0 to start to be suff. different from first coord.

        tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        if(PERF_CAL) {
            GatherData.addData(eventcount,"*** Calibration Data\n");
        }
    }

    private int getIndex(MotionEvent event) {

        int idx = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        return idx;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerCount = event.getPointerCount();
        if (pointerCount > MAX_TOUCHPOINTS) {
            pointerCount = MAX_TOUCHPOINTS;
            return false;
        }
        Canvas c = null;

        if(SHOW_VIS) {
            try {
                c = getHolder().lockCanvas();
            } catch (IllegalStateException e) {
                return false;//e.printStackTrace();
            }
        }

        if (c != null || !SHOW_VIS) {

            action = event.getActionMasked();
            t = (int) event.getEventTime();
            if (zerotime == 0) zerotime = t;
            t = t - zerotime;

            if(SHOW_VIS) {
                c.drawColor(Color.BLACK);
                for (int i = 0; i < pointerCount; i++) {
                    id = event.getPointerId(i);
                    x = (int) event.getX(i);
                    y = (int) event.getY(i);
                    //System.out.println("Pressure for ID "+i+" : "+event.getPressure(i));
                    //System.out.println("Size for ID "+i+" : "+event.getSize(i));
                    // writeData(action, t, x, y, id);
                    drawCircle(x, y, touchPaints[id], c);

                }
            }

            // calibration: 4 single finger presses
            if(PERF_CAL && eventcount <= 4) {
                if (action == MotionEvent.ACTION_DOWN) {
                    //if(Math.abs(x - event.getX(i)) > 20 && Math.abs(y - event.getY(i)) > 20) {
                        i = getIndex(event);
                        x = (int) event.getX(i);
                        y = (int) event.getY(i);
                        writeData("*", t, x, y, 0);
                    //}

                    if (eventcount < 4) {
                        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 100);
                    }
                    else if(eventcount == 4){
                        GatherData.addData(++eventcount,"***\n");
                        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 400);
                    }
                }
            }
            else {

                //System.out.println(event.getAction());
                // if we had 3 or more 6s in a row, then there's likely been a sensor block
                if(event.getAction() ==  6 && histBlocks > 1) {
                    try {
                        //ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                        tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 200);
                    }
                    catch (Exception e){
                        ; //throw new RuntimeException(e);
                    }

                    //System.out.println("Sensor blocked");
                    writeData("-1", t, 0, 0, 0);
                }

                    if(event.getAction() ==  6)
                        histBlocks++;
                    else
                        histBlocks = 0;

                switch(action & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN : {

                        id = event.getPointerId(0);
                        x = (int) event.getX(0);
                        y = (int) event.getY(0);

                        writeData("1", t, x, y, id);
                        break;

                    }

                    case MotionEvent.ACTION_POINTER_DOWN : {

                        id = event.getPointerId(getIndex(event));
                        i = getIndex(event);
                        x = (int) event.getX(i);
                        y = (int) event.getY(i);
                        writeData("1", t, x, y, id);

                        break;

                    }

                    case MotionEvent.ACTION_POINTER_UP : {

                        id = event.getPointerId(getIndex(event));
                        i = getIndex(event);
                        x = (int) event.getX(i);
                        y = (int) event.getY(i);
                        writeData("0", t, x, y, id);
                        break;

                    }

                    case MotionEvent.ACTION_MOVE : {

                        //cycle through pointers and gather coords
                        for (int j = 0; j < pointerCount; j++) {
                            id = event.getPointerId(j);
                            //int i = getIndex(event);
                            x = (int) event.getX(j);
                            y = (int) event.getY(j);

                            // if pointer has not moved, do not record event
                            if (histCoords[j][0] != x && histCoords[j][1] != y) {
                                //System.out.println("Write data for ID"+j);
                                // write historical values from the event batch, if there are any
                                writeData("2", t, x, y, id);
                            }

                            // record coords for history
                            histCoords[j][0] = x;
                            histCoords[j][1] = y;

                            // record possible blocking moves

                        }

                        break;

                    }

                    case MotionEvent.ACTION_UP : {

                        id = event.getPointerId(0);
                        x = (int) event.getX(0);
                        y = (int) event.getY(0);
                        writeData("0", t, x, y, id);

                        break;

                    }

                    case MotionEvent.ACTION_CANCEL : {

                        //ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                        tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 200);
                        System.out.println("Strange input signal 1");
                        writeData("-1", t, x, y, id);
                        break;

                    }
                    case (MotionEvent.ACTION_OUTSIDE) : {
                        //ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                        tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 200);
                        System.out.println("Outside bounds");
                        break;
                    }
                }
            }

            if(SHOW_VIS)
                getHolder().unlockCanvasAndPost(c);
        }
        else
            System.out.println("not processing touch");
        return true; //super.onTouchEvent(event); doesn't work
    }

    protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if(!gainFocus)
            this.findFocus();

    }

    private void drawCircle(int x, int y, Paint paint, Canvas c) {
       c.drawCircle(x, y, 30 * scale, paint);
    }

    private void writeData(String a, int t, int x, int y, int id) {
        eventcount++;
        storeDataLine(eventcount, id + "," + x + "," + y + "," + t + "," + a + "\n");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //this.width = width;
        //this.height = height;
        //if (width > height) {
        //    this.scale = width / 480f;
        // } else {
        //    this.scale = height / 480f;
        //}
        textPaint.setTextSize(14 * scale);
        Canvas c = getHolder().lockCanvas();
        if (c != null) {
// clear screen
            c.drawColor(Color.BLACK);
            float tWidth = textPaint.measureText(START_TEXT);
            c.drawText(START_TEXT, width / 2 - tWidth / 2, height / 2, textPaint);
            getHolder().unlockCanvasAndPost(c);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }


}