/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.mapreducev2.cd;

/**
 * Configuration constants for community detection on MapReduce version 2.
 *
 * @author Tim Hegeman
 */
public final class CommunityDetectionConfiguration {
    public static final String NODE_PREFERENCE = "CD.NodePreference";
    public static final String HOP_ATTENUATION = "CD.HopAttenuation";

    public enum LABEL_STATUS {
        STABLE,
        CHANGED
    }
}
