package mwgrid.middleware.distributedobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import mwgrid.middleware.distributedobject.Location.Neighbours;

import org.junit.Test;

/**
 * @author Dr B.G.W. Craenen (b.g.w.craenen@cs.bham.ac.uk)
 */
public class LocationTest {
    private static final double DISTANCE = 5d;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    
    /**
     * Constructor
     */
    public LocationTest() {
        // Constructor
    }
    
    /**
     * Test constructor
     */
    @Test
    public void testConstructor() {
        final Location location = new Location(0, 0);
        assertTrue(location.getX() == 0);
        assertTrue(location.getY() == 0);
    }
    
    /**
     * Test equals
     */
    @Test
    public void testEquals() {
        final Location location1 = new Location(1, 1);
        assertTrue(location1.equals(location1));
        final Location location2 = new Location(2, 2);
        assertFalse(location1.equals(location2));
        assertFalse(location2.equals(location1));
        final Location location3 = new Location(1, 1);
        assertTrue(location1.equals(location3));
        assertTrue(location3.equals(location1));
        assertFalse(location2.equals(location3));
        assertFalse(location3.equals(location2));
    }
    
    /**
     * Test hash codes
     */
    @Test
    public void testHashCode() {
        final Location location1 = new Location(1, 1);
        final Location location2 = new Location(2, 2);
        assertFalse(location1.hashCode() == location2.hashCode());
        final Location location3 = new Location(1, 1);
        assertTrue(location1.hashCode() == location3.hashCode());
        assertFalse(location2.hashCode() == location3.hashCode());
    }
    
    /**
     * Test distance
     */
    @Test
    public void testDistance() {
        final Location location0 = new Location(0, 0);
        final Location location1 = new Location(3, 4);
        assertEquals(location0.distanceTo(location1), DISTANCE, 0);
        assertEquals(location1.distanceTo(location0), DISTANCE, 0);
        final Location location2 = new Location(-3, 4);
        assertEquals(location0.distanceTo(location2), DISTANCE, 0);
        assertEquals(location2.distanceTo(location0), DISTANCE, 0);
        final Location location3 = new Location(3, -4);
        assertEquals(location0.distanceTo(location3), DISTANCE, 0);
        assertEquals(location3.distanceTo(location0), DISTANCE, 0);
        final Location location4 = new Location(-3, -4);
        assertEquals(location0.distanceTo(location4), DISTANCE, 0);
        assertEquals(location4.distanceTo(location0), DISTANCE, 0);
    }
    
    /**
     * Test neighbours
     */
    @Test
    public void testNeighbours() {
        final Location location0 = new Location(0, 0);
        final List<Location> neighbours = location0.neighbours();
        assertTrue(neighbours.size() == Location.Neighbours.values().length);
        for (Neighbours neighbour : Location.Neighbours.values()) {
            assertEquals(neighbours.get(neighbour.ordinal()), neighbour
                    .getLocation(location0));
        }
    }
    
    /**
     * Test four neighbours
     */
    @Test
    public void testFourNeighbours() {
        final Location location0 = new Location(0, 0);
        final List<Location> fourNeighbourList = location0.fourNeigbours();
        assertTrue(fourNeighbourList.size() == FOUR);
        assertEquals(fourNeighbourList.get(0), Location.Neighbours.UP
                .getLocation(location0));
        assertEquals(fourNeighbourList.get(1), Location.Neighbours.RIGHT
                .getLocation(location0));
        assertEquals(fourNeighbourList.get(2), Location.Neighbours.DOWN
                .getLocation(location0));
        assertEquals(fourNeighbourList.get(THREE), Location.Neighbours.LEFT
                .getLocation(location0));
    }
}
