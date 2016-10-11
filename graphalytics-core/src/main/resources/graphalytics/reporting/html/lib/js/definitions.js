
var platformDefs = {
    "PGX": {id: "PGX", name: "PGX(S)", acronym: "pgx", defThreads: -1},
    "PGXD": {id: "PGXD", name: "PGX(D)", acronym: "pgxd", defThreads: 28},
    "GIRAPH": {id: "GIRAPH", name: "Giraph", acronym: "gph", defThreads: 32},
    "GRAPHX": {id: "GRAPHX", name: "GraphX", acronym: "grx", defThreads: 32},
    "GRAPHMAT": {id: "GRAPHMAT", name: "G'Mat(S)", acronym: "gmat", defThreads: 16},
    "GRAPHMATD": {id: "GRAPHMATD", name: "G'Mat(D)", acronym: "gmatd", defThreads: 16},
    "POWERGRAPH": {id: "POWERGRAPH", name: "P'Graph", acronym: "pgph", defThreads: 32},
    "OPENG": {id: "OPENG", name: "OpenG", acronym: "opnj", defThreads: 16}
}


var datasetDefs = {
    XDIR: {id: "XDIR", filename: "example-directed", scale: "XD(XXS)", isDirected: true, hasProperty: true, vertexSize: 10, edgeSize: 17},
    XUNDIR: {id: "XUNDIR", filename: "example-undirected", scale: "XU(XXS)", isDirected: false, hasProperty: true, vertexSize: 9, edgeSize: 12},

    WIKI: {id: "WIKI", filename: "wiki-Talk", scale: "R1(2XS)", isDirected: true, hasProperty: false, vertexSize: 2394385, edgeSize: 5021410},
    KGS: {id: "KGS", filename: "kgs", scale: "R2(XS)", isDirected: false, hasProperty: true, vertexSize: 832247, edgeSize: 17891698},
    CITA: {id: "CITA", filename: "cit-Patents", scale: "R3(XS)", isDirected: true, hasProperty: false, vertexSize: 3774768, edgeSize: 16518948},
    DOTA: {id: "DOTA", filename: "dota-league", scale: "R4(S)", isDirected: false, hasProperty: true, vertexSize: 61170, edgeSize: 50870313},
    FSTER: {id: "FSTER", filename: "com-friendster", scale: "R5(XL)", isDirected: false, hasProperty: false, vertexSize: 65608366, edgeSize: 1806067135},
    TWIT: {id: "TWIT", filename: "twitter_mpi", scale: "R6(XL)", isDirected: true, hasProperty: false, vertexSize: 52579682, edgeSize: 1963263821},


    DG100: {id: "DG100", filename: "datagen-100", scale: "D100(M)", isDirected: true, hasProperty: true, vertexSize: 1670000, edgeSize: 101749033},
    DG100C5: {id: "DG100C5", filename: "datagen-100-fb-cc0_05", scale: "D100'(M)", isDirected: true, hasProperty: true, vertexSize: 1670000, edgeSize: 103396508},
    DG100C15: {id: "DG100C15", filename: "datagen-100-fb-cc0_15", scale: "D100''(M)", isDirected: true, hasProperty: true, vertexSize: 1670000, edgeSize: 102694411},
    DG300: {id: "DG300", filename: "datagen-300", scale: "D300(L)", isDirected: true, hasProperty: true, vertexSize: 4350000, edgeSize: 304029144},
    DG1000: {id: "DG1000", filename: "datagen-1000", scale: "D1000(XL)", isDirected: true, hasProperty: true, vertexSize: 12750000, edgeSize: 1014688802},

    GR22: {id: "GR22", filename: "graph500-22", scale: "G22(S)", isDirected: false, hasProperty: false, vertexSize: 2396657, edgeSize: 64155735},
    GR23: {id: "GR23", filename: "graph500-23", scale: "G23(M)", isDirected: false, hasProperty: false, vertexSize: 4610222, edgeSize: 129333677},
    GR24: {id: "GR24", filename: "graph500-24", scale: "G24(M)", isDirected: false, hasProperty: false, vertexSize: 8870942, edgeSize: 260379520},
    GR25: {id: "GR25", filename: "graph500-25", scale: "G25(L)", isDirected: false, hasProperty: false, vertexSize: 17062472, edgeSize: 523602831},
    GR26: {id: "GR26", filename: "graph500-26", scale: "G26(XL)", isDirected: false, hasProperty: false, vertexSize: 32804978, edgeSize: 1051922853}
}



function defineType1EvpsTournament() {

    var text = "This tournament consists of 6 matches: " +
        "<ul><li>alg_bfs_evps_v1, alg_wcc_evps_v1, <li>alg_pr_evps_v1, alg_cdlp_evps_v1," +
        "<li>alg_lcc_evps_v1, alg_sssp_evps_v1</ul>" +
        "The EVPS (on processing time) is used as the performance metric.<br>" +
        "";
    var tournament = {id: "type1-evps", name: "Type1-EVPS", text: text, types: [], matches: []};



    var metric = {func: "metricEvpsV1", unit: ""};

    var score = {instanceFunc: "instanceScore", matchFunc: "matchScoresV1"};
    tournament.scoreFunc = "tourScoresBinV1";
    tournament.types = [
        {name: "alg_bfs_evps_v1",
            selector: {func: "jobSelectorBasicV1", arg1: "BFS", arg2: "1"},
            score: score, metric: metric},

        {name: "alg_wcc_evps_v1",
            selector: {func: "jobSelectorBasicV1", arg1: "WCC", arg2: "1"},
            score: score, metric: metric},

        {name: "alg_pr_evps_v1",
            selector: {func: "jobSelectorBasicV1", arg1: "PR", arg2: "1"},
            score: score, metric: metric},

        {name: "alg_cdlp_evps_v1",
            selector: {func: "jobSelectorBasicV1", arg1: "CDLP", arg2: "1"},
            score: score, metric: metric},

        {name: "alg_lcc_evps_v1",
            selector: {func: "jobSelectorBasicV1", arg1: "LCC", arg2: "1"},
            score: score, metric: metric},

        {name: "alg_sssp_evps_v1",
            selector: {func: "jobSelectorBasicV1", arg1: "SSSP", arg2: "1"},
            score: score, metric: metric},
    ];
    tournaments[tournament.id] = tournament;
}


function defineTournamentRules() {

    defineType1EvpsTournament();
}
