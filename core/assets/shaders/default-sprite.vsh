#version 150

in vec3 position;
in vec2 texcoord;

out vec2 m_texcoord;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform sampler2D m_texture;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);
    m_texcoord = texcoord;
}