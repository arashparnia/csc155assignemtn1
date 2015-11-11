#version 430
layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
layout (location = 2) in vec2 texPos;

out vec2 tc;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;

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
uniform int l;
uniform int flipNormal;
// four parameters are definition of a plane: Ax + By + Cz + D
vec4 clip_plane = vec4(0.0, 1.0, 0.0, 2.2);

void main(void)
{
if (flipNormal==1) varyingNormal = -varyingNormal;
 gl_ClipDistance[0] = dot(vec4(vertPos,1.0), clip_plane);

    if (gl_InstanceID > 0){
        float x = (1.3 * sin( gl_InstanceID )) * (gl_InstanceID/5);
        float y = +cos(gl_InstanceID) * 1.2;
        float z = (1.3* cos(gl_InstanceID )) * (gl_InstanceID/5);
        vec3 pos = vertPos + vec3(x,y,z);

        varyingVertPos = (mv_matrix * vec4(pos,1.0)).xyz;
        varyingLightDir = light.position - varyingVertPos;
        varyingNormal = (normalMat * vec4(vertNormal,1.0)).xyz;
        varyingHalfVector =normalize(normalize(varyingLightDir)+ normalize(-varyingVertPos)).xyz;
        gl_Position = proj_matrix * mv_matrix * vec4(pos,1.0);
        tc = texPos;
    } else {
        varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	    varyingLightDir = light.position - varyingVertPos;
	    varyingNormal = (normalMat * vec4(vertNormal,1.0)).xyz;
	    varyingHalfVector =normalize(normalize(varyingLightDir) + normalize(-varyingVertPos)).xyz;
	    gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
	    tc = texPos;
	}
}

