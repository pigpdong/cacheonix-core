/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.net.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.net.cluster.JoinRequest;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tester for TCPServer.
 */
public final class ReceiverTest extends CacheonixTestCase {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ReceiverTest.class); // NOPMD

   private static final int PORT = TestConstants.PORT_7676;

   private static final String LOCALHOST = TestConstants.LOCALHOST;

   private static final long SOCKET_TIMEOUT_MILLIS = ConfigurationConstants.DEFAULT_SO_TIMEOUT_MILLIS;

   private static final long SELECTOR_TIMEOUT_MILLIS = ConfigurationConstants.DEFAULT_SELECTOR_TIMEOUT_MILLIS;

   private Receiver server = null;

   private Serializer serializer = null;

   private final List<Message> received = new ArrayList<Message>(1);

   private final ReentrantLock lock = new ReentrantLock();

   private final Condition condition = lock.newCondition();


   public void testStartup() throws Exception {
      // Tests that can connect
      final Socket socket = new Socket(LOCALHOST, PORT);
      assertTrue(socket.isConnected());
      socket.close();
   }


   public void testReceivesOneMessage() throws Exception {

      writeFrame();
      waitForReceivedQueueSize(1);
      assertEquals(1, received.size());
   }


   public void testReceivesManyMessages() throws Exception {

      writeFrame();
      writeFrame();
      waitForReceivedQueueSize(2);
      assertEquals(2, received.size());
   }


   private void waitForReceivedQueueSize(final int expectedSize) throws InterruptedException {

      lock.lock();
      try {

         boolean receivedAll = received.size() >= expectedSize;
         while (!receivedAll) {

            condition.await(10, TimeUnit.MILLISECONDS);
            receivedAll = received.size() >= expectedSize; // NOPMD
         }
      } finally {

         lock.unlock();
      }
   }


   private void writeFrame() throws IOException {
      // Create frame
      final JoinRequest joinRequest = new JoinRequest(TestUtils.createTestAddress(1));
      final Frame frame = new Frame(Integer.MAX_VALUE, serializer,
              Frame.NO_COMPRESSION, 0L, joinRequest);

      // Send frame
      final Socket socket = new Socket(LOCALHOST, PORT);
      final OutputStream os = socket.getOutputStream();
      frame.write(os);
      os.flush();
      os.close();
      socket.close();
   }


   public void testShutdown() {

      server.shutdown();
      assertTrue(server.isShutDown());
   }


   public void testToString() {

      assertNotNull(server.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      //
      serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);

      // Initialize server
      final MessageDispatcher messageDispatcher = new MessageDispatcher() {

         public void dispatch(final Message message) {

            lock.lock();
            try {

               received.add(message);
               condition.signalAll();

               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled()) LOG.debug("message: " + message); // NOPMD
            } finally {

               lock.unlock();
            }
         }
      };
      server = new Receiver(getClock(), LOCALHOST, PORT, messageDispatcher, SOCKET_TIMEOUT_MILLIS,
              SELECTOR_TIMEOUT_MILLIS);
      server.startup();
   }


   protected void tearDown() throws Exception {

      super.tearDown();

      // Shutdown the server
      if (server != null && !server.isShutDown()) {
         server.shutdown();
      }
   }


   public String toString() {

      return "TCPServerTest{" +
              "server=" + server +
              "} " + super.toString();
   }
}
