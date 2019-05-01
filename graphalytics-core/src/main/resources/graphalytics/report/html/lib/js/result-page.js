
function resultPage() {

    var tab = $('<div ng-controller="result-tab"></div>');

    tab.append($('<h3 class="text-muted title">Experimental Result</h3><hr>'));
    tab.append("<p>This section describes the experimental results.</p>");

    try {
        tab.append(resultPanel(data.result));
    } catch(err) {
        printFast("Result page cannot be loaded due to: '" + err + "'.");
    }



    return tab;
}

function resultPanel(result) {

    var panelContainer = $('<div class="row"><div class="col-md-12"></div></div>');
    var panel = $('<div class="card borderless-card col-centered" ></div>');
    panelContainer.find('div').append(panel);

    var header = $('<div class="card-header">' + "Graphalytics Experiments [benchmark: " + data.id + ']</div>');
    panel.append(header);
    panel.append(panelTabs(result));
    return panelContainer;
}

function panelTabs(result) {


    var tabDiv = $('<div role="tabpanel">');
    var tabList = $('<ul class="nav nav-tabs border-tabs" role="tablist" />');
    var tabContent = $('<div class="tab-content" />');

    tabDiv.append(tabList);
    tabDiv.append(tabContent);

    var index = 0;
    for(var e in result.experiments) {
        var exp = result.experiments[e];
        var labelClasses = (index==0) ? "active" : "";
        var tabLabel = $('<li class="nav-item"></li>');
        tabLabel.append($('<a class="nav-link job-nav-link '+ labelClasses +'" data-toggle="tab" href="#'+exp.id+'" role="tab">'+exp.type+'</a>'));
        tabList.append(tabLabel);

        var currentContent = (index==0) ? "active in" : "";
        var content = $('<div class="tab-pane fade '+currentContent+'" id="' + exp.id + '" role="tabpanel"></div>');

        content.append(panelTab(exp, result));
        tabContent.append(content);
        index++;
    }

    return tabDiv;
}


function panelTab(exp, result) {

    var tab = $('<div class="card col-md-12" ></div>');

    var text = "This experiment consists of "+ exp.jobs.length +" jobs. ";
    text += "TODO: A descriptive text regarding this experiment type: " + exp.type + ".";
    tab.append($('<br><p />').append(text));

    // var jobPnl = $('<div class="col-md-9" id="accordion" role="tablist" aria-multiselectable="true" />');
    // exp.jobs.forEach(function (j) {
    //     jobPnl.append(jobPanel(result.jobs[j], exp, result));
    // })
    // jobPnl.append($('<div class="panel panel-default" >&nbsp;</div>'));

    tab.append(jobCard(result, exp));
    tab.append(runCard(result, exp));


    return tab;
}

function jobCard(result, exp) {

    if(exp.jobs.length > 0) {
        var card = $('<div class="card col-md-5" ></div>');
        card.append($('<h4>Jobs</h4>'));
        card.append($('<p>The list of jobs in Experiment [' + exp.type+ '].</p>'));

        var table = $('<table class="table table-no-bordered">');
        var tHead = $('<thead></thead>');
        var tBody = $('<tbody></tbody>');
        table.append(tHead);
        table.append(tBody);

        tHead.append($('<tr><th>id</th><th>algorithm</th><th>dataset</th><th>resources</th></tr>'))

        exp.jobs.forEach(function (j) {

            var job = result.jobs[j];

            var jobIdBtn = $('<a href="" class="button" expId = "' + exp.id + '" jobId="' + job.id + '">' + job.id + '</a>');

            jobIdBtn.on('click', function (e) {
                e.preventDefault();
                var expId = $(this).attr('expId');
                var jobId = $(this).attr('jobId');
                var card = $('#run-card-' + expId);
                card.replaceWith(runCard(data.result, data.result.experiments[expId], data.result.jobs[jobId]));
            });

            var tRow = $('<tr />');
            tRow.append($('<td />').append(jobIdBtn));
            tRow.append('<td>' + job.algorithm + '</td>');
            tRow.append('<td>' + job.dataset + '</td>');
            tRow.append('<td>' + job.scale + 'x</td>');
            tBody.append(tRow);
        });

        card.append(table);

        return card;
    } else {
        return $('<div class="card borderless-card col-md-5" ></div>');
    }


}

function runCard(result, exp, job) {

    if(job) {
        var card = $('<div class="card col-md-7" id="run-card-'+exp.id+'" ></div>');
        card.append($('<h4>Runs</h4>'))
        card.append($('<p>The list of runs in Job [' + job.id + '].</p>'));

        var table = $('<table class="table table-no-bordered">');
        var tHead = $('<thead></thead>');
        var tBody = $('<tbody></tbody>');
        table.append(tHead);
        table.append(tBody);

        tHead.append($('<tr><th>id</th><th>timestamp</th><th>success</th><th>load time</th><th>makespan</th><th>processing time</th></tr>'))

        job.runs.forEach(function (r) {
            var run = result.runs[r];
            var tRow = $('<tr />');
            if(run.archive_link) {
                tRow.append('<td><a href="' + run.archive_link + '">' + run.id + '</a></td>');
            } else {
                tRow.append('<td>' + run.id + '</td>');
            }
            tRow.append($('<td>' + '<div title="' + run.timestamp + '">' + timeConverter(run.timestamp) + '</div>' + '</td>'));
            tRow.append('<td>' + run.success + '</td>');
            if(run["load_time"] == "nan") {
                tRow.append('<td>' + "nan" + '</td>');
            } else {
                tRow.append('<td>' + run["load_time"] + ' s</td>');
            }

            if(run["makespan"] == "nan") {
                tRow.append('<td>' + "nan" + '</td>');
            } else {
                tRow.append('<td>' + run["makespan"] + ' s</td>');
            }

            if(run["processing_time"] == "nan") {
                tRow.append('<td>' + "nan" + '</td>');
            } else {
                tRow.append('<td>' + run["processing_time"] + ' s</td>');
            }

            tBody.append(tRow);
        })

        card.append(table);
        return card;
    } else {
        return  $('<div class="card borderless-card col-md-6" id="run-card-'+exp.id+'" ></div>');
    }



}
