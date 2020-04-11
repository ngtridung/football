package com.example.tn6g10.mybouncingball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/*
 * CS 193A, Winter 2015, Marty Stepp
 * This class is a graphical view of a simple animated app
 * with a ball that moves around and bounces off the edges of the screen,
 * as well as a yellow "pac-man" sprite that can move around in response
 * to the user touching the screen at its various edges.
 */


public class TestGameView extends View {
    private static final float BALL_SIZE = 15;
    private static final float PLAYER_SIZE = 40;
    private static final float BALL_MAX_VELOCITY = 80;

    private static final float FIELD_WIDTH = 110;
    private static final float FIELD_LENGTH = 70;
    private static final float GOAL_WIDTH = 7.32f;
    private static final float GOAL_AREA = 9.16f;
    private static final float GOAL_AREA_WIDTH = 5.5f;
    private static final float PENALTY_AREA = 20.16f;
    private static final float PENALTY_AREA_WIDTH = 16.5f;
    private static final float PENALTY = 11f;
    private static final float CORNER_RADIUS = 1f;
    private static final float CENTRE_RADIUS = 9.15f;





    private static final int nPlayers = 11;

    private MovingObject ball;
    //private MovingObject ref;
    private DrawingThread dthread;

    private ArrayList<MovingObject> players;// = new ArrayList<MovingObject>();

    private String txt = "";

    private float scale = 1;



    /*
     * This constructor sets up the initial state of the view and sprites.
     */
    public TestGameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        players = new ArrayList<MovingObject>();

        initialisePaint();

        // set up initial state of ball
        ball = new MovingObject();
        ball.setLocation(200, 450);
        ball.setSize(BALL_SIZE, BALL_SIZE);
        ball.setDpf(9);
        ball.setId(""+(0));
        //ball.setVelocity(
        //        (float) ((Math.random() - .5) * 2 * BALL_MAX_VELOCITY),
        //        (float) ((Math.random() - .5) * 2 * BALL_MAX_VELOCITY)
        //);
        ball.paint.setARGB(255, 255, 0, 0);



        for (int i = 0; i < nPlayers*2; i++) {
            // set up initial state of pac-man
            MovingObject player = new MovingObject();
            player.setSize(PLAYER_SIZE, PLAYER_SIZE);
            player.setDpf(2);
            player.setTargetObject(ball);
            float w = 600;//getWidth();
            float h = 400;//getHeight();
            player.setLocation((int) (Math.random()*w), (int) (Math.random()*h));
            if (i < nPlayers) {
                //player.setLocation(50 + 50 * i, 50 + 50 * i);
                //player.setLocation((float) Math.random()*w, (float) Math.random()*h);
                player.paint.setARGB(255, 200, 200, 0);
                player.setId(""+(i+1));
                player.setSide(1);
            }
            else {
                //player.setLocation(50 * i - 500, 0 + 50 * i);
                player.paint.setARGB(255, 0, 0, 255);
                player.setId(""+(i-nPlayers+1));
                player.setSide(2);
            }

            players.add(player);
        }
        players.add(ball);

        setInitialLocations();

        ball.rect.offsetTo(players.get(0).rect.left,players.get(0).rect.top);
        players.get(0).setHasBall(true);
        ball.setOwner(players.get(0));

        // start a drawing thread to animate screen at 50 frames/sec
        dthread = new DrawingThread(this, 50);
        dthread.start();
    }



    public int getObjectAtLocation(float x, float y){
        for (int i = players.size()-1; i >= 0;i--) {
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
        for (int i = 0; i < players.size(); i++) {
            Log.println(Log.INFO,"tag", "player: "+ (i+1)+" location: " + players.get(i).rect.toString());
        }
        /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK. Alternatively a call to
         * event.getActionMasked() would yield in the action as well.
         */
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {
                // first pressed gesture has started
                int i = getObjectAtLocation(event.getX(0), event.getY(0));
                Log.println(Log.INFO,"tag","1st mouse down, player: "+i);
                if (i >= 0) {
                    players.get(i).setBeingControlled(true);

                    /*
                     * Only one touch event is stored in the MotionEvent. Extract
                     * the pointer identifier of this touch from the first index
                     * within the MotionEvent object.
                     */
                    int id = event.getPointerId(0);
                    players.get(i).setEventId(id);
                    players.get(i).addToTrajectory(0,event.getX(0), event.getY(0));


                }

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


                int i = getObjectAtLocation(event.getX(index), event.getY(index));
                Log.println(Log.INFO,"tag","index mouse down, index: " + index + " id: " + id + " player: "+i);

                if (i >= 0) {
                    players.get(i).setBeingControlled(true);
                    players.get(i).setEventId(id);
                    players.get(i).addToTrajectory(0,event.getX(index), event.getY(index));
                }

                /*
                 * Store the data under its pointer identifier. The index of
                 * this pointer can change over multiple events, but this
                 * pointer is always identified by the same identifier for this
                 * active gesture.
                 */

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
                Log.println(Log.INFO,"tag"," mouse up id: " + id);

                for (int i = 0; i < players.size(); i++) {
                    if (id == players.get(i).getEventId()) {
                        players.get(i).setBeingControlled(false);
                        players.get(i).setEventId(-1);
                        Log.println(Log.INFO,"tag","index mouse down player: "+i);

                    }
                }




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
                Log.println(Log.INFO,"tag","index mouse up id: " + id);
                for (int i = 0; i < players.size(); i++) {
                    if (id == players.get(i).getEventId()) {
                        players.get(i).setBeingControlled(false);
                        players.get(i).setEventId(-1);
                        Log.println(Log.INFO,"tag","index mouse up player: "+i);
                    }
                }
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
                    Log.println(Log.INFO,"tag","index mouse move id: " + id);
                    for (int i = 0; i < players.size(); i++) {
                        if (id == players.get(i).getEventId()) {
                            players.get(i).addToTrajectory(event.getX(index), event.getY(index));
                            Log.println(Log.INFO,"tag","index mouse move, index: " + index + " id: " + id + " player: "+i);
                        }
                    }

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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        Paint paint = new Paint();

        paint.setARGB(255,0,255,0);

        canvas.drawRect(0,0, width, height, paint);




        float fieldLength = 110f;
        float fieldWidth = 70f;
        float scaleWidth = fieldLength+2;
        float scaleHeight = fieldWidth+2;
        //float scale;
        if((width/height) >= (110f/70f)) {
            scale = height/scaleHeight;
        } else {
            scale = width/scaleWidth;
        }

        //canvas.scale(scale, scale);
        //canvas.translate(1, 1);
        //g2.setColor(Color.WHITE);
        //Stroke stroke = new BasicStroke(5f/36f);
        //canvas.setStroke(stroke);

        fieldWidth = height;
        fieldLength = width;

        paint.setStyle(Paint.Style.STROKE);
        //paint.setColor(Color.MAGENTA);

        paint.setARGB(255,255,0,0);
        drawTouchLines(canvas, fieldLength, fieldWidth, paint);
        drawGoalLines(canvas, fieldLength, fieldWidth, paint);
        drawCenterLine(canvas, fieldLength, fieldWidth, paint);

        paint.setARGB(255,0,0,100);
        drawCenterCircle(canvas, fieldLength, fieldWidth, paint);

        paint.setARGB(255,255,0,0);
        drawCenterMark(canvas, fieldLength, fieldWidth, paint);
        drawCornerArches(canvas, fieldLength, fieldWidth, paint);

        paint.setARGB(255,0,0,255);

        drawGoalAreas(canvas, fieldLength, fieldWidth, paint);

        drawPenaltyAreas(canvas, fieldLength, fieldWidth, paint);
        drawPenaltyMarks(canvas, fieldLength, fieldWidth, paint);
        drawPenaltyArches(canvas, fieldLength, fieldWidth, paint);

        //canvas.scale(1, 1);
        //canvas.translate(0, 0);
        /**/

        paint.setARGB(255,125,125,125);
        canvas.drawOval(ball.rect, ball.paint);
        //canvas.drawOval(pacman.rect, pacman.paint);

        paint.setStyle(Paint.Style.FILL);
        drawGoals(canvas, fieldLength, fieldWidth, paint);

        paint.setTextSize(PLAYER_SIZE/2);



        for (int i = 0; i < players.size(); i++) {
            canvas.drawOval(players.get(i).rect, players.get(i).paint);
            //players.get(i).drawEyes(canvas);
            if (i < players.size()-1) { // if not the ball
                float factor = 0.7f;
                if (players.get(i).getId().length() > 1) factor = 0.5f; // if id is too long
                float x = factor * players.get(i).rect.centerX() + (1 - factor) * players.get(i).rect.left;
                float y = factor * players.get(i).rect.centerY() + (1 - factor) * players.get(i).rect.bottom;
                canvas.drawText(players.get(i).getId(), x, y, paint);

            }
            //Log.println(Log.INFO,"tag", "player: "+ (i+1)+" location: " + players.get(i).rect.toString());
            //players.get(i).move(getWidth(),getHeight());
            players.get(i).move();
        }

        //int w = getWidth();
        //int h = getHeight();
        //canvas.drawText(txt, w/2, h/2, ball.paint);

        if (ball.trajectory.size() == 0) {
            findScores();

            MovingObject owner = ball.getOwner();
            int playerToPass = -1;
            float minObstacles = Float.POSITIVE_INFINITY;
            ;
            for (int i = 0; i < players.size() - 1; i++) {
                //players.get(i).setId(""+players.get(i).getPresure());
                //if (!players.get(i).getHasBall())
                if ((players.get(i).getObstacles() + 11*players.get(i).getPresure() < minObstacles) && !(players.get(i).getHasBall())
                && (owner.getSide() == players.get(i).getSide())) {
                    minObstacles = players.get(i).getObstacles() + 11*players.get(i).getPresure();
                    playerToPass = i;
                }
            }
            ball.addToTrajectory(players.get(playerToPass).rect.centerX(), players.get(playerToPass).rect.centerY());
            ball.setOwner(players.get(playerToPass));
            players.get(playerToPass).setHasBall(true);
            owner.setHasBall(false);
        }

        updateSprites();



    }

    // updates sprites' positions between frames of animation
    private void updateSprites() {
        //pacman.moveAlongTrajectory();
        for (int i = 0; i < players.size(); i++) {
            //if ( i != 10)
                players.get(i).moveAlongTrajectory();
            //players.get(i).tracingTarget();

        }
        for (int i = 0; i < players.size()-1; i++) {
            //if ( i == 1)
            //players.get(i).moveAlongTrajectory();
            //players.get(i).tracingTarget();

        }
        //players.get(22).moveAlongTrajectory();
        //players.get(10).tracingTarget();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawPenaltyArches(Canvas g2, float fieldLength,
                                   float fieldWidth, Paint paint) {
//		double extent = 2*Math.toDegrees(Math.acos(6d/10d));
        float extent = 106.26020470831196f;
        //g2.draw(new Arc2D.Double(fieldLength-12-10, (fieldWidth/2)-10, 20, 20, 180-(extent/2), extent, Arc2D.OPEN));
        //g2.draw(new Arc2D.Double(12-10, (fieldWidth/2)-10, 20, 20, -extent/2, extent, Arc2D.OPEN));
        g2.drawArc((PENALTY-CENTRE_RADIUS)*scale,fieldWidth/2-CENTRE_RADIUS*scale,
                (PENALTY+CENTRE_RADIUS)*scale,fieldWidth/2+CENTRE_RADIUS*scale,
                -(extent/2), extent, true, paint);
        g2.drawArc(fieldLength-(PENALTY+CENTRE_RADIUS)*scale,fieldWidth/2-CENTRE_RADIUS*scale,
                fieldLength-(PENALTY-CENTRE_RADIUS)*scale,fieldWidth/2+CENTRE_RADIUS*scale,
                180-(extent/2), extent, true, paint);
    }

    private void drawPenaltyMarks(Canvas g2, float fieldLength,
                                  float fieldWidth, Paint paint) {
        //g2.drawCircle(fieldLength-12*scale-(10f/36)*scale, (fieldWidth/2)-(10f/36)*scale, (10f/36)*scale, paint);
        //g2.drawCircle(12*scale-(10f/36)*scale, (fieldWidth/2)-(10f/36)*scale, (10f/36)*scale, paint);
        g2.drawCircle(PENALTY*scale,fieldWidth/2, (10f/36)*scale, paint);
        g2.drawCircle(fieldLength-PENALTY*scale,(fieldWidth/2), (10f/36)*scale, paint);
    }

    private void drawPenaltyAreas(Canvas g2, float fieldLength,
                                  float fieldWidth, Paint paint) {
        //g2.drawRect(0, (fieldWidth/2)-22*scale, 18*scale, 42*scale, paint);
        //g2.drawRect(fieldLength-18*scale, (fieldWidth/2)-22*scale, 18*scale, 42*scale, paint);
        g2.drawRect(0, (fieldWidth/2)-PENALTY_AREA*scale, PENALTY_AREA_WIDTH*scale, (fieldWidth/2)+PENALTY_AREA*scale, paint);
        g2.drawRect(fieldLength-PENALTY_AREA_WIDTH*scale, (fieldWidth/2)-PENALTY_AREA*scale, fieldLength, (fieldWidth/2)+PENALTY_AREA*scale, paint);
    }

    private void drawGoalAreas(Canvas g2, float fieldLength,
                               float fieldWidth, Paint paint) {
        //g2.drawRect(0, (fieldWidth/2)-10*scale, 6*scale, 20*scale, paint);
        //g2.drawRect(fieldLength-6*scale, (fieldWidth/2)-10*scale, 6*scale, 20*scale, paint);
        g2.drawRect(0, (fieldWidth/2)-GOAL_AREA*scale, GOAL_AREA_WIDTH*scale, (fieldWidth/2)+GOAL_AREA*scale, paint);
        g2.drawRect(fieldLength-GOAL_AREA_WIDTH*scale, (fieldWidth/2)-GOAL_AREA*scale, fieldLength, (fieldWidth/2)+GOAL_AREA*scale, paint);

    }

    private void drawGoals(Canvas g2, float fieldLength, float fieldWidth, Paint paint) {
        //g2.drawRect(-1, (fieldWidth/2)-4*scale, 1*scale, 8*scale, paint);
        //g2.drawRect(fieldLength, (fieldWidth/2)-4*scale, 1*scale, 8*scale, paint);

        g2.drawRect(-1, (fieldWidth/2)-GOAL_WIDTH/2*scale, 1*scale, (fieldWidth/2)+GOAL_WIDTH/2*scale, paint);
        g2.drawRect(fieldLength-1*scale, (fieldWidth/2)-GOAL_WIDTH/2*scale, fieldLength, (fieldWidth/2)+GOAL_WIDTH/2*scale, paint);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawCornerArches(Canvas g2, float fieldLength,
                                  float fieldWidth, Paint paint) {
        /*
        g2.draw(new Arc2D.Double(-1, -1, 2, 2, 270, 90, Arc2D.OPEN));
        g2.draw(new Arc2D.Double(fieldLength-1, -1, 2, 2, 180, 90, Arc2D.OPEN));
        g2.draw(new Arc2D.Double(fieldLength-1, fieldWidth-1, 2, 2, 90, 90, Arc2D.OPEN));
        g2.draw(new Arc2D.Double(-1, fieldWidth-1, 2, 2, 0, 90, Arc2D.OPEN));

         */
        //g2.drawArc(-1, -1, 2, 2, 270, 90,true, paint);
        g2.drawArc(-2*scale, -2*scale, 2*scale, 2*scale, 270, 90,true,paint);
        g2.drawArc(fieldLength-2*scale, -2*scale, fieldLength+2*scale, 2*scale, 180, 90,true,paint);
        g2.drawArc(fieldLength-2*scale, fieldWidth-2*scale, fieldLength+2*scale, fieldWidth+2*scale, 90, 90,true,paint);
        g2.drawArc(-2*scale, fieldWidth-2*scale, 2*scale, fieldWidth+2*scale, 0, 90,true,paint);

    }

    private void drawCenterMark(Canvas g2, float fieldLength,
                                float fieldWidth, Paint paint) {
        //g2.fill(new Ellipse2D.Double((fieldLength/2)-(10d/36), (fieldWidth/2)-(10d/36), (20d/36), (20d/36)));
        g2.drawCircle((fieldLength/2), (fieldWidth/2), (20f/36)*scale, paint);
    }

    private void drawCenterCircle(Canvas g2, float fieldLength,
                                  float fieldWidth, Paint paint) {
        //g2.draw(new Ellipse2D.Double((fieldLength/2)-10, (fieldWidth/2)-10, 20, 20));
        //g2.drawCircle((fieldLength/2), (fieldWidth/2), 10f*scale, paint);

        g2.drawCircle((fieldLength/2), (fieldWidth/2), 9.15f*scale, paint);

        //Log.println(Log.INFO,"tag","scale="+scale);
        //Log.println(Log.INFO,"tag","scale="+20f*scale);
        //Log.println(Log.INFO,"tag","scale="+(20f/36)*scale);
    }

    private void drawCenterLine(Canvas g2, float fieldLength,
                                float fieldWidth, Paint paint) {
        g2.drawLine(fieldLength/2, 0, fieldLength/2, fieldWidth, paint);
    }

    private void drawGoalLines(Canvas g2, float fieldLength,
                               float fieldWidth, Paint paint) {
        g2.drawLine(0, 0, 0, fieldWidth, paint);
        g2.drawLine(fieldLength, 0, fieldLength, fieldWidth, paint);
    }

    private void drawTouchLines(Canvas g2, float fieldLength,
                                float fieldWidth, Paint paint) {
        g2.drawLine(0, 0, fieldLength, 0, paint);
        g2.drawLine(0, fieldWidth, fieldLength, fieldWidth, paint);
    }


    public void findScores(){

        for (int i = 0; i < players.size()-1; i++){
            float obstacle = 0;
            float presure = 0;
            float connectivity = 0;
            float goalX = getWidth();
            float goalY = getHeight()/2;
            if (i >= nPlayers)
                goalX = 0;


            for (int j = 0; j < players.size()-1; j++) {
                // To change to take into account if CH > CA; in that case, obstacle += 0
                if ((players.get(i).findRelativeRatio(players.get(j).rect.centerX(),
                        players.get(j).rect.centerY(), goalX, goalY) > 1) &&
                        (players.get(i).getSide() != players.get(j).getSide()))
                    obstacle += 1;
                //ratio = CH/CA
                //float ratio = players.get(i).findRelativeRatio(players.get(j).rect.centerX(),
                  //      players.get(j).rect.centerY(), ball.rect.centerX(), ball.rect.centerY());
                float ratio = ball.getOwner().findRelativeRatio(players.get(j).rect.centerX(),
                        players.get(j).rect.centerY(), players.get(i).rect.centerX(),
                        players.get(i).rect.centerY());
                if ((ratio > ball.getDpf()/players.get(i).getDpf()) &&
                        (players.get(i).getSide() != players.get(j).getSide())) presure += 1;
            }
            players.get(i).setObstacles(obstacle);
            players.get(i).setPresure(presure);
            players.get(i).setConnectivity(connectivity);
            Log.println(Log.INFO,"tag", "i:"+i+" obs"+obstacle+"pre:"+presure);
        }

    }

    public void setInitialLocations(){

        /*
        11,11

         */
        if (players.size() == 23) {
            players.get(0).setLocation(50, 320);
            players.get(1).setLocation(100, 50);
            players.get(2).setLocation(100, 320);
            players.get(3).setLocation(100,550 );
            players.get(4).setLocation(200, 50);
            players.get(5).setLocation(200, 550 );
            players.get(6).setLocation(250, 320 );
            players.get(7).setLocation(300, 250 );
            players.get(8).setLocation(300, 390);
            players.get(9).setLocation(400,320 );
            players.get(10).setLocation(400, 390);

            players.get(11).setLocation(1000, 320);
            players.get(12).setLocation(900, 50 );
            players.get(13).setLocation(900, 320 );
            players.get(14).setLocation(900, 550 );
            players.get(15).setLocation(800, 50 );
            players.get(16).setLocation(800, 550);
            players.get(17).setLocation(750, 320);
            players.get(18).setLocation(700,250);
            players.get(19).setLocation(700,390);
            players.get(20).setLocation(600,320);
            players.get(21).setLocation(600, 390);
        }
    }
}

