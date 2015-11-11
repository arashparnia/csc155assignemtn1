package a3;

/**
 * Created by arash on 10/31/2015.
 */

import com.jogamp.newt.event.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.test.junit.graph.demos.ui.UIShape;
import graphicslib3D.*;
import graphicslib3D.light.PositionalLight;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.FloatBuffer;
import java.util.Random;
import static com.jogamp.opengl.GL.*;
import com.jogamp.opengl.*;


import models.ImportedModel;
import shapes.Cube;
import utilities.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.jogamp.opengl.GL.GL_CCW;


public class Ass3 extends JFrame implements GLEventListener, ActionListener, MouseListener,MouseWheelListener,MouseMotionListener, KeyListener{
    private boolean animated = true;
    private Point mousePoint= new Point();
    public static boolean axis = false;

    private Dimension dimention = new Dimension(1000, 1000);
    private GLCanvas myCanvas;

    private int vao[] = new int[1];
    private int vbo[] = new int[200];

    private int rendering_program;
    private int rendering_program_axis;
    private int  rendering_program_no_lighting;
    private int rendering_program_blinnphong_lighting;

    private GLSLUtils util = new GLSLUtils();

    private Animator animator;

    private Random rand;

    int mv_loc ;
    int proj_loc;
    int n_location ;
    int light ;
    int clip;
    int flip_location;
    float aspect;
    Matrix3D pMat;
    MatrixStack mvStack;
    //----------------------------------------------------------------------------------------OBJECTS
    private Cube b = new Cube();
    private shapes.Ground ground = new shapes.Ground(1000);
    private shapes.Sphere mySphere = new shapes.Sphere(48);
    private shapes.Astroid rock = new shapes.Astroid(100);
    private ImportedModel grassModel,myModel;
    //------------------------------------------------------------------------------------------MATRICIES
    private Matrix3D m_matrix = new Matrix3D();
    private Matrix3D v_matrix = new Matrix3D();
    private Matrix3D mv_matrix = new Matrix3D();
    private Matrix3D proj_matrix = new Matrix3D();
    //-------------------------------------------------------------------------------------------CAMERA
    public static float zoom = 0.0f;
    public static float pan = 0.0f;
    public static float pitch = 0.0f;
    public static float strafe = 0.0f;
    private float upDown = 0.0f;
    private Vector3D u = new Vector3D(1, 0, 0);
    private Vector3D v = new Vector3D(0, 1, 0);
    private Vector3D n = new Vector3D(0, 0, 1 );
    private Vector3D xyz = new Vector3D(0,0,0);
    //------------------------------------------------------------------------------------------TEXTURE
    private TextureReader tr = new TextureReader();
    private  int sunTexture,moonTexture,grassTexture,tigerTexture;
    //-------------------------------------------------------------------------------------------MATERIALS
    private float[] rockambient = {0.0f,0.0f,0.0f,1.0f};
    private float[] rockdiffuse = {0.1f,0.1f,0.1f,1.0f};
    private float[] rockspecular =  {0.1f,0.1f,0.1f,1.0f};
    private float[] rockemission = {0.1f,0.1f,0.1f,1.0f};
    private float rockshininess = 0.0f;
    graphicslib3D.Material rockMaterial = new Material("rock",rockambient,rockdiffuse,rockspecular,rockemission,rockshininess);
    private float[] grassambient = {0.0f,0.0f,0.0f,1.0f};
    private float[] grassdiffuse = {0.1f,0.35f,0.1f,1.0f};
    private float[] grassspecular =  {0.45f,0.55f,0.45f,1.0f};
    private float[] grassemission = {0.1f,0.1f,0.1f,1.0f};
    private float grassshininess = 10f;
    graphicslib3D.Material grassMaterial = new Material("grass",grassambient,grassdiffuse,grassspecular,grassemission,grassshininess);
    private float[] sunambient = {1.0f,1.0f,1.0f,1.0f};
    private float[] sundiffuse = {1.0f,0.5f,0.0f,1.0f};
    private float[] sunspecular =  {0.0f,0.0f,0.0f,1.0f};
    private float[] sunemission = {1.0f,1.0f,1.0f,1.0f};
    private float sunshininess = 10f;
    graphicslib3D.Material sunMaterial = new Material("sun",sunambient,sundiffuse,sunspecular,sunemission,sunshininess);
    private float[] skinambient = {0.3f,0.2f,0.2f,1.0f};
    private float[] skindiffuse = {1.0f,0.9f,0.8f,1.0f};
    private float[] skinspecular =  {0.4f,0.2f,0.2f,1.0f};
    private float[] skinemission = {1.0f,1.0f,1.0f,1.0f};
    private float skinshininess = 44.8f;
    graphicslib3D.Material skinMaterial = new Material("skin",skinambient,skindiffuse,skinspecular,skinemission,skinshininess);
    //-------------------------------------------------------------------------------------------------LIGHT
    private int lights = 1;
    private PositionalLight currentLight = new PositionalLight();
    private Point3D lightLoc = new Point3D( 8f,4f,11f);
    float [] globalAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
    //------------------------------------------------------------------------------------------------------
    public Ass3() {
        setTitle("Assignment 3 CSC155");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        rand = new Random();
        add(myCanvas);
        myCanvas.addKeyListener(this);
        this.addKeyListener(this);
        myCanvas.addMouseWheelListener(this);
        myCanvas.addMouseMotionListener(this);
        myCanvas.addMouseListener(this);
        myCanvas.requestFocus();
        keyMaping();
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }
    public void init(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();

        Shader sh = new Shader();
        rendering_program_axis = sh.createShaderPrograms(drawable,"shaders/axisvert.glsl","shaders/axisfrag.glsl");
        rendering_program_no_lighting = sh.createShaderPrograms(drawable,"shaders/vert.glsl","shaders/frag.glsl");
        rendering_program_blinnphong_lighting = sh.createShaderPrograms(drawable,"shaders/blinnphongvert.glsl","shaders/blinnphongfrag.glsl");

        grassModel = new ImportedModel("Grass_02.obj");
        myModel = new ImportedModel("Tiger.obj");

        setupVertices(gl);
        xyz.setZ(25);
        xyz.setY(4);
        Matrix3D r = new Matrix3D();
        r.rotate(5, u.normalize());
        n = n.mult(r);
        v = v.mult(r);

        grassTexture    = tr.loadTexture(drawable, "textures/grass.jpg");
        sunTexture  = tr.loadTexture(drawable, "textures/sunmap.jpg");
        moonTexture  = tr.loadTexture(drawable, "textures/moonmap1k.jpg");
        tigerTexture = tr.loadTexture(drawable, "textures/tigertexture.jpg");
        animator = new Animator(myCanvas);
        Thread thread =new Thread(new Runnable(){ public void run() { animator.start();}});
        thread.start();
    }
    private Matrix3D getUVNCamera() {
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
    private void setupDisplay(GL4 gl){
        gl.glBindVertexArray(vao[0]);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mvStack.peek().inverse()).transpose().getFloatValues(),0);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glEnable(GL_CULL_FACE);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glGenerateMipmap(GL_TEXTURE_2D);
        gl.glFrontFace(GL_CCW);
        gl.glUniform1i(flip_location, 0);
    }
    public void display(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();
        gl.glClear(GL_DEPTH_BUFFER_BIT);
        FloatBuffer background = FloatBuffer.allocate(4);
        gl.glClearBufferfv(gl.GL_COLOR, 0, background);
        gl.glUseProgram(rendering_program_blinnphong_lighting);
        mv_loc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "mv_matrix");
        proj_loc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "proj_matrix");
        n_location = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "normalMat");
        light = gl.glGetUniformLocation(rendering_program_blinnphong_lighting,"l");
        flip_location = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "flipNormal");
        //clip = gl.glGetUniformLocation(rendering_program_blinnphong_lighting,"clip_plane");

        gl.glUniform1i(light,lights);
        aspect = myCanvas.getWidth() / myCanvas.getHeight();
        pMat = perspective(60.0f, aspect, 0.001f, 10000.0f);
        mvStack = new MatrixStack(100);

        // --------------------------- CAMERA

        mvStack.pushMatrix();
        mvStack.multMatrix(getUVNCamera());

//        int min = -50; int max = 50;
//        lightLoc.setX(lightLoc.getX()+rand.nextInt((max - min) + 1) + min);
//        lightLoc.setY(lightLoc.getY()+rand.nextInt((max - min) + 1) + min);
//        lightLoc.setZ(lightLoc.getZ()+rand.nextInt((max - min) + 1) + min);
        currentLight.setPosition(lightLoc);
//----------------------------------------------------------------------------------ground
        installLights(mvStack.peek(),grassMaterial, drawable);
        mvStack.pushMatrix();
        mvStack.scale(100, 1, 100);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[100]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[101]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[102]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
         setupDisplay(gl);
        gl.glBindTexture(GL_TEXTURE_2D, grassTexture);
        gl.glGenerateMipmap(GL_TEXTURE_2D);
        gl.glFrontFace(GL_CW);
        gl.glDrawArrays(GL_TRIANGLES, 0, b.getFValues().length/3);
        mvStack.popMatrix();
//------------------------------------------------------------------------------- TIGER
        installLights(mvStack.peek(),skinMaterial , drawable);
        mvStack.pushMatrix();
        mvStack.translate(5,1,10);
        mvStack.scale(0.003, 0.003, 0.003);
        mvStack.rotate(-85, 0, 1,0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[30]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[31]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[32]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
        gl.glBindTexture(GL_TEXTURE_2D, tigerTexture);
        setupDisplay(gl);
        gl.glDrawArrays(GL_TRIANGLES, 0, myModel.getIndices().length);
        mvStack.popMatrix();
        //------------------------------------------------------------------------ROCK
        gl.glEnable(gl.GL_CLIP_DISTANCE0);


        installLights(mvStack.peek(),Material.GOLD, drawable);
        mvStack.pushMatrix();
        mvStack.translate(-5,1,-5);
        mvStack.scale(4, 20, 4);
        //mvStack.scale(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
        mvStack.pushMatrix();
        mvStack.rotate(-15, 0, 0,1);
        //mvStack.rotate(-degreePerSec(0.01f),0,1,0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[20]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[21]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[22]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
        gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
        setupDisplay(gl);
        gl.glDrawArrays(GL_TRIANGLES, 0, rock.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();
        gl.glDisable(gl.GL_CLIP_DISTANCE0);



        //---------------------------------------------------------------------------GRASS
        installLights(mvStack.peek(), grassMaterial, drawable);
        mvStack.pushMatrix();
        //mvStack.translate(i, 0, j);
                //mvStack.scale(.1, .1, .1);
        mvStack.pushMatrix();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[40]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[41]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[42]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
        gl.glBindTexture(GL_TEXTURE_2D, grassTexture);
        gl.glGenerateMipmap(GL_TEXTURE_2D);
        setupDisplay(gl);
        gl.glDrawArraysInstanced(GL_TRIANGLES, 0, grassModel.getIndices().length,100);
        mvStack.popMatrix();
        mvStack.popMatrix();

        //----------------------------------------------------------------------------------light
        installLights(mvStack.peek(),sunMaterial, drawable);
        mvStack.pushMatrix();
        mvStack.translate(lightLoc.getX(),lightLoc.getY(),lightLoc.getZ());
        mvStack.scale(0.1, 0.1, 0.1);
        mvStack.pushMatrix();
        mvStack.rotate(degreePerSec(1f),1,0,0);
        mvStack.scale(0.5+rand.nextFloat()/2,0.5+rand.nextFloat()/2,0.5+rand.nextFloat()/2);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[20]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[21]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[22]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
        setupDisplay(gl);
        gl.glUniform1i(flip_location, 1);
        gl.glBindTexture(GL_TEXTURE_2D, sunTexture);
        gl.glGenerateMipmap(GL_TEXTURE_2D);
        gl.glFrontFace(GL_CCW);
        gl.glDrawArrays(GL_TRIANGLES, 0, rock.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();


        mvStack.popMatrix();
    }
    private void setupVerteciesCube(GL4 gl){
        float[] fvalues = b.getFValues();
        float[] tvalues = b.getTValues();
        float[] nvalues = b.getNValues();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[100]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[101]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[102]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);
    }//100,101,102
    private void setupVerticesSphere(GL4 gl){
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
    }//00,01,02
    private void setupVerticesGrass(GL4 gl) { // GRASS
        Vertex3D[] vertices = ground.getVertices();
        int[] indices = ground.getIndices();
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

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[10]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[11]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[12]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);


    }//10,11,12
    private void setupVerticesRock(GL4 gl) { // ROCK
        Vertex3D[] vertices = rock.getVertices();
        int[] indices = rock.getIndices();
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

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[20]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[21]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[22]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);
    }//20,21,22
    private void setupVerticesMyModel(GL4 gl){ // imported model
        Vertex3D[] vertices = myModel.getVertices();
        int[] indices = myModel.getIndices();
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

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[30]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[31]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[32]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);


    }//30,31,32
    private void setupVerticesGrassModel(GL4 gl) { // imported model
        Vertex3D[] vertices = grassModel.getVertices();
        int[] indices = grassModel.getIndices();
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

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[40]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[41]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[42]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);


    }//40,41,42
    private void setupVertices(GL4 gl) {
        gl.glGenVertexArrays(vao.length, vao,0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);
        setupVerticesSphere(gl);
        setupVerticesGrass(gl);
        setupVerticesRock(gl);
        setupVerticesMyModel(gl);
        setupVerticesGrassModel(gl);
        setupVerteciesCube(gl);
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
    private void installLights(Matrix3D v_matrix,Material material, GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();
        Material currentMaterial = material;
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
    private Matrix3D lookAt(graphicslib3D.Point3D eyeP, graphicslib3D.Point3D centerP, Vector3D upV) {	Vector3D eyeV = new Vector3D(eyeP);
        Vector3D cenV = new Vector3D(centerP);
        Vector3D f = (cenV.minus(eyeV)).normalize();
        Vector3D sV = (f.cross(upV)).normalize();
        Vector3D nU = (sV.cross(f)).normalize();

        Matrix3D l = new Matrix3D();
        l.setElementAt(0,0,sV.getX());l.setElementAt(0,1,nU.getX());l.setElementAt(0,2,-f.getX());l.setElementAt(0,3,0.0f);
        l.setElementAt(1,0,sV.getY());l.setElementAt(1,1,nU.getY());l.setElementAt(1,2,-f.getY());l.setElementAt(1,3,0.0f);
        l.setElementAt(2,0,sV.getZ());l.setElementAt(2,1,nU.getZ());l.setElementAt(2,2,-f.getZ());l.setElementAt(2,3,0.0f);
        l.setElementAt(3,0,sV.dot(eyeV.mult(-1)));
        l.setElementAt(3,1,nU.dot(eyeV.mult(-1)));
        l.setElementAt(3,2,(f.mult(-1)).dot(eyeV.mult(-1)));
        l.setElementAt(3,3,1.0f);
        return(l.transpose());
    }
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    private float degreePerSec(float rev) {
        return (float) (System.currentTimeMillis() % 360000) / (1 / rev);
    }
    public void dispose(GLAutoDrawable drawable) {}
    public static int randInt(int min, int max) {
        Random rand = new Random();
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    // ------------------------------------------------------------------------------------- CONTROLS
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() < 0)
            lightLoc.setY(lightLoc.getY()+.1);
        else
            lightLoc.setY(lightLoc.getY()-.1);
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (mousePoint.getX() > e.getX()) lightLoc.setX(lightLoc.getX()-.1);
        if (mousePoint.getX() < e.getX()) lightLoc.setX(lightLoc.getX()+.1);
        if (mousePoint.getY() > e.getY()) lightLoc.setZ(lightLoc.getZ()-.1);
        if (mousePoint.getY() < e.getY()) lightLoc.setZ(lightLoc.getZ()+.1);
        mousePoint.setLocation(e.getPoint());
    }
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) { mousePoint.setLocation(e.getPoint());}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_L: {if (lights==0)lights = 1; else lights=0;break;}
            case KeyEvent.VK_SPACE: {axis = !axis;break;}
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void actionPerformed(ActionEvent e) {}
    private void keyMaping() {
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
    private class ZoomIn extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Vector3D t = new Vector3D();
            t.setX(n.getX());t.setY(n.getY());t.setZ(n.getZ());
            t.scale(-0.5f);
            xyz = xyz.add(t);
            zoom += 1f;
        }
    }
    private class ZoomOut extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(n.getX());t.setY(n.getY());t.setZ(n.getZ());
            t.scale(0.5f);
            xyz = xyz.add(t);
            //if  (zoom > 1 )
            zoom -= 1f;
        }
    }
    private class StrafeRight extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(u.getX());t.setY(u.getY());t.setZ(u.getZ());
            t.scale(0.5f);
            xyz = xyz.add(t);
            strafe += 5f;
            //System.out.println("zoom - 1.0");
        }
    }
    private class StrafeLeft extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Vector3D t = new Vector3D();
            t.setX(u.getX());t.setY(u.getY());t.setZ(u.getZ());
            t.scale(-0.5f);
            xyz = xyz.add(t);
            strafe -= 5f;
            //System.out.println("zoom - 1.0");
        }
    }
    private class down extends AbstractAction {
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
    private class up extends AbstractAction {
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
    private class panRight extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(-10, v.normalize());
            n = n.mult(r);
            u = u.mult(r);

        }
    }
    private class panLeft extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(+10, v.normalize());
            n = n.mult(r);
            u = u.mult(r);

        }
    }
    private class pitchUp extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Matrix3D r = new Matrix3D();
            r.rotate(10, u.normalize());
            n = n.mult(r);
            v = v.mult(r);
        }
    }
    private class pitchDown extends AbstractAction {
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





