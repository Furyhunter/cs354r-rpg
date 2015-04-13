package rpg.scene;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class RenderItem {
    private ShaderProgram shader;
    private Texture textureToBind;
    private Mesh mesh;
    private Matrix4 modelMatrix;
    private int primitiveType;

    public RenderItem(ShaderProgram shader, Texture textureToBind, Mesh mesh, Matrix4 modelMatrix, int primitiveType) {
        this.shader = shader;
        this.textureToBind = textureToBind;
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

    public Texture getTextureToBind() {
        return textureToBind;
    }

    public void setTextureToBind(Texture textureToBind) {
        this.textureToBind = textureToBind;
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
}
