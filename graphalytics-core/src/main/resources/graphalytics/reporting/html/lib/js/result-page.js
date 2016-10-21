
function resultTab() {

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

    var panelContainer = $('<div class="row"><div class="col-md-12 col-centered"></div></div>');
    var panel = $('<div class="card text-xs-center job-card" ></div>');
    panelContainer.find('div').append(panel);

    var header = $('<div class="card-header dark job-border">' + "Graphalytics Experiments" + '</div>');
    panel.append(header);
    panel.append(panelTabs(result));
    return panelContainer;
}

function panelTabs(result) {


    var tabDiv = $('<div role="tabpanel">');
    var tabList = $('<ul class="nav nav-tabs" role="tablist" />');
    var tabContent = $('<div class="tab-content" />');

    tabDiv.append(tabList);
    tabDiv.append(tabContent);

    var index = 0;
    for(var e in result.experiments) {
        var exp = result.experiments[e];
        var labelClasses = (index==0) ? "active" : "";
        var tabLabel = $('<li class="nav-item"></li>');
        tabLabel.append($('<a class="nav-link '+ labelClasses +'" data-toggle="tab" href="#'+exp.id+'" role="tab">'+exp.type+'</a>'));
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

    var textPnl = $('<div class="panel panel-default col-md-3" />');
    var text = "This experiment consists of "+ exp.jobs.length +" jobs. ";
    text += "TODO: A descriptive text regarding this experiment type: " + exp.type + ".";
    textPnl.append($('<br><p />').append(text));

    var jobPnl = $('<div class="col-md-9" id="accordion" role="tablist" aria-multiselectable="true" />');
    exp.jobs.forEach(function (j) {
        jobPnl.append(jobPanel(result.jobs[j], exp, result));
    })
    jobPnl.append($('<div class="panel panel-default" >&nbsp;</div>'));

    tab.append(textPnl);
    tab.append(jobPnl);

    return tab;
}

function jobPanel(job, exp, result) {
    var pnl = $('<div class="panel panel-default" />');

    var contentId = job.id + '-' + exp.id + '-content';
    var header = $('<div class="panel-heading panel" role="tab" />');

    var jobText = "Job(" + "id=" + job.id + ", alg=" + job.algorithm + ", dataset=" + job.dataset + ", scale=" + job.scale + ", repetition=" + job.repetition + ")";
    header.append($('<br><button type="button" class="btn btn-secondary btn-group-lg col-md-12" a data-toggle="collapse" data-parent="#accordion" href="#'+ contentId +'" aria-expanded="true"></button>')
        .append($('<h5>'+jobText+'</h5>')));

    var content = $('<div id="'+ contentId + '" class="panel-collapse collapse col-md-12" role="tabpanel" />');

    content.append(runsPanel(job.runs, result));
    pnl.append(header);
    pnl.append(content);
    return pnl;
}

function runsPanel(jobRuns, result) {

    var pnl = $('<div class="card text-xs-center col-md-12" ></div>');

    var runs = {};
    jobRuns.forEach(function (r) {
        runs[r] = result.runs[r];
    })


    var table = $('<table class="table table-active">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    for(var r in runs) {
        var run = runs[r];

        tBody.append( $('<tr />').append(
            '<td>run=' + run.id +'</td>' +
            '<td>timestamp=' + run.timestamp +'</td>' +
            '<td>success=' + run.success +'</td>' +
            '<td>makespan=' + run.makespan +'</td>' +
            '<td>processing-time=' + run["processing_time"] +'</td>'
        ));
    }
    pnl.append(table);
    return pnl;
}
