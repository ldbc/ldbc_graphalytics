
function metricProcTimeV1(jobs) {
    var job = jobs[0];
    var result = medianResult(job, "procTime");
    return (result < 0) ? result : result;
}



function metricEvpsV1(jobs) {
    // return -1 if job failed
    var job = jobs[0];
    var graphsize = datasetDefs[job.dataset].vertexSize + datasetDefs[job.dataset].edgeSize;
    var result = medianResult(job, "procTime") / 1000;
    return (result < 0) ? -1 : graphsize / result;
}


function metricEvpsV2(jobs) {
    // return 0 if job failed
    var job = jobs[0];
    var graphsize = datasetDefs[job.dataset].vertexSize + datasetDefs[job.dataset].edgeSize;
    var result = medianResult(job, "procTime") / 1000;
    return (result < 0) ? 0 : graphsize / result;
}

function metricEvpsV3(jobs) {
    // return evps with Tproc=3600
    var job = jobs[0];
    var graphsize = datasetDefs[job.dataset].vertexSize + datasetDefs[job.dataset].edgeSize;
    var result = medianResult(job, "procTime") / 1000;
    var timeout = 3600;
    return (result < 0) ? graphsize / timeout : graphsize / result;
}

function metricRelEvpsV3(jobs) {

    var job = jobs[0];
    var bestEvps = metricBestEVPS(job.alg, job.dataset);
    var graphsize = datasetDefs[job.dataset].vertexSize + datasetDefs[job.dataset].edgeSize;

    var procTime = medianResult(job, "procTime") / 1000;
    if(procTime < 0 || bestEvps == 0) {
        return 0
    } else {
        var evps = graphsize / procTime;
        return evps / bestEvps;
    }
}

function metricBestEVPS(algorithm, dataset) {
    var jobSets = {};
    var bestEVPS = -1;
    var bestPlatform;

    for (var jId in jobs) {
        var job = jobs[jId];
        if (job.alg == algorithm && job.dataset == dataset &&
            platformDefs[job.platform].defThreads == job.threadSize && job.workerSize == 1) {

            if (!jobSets[job.platform]) {
                jobSets[job.platform] = [job];
            } else {
                jobSets[job.platform].push(job);
            }
        }
    }

    platforms = ["GIRAPH", "GRAPHX", "POWERGRAPH", "OPENG", "GRAPHMATD", "PGXD"] // overwrite for convenience.
    platforms.forEach(function (sId) {
        var evps = metricEvpsV2(jobSets[sId]);
        if(evps >= bestEVPS) {
            bestEVPS = evps;
            bestPlatform = sId;
        }
    })

    return bestEVPS;
}


function metricVpsV1(jobs) {
    var job = jobs[0];
    var graphsize = datasetDefs[job.dataset].vertexSize;
    var result = medianResult(job, "procTime") / 1000;
    return (result < 0) ? -1 : graphsize / result;
}


function metricEpsV1(jobs) {
    var job = jobs[0];
    var graphsize = datasetDefs[job.dataset].edgeSize;
    var result = medianResult(job, "procTime") / 1000;
    return (result < 0) ? -1 : graphsize / result;
}


function metricSHSpeedupV1(jobs) {
    var scales = {};
    jobs.forEach(function (job) {
        var result = medianResult(job, "procTime");
        (result > 0) ? scales[job.workerSize] = result : doNothing();
    });

    function completed() {return [1,2,4,8,16].map(function (i) {return !(!(scales[i]));}).reduce(function (a, b) { return a && b;})};

    if(!completed()) {
        interpolate([1,2,4,8,16], scales);
    }

    return (completed()) ? Math.max(Math.max(scales[1] / scales[4], scales[2] / scales[8]), scales[4] / scales[16]) : -1;

}


function metricSHAreaV1(jobs) {
    var scales = {};
    jobs.forEach(function (job) {
        var result = medianResult(job, "procTime");
        (result > 0) ? scales[job.workerSize] = result : doNothing();
    });

    function completed() {return [1,2,4,8,16].map(function (i) {return !(!(scales[i]));}).reduce(function (a, b) { return a && b;})};

    if(!completed()) {
        interpolate([1,2,4,8,16], scales);
    }

    return (completed()) ? metricScalabilityArea([1,2,4,8,16], scales) : -1;

}


function metricScalabilityArea(sizes, scales) {
    var areaValue = 0;

    for (var i = 0; i < sizes.length - 1; i++) {
        var sizeA = sizes[i];
        var sizeB = sizes[i+1];
        var scaleA = scales[sizeA];
        var scaleB = scales[sizeB];
        areaValue += (sizeB - sizeA) * (scaleA + scaleB) / 2;
    }
    return areaValue;

}

function interpolate(sizes, scales) {
    // find previous point
    function previous(pos) {
        if(pos - 1 < 0) {
            return null;
        } else if(scales[sizes[pos - 1]]) {
            return {pos: pos - 1, size: sizes[pos - 1], scale: scales[sizes[pos - 1]]};
        } else {
            return previous(pos - 1);
        }
    }

    // find next point
    function next(pos) {
        if(pos + 1 >= sizes.length) {
            return null;
        } else if(scales[sizes[pos + 1]]) {
            return {pos: pos + 1, size: sizes[pos + 1], scale: scales[sizes[pos + 1]]};
        } else {
            return next(pos + 1);
        }
    }


    // verify there are as least two points
    var count = 0;
    sizes.forEach(function (v) { if(scales[v]) { count++ }});
    if(count < 2) { return null; }

    for (var i = 0; i < sizes.length; i++) {

        // if a point is missing
        if(!scales[sizes[i]]) {
            var p = previous(i);
            var n = next(i);
            // if there is no previous point, use two next points
            if(!p) {
                var tmp = n;
                p = tmp;
                n = next(tmp.pos);
            }
            // if there is no next point, use two previous pionts.
            if(!n) {
                var tmp = p;
                n = tmp;
                p = previous(tmp.pos);
            }

            // calc ax + b;
            var a = (n.scale - p.scale) / (n.size - p.size);
            var b = n.scale - a * n.size;
            var size = sizes[i];
            scales[size] = a * size + b;
        }
    }
}



function metricWHSpeedupV1(jobs) {
    var scales = {};
    jobs.forEach(function (job) {
        var result = medianResult(job, "procTime");
        if(result > 0) {
            scales[job.workerSize] = result;
        }
    });

    function completed() { return [1,2,4,8,16].map(function (i) {return !(!(scales[i]));}).reduce(function (a, b) { return a && b;})};

    if(!completed()) {
        interpolate([1,2,4,8,16], scales);
    }
    return (completed()) ? Math.max(Math.max(scales[1] / scales[4], scales[2] / scales[8]), scales[4] / scales[16]) : -1;

}

function metricWHAreaV1(jobs) {
    var scales = {};
    jobs.forEach(function (job) {
        var result = medianResult(job, "procTime");
        if(result > 0) {
            scales[job.workerSize] = result;
        }
    });

    function completed() { return [1,2,4,8,16].map(function (i) {return !(!(scales[i]));}).reduce(function (a, b) { return a && b;})};

    if(!completed()) {
        interpolate([1,2,4,8,16], scales);
    }
    return (completed()) ? metricScalabilityArea([1,2,4,8,16], scales) : -1;

}

function metricSVSpeedupV1(jobs) {
    var scales = {};
    jobs.forEach(function (job) {
        var result = medianResult(job, "procTime");
        if(result > 0) {
            scales[job.threadSize] = result;
        }
    });

    function completed() {return [1,2,4,8,16].map(function (i) {return !(!(scales[i]));}).reduce(function (a, b) { return a && b;})};
    if(!completed()) {
        interpolate([1,2,4,8,16], scales);
    }
    return (completed()) ? Math.max(Math.max(scales[1] / scales[4], scales[2] / scales[8]), scales[4] / scales[16]) : -1;
}


function metricSVAreaV1(jobs) {
    var scales = {};
    jobs.forEach(function (job) {
        var result = medianResult(job, "procTime");
        if(result > 0) {
            scales[job.threadSize] = result;
        }
    });

    function completed() {return [1,2,4,8,16].map(function (i) {return !(!(scales[i]));}).reduce(function (a, b) { return a && b;})};
    if(!completed()) {
        interpolate([1,2,4,8,16], scales);
    }
    return (completed()) ? metricScalabilityArea([1,2,4,8,16], scales) : -1;
}




function medianResult(job, metric) {
    var values = [];
    job.runs.forEach(function (r) {
        var run = runs[r];
        if (run.success == 0 && run[metric] > -1) {
            values.push(run[metric]);
        }
    });
    return (values.length > 0) ? median(values) : -1;
}
