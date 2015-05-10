attribute vec3 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_pvmMatrix;

uniform mat3 u_texCoord0Matrix;
uniform sampler2D u_texture1;

varying float v_index;
varying vec2 v_texCoord0;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position.xy, 0, 1);
    v_index = a_position.z;
    vec3 tv = u_texCoord0Matrix * vec3(a_texCoord0, 1);
    tv /= tv.z;
    v_texCoord0 = tv.xy;
}
