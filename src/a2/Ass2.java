package a2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.PMVMatrix;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vertex3D;
import graphicslib3D.shape.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;


public class Ass2 extends JFrame implements GLEventListener, ActionListener, MouseWheelListener, KeyListener {

    //scaled at 10 billion
    private static final float sunSize = 10.39f; //cm

    private static final float mercurySize = 0.05f; //cm
    private static final float mercuryDistance = 5f; //57,909,000
    //private float MercuryOrbit = 5.8 //m

    private static final float venusSize = 0.12f; //cm
    private static final float venusDistance = 10f; //108,200,000
    //private float VenusOrbit = 10.8 //m

    private static final float earthSize = 0.13f; //cm
    private static final float earthDistance = 14f; //149,600,000
    //private float EarthOrbit = 15.0 //m

    private static final float marsSize = 0.07f; //cm
    private static final float marsDistance = 22f; //227,940,000
    //private float MarsOrbit = 22.8 //m

    private static final float jupiterSize = 1.43f;//1.43 cm
    private static final float jupiterDistance = 77f; //778,400,000
    //private float JupiterOrbit = 77.8 //m

    private static final float saturnSize = 1.2f;//1.2 cm
    private static final float saturnDistance = 142f; // 1,423,600,000
    //private float SaturnOrbit = 142.4 //m

    private static final float uranusSize = 0.5f;//0.51 cm
    private static final float uranusDistance = 286f; // 2,867,000,000
    //private float UranusOrbit =  286.7 //m

    private static final float neptuneSize = 0.49f;//0.49 cm
    private static final float neptuneDistance = 448f; //4,488,400,000
    //private float NeptuneOrbit = 448.9 //m

    private static final float plutoSize = 0.02f; // 0.02 cm
    private static final float plutoDistance = 590f; // 5,909,600,000
    //private float PlutoOrbit = 591.0 //m



    public static float zoom = 0.0f;
    public static float pan = 0.0f;
    public static float pitch = 0.0f;
    public static float strafe = 0.0f;
    public static boolean axis = false;
    private Dimension dimention = new Dimension(1500, 1500);
    private GLCanvas myCanvas;

    private int vao[] = new int[1];
    private int vbo[] = new int[3];
    private float cameraX, cameraY, cameraZ;
    private int rendering_program;
    private int VAO[] = new int[1];
    private GLSLUtils util = new GLSLUtils();
    private float upDown = 0.0f;
    private boolean animated = true;
    private Animator animator;

    private Random rand;

    private Sphere mySphere = new Sphere(50);

    private TextureReader tr = new TextureReader();
    private int sunTexture;
    private int earthTexture;
    private int moonTexture;
    private int marsTexture;
    private int jupiterTexture;
    private int saturnTexture;
    private int[] samplers = new int[2];

    public Ass2() {
        setTitle("Assignment 2 CSC155");
        setSize(dimention);
        this.addMouseWheelListener(this);

        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        rand = new Random();
        add(myCanvas);
        this.addKeyListener(this);
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }


    public void init(GLAutoDrawable drawable) {
        //this.getRootPane().requestFocus();
        GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderPrograms(drawable);
        setupVertices(gl);

        int tx_loc = gl.glGetUniformLocation(rendering_program, "s");
        gl.glGenSamplers(1, samplers, 0);
        gl.glBindSampler(0, tx_loc);
        sunTexture = tr.loadTexture(drawable, "textures/sunmap.jpg");
        earthTexture = tr.loadTexture(drawable, "textures/earthmap1k.jpg");
        moonTexture = tr.loadTexture(drawable, "textures/moonmap1k.jpg");
        marsTexture = tr.loadTexture(drawable, "textures/marsmap1k.jpg");
        jupiterTexture = tr.loadTexture(drawable, "textures/jupitermap.jpg");
        saturnTexture = tr.loadTexture(drawable, "textures/saturnmap.jpg");
        animator = new Animator(myCanvas);
        Thread thread =
                new Thread(new Runnable() {
                    public void run() {
                        animator.start();
                    }
                });
        thread.start();
    }

    public void display(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();

        gl.glClear(GL_DEPTH_BUFFER_BIT);
        FloatBuffer background = FloatBuffer.allocate(4);
        gl.glClearBufferfv(GL_COLOR, 0, background);

        gl.glUseProgram(rendering_program);

        int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
        int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");

        double orbitSpeed[] = new double[15];
        for (int i = 1; i < 15; i++) {
            orbitSpeed[i] = (double) (System.currentTimeMillis() % 360000) / (1000.0 * i);
        }
        float aspect = myCanvas.getWidth() / myCanvas.getHeight();
        //Matrix3D pMat = perspective(30.0f, aspect, 0.1f, 10000.0f);
        PMVMatrix mvStack = new PMVMatrix();
        mvStack.glLoadIdentity();
        mvStack.gluPerspective(60f, aspect, 0.1f, 1000.0f);
        // --------------------------- CAMERA
        // Perspective.

        //GLU glu = new GLU();
        float widthHeightRatio = (float) getWidth() / (float) getHeight();
        //glu.gluPerspective(45, widthHeightRatio, 1, 1000);
        //glu.gluLookAt(0, 0, 200, 0, 0, 0, 0, 1, 0);


//        mvStack.gluLookAt(
//                (float) Math.sin(orbitSpeed[7]) * earthDistance + strafe, 0.5f, (float) Math.cos(orbitSpeed[7]) * earthDistance - zoom,
//                (float) Math.sin(orbitSpeed[7]) * earthDistance, 0.00f, (float) Math.cos(orbitSpeed[7]) * earthDistance,
//                0, 1, 0);
        mvStack.gluLookAt(
                (float) Math.sin(orbitSpeed[5]) * jupiterDistance + strafe, 2f, (float) Math.cos(orbitSpeed[5]) * jupiterDistance - zoom,
                (float) Math.sin(orbitSpeed[5]) * jupiterDistance, 0.00f, (float) Math.cos(orbitSpeed[5]) * jupiterDistance,
                0, 1, 0);



        if (axis) {
            // ----------------------   == X-AXIS
            mvStack.glPushMatrix();
            mvStack.glTranslatef(0, 0, 0);
            mvStack.glScalef(1000f, 0.01f, 0.01f);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
            gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glEnable(GL_CULL_FACE);
            gl.glFrontFace(GL_CCW);
            gl.glEnable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
            mvStack.glPopMatrix();
            // ----------------------   == Y-AXIS
            mvStack.glPushMatrix();
            mvStack.glTranslatef(0, 0, 0);
            mvStack.glScalef(0.01f, 1000f, 0.01f);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
            gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glEnable(GL_CULL_FACE);
            gl.glFrontFace(GL_CCW);
            gl.glEnable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
            mvStack.glPopMatrix();
            // ----------------------   == Z-AXIS
            mvStack.glPushMatrix();
            mvStack.glTranslatef(0, 0, 0);
            mvStack.glScalef(0.01f, 0.01f, 1000f);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
            gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glEnable(GL_CULL_FACE);
            gl.glFrontFace(GL_CCW);
            gl.glEnable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
            mvStack.glPopMatrix();
        }
        // ----------------------   == sun
        mvStack.glPushMatrix();
        mvStack.glTranslatef(0, 0, 0);
        mvStack.glScalef(sunSize, sunSize, sunSize);
        mvStack.glRotatef(degreePerSec(0.001f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, sunTexture);
        //gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        //gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        //-----------------------   == Mercury
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[9]) * mercuryDistance, 0.0f, (float) Math.cos(orbitSpeed[9]) * mercuryDistance);
        mvStack.glScalef(mercurySize, mercurySize, mercurySize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((float) ((float) (System.currentTimeMillis() % 3600) / 20.0), 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix(); //poping Mercury
        //-----------------------   == venus
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[8]) * venusDistance, 0.0f, (float) Math.cos(orbitSpeed[8]) * venusDistance);
        mvStack.glScalef(venusSize, venusSize, venusSize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((float) ((float) (System.currentTimeMillis() % 3600) / 20.0), 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping venus
        //-----------------------   == earth
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[7]) * earthDistance, 0.0f, (float) Math.cos(orbitSpeed[7]) * earthDistance);
        mvStack.glScalef(earthSize, earthSize, earthSize);
        mvStack.glPushMatrix();

        mvStack.glRotatef(degreePerSec(0.1f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        //gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        //-----------------------   == earth moon
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[1] / 10) * 2, 0f, (float) Math.cos(orbitSpeed[1] / 10) * 2);
        mvStack.glScalef(earthSize / 1.1f, earthSize / 1.1f, earthSize / 1.1f);
        mvStack.glRotatef(degreePerSec(0.01f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        //gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping earth
        //-----------------------   == mars
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[6]) * marsDistance, 0.0f, (float) Math.cos(orbitSpeed[6]) * marsDistance);
        mvStack.glScalef(marsSize, marsSize, marsSize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((float) ((float) (System.currentTimeMillis() % 3600) / 20.0), 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        //gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping mars

        //-----------------------   == jupiter
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[5]) * jupiterDistance, 0.0f, (float) Math.cos(orbitSpeed[5]) * jupiterDistance);
        mvStack.glScalef(jupiterSize, jupiterSize, jupiterSize);
        mvStack.glPushMatrix();
        mvStack.glRotatef(degreePerSec(0.01f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        //gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, jupiterTexture);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping jupiter
        //-----------------------   == saturn
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[4]) * saturnDistance, 0.0f, (float) Math.cos(orbitSpeed[4]) * saturnDistance);
        mvStack.glScalef(saturnSize, saturnSize, saturnSize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((float) ((float) (System.currentTimeMillis() % 3600) / 20.0), 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        //gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, saturnTexture);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping saturn
        //-----------------------   == uranus
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[3]) * uranusDistance, 0.0f, (float) Math.cos(orbitSpeed[3]) * uranusDistance);
        mvStack.glScalef(uranusSize, uranusSize, uranusSize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((float) ((float) (System.currentTimeMillis() % 3600) / 20.0), 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping uranus
//-----------------------   == neptune
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[2]) * neptuneDistance, 0.0f, (float) Math.cos(orbitSpeed[2]) * neptuneDistance);
        mvStack.glScalef(neptuneSize, neptuneSize, neptuneSize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((float) ((float) (System.currentTimeMillis() % 3600) / 20.0), 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping neptune
        //-----------------------   == pluto
        mvStack.glPushMatrix();
        mvStack.glTranslatef((float) Math.sin(orbitSpeed[1]) * plutoDistance, 0.0f, (float) Math.cos(orbitSpeed[1]) * plutoDistance);
        mvStack.glScalef(plutoSize, plutoSize, plutoSize);
        mvStack.glPushMatrix();
        mvStack.glRotate(new Quaternion((System.currentTimeMillis() % 3600) / 10.0f, 0.0f, 1.0f, 0.0f));
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.glGetMvMatrixf());
        gl.glUniformMatrix4fv(proj_loc, 1, false, mvStack.glGetPMatrixf());
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.glPopMatrix();
        mvStack.glPopMatrix();// poping pluto

        // mvStack.glPopMatrix();// poping the sun!!!

    }


    private void setupVertices(GL4 gl) {
        Vertex3D[] vertices = mySphere.getVertices();
        int[] indices = mySphere.getIndices();

        float[] fvalues = new float[indices.length * 3];
        float[] tvalues = new float[indices.length * 2];
        float[] nvalues = new float[indices.length * 3];

        for (int i = 0; i < indices.length; i++) {
            fvalues[i * 3] = (float) (vertices[indices[i]]).getX();
            fvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
            fvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
            tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
            tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
            nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
            nvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getNormalY();
            nvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
        }

        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

    }


    private Matrix3D perspective(float fovy, float aspect, float n, float f) {
        float q = 1.0f / (float) Math.tan((float) Math.toRadians(0.5f * fovy));
        float A = q / aspect;
        float B = (n + f) / (n - f);
        float C = (2.0f * n * f) / (n - f);
        Matrix3D r = new Matrix3D();
        Matrix3D rt;// = new Matrix3D();
        r.setElementAt(0, 0, A);
        r.setElementAt(1, 1, q);
        r.setElementAt(2, 2, B);
        r.setElementAt(2, 3, -1.0f);
        r.setElementAt(3, 2, C);
        rt = r.transpose();
        return rt;
    }

    //    private Matrix3D BuildCameraMatrix( Vector3D rot,Vector3D pos )
//    {
//        Matrix4 matCam = new Matrix4();
//
//       /* ROTATION MATRIX */
//        Vector3D vUp      = new Vector3D(0,1,0);
//        Vector3D vForward = new Vector3D(0,0,1);
//        Vector3D vRight   = new Vector3D(1,0,0);
//
//
//        vForward = Vector3D.rotatey( vForward, rot.m_y );
//        vRight   = Vector3D.rotatey( vRight, rot.m_y );
//
//        //vUp = Vector3.cross( vRight, vForward );
//        //vUp = Vector3.normalize(vRight);
//
//        matCam.m[0][0]=vRight.m_x;
//        matCam.m[1][0]=vRight.m_y;
//        matCam.m[2][0]=vRight.m_z;
//        matCam.m[3][0]= 0;
//
//        matCam.m[0][1]=vUp.m_x;
//        matCam.m[1][1]=vUp.m_y;
//        matCam.m[2][1]=vUp.m_z;
//        matCam.m[3][1]= 0;
//
//        matCam.m[0][2]=vForward.m_x;
//        matCam.m[1][2]=vForward.m_y;
//        matCam.m[2][2]=vForward.m_z;
//        matCam.m[3][2]= 0;
//
//
//        matCam.m[0][3]=0;
//        matCam.m[1][3]=0;
//        matCam.m[2][3]=0;
//        matCam.m[3][3]= 1;
//
//       /* TRANSLATION MATRIX */
//        Matrix4 vPos = new Matrix4();
//        vPos = Matrix4.identity(vPos);
//        vPos.m[3][0] = -pos.m_x;
//        vPos.m[3][1] = -pos.m_y;
//        vPos.m[3][2] = -pos.m_z;
//
//       /* Combine Rot and Trans Matrix to create the Camera Matrix */
//        matCam = Matrix4.multiply( vPos, matCam );
//
//       /*
//        | Ux  Vx  Nx |
//        | Uy  Vy  Ny |
//        | Uz  Vz  Nz |
//
//
//        Where U is the "right" vector, V the "up" vector and N the
//        direction you are looking.
//       */
//
//        return matCam;
//    }// End BuildCameraMatrix(..)
//
//
//    //  Camera Matrix
//    //  | right.x    up.x     forward.x     0 |
//    //  | right.y    up.y     forward.y     0 |
//    //  | right.z    up.z     forward.z     0 |  ; (rotation4x4)
//    //  | 0          0            0         1 |
    private int createShaderPrograms(GLAutoDrawable drawable) {
        int[] vertCompiled = new int[1];
        int[] fragCompiled = new int[1];
        int[] linked = new int[1];

        GL4 gl = (GL4) drawable.getGL();

        String vshaderSource[] = GLSLUtils.readShaderSource("a2shaders/vert.glsl");
        String fshaderSource[] = GLSLUtils.readShaderSource("a2shaders/frag.glsl");
        int lengths[];

        int vShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
        int fShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);

        lengths = new int[vshaderSource.length];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = vshaderSource[i].length();
        }
        gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, lengths, 0);

        lengths = new int[fshaderSource.length];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = fshaderSource[i].length();
        }
        gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, lengths, 0);

        gl.glCompileShader(vShader);
        GLSLUtils.printOpenGLError(drawable);  // can use returned boolean
        gl.glGetShaderiv(vShader, GL4.GL_COMPILE_STATUS, vertCompiled, 0);
        if (vertCompiled[0] == 1) {
            System.out.println("vertex compilation success");
        } else {
            System.out.println("vertex compilation failed");
            GLSLUtils.printShaderInfoLog(drawable, vShader);
        }

        gl.glCompileShader(fShader);
        GLSLUtils.printOpenGLError(drawable);  // can use returned boolean
        gl.glGetShaderiv(fShader, GL4.GL_COMPILE_STATUS, fragCompiled, 0);
        if (fragCompiled[0] == 1) {
            System.out.println("fragment compilation success");
        } else {
            System.out.println("fragment compilation failed");
            GLSLUtils.printShaderInfoLog(drawable, fShader);
        }

        int vfprogram = gl.glCreateProgram();
        gl.glAttachShader(vfprogram, vShader);
        gl.glAttachShader(vfprogram, fShader);
        gl.glLinkProgram(vfprogram);
        GLSLUtils.printOpenGLError(drawable);
        gl.glGetProgramiv(vfprogram, GL4.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 1) {
            System.out.println("linking succeeded");
        } else {
            System.out.println("linking failed");
            GLSLUtils.printProgramInfoLog(drawable, vfprogram);
        }
        return vfprogram;
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    private float degreePerSec(float rev) {
        return (float) (System.currentTimeMillis() % 360000) / (1 / rev);
    }
    public void dispose(GLAutoDrawable drawable) {
    }

    public void actionPerformed(ActionEvent e) {
//        if ("up".equals(e.getActionCommand()) && upDown < 1) {
//            upDown += 0.1;
//        } else if ("down".equals(e.getActionCommand()) && upDown > -1) {
//            upDown -= 0.1;
//        } else if ("color".equals(e.getActionCommand())) {
//            if (g != 2) g = 2;
//            else g = 1;
//        } else
//        if ("noise".equals(e.getActionCommand())) {
//            if (n != 0.0) n = 0.0f;
//            else n = 1.0f;
//        }
//        else if ("autozoom".equals(e.getActionCommand())) {
//            autozoom = !autozoom;
//        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() < 0) {
            System.out.println("wheels up size is" + zoom);
            zoom += 1f;
        } else {
            System.out.println("wheels down size is " + zoom);
            zoom -= 1f;
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: {
                zoom += 0.1f;
                break;
            }
            case KeyEvent.VK_S: {
                zoom -= 0.1f;
                break;
            }
            case KeyEvent.VK_A: {
                strafe -= 0.1f;
                break;
            }
            case KeyEvent.VK_D: {
                strafe += 0.1f;
                break;
            }
            case KeyEvent.VK_RIGHT: {
                pan += 1f;
                break;
            }
            case KeyEvent.VK_LEFT: {
                pan -= 1f;
                break;
            }
            case KeyEvent.VK_UP: {
                pitch += 1f;
                break;
            }
            case KeyEvent.VK_DOWN: {
                pitch -= 1f;
                break;
            }
            case KeyEvent.VK_SPACE: {
                axis = !axis;
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
//
//    private class ZoomIn extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.zoom += 5.0f;
//            //System.out.println("zoom + 1.0");
//        }
//    }
//
//    private class ZoomOut extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.zoom -= 5.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class StrafeRight extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.strafe += 5.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class StrafeLeft extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.strafe -= 5.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class PanLeft extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.pan -= 1.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class PanRight extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.pan += 5.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class PitchUP extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.pitch += 1.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class PitchDown extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Ass2.pitch -= 5.0f;
//            //System.out.println("zoom - 1.0");
//        }
//    }
//
//    private class SetAxis extends AbstractAction {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            if (Ass2.axis == true) {
//                Ass2.axis = false;
//            } else if (Ass2.axis == false) {
//                Ass2.axis = true;
//            }
//            //System.out.println("zoom - 1.0");
//        }
//    }
//    private void keyMaping(){
//        int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
//        InputMap imap = this.getRootPane().getInputMap(mapName);
//        ActionMap amap = this.getRootPane().getActionMap();
//
//        KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
//        imap.put(spaceKey, "space");
//        SetAxis setaxis = new SetAxis();
//        amap.put("space", setaxis);
//
//        KeyStroke wKey = KeyStroke.getKeyStroke('w');
//        imap.put(wKey, "zoomin");
//        ZoomIn zoomin = new ZoomIn();
//        amap.put("zoomin", zoomin);
//
//        KeyStroke sKey = KeyStroke.getKeyStroke('s');
//        imap.put(sKey, "zoomout");
//        ZoomOut zoomout = new ZoomOut();
//        amap.put("zoomout", zoomout);
//
//        KeyStroke dKey = KeyStroke.getKeyStroke('d');
//        imap.put(dKey, "straferight");
//        StrafeRight straferight = new StrafeRight();
//        amap.put("straferight", straferight);
//
//        KeyStroke aKey = KeyStroke.getKeyStroke('a');
//        imap.put(aKey, "strafeleft");
//        StrafeLeft strafeleft = new StrafeLeft();
//        amap.put("strafeleft", strafeleft);
//
//        KeyStroke upKey = KeyStroke.getKeyStroke("UP");
//        imap.put(upKey, "pitchup");
//        PitchUP pitchup = new PitchUP();
//        amap.put("pitchup", pitchup);
//
//        KeyStroke downKey = KeyStroke.getKeyStroke("DOWN");
//        imap.put(downKey, "pitchdown");
//        PitchDown pitchdown = new PitchDown();
//        amap.put("pitchdown", pitchdown);
//
//        KeyStroke rightKey = KeyStroke.getKeyStroke("RIGHT");
//        imap.put(rightKey, "panright");
//        PanRight panright = new PanRight();
//        amap.put("panright", panright);
//
//        KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
//        imap.put(leftKey, "panleft");
//        PanLeft panleft = new PanLeft();
//        amap.put("panleft", panleft);
//
//    }
}
