attribute vec3 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord0;

uniform mat4 u_pMatrix;
uniform mat4 u_vmMatrix;

uniform sampler2D u_texture1;

uniform mat3 u_texCoord0Transform;
uniform float u_billboard;
uniform mat4 u_spriteRotScale;
uniform vec2 u_spriteOffset;

void main() {
    if (u_billboard == 1.0) {
        gl_Position = u_pMatrix * (u_vmMatrix * vec4(u_spriteOffset.x, 0.0, u_spriteOffset.y, 1.0) + (u_spriteRotScale * vec4(a_position, 0)));
    } else {
        gl_Position = u_pMatrix * u_vmMatrix * vec4(a_position, 1);
    }
    vec3 texC = u_texCoord0Transform * vec3(a_texCoord0, 1);
    texC /= texC.z;
    v_texCoord0 = vec2(texC.xy);
}
