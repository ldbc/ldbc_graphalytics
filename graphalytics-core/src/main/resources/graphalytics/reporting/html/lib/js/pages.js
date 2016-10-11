function overviewTab() {

    var tab = $('<div ng-controller="roadmap-tab"></div>');


    tab.append($('<h3 class="text-muted title">Overview</h3><hr>'));
    tab.append("<p>This section describes the overview of Graphalytics scoring system.</p>");

    tab.append($('<iframe src="http://docs.google.com/gview?url=http://'+location.host+'/doc/scoring-system.pdf&embedded=true" ' +
        'style="width:860px; height:650px;" frameborder="0"></iframe>'));
    return tab;
}



function tournamentTab(tournament) {

    var tab = $('<div ng-controller="tournament-tab"></div>');


    var upperRow = $('<div class="row" />');


    tab.append($('<h3 class="text-muted title">Tournament</h3> '+tournament.name+'<hr>'));
    upperRow.append($('<div class="col-sm-8" />').append($('<p>'+tournament.text+'</p>')));
    upperRow.append($('<div class="col-sm-4" />').append(rankCard(tournament)));
    tab.append(upperRow);
    tab.append($('<br>'));
    tab.append($('<br>'));

    tab.append(tourCard(tournament));

    var underRow = $('<div class="row" />');
    underRow.append($('<div class="col-sm-6" />').append(matchesCard(tournament)));
    underRow.append($('<div class="col-sm-6" />').append(matchCard(tournament)));
    tab.append(underRow);

    return tab;
}


function rankCard(tournament) {


    var card = $('<div class="card" id="ranking-table-'+tournament.id+'"/>');
    card.append($('<h3>Ranking</h3>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    var pwScores = tournament.scores;

    tHead.append('<tr />');
    tHead.find('tr').append('<th></th>');
    tHead.find('tr').append('<th>rank</th>');
    tHead.find('tr').append('<th>score</th>');

    var pScores = [];

    platforms.forEach(function(p1){
        var total = 0;
        platforms.forEach(function(p2){
            var score =  pwScores[p1 + ":" + p2];
            if (score != null) {
                total += score;
            }
        });
        pScores.push({name: p1, score: total});
    });

    pScores.sort(function (a, b) {
        return b.score - a.score;
    });

    for(var p = 0; p < pScores.length; p++) {
        var pScore = pScores[p];
        if(p > 0 && pScore.score == pScores[p - 1].score) {
            pScore.rank = pScores[p - 1].rank;
        } else {
            pScore.rank = p + 1;
        }
    }


    pScores.forEach(function(pScore){
        tBody.append('<tr>');
        tBody.append('<td>'+pScore.name+'</td>');
        tBody.append('<td>'+pScore.rank+'</td>');
        tBody.append('<td>'+pScore.score.toFixed(2)+'</td>');
        tBody.append('</tr>');
    });

    tBody.append();

    card.append(table);
    return card;
}




function tourCard(tournament) {


    var card = $('<div class="card" id="tour-table-'+tournament.id+'"/>');
    card.append($('<h3>Tournament</h3>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    var pwScores = tournament.scores;

    tHead.append('<tr />');
    tHead.find('tr').append('<th></th>');
    platforms.forEach(function(p){
        tHead.find('tr').append('<th>'+p+'</th>');
    });
    tHead.find('tr').append('<th>total</th>');

    var pScores = {};

    platforms.forEach(function(p1){
        tBody.append('<tr>');
        tBody.append('<td>'+p1+'</td>');
        var total = 0;
        platforms.forEach(function(p2){
            var score =  pwScores[p1 + ":" + p2];
            if (score || score == 0) {
                var tourId = tournament.id;

                var scoreBtn = $('<a href="" class="button" tourId="'+tourId+'" p1="'+p1+'" p2="'+p2+'">'+score.toFixed(2)+'</a>');

                scoreBtn.on('click', function (e) {
                    e.preventDefault();
                    var tourId = $(this).attr('tourId');
                    var p1 = $(this).attr('p1');
                    var p2 = $(this).attr('p2');
                    var card = $('#matches-card-'+tourId);
                    card.replaceWith(matchesCard(tournaments[tourId], p1, p2));
                });
                tBody.append($('<td />').append(scoreBtn));
                total += score;
            } else {
                tBody.append('<td>'+"-"+'</td>');
            }

        });

        pScores[p1] = total;

        tBody.append('<td>'+total.toFixed(2)+'</td>');
        tBody.append('</tr>');
    });

    // tHead.append($('<tr><th>Name</th><th>Dataset</th><th>Reference Output</th><th>Prop. File</th><th>Zip</th><th>Status</th><th>Description</th></tr>'))

    tBody.append();

    card.append(table);
    return card;
}




function matchesCard(tournament, platformA, platformB) {

    var card = $('<div class="card" id="matches-card-'+tournament.id+'" />');
    card.append($('<h3>Matches</h3>'));


    if(platformA  && platformB) {

        var table = $('<table class="table table-no-bordered">');
        var tHead = $('<thead></thead>');
        var tBody = $('<tbody></tbody>');
        table.append(tHead);
        table.append(tBody);

        tHead.append('<tr />');
        tHead.find('tr').append('<th></th>');
        tHead.find('tr').append('<th>'+platformA+'</th>');
        tHead.find('tr').append('<th>'+platformB+'</th>');

        tournament.types.forEach(function (type) {

            var match = tournament.matches[type.name + ":" + platformA + ":" + platformB];
            if (match) {

                var tourScore = platformScoresV1(match.scoreA, match.scoreB);

                var tScoreA = tourScore.scoreA;
                var tScoreB = tourScore.scoreB;
                tBody.append('<tr>');

                var tourId = tournament.id;
                var matchName = type.name + ":" + platformA + ":" + platformB;

                var matchLink = $('<a href="" class="button" tourId="' + tourId + '" matchName=' + matchName + ' >' + type.name + '</a>');

                tBody.append($('<td>').append(matchLink));
                matchLink.on('click', function (e) {
                    e.preventDefault();
                    var tourId = $(this).attr('tourId');
                    var matchName = $(this).attr('matchName');
                    var card = $('#match-card-' + tourId);
                    card.replaceWith(matchCard(tournaments[tourId], tournaments[tourId].matches[matchName]));
                });

                function resText(tScore, mScore, jobSize) { return  tScore + ' (' + mScore.toFixed(2) + '/' + jobSize };
                tBody.append('<td class="'+colorClass(tScoreA)+'">' + resText(tScoreA, match.scoreA, match.jobSize) + ')</td>');
                tBody.append('<td class="'+colorClass(tScoreB)+'">' + resText(tScoreB, match.scoreB, match.jobSize) + ')</td>');

                tBody.append('</tr>');

            }
        });
        card.append(table);
    } else {
        card.append($('<div>Choose matches between two plaforms.</div>'))
    }


    return card;
}

function matchCard(tournament, match) {

    var card = $('<div class="card" id="match-card-'+tournament.id+'"/>');
    card.append($('<h3>Match </h3>'));

    if(match) {
        card.find("h3").append($(' <small>('+match.type+')</small>'));

        var table = $('<table class="table table-no-bordered">');
        var tHead = $('<thead></thead>');
        var tBody = $('<tbody></tbody>');
        table.append(tHead);
        table.append(tBody);

        tHead.append('<tr />');
        tHead.find('tr').append('<th></th>');
        tHead.find('tr').append('<th>' + match.platformA + '</th>');
        tHead.find('tr').append('<th>' + match.platformB + '</th>');

        var instances = match.instances;

        for(var i in instances) {
            var instance = instances[i];
            var itemA = instance.setA;
            var itemB = instance.setB;

            tBody.append('<tr>');
            tBody.append('<td>'+instance.id+'</td>');
            function valueText(value, unit) { return (value < 0) ? "-1==failed" : ""+value.toFixed(2) + ' ' + unit};
            tBody.append('<td class="'+colorClass(itemA.score)+'">'+itemA.score+ ' ('+ valueText(itemA.value, itemA.unit) +')</td>');
            tBody.append('<td class="'+colorClass(itemB.score)+'">'+itemB.score+ ' ('+ valueText(itemB.value, itemB.unit) +')</td>');
            tBody.append('</tr>');
        }

        tBody.append();

        card.append(table);
    } else {
        card.append($('<div>Choose a match.</div>'));
    }

    return card;
}


function colorClass(score) {
    if(score ==1) {
        return "bg-success";
    } else if(score == 0.5) {
        return "bg-info";
    } else if (score == 0) {
        return  "bg-danger";
    } else if(score == -1) {
        return "bg-primary";
    }

}

