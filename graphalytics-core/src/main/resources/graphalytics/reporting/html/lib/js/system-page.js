function systemTab() {

    var tab = $('<div ng-controller="system-tab"></div>');

    tab.append($('<h3 class="text-muted title">System under Test</h3><hr>'));
    tab.append("<p>This section describes the system under test.</p>");
    tab.append($('<br>'));


    var system = results.system;

    tab.append($('<h3>Platform</h3>'));
    tab.append(platformTable(system.platform));
    tab.append($('<br>'));

    tab.append($('<h3>Environment</h3>'));
    tab.append(envTable(system.environment));
    tab.append($('<br>'));
    tab.append(machineTable(system.environment.machines));
    tab.append($('<br>'));

    tab.append($('<h3>Benchmark</h3>'));
    tab.append(benchmarkTable(system.benchmark));
    tab.append($('<br>'));

    return tab;
}

function benchmarkTable(benchmark) {

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, version, link) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + version + '</strong></td><td><strong>' + link + '</strong></td>');
    }

    for(var b in benchmark) {
        var bench = benchmark[b];
        tBody.append(tRow(bench.name, bench.version, bench.link));
    }


    tBody.append();

    return table;
}



function platformTable(platform) {

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
    tBody.append(tRow("Link", platform.link));

    tBody.append();

    return table;
}



function envTable(env) {

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
    tBody.append(tRow("Link", env.link));

    tBody.append();

    return table;
}


function machineTable(machines) {

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
            '<td>' + m.cpu.name + ' (' + m.cpu.cores  + ' cores'+ ')' +'</td>' +
            '<td>' + m.memory.name  + ' (' + m.memory.size + ')' +'</td>' +
            '<td>' + m.network.name  + ' (' + m.network.throughput + ')' +'</td>' +
            '<td>' + m.storage.name + ' (' + m.storage.volume + ')' +'</td>' +
            '<td>' + m.accel.name  + ' (?)' +'</td>'+
            '<td>' + m.quantity + '</td>'
        ));
    });

    return table;
}
