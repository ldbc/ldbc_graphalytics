package nl.tudelft.graphalytics.domain.graph;

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
    TWIT("twit", "twitter_mpi", "R6", true, true, false, 52579682, 1963263821),

    DG100("dg100", "datagen-100", "D100", true, false, true, 1611868, 101749033),
    DG100C5("dg100c5", "datagen-100-fb-cc0_05", "D100'", true, false, true, 1611868, 103396508),
    DG100C15("dg100c15", "datagen-100-fb-cc0_15", "D100''", true, false, true, 1611868, 102694411),
    DG300("dg300", "datagen-300", "D300", true, false, true, 4241846, 304029144),
    DG1000("dg1000", "datagen-1000", "D1000", true, false, true, 12431110, 1014688802),

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