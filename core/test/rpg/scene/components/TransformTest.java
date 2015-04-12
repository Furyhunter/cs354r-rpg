package rpg.scene.components;

import com.badlogic.gdx.backends.headless.HeadlessNativesLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import org.junit.Before;
import org.junit.Test;

import static com.badlogic.gdx.math.Matrix3.M00;
import static com.badlogic.gdx.math.Matrix3.M02;
import static org.junit.Assert.assertEquals;

public class TransformTest {

    @Before
    public void setUp() {
        HeadlessNativesLoader.load();
    }

    @Test
    public void testScaleTransformApplication() {
        Matrix3 mat = new Matrix3();
        mat.translate(1, 0);

        Transform t = new Transform();
        t.setScale(new Vector2(2, 2));
        t.applyTransform(mat);
        assertEquals(mat.getValues()[0], 2, 0.0001);
    }

    @Test
    public void testTranslateTransformApplication() {
        Matrix3 mat = new Matrix3();

        Transform t = new Transform();
        t.setPosition(new Vector2(40, 0));
        t.applyTransform(mat);
        assertEquals(mat.getValues()[M02], 40, 0.0001);
    }

    @Test
    public void testRotateTransformApplication() {
        Matrix3 mat = new Matrix3();

        Transform t = new Transform();
        t.setRotation(MathUtils.PI);
        t.applyTransform(mat);
        assertEquals(mat.getValues()[M00], -1, 0.0001);
    }

    @Test
    public void testApplyMultiTransform() {
        Matrix3 mat = new Matrix3();

        Transform t1 = new Transform();
        Transform t2 = new Transform();

        t1.setScale(new Vector2(4, 4));
        t2.setPosition(new Vector2(1, 1));

        // starting at 1,1
        t1.applyTransform(mat); // coord will now be 4,4
        t2.applyTransform(mat); // coord will now be 5,5

        Vector2 res = new Vector2(1, 1).mul(mat);
        assertEquals(new Vector2(5, 5), res);

        mat = new Matrix3();

        // starting at 1,1
        t2.applyTransform(mat); // coord will now be 2,2
        t1.applyTransform(mat); // coord will now be 8,8

        assertEquals(new Vector2(8, 8), new Vector2(1, 1).mul(mat));
    }

    @Test
    public void testNonTrivialTransform() {
        // Test the order of transformations in the transform
        Matrix3 mat = new Matrix3();
        Transform t = new Transform();
        t.setPosition(new Vector2(8, 8));
        t.setScale(new Vector2(60, 60));
        t.setRotation(MathUtils.PI); // 180 degree counter-clockwise

        t.applyTransform(mat);
        assertEquals(new Vector2(-68, 68), new Vector2(1, 1).mul(mat));
    }

    @Test
    public void testApplyMultiInverseTransform() {
        // Inverse application of transform is used to get the viewpoint matrix.
        Matrix3 mat = new Matrix3();

        Transform t1 = new Transform();
        Transform t2 = new Transform();

        t1.setScale(new Vector2(4, 4));
        t2.setPosition(new Vector2(1, 1));

        // starting at 1,1
        t1.inverseApplyTransform(mat); // coord will now be .25,.25
        t2.inverseApplyTransform(mat); // coord will now be -.75, -.75

        Vector2 res = new Vector2(1, 1).mul(mat);
        assertEquals(new Vector2(-.75f, -.75f), res);

        mat = new Matrix3();

        // starting at 1,1
        t2.inverseApplyTransform(mat); // coord will now be 0,0
        t1.inverseApplyTransform(mat); // coord will now be 0,0 (no effect

        assertEquals(new Vector2(0, 0), new Vector2(1, 1).mul(mat));
    }
}