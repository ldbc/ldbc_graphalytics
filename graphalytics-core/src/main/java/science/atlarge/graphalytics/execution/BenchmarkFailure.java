/*
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
package science.atlarge.graphalytics.execution;

/**
 * Created by wlngai on 6/15/17.
 */
public enum BenchmarkFailure {

    NON("non", "None"),
    DAT("dat", "Data"), // TODO not covered yet.
    INI("ini", "initialization"),
    TIM("tim", "timeout"),
    EXE("exe", "execution"), // TODO not covered yet.
    VAL("val", "validation"),
    MET("met", "metric"); // TODO not covered yet.

    public String id;
    public String type;

    BenchmarkFailure(String id, String type) {
        this.id = id;
        this.type = type;
    }
}