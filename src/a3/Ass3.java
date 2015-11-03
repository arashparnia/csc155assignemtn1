package a3;

/**
 * Created by arash on 10/31/2015.
 */
// todo : create seperate shader for sun

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.*;
import graphicslib3D.light.PositionalLight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;


import shapes.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.jogamp.opengl.GL.GL_CCW;


public class Ass3 extends JFrame implements GLEventListener, ActionListener, MouseWheelListener, KeyListener, com.jogamp.newt.event.KeyListener {



    public static float zoom = 0.0f;
    public static float pan = 0.0f;
    public static float pitch = 0.0f;
    public static float strafe = 0.0f;
    public static boolean axis = false;
    private Dimension dimention = new Dimension(1000, 1000);
    private GLCanvas myCanvas;

    private int vao[] = new int[1];
    private int vbo[] = new int[50];
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

    private GLSLUtils util = new GLSLUtils();
    private float upDown = 0.0f;
    private boolean animated = true;
    private Animator animator;

    private Random rand;

    private shapes.Ground ground = new shapes.Ground(1000);
    private shapes.Sphere mySphere = new Sphere(48);
    private shapes.Astroid rock = new shapes.Astroid(100);
    private Ring ring = new Ring(20, 40, 48);
    private TextureReader tr = new TextureReader();

    private  int moonTexture;
    private  int grassTexture;
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

    private PositionalLight currentLight = new PositionalLight();
    private Point3D lightLoc = new Point3D( 10f,1000f,10f);
    float [] globalAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };


    public Ass3()
    {
        setTitle("Assignment 3 CSC155");
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
        rendering_program_axis = sh.createShaderPrograms(drawable,"shaders/axisvert.glsl","shaders/axisfrag.glsl");
        rendering_program_no_lighting = sh.createShaderPrograms(drawable,"shaders/vert.glsl","shaders/frag.glsl");
        rendering_program_blinnphong_lighting = sh.createShaderPrograms(drawable,"shaders/blinnphongvert.glsl","shaders/blinnphongfrag.glsl");


        setupVertices(gl);
        xyz.setZ(120);
        xyz.setY(30);
        Matrix3D r = new Matrix3D();
        r.rotate(-15, u.normalize());
        n = n.mult(r);
        v = v.mult(r);

        // could be handleed directly with layout in frag shader
        int tx_loc = gl.glGetUniformLocation(rendering_program_blinnphong_lighting, "s");
        gl.glGenSamplers(1, samplers, 0);
        gl.glBindSampler(0, tx_loc);
        grassTexture    = tr.loadTexture(drawable, "textures/grass.jpg");
        moonTexture  = tr.loadTexture(drawable, "textures/moonmap1k.jpg");
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

//        double orbitSpeed[] = new double[15];
//        float ii = 8.0f;
//        for (int i = 0; i < 15; i++)
//        {
//            ii-=0.3f;
//            orbitSpeed[i] = (double) (System.currentTimeMillis() % 360000) / (1000.0 * ii);
//        }
        float aspect = myCanvas.getWidth() / myCanvas.getHeight();
        Matrix3D pMat = perspective(60.0f, aspect, 0.001f, 10000.0f);
        //Matrix3D vMat = new Matrix3D();
        //vMat.translate(0,0,0);

        MatrixStack mvStack = new MatrixStack(100);

        // push view matrix onto the stack

        // --------------------------- CAMERA

        mvStack.pushMatrix();
        mvStack.multMatrix(getUVNCamera());

        currentLight.setPosition(lightLoc);
        //mvStack.rotate(-degreePerSec(0.01f),0,1,0);

        //ROCK
        installLights(mvStack.peek(),rockMaterial, drawable);
        mvStack.pushMatrix();
       // mvStack.translate(-500,0,500);
        mvStack.scale(50, 50, 50);
        mvStack.pushMatrix();
        mvStack.rotate(115, 1, 1,1);
        gl.glBindVertexArray(vao[0]);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(n_location, 1, false,(mvStack.peek().inverse()).transpose().getFloatValues(),0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[20]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[21]);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[22]);
        gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glActiveTexture(GL_TEXTURE0);

        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glDepthFunc(GL_EQUAL);
        gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
        gl.glGenerateMipmap(GL_TEXTURE_2D);
        gl.glDrawArrays(GL_TRIANGLES, 0, rock.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();

        //GRASS
        for(int i = -500;i<500;i+=100) {
            for (int j = -500; j < 500; j += 100) {
                installLights(mvStack.peek(), grassMaterial, drawable);
                mvStack.pushMatrix();
                mvStack.translate(i, 0, j);
                mvStack.scale(.1, .1, .1);
                mvStack.pushMatrix();
                //mvStack.rotate(-degreePerSec(0.01f), 0, 1,0);
                gl.glBindVertexArray(vao[0]);
                gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
                gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
                gl.glUniformMatrix4fv(n_location, 1, false, (mvStack.peek().inverse()).transpose().getFloatValues(), 0);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[10]);
                gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
                gl.glEnableVertexAttribArray(0);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[11]);
                gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
                gl.glEnableVertexAttribArray(2);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[12]);
                gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 0, 0);
                gl.glEnableVertexAttribArray(1);
                gl.glActiveTexture(GL_TEXTURE0);

                gl.glEnable(GL_CULL_FACE);
                gl.glFrontFace(GL_CCW);
                gl.glEnable(GL_DEPTH_TEST);
                //gl.glDepthFunc(GL_EQUAL);
                gl.glBindTexture(GL_TEXTURE_2D, grassTexture);
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                gl.glDrawArrays(GL_TRIANGLES, 0, ground.getIndices().length);

                mvStack.popMatrix();
                mvStack.popMatrix();
            }
        }
        mvStack.popMatrix();
    }


    private void setupVertices(GL4 gl)
    {
        gl.glGenVertexArrays(vao.length, vao,0);
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

        { // GRASS
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


        }

        { // ROCK
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
    private void installLights(Matrix3D v_matrix,Material material, GLAutoDrawable drawable)
    {	GL4 gl = (GL4) drawable.getGL();

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



    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    private float degreePerSec(float rev) {
        return (float) (System.currentTimeMillis() % 360000) / (1 / rev);
    }

    public void dispose(GLAutoDrawable drawable) {}

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
            //if  (zoom > 1 )
            zoom -= 1f;
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
            //if  (zoom > 1 )
            zoom -= 1f;
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
            strafe += 5f;
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
            strafe -= 5f;
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





