package com.example.tn6g10.mybouncingball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Scanner;

/*
 * CS 193A, Winter 2015, Marty Stepp
 * This class is a graphical view of a simple animated app
 * with a ball that moves around and bounces off the edges of the screen,
 * as well as a yellow "pac-man" sprite that can move around in response
 * to the user touching the screen at its various edges.
 */


public class FootBallView extends View {
    private static final float BALL_SIZE = 30;
    private static final float PLAYER_SIZE = 60;
    private static final float BALL_MAX_VELOCITY = 80;

    private MovingObject ball;
    //private MovingObject ref;
    private DrawingThread dthread;

    private ArrayList<MovingObject> players;// = new ArrayList<MovingObject>();

    private String txt = "";

    // Hold data for active touch pointer IDs
    private SparseArray<TouchHistory> mTouches;

    // Is there an active touch?
    private boolean mHasTouch = false;

    /**
     * Holds data related to a touch pointer, including its current position,
     * pressure and historical positions. Objects are allocated through an
     * object pool using {@link #obtain()} and {@link #recycle()} to reuse
     * existing objects.
     */
    static final class TouchHistory {

        // number of historical points to store
        public static final int HISTORY_COUNT = 200;

        public float x;
        public float y;
        public float pressure = 0f;
        public String label = null;
        public int playerIdx = -1;

        // current position in history array
        public int historyIndex = 0;
        public int historyCount = 0;

        // arrray of pointer position history
        public PointF[] history = new PointF[HISTORY_COUNT];

        private static final int MAX_POOL_SIZE = 10;
        private static final Pools.SimplePool<TouchHistory> sPool =
                new Pools.SimplePool<TouchHistory>(MAX_POOL_SIZE);

        public static TouchHistory obtain(float x, float y, float pressure) {
            TouchHistory data = sPool.acquire();
            if (data == null) {
                data = new TouchHistory();
            }

            data.setTouch(x, y, pressure);

            return data;
        }

        public TouchHistory() {

            // initialise history array
            for (int i = 0; i < HISTORY_COUNT; i++) {
                history[i] = new PointF();
            }
        }

        public void setTouch(float x, float y, float pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }

        public void recycle() {
            this.historyIndex = 0;
            this.historyCount = 0;
            sPool.release(this);
        }

        /**
         * Add a point to its history. Overwrites oldest point if the maximum
         * number of historical points is already stored.
         *
         * @param point
         */
        public void addHistory(float x, float y) {
            PointF p = history[historyIndex];
            p.x = x;
            p.y = y;

            historyIndex = (historyIndex + 1) % history.length;

            if (historyCount < HISTORY_COUNT) {
                historyCount++;
            }
        }

    }

    /*
     * This constructor sets up the initial state of the view and sprites.
     */
    public FootBallView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // SparseArray for touch events, indexed by touch id
        mTouches = new SparseArray<TouchHistory>(10);
        players = new ArrayList<MovingObject>();

        initialisePaint();

        // set up initial state of ball
        ball = new MovingObject();
        ball.setLocation(200, 200);
        ball.setSize(BALL_SIZE, BALL_SIZE);
        //ball.setVelocity(
        //        (float) ((Math.random() - .5) * 2 * BALL_MAX_VELOCITY),
        //        (float) ((Math.random() - .5) * 2 * BALL_MAX_VELOCITY)
        //);
        ball.paint.setARGB(255, 0, 0, 255);

        /*
        // set up initial state of pac-man
        pacman = new MovingObject();
        pacman.setLocation(0, 0);
        pacman.setSize(80, 80);
        pacman.paint.setARGB(255, 200, 200, 0);
        */
       /*
        Scanner scan = new Scanner(
                getResources().openRawResource(R..player_locs));
        String allText = ""; // read entire file
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            allText += line;
        }
        myTextView.setText(allText);
        scan.close();
        */

        for (int i = 0; i < 22; i++) {
            // set up initial state of pac-man
            MovingObject player = new MovingObject();
            player.setSize(PLAYER_SIZE, PLAYER_SIZE);
            player.setDpf(1);
            if (i < 11) {
                player.setLocation(50 + 50 * i, 50 + 50 * i);
                player.paint.setARGB(255, 200, 200, 0);
                player.setId(""+(i+1));
            }
            else {
                player.setLocation(50 * i - 500, 0 + 50 * i);
                player.paint.setARGB(255, 200, 0, 0);
                player.setId(""+(i-10));
            }

            players.add(player);
        }

        // start a drawing thread to animate screen at 50 frames/sec
        dthread = new DrawingThread(this, 50);
        dthread.start();
    }

    /*
     * Called when the user touches the screen with their finger.
     * Used to control Pac-Man's movement.
     * If the user touches the edges of the screen, moves Pac-Man toward that edge.
     */
    /*
    @Override

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int w = getWidth();
        int h = getHeight();

        if (x < w/5) {
            pacman.dx = -10;              // left edge of screen
        } else if (x >= w*4/5) {
            pacman.dx = 10;               // right edge
        } else {
            pacman.dx = 0;                // center
        }
        if (y < h/5) {
            pacman.dy = -10;              // top edge
        } else if (y >= h*4/5) {
            pacman.dy = 10;               // bottom edge
        } else {
            pacman.dy = 0;                // center
        }

        txt = "("+x+","+y+")";

        return super.onTouchEvent(event);
    }

     */

    public int getObjectAtLocation(float x, float y){
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isInObject(x, y)) {
                return i;
            }
        }
        return -1;
    }


    // BEGIN_INCLUDE(onTouchEvent)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();

        /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK. Alternatively a call to
         * event.getActionMasked() would yield in the action as well.
         */
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {
                // first pressed gesture has started
                int i = getObjectAtLocation(event.getX(0), event.getY(0));
                if (i >= 0) {
                    players.get(i).setBeingControlled(true);
                }
                    /*
                     * Only one touch event is stored in the MotionEvent. Extract
                     * the pointer identifier of this touch from the first index
                     * within the MotionEvent object.
                     */
                    int id = event.getPointerId(0);

                    TouchHistory data = TouchHistory.obtain(event.getX(0), event.getY(0),
                            event.getPressure(0));
                    data.label = "id: " + 0;
                    data.playerIdx = i;

                    /*
                     * Store the data under its pointer identifier. The pointer
                     * number stays consistent for the duration of a gesture,
                     * accounting for other pointers going up or down.
                     */
                    mTouches.put(id, data);

                    mHasTouch = true;
                //}

                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                /*
                 * A non-primary pointer has gone down, after an event for the
                 * primary pointer (ACTION_DOWN) has already been received.
                 */

                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */


                int index = event.getActionIndex();
                int id = event.getPointerId(index);

                TouchHistory data = TouchHistory.obtain(event.getX(index), event.getY(index),
                        event.getPressure(index));

                int i = getObjectAtLocation(event.getX(index), event.getY(index));
                if (i >= 0) {
                    players.get(i).setBeingControlled(true);
                }

                data.label = "id: " + id;
                data.playerIdx = i;
                /*
                 * Store the data under its pointer identifier. The index of
                 * this pointer can change over multiple events, but this
                 * pointer is always identified by the same identifier for this
                 * active gesture.
                 */
                mTouches.put(id, data);

                break;
            }

            case MotionEvent.ACTION_UP: {
                /*
                 * Final pointer has gone up and has ended the last pressed
                 * gesture.
                 */

                /*
                 * Extract the pointer identifier for the only event stored in
                 * the MotionEvent object and remove it from the list of active
                 * touches.
                 */
                int id = event.getPointerId(0);
                TouchHistory data = mTouches.get(id);
                if (data.playerIdx >=0) {
                    players.get(data.playerIdx).setBeingControlled(false);
                    data.playerIdx = -1;
                }

                mTouches.remove(id);
                data.recycle();

                mHasTouch = false;

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                /*
                 * A non-primary pointer has gone up and other pointers are
                 * still active.
                 */

                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */
                int index = event.getActionIndex();
                int id = event.getPointerId(index);

                TouchHistory data = mTouches.get(id);
                if (data.playerIdx >=0) {
                    players.get(data.playerIdx).setBeingControlled(false);
                    data.playerIdx = -1;
                }
                mTouches.remove(id);
                data.recycle();

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                /*
                 * A change event happened during a pressed gesture. (Between
                 * ACTION_DOWN and ACTION_UP or ACTION_POINTER_DOWN and
                 * ACTION_POINTER_UP)
                 */
                //if (pacman.getBeingControlled())
                  //  pacman.addToTrajectory(event.getX(0), event.getY(0));
                /*
                 * Loop through all active pointers contained within this event.
                 * Data for each pointer is stored in a MotionEvent at an index
                 * (starting from 0 up to the number of active pointers). This
                 * loop goes through each of these active pointers, extracts its
                 * data (position and pressure) and updates its stored data. A
                 * pointer is identified by its pointer number which stays
                 * constant across touch events as long as it remains active.
                 * This identifier is used to keep track of a pointer across
                 * events.
                 */
                for (int index = 0; index < event.getPointerCount(); index++) {
                    // get pointer id for data stored at this index
                    int id = event.getPointerId(index);

                    // get the data stored externally about this pointer.
                    TouchHistory data = mTouches.get(id);

                    // add previous position to history and add new values
                    data.addHistory(data.x, data.y);
                    data.setTouch(event.getX(index), event.getY(index),
                            event.getPressure(index));


                    if (data.playerIdx >= 0)
                        if (players.get(data.playerIdx).getBeingControlled())
                            players.get(data.playerIdx).addToTrajectory(event.getX(index), event.getY(index));

                }

                break;
            }
        }

        // trigger redraw on UI thread
        this.postInvalidate();

        return true;
    }

    /*
     * This method draws the bouncing ball and Pac-Man on the screen,
     * and also updates their positions for the next time the screen
     * is redrawn.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawOval(ball.rect, ball.paint);
        //canvas.drawOval(pacman.rect, pacman.paint);
        Paint paint = new Paint();
        paint.setARGB(255,125,125,125);
        paint.setTextSize(30);
        for (int i = 0; i < players.size(); i++) {
            canvas.drawOval(players.get(i).rect, players.get(i).paint);
            canvas.drawText(players.get(i).getId(),players.get(i).rect.centerX(),
                    players.get(i).rect.centerY(),paint);
        }

        //int w = getWidth();
        //int h = getHeight();
        //canvas.drawText(txt, w/2, h/2, ball.paint);

        updateSprites();

/*
        // Canvas background color depends on whether there is an active touch
        if (mHasTouch) {
            canvas.drawColor(BACKGROUND_ACTIVE);
        } else {
            // draw inactive border
            canvas.drawRect(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth, getHeight()
                    - mBorderWidth, mBorderPaint);
        }

        // loop through all active touches and draw them
        for (int i = 0; i < mTouches.size(); i++) {

            // get the pointer id and associated data for this index
            int id = mTouches.keyAt(i);
            TouchHistory data = mTouches.valueAt(i);

            // draw the data and its history to the canvas
            drawCircle(canvas, id, data);
        }
        */

    }

    // updates sprites' positions between frames of animation
    private void updateSprites() {
        //pacman.moveAlongTrajectory();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).moveAlongTrajectory();
        }
        //ball.move();

        // handle ball bouncing off edges
        //if (ball.rect.left < 0 || ball.rect.right >= getWidth()) {
        //    ball.dx = -ball.dx;
        //}
        //if (ball.rect.top < 0 || ball.rect.bottom >= getHeight()) {
        //    ball.dy = -ball.dy;
        //}
    }


    /*
     * Below are only helper methods and variables required for drawing.
     */

    // radius of active touch circle in dp
    private static final float CIRCLE_RADIUS_DP = 75f;
    // radius of historical circle in dp
    private static final float CIRCLE_HISTORICAL_RADIUS_DP = 7f;

    // calculated radiuses in px
    private float mCircleRadius;
    private float mCircleHistoricalRadius;

    private Paint mCirclePaint = new Paint();
    private Paint mTextPaint = new Paint();

    private static final int BACKGROUND_ACTIVE = Color.WHITE;

    // inactive border
    private static final float INACTIVE_BORDER_DP = 15f;
    private static final int INACTIVE_BORDER_COLOR = 0xFFffd060;
    private Paint mBorderPaint = new Paint();
    private float mBorderWidth;

    public final int[] COLORS = {
            0xFF33B5E5, 0xFFAA66CC, 0xFF99CC00, 0xFFFFBB33, 0xFFFF4444,
            0xFF0099CC, 0xFF9933CC, 0xFF669900, 0xFFFF8800, 0xFFCC0000
    };

    /**
     * Sets up the required {@link Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {

        // Calculate radiuses in px from dp based on screen density
        float density = getResources().getDisplayMetrics().density;
        mCircleRadius = CIRCLE_RADIUS_DP * density;
        mCircleHistoricalRadius = CIRCLE_HISTORICAL_RADIUS_DP * density;

        // Setup text paint for circle label
        mTextPaint.setTextSize(27f);
        mTextPaint.setColor(Color.BLACK);

        // Setup paint for inactive border
        mBorderWidth = INACTIVE_BORDER_DP * density;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(INACTIVE_BORDER_COLOR);
        mBorderPaint.setStyle(Paint.Style.STROKE);

    }

    /**
     * Draws the data encapsulated by a {@link TouchDisplayView.TouchHistory} object to a canvas.
     * A large circle indicates the current position held by the
     * {@link TouchDisplayView.TouchHistory} object, while a smaller circle is drawn for each
     * entry in its history. The size of the large circle is scaled depending on
     * its pressure, clamped to a maximum of <code>1.0</code>.
     *
     * @param canvas
     * @param id
     * @param data
     */
    protected void drawCircle(Canvas canvas, int id, TouchHistory data) {
        // select the color based on the id
        int color = COLORS[id % COLORS.length];
        mCirclePaint.setColor(color);

        /*
         * Draw the circle, size scaled to its pressure. Pressure is clamped to
         * 1.0 max to ensure proper drawing. (Reported pressure values can
         * exceed 1.0, depending on the calibration of the touch screen).
         */
        float pressure = Math.min(data.pressure, 1f);
        float radius = pressure * mCircleRadius;

        canvas.drawCircle(data.x, (data.y) - (radius / 2f), radius,
                mCirclePaint);

        // draw all historical points with a lower alpha value
        mCirclePaint.setAlpha(125);
        for (int j = 0; j < data.history.length && j < data.historyCount; j++) {
            PointF p = data.history[j];
            canvas.drawCircle(p.x, p.y, mCircleHistoricalRadius, mCirclePaint);
        }

        // draw its label next to the main circle
        canvas.drawText(data.label, data.x + radius, data.y
                - radius, mTextPaint);
    }
}

