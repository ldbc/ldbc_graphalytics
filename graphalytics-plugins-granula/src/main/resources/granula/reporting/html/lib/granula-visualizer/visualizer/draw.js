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


function drawIntro() {
    $('#intro-container').load('doc/intro/intro.htm');
}

function drawDoc() {
    $('#doc-container').load('doc/doc.htm');
}

function drawAbout() {
    $('#about-container').load('doc/about/about.htm');
}


function drawDashboard() {

    drawJobLists();

    var dashboard = $("#dashboard");
    dashboard.find('h2').html("Dashboard");
    $(".description").html("Here is an overview of the Granula archives that you are reviewing.");
    $("#archiveURL").val("");

    $("#add-arc-form").on("submit", function (e) {
        e.preventDefault();
        var arcUrlInput = $("#archiveURL");
        if(arcUrlInput.val() !== "") {
            addJobList(arcUrlInput.val());
            arcUrlInput.val("");
            drawJobLists();
            //drawArchiveTable();
        }
    });
}

function getJobItm(jobArc, arcId) {

    var jobItm = $('<li class="list-group-item job-item">' + jobArc.type + " [ " + jobArc.name  + " ]" +'</li>');
    if(selectedJobArchive == jobArc) {
        jobItm.addClass("selected-job-item");
    }
    jobItm.append(' (' + jobArc.status + ')');

    var jobBtnGroup = $('<span class="pull-right"></span>');

    var jobLoadBtn = $('<button class="btn btn-xs btn-info"></button>');
    jobLoadBtn.attr('arc-id', arcId);
    jobLoadBtn.attr('job-arc-uuid', jobArc.uuid);
    jobLoadBtn.append($('<span class="glyphicon glyphicon glyphicon-play"></span>') );
    jobLoadBtn.append(' Load');
    jobBtnGroup.append(jobLoadBtn);

    jobBtnGroup.append(" ");

    var jobViewBtn = $('<button class="btn btn-xs btn-info"></button>');
    jobViewBtn.attr('arc-id', arcId);
    jobViewBtn.attr('job-arc-uuid', jobArc.uuid);
    //jobViewBtn.attr('op-uuid', job.topOperation.uuid);
    jobViewBtn.append($('<span class="glyphicon glyphicon glyphicon-fullscreen"></span>') );
    jobViewBtn.append(' View');
    jobViewBtn.prop("disabled", true);
    jobBtnGroup.append(jobViewBtn);




    jobItm.append(jobBtnGroup);

    var drptTxt = $('<p class="list-group-item-text"></p>');
    drptTxt.html('<a href="' + fullUrl(jobArc.url) + '" target="_blank">' + fullUrl(jobArc.url) + '</a>');

    jobItm.append(drptTxt);

    jobViewBtn.on("click", function(){
        selectTarget($(this).attr('arc-id'), $(this).attr('job-arc-uuid'));
        displayJobPerfermance();
    });

    jobLoadBtn.on("click", function(){
        selectTarget($(this).attr('arc-id'), $(this).attr('job-arc-uuid'));
        loadJobArchive($(this).attr('arc-id'), $(this).attr('job-arc-uuid'));
    });


    if(jobArc.file) {

        jobViewBtn.prop("disabled", false);

        jobLoadBtn.empty();
        jobLoadBtn.append($('<span class="glyphicon glyphicon glyphicon-pause"></span>') );
        jobLoadBtn.append(" Unload");
        jobLoadBtn.off();
        jobLoadBtn.on("click", function(){
            unloadJobArchive($(this).attr('arc-id'), $(this).attr('job-arc-uuid'));
        });
    }


    return jobItm;
}

function getJobListItm(archive) {



    var arcItm = $('<li class="list-group-item"></li>');

    arcItm.append($('<span class="glyphicon glyphicon-pushpin"></span>'));
    arcItm.append($('<strong>' + ' ' + get2Digit(archive.id) + " "+ getFilename(archive.url) + '</strong>'));
    arcItm.append(' (' + archive.status + ')');

    var btnGroup = $('<span class="pull-right"></span>');

    var loadBtn = $('<button class="btn btn-xs btn-info"></button>');
    loadBtn.attr('arc-id', archive.id);
    loadBtn.append($('<span class="glyphicon glyphicon glyphicon-play"></span>') );
    loadBtn.append(' Load');

    //var saveBtn = $('<button class="btn btn-xs btn-info"></button>');
    //saveBtn.attr('arc-id', archive.id);
    //saveBtn.append($('<span class="glyphicon glyphicon glyphicon-floppy-save"></span>') );
    //saveBtn.append(' Save');

    var viewBtn = $('<button class="btn btn-xs btn-info" data-toggle="collapse" class="clickable"></button>');
    viewBtn.attr('arc-id', archive.id);
    viewBtn.attr('data-target', '#arc-' + archive.id +'-job-pnl');
    viewBtn.append($('<span class="glyphicon glyphicon glyphicon-fullscreen"></span>'));
    viewBtn.append(' View');

    btnGroup.append(loadBtn);
    btnGroup.append(" ");
    //btnGroup.append(saveBtn);
    //btnGroup.append(" ");
    btnGroup.append(viewBtn);
    arcItm.append(btnGroup);

    loadBtn.on("click", function(){
        loadJobList($(this).attr('arc-id'));
    });

    //saveBtn.on("click", function(){
    //    window.open(
    //        getArchiveById($(this).attr('arc-id')).url,
    //        '_blank'
    //    );
    //});

    viewBtn.on("click", function(){
        //selectTarget($(this).attr('arc-id'), null);
        //displayJobPerfermance();
    });

    //saveBtn.prop("disabled", true);
    viewBtn.prop("disabled", true);


    var drptTxt = $('<p class="list-group-item-text"></p>');
    drptTxt.html('<a href="' + fullUrl(archive.url) + '" target="_blank">' + fullUrl(archive.url) + '</a>');

    arcItm.append(drptTxt);

    //loadJobList(archive.id);

    if(archive.file) {

        loadBtn.empty();
        loadBtn.append($('<span class="glyphicon glyphicon glyphicon-pause"></span>') );
        loadBtn.append(" Unload");
        loadBtn.off();
        loadBtn.on("click", function(){
            unloadJobList($(this).attr('arc-id'));
        });

        //saveBtn.prop("disabled", false);
        viewBtn.prop("disabled", false);

        var jobListPnl = $('<div id="" class="panel panel-default collapse"></div>');
        jobListPnl.attr('id', 'arc-' + archive.id +'-job-pnl');
        if(selectedJobList && archive.id == selectedJobList.id) {
            jobListPnl.addClass("in")
        }
        var jobList = $('<ul class="list-group"></ul>');
        jobListPnl.append(jobList);

        var jobArcs = archive.jobArcs;
        for(var i = 0; i < jobArcs.length; i++) {
            jobList.append(getJobItm(jobArcs[i], archive.id));
        }

        arcItm.append(jobListPnl);
    }

    return arcItm;
}


function drawJobLists() {
    var dashboard = $("#dashboard");

    var jobListsPnl = dashboard.find("#arc-list-pnl");

    jobListsPnl.find(".panel-heading").text("Recently viewed Granula Archives");

    var jobListsContainer = jobListsPnl.find(".arc-list");
    jobListsContainer.empty();

    for(var i = 0; i < jobLists.length; i++) {
        var archive = jobLists[i];
        jobListsContainer.append(getJobListItm(archive));
    }
}

function drawTopOperation() {
    $(selectedJobArchive.file).children("Job").children("Operations").children("Operation").each(function () {
        drawOperation(new Operation($(this)).uuid);
    });

}


function getAncestorLink(operation) {

    var currentOperation = operation;

    var ancestorLink = $('<p></p>');

    ancestorLink.prepend('&nbsp;&nbsp;>&nbsp;&nbsp;' + currentOperation.getTitle());
    currentOperation = currentOperation.getSuperoperation();

    while(currentOperation !== null) {
        var parentLink = $('<a>' + currentOperation.getTitle() +'</a>');
        parentLink.attr('uuid', currentOperation.uuid);

        parentLink.on('click', function (e) {
            e.preventDefault();
            selectedOperationUuid = $(this).attr('uuid');
            drawJobPerformance();
        });
        ancestorLink.prepend(parentLink);
        ancestorLink.prepend('&nbsp;&nbsp;>&nbsp;&nbsp;')
        currentOperation = currentOperation.getSuperoperation();
    }

    var jobLink = $('<a>' + selectedJobArchive.type + " [ " + selectedJobArchive.name  + " ]" + '</a>');


    jobLink.on('click', function (e) {
        e.preventDefault();
        if(selectedJobList) {
            displayDashboard();
        }
    });

    //ancestorLink.prepend('&nbsp;&nbsp;');
    ancestorLink.prepend( jobLink);

    return ancestorLink;
}

function drawOperation(operationId) {

    var perfBoard = $("#perfboard");
    //perfBoard.find('h2').html("Job Performance");


    if(selectedOperationUuid != operationId) {
        transLevel = 1;
    }

    selectedOperationUuid = operationId;

    var operationNode = $(selectedJobArchive.file).children("Job").children("Operations").find('Operation[uuid="' + operationId + '"]');
    plotOperation(operationNode);

    var perfBoard = $("#perfboard");

    perfBoard.find('h2').empty().html('Job Performance Visualizer');
    perfBoard.find('.description').html('<h3><small><p class="ancestors-link"></p></small></h3>');
    perfBoard.find('.description').find('.ancestors-link').append(getAncestorLink(new Operation(operationNode)));


    var btnGroup = perfBoard.find('.header-btn-group');
    btnGroup.empty();

    var shareBtn = $('<button class="btn btn-default share-btn">Share</button>');
    var url = getDomainURL() + '?' + 'arc=' + selectedJobArchive.url + '&' + 'operation=' + selectedOperationUuid;
    shareBtn.attr('url', url);

    if(isDomainLocal()) {
        //shareBtn.prop("disabled", true);
    }

    var viewBtn = $('<button class="btn btn-default view-btn">View</button>');

    var dwnBtn = $('<button class="btn btn-default share-btn">Download</button>');
    var arcUrl = selectedJobArchive.url;
    dwnBtn.attr('arc-url', arcUrl);



    btnGroup.append(shareBtn);
    btnGroup.append(" ");
    btnGroup.append(viewBtn);
    btnGroup.append(" ");
    btnGroup.append(dwnBtn);
    //btnGroup.append(" ");
    //btnGroup.append(dashboardBtn);

    //dashboardBtn.on('click', function() {
    //    displayDashboard();
    //})

    shareBtn.on("click", function(){

        var modal = $("#share-modal");
        var mdlBody = modal.find(".modal-body");
        var mdlTitle = modal.find(".modal-title");
        mdlTitle.html('Sharing/Bookmarking the Current View');
        var drptText = modal.find(".drpt-text");
        var urlPnl = modal.find(".url-pnl");

        var job = new Job($(selectedJobArchive.file).find("Job"));
        var operation = new Operation($(selectedJobArchive.file).children("Job").children("Operations").find('Operation[uuid="' + operationId + '"]'));
        var contextText = 'You are currently viewing Operation [' + operation.name + '] of Job [' + job.name  + '], found in Granula Archive [' + selectedJobArchive.url + ']. ';
        var actionText = 'Please use the following URL to share/bookmark this view: ';
        drptText.empty().append($('<p>' + contextText + ' ' + actionText + '</p>'));

        var urlText = $(this).attr('url');
        urlPnl.empty().append($('<p>' + '<a href=' + urlText + ' target="_blank">' + urlText + '</a>' + '</p>'));

        showShareModal();
        //window.prompt("Use the following URL to share the current view:", $(this).attr('url'));
    });

    viewBtn.on("click", function(){
        var operationNode = $(selectedJobArchive.file).children("Job").children("Operations").find('Operation[uuid="' + operationId + '"]');
        var xmlFull = (new XMLSerializer()).serializeToString(operationNode[0]);
        var xmlIm = $(xmlFull);
        xmlIm.children('Children').children('Operation').empty();
        xmlIm.children('Visuals').children('Visual').empty();
        xmlIm.children('Infos').children('Info').empty();
        var xml = getPrintableXml(xmlIm[0]);
        drawXmlModal((new Operation(operationNode)).getTitle(), xml);
        showXmlModal();

    });

    dwnBtn.on("click", function(){
        window.open(
            $(this).attr('arc-url'),
            '_blank'
        );
    });
}


function plotOperation(operation) {

    var visualNodes = operation.children("Visuals").children("Visual");

    var op = new Operation(operation);
    var board = new Board();

    var titleFrame = new Frame();
    board.addFrame(titleFrame);
    var titleVisual = new TitleVisual('Operation ' + op.getTitle());
    titleFrame.setVisual(titleVisual);

    var drptFrame = new Frame();
    board.addFrame(drptFrame);
    var drptVisual = new DescriptionVisual(operation.children("Visuals").children("Visual[name=SummaryVisual]"));
    drptFrame.setVisual(drptVisual);

    var operationFrame = new Frame();
    board.addFrame(operationFrame);
    var operationVisual = new OperationVisual(op);
    operationFrame.setVisual(operationVisual);

    visualNodes.each(function () {
        var visualNode = $(this);
        var d =  (new Date());
        if(visualNode.attr("name") !== "AllInfoVisual" && visualNode.attr("name") !== "SummaryVisual") {

            if(visualNode.attr("type") == "TableVisual") {
                var visualFrame = new Frame();
                board.addFrame(visualFrame);
                var visual = new TableVisual(visualNode);
                visualFrame.setVisual(visual);
            }

            if(visualNode.attr("type") == "TimeSeriesVisual") {
                var visualFrame = new Frame();
                board.addFrame(visualFrame);
                var visual = new TimeSeriesVisual(visualNode);
                visualFrame.setVisual(visual);
            }
        }
    });

    //var linechartFrame = new Frame();
    //board.addFrame(linechartFrame);
    //var visualStats = new LSC_VisualStats();
    //visualStats.load(op);
    //var linechartVisual = new LineChartVisual(visualStats);
    //linechartFrame.setVisual(linechartVisual);

    var allInfoFrame = new Frame();
    board.addFrame(allInfoFrame);
    var allInfoVisual = new TableVisual(operation.children("Visuals").children("Visual[name=AllInfoVisual]"));
    allInfoFrame.setVisual(allInfoVisual);

    board.construct();
    board.position();
    board.draw();
}

function drawNavPanel() {


    var dashboardBtn = $("#dashboard-btn");
    dashboardBtn.on("click", function(e) {
        e.preventDefault();
        $('#nav-container li').removeClass('active');
        $(this).parent().addClass('active');
        displayDashboard();
    });

    var homeBtn = $("#intro-btn");
    homeBtn.on("click", function(e) {
        e.preventDefault();
        $('#nav-container li').removeClass('active');
        $(this).parent().addClass('active');
        displayIntro();
    });

    var docBtn = $("#doc-btn");
    docBtn.on("click", function(e) {
        e.preventDefault();
        $('#nav-container li').removeClass('active');
        $(this).parent().addClass('active');
        displayDoc();
    });

    var aboutBtn = $("#about-btn");
    aboutBtn.on("click", function(e) {
        e.preventDefault();
        $('#nav-container li').removeClass('active');
        $(this).parent().addClass('active');
        displayAbout();
    });

}

function drawFooter() {
    $(".footer").append("<p>Granula Visualizer 0.1.2 &copy; 2015 Parallel and Distributed Systems Group, Delft University of Technology ");
    $(".footer").append("<p>Access time: " + new Date() + "</p>");

}
