package com.example.tn6g10.mybouncingball;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;


public class MovingObject {
    private static final int HISTORY_COUNT = 20;
    private static final float BALL_DETECT_OFFSET = 30;
    // state (fields)
    public RectF rect = new RectF();
    public float dx = 0;
    public float dy = 0;
    public Paint paint = new Paint();
    private float dpf = 1; //distance per frame
    private String Id = "";

    private float orientation = 0;

    private Boolean isBeingControlled = false;
    private int eventId = -1;

    // arrray of pointer position history
    public ArrayList<PointF> trajectory = new ArrayList<PointF>();
    // current position in trajectory array
    public int trajectoryIndex = 0;
    public int trajectoryCount = 0;

    private MovingObject targetObject = null;

    private float obstacles = 0;
    private float presure = 0;
    private float connectivity = 0;

    private int side = 1;

    private Boolean hasBall = false;

    private MovingObject owner = null;

    /* Constructs a default empty sprite. */
    public MovingObject() {
        // empty
        // initialise history array
        orientation = (float) (360f*Math.random());
    }

    /**
     * Add a point to its history. Overwrites oldest point if the maximum
     * number of historical points is already stored.
     *
     */
    public void addToTrajectory(int index, float x, float y) {
        PointF p = new PointF(x,y);
        //p.x = x;
        //p.y = y;
        trajectory.add(index,p);
    }

    public void addToTrajectory(float x, float y) {
        PointF p = new PointF(x,y);
        //p.x = x;
        //p.y = y;
        trajectory.add(p);
    }


    /* Constructs a sprite of the given location and size. */
    public MovingObject(float x, float y, float width, float height) {
        setLocation(x, y);
        setSize(width, height);
    }

    /* Tells the sprite to move itself by its current velocity dx,dy. */
    public void move() {

        rect.offset(dx, dy);

        // handle ball bouncing off edges
        //if (ball.rect.left < 0 || ball.rect.right >= getWidth()) {
        //    ball.dx = -ball.dx;
        //}
        //if (ball.rect.top < 0 || ball.rect.bottom >= getHeight()) {
        //    ball.dy = -ball.dy;
        //}
    }
    /* Tells the sprite to move itself by its current velocity dx,dy. */
    public void move(double w, double h) {

        rect.offset(dx, dy);



        // handle ball bouncing off edges
        if (rect.left < 0 || rect.right >= w) {
            dx = -dx;
        }
        if (rect.top < 0 || rect.bottom >= h) {
            dy = -dy;
        }


    }

    /* Stops the sprite from moving by setting its velocity to 0,0. */
    public void stopMoving() {
        setVelocity(0, 0);
    }

    /* Sets the sprite's x,y location on screen to be the given values. */
    public void setLocation(float x, float y) {
        rect.offsetTo(x, y);
    }

    /* Sets the sprite's size to be the given values. */
    public void setSize(float width, float height) {
        rect.right = rect.left + width;
        rect.bottom = rect.top + height;
    }

    /* Sets the sprites dx,dy velocity to be the given values. */
    public void setVelocity(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /* Sets the sprites dx,dy velocity along the trajectory. */
    public void moveAlongTrajectory() {
        Log.println(Log.INFO, "tag", "trajectory size: "+ trajectory.size());
        Random ran = new Random();
        if (trajectory.size()<1) {
            this.dx = 0;//(float) (Math.random()-0.5)*this.dpf;
            this.dy = 0;//(float) (Math.random()-0.5)*this.dpf;
            return;
        }
        /*
        if (trajectory.size()>1) {
            this.dx = trajectory.get(1).x-trajectory.get(0).x;
            this.dy = trajectory.get(1).y-trajectory.get(0).y;
            this.move();
            trajectory.remove(0);
        }*/
        this.addToTrajectory(0,rect.centerX(),rect.centerY());

        this.dx = 0;
        this.dy = 0;

        double dist = 0;//Math.sqrt(Math.pow(trajectory.get(0).x-rect.centerX(),2)+Math.pow(trajectory.get(0).y-rect.centerY(),2));
        double scale = 1;//dpf/dist;
        double seg = 0;
        int i = 1;

        do {
            seg = Math.sqrt(Math.pow(trajectory.get(i).x-trajectory.get(i-1).x,2)+Math.pow(trajectory.get(i).y-trajectory.get(i-1).y,2));
            dist += seg;
            i += 1;
            Log.println(Log.INFO, "tag", "dist: "+ dist);
        } while  (dist < dpf && i < trajectory.size());
        i -= 1;
        if (seg > 0.001) {
            scale = (dpf - dist + seg) / seg;
        }
        else {
            scale = 0;
        }
        this.dx += (trajectory.get(i - 1).x - trajectory.get(0).x) + scale * (trajectory.get(i).x - trajectory.get(i - 1).x);
        this.dy += (trajectory.get(i - 1).y - trajectory.get(0).y) + scale * (trajectory.get(i).y - trajectory.get(i - 1).y);
        Log.println(Log.INFO, "tag", "dxy : "+ dx + " dy: " + dy);
        /*
        for (i = 1; i < trajectory.size(); i++) {
            scale = dpf/dist;
            if (dist < dpf || i == trajectory.size()-1){
                this.dx += -scale*(trajectory.get(i).x-trajectory.get(i-1).x);
                this.dy += -scale*(trajectory.get(i).y-trajectory.get(i-1).y);

                //trajectory.remove(0);
            }
            else
                break;
        }*/
        //this.move();
        Log.println(Log.INFO, "tag", "trajectory size: "+ trajectory.size());

        if (dist < dpf) i = trajectory.size();
        for (int j = 0; j < i; j++) {
            trajectory.remove(0);
        }
        Log.println(Log.INFO, "tag", "trajectory size: "+ trajectory.size());
    }

    /* Sets the sprites dx,dy velocity along the trajectory. */
    public void tracingTarget() {
        //Log.println(Log.INFO, "tag", "trajectory size: "+ trajectory.size());
        if (targetObject.trajectory.size()<2) {
            float seg = (float) Math.sqrt(Math.pow(targetObject.rect.centerX()-rect.centerX(),2)+
                    Math.pow(targetObject.rect.centerY()-rect.centerY(),2));

            this.dx = dpf/seg*(targetObject.rect.centerX()-rect.centerX());
            this.dy = dpf/seg*(targetObject.rect.centerY()-rect.centerY());
            return;
        }


        double dist = 0;//Math.sqrt(Math.pow(trajectory.get(0).x-rect.centerX(),2)+Math.pow(trajectory.get(0).y-rect.centerY(),2));
        double targetDist = 0;
        double scale = 1;//dpf/dist;
        double seg = 0;
        int i = 1;

        do {
            seg = Math.sqrt(Math.pow(targetObject.trajectory.get(i).x-targetObject.trajectory.get(i-1).x,2)+
                    Math.pow(targetObject.trajectory.get(i).y-targetObject.trajectory.get(i-1).y,2));
            dist += seg;
            targetDist = Math.sqrt(Math.pow(targetObject.trajectory.get(i).x-rect.centerX(),2)+
                    Math.pow(targetObject.trajectory.get(i).y-rect.centerY(),2));
            i += 1;
            Log.println(Log.INFO, "tag", "dist: "+ dist);
        } while  (dist/targetObject.getDpf() < targetDist/dpf && i < targetObject.trajectory.size());

        i = i-1;
        seg = Math.sqrt(Math.pow(targetObject.trajectory.get(i).x-rect.centerX(),2)+
                Math.pow(targetObject.trajectory.get(i).y-rect.centerY(),2));

        this.dx = (float) (dpf/seg*(targetObject.trajectory.get(i).x-rect.centerX()));
        this.dy = (float) (dpf/seg*(targetObject.trajectory.get(i).y-rect.centerY()));

    }

    /* Sets the sprites dx,dy velocity along the trajectory. */
    public boolean isInObject(float x, float y) {
        float offset = 0;
        //if (getId().compareTo("0") == 0)
            offset = BALL_DETECT_OFFSET;
        if (x >= rect.left - offset && x <= rect.right + offset && y <= rect.bottom + offset && y >= rect.top - offset) {
            return true;
        }
        return false;
    }

    public Boolean getBeingControlled() {
        return isBeingControlled;
    }

    public void setBeingControlled(Boolean beingControlled) {
        isBeingControlled = beingControlled;
    }



    public float getDpf() {
        return dpf;
    }

    public void setDpf(float dpf) {
        this.dpf = dpf;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public MovingObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(MovingObject targetObject) {
        this.targetObject = targetObject;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public void drawEyes(Canvas cv){
        //orientation = (float) (Math.atan2(dy,dx)*180f/Math.PI);

        float radius = rect.width()/2;

        float[] pts = new float[16];

        Path wallpath = new Path();
        paint.setStyle(Paint.Style.STROKE);
        //paint.setColor(Color.RED);
        //paint.setStrokeWidth(4);
        wallpath.reset(); // only needed when reusing this path for a new build
        wallpath.moveTo(rect.centerX() + (float) (radius*Math.cos(orientation-45)),
                rect.centerY() - (float) (radius*Math.sin(orientation-45))); // used for first point
        wallpath.lineTo(rect.centerX() + (float) (radius/Math.cos(22.5)*Math.cos(orientation-22.5)),
                rect.centerY() - (float) (radius/Math.cos(22.5)*Math.sin(orientation-22.5)));
        wallpath.lineTo(rect.centerX() + (float) (radius*Math.cos(orientation)),
                rect.centerY() - (float) (radius*Math.sin(orientation)));
        wallpath.lineTo(rect.centerX() + (float) (radius/Math.cos(22.5)*Math.cos(orientation+22.5)),
                rect.centerY() - (float) (radius/Math.cos(22.5)*Math.sin(orientation+22.5)));
        wallpath.lineTo(rect.centerX() + (float) (radius*Math.cos(orientation+45)),
                rect.centerY() - (float) (radius*Math.sin(orientation+45))); // there is a setLastPoint action but i found it not to work as expected

        cv.drawPath(wallpath, paint);

        paint.setStyle(Paint.Style.FILL);
        /*

            pts[0] = rect.centerX() + (float) (radius*Math.cos(orientation-45));
            pts[1] = rect.centerY() - (float) (radius*Math.sin(orientation-45));

            pts[2] = rect.centerX() + (float) (radius/Math.cos(22.5)*Math.cos(orientation-22.5));
            pts[3] = rect.centerY() - (float) (radius/Math.cos(22.5)*Math.sin(orientation-22.5));

            pts[4] = pts[2];
            pts[5] = pts[3];

            pts[6] = rect.centerX() + (float) (radius*Math.cos(orientation));
            pts[7] = rect.centerY() - (float) (radius*Math.sin(orientation));

            pts[8] = pts[6];
            pts[9] = pts[7];

            pts[10] = rect.centerX() + (float) (radius/Math.cos(22.5)*Math.cos(orientation+22.5));
            pts[11] = rect.centerY() - (float) (radius/Math.cos(22.5)*Math.sin(orientation+22.5));

            pts[12] = pts[2];
            pts[13] = pts[3];

            pts[14] = rect.centerX() + (float) (radius*Math.cos(orientation+45));
            pts[15] = rect.centerY() - (float) (radius*Math.sin(orientation+45));

*/

        //cv.drawLines(pts, paint);
    }

    public float getObstacles() {
        return obstacles;
    }

    public void setObstacles(float obstacles) {
        this.obstacles = obstacles;
    }

    public float getPresure() {
        return presure;
    }

    public void setPresure(float presure) {
        this.presure = presure;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    // find relative angle ACB from player at location C to two points A and B
    public float findRelativeAngle(float Ax, float Ay, float Bx, float By){
        float CB = findDist(rect.centerX(), rect.centerY(), Bx, By);
        float CH = CB*CB/findDotProd( Ax,  Ay,  Bx,  By);

        //float xDiff = x2 - x1;
        //float yDiff = y2 - y1;
        //return (float) (Math.atan2(yDiff, xDiff) * (180 / Math.PI));
        return (float) Math.acos(CH/CB);
    }
    // find relative angle ACB from player at location C to two points A and B
    public float findRelativeRatio(float Ax, float Ay, float Bx, float By){
        float CA = findDist(rect.centerX(), rect.centerY(), Ax, Ay);
        float CB = findDist(rect.centerX(), rect.centerY(), Bx, By);
        float CH = findDotProd( Ax-rect.centerX(),  Ay-rect.centerY(),
                Bx-rect.centerX(),  By-rect.centerY())/CB;

        //float xDiff = x2 - x1;
        //float yDiff = y2 - y1;
        //return (float) (Math.atan2(yDiff, xDiff) * (180 / Math.PI));
        float AH = (float) Math.pow(CA*CA-CH*CH,2);
        return (CH/AH);
    }

    public float findDist(float Ax, float Ay, float Bx, float By) {
        return  (float) Math.sqrt(Math.pow(Ax - Bx, 2) +
                Math.pow(Ay - By, 2));
    }
    public float findDotProd(float Ax, float Ay, float Bx, float By) {
        return  Ax*Bx + Ay*By;
    }

    public float getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(float connectivity) {
        this.connectivity = connectivity;
    }

    public Boolean getHasBall() {
        return hasBall;
    }

    public void setHasBall(Boolean hasBall) {
        this.hasBall = hasBall;
    }

    public MovingObject getOwner() {
        return owner;
    }

    public void setOwner(MovingObject owner) {
        this.owner = owner;
    }
}
