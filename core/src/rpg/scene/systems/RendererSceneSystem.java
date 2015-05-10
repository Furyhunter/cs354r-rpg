package rpg.scene.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import rpg.scene.Node;
import rpg.scene.RenderItem;
import rpg.scene.components.Renderable;
import rpg.scene.components.Transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RendererSceneSystem extends AbstractSceneSystem {

    private Matrix4 projectionMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();

    private List<RenderItem> renderItems;

    private Node viewTarget = null;

    private float elapsedTime = 0;

    @Override
    public void beginProcessing() {
        renderItems = new ArrayList<>();

        viewMatrix = new Matrix4();
        if (viewTarget != null) {
            Transform t = viewTarget.getTransform();
            viewMatrix.translate(0, 0, -10);
            viewMatrix.rotate(Vector3.X, -30);

            viewMatrix.translate(t.getWorldPosition().cpy().scl(-1));
            viewMatrix.rotate(t.getWorldRotation().conjugate());
            viewMatrix.scale(1.f / t.getWorldScale().x, 1.f / t.getWorldScale().y, 1.f / t.getWorldScale().z);
        }
    }

    @Override
    public void processNode(Node n, float deltaTime) {
        // Get all renderable components on this node.
        List<Renderable> renderables = n.findComponents(Renderable.class);
        Transform t = n.getTransform();

        // Map Renderable to RenderItem with render() method, to get a RenderItem list
        List<RenderItem> items = renderables.stream().map(r -> {
            RenderItem i = r.render();
            if (i == null) return null;

            // Allow the incoming RenderItem have its own model matrix.
            if (!i.isAbsoluteModelPosition()) {
                Matrix4 model = new Matrix4();
                model.translate(t.getWorldPosition());
                model.rotate(t.getWorldRotation());
                model.scale(t.getWorldScale().x, t.getWorldScale().y, t.getWorldScale().z);

                if (i.getModelMatrix() == null) {
                    i.setModelMatrix(model);
                } else {
                    i.getModelMatrix().mulLeft(model);
                }
            } else if (i.getModelMatrix() == null) {
                i.setModelMatrix(new Matrix4());
            }
            return i;
        }).filter(r -> r != null).collect(Collectors.toList());

        // Append the collecting list of render items.
        renderItems.addAll(items);
    }

    @Override
    public void endProcessing() {
        // Sort the render item list by GL texture handle (to minimize texture rebinds)
        renderItems.sort(Comparator.comparingInt(r -> ((RenderItem) r).isTransparent() ? 1000000 : -1000000)
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(0) == null ? -10000 : ((RenderItem) r).getTextureToBind(0).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(1) == null ? -10000 : ((RenderItem) r).getTextureToBind(1).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(2) == null ? -10000 : ((RenderItem) r).getTextureToBind(2).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(3) == null ? -10000 : ((RenderItem) r).getTextureToBind(3).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(4) == null ? -10000 : ((RenderItem) r).getTextureToBind(4).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(5) == null ? -10000 : ((RenderItem) r).getTextureToBind(5).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(6) == null ? -10000 : ((RenderItem) r).getTextureToBind(6).getTextureObjectHandle())
                        .thenComparingInt(r -> ((RenderItem) r).getTextureToBind(7) == null ? -10000 : ((RenderItem) r).getTextureToBind(7).getTextureObjectHandle())
        );

        // Combine projection and view
        Matrix4 projview = new Matrix4(viewMatrix).mulLeft(projectionMatrix);

        // Draw the draw list
        Texture currentlyBoundTexture = null;
        boolean transparencySet = false;
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE3);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE4);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE5);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE6);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE7);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        for (RenderItem r : renderItems) {
            if (r.getTextureToBind(0) != currentlyBoundTexture) {
                if (r.getTextureToBind(0) != null) r.getTextureToBind(0).bind(0);
                if (r.getTextureToBind(1) != null) r.getTextureToBind(1).bind(1);
                if (r.getTextureToBind(2) != null) r.getTextureToBind(2).bind(2);
                if (r.getTextureToBind(3) != null) r.getTextureToBind(3).bind(3);
                if (r.getTextureToBind(4) != null) r.getTextureToBind(4).bind(4);
                if (r.getTextureToBind(5) != null) r.getTextureToBind(5).bind(5);
                if (r.getTextureToBind(6) != null) r.getTextureToBind(6).bind(6);
                if (r.getTextureToBind(7) != null) r.getTextureToBind(7).bind(7);
                currentlyBoundTexture = r.getTextureToBind(0);
            }
            if (r.isTransparent() && !transparencySet) {
                Gdx.gl20.glEnable(GL20.GL_BLEND);
                Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                transparencySet = true;
            } else if (!r.isTransparent() && transparencySet) {
                Gdx.gl20.glDisable(GL20.GL_BLEND);
            }
            Matrix4 pvm = new Matrix4(projview).mul(r.getModelMatrix());

            ShaderProgram s = r.getShader();
            s.begin();
            if (s.getUniformLocation("u_pMatrix") != -1) s.setUniformMatrix("u_pMatrix", projectionMatrix);
            if (s.getUniformLocation("u_vmMatrix") != -1)
                s.setUniformMatrix("u_vmMatrix", viewMatrix.cpy().mul(r.getModelMatrix()));
            if (s.getUniformLocation("u_pvmMatrix") != -1) s.setUniformMatrix("u_pvmMatrix", pvm);
            if (s.getUniformLocation("u_time") != -1) s.setUniformf("u_time", elapsedTime);
            if (s.getUniformLocation("u_texture1") != -1) s.setUniformi("u_texture1", 0);
            if (s.getUniformLocation("u_texture2") != -1) s.setUniformi("u_texture2", 1);
            if (s.getUniformLocation("u_texture3") != -1) s.setUniformi("u_texture3", 2);
            if (s.getUniformLocation("u_texture4") != -1) s.setUniformi("u_texture4", 3);
            if (s.getUniformLocation("u_texture5") != -1) s.setUniformi("u_texture5", 4);
            if (s.getUniformLocation("u_texture6") != -1) s.setUniformi("u_texture6", 5);
            if (s.getUniformLocation("u_texture7") != -1) s.setUniformi("u_texture7", 6);
            if (s.getUniformLocation("u_texture8") != -1) s.setUniformi("u_texture8", 7);
            if (r.getUniformSetFunction() != null) r.getUniformSetFunction().setUniforms(s);
            r.getMesh().render(s, r.getPrimitiveType());
            s.end();
        }

        Gdx.gl20.glDisable(GL20.GL_BLEND);

        renderItems.clear();

        elapsedTime += Gdx.graphics.getRawDeltaTime();
    }

    public void setViewTarget(Node viewTarget) {
        this.viewTarget = viewTarget;
    }

    public Node getViewTarget() {
        return viewTarget;
    }

    public void setProjectionMatrix(Matrix4 mat) {
        projectionMatrix = mat;
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }
}
