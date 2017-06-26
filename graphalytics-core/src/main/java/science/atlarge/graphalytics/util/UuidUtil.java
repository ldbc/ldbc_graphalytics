/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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

package science.atlarge.graphalytics.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class UuidUtil {

    private static Set<String> usedUUIDs = new HashSet<>();

    public static String getRandomUUID() {
        String uuid = String.valueOf(UUID.randomUUID().getLeastSignificantBits() * -1l);

        while(usedUUIDs.contains(uuid)) {
            uuid = String.valueOf(UUID.randomUUID().getLeastSignificantBits() * -1l);
        }
        usedUUIDs.add(uuid);

        return uuid;
    }

    public static String getRandomUUID(String prefix, int length) {
        String uuid = prefix + String.valueOf(getRandomUUID().substring(0, length));

        while(usedUUIDs.contains(uuid)) {
            uuid = prefix + String.valueOf(getRandomUUID().substring(0, length));
        }
        usedUUIDs.add(uuid);

        return uuid;
    }

    public static String getRandomUUID(int length) {
        return getRandomUUID("", length);
    }

}
