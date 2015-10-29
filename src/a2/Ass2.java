// todo : create seperate shader for sun

package a2;

import com.jogamp.newt.event.*;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import graphicslib3D.*;
import graphicslib3D.shape.Sphere;
import graphicslib3D.shape.*;
import graphicslib3D.light.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;


public class Ass2 extends JFrame implements GLEventListener, ActionListener, MouseWheelListener, KeyListener, com.jogamp.newt.event.KeyListener {

    //scaled at 10 billion
    private static final float sunSize = 13.39f; //cm

    private static final float mercurySize = 0.05f; //cm
    private static final float mercuryDistance = 15f; //57,909,000
    //private float MercuryOrbit = 5.8 //m

    private static final float venusSize = 0.12f; //cm
    private static final float venusDistance = 18f; //108,200,000
    //private float VenusOrbit = 10.8 //m

    private static final float earthSize = 0.13f; //cm
    private static final float earthDistance = 24f; //149,600,000
    //private float EarthOrbit = 15.0 //m

    private static final float marsSize = 0.07f; //cm
    private static final float marsDistance = 32f; //227,940,000
    //private float MarsOrbit = 22.8 //m

    private static final float jupiterSize = 1.43f;//1.43 cm
    private static final float jupiterDistance = 57f; //778,400,000
    //private float JupiterOrbit = 77.8 //m

    private static final float saturnSize = 1.2f;//1.2 cm
    private static final float saturnDistance = 77f; // 1,423,600,000
    //private float SaturnOrbit = 142.4 //m

    private static final float uranusSize = 0.5f;//0.51 cm
    private static final float uranusDistance = 87f; // 2,867,000,000
    //private float UranusOrbit =  286.7 //m

    private static final float neptuneSize = 0.49f;//0.49 cm
    private static final float neptuneDistance = 97f; //4,488,400,000
    //private float NeptuneOrbit = 448.9 //m

    private static final float plutoSize = 0.02f; // 0.02 cm
    private static final float plutoDistance = 590f; // 5,909,600,000
    //private float PlutoOrbit = 591.0 //m


    public static float zoom = 0.0f;
    public static float pan = 0.0f;
    public static float pitch = 0.0f;
    public static float strafe = 0.0f;
    public static boolean axis = false;
    private Dimension dimention = new Dimension(1000, 1000);
    private GLCanvas myCanvas;

    private int vao[] = new int[1];
    private int vbo[] = new int[20];
    private float x[] = new float[100];
    private float y[] = new float[100];
    private float z[] = new float[100];
    private float r[] = new float[100];
    private float s[] = new float[100];
    private float scalefactor = 0;

    private int rendering_program;
    private int rendering_program_axis;
   private int  rendering_program_no_lighting;
    private int rendering_program_blinnphong_lighting;

    private int VAO[] = new int[1];
    private GLSLUtils util = new GLSLUtils();
    private float upDown = 0.0f;
    private boolean animated = true;
    private Animator animator;

    private Random rand;

    private Astroid mySphere = new Astroid(20);
    private Ring ring = new Ring(20, 40, 48);
    private TextureReader tr = new TextureReader();
    private int sunTexture;
    private int solarflareTexture;
    private int mercuryTexture;
    private int venusTexture;
    private int earthTexture;
    private int moonTexture;
    private int marsTexture;
    private int jupiterTexture;
    private int saturnTexture;
    private int saturnRingTexture;
    private int saturnRingPattern;
    private int uranusTexture;
    private int uranusRingTexture;
    private int neptuneTexture;
    private int plutoTexture;
    private int skydomeTexture;
    private int[] samplers = new int[2];
    private Vector3D u = new Vector3D(1, 0, 0);
    private Vector3D v = new Vector3D(0, 1, 0);
    private Vector3D n = new Vector3D(0, 0, 1 );
    private Vector3D xyz = new Vector3D(0,0,0);
    private int lookatcamera = 0;

    private Matrix3D m_matrix = new Matrix3D();
    private Matrix3D v_matrix = new Matrix3D();
    private Matrix3D mv_matrix = new Matrix3D();
    private Matrix3D proj_matrix = new Matrix3D();

    graphicslib3D.Material thisMaterial = Material.SILVER;
    private PositionalLight currentLight = new PositionalLight();
    private Point3D lightLoc = new Point3D(0f, 0f, 0f);
    float [] globalAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };


    public Ass2()
    {
        setTitle("Assignment 2 CSC155");
        setSize(dimention);
        this.addMouseWheelListener(this);

        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        rand = new Random();
        add(myCanvas);
        this.addKeyListener(this);
        keyMaping();
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }


    public void init(GLAutoDrawable drawable)
    {
        //this.getRootPane().requestFocus();
        GL4 gl = (GL4) drawable.getGL();
        //rendering_program = createShaderPrograms(drawable,"a2shaders/vert.glsl","a2shaders/frag.glsl");
        Shader sh = new Shader();
        rendering_program_axis = sh.createShaderPrograms(drawable,"a2shaders/axisvert.glsl","a2shaders/axisfrag.glsl");
        rendering_program_no_lighting = sh.createShaderPrograms(drawable,"a2shaders/vert.glsl","a2shaders/frag.glsl");
        rendering_program_blinnphong_lighting = sh.createShaderPrograms(drawable,"a2shaders/blinnphongvert.glsl","a2shaders/blinnphongfrag.glsl");


        setupVertices(gl);
        upDown = 10;
        zoom = -100;
        xyz.setZ(20);
        xyz.setX(-15);

        // could be handleed directly with layout in frag shader
        int tx_loc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "s");
        gl.glGenSamplers(1, samplers, 0);
        gl.glBindSampler(0, tx_loc);
        sunTexture = tr.loadTexture(drawable, "textures/sunmap.jpg");
        mercuryTexture = tr.loadTexture(drawable, "textures/mercurymap.jpg");
        venusTexture = tr.loadTexture(drawable, "textures/venusmap.jpg");
        earthTexture = tr.loadTexture(drawable, "textures/earthmap1k.jpg");
        moonTexture = tr.loadTexture(drawable, "textures/moonmap1k.jpg");
        marsTexture = tr.loadTexture(drawable, "textures/marsmap1k.jpg");
        jupiterTexture = tr.loadTexture(drawable, "textures/jupitermap.jpg");
        saturnTexture = tr.loadTexture(drawable, "textures/saturnmap.jpg");
        saturnRingTexture = tr.loadTexture(drawable, "textures/saturnringcolor.jpg");
        saturnRingPattern = tr.loadTexture(drawable, "textures/saturnringpattern.jpg");
        uranusTexture = tr.loadTexture(drawable, "textures/uranusmap.jpg");
        uranusRingTexture = tr.loadTexture(drawable, "textures/uranusringcolour.jpg");
        neptuneTexture = tr.loadTexture(drawable, "textures/neptunemap.jpg");
        plutoTexture = tr.loadTexture(drawable, "textures/plutomap1k.jpg");
        solarflareTexture = tr.loadTexture(drawable, "textures/solarflare.png");
        skydomeTexture    = tr.loadTexture(drawable, "textures/milkywayTexture.jpg");
        animator = new Animator(myCanvas);
        Thread thread =
                new Thread(new Runnable(){ public void run() { animator.start();}});
        thread.start();
    }

    private Matrix3D getUVNCamera()
    {
        Matrix3D uvnMatrix = new Matrix3D();
        uvnMatrix.setRow(0, u);
        uvnMatrix.setRow(1, v);
        uvnMatrix.setRow(2, n);
        uvnMatrix.setRow(3, new Vector3D(0, 0, 0, 1));
        Matrix3D t = new Matrix3D();
        t.setRow(0, new Vector3D(1, 0, 0, -xyz.getX()));
        t.setRow(1, new Vector3D(0, 1, 0, -xyz.getY()));
        t.setRow(2, new Vector3D(0, 0, 1, -xyz.getZ()));
        t.setRow(3, new Vector3D(0, 0, 0, 1));
        uvnMatrix.concatenate(t);
        return uvnMatrix;
    }

    private void setupGl(GL4 gl)
    {

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[2]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glActiveTexture(GL_TEXTURE0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glDepthFunc(GL_EQUAL);


    }

    public void display(GLAutoDrawable drawable)
    {
        GL4 gl = (GL4) drawable.getGL();

        gl.glClear(GL_DEPTH_BUFFER_BIT);
        FloatBuffer background = FloatBuffer.allocate(4);
        gl.glClearBufferfv(GL_COLOR, 0, background);

        gl.glUseProgram(rendering_program_blinnphong_lighting);


        int mv_loc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "mv_matrix");
        int proj_loc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "proj_matrix");
        int n_location = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "normalMat");

        double orbitSpeed[] = new double[15];
        float ii = 8.0f;
        for (int i = 0; i < 15; i++)
        {
            ii-=0.3f;
            orbitSpeed[i] = (double) (System.currentTimeMillis() % 360000) / (1000.0 * ii);
        }
        float aspect = myCanvas.getWidth() / myCanvas.getHeight();
        Matrix3D pMat = perspective(60.0f, aspect, 0.001f, 10000.0f);
        Matrix3D vMat = new Matrix3D();
        vMat.translate(0,0,0);

        MatrixStack mvStack = new MatrixStack(100);

        // push view matrix onto the stack

        // --------------------------- CAMERA
        mvStack.pushMatrix();


        if (lookatcamera == 0) {
            mvStack.pushMatrix();
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 1)
        {
            xyz.setX((float) Math.sin(orbitSpeed[9]) * mercuryDistance);
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[9]) * mercuryDistance );
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 2)
        {
            xyz.setX((float) Math.sin(orbitSpeed[8]) * venusDistance );
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[8]) * venusDistance );
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 3)
        {
            Vector3D c = new Vector3D(
            (float) Math.sin(orbitSpeed[7]) * earthDistance ,
            0,
            (float) Math.cos(orbitSpeed[7]) * earthDistance );
            xyz = c;
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 4)
        {
            xyz.setX((float) Math.sin(orbitSpeed[6]) * marsDistance );
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[6]) * marsDistance );
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 5)
        {
            xyz.setX((float) Math.sin(orbitSpeed[5]) * jupiterDistance + zoom);
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[5]) * jupiterDistance + zoom);
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 6)
        {
            xyz.setX((float) Math.sin(orbitSpeed[4]) * saturnDistance  + zoom);
            xyz.setY(0+ zoom);
            xyz.setZ((float) Math.cos(orbitSpeed[4]) * saturnDistance  + zoom);
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 7)
        {
            xyz.setX((float) Math.sin(orbitSpeed[3]) * uranusDistance + zoom);
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[3]) * uranusDistance  + zoom);
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 8)
        {
            xyz.setX((float) Math.sin(orbitSpeed[2]) * neptuneDistance + zoom);
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[2]) * neptuneDistance  + zoom);
            mvStack.multMatrix(getUVNCamera());
        }
        else if (lookatcamera == 9)
        {
            xyz.setX((float) Math.sin(orbitSpeed[1]) * plutoDistance );
            xyz.setY(0);
            xyz.setZ((float) Math.cos(orbitSpeed[1]) * plutoDistance  );
            mvStack.multMatrix(getUVNCamera());
        }
        //lightLoc.setX(-xyz.getX()); lightLoc.setY(-xyz.getY()); lightLoc.setZ(-xyz.getZ());
        //currentLight.setPosition(lightLoc);
        installLights(mvStack.peek(), drawable);
        //-----------------------   == universe
        mvStack.pushMatrix();
        mvStack.scale(9999, 9999, 9999);
        mvStack.pushMatrix();
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glFrontFace(GL_CCW);
        gl.glBindTexture(GL_TEXTURE_2D, skydomeTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping

        // ----------------------   == sun
        mvStack.pushMatrix();
        mvStack.translate(0, 0, 0);
        mvStack.pushMatrix();
        mvStack.scale(sunSize, sunSize, sunSize);
        mvStack.rotate(degreePerSec(0.005f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false, mvStack.peek().getFloatValues(), 0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, sunTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);

        //--------------------------------- solar flare \

        if (System.currentTimeMillis() % 100 == 0 )
        {
            scalefactor = 0;
            for (int i = 0; i < 10; i++) {
                x[i] = (rand.nextFloat());
                y[i] = (rand.nextFloat());
                z[i] = (rand.nextFloat());
                r[i] = (float) Math.sqrt(x[i] * x[i] + y[i] * y[i] + z[i] * z[i]);
                x[i] /= r[i];
                y[i] /= r[i];
                z[i] /= r[i];
                if (rand.nextBoolean()) x[i] = -x[i];
                if (rand.nextBoolean()) y[i] = -y[i];
                if (rand.nextBoolean()) z[i] = -z[i];
                s[i] = 0.01f + (rand.nextFloat() / 10);

            }
        }
        scalefactor += 0.05;

        for (int i = 0; i < 10; i++)
        {

            mvStack.pushMatrix();
            mvStack.translate(x[i], y[i], z[i]);
            //mvStack.rotate(20,1,1,1);
            float ss = s[i] + (float) Math.sin(scalefactor) / 20;
            mvStack.scale(ss, ss, ss);
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
            gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
            gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);

            setupGl(gl);
            gl.glBindTexture(GL_TEXTURE_2D, solarflareTexture);
            //gl.glCullFace(GL_CCW);
            // Enable blending
            gl.glEnable(GL_BLEND);
            //gl.glBlendEquation(GL_FUNC_ADD);
            //gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            //gl.glBlendFunc(GL_ONE, GL_SRC_COLOR);
            gl.glBlendFunc(GL_ONE, GL_ONE);
            gl.glDrawArrays(GL_TRIANGLES, 0, ring.getIndices().length);
            gl.glDisable(GL_BLEND);
            mvStack.popMatrix();// poping solar flare
        }

        mvStack.popMatrix();// poping sun rotation
        //mvStack.popMatrix();
        //-----------------------   == Mercury
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[9]) * mercuryDistance, 0.0f, (float) Math.cos(orbitSpeed[9]) * mercuryDistance);
        mvStack.scale(mercurySize, mercurySize, mercurySize);
        mvStack.pushMatrix();
        mvStack.rotate(-degreePerSec(0.005f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, mercuryTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix(); //poping Mercury
        //-----------------------   == venus
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[8]) * venusDistance, 0.0f, (float) Math.cos(orbitSpeed[8]) * venusDistance);
        mvStack.scale(venusSize, venusSize, venusSize);
        mvStack.pushMatrix();
        mvStack.rotate(-degreePerSec(0.005f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, venusTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping venus
        //-----------------------   == earth
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[7]) * earthDistance, 0.0f, (float) Math.cos(orbitSpeed[7]) * earthDistance);
        mvStack.scale(earthSize, earthSize, earthSize);
        mvStack.pushMatrix();
        mvStack.rotate(degreePerSec(0.1f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        //-----------------------   == earth moon
        mvStack.pushMatrix();
        mvStack.translate((float) Math.cos(orbitSpeed[9]) * 2, 0f, (float) Math.sin(orbitSpeed[9]) * 2);
        mvStack.scale(earthSize / 1.1f, earthSize / 1.1f, earthSize / 1.1f);
        mvStack.rotate(degreePerSec(0.01f), 0.0f, 1.0f, 0.0f);

        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping earth


        //-----------------------   == mars
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[6]) * marsDistance, 0.0f, (float) Math.cos(orbitSpeed[6]) * marsDistance);
        mvStack.scale(marsSize * 2, marsSize * 2, marsSize * 2);
        mvStack.pushMatrix();
        mvStack.rotate(degreePerSec(0.01f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, marsTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping mars


        //-----------------------   == jupiter
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[5]) * jupiterDistance, 0.0f, (float) Math.cos(orbitSpeed[5]) * jupiterDistance);
        mvStack.scale(jupiterSize, jupiterSize, jupiterSize);
        mvStack.pushMatrix();
        mvStack.rotate(degreePerSec(0.01f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, jupiterTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping jupiter
        //-----------------------   == saturn
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[4]) * saturnDistance, 0.0f, (float) Math.cos(orbitSpeed[4]) * saturnDistance);
        mvStack.scale(saturnSize, saturnSize, saturnSize);
        mvStack.pushMatrix();
        mvStack.rotate(degreePerSec(0.01f), 0.0f, 1.0f, 0.0f);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, saturnTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        //-----------------------   == saturn ring

        mvStack.pushMatrix();
        mvStack.scale(saturnSize / 10, saturnSize / 10, saturnSize / 10);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[3]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[4]);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, saturnRingPattern);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_BLEND);
        //gl.glBlendEquation(GL_FUNC_ADD);
        //gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.glBlendFunc(GL_ONE, GL_ONE);
        // gl.glBlendFunc(GL_ONE, GL_ONE);
        gl.glDrawArrays(GL_TRIANGLES, 0, ring.getIndices().length);
        gl.glDisable(GL_BLEND);
        mvStack.popMatrix();

        mvStack.popMatrix();
        mvStack.popMatrix();// poping saturn
        //-----------------------   == uranus
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[3]) * uranusDistance, 0.0f, (float) Math.cos(orbitSpeed[3]) * uranusDistance);
        mvStack.scale(uranusSize, uranusSize, uranusSize);
        mvStack.pushMatrix();
        mvStack.rotate(-degreePerSec(0.05f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, uranusTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping uranus
        //-----------------------   == neptune
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[2]) * neptuneDistance, 0.0f, (float) Math.cos(orbitSpeed[2]) * neptuneDistance);
        mvStack.scale(neptuneSize, neptuneSize, neptuneSize);
        mvStack.pushMatrix();
        mvStack.rotate(-degreePerSec(0.005f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, neptuneTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping neptune
        //-----------------------   == pluto
        mvStack.pushMatrix();
        mvStack.translate((float) Math.sin(orbitSpeed[1]) * plutoDistance, 0.0f, (float) Math.cos(orbitSpeed[1]) * plutoDistance);
        mvStack.scale(plutoSize, plutoSize, plutoSize);
        mvStack.pushMatrix();
        mvStack.rotate(-degreePerSec(0.005f), 0, 1, 0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mv_matrix.inverse()).transpose().getFloatValues(),0);
        setupGl(gl);
        gl.glBindTexture(GL_TEXTURE_2D, plutoTexture);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();// poping pluto


        if (axis) {
            gl.glUseProgram(rendering_program_axis);
            mv_loc = gl.glGetUniformLocation(rendering_program_axis, "mv_matrix");
            proj_loc = gl.glGetUniformLocation(rendering_program_axis, "proj_matrix");
            gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
            gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
            gl.glDrawArrays(GL_LINES, 0, 6);
        }
        if (lookatcamera == 0) mvStack.popMatrix();// poping the camera!!!
        mvStack.popMatrix();
    }


    private void setupVertices(GL4 gl)
    {
        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);
        {
            Vertex3D[] vertices = mySphere.getVertices();
            int[] indices = mySphere.getIndices();

            float[] fvalues = new float[indices.length * 3];
            float[] tvalues = new float[indices.length * 2];
            float[] nvalues = new float[indices.length * 3];

            for (int i = 0; i < indices.length; i++)
            {
                fvalues[i * 3] = (float) (vertices[indices[i]]).getX();
                fvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
                fvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
                tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
                tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
                nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
                nvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getNormalY();
                nvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
            }



            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
            FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
            FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[2]);
            FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);


        }
        {
            Vertex3D[] vertices = ring.getVertices();
            int[] indices = ring.getIndices();

            float[] fvalues = new float[indices.length * 3];
            float[] tvalues = new float[indices.length * 2];


            for (int i = 0; i < indices.length; i++)
            {
                fvalues[i * 3] = (float) (vertices[indices[i]]).getX();
                fvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
                fvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
                tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
                tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();

            }


            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[3]);
            FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[4]);
            FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);


        }

    }


    private Matrix3D perspective(float fovy, float aspect, float n, float f)
    {
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
    private void installLights(Matrix3D v_matrix, GLAutoDrawable drawable)
    {	GL4 gl = (GL4) drawable.getGL();

        Material currentMaterial = thisMaterial;
        Point3D lightP = currentLight.getPosition();
        Point3D lightPv = lightP.mult(v_matrix);
        float [] currLightPos =  new float[] { (float) lightPv.getX(), (float) lightPv.getY(), (float) lightPv.getZ() };

        // set the current globalAmbient settings
        int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, globalAmbLoc, 1, globalAmbient, 0);

        // get the locations of the light and material fields in the shader
        int ambLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "light.ambient");
        int diffLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "light.diffuse");
        int specLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "light.specular");
        int posLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "light.position");
        int MambLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "material.ambient");
        int MdiffLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "material.diffuse");
        int MspecLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "material.specular");
        int MshiLoc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "material.shininess");

        //  set the uniform light and material values in the shader
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, ambLoc, 1, currentLight.getAmbient(), 0);
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, diffLoc, 1, currentLight.getDiffuse(), 0);
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, specLoc, 1, currentLight.getSpecular(), 0);
        gl.glProgramUniform3fv(rendering_program_blinnphong_lighting, posLoc, 1, currLightPos, 0);
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, MambLoc, 1, currentMaterial.getAmbient(), 0);
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
        gl.glProgramUniform4fv(rendering_program_blinnphong_lighting, MspecLoc, 1, currentMaterial.getSpecular(), 0);
        gl.glProgramUniform1f(rendering_program_blinnphong_lighting, MshiLoc, currentMaterial.getShininess());
    }



    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
    }

    private float degreePerSec(float rev) {
        return (float) (System.currentTimeMillis() % 360000) / (1 / rev);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void actionPerformed(ActionEvent e)
    {
//        if ("up".equals(e.getActionCommand()) && upDown < 1) {
//            upDown += 0.1;
//        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e.getUnitsToScroll() < 0) {
            Vector3D t = new Vector3D();
            t.setX(n.getX());t.setY(n.getY());t.setZ(n.getZ());
            t.scale(0.5f);
            xyz = xyz.add(t);
            zoom += 1f;
            System.out.println("wheels down size is " + xyz.getZ());
        } else {
            Vector3D t = new Vector3D();
            t.setX(n.getX());t.setY(n.getY());t.setZ(n.getZ());
            t.scale(-0.5f);
            xyz = xyz.add(t);
             if  (zoom > 1 )zoom -= 1f;
            System.out.println("wheels down size is " + xyz.getZ());
        }
    }


    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_SPACE:
            {
                axis = !axis;
                break;
            }
            case KeyEvent.VK_0:
            {
                lookatcamera = 0;
                zoom = 10;
                strafe = 0;
                upDown = 5;
                break;
            }
            case KeyEvent.VK_1:
            {
                lookatcamera = 1;
                zoom = 5f;
                strafe = 0;
                break;
            }
            case KeyEvent.VK_2:
            {
                lookatcamera = 2;
                zoom = 5f;
                strafe = 0;
                break;
            }
            case KeyEvent.VK_3:
            {
                lookatcamera = 3;
                zoom = 5;
                strafe = 0;
                break;
            }
            case KeyEvent.VK_4:
            {
                lookatcamera = 4;
                zoom = 5f;
                strafe = 0;
                break;
            }
            case KeyEvent.VK_5:
            {
                lookatcamera = 5;
                zoom = 5f;
                break;
            }
            case KeyEvent.VK_6:
            {
                lookatcamera = 6;
                zoom = 5f;
                break;
            }
            case KeyEvent.VK_7:
            {
                lookatcamera = 7;
                zoom = 5f;
                break;
            }
            case KeyEvent.VK_8:
            {
                lookatcamera = 8;
                zoom = 5f;
                break;
            }
            case KeyEvent.VK_9:
            {
                lookatcamera = 9;
                zoom = 5f;
                strafe = 0;
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }

    private void keyMaping()
    {
        int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap imap = this.getRootPane().getInputMap(mapName);
        ActionMap amap = this.getRootPane().getActionMap();



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

        KeyStroke eKey = KeyStroke.getKeyStroke('e');
        imap.put(eKey, "upkey");
        up up = new up();
        amap.put("upkey", up);

        KeyStroke qKey = KeyStroke.getKeyStroke('q');
        imap.put(qKey, "downkey");
        down down = new down();
        amap.put("downkey", down);

        KeyStroke upKey = KeyStroke.getKeyStroke("UP");
        imap.put(upKey, "pitchup");
        pitchUp pitchup = new pitchUp();
        amap.put("pitchup", pitchup);

        KeyStroke downKey = KeyStroke.getKeyStroke("DOWN");
        imap.put(downKey, "pitchdown");
        pitchDown pitchdown = new pitchDown();
        amap.put("pitchdown", pitchdown);

        KeyStroke rightKey = KeyStroke.getKeyStroke("RIGHT");
        imap.put(rightKey, "panright");
        panRight panright = new panRight();
        amap.put("panright", panright);

        KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
        imap.put(leftKey, "panleft");
        panLeft panleft = new panLeft();
        amap.put("panleft", panleft);

    }

    @Override
    public void keyPressed(com.jogamp.newt.event.KeyEvent keyEvent) {
        System.out.print("key pressed jogl");
    }

    @Override
    public void keyReleased(com.jogamp.newt.event.KeyEvent keyEvent) {

    }


    private class ZoomIn extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            Vector3D t = new Vector3D();
            t.setX(n.getX());t.setY(n.getY());t.setZ(n.getZ());
            t.scale(-0.5f);
            xyz = xyz.add(t);
            zoom += 1f;
        }
    }


    private class ZoomOut extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(n.getX());t.setY(n.getY());t.setZ(n.getZ());
            t.scale(0.5f);
            xyz = xyz.add(t);
            if  (zoom > 1 )zoom -= 1f;
        }
    }

    private class StrafeRight extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(u.getX());t.setY(u.getY());t.setZ(u.getZ());
            t.scale(0.5f);
            xyz = xyz.add(t);
            Ass2.strafe -= 5f;
            Ass2.strafe += 5f;
            //System.out.println("zoom - 1.0");
        }
    }

    private class StrafeLeft extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(u.getX());t.setY(u.getY());t.setZ(u.getZ());
            t.scale(-0.5f);
            xyz = xyz.add(t);
            Ass2.strafe -= 5f;
            //System.out.println("zoom - 1.0");
        }
    }
    private class down extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(v.getX());t.setY(v.getY());t.setZ(v.getZ());
            t.scale(-0.5f);
            xyz = xyz.add(t);
            upDown -= 5f;
        }
    }

    private class up extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(v.getX());t.setY(v.getY());t.setZ(v.getZ());
            t.scale(0.5f);
            xyz = xyz.add(t);
            upDown += 5f;

        }
    }

    private class panRight extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(-10, v.normalize());
            n = n.mult(r);
            u = u.mult(r);

        }
    }
    private class panLeft extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(+10, v.normalize());
            n = n.mult(r);
            u = u.mult(r);

        }
    }

    private class pitchUp extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(10, u.normalize());
            n = n.mult(r);
            v = v.mult(r);
        }
    }

    private class pitchDown extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(-10, u.normalize());
            n = n.mult(r);
            v = v.mult(r);
        }
    }



}





