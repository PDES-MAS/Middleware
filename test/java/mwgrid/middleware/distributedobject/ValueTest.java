package mwgrid.middleware.distributedobject;

import org.junit.Assert;
import org.junit.Test;

public class ValueTest {
    /**
     * Empty constructor
     */
    public ValueTest() {
        // Empty constructor
    }
    
    /**
     * Test creating values for basic types
     */
    @Test
    public void testBasicValues() {
        Integer integer = new Integer(1);
        final Value<Integer> integerValue = new Value<Integer>(integer);
        Assert.assertEquals(integerValue.get(), integer);
        Assert.assertEquals(integerValue.getType(), Integer.class);
        Assert.assertEquals(integerValue.get(), new Integer(1));
        Assert.assertTrue(integerValue.get().intValue() == 1);
        integer = new Integer(2);
        Assert.assertTrue(integerValue.get().intValue() == 1);
        Double doubleStub = new Double(1.0);
        final Value<Double> doubleValue = new Value<Double>(doubleStub);
        Assert.assertEquals(doubleValue.get(), doubleStub);
        Assert.assertEquals(doubleValue.getType(), Double.class);
        Assert.assertEquals(doubleValue.get(), new Double(1.0));
        Assert.assertEquals(doubleValue.get().doubleValue(), 1.0, 0);
        doubleStub = new Double(2.0);
        Assert.assertEquals(doubleValue.get().doubleValue(), 1.0, 0);
        Boolean booleanStub = new Boolean(true);
        final Value<Boolean> booleanValue = new Value<Boolean>(booleanStub);
        Assert.assertEquals(booleanValue.get(), booleanStub);
        Assert.assertEquals(booleanValue.getType(), Boolean.class);
        Assert.assertEquals(booleanValue.get(), new Boolean(true));
        Assert.assertTrue(booleanValue.get().booleanValue());
        booleanStub = new Boolean(false);
        Assert.assertTrue(booleanValue.get().booleanValue());
    }
    
    /**
     * Test Location value
     */
    @Test
    public void testLocationValue() {
        Location location = new Location(0, 0);
        final Value<Location> locationValue = new Value<Location>(location);
        Assert.assertEquals(locationValue.getType(), Location.class);
        Assert.assertEquals(locationValue.get(), new Location(0, 0));
        location = new Location(1, 1);
        Assert.assertEquals(locationValue.get(), new Location(0, 0));
    }
}
