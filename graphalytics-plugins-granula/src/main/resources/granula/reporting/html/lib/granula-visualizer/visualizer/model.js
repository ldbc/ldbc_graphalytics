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


function Actor(type, id, uuid) {

    this.type = type;
    this.id = id;
    this.uuid = uuid;

    this.toString = function () {
        var str = this.type;
        if(this.id != "Id.Unique" && this.id !== "Id.LocalUnique") {
            str += "-" + this.id;
        }
        return str;
    };
}

function getOperationActor(operation) {

    var operationActor = operation.children("Actor");
    var type = operationActor.attr("type");
    var id = operationActor.attr("id");
    var uuid = operationActor.attr("uuid");
    return new Actor(type, id, uuid);
}

function getOperationSubactors(operation) {

    var actors = operation.children("Children").children("Operation").map( function () { return getOperationActor($(this)); }).get();
    var uniqueActors = _.unique(actors, function (actor) { return actor.uuid; });
    var sortedUniqueActors =  _(uniqueActors).sortBy(function(actor){ return actor.id; });

    return sortedUniqueActors;
}


function Mission(type, id, name, uuid) {

    this.type = type;
    this.id = id;
    this.name = name;
    this.uuid = uuid;

    this.toString = function () {
        var str = this.type;
        if(this.id != "Id.Unique" && this.id !== "Id.LocalUnique") {
            str += "-" + this.id;
        }
        return str;
    };
}

function getOperationMission(operation) {

    var operationMission = operation.children("Mission");
    var type = operationMission.attr("type");
    var id = operationMission.attr("id");
    var uuid = operationMission.attr("uuid");
    return new Mission(type, id, uuid);
}


function Job(jobNode) {
    this.node = jobNode;
    this.topOperation = new Operation(jobNode.children("Operations").children("Operation"));
    this.startTime = this.topOperation.getInfo("StartTime").value;
    this.endTime = this.topOperation.getInfo("EndTime").value;
    this.name =  (new Info(this.node.children("Infos").children("Info[name=JobName]"))).value;
    this.uuid = jobNode.attr("uuid");

    this.toString = function () {
        return 'Job (' + this.startTime + ' ' + this.endTime + ')';
    };
}


function JobList(jobListId, url) {
    this.id = jobListId;
    this.url = url;

    this.file = null;
    this.jobArcs = [];

    this.status = "Unverified";

    this.load = function() {
        this.status = "Loaded";
    }

    this.unload = function() {
        this.file = null;
        this.status = "Unloaded";
    }
}


function JobArchive(jobNode) {

    if(jobNode) {
        this.uuid = jobNode.attr("uuid");
        this.name = jobNode.attr("name");
        this.type = jobNode.attr("type");

        this.url = jobNode.children("Url").text();
        this.description = jobNode.children("Description").text();

        this.file = null;
        this.status = "Unverified";

        this.load = function() {
            this.status = "Loaded";
        }

        this.unload = function() {
            this.file = null;
            this.status = "Unloaded";
        }
    } else {
        this.uuid = "unspecified";
        this.name = "unspecified";
        this.type = "unspecified";

        this.url = "unspecified";
        this.description = "unspecified";

        this.file = null;
        this.status = "Unverified";

        this.load = function() {
            this.status = "Loaded";
        }

        this.unload = function() {
            this.file = null;
            this.status = "Unloaded";
        }
    }


}

function Operation(operationNode) {
    this.node = operationNode;
    this.uuid = operationNode.attr("uuid");
    this.name = operationNode.attr('name');

    this.getTitle = function () {
        var actor = getOperationActor(this.node);
        var mission = getOperationMission(this.node);
        var operationStr = "[" + actor + " @ " + mission + "]";

        return operationStr;
    };

    this.getActor = function() {
        var actor = this.node.children("Actor");
        var type = actor.attr("type");
        var id = actor.attr("id");
        var uuid = actor.attr("uuid");
        return new Actor(type, id, uuid);
    };

    this.getMission = function() {
        var mission = this.node.children("Mission");
        var type = mission.attr("type");
        var id = mission.attr("id");
        var name = mission.attr("name");
        var uuid = mission.attr("uuid");
        return new Mission(type, id, name, uuid);
    };

    this.getInfos = function() {
        return this.node.children("Infos").children("Info").map(function () { return new Info($(this))}).get();
    };

    this.getSubactors = function() {

        var actors = this.node.children("Children").children("Operation").map( function () { return getOperationActor($(this)); }).get();
        var uniqueActors = _.unique(actors, function (actor) { return actor.uuid; });
        var sortedUniqueActors =  _(uniqueActors).sortBy(function(actor){ return parseInt(actor.id); });

        return sortedUniqueActors;
    };

    this.getSuboperations = function() {
        return this.node.children("Children").children("Operation").map(function () { return new Operation($(this))}).get();
    };

    this.getSuperoperation = function() {
        if(this.node.parent("Children").parent("Operation").length > 0) {
            return new Operation(this.node.parent("Children").parent("Operation"));
        } else {
            return null;
        }
    };

    this.getTopoperation = function() {

        var currentNode = this.node;
        while(currentNode.parent("Children").parent("Operation").length !== 0) {

            var currentNode = currentNode.parent("Children").parent("Operation");
        }
        return new Operation(currentNode);
    };

    this.getInfo = function(name) {
        var info = this.node.children("Infos").find("Info[name=" + name + "]");
        return new Info(info);
    };
}
function Info(infoNode) {
    this.node = infoNode;
    this.name = infoNode.attr("name"),
    this.value = infoNode.attr("value");
    this.type = infoNode.attr("type");
    this.uuid = infoNode.attr("uuid");
    this.description = infoNode.children("description").text();
    this.owner = infoNode.parent("Infos").parent();

}

function Source(sourceNode) {
    this.name = sourceNode.attr("name"),
    this.type = sourceNode.attr("type");
    if(this.type == "InfoSource") {
        this.infoUuids = sourceNode.attr("infoUuid").split(";");
    }
    if(this.type == "RecordSource") {
        this.recordLocation = sourceNode.attr("location").split(";");
    }

}

function SummaryInfo(infoNode) {
    this.inheritFrom = Visual;
    this.inheritFrom();

    this.name = infoNode.attr("name"),
    this.value = infoNode.attr("value");
    this.type = infoNode.attr("type");
    this.uuid = infoNode.attr("uuid");
    this.summary = infoNode.children("Summary").text();
}

function Timeline(name, unit, axisId) {
    this.name = name;
    this.unit = unit;
    this.axisId = axisId;
    this.datapoints = [];
    this.addDatapoint = function(timestamp, value) {
        this.datapoints.push(new Datapoint(timestamp, value));
    };
}

function Datapoint(timestamp, value) {
    this.timestamp = timestamp;
    this.value = value;
}

function LSC_VisualStats() {
    this.title;
    this.timelines = [];
    this.xAxisInfo;
    this.y1AxisInfo;
    this.y2AxisInfo;
    this.gridSpace;

    this.load = function(operation) {

        var startTime = parseFloat(operation.getInfo("StartTime").value);
        var endTime = parseFloat(operation.getInfo("EndTime").value);

        var timelines = [];

        var timelines1 = this.createTimelines(startTime, endTime, 1000, 2);
        var timelines2 = this.createTimelines(startTime, endTime, 1, 1);

        for(var i = 0; i < timelines1.length; i++) {
            timelines.push(timelines1[i]);
            timelines.push(timelines2[i]);
        }

        this.timelines = timelines;

        this.title = pickRandom(["Network Utilization", "Storage Utilization"]);

        this.xAxisInfo = new AxisInfo("Execution Time", "s");
        this.y1AxisInfo = new AxisInfo("Sent Messages", "Msgs");
        this.y2AxisInfo = new AxisInfo("Sent Messages Volume", "Bytes")

        this.gridSpace = this.getGridSpace(operation, timelines);

    }

    this.getGridSpace = function(operation, timelines) {

        var startTime = parseFloat(operation.getInfo("StartTime").value);
        var endTime = parseFloat(operation.getInfo("EndTime").value);

        var values1 = [];
        var values2 = [];
        for(var j = 0; j < timelines.length; j++) {
            var timeline = timelines[j];

            for(var i = 0; i < timeline.datapoints.length; i++) {
                if(timeline.axisId == 1) {
                    values1.push(timeline.datapoints[i].value);
                } else if ((timeline.axisId == 2)) {
                    values2.push(timeline.datapoints[i].value);
                } else {
                    console.error("Timeline cannot be drawn on y axis that are not y1 or y2.");
                }
            }
        }

        var valueRange1 = Math.max.apply(Math, values1) - Math.min.apply(Math, values1);

        var startValue1 = Math.min.apply(Math, values1) - valueRange1 * 0.1;
        var endValue1 = Math.max.apply(Math, values1) + valueRange1 * 0.1;

        var valueRange2 = Math.max.apply(Math, values2) - Math.min.apply(Math, values2);
        var startValue2 = Math.min.apply(Math, values2) - valueRange2 * 0.1;
        var endValue2 = Math.max.apply(Math, values2) + valueRange2 * 0.1;

        var gridSpace = new GridSpace(startTime, endTime, startValue1, endValue1, startValue2, endValue2);
        return gridSpace;
    }

    this.createTimelines = function(startTime, endTime, multifier, axisId) {

        var x1 = Math.round(startTime  + (endTime - startTime ) /10 * 2);
        var x2 = Math.round(startTime  + (endTime - startTime ) /10 * 5);
        var x3 = Math.round(startTime  + (endTime - startTime ) /10 * 7);

        var timelines = [];

        var msgTL1 = new Timeline("network-bytes-in", "bytes", axisId);
        msgTL1.addDatapoint(startTime, randomInt(200, 600 * multifier));
        msgTL1.addDatapoint(x1, randomInt(200, 600 * multifier));
        msgTL1.addDatapoint(x2, randomInt(200, 600 * multifier));
        msgTL1.addDatapoint(x3, randomInt(200, 600 * multifier));
        msgTL1.addDatapoint(endTime, randomInt(200, 600 * multifier));
        timelines.push(msgTL1);

        var msgTL2 = new Timeline("network-bytes-out", "bytes", axisId);
        msgTL2.addDatapoint(startTime, randomInt(200, 600 * multifier));
        msgTL2.addDatapoint(x1, randomInt(200, 600 * multifier));
        msgTL2.addDatapoint(x2, randomInt(200, 600 * multifier));
        msgTL2.addDatapoint(x3, randomInt(200, 600 * multifier));
        msgTL2.addDatapoint(endTime, randomInt(200, 600 * multifier));
        timelines.push(msgTL2);

        return timelines;
    }


}

function AxisInfo(title, unit) {
    this.title = title;
    this.unit = unit;
}


function GridSpace(startTime, endTime, startValue1, endValue1, startValue2, endValue2) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = endTime - startTime;

    this.startValue1 = startValue1;
    this.endValue1 = endValue1;
    this.valueRange1 = endValue1 - startValue1;

    this.startValue2 = startValue2;
    this.endValue2 = endValue2;
    this.valueRange2 = endValue2 - startValue2;

}
