/**
 * 
 */
package mwgrid.middleware.distributedobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import mwgrid.middleware.distributedobject.AbstractMessage.MessageComparator;
import mwgrid.middleware.exception.RollbackException;
import mwgrid.middleware.kernel.Kernel;
import mwgrid.middleware.kernel.SchedulerListener;

import org.junit.Test;

/**
 * @author Dr B.G.W. Craenen <b.g.w.craenen@cs.bham.ac.uk>
 */
public class DistributedObjectTest {
    public class TestAgent extends DistributedObject {
        private final Map<Variable, Value<?>> fVariableMap;
        
        /**
         * Constructor
         */
        public TestAgent() {
            super(1);
            this.fVariableMap = new HashMap<Variable, Value<?>>();
            this.fVariableMap.put(KernelVariables.MESSAGES,
                new Value<String>(new String("|")));
        }
        
        @Override
        public Value<?> getVariable(final Variable pVariable) {
            return this.fVariableMap.get(pVariable);
        }
        
        @Override
        public Value<?> getVariable(final long pObjectID,
                final Variable pVariable) {
            if (pObjectID == this.getObjectId())
                return this.getVariable(pVariable);
            return null;
        }
        
        @Override
        public void setVariable(final Variable pVariable,
                final Value<?> pValue) throws RollbackException {
            this.fVariableMap.put(pVariable, pValue);
        }
        
        @Override
        public void setVariable(final long pObjectID,
                final Variable pVariable, final Value<?> pValue)
                throws RollbackException {
            if (pObjectID == this.getObjectId()) {
                this.setVariable(pVariable, pValue);
                return;
            }
            return;
        }
        
        @Override
        public void step() throws RollbackException {
            // Empty step
        }
        
        @Override
        public String report() throws RollbackException {
            // Empty report
            return "Empty";
        }
        
        /**
         * @param pString
         *            - string
         * @param pMessage
         *            - message
         * @return (String) message converted to string added to string
         */
        public String callAddMessageToString(final String pString,
                final Message pMessage) {
            return this.addMessageToString(pString, pMessage);
        }
        
        /**
         * @param pMessageQueue
         *            - message queue
         * @param pString
         *            - string
         * @return (Queue<Message>) queue with message attached from string
         */
        public Queue<Message> callAddStringToQueue(
                final Queue<Message> pMessageQueue, final String pString) {
            return this.addStringToQueue(pMessageQueue, pString);
        }
        
        /**
         * @param pMessageQueue
         *            - message queue
         * @return (String) message queue converted to string
         */
        public String callConvertMessageQueueToString(
                final Queue<Message> pMessageQueue) {
            return this.convertMessageQueueToString(pMessageQueue);
        }
        
        /**
         * @param pMessageString
         *            - message string
         * @return (Queue<Message>) message string converted to message queue
         */
        public Queue<Message> callConvertStringToMessageQueue(
                final String pMessageString) {
            return this.convertStringToMessageQueue(pMessageString);
        }
    }
    
    public class TestListener implements SchedulerListener {
        /**
         * Constructor
         */
        public TestListener() {
            // Constructor
        }
        
        /**
         * @see mwgrid.middleware.kernel.SchedulerListener#collectReport(long, int, java.lang.String)
         * 
         * @param pAgentId - agent ID
         * @param pTime - time
         * @param pReport - report
         */
        @Override
        public void collectReport(final long pAgentId, final int pTime, final String pReport) {
            // Empty report collection
        }
    }
    
    /**
     * Constructor
     */
    public DistributedObjectTest() {
        // Constructor
    }
    
    /**
     * Test addMessageToString method
     */
    @Test
    public void testAddMessageToString() {
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final String string = "|";
        final long destinationID = 1;
        final long sourceID = 2;
        final int time = 3;
        final Location location = new Location(4, 5);
        final TestMessage testMessage =
                new TestMessage(destinationID, sourceID, time, location);
        final TestAgent testAgent = new TestAgent();
        final String result =
                testAgent.callAddMessageToString(string, testMessage);
        assertEquals(result,
            "|mwgrid.middleware.distributedobject.TestMessage=1#2#3#4#5|");
    }
    
    /**
     * Test addStringToQueue method
     */
    @Test
    public void testAddStringToQueue() {
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final Queue<Message> queue = new PriorityQueue<Message>();
        final String message =
                "mwgrid.middleware.distributedobject.TestMessage=1#2#3#4#5";
        final TestAgent testAgent = new TestAgent();
        final Queue<Message> result =
                testAgent.callAddStringToQueue(queue, message);
        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.peek().getDestination(), 1);
        assertEquals(result.peek().getSource(), 2);
        assertEquals(result.peek().getTime(), 3);
        assertEquals(result.peek().getType(), TestMessage.class);
        assertEquals(((TestMessage) result.peek()).getLocation(),
            new Location(4, 5));
        final TestMessage testMessage = new TestMessage();
        testMessage.convertFromString(message.split("=")[1]);
        assertEquals(result.peek(), testMessage);
    }
    
    /**
     * Test adding message to string and string to queue
     */
    @Test
    public void testAddMessageToStringAndAddStringToQueue() {
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final String string = "|";
        final long destination = 1;
        final long source = 2;
        final int time = 3;
        final Location location = new Location(4, 5);
        final TestMessage testMessage =
                new TestMessage(destination, source, time, location);
        final TestAgent testAgent = new TestAgent();
        final String resultString =
                testAgent.callAddMessageToString(string, testMessage);
        final String[] stringArray = resultString.split("\\|");
        assertEquals(stringArray.length, 2);
        assertTrue(stringArray[0].isEmpty());
        assertFalse(stringArray[1].isEmpty());
        final Queue<Message> queue = new PriorityQueue<Message>();
        final Queue<Message> resultQueue =
                testAgent.callAddStringToQueue(queue, stringArray[1]);
        assertNotNull(resultQueue);
        assertEquals(resultQueue.size(), 1);
        assertEquals(resultQueue.peek().getDestination(), destination);
        assertEquals(resultQueue.peek().getSource(), source);
        assertEquals(resultQueue.peek().getTime(), time);
        assertEquals(resultQueue.peek().getType(), TestMessage.class);
        assertEquals(((TestMessage) resultQueue.peek()).getLocation(),
            location);
    }
    
    /**
     * Test convertMessageQueueToString method
     */
    @Test
    public void testConvertMessageQueueToString() {
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final Queue<Message> queue =
                new PriorityQueue<Message>(3, new MessageComparator());
        final long destinationOne = 1;
        final long sourceOne = 2;
        final int timeOne = 3;
        final Location locationOne = new Location(4, 5);
        final TestMessage testMessageOne =
                new TestMessage(destinationOne, sourceOne, timeOne,
                        locationOne);
        queue.add(testMessageOne);
        final long destinationTwo = 6;
        final long sourceTwo = 7;
        final int timeTwo = 8;
        final Location locationTwo = new Location(9, 10);
        final TestMessage testMessageTwo =
                new TestMessage(destinationTwo, sourceTwo, timeTwo,
                        locationTwo);
        queue.add(testMessageTwo);
        final long destinationThree = 11;
        final long sourceThree = 12;
        final int timeThree = 13;
        final Location locationThree = new Location(14, 15);
        final TestMessage testMessageThree =
                new TestMessage(destinationThree, sourceThree, timeThree,
                        locationThree);
        queue.add(testMessageThree);
        final TestAgent testAgent = new TestAgent();
        final String result =
                testAgent.callConvertMessageQueueToString(queue);
        assertFalse(result.isEmpty());
        assertEquals(
            result,
            "|mwgrid.middleware.distributedobject.TestMessage=1#2#3#4#5|mwgrid.middleware.distributedobject.TestMessage=6#7#8#9#10|mwgrid.middleware.distributedobject.TestMessage=11#12#13#14#15|");
    }
    
    /**
     * Test convertStringToMessageQueue method
     */
    @Test
    public void testConvertStringToMessageQueue() {
        final String messageString =
                "|mwgrid.middleware.distributedobject.TestMessage=1#2#3#4#5|mwgrid.middleware.distributedobject.TestMessage=6#7#8#9#10|mwgrid.middleware.distributedobject.TestMessage=11#12#13#14#15|";
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final TestAgent testAgent = new TestAgent();
        final Queue<Message> result =
                testAgent.callConvertStringToMessageQueue(messageString);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.size(), 3);
        final Message messageOne = result.poll();
        assertNotNull(messageOne);
        assertEquals(messageOne.getDestination(), 1);
        assertEquals(messageOne.getSource(), 2);
        assertEquals(messageOne.getTime(), 3);
        assertEquals(messageOne.getType(), TestMessage.class);
        assertEquals(((TestMessage) messageOne).getLocation(), new Location(
                4, 5));
        final Message messageTwo = result.poll();
        assertNotNull(messageTwo);
        assertEquals(messageTwo.getDestination(), 6);
        assertEquals(messageTwo.getSource(), 7);
        assertEquals(messageTwo.getTime(), 8);
        assertEquals(messageTwo.getType(), TestMessage.class);
        assertEquals(((TestMessage) messageTwo).getLocation(), new Location(
                9, 10));
        final Message messageThree = result.poll();
        assertNotNull(messageThree);
        assertEquals(messageThree.getDestination(), 11);
        assertEquals(messageThree.getSource(), 12);
        assertEquals(messageThree.getTime(), 13);
        assertEquals(messageThree.getType(), TestMessage.class);
        assertEquals(((TestMessage) messageThree).getLocation(),
            new Location(14, 15));
        assertTrue(result.isEmpty());
    }
    
    /**
     * Test sendMessage method
     */
    @Test
    public void testSendMessage() {
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final TestAgent testAgent = new TestAgent();
        final long destinationID = testAgent.getObjectId();
        final long sourceID = testAgent.getObjectId();
        final int time = 1;
        final Location location = new Location(1, 2);
        final TestMessage testMessage =
                new TestMessage(destinationID, sourceID, time, location);
        try {
            testAgent.sendMessage(testMessage);
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        final String messages =
                (String) testAgent.getVariable(KernelVariables.MESSAGES)
                        .get();
        assertFalse(messages.isEmpty());
        assertEquals(messages, "|" + testMessage.getClass().getName() + "="
                + destinationID + "#" + sourceID + "#" + time + "#"
                + location.getX() + "#" + location.getY() + "|");
    }
    
    /**
     * Test getMessage method
     */
    @SuppressWarnings("null")
    @Test
    public void testGetMessage() {
        Kernel.getSequentionalInstance(new TestListener(), 10);
        final TestAgent testAgent = new TestAgent();
        Message emptyResult = null;
        try {
            emptyResult = testAgent.getMessage();
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        assertNull(emptyResult);
        final long destinationID = testAgent.getObjectId();
        final long sourceID = testAgent.getObjectId();
        final int time = 0;
        final Location location = new Location(1, 2);
        final TestMessage testMessageOne =
                new TestMessage(destinationID, sourceID, time, location);
        try {
            testAgent.sendMessage(testMessageOne);
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        Message result = null;
        try {
            result = testAgent.getMessage();
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertEquals(result.getDestination(), destinationID);
        assertEquals(result.getSource(), sourceID);
        assertEquals(result.getTime(), time);
        assertEquals(result.getType(), TestMessage.class);
        assertEquals(((TestMessage) result).getLocation(), location);
        final String messages =
                (String) testAgent.getVariable(KernelVariables.MESSAGES)
                        .get();
        assertFalse(messages.isEmpty());
        assertEquals(messages, "|");
        final Location locationTwo = new Location(3, 4);
        final TestMessage testMessageTwo =
                new TestMessage(destinationID, sourceID, time, locationTwo);
        try {
            testAgent.sendMessage(testMessageOne);
            testAgent.sendMessage(testMessageTwo);
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        Message resultOne = null;
        try {
            resultOne = testAgent.getMessage();
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(resultOne);
        assertEquals(resultOne.getDestination(), destinationID);
        assertEquals(resultOne.getSource(), sourceID);
        assertEquals(resultOne.getTime(), time);
        assertEquals(resultOne.getType(), TestMessage.class);
        assertEquals(((TestMessage) resultOne).getLocation(), location);
        Message resultTwo = null;
        try {
            resultTwo = testAgent.getMessage();
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(resultTwo);
        assertEquals(resultTwo.getDestination(), destinationID);
        assertEquals(resultTwo.getSource(), sourceID);
        assertEquals(resultTwo.getTime(), time);
        assertEquals(resultTwo.getType(), TestMessage.class);
        assertEquals(((TestMessage) resultTwo).getLocation(), locationTwo);
        Message resultThree = null;
        try {
            resultThree = testAgent.getMessage();
        } catch (RollbackException e) {
            e.printStackTrace();
            fail();
        }
        assertNull(resultThree);
    }
}
