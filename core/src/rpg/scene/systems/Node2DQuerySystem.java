package rpg.scene.systems;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import rpg.scene.Node;
import rpg.scene.components.Component;
import rpg.scene.components.Spatial2D;

import java.util.*;

public class Node2DQuerySystem extends AbstractSceneSystem {

    protected RTree<Node, Geometry> rtree = RTree.create();
    protected final Object rtreeLock = new Object();

    protected final Set<Node> dirtyNodes = new HashSet<>();
    protected final Set<Node> removedNodes = new HashSet<>();
    protected Map<Node, Geometry> nodeToGeom = new HashMap<>();

    private boolean treeDirty = false;

    public boolean doesProcessNodes() {
        return false;
    }

    @Override
    public void nodeAttached(Node n) {
        dirtyNodes.add(n);
        treeDirty = true;
    }

    @Override
    public void nodeDetached(Node n) {
        removedNodes.add(n);
        treeDirty = true;
    }

    @Override
    public void componentAttached(Component c) {
        if (c instanceof Spatial2D) {
            dirtyNodes.add(c.getParent());
            treeDirty = true;
        }
    }

    @Override
    public void componentDetached(Component c) {
        if (c instanceof Spatial2D) {
            dirtyNodes.add(c.getParent());
            treeDirty = true;
        }
    }

    public void addDirtyNode(Node n) {
        dirtyNodes.add(n);
        treeDirty = true;
    }

    protected void evaluateTreeChanges() {
        RTree<Node, Geometry> rt = rtree;

        dirtyNodes.removeAll(removedNodes);

        // Remove removed nodes
        ArrayList<Entry<Node, Geometry>> pairs = new ArrayList<>();
        for (Node n : removedNodes) {
            Geometry g = nodeToGeom.get(n);
            if (g != null) {
                pairs.add(Entry.entry(n, g));
            }
        }
        rt = rt.delete(pairs);

        // Reinsert dirty nodes
        pairs.clear();
        for (Node dirtyNode : dirtyNodes) {
            Geometry g = nodeToGeom.get(dirtyNode);
            if (g != null) {
                rt = rt.delete(dirtyNode, g);
            }
            g = geometryForNode(dirtyNode);
            pairs.add(Entry.entry(dirtyNode, g));
            nodeToGeom.put(dirtyNode, g);
        }
        rt.add(pairs);

        synchronized (rtreeLock) {
            rtree = rt;
        }

        treeDirty = false;
    }

    protected Geometry geometryForNode(Node n) {
        List<Spatial2D> spatials = n.findComponents(Spatial2D.class);
        Geometry ret;
        if (spatials.isEmpty()) {
            ret = Geometries.point(n.getTransform().getWorldPosition().x, n.getTransform().getWorldPosition().y);
        } else {
            Rectangle r = spatials.stream().map(Spatial2D::getRectangle).reduce(new Rectangle(), Rectangle::merge);
            Vector2 pos = new Vector2();
            Vector2 dim = new Vector2();
            Vector3 worldPosition = n.getTransform().getWorldPosition();
            Vector3 worldScale = n.getTransform().getWorldScale();
            Matrix4 mat = new Matrix4();

            mat.translate(worldPosition);
            mat.scale(worldScale.x, worldScale.y, worldScale.z);
            mat.translate(pos.x, pos.y, 0);

            r.getPosition(pos);
            r.getSize(dim);
            Vector3 pos3D = new Vector3(pos.x, pos.y, 0);
            Vector3 dim3D = new Vector3(dim.x, dim.y, 0);

            pos3D.mul(mat);
            dim3D.scl(worldScale);

            ret = Geometries.rectangle(pos3D.x, pos3D.y, pos3D.x + dim3D.x, pos3D.y + dim3D.y);
        }
        return ret;
    }

    public Set<Node> queryNodesInArea(Rectangle rect) {
        if (treeDirty) evaluateTreeChanges();
        HashSet<Node> ret = new HashSet<>();
        com.github.davidmoten.rtree.geometry.Rectangle rtreeRect = gdxRectToRtreeRect(rect);
        rtree.search(rtreeRect).forEach(p -> ret.add(p.value()));
        return ret;
    }

    private com.github.davidmoten.rtree.geometry.Rectangle gdxRectToRtreeRect(Rectangle r) {
        return Geometries.rectangle(r.x, r.y, r.x + r.width, r.y + r.height);
    }
}
