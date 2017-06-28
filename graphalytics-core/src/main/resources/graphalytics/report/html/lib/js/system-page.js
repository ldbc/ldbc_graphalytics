function systemPage() {

    var tab = $('<div ng-controller="system-tab"></div>');

    tab.append($('<h3 class="text-muted title">System under Test</h3><hr>'));
    tab.append("<p>This section describes the system under test: " +
        "the platform (graph analytics platform), " +
        "the environment (cluster environment), " +
        "and the benchmarking tools (graphalytics etc).</p>");
    tab.append($('<br>'));

    try {
        var system = data.system;

        tab.append(systemCard(system));
        tab.append($('<br>'));

        tab.append(platformCard(system.platform));
        tab.append($('<br>'));

        tab.append(envCard(system.environment));
        tab.append($('<br>'));
        tab.append(machineCard(system.environment.machines));
        tab.append($('<br>'));

        tab.append(toolCard(system.tool));
        tab.append($('<br>'));
    } catch(err) {
        printFast("System page cannot be loaded due to: '" + err + "'.");
    }


    return tab;
}

function systemCard(system) {
    var card = $('<div class="card col-md-12" ></div>');
    card.append($('<h3>System</h3>'));

    card.append("<p>Information of the system-under-test:</p>");

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, version) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + version + '</strong></td>');
    }

    tBody.append(tRow("Platform", system.platform.name));
    tBody.append(tRow("Environment", system.environment.name));
    tBody.append(tRow("Pricing", "$" + system.pricing ));

    card.append(table);
    return card;
}

function platformCard(platform) {
    var card = $('<div class="card col-md-12" ></div>');
    card.append($('<h3>Platform</h3>'));

    card.append("<p>Information of the graph processing platform (software):</p>");

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, version) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + version + '</strong></td>');
    }

    tBody.append(tRow("Name", platform.name));
    tBody.append(tRow("Version", platform.version));
    tBody.append(tRow("Acronym", platform.acronym));
    tBody.append(tRow("Link", '<a href="'+platform.link+'">'+platform.link+'</a>'));

    card.append(table);
    return card;
}




function envCard(env) {
    var card = $('<div class="card col-md-12" ></div>');

    card.append($('<h3>Environment</h3>'));
    card.append("<p>Information of the cluster environment (hardware):</p>");

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(key, value) {
        return $('<tr />').append('<td>' + key + '</td><td><strong>' + value + '</strong></td>');
    }

    tBody.append(tRow("Name", env.name));
    tBody.append(tRow("Version", env.version));
    tBody.append(tRow("Acronym", env.acronym));
    tBody.append(tRow("Link", '<a href="'+env.link+'">'+env.link+'</a>'));


    card.append(table);
    return card;
}


function machineCard(machines) {
    var card = $('<div class="card col-md-12" ></div>');

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    tHead.append('<tr />');
    tHead.find('tr').append('<th>CPU</th>');
    tHead.find('tr').append('<th>Mem</th>');
    tHead.find('tr').append('<th>Network</th>');
    tHead.find('tr').append('<th>Storage</th>');
    tHead.find('tr').append('<th>Accelerator</th>');
    tHead.find('tr').append('<th>Quantity</th>');

    machines.forEach(function (m) {

        tBody.append( $('<tr />').append(
            '<td>' + m.cpu +'</td>' +
            '<td>' + m.memory + '</td>' +
            '<td>' + m.network + '</td>' +
            '<td>' + m.storage + '</td>' +
            '<td>' + m.accel + '</td>'+
            '<td>' + m.quantity + '</td>'
        ));
    });

    card.append(table);

    return card;
}



function toolCard(tools) {
    var card = $('<div class="card col-md-12" ></div>');

    card.append($('<h3>Benchmark Tools</h3>'));
    card.append("<p>The details of the benchmarking tools.</p>");

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, version, link) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + version + '</strong></td><td><strong>' + link + '</strong></td>');
    }

    for(var t in tools) {
        var tool = tools[t];
        tBody.append(tRow(tool.name, tool.version, '<a href="'+tool.link+'">'+tool.link+'</a>'));
    }

    card.append(table);
    return card;
}
