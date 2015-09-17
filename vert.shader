#version 430
layout (location = 0) in vec4 offset;
layout (location = 1) in float scale;
out vec4 vs_color;
void main(void) {
    const vec4 vertices[3] = vec4[3](
        vec4( 0.25 * scale , -0.25 * scale, 0.5 * scale, 1.0) ,
        vec4(-0.25 * scale , -0.25 * scale, 0.5 * scale, 1.0) ,
        vec4( 0.25 * scale , 0.25 * scale , 0.5 * scale, 1.0)) ;

    const vec4 colors[3] = vec4[3](
        vec4( 1.0, 0.0, 0.0, 1.0),
        vec4( 0.0, 1.0, 0.0, 1.0),
        vec4( 0.0, 0.0, 1.0, 1.0));

    gl_Position = vertices[gl_VertexID] + offset;
    vs_color = colors[gl_VertexID];
}
