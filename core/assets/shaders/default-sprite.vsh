attribute vec3 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord0;

uniform mat4 u_pvmMatrix;

uniform sampler2D u_texture1;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position, 1);
    v_texCoord0 = a_texCoord0;
}
