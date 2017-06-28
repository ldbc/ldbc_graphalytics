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
package science.atlarge.graphalytics.domain.graph;

/**
 * @author Wing Lung Ngai
 */
public enum GraphScale {
    XXS("XXS", 6.5, 6.9),
    XS("XS", 7.0, 7.4),
    S("S", 7.5, 7.9),
    M("M", 8.0, 8.4),
    L("L", 8.5, 8.9),
    XL("XL", 9.0, 9.4),
    XXL("XXL", 9.5, 10.0);

    public String text;
    public double minSize;
    public double maxSize;

    GraphScale(String text, double minScale, double maxScale) {
        this.text = text;
        this.minSize = minScale;
        this.maxSize = maxScale;
    }
}
