package rpg.scene.components;

import com.badlogic.gdx.backends.headless.HeadlessNativesLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.junit.Before;
import org.junit.Test;
import rpg.scene.Node;
import rpg.scene.Scene;

import static com.badlogic.gdx.math.Matrix4.M00;
import static com.badlogic.gdx.math.Matrix4.M03;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformTest {

    @Before
    public void setUp() {
        HeadlessNativesLoader.load();
    }

    @Test
    public void testScaleTransformApplication() {
        Matrix4 mat = new Matrix4();
        mat.translate(1, 0, 0);

        Transform t = new Transform();
        t.setScale(new Vector3(2, 2, 2));
        t.applyTransform(mat);
        assertEquals(mat.getValues()[M00], 2, 0.0001);
    }

    @Test
    public void testTranslateTransformApplication() {
        Matrix4 mat = new Matrix4();

        Transform t = new Transform();
        t.setPosition(new Vector3(40, 0, 0));
        t.applyTransform(mat);
        assertEquals(mat.getValues()[M03], 40, 0.0001);
    }

    @Test
    public void testRotateTransformApplication() {
        Matrix4 mat = new Matrix4();

        Transform t = new Transform();
        t.setRotation(new Quaternion(Vector3.Z, 180));
        t.applyTransform(mat);
        assertEquals(mat.getValues()[M00], -1, 0.0001);
    }

    @Test
    public void testApplyMultiTransform() {
        Matrix4 mat = new Matrix4();

        Transform t1 = new Transform();
        Transform t2 = new Transform();

        t1.setScale(new Vector3(4, 4, 4));
        t2.setPosition(new Vector3(1, 1, 1));

        // starting at 1,1
        t1.applyTransform(mat); // coord will now be 4,4,4
        t2.applyTransform(mat); // coord will now be 5,5,5

        Vector3 res = new Vector3(1, 1, 1).mul(mat);
        assertEquals(new Vector3(5, 5, 5), res);

        mat = new Matrix4();

        // starting at 1,1
        t2.applyTransform(mat); // coord will now be 2,2,2
        t1.applyTransform(mat); // coord will now be 8,8,8

        assertEquals(new Vector3(8, 8, 8), new Vector3(1, 1, 1).mul(mat));
    }

    @Test
    public void testNonTrivialTransform() {
        // Test the order of transformations in the transform
        Matrix4 mat = new Matrix4();
        Transform t = new Transform();
        t.setPosition(new Vector3(8, 8, 8));
        t.setScale(new Vector3(60, 60, 60));
        t.setRotation(new Quaternion(Vector3.Z, 180)); // 180 degree counter-clockwise

        t.applyTransform(mat);
        Vector3 actual = new Vector3(1, 1, 1).mul(mat);
        Vector3 expected = new Vector3(-52, -52, 68);
        assertTrue("Transforms not applied correctly, expected " + expected + " but actual " + actual, expected.epsilonEquals(actual, 0.00001f));
    }

    @Test
    public void testApplyMultiInverseTransform() {
        // Inverse application of transform is used to get the viewpoint matrix.
        Matrix4 mat = new Matrix4();

        Transform t1 = new Transform();
        Transform t2 = new Transform();

        t1.setScale(new Vector3(4, 4, 4));
        t2.setPosition(new Vector3(1, 1, 1));

        // starting at 1,1
        t1.inverseApplyTransform(mat); // coord will now be .25,.25, .25
        t2.inverseApplyTransform(mat); // coord will now be -.75, -.75, -.75

        Vector3 res = new Vector3(1, 1, 1).mul(mat);
        assertEquals(new Vector3(-.75f, -.75f, -.75f), res);

        mat = new Matrix4();

        // starting at 1,1
        t2.inverseApplyTransform(mat); // coord will now be 0,0,0
        t1.inverseApplyTransform(mat); // coord will now be 0,0,0 (no effect

        assertEquals(new Vector3(0, 0, 0), new Vector3(1, 1, 1).mul(mat));
    }

    @Test
    public void testGetWorldTransform() {
        Scene s = new Scene();
        Node n1 = new Node();
        Node n2 = new Node();
        s.getRoot().addChild(n1);
        n1.addChild(n2);

        n1.getTransform().translate(4, 0, 0);

        assertEquals(4, n2.getTransform().getWorldPosition().x, 0.00001);

        n1.getTransform().translate(-4, 0, 0);
        n1.getTransform().scale(4);
        n2.getTransform().translate(2, 0, 0); // 8

        assertEquals(8, n2.getTransform().getWorldPosition().x, 0.00001);

        n1.getTransform().rotate(Vector3.Z, 180);
        assertEquals(-8, n2.getTransform().getWorldPosition().x, 0.00001);
    }
}