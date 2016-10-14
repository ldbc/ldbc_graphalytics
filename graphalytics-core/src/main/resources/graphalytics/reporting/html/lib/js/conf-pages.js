

function confTab() {

    var tab = $('<div ng-controller="conf-tab"></div>');


    tab.append($('<h3 class="text-muted title">Benchmark Configuration</h3><hr>'));
    tab.append("<p>This section describes the benchmark configuration.</p>");


    try {
        var conf = results.configuration;

        tab.append($('<h3>Target-scale</h3>'));
        var scaleText = "The target-scale for this benchmark is of size <strong>" + conf["target_scale"] + "</strong>.";
        tab.append($('<p />').append(scaleText));
        tab.append($('<br>'));

        tab.append($('<h3>Resources</h3>'));
        tab.append(resourcesTable(conf.resources));
        tab.append($('<br>'));

        tab.append($('<br>'));
    } catch(err) {
        printFast("Configuration page cannot be loaded due to: '" + err + "'.");
    }

    return tab;
}


function resourcesTable(resources) {
    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    tHead.append($('<tr><th>Type</th><th>Baseline</th><th>Scalability</th></tr>'));

    function tRow(name, baseline, scalable) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + baseline + '</strong></td><td><strong>' + scalable + '</strong></td>');
    }

    for(var r in resources) {
        var resource = resources[r];
        tBody.append(tRow(resource.name, resource.baseline, resource.scalability));
    }
    return table;
}





