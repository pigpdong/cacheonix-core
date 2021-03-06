/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cacheonix.impl.util.logging;

/**
 * CategoryKey is a wrapper for String that apparently accelerated hash table lookup in early JVM's.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
final class CategoryKey {

   final String name;
   final int hashCache;


   CategoryKey(final String name) {
      this.name = name;
      hashCache = name.hashCode();
   }


   public final int hashCode() {
      return hashCache;
   }


   public final boolean equals(final Object rArg) {

      if (this == rArg) {
         return true;
      }

      return rArg != null && rArg.getClass() == CategoryKey.class && name.equals(((CategoryKey) rArg).name);
   }
}
