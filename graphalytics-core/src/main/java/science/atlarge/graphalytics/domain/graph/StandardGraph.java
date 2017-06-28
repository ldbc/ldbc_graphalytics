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
public enum StandardGraph {

    XDIR("xdir", "example-directed", "XD", true, false, true, 10, 17),
    XUNDIR("xundir", "example-undirected", "XU", false, false, true, 9, 12),

    WIKI("wiki", "wiki-Talk", "R1", true, true, false, 2394385, 5021410),
    KGS("kgs", "kgs", "R2", false, true, true, 832247, 17891698),
    CITA("cita", "cit-Patents", "R3", true, true, false, 3774768, 16518948),
    DOTA("dota", "dota-league", "R4", false, true, true, 61170, 50870313),
    FSTER("fster", "com-friendster", "R5", false, true, false, 65608366, 1806067135),
    TWIT("twit", "twitter_mpi", "R6", true, true, false, 52579678, 1963263508),

    DG75FB("dg7.5fb", "datagen-7_5-fb", "DG7.5FB", false, false, true, 633432, 34185747),
    DG76FB("dg7.6fb", "datagen-7_6-fb", "DG7.6FB", false, false, true, 754147, 42162988),
    DG77ZF("dg7.7zf", "datagen-7_7-zf", "DG7.7ZF", false, false, true, 13180508, 32791267),
    DG78ZF("dg7.8zf", "datagen-7_8-zf", "DG7.8ZF", false, false, true, 16521886, 41025255),
    DG79FB("dg7.9fb", "datagen-7_9-fb", "DG7.9FB", false, false, true, 1387587, 85670523),
    DG80FB("dg8.0fb", "datagen-8_0-fb", "DG8.0FB", false, false, true, 1706561, 107507376),
    DG81FB("dg8.1fb", "datagen-8_1-fb", "DG8.1FB", false, false, true, 2072117, 134267822),
    DG82ZF("dg8.2zf", "datagen-8_2-zf", "DG8.2ZF", false, false, true, 43734497, 106440188),
    DG83ZF("dg8.3zf", "datagen-8_3-zf", "DG8.3ZF", false, false, true, 53525014, 130579909),
    DG84FB("dg8.4fb", "datagen-8_4-fb", "DG8.4FB", false, false, true, 3809084, 269479177),
    DG85FB("dg8.5fb", "datagen-8_5-fb", "DG8.5FB", false, false, true, 4599739, 332026902),
    DG86FB("dg8.6fb", "datagen-8_6-fb", "DG8.6FB", false, false, true, 5667674, 421988619),
    DG87ZF("dg8.7zf", "datagen-8_7-zf", "DG8.7ZF", false, false, true, 145050709, 340157363),
    DG88ZF("dg8.8zf", "datagen-8_8-zf", "DG8.8ZF", false, false, true, 168308893, 413354288),
    DG89FB("dg8.9fb", "datagen-8_9-fb", "DG8.9FB", false, false, true, 10572901, 848681908),
    DG90FB("dg9.0fb", "datagen-9_0-fb", "DG9.0FB", false, false, true, 12857671, 1049527225),
    DG91FB("dg9.1fb", "datagen-9_1-fb", "DG9.1FB", false, false, true, 16087483, 1342158397),
    DG92ZF("dg9.2zf", "datagen-9_2-zf", "DG9.2ZF", false, false, true, 434943376, 1042340732),
    DG93ZF("dg9.3zf", "datagen-9_3-zf", "DG9.3ZF", false, false, true, 555270053, 1309998551),
    DG94FB("dg9.4fb", "datagen-9_4-fb", "DG9.4FB", false, false, true, 29310565, 2588948669l),

    GR22("gr22", "graph500-22", "G22", false, false, false, 2396657, 64155735),
    GR23("gr23", "graph500-23", "G23", false, false, false, 4610222, 129333677),
    GR24("gr24", "graph500-24", "G24", false, false, false, 8870942, 260379520),
    GR25("gr25", "graph500-25", "G25", false, false, false, 17062472, 523602831),
    GR26("gr26", "graph500-26", "G26", false, false, false, 32804978, 1051922853);



    public String id;
    public String fileName;
    public String scale;
    public boolean isDirected;
    public boolean isReal;
    public boolean hasProperty;
    public long vertexSize;
    public long edgeSize;
    public long graphSize;

    StandardGraph(String id, String fileName, String scale, boolean isDirected, boolean isReal, boolean hasProperty, long vertexSize, long edgeSize) {
        this.id = id;
        this.fileName = fileName;
        this.scale = scale;
        this.isDirected = isDirected;
        this.isReal = isReal;
        this.hasProperty = hasProperty;
        this.vertexSize = vertexSize;
        this.edgeSize = edgeSize;
        this.graphSize = vertexSize + edgeSize;
    }
}
