/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function drawNotificationTraceModal(title, operationText, errorText, resultText) {

    var modal = $("#notification-modal");
    var mdlBody = modal.find(".modal-body");
    var mdlTitle = modal.find(".modal-title");
    mdlTitle.html(title);
    mdlBody.empty();
    mdlBody.append($('<p>' + operationText + ' ' + errorText + ' ' + resultText + '</p>'));

}

function drawXmlModal(title, xml) {

    var modal = $("#xml-modal");
    var mdlBody = modal.find(".modal-body");
    var mdlTitle = modal.find(".modal-title");
    var preField = mdlBody.find('.prettyprint');
    mdlTitle.html(title);
    preField.html(xml);
    prettyPrint();
}

function drawInfoTraceModal(infoUuidsText) {

    var infoUuids = infoUuidsText.split("-");
    var infoUuid = infoUuids[infoUuids.length -1];

    var infoNode = $(selectedJobArchive.file).find('Info[uuid=' + infoUuid +']');
    var ownerNode = infoNode.parent("Infos").parent();

    var info = new Info(infoNode);

    if(ownerNode[0].tagName == "Operation") {
        var owner = new Operation(ownerNode);
    } else {
        console.error("Unknown information owner type");
    }

    var modal = $("#default-modal");
    var mdlBody = modal.find(".modal-body");
    var mdlTitle = modal.find(".modal-title");
    mdlTitle.html("Information Tracing");
    mdlBody.empty();



    var traceStack = $('<p />');
    for(var i = 0; i < infoUuids.length; i++) {
        var historyInfo = new Info($(selectedJobArchive.file).find('Info[uuid=' + infoUuids[i] +']'));
        var infoLink = $('<a>' + historyInfo.name + '@' + (new Operation(historyInfo.owner)).getTitle() + '</a>');
        traceStack.append($('<small> &#8680 </small>'));
        traceStack.append(infoLink);


        var partUuidText = "";
        for(var j = 0; j <= i; j++) {
            partUuidText += (partUuidText == "") ? infoUuids[j] : "-" + infoUuids[j];
        }

        infoLink.attr('uuid', partUuidText);

        infoLink.on('click', function (e) {
            e.preventDefault();
            drawInfoTraceModal($(this).attr('uuid'));
        });
    }
    mdlBody.append(traceStack);

    var infoContainer = $('<div class="panel-body tab-container"><div class="tab">Target Information</div><br></div>');
    mdlBody.append($('<div class="panel panel-default"></div>').append(infoContainer));

    infoContainer.append($('<p>' + info.description + '</p>'));


    var infoTblContainer = $('<div class="panel panel-default">');
    var infoTblLabel = $('<div class="panel-heading">Information [' + info.name + ']</div>');

    var infoTable = $('<table class="table table-bordered">');
    infoTable.append($('<tr><th>Name</th><th>Type</th><th>Value</th><th>Information Owner</th></tr>'));
    var row = $('<tr></tr>');
    row.append('<td>' + info.name + '</td>');
    row.append('<td>' + info.type + '</td>');
    row.append('<td>' + info.value + '</td>');
    row.append('<td>' + 'Operation ' + owner.getTitle() + '</td>');

    var xmlField = $('<div></div>');

    xmlField.hide();

    var printBtn = $('<button class="btn btn-xs trace-btn pull-right">');
    printBtn.append('<span class="glyphicon glyphicon glyphicon-menu-down"></span>' + '</button>');
    printBtn.attr('uuid', info.uuid);
    printBtn.attr('toggle', "off");
    printBtn.on("click", function(){
        if($(this).attr('toggle') == "off") {
            $(this).attr('toggle', "on");
            $(this).find('span').removeClass('glyphicon-menu-down');
            $(this).find('span').addClass('glyphicon-menu-up');
            var btnInfo = new Info($(selectedJobArchive.file).find('Info[uuid=' + $(this).attr('uuid') +']'));

            var printableXml = getPrintableXml((btnInfo.node)[0]);
            var preField = $('<pre class="prettyprint"></pre>');
            preField.html(printableXml);
            xmlField.append(preField);
            xmlField.show();
            prettyPrint();
        } else {
            $(this).attr('toggle', "off");
            $(this).find('span').addClass('glyphicon-menu-down');
            $(this).find('span').removeClass('glyphicon-menu-up');
            xmlField.empty();
            xmlField.hide();
        }
    });

    infoTblLabel.append(printBtn);
    infoTable.append(row);

    infoTblContainer.append(infoTblLabel);
    infoTblContainer.append(infoTable);
    infoContainer.append(infoTblContainer);
    infoContainer.append(xmlField);


    var sourcesContainer = $('<div class="panel-body tab-container"><div class="tab">Source List</div><br></div>');
    mdlBody.append($('<div class="panel panel-default"></div>').append(sourcesContainer));

    var sources = infoNode.children("Sources").children("Source").map( function () { return new Source($(this)); }).get();

    if(sources.length == 0) {
        sourcesContainer.append($('<p>Strangely, there are not any sources for this information.</p>'));
    }


    for(var i = 0; i < sources.length; i++) {

        var source = sources[i];

        var sourceTblContainer = $('<div class="panel panel-default">');
        var sourceTblLabel = $('<div class="panel-heading">Source [' + source.name + ']</div>');
        var sourceTable = $('<table class="table table-bordered">');

        if (source.type == "InfoSource") {
            sourceTable.append($('<tr><th>Name</th><th>Value</th><th>Information Owner</th></tr>'));
            for(var j = 0; j < source.infoUuids.length; j++) {
                var infoUUid = source.infoUuids[j];
                var sourceInfoNode = $(selectedJobArchive.file).find('Info[uuid=' + infoUUid +']');

                var sourceInfo = new Info(sourceInfoNode);
                var ownerNode = sourceInfoNode.parent("Infos").parent();
                var owner = new Operation(ownerNode);
                var row = $('<tr></tr>');

                var traceSign = $('<button class="btn btn-xs trace-btn" uuid="' + infoUuidsText + '-' + sourceInfo.uuid +'">');
                traceSign.append('<span class="glyphicon glyphicon glyphicon-zoom-in"></span>' + '</button>');

                var nameCell = $('<td></td>');
                row.append(nameCell);

                nameCell.append('<b>' + sourceInfo.name + '</b>');
                nameCell.append(traceSign);

                row.append('<td>' + sourceInfo.value + '</td><td>' + 'Operation ' + owner.getTitle() + '</td>');

                traceSign.on("click", function(){
                    drawInfoTraceModal($(this).attr("uuid"));
                });

                sourceTable.append(row);
            }
        } else {
            sourceTable.append($('<tr><th>Record Location</th><td>' + source.recordLocation  + '</td><</tr>'));
        }


        sourceTblContainer.append(sourceTblLabel);
        sourceTblContainer.append(sourceTable);
        sourcesContainer.append(sourceTblContainer);


    }
}
