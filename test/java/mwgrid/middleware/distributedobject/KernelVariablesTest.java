package mwgrid.middleware.distributedobject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class KernelVariablesTest {
    /**
     * Constructor
     */
    public KernelVariablesTest() {
        // Constructor
    }
    
    /**
     * Test reflection on KernelVariables
     */
    @Test
    public void testReflection() {
        assertFalse(KernelVariables.CLASS.getClass().isAnnotationPresent(
            PublicVariable.class));
        try {
            assertTrue(KernelVariables.CLASS.getClass().getField(
                KernelVariables.CLASS.name()).isAnnotationPresent(
                PublicVariable.class));
        } catch (SecurityException e) {
            fail();
        } catch (NoSuchFieldException e) {
            fail();
        }
        assertTrue(KernelVariables.CLASS.getClass().isEnum());
    }
}
