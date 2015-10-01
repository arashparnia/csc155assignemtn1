package a2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.MatrixStack;
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
    private static final float sunSize = 1.39f; //cm

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
    public static boolean axis = true;
    private Dimension dimention = new Dimension(1000, 1000);
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

    private Sphere mySphere = new Sphere(100);

    public Ass2() {
        int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap imap = this.getRootPane().getInputMap(mapName);
        ActionMap amap = this.getRootPane().getActionMap();

        KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
        imap.put(spaceKey, "space");
        SetAxis setaxis = new SetAxis();
        amap.put("space", setaxis);

        KeyStroke wKey = KeyStroke.getKeyStroke('w');
        imap.put(wKey, "zoomin");
        ZoomIn zoomin = new ZoomIn();
        amap.put("zoomin", zoomin);

        KeyStroke sKey = KeyStroke.getKeyStroke('s');
        imap.put(sKey, "zoomout");
        ZoomOut zoomout = new ZoomOut();
        amap.put("zoomout", zoomout);

        KeyStroke dKey = KeyStroke.getKeyStroke('d');
        imap.put(dKey, "straferight");
        StrafeRight straferight = new StrafeRight();
        amap.put("straferight", straferight);

        KeyStroke aKey = KeyStroke.getKeyStroke('a');
        imap.put(aKey, "strafeleft");
        StrafeLeft strafeleft = new StrafeLeft();
        amap.put("strafeleft", strafeleft);

        KeyStroke upKey = KeyStroke.getKeyStroke("UP");
        imap.put(upKey, "pitchup");
        PitchUP pitchup = new PitchUP();
        amap.put("pitchup", pitchup);

        KeyStroke downKey = KeyStroke.getKeyStroke("DOWN");
        imap.put(downKey, "pitchdown");
        PitchDown pitchdown = new PitchDown();
        amap.put("pitchdown", pitchdown);

        KeyStroke rightKey = KeyStroke.getKeyStroke("RIGHT");
        imap.put(rightKey, "panright");
        PanRight panright = new PanRight();
        amap.put("panright", panright);

        KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
        imap.put(leftKey, "panleft");
        PanLeft panleft = new PanLeft();
        amap.put("panleft", panleft);
        
        setTitle("Assignment 2 CSC155");
        setSize(dimention);
        this.addMouseWheelListener(this);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        rand = new Random();
        add(myCanvas);
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }


    public void init(GLAutoDrawable drawable) {
        this.getRootPane().requestFocus();
        GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderPrograms(drawable);
        setupVertices(gl);
        cameraX = 0.0f;
        cameraY = 0.0f;
        cameraZ = 0.0f;
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

        float aspect = myCanvas.getWidth() / myCanvas.getHeight();
        Matrix3D pMat = perspective(30.0f, aspect, 0.1f, 10000.0f);
        MatrixStack mvStack = new MatrixStack(10);
        // --------------------------- CAMERA
        mvStack.pushMatrix();
        //mvStack.rotate(-45, 1, 0, 0);
        mvStack.translate(-cameraX, -cameraY, -cameraZ);
        mvStack.translate(-strafe, 0, zoom);
        mvStack.rotate(pitch, 1, 0, 0);
        mvStack.rotate(pan, 0, 1, 0);
        //Quaterni q = new Quaternion();//ben botto
        double orbitSpeed[] = new double[15];
        for (int i = 1; i < 15; i++) {
            orbitSpeed[i] = (double) (System.currentTimeMillis() % 360000) / (1000.0 * i);
        }
        if (axis) {
            // ----------------------   == X-AXIS
            mvStack.pushMatrix();
            mvStack.translate(0, 0, 0);
            mvStack.scale(1000, 0.01, 0.01);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
            gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glEnable(GL_CULL_FACE);
            gl.glFrontFace(GL_CCW);
            gl.glEnable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
            mvStack.popMatrix();
            // ----------------------   == Y-AXIS
            mvStack.pushMatrix();
            mvStack.translate(0, 0, 0);
            mvStack.scale(0.01, 1000, 0.01);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
            gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glEnable(GL_CULL_FACE);
            gl.glFrontFace(GL_CCW);
            gl.glEnable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
            mvStack.popMatrix();
            // ----------------------   == Z-AXIS
            mvStack.pushMatrix();
            mvStack.translate(0, 0, 0);
            mvStack.scale(0.01, 0.01, 1000);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
            gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glEnable(GL_CULL_FACE);
            gl.glFrontFace(GL_CCW);
            gl.glEnable(GL_DEPTH_TEST);
            gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
            mvStack.popMatrix();
        }
        // ----------------------   == sun
        mvStack.pushMatrix();
        mvStack.translate(0, 0, 0);
        mvStack.scale(sunSize, sunSize, sunSize);
        mvStack.rotate((System.currentTimeMillis() % 3600 / 10000), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        //-----------------------   == Mercury
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[9]) * mercuryDistance, 0.0f, Math.cos(orbitSpeed[9]) * mercuryDistance);
        mvStack.scale(mercurySize, mercurySize, mercurySize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 20.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix(); //poping Mercury
        //-----------------------   == venus
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[8]) * venusDistance, 0.0f, Math.cos(orbitSpeed[8]) * venusDistance);
        mvStack.scale(venusSize, venusSize, venusSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping venus
        //-----------------------   == earth
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[7]) * earthDistance, 0.0f, Math.cos(orbitSpeed[7]) * earthDistance);
        mvStack.scale(earthSize, earthSize, earthSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        //-----------------------   == earth moon
        mvStack.pushMatrix();
        mvStack.scale(earthSize / 10, earthSize / 10, earthSize / 10);
        mvStack.translate(0.0f, Math.sin(orbitSpeed[3]) * 2, Math.cos(orbitSpeed[3]) * 2);
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 0.0, 1.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping earth
        //-----------------------   == mars
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[6]) * marsDistance, 0.0f, Math.cos(orbitSpeed[6]) * marsDistance);
        mvStack.scale(marsSize, marsSize, marsSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping mars

        //-----------------------   == jupiter
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[5]) * jupiterDistance, 0.0f, Math.cos(orbitSpeed[5]) * jupiterDistance);
        mvStack.scale(jupiterSize, jupiterSize, jupiterSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping jupiter
        //-----------------------   == saturn
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[4]) * saturnDistance, 0.0f, Math.cos(orbitSpeed[4]) * saturnDistance);
        mvStack.scale(saturnSize, saturnSize, saturnSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping saturn
        //-----------------------   == uranus
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[3]) * uranusDistance, 0.0f, Math.cos(orbitSpeed[3]) * uranusDistance);
        mvStack.scale(uranusSize, uranusSize, uranusSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping uranus
//-----------------------   == neptune
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[2]) * neptuneDistance, 0.0f, Math.cos(orbitSpeed[2]) * neptuneDistance);
        mvStack.scale(neptuneSize, neptuneSize, neptuneSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping neptune
        //-----------------------   == pluto
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(orbitSpeed[1]) * plutoDistance, 0.0f, Math.cos(orbitSpeed[1]) * plutoDistance);
        mvStack.scale(plutoSize, plutoSize, plutoSize);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping pluto


        mvStack.popMatrix();// poping the sun!!!






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
        float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
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
        gl.glGetShaderiv(fShader, GL4.GL_COMPILE_STATUS, vertCompiled, 0);
        if (vertCompiled[0] == 1) {
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

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class ZoomIn extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.zoom += 5.0f;
            //System.out.println("zoom + 1.0");
        }
    }

    private class ZoomOut extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.zoom -= 5.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class StrafeRight extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.strafe += 5.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class StrafeLeft extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.strafe -= 5.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class PanLeft extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.pan -= 1.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class PanRight extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.pan += 5.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class PitchUP extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.pitch += 1.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class PitchDown extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.pitch -= 5.0f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class SetAxis extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (Ass2.axis == true) {
                Ass2.axis = false;
            } else if (Ass2.axis == false) {
                Ass2.axis = true;
            }
            //System.out.println("zoom - 1.0");
        }
    }
}
