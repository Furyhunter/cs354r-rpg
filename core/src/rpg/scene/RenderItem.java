package rpg.scene;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import java.util.Arrays;

public class RenderItem {
    private ShaderProgram shader;
    private Texture[] texturesToBind = new Texture[8];
    private Mesh mesh;
    private Matrix4 modelMatrix;
    private int primitiveType;
    private UniformSetFunction uniformSetFunction;
    private boolean absoluteModelPosition = false;

    private boolean transparent = false;

    public UniformSetFunction getUniformSetFunction() {
        return uniformSetFunction;
    }

    public void setUniformSetFunction(UniformSetFunction uniformSetFunction) {
        this.uniformSetFunction = uniformSetFunction;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    @FunctionalInterface
    public interface UniformSetFunction {
        /**
         * Set the uniforms on the shader.
         *
         * @param shader shader to set uniforms on
         */
        void setUniforms(ShaderProgram shader);
    }

    public RenderItem() {

    }

    public RenderItem(ShaderProgram shader, Texture[] texturesToBind, Mesh mesh, Matrix4 modelMatrix, int primitiveType) {
        this.shader = shader;
        if (texturesToBind.length == 8) {
            this.texturesToBind = texturesToBind;
        } else {
            this.texturesToBind = Arrays.copyOf(texturesToBind, 8);
        }
        this.mesh = mesh;
        this.modelMatrix = modelMatrix;
        this.primitiveType = primitiveType;
    }

    public Matrix4 getModelMatrix() {
        return modelMatrix;
    }

    public void setModelMatrix(Matrix4 modelMatrix) {
        this.modelMatrix = modelMatrix;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public Texture getTextureToBind(int index) {
        return texturesToBind[index];
    }

    public void setTextureToBind(int index, Texture textureToBind) {
        this.texturesToBind[index] = textureToBind;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public void setShader(ShaderProgram shader) {
        this.shader = shader;
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(int primitiveType) {
        this.primitiveType = primitiveType;
    }

    public boolean isAbsoluteModelPosition() {
        return absoluteModelPosition;
    }

    public void setAbsoluteModelPosition(boolean absoluteModelPosition) {
        this.absoluteModelPosition = absoluteModelPosition;
    }
}
