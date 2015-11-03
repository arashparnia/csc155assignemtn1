package shapes;

/**
 * Created by arash on 10/23/2015.
 */

import com.jogamp.graph.geom.Triangle;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import graphicslib3D.Vertex3D;

import java.util.Random;
import java.util.Set;

import static java.lang.Math.*;

public class Astroid
{
    private int numVertices, numIndices, prec=8;
    private int[] indices;
    private Vertex3D[] vertices;
    private Random random = new Random();

    public static int randInt(int min, int max) {
        Random rand = new Random();
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public Astroid(int p)
    {	prec = p;
        InitAstroid();
    }

    public void InitAstroid()
    {	numVertices = (prec+1) * (prec+1);
        numIndices = (prec+1) * (prec) * 6;
        vertices = new Vertex3D[numVertices];
        indices = new int[numIndices];

        for (int i=0; i<numVertices; i++) { vertices[i] = new Vertex3D(); }

        // calculate triangle vertices
        for (int i=0; i<=prec; i++) {
            for (int j=0; j<=prec; j++) {
                // calculate vertex location
            float y = (float)cos(toRadians(180-i*180/prec));
            float x = (-(float)cos(toRadians(j*360.0/prec))*(float)abs(cos(asin(y))));
            float z = ((float)sin(toRadians(j*360.0f/prec))*(float)abs(cos(asin(y))));
            vertices[i*(prec+1)+j].setLocation(new Point3D(x,y,z));

            // calculate tangent vector
            float nextY = (float)cos(toRadians(180-(i+1)*180/prec));
            float nextX = -(float)cos(toRadians((j+1)*360.0/prec))*(float)abs(cos(asin(nextY)));
            float nextZ = (float)sin(toRadians((j+1)*360.0f/(float)(prec)))*(float)abs(cos(asin(nextY)));
            Vector3D thisPt = new Vector3D(x,y,z);
            Vector3D nextPt = new Vector3D(nextX,nextY,nextZ);
            Vector3D tangent = nextPt.minus(thisPt);
            vertices[i*(prec+1)+j].setTangent(tangent);

            // calculate texture coordinates
            vertices[i*(prec+1)+j].setS((float)j/(float)(prec));
            vertices[i*(prec+1)+j].setT((float)i/(float)(prec));

            // calculate normal vector
           vertices[i*(prec+1)+j].setNormal(new Vector3D(vertices[i*(prec+1)+j].getLocation()));
        }	}

        //vertex manipulation
        int radius = 5;
        int bumps = 1000;
        float depth = -0.01f;
        for (int k = 0;k<bumps;k++) {
            float d = depth;
            if (random.nextBoolean()) d *= -1;
            carve(randInt(radius, prec-radius), randInt(radius, prec-radius), radius, d);
        }
        //calculate normlas
        for (int i=0; i<=prec; i++) {
            for (int j=0; j<=prec; j++) {

                Vertex3D from = vertices[(i%prec)*(prec+1)+(j%prec)+1];
                Vertex3D to1 = vertices[(i%prec)*(prec+1)+(j%prec)];
                Vertex3D to2 = vertices[((i%prec)+1)*(prec+1)+(j%prec)];
                Vector3D U = new Vector3D(from.minus(to1));
                Vector3D V = new Vector3D(from.minus(to2));
                Vector3D normal = V.cross(U);
                vertices[(i%prec)*(prec+1)+(j%prec)].setNormal(normal);
            }}

        // calculate triangle indices
        for(int i=0; i<=prec; i++)
        {	for(int j=0; j<=prec; j++)
        {	indices[6*((i%prec)*(prec+1)+(j%prec))+0] = (i%prec)*(prec+1)+(j%prec);
            indices[6*((i%prec)*(prec+1)+(j%prec))+1] = (i%prec)*(prec+1)+(j%prec)+1;
            indices[6*((i%prec)*(prec+1)+(j%prec))+2] = ((i%prec)+1)*(prec+1)+(j%prec);
            indices[6*((i%prec)*(prec+1)+(j%prec))+3] = (i%prec)*(prec+1)+(j%prec)+1;
            indices[6*((i%prec)*(prec+1)+(j%prec))+4] = ((i%prec)+1)*(prec+1)+(j%prec)+1;
            indices[6*((i%prec)*(prec+1)+(j%prec))+5] = ((i%prec)+1)*(prec+1)+(j%prec);
        }	}	}

    public int[] getIndices()
    {	return indices;
    }

    public Vertex3D[] getVertices()
    {	return vertices;
    }
    public void carve (int centerx,int centery, int delta, float depth){
        int centy =centery;
        int centx = centerx;
        int deltay = delta;
        float deltab = depth;
        int ne = centx;int nw = centx;
        int se = centx;int sw = centx;
        float b =1;
        for (int i=centy+deltay; i>=centy; i--) {
            ne--;b =1;
            //north east
            for (int j=ne; j<centx; j++) {
                b+=deltab;
                float x = (float) vertices[i * (prec + 1) + j].getX();
                float y = (float) vertices[i * (prec + 1) + j].getY();
                float z = (float) vertices[i * (prec + 1) + j].getZ();
                vertices[i * (prec + 1) + j].setX(x * b);
                vertices[i * (prec + 1) + j].setY(y * b);
                vertices[i * (prec + 1) + j].setZ(z * b);
            }
            nw++;b=1;
            for (int j=nw; j>=centx; j--) {
                b+=deltab;
                float x = (float) vertices[i * (prec + 1) + j].getX();
                float y = (float) vertices[i * (prec + 1) + j].getY();
                float z = (float) vertices[i * (prec + 1) + j].getZ();
                vertices[i * (prec + 1) + j].setX(x * b);
                vertices[i * (prec + 1) + j].setY(y * b);
                vertices[i * (prec + 1) + j].setZ(z * b);
            }
        }
        for (int i=centy-deltay; i<centy; i++) {
            se--;b =1;
            //north east
            for (int j=se; j<centx; j++) {
                b+=deltab;
                float x = (float) vertices[i * (prec + 1) + j].getX();
                float y = (float) vertices[i * (prec + 1) + j].getY();
                float z = (float) vertices[i * (prec + 1) + j].getZ();
                vertices[i * (prec + 1) + j].setX(x * b);
                vertices[i * (prec + 1) + j].setY(y);
                vertices[i * (prec + 1) + j].setZ(z * b);
            }
            sw++;b=1;
            for (int j=sw; j>=centx; j--) {
                b+=deltab;
                float x = (float) vertices[i * (prec + 1) + j].getX();
                float y = (float) vertices[i * (prec + 1) + j].getY();
                float z = (float) vertices[i * (prec + 1) + j].getZ();
                vertices[i * (prec + 1) + j].setX(x * b);
                vertices[i * (prec + 1) + j].setY(y);
                vertices[i * (prec + 1) + j].setZ(z * b);
            }
        }


    }
}