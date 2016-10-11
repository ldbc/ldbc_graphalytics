function buildTournaments() {
    for(var t in tournaments) {
        var tournament = tournaments[t];

        // build all matches
        platforms.forEach(function (pA) {
            platforms.forEach(function (pB) {
                if(pA != pB) {
                    tournament.types.forEach(function (matchType) {
                        var match = buildMatch(matchType, pA, pB);
                        tournament.matches[matchType.name + ":" + pA + ":" + pB] = match;
                    })
                }
            })
        })

        // calc tournament scores
        tournament.scores = {};
        platforms.forEach(function (pA) {
            platforms.forEach(function (pB) {
                if (pA != pB) {
                    var tScoreA = 0;
                    tScoreA = runFunction(tournament.scoreFunc, tournament, pA, pB);
                    tournament.scores[pA + ":" + pB] = tScoreA;
                }

            })
        })
    }
}


function buildMatch(type, platformA, platformB) {

    var match = {};
    match.type = type.name;
    match.platformA = platformA;
    match.platformB = platformB;
    match.scoreA = 0;
    match.scoreB = 0;
    match.jobSize = 0;
    match.instances = {};

    var selector = type.selector;
    var jobSets = runFunction(selector.func, selector.arg1, selector.arg2);
    var instances = match.instances;

    for(var iId in jobSets) {
        var jobSet = jobSets[iId];
        var jobSetA = jobSet[platformA];
        var jobSetB = jobSet[platformB];
        instances[iId] = {id: iId, setA: {jobs: jobSetA}, setB: {jobs: jobSetB}};
    }

    var matchScoreFunc = (type.score.matchFunc) ? type.score.matchFunc : "matchScoresV1";
    var scores = runFunction(matchScoreFunc, type, instances);

    match.scoreA += scores.scoreA;
    match.scoreB += scores.scoreB;
    match.jobSize = scores.jobSize++;

    return match;
}


function matchScoresMeanV1(type, instances) {

    var scoreA = 0;
    var scoreB = 0;
    var jobSize = 0;

    for(var i in instances) {
        var instance = instances[i];

        [instance.setA, instance.setB].forEach(function (set) {
            var metricDef = type.metric;
            set.value = runFunction(metricDef.func, set.jobs);
            set.unit = metricDef.unit;
        });

        // verbose
        var iScores = runFunction(type.score.instanceFunc, instance);
        instance.setA.score = iScores.setA;
        instance.setB.score = iScores.setB;

        scoreA += instance.setA.value;
        scoreB += instance.setB.value;
        jobSize++;
    }
    scoreA /= jobSize;
    scoreB /= jobSize;
    return {scoreA: scoreA, scoreB: scoreB, jobSize: 1};

}

function matchScoresV1(type, instances) {

    var scoreA = 0;
    var scoreB = 0;
    var jobSize = 0;

    for(var i in instances) {
        var instance = instances[i];
        var items = [instance.setA, instance.setB];
        items.forEach(function (item) {
            var metricDef = type.metric;

            item.value = runFunction(metricDef.func, item.jobs);
            item.unit = metricDef.unit;
        });

        var iScores = runFunction(type.score.instanceFunc, instance);
        instance.setA.score = iScores.setA;
        instance.setB.score = iScores.setB;

        scoreA += instance.setA.score;
        scoreB += instance.setB.score;
        jobSize++;
    }
    return {scoreA: scoreA, scoreB: scoreB, jobSize: jobSize};

}

function tourScoresNumV1(tournament, platformA, platformB) {

    var mScores = tournament.types.map(function (m) {
        var match = tournament.matches[m.name + ":" + platformA + ":" + platformB];
        return platformScoresV1(match.scoreA, match.scoreB).scoreA;
    });

    return sum(mScores);
}

function tourScoresBinV1(tournament, platformA, platformB) {

    var tScoreA = 0;
    var tScoreB = 0;

    tournament.types.forEach(function (m) {
        var match = tournament.matches[m.name + ":" + platformA + ":" + platformB];
            tScoreA += platformScoresV1(match.scoreA, match.scoreB).scoreA;
            tScoreB += platformScoresV1(match.scoreA, match.scoreB).scoreB;
    });

    return score(tScoreA, tScoreB);

}

function tourScoresMeanV1(tournament, platformA, platformB) {

    var mScores = tournament.types.map(function (m) {
        var match = tournament.matches[m.name + ":" + platformA + ":" + platformB];
        return platformScoresV1(match.scoreA, match.scoreB).scoreA;
    });
    return mean(mScores);
}

function platformScoresV1(mScoreA, mScoreB) {

    var pScoreA = 0;
    var pScoreB = 0;

    if(mScoreA <= 0 && mScoreB <= 0) {
        pScoreA = 0;
        pScoreB = 0;
    } else {
        pScoreA = score(mScoreA, mScoreB);
        pScoreB = score(mScoreB, mScoreA);
    }
    return {scoreA: pScoreA, scoreB: pScoreB};
}


function instanceScoreRev(instance) {

    var valueA = instance.setA.value;
    var valueB = instance.setB.value;
    var scoreA = 0;
    var scoreB = 0;

    if(valueA <= 0 && valueB <= 0) { // both A and B failed
        scoreA = 0;
        scoreB = 0;
    } else if (valueA <= 0 || valueB <= 0) { // if one of A or B failed
        if (valueA <= 0) {
            scoreA = 0;
            scoreB = 1;
        } else {
            scoreA = 1;
            scoreB = 0;
        }
    } else {
        scoreA = scoreRev(valueA, valueB);
        scoreB = scoreRev(valueB, valueA);
    }
    return {setA: scoreA, setB: scoreB};
}


function instanceScore(instance) {

    var valueA = instance.setA.value;
    var valueB = instance.setB.value;
    var scoreA = 0;
    var scoreB = 0;

    if(valueA <= 0 && valueB <= 0) { // both A and B failed
        scoreA = 0;
        scoreB = 0;
    } else if (valueA <= 0 || valueB <= 0) { // if one of A or B failed
        if (valueA <= 0) {
            scoreA = 0;
            scoreB = 1;
        } else {
            scoreA = 1;
            scoreB = 0;
        }
    } else {
        scoreA = score(valueA, valueB);
        scoreB = score(valueB, valueA);
    }
    return {setA: scoreA, setB: scoreB};
}

