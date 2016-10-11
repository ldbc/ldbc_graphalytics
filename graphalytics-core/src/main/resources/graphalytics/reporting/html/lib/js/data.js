var runs = {}; // a map of runs
var jobs = {}; // a map of jobs
var platforms = []; // a list of platforms, derived from raw data
var tournaments = {}; // a map of tournaments, derived from raw data


var testT;

function loadData() {

    // load runs from raw data;
    srcData.forEach(function (run) {
        runs[run[0]] = {
            id: run[0],
            timestamp: run[1],
            platform: run[2],
            alg: run[3],
            dataset: run[4],
            workerSize: run[5],
            threadSize: run[6],
            success: run[7],
            makespan: run[8],
            procTime: run[9]};
    })

    // assemble jobs from runs, in the future the raw data should contains a list of jobs.
    for(var runId in runs) {
        var run = runs[runId];
        var jobId = run.platform + ":" + run.alg + ":" + run.dataset + ":" + run.workerSize + ":" + run.threadSize;

        if(!jobs.hasOwnProperty(jobId)) {
            jobs[jobId] = {platform: run.platform, alg: run.alg, dataset: run.dataset, workerSize: run.workerSize, threadSize: run.threadSize, runs:[runId]};
        } else {
            jobs[jobId].runs.push(runId);
        }
    }

    // for(var j in jobs) {
    //     var job = jobs[j];
    //     printFast("####" + j);
    //     printFast("@@@@" + job.platform + ":proc:" + job.dataset+ ":" + job.workerSize + ":" + job.threadSize);
    // }


    platforms = [];
    for(var runId in runs) {
        var run = runs[runId];
        platforms.push(run.platform);
    }
    platforms = platforms.filter(function(item, i, ar){ return ar.indexOf(item) === i; });
    platforms = ["GIRAPH", "GRAPHX", "POWERGRAPH", "OPENG", "GRAPHMATD", "PGXD"] // overwrite for convenience.


}


