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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.net.processor.Message;

/**
 * This exception is thrown when there is an error while partitioning a message.
 */
public final class NotPartitionableException extends CacheonixException {

   private static final long serialVersionUID = -140684474528361834L;


   public NotPartitionableException(final Message message, final Throwable e) {

      super(messageToErrorString(message), e);
   }


   private static String messageToErrorString(final Message message) {

      return message.toString();
   }


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public NotPartitionableException() {

   }
}
