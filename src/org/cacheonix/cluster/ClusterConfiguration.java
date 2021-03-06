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
package org.cacheonix.cluster;

import java.io.Externalizable;
import java.util.Collection;

/**
 * A cluster configuration.
 */
public interface ClusterConfiguration extends Externalizable {

   /**
    * Returns an un-modifiable collection of servers constituting the cluster.
    *
    * @return the un-modifiable collection of servers of the cluster.
    */
   Collection<ClusterMember> getClusterMembers();

   /**
    * Returns a cluster state.
    *
    * @return the ClusterState.
    * @see ClusterState#BLOCKED
    * @see ClusterState#OPERATIONAL
    * @see ClusterState#RECONFIGURING
    */
   ClusterState getClusterState();

   /**
    * Returns a unique cluster ID.
    *
    * @return a unique cluster ID. Returns null if the cluster has never been operational.
    */
   String getClusterUUID();
}
