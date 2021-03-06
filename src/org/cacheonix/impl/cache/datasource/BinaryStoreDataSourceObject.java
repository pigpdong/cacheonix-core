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
package org.cacheonix.impl.cache.datasource;

import java.io.Serializable;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;

/**
 * An object that is returned by {@link BinaryStoreDataSource#get(Binary)}. <code>BinaryStoreDataSourceObject</code>
 * implements the Value Object Pattern.
 *
 * @see BinaryStoreDataSource
 */
public interface BinaryStoreDataSourceObject {

   /**
    * @return a serializable object or null.
    * @see BinaryStoreDataSource#get(Binary)
    * @see Serializable
    */
   Serializable getObject();

   /**
    * Returns time it took to read from the data source.
    *
    * @return time it took to read from the data source.
    */
   Time getTimeToRead();
}
