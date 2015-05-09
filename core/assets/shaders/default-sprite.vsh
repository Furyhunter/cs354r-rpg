attribute vec3 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord0;

uniform mat4 u_pvmMatrix;

uniform sampler2D u_texture1;

uniform mat3 u_texCoord0Transform;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position, 1);
    vec3 texC = u_texCoord0Transform * vec3(a_texCoord0, 1);
    texC /= texC.z;
    v_texCoord0 = vec2(texC.xy);
}
