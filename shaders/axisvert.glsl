#version 430

const vec4 vertices[6] = vec4[6]
(vec4(-1000,0.0,0.0, 1.0),
vec4( 1000.0,0.0,0.0, 1.0),
vec4( 0.0,-1000,0.0, 1.0),
vec4( 0.0,1000.0,0.0, 1.0),
vec4( 0.0,0,-1000, 1.0),
vec4( 0.0,0,1000, 1.0));

const vec4 colors[6] = vec4[6]
(vec4(1.0,0.0,0.0, 1.0),
vec4( 1.0,0.0,0.0, 1.0),
vec4( 0.0,1.0,0.0, 1.0),
vec4( 0.0,1.0,0.0, 1.0),
vec4( 0.0,0.0,1.0, 1.0),
vec4( 0.0,0.0,1.0, 1.0));

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

out vec4 vColor;

void main(void){
    gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
//  gl_Position = vertices[gl_VertexID];
    vColor = colors[gl_VertexID];
}
