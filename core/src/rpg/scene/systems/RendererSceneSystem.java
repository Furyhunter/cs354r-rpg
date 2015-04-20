package rpg.scene.systems;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import rpg.scene.Node;
import rpg.scene.RenderItem;
import rpg.scene.components.Renderable;
import rpg.scene.components.Transform;

import java.util.*;
import java.util.stream.Collectors;

public class RendererSceneSystem extends AbstractSceneSystem {

    private Matrix4 projectionMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();

    private Deque<Matrix4> modelMatrixStack = new ArrayDeque<>();

    private List<RenderItem> renderItems;

    private Node viewTarget = null;

    @Override
    public void beginProcessing() {
        renderItems = new ArrayList<>();
        modelMatrixStack.clear();
        modelMatrixStack.push(new Matrix4());

        viewMatrix = new Matrix4();
        if (viewTarget != null) {
            // check that either we have a parent, or that we are the scene root
            // note: this could result in infinite loop/dumb behavior if the scene graph is not actually a DAG
            // we do not verify integrity at all... oh well!
            if (viewTarget.getParent() != null || (viewTarget.getScene() != null)) {
                Node visitor = viewTarget;
                Deque<Node> visitors = new ArrayDeque<>();

                // find our way to the top of the tree, stacking nodes along the way
                while (visitor != null && visitor.getNetworkID() != Node.ROOT_NODE_NETWORK_ID) {
                    visitors.push(visitor);
                    visitor = visitor.getParent();
                }

                // go through from root to our node, applying transforms
                while (!visitors.isEmpty()) {
                    Node n = visitors.pop();
                    n.getTransform().inverseApplyTransform(viewMatrix);
                }
            }
        }
    }

    @Override
    public void enterNode(Node n, float deltaTime) {
        Transform t = n.getTransform();
        Matrix4 newModel = new Matrix4(modelMatrixStack.peek());
        t.applyTransform(newModel);
        modelMatrixStack.push(newModel);
    }

    @Override
    public void processNode(Node n, float deltaTime) {
        // Get all renderable components on this node.
        List<Renderable> renderables = n.findComponents(Renderable.class);


        // Map Renderable to RenderItem with render() method, to get a RenderItem list
        List<RenderItem> items = renderables.stream().map(r -> {
            RenderItem i = r.render();
            // Allow the incoming RenderItem have its own model matrix.
            if (i.getModelMatrix() == null) {
                i.setModelMatrix(modelMatrixStack.peek());
            } else {
                i.getModelMatrix().mulLeft(modelMatrixStack.peek());
            }
            return i;
        }).collect(Collectors.toList());

        // Append the collecting list of render items.
        renderItems.addAll(items);
    }

    @Override
    public void exitNode(Node n, float deltaTime) {
        modelMatrixStack.pop();
    }

    @Override
    public void endProcessing() {
        // Sort the render item list by GL texture handle (to minimize texture rebinds)
        renderItems.sort(Comparator
                .comparingInt(r -> r.getTextureToBind() == null ? 0 : r.getTextureToBind().getTextureObjectHandle()));

        // Combine projection and view
        Matrix4 projview = new Matrix4(viewMatrix).mulLeft(projectionMatrix);

        // Draw the draw list
        Texture currentlyBoundTexture = null;
        for (RenderItem r : renderItems) {
            if (r.getTextureToBind() != currentlyBoundTexture) {
                r.getTextureToBind().bind(0);
                currentlyBoundTexture = r.getTextureToBind();
            }
            Matrix4 pvm = new Matrix4(projview).mul(r.getModelMatrix());

            ShaderProgram s = r.getShader();
            s.begin();
            s.setUniformMatrix("u_pvmMatrix", pvm);
            if (r.getTextureToBind() != null) {
                s.setUniformi("m_texture", 0);
            }
            r.getMesh().render(s, r.getPrimitiveType());
            s.end();
        }


        renderItems.clear();
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
