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
package org.cacheonix.impl;

import junit.framework.TestCase;
import org.cacheonix.impl.util.logging.Logger;

/**
 * RuntimeInterruptedException Tester
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 21, 2008 12:40:38 AM
 */
public final class RuntimeInterruptedExceptionTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RuntimeInterruptedExceptionTest.class); // NOPMD

   private static final String MESSAGE = "message";


   public void testCreate() {

      final InterruptedException cause = new InterruptedException();
      assertEquals(cause, new RuntimeInterruptedException(cause).getCause());
      assertEquals(cause, new RuntimeInterruptedException(MESSAGE, cause).getCause());
      assertTrue(new RuntimeInterruptedException(MESSAGE, cause).getMessage().contains(MESSAGE));
   }
}
