package shapes;

/**
 * Created by arash on 10/15/2015.
 */

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import graphicslib3D.Vertex3D;

import java.util.Random;

import static java.lang.Math.*;

public class Ring{
    private int numVertices, numIndices, prec=8,inner = 3,outter = 5;
    private int[] indices;
    private Vertex3D[] vertices;
    private Random rand = new Random();


    public Ring(int inner,int outter,int p)
    {	prec = p;
        this.inner = inner ;
        this.outter =outter ;
        InitRing();
    }

    public void InitRing() {
        numVertices = (prec + 1) * (prec + 1);
        numIndices = (prec + 1) * (prec) * 6;
        vertices = new Vertex3D[numVertices];
        indices = new int[numIndices];

        for (int i = 0; i < numVertices; i++) {
            vertices[i] = new Vertex3D();
        }

        // calculate triangle vertices
        for (int i = inner; i <= outter; i++) {
                for (int j = 0; j <= prec; j++) {
                    // calculate vertex location

                    float y = 0;
                    float x = -(float) cos(toRadians(j * 360.0 / prec)) * (float) abs(cos(asin(y))) * i;
                    float z = (float) sin(toRadians(j * 360.0f / (float) (prec))) * (float) abs(cos(asin(y))) * i;
                    vertices[i * (prec + 1) + j].setLocation(new Point3D(x, y, z));


                    // calculate texture coordinates
                    vertices[i * (prec + 1) + j].setS((float) j / (float) (prec));
                    vertices[i * (prec + 1) + j].setT((float) i / (float) (prec));

                    // calculate normal vector
                    vertices[i * (prec + 1) + j].setNormal(new Vector3D(vertices[i * (prec + 1) + j].getLocation()));

            }
        }
        // calculate triangle indices
        for (int i = inner; i <= outter; i++) {
            for (int j = 0; j <= prec; j++) {
                    indices[6 * (i * (prec + 1) + j) + 0] = i * (prec + 1) + j;
                    indices[6 * (i * (prec + 1) + j) + 1] = i * (prec + 1) + j + 1;
                    indices[6 * (i * (prec + 1) + j) + 2] = (i + 1) * (prec + 1) + j;
                    indices[6 * (i * (prec + 1) + j) + 3] = i * (prec + 1) + j + 1;
                    indices[6 * (i * (prec + 1) + j) + 4] = (i + 1) * (prec + 1) + j + 1;
                    indices[6 * (i * (prec + 1) + j) + 5] = (i + 1) * (prec + 1) + j;

            }
        }
    }
    public int[] getIndices()
    {	return indices;
    }

    public Vertex3D[] getVertices()
    {	return vertices;
    }
}