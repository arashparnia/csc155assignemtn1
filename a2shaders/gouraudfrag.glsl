#version 430
in vec2 tc;
in vec4 varyingColor;
out vec4 fragColor;

//  uniforms match those in the vertex shader,
//  but aren�t used directly in this fragment shader

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
uniform sampler2D s;


void main(void)
{	fragColor = texture2D(s,tc);//*0.5 +  varyingColor*0.5 ;
}
