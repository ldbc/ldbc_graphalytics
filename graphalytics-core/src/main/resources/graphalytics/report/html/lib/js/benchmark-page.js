

function benchmarkPage() {

    var tab = $('<div ng-controller="benchmark-tab"></div>');


    tab.append($('<h3 class="text-muted title">Benchmark Configuration</h3><hr>'));
    tab.append("<p>This section describes the benchmark configuration.</p>");


    try {
        var benchmark = data.benchmark;

        var upperRow = $('<div class="card-group col-md-12" ></div>');
        upperRow.append(typeCard(benchmark));
        upperRow.append(scaleCard(benchmark));
        tab.append(upperRow);
        tab.append($('<br>'));
        tab.append($('<br>'));

        var midRow = $('<div class="card-group col-md-12" ></div>');
        midRow.append(outputCard(benchmark.output));
        midRow.append(validationCard(benchmark.validation));
        tab.append(midRow);

        // tab.append(resourcesCard(benchmark.resources));
        // tab.append($('<br>'));

        tab.append(configurationCard(benchmark.configurations));
        tab.append($('<br>'));



        tab.append($('<br>'));
    } catch(err) {
        printFast("Benchmark page cannot be loaded due to: '" + err + "'.");
    }

    return tab;
}

function typeCard(benchmark) {

    var card = $('<div class="card col-md-6" ></div>');

    card.append($('<h3>Benchmark</h3>'));
    card.append($('<p>The name and the type of the benchmark.</p>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, value) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + value + '</strong></td>');
    }

    tBody.append(tRow("name", benchmark.name));
    tBody.append(tRow("type", benchmark.type));

    card.append(table);

    return card;

}

function scaleCard(benchmark) {

    var card = $('<div class="card col-md-6" ></div>');
    card.append($('<h3>Setup</h3>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, value) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + value + '</strong></td>');
    }

    if(benchmark.type == "standard") {
        tBody.append(tRow("target-scale", benchmark["target_scale"]));
        tBody.append(tRow("duration", Math.round(benchmark.duration /1000 /60) + " min"));
        tBody.append(tRow("timeout", benchmark.timeout + " seconds"));

    } else if (benchmark.type == "test") {
        tBody.append(tRow("target-scale", "2XS"));
        tBody.append(tRow("duration", Math.round(benchmark.duration /1000 /60) + " min"));
        tBody.append(tRow("timeout", benchmark.timeout + " seconds"));
    }



    card.append(table);

    // var scaleText;
    // if(benchmark.type.startsWith("standard")) {
    //     scaleText = "The target-scale for this benchmark is of size <strong>" + benchmark["target_scale"] + "</strong>.";
    // } else{
    //     scaleText = "The target-scale is not applicable for non-standard benchmark.";
    // }
    // tBody.append(tRow("duration", Math.round(benchmark.duration /1000 /60) + " min"));
    //
    // card.append($('<p />').append(scaleText));

    return card;
}


function outputCard(output) {

    var card = $('<div class="card col-md-6" ></div>');

    card.append($('<h3>Output</h3>'));
    card.append($('<p>The output configuration of the benchmark.</p>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, value) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + value + '</strong></td>');
    }

    tBody.append(tRow("output-enabled", output.required));
    tBody.append(tRow("output-directory", output.directory));

    card.append(table);

    return card;
}


function validationCard(validation) {


    var card = $('<div class="card col-md-6" ></div>');

    card.append($('<h3>Validation</h3>'));
    card.append($('<p>The validation configuration of the benchmark.</p>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);

    function tRow(name, value) {
        return $('<tr />').append('<td>' + name + '</td><td><strong>' + value + '</strong></td>');
    }

    tBody.append(tRow("validation-enabled", validation.required));
    tBody.append(tRow("validation-directory", validation.directory));

    tBody.append();

    card.append(table);
    return card;
}


//
//
// function resourcesCard(resources) {
//
//     var card = $('<div class="card col-md-12" ></div>');
//
//     card.append($('<h3>Resources</h3>'));
//     card.append($('<p>The resources configuration of the benchmark. ' +
//         'Baseline indicates the amount of resources being used during the baseline benchmark. ' +
//         'Scalability indicates if certain resources type is scalable during the scalability benchmark (if enabled).</p>'));
//
//     var table = $('<table class="table table-no-bordered">');
//     var tHead = $('<thead></thead>');
//     var tBody = $('<tbody></tbody>');
//     table.append(tHead);
//     table.append(tBody);
//     tHead.append($('<tr><th>Type</th><th>Baseline</th><th>Scalability</th></tr>'));
//
//     function tRow(name, baseline, scalable) {
//         return $('<tr />').append('<td>' + name + '</td><td><strong>' + baseline + '</strong></td><td><strong>' + scalable + '</strong></td>');
//     }
//
//     for(var r in resources) {
//         var resource = resources[r];
//         tBody.append(tRow(resource.name, resource.baseline, resource.scalability));
//     }
//
//     card.append(table);
//
//     return card;
// }



function configurationCard(configurations) {

    var card = $('<div class="card col-md-12" ></div>');

    card.append($('<h3>Configuration</h3>'));
    card.append($('<p>The benchmark configuration of the benchmark ' +
        '(with exception of graph dataset configuration).</p>'));

    var table = $('<table class="table table-no-bordered">');
    var tHead = $('<thead></thead>');
    var tBody = $('<tbody></tbody>');
    table.append(tHead);
    table.append(tBody);
    tHead.append($('<tr><th style="width: 400px">Key</th><th>Value</th></tr>'));

    function tRow(key, value) {
        return $('<tr />').append('<td>' + key + '</td><td><strong>' + value + '</strong></td>');
    }

    var keys = Object.keys(configurations);
    keys.sort();

    keys.forEach(function (key) {
        if(!key.startsWith("graph.")) {
            var value = configurations[key];
            tBody.append(tRow(key, value));
        }
    });

    card.append(table);

    return card;
}


