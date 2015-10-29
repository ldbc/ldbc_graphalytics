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


var jobLists = [];
var selectedJobList = null;
var selectedJobArchive = null;
var selectedOperationUuid = null;
var transLevel = 1;

var tmpLineX;
var tmpLineY;

var fv_open_modals = 0;


function jobListAdded(archiveUrl) {
    if(_.filter(jobLists, function(archive) { return archive.url === archiveUrl}).length > 0) {
        return true;
    }
    else {
        return false;
    }
}

function addJobList(jobListUrl) {

    if(jobListAdded(jobListUrl)) {
        alert("Warning: Archive with url " + jobListUrl + " exists");
    } else{
        var archiveId = null;
        archiveId = (jobLists.length > 0 ) ? (_.last(jobLists)).id + 1 : 1;
        jobLists.push(new JobList(archiveId, jobListUrl));
    }

}



function loadArchiveAndDisplay(archive, jobUuid, operationId) {

    showLoadingDiv();

    //var archiveTableRow = $("#dashboard .table").find('tr[archiveId=' + archiveId + ']');

    if(!isSameOrigin(archive.url)) {
        alert('Error: file ' + archive.url +' does not follow the same origin policy');
        console.log('Error: file ' + archive.url +' does not follow the same origin policy');
        archive.status = "Failed";
        //archiveTableRow.find(".archive-status").html("Failed");
        return;
    }

    $.ajax({
        type: "GET",
        url: archive.url,
        dataType: 'xml',
        success: function(data){
            archive.file = data;
            archive.status = "Loaded";
            selectTarget(archive.id, jobUuid, operationId);

            if(jobUuid || operationId) {

                if($(selectedJobList.file).find('Job[uuid=' + jobUuid +']').length == 0) {
                    var opText = 'Tried to find Job [' + jobUuid + '] in Archive [' + archive.url +'].';
                    var errText = 'But Job [' + jobUuid + '] could not be found.';
                    var resText = "Redirecting to Dashboard."
                    drawNotificationTraceModal("Job Retrieval Failed", opText, errText, resText);
                    showNotificationModal();
                    displayDashboard();
                    selectedOperationUuid = null;
                    return;
                }

                if($(selectedJobList.file).find('Job[uuid=' + jobUuid +']').find('Operation[uuid=' + operationId +']').length == 0) {
                    var opText = 'Tried to find Operation [' + operationId + '] in Job [' + jobUuid +'].';
                    var errText = 'However Operation [' + operationId + '] could not be found.';
                    var resText = 'Redirecting to the top operation of Job[' + jobUuid + '].';
                    drawNotificationTraceModal("Job Retrieval Failed", opText, errText, resText);
                    showNotificationModal();
                    selectedOperationUuid = null;
                }

                displayJobPerfermance();
            } else {
                displayDashboard();
            }
        },
        xhr: function () {
            var xhr = new window.XMLHttpRequest();
            xhr.addEventListener("progress", function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = evt.loaded / evt.total;
                    $('#loading-bar').html('Loading Granula Archive [' + archive.url + '] - ' + (percentComplete * 100).toFixed(0) + '%')
                }
            }, false);
            return xhr;
        },
        error: function(xhr, textStatus, errorThrown){
            archive.status = "Failed";
            //displayDashboard();
            console.log('Error: url ' + archive.url +' cannot be loaded');

            var opText = 'Tried to load granula archive at path ' + archive.url +'.';
            var errText = 'But the loading of the archive was not successful.';
            var resText = "Redirecting to Dashboard."
            drawNotificationTraceModal("Archive loading Failed", opText, errText, resText);
            showNotificationModal();
            displayDashboard();
        }
    });


}

function loadJobList(jobListId) {

    var jobList = getJobListById(jobListId);

    jobList.status = "Loading";
    displayDashboard();

    if(!isSameOrigin(jobList.url)) {
        alert('Error: file ' + jobList.url +' does not follow the same origin policy');
        console.log('Error: file ' + jobList.url +' does not follow the same origin policy');
        jobList.status = "Failed";
        //archiveTableRow.find(".archive-status").html("Failed");
        return;
    }

    $.ajax({
        type: "GET",
        url: jobList.url,
        dataType: 'xml',
        success: function(data){
            jobList.file = data;
            jobList.status = "Loaded";
            jobList.jobArcs = $(jobList.file).children("Jobs").children("Job").map(function(){ return new JobArchive($(this))}).get();
            drawJobLists();
            //displayDashboard();
        },
        error: function(xhr, textStatus, errorThrown){
            var isCompatible = isUrlCompatible(jobArc.url);
            jobList.status = "Failed";
            console.log('Error: url ' + jobList.url +' cannot be loaded');
        }, xhr: function () {
            var xhr = new window.XMLHttpRequest();
            xhr.addEventListener("progress", function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = evt.loaded / evt.total;
                    jobList.status  = 'Loading ' + (percentComplete * 100).toFixed(0) + '%';
                    displayDashboard();
                }
            }, false);
            return xhr;
        }
    });


}



function loadArchiveFromUrl(arcUrl, operationUuid) {

    showLoadingDiv();

    var jobArc = new JobArchive()
    jobArc.url = fullUrl(arcUrl);

    if(!isSameOrigin(jobArc.url)) {
        alert('Error: file ' + jobArc.url +' does not follow the same origin policy');
        console.log('Error: file ' + jobArc.url +' does not follow the same origin policy');
        jobArc.status = "Failed";
        //archiveTableRow.find(".archive-status").html("Failed");
        return;
    }


    $.ajax({
        type: "GET",
        url: jobArc.url,
        dataType: 'xml',
        success: function(data){
            jobArc.file = data;
            jobArc.status = "Loaded";
            jobArc.name = $(data).children("job").attr("name")
            jobArc.uuid = $(data).children("job").attr("uuid")
            jobArc.type = $(data).children("job").attr("type")

            selectedJobList = null;
            selectedJobArchive = jobArc;
            if(operationUuid) {
                selectedOperationUuid = operationUuid;
            }
            displayJobPerfermance();
        },
        error: function(xhr, textStatus, errorThrown){
            var isCompatible = isUrlCompatible(jobArc.url);
            setTimeout(printFast(xhr.responseText), 5000)
            jobArc.status = "Failed";

            console.log('Error: url ' + jobArc.url +' cannot be loaded');
        }, xhr: function () {
            var xhr = new window.XMLHttpRequest();
            xhr.addEventListener("progress", function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = evt.loaded / evt.total;
                    jobArc.status  = 'Loading ' + (percentComplete * 100).toFixed(0) + '%';
                    displayJobPerfermance();

                }
            }, false);
            return xhr;
        }
    });


}


function loadJobArchive(archiveId, jobArcUuid) {

    var jobArc = getJobArchiveByUuid(archiveId, jobArcUuid);

    jobArc.status = "Loading";
    displayDashboard();

    if(!isSameOrigin(jobArc.url)) {
        alert('Error: file ' + jobArc.url +' does not follow the same origin policy');
        console.log('Error: file ' + jobArc.url +' does not follow the same origin policy');
        jobArc.status = "Failed";
        //archiveTableRow.find(".archive-status").html("Failed");
        return;
    }



    $.ajax({
        type: "GET",
        url: jobArc.url,
        dataType: 'xml',
        success: function(data){
            jobArc.file = data;
            jobArc.status = "Loaded";
            drawJobLists();
            //displayDashboard();
        },
        error: function(xhr, textStatus, errorThrown){
            jobArc.status = "Failed";
            //displayDashboard();
            console.log('Error: url ' + jobArc.url +' cannot be loaded');
        }, xhr: function () {
            var xhr = new window.XMLHttpRequest();
            xhr.addEventListener("progress", function (evt) {
                if (evt.lengthComputable) {
                    var percentComplete = evt.loaded / evt.total;
                    jobArc.status  = 'Loading ' + (percentComplete * 100).toFixed(0) + '%';
                    displayDashboard();
                }
            }, false);
            return xhr;
        }
    });


}



function unloadJobArchive(archiveId, jobArcUuid) {

    var jobArc = getJobArchiveByUuid(archiveId, jobArcUuid);

    jobArc.file = null;
    jobArc.status = "Unloaded";
    displayDashboard();

}


function unloadJobList(jobListId) {

    var jobList = getJobListById(jobListId);

    jobList.file = null;
    jobList.status = "Unloaded";
    jobList.jobArcs = [];
    displayDashboard();

}

function getJobListById(jogListId) {

    var matchedJobLists = _.filter(jobLists, function(archiveX) { return archiveX.id == jogListId});
    if(matchedJobLists.length != 1) {console.log('Error: Find ' + matchedJobLists.length  +' job list with id ' + jogListId)};

    return matchedJobLists[0];
}


function getJobArchiveByUuid(archiveId, jobArcUuid) {

    var archive = getJobListById(archiveId);
    var matchedJobArchives = _.filter(archive.jobArcs, function(jobArc) { return jobArc.uuid == jobArcUuid});
    if(matchedJobArchives.length != 1) {console.log('Error: Find ' + matchedJobArchives.length  +' job archives with uuid ' + jobArcUuid)};

    return matchedJobArchives[0];
}

function getJobListByURL(jobListUrl) {

    var matchedJobLists = _.filter(jobLists, function(jobList) { return jobList.url == jobListUrl});
    if(matchedJobLists.length != 1) {console.log('Error: Find ' + matchedJobLists.length  +' job list with url ' +  jobListUrl)};

    return matchedJobLists[0];
}


function selectTarget(archiveId, jobUuid, operationUuid) {
    selectedJobList = getJobListById(archiveId);
    selectedJobArchive = getJobArchiveByUuid(archiveId, jobUuid);
    selectedOperationUuid = operationUuid;
}