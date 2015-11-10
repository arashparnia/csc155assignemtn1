#version 430

in vec2 tc;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;

out vec4 fragColor;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};

struct Material
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform int l;
layout (binding=0)  uniform sampler2D s;


void main(void)
{	// normalize the light, normal, and view vectors:


	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
//
//    vec3 nn = (sin(V.x),V.y,cos(V.z));
//    N = dot(N,nn);
// float a = 0.5; // a controls depth of bumps
//  float b = 200.0; // b controls width of bumps
//  float x = varyingVertPos.x;
//  float y = varyingVertPos.y;
//  float z = varyingVertPos.z;
//  N.x = varyingNormal.x*0.8 + a*sin(b*x) * cos(b*x) ;
// // N.y = varyingNormal.y*0.8 + a*sin(b*y);
//  N.z = varyingNormal.z*0.8 + a*cos(b*z)* sin(b*z);
//  N = normalize(N);


	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);

	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = varyingHalfVector;

	// compute ADS contributions (per pixel):
	if (l==0){
	fragColor =  texture2D(s,tc) ;//+ 0.8 * (globalAmbient * material.ambient);
	}else{
	fragColor = 0.2 * texture2D(s,tc) + 0.8 * (globalAmbient * material.ambient
	+ light.ambient * material.ambient
	+ light.diffuse * material.diffuse * max(cosTheta,0.0)
	+ light.specular  * material.specular *
		pow(max(dot(H,N),0.0), material.shininess*3.0) );
	}
}
