

function jobSelectorBasicV1(algorithm, workerSize) {
    var jobSets = {};

    for (var jId in jobs) {
        var job = jobs[jId];
        if (job.alg == algorithm &&
            platformDefs[job.platform].defThreads == job.threadSize &&
            job.workerSize == workerSize) {

            var setId = job.alg + ":" + job.dataset + ":" + job.workerSize;
            if (!jobSets[setId]) {
                jobSets[setId] = {};
                jobSets[setId][job.platform] = [job];
            } else {
                if (!jobSets[setId][job.platform]) {
                    jobSets[setId][job.platform] = [job];
                } else {
                    jobSets[setId][job.platform].push(job);
                }
            }
        }
    }
    return jobSets;
}

function jobSelectorSVScaleV1() {
    var jobSets = {};

    for(var jId in jobs) {
        var job = jobs[jId];
        if(job.dataset == "DG300"  &&
            (job.alg == "BFS" | job.alg == "PR") ) {
            var setId = job.alg + ":" + job.dataset;
            if(!jobSets[setId]) {
                jobSets[setId] = {};
                jobSets[setId][job.platform] = [job];
            } else {
                if(!jobSets[setId][job.platform]) {
                    jobSets[setId][job.platform] = [job];
                } else {
                    jobSets[setId][job.platform].push(job);
                }
            }

        }
    }
    return jobSets;
}


function jobSelectorSHScaleV1() {
    var jobSets = {};

    for(var jId in jobs) {
        var job = jobs[jId];
        if(job.dataset == "DG1000" &&
            platformDefs[job.platform].defThreads == job.threadSize &&
            (job.alg == "BFS" | job.alg == "PR") ) {
            var setId = job.alg + ":" + job.dataset;
            if(!jobSets[setId]) {
                jobSets[setId] = {};
                jobSets[setId][job.platform] = [job];
            } else {
                if(!jobSets[setId][job.platform]) {
                    jobSets[setId][job.platform] = [job];
                } else {
                    jobSets[setId][job.platform].push(job);
                }
            }

        }
    }
    return jobSets;
}

function jobSelectorWHScaleV1() {
    var jobSets = {};

    for(var jId in jobs) {
        var job = jobs[jId];
        if(((job.dataset == "GR22" && job.workerSize == "1") ||
            (job.dataset == "GR23" && job.workerSize == "2")  ||
            (job.dataset == "GR24" && job.workerSize == "4") ||
            (job.dataset == "GR25" && job.workerSize == "8") ||
            (job.dataset == "GR26" && job.workerSize == "16"))
            && platformDefs[job.platform].defThreads == job.threadSize &&
            (job.alg == "BFS" | job.alg == "PR") ) {
            var setId = job.alg;
            if(!jobSets[setId]) {
                jobSets[setId] = {};
                jobSets[setId][job.platform] = [job];
            } else {
                if(!jobSets[setId][job.platform]) {
                    jobSets[setId][job.platform] = [job];
                } else {
                    jobSets[setId][job.platform].push(job);
                }
            }

        }
    }
    return jobSets;
}
