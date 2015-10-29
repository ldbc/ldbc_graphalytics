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


var columns = new Columns();


function Columns() {

    this.minorColumn = null;
    this.majorColumn = null;

    this.setMinorColumn = function (minorColumn) {
        this.minorColumn = minorColumn;
    }

    this.setMajorColumn = function (majorColumn) {
        this.majorColumn = majorColumn;
    }

}

function Column(x, w) {
    this.x = x;
    this.w= w;
    this.x2 = x + w;
}


function Board() {

    this.inheritFrom = BoundingBox;
    this.inheritFrom();

    this.surface = null;
    this.drawable = null;
    this.frames = [];


    this.padding = 5;
    this.x = 0 + this.padding;
    this.y = 0 + this.padding;
    this.w = 1000;
    this.h = 0;



    this.setSurface = function () {

        $("#board").attr("width", (this.w + this.padding * 2));
        $("#board").attr("height", (this.h + this.padding * 2));
        this.surface = Snap("#board");
        this.surface.clear();

    };

    this.addFrame = function (frame) {
        this.frames.push(frame);
        frame.setBoard(this);
    };

    this.construct = function () {

        this.drawable = new RectDrawable();
        this.drawable.setColor("#FFF").setShadow().setRadius(7);

        for (var i = 0; i < this.frames.length; ++i) {
            this.frames[i].construct();
        }
    };

    this.position = function () {

        var y = this.y;
        for (var i = 0; i < this.frames.length; ++i) {
            this.frames[i].position(this.x, y);
            y += this.frames[i].h;
        }
        this.h = y;

        this.drawable.setCoordinate(this.x, this.y, this.w, this.h);

    };

    this.draw = function() {


        this.setSurface();

        this.drawable.setSurface(this.surface);
        this.drawable.draw();

        for (var i = 0; i < this.frames.length; ++i) {
            this.frames[i].draw();
        }
    };

    this.toString = function() {
        return "This board is at " + "x=" + this.x + ", y=" + this.y + ", width=" + this.w + ", height=" + this.h;
    }
}

function Frame() {

    this.inheritFrom = BoundingBox;
    this.inheritFrom();

    this.padding = 5;

    this.board = null;
    this.drawable = null;
    this.visual = null;

    this.setBoard = function (board) {
        this.board = board;
        this.w = this.board.w;
    };

    this.setVisual = function(visual) {
        this.visual = visual;
        this.visual.setFrame(this);
    };

    this.construct = function () {
        this.visual.construct();
        this.h = this.visual.h + this.padding * 2;
    };

    this.position = function (x, y) {
        this.x += x;;
        this.y += y;
        this.visual.position(x + this.padding * 2, y + this.padding);

    };

    this.draw = function() {
        this.visual.draw();
    };

    this.toString = function() {
        return "This frame is at " + "x=" + this.x + ", y=" + this.y + ", width=" + this.w + ", height=" + this.h;
    }
}

function Visual() {

    this.inheritFrom = BoundingBox;
    this.inheritFrom();

    this.frame = null;
    this.drawables = [];

    this.setFrame = function (frame) {
        this.frame = frame;
        this.w = frame.w  - frame.padding * 4;
    };

    this.construct = function () {
        //
    };


    this.position = function (x, y) {
        this.x += x;
        this.y += y;

        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].position(this.x, this.y);
        }
    };

    this.draw = function() {

        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].setSurface(this.frame.board.surface);
            this.drawables[i].draw();
        }
    };

    this.toString = function() {
        return "This visual is at " + "x=" + this.x + ", y=" + this.y + ", width=" + this.w + ", height=" + this.h;
    };
}

function RectangleVisual() {
    this.inheritFrom = Visual;
    this.inheritFrom();

    this.drawable = null;

    this.construct = function () {
        this.h = Math.floor(Math.random() * 160 + 100);
        this.drawable = new RectDrawable();
        this.drawable.setCoordinate(this.x, this.y, this.w, this.h);
        this.drawable.setColor(getRandomColor());
        this.drawables.push(this.drawable);
    };
}

function TitleVisual(title) {
    this.inheritFrom = Visual;
    this.inheritFrom();
    this.title = title;

    this.construct = function () {
        this.h = 30;
        this.drawable = new RectDrawable();
        this.drawable.setCoordinate(this.x, this.y, this.w, this.h)
        this.drawable.setColor("#FFF").setOpacity(0.4);
        this.drawables.push(this.drawable);

        this.textDrawable = new TextDrawable();
        this.textDrawable.setSurface(this.frame.board.surface);
        this.textDrawable.setCoordinate(this.x, this.y, this.w, this.h)
        this.textDrawable.setText(this.title).setColor("#555").setAlignment("left").setFontSize(20).setFontWeight("bold");
        this.drawables.push(this.textDrawable);
    };

}

function EmptyVisual(h) {
    this.inheritFrom = Visual;
    this.inheritFrom();

    this.h = h;

    this.construct = function () {
        this.drawable = new RectDrawable();
        this.drawable.setCoordinate(this.x, this.y, this.w, this.h);
        this.drawable.setOpacity(0);
        this.drawables.push(this.drawable);
    };
}

function OperationVisual(operation) {
    this.inheritFrom = Visual;
    this.inheritFrom();

    this.operation = operation;
    this.subactors = null;
    this.suboperations = null;

    this.construct = function () {

        this.subactors = this.operation.getSubactors();
        this.suboperations = this.operation.getSuboperations();

        var padding = 20;
        var majPadding = 60;
        var midline = (this.w - padding * 2) * 0.2;

        var majorColumn = new Column(padding + midline + majPadding, (this.w - padding * 2) - midline - 2 * majPadding);
        var minorColumn = new Column(padding, midline - padding * 2);
        columns.setMajorColumn(majorColumn);
        columns.setMinorColumn(minorColumn);

        var headerH = 60;
        var footerH = 30;
        var subactorH = 30;
        var subactorPadding = subactorH * 0.2;


        var mainOpBBox = this.calcMainOpBBox(majorColumn, headerH, subactorPadding, subactorH);
        var mainActorBBox = this.calcMainActorBBox(minorColumn, headerH, subactorPadding, subactorH);

        this.h = headerH + mainOpBBox.h + footerH;

        this.constructBackground();
        this.contructHeaders(mainOpBBox, headerH);
        this.contructMainOperation(mainOpBBox);
        this.constructSubactors(mainActorBBox, subactorPadding, subactorH);

        if(transLevel == 1) {

            this.constructSubmissions(mainOpBBox, subactorPadding, subactorH);
        } else if (transLevel == 2) {
            this.construct2LevelSubmissions(mainOpBBox, subactorPadding, subactorH);
        } else {
            this.construct3LevelSubmissions(mainOpBBox, subactorPadding, subactorH);
        }


        this.contructFooters(mainOpBBox, headerH, footerH);
    };

    OperationVisual.prototype.constructBackground = function() {
        var superoperationDrawable = new RectDrawable();
        superoperationDrawable.setCoordinate(this.x, this.y, this.w, this.h).setColor("#EEE").setRadius(5).setStroke(1, "#CCC")
        if(this.operation.getSuperoperation()) {
            superoperationDrawable.setUuid(this.operation.getSuperoperation().uuid);
            superoperationDrawable.setClickEvent(function reloadOperationEvent(event) {
                drawOperation(this.attr("uuid"));
            });
        }
        this.drawables.push(superoperationDrawable);
    };

    OperationVisual.prototype.contructHeaders = function(mainOpBBox,  headerH) {

        var bBox = new BoundingBox().setCoordinate(mainOpBBox.x, 0, mainOpBBox.w, headerH);

        var timelineDrawable = new FlippedTimelineDrawable().setCoordinate(bBox.x, bBox.h / 2, bBox.w, bBox.h / 2);
        timelineDrawable.construct(this.operation);
        this.drawables.push(timelineDrawable);


        var transDrawable = new RectDrawable();
        transDrawable.setCoordinate(bBox.x + bBox.w - 10, bBox.y, 90, 20);
        transDrawable.setColor("#EEEEEE").setStroke(0.5, "#999999").setRadius(2);
        transDrawable.setHint("Adjust the display level. By increasing the level, lower level operations can be previewed.");

        transDrawable.setClickEvent(function reloadOperationEvent(event) {
            transLevel = transLevel % 3 + 1;
            drawOperation(selectedOperationUuid, transLevel)
        });
        this.drawables.push(transDrawable);

        var transTextDrawable = new CollapseTextDrawable();
        transTextDrawable.setCoordinate(bBox.x + bBox.w - 10, bBox.y, 90, 20);
        transTextDrawable.setText("Display level:+" + transLevel).setFontSize(12).setColor("#000").setAlignment("middle");
        this.drawables.push(transTextDrawable);
    };


    OperationVisual.prototype.contructMainOperation = function(mainOpBBox) {
        this.mainOpDrawable = new RectDrawable();
        this.mainOpDrawable.setCoordinate(mainOpBBox.x - 1, mainOpBBox.y, mainOpBBox.w + 2, mainOpBBox.h)
        this.mainOpDrawable.setColor("#66FFCC").setRadius(3).setStroke(1, "#009999");
        this.drawables.push(this.mainOpDrawable);
    };

    OperationVisual.prototype.constructSubactors = function(mainActorBBox, subactorPadding, subactorH) {
        for(var i = 0; i < this.subactors.length; ++i) {

            var subactor = this.subactors[i];
            var subactorX = mainActorBBox.x;
            var subactorY = mainActorBBox.y + subactorPadding + (subactorH + subactorPadding) * i;
            var subactorW = mainActorBBox.w;

            var subactorDrawable = new RectDrawable();
            subactorDrawable.setCoordinate(subactorX, subactorY, subactorW, subactorH).setColor("#DDD").setRadius(3).setStroke(1, "#999").setOpacity(0);
            this.drawables.push(subactorDrawable);

            var subactorTextDrawable = new TextDrawable();
            subactorTextDrawable.setCoordinate(subactorX, subactorY, subactorW, subactorH).setText(subactor.toString()).setFontSize(15).setAlignment("middle");
            this.drawables.push(subactorTextDrawable);
        }
    };

    OperationVisual.prototype.constructSubmissions = function(mainOpBBox, subactorPadding, subactorH) {

        var mainOpStarttime = parseFloat(this.operation.getInfo("StartTime").value);
        var mainOpEndtime = parseFloat(this.operation.getInfo("EndTime").value);
        var mainOpDuration = parseFloat(this.operation.getInfo("Duration").value);

        for(var i = 0; i < this.suboperations.length; ++i) {

            var suboperation = this.suboperations[i];

            var subOpStarttime =  parseFloat(suboperation.getInfo("StartTime").value);
            var subOpEndtime = parseFloat(suboperation.getInfo("EndTime").value);
            var subOpDuration = parseFloat(suboperation.getInfo("Duration").value);

            var subactor = this.suboperations[i].getActor();
            var subactorPos;
            _.find(this.subactors, function(actor, j){
                if(actor.uuid == subactor.uuid ){ subactorPos = j; return true;};
            });

            var suboperationX = mainOpBBox.x + mainOpBBox.w * (subOpStarttime - mainOpStarttime) / mainOpDuration;
            var suboperationY = mainOpBBox.y + subactorPadding + (subactorH + subactorPadding) * subactorPos;
            var suboperationW = mainOpBBox.w * (subOpDuration / mainOpDuration);
            var suboperationH = subactorH;

            var submissionDrawable = new RectDrawable();
            submissionDrawable.setCoordinate(suboperationX, suboperationY, suboperationW, suboperationH);
            submissionDrawable.setColor("#3399FF").setStroke(0.5, "#0033CC").setUuid(suboperation.uuid)
            submissionDrawable.setHint(suboperation.getMission().name + " (" + suboperation.getInfo("Duration").value + "ms)");
            submissionDrawable.setColor(suboperation.getInfo("Color").value).setStroke(0.5, change_brightness(suboperation.getInfo("Color").value, -80));
            submissionDrawable.setClickEvent(function reloadOperationEvent(event) {
                drawOperation(this.attr("uuid"));
            });
            this.drawables.push(submissionDrawable);

            var submissionTextDrawable = new CollapseTextDrawable();
            submissionTextDrawable.setCoordinate(suboperationX, suboperationY, suboperationW, suboperationH);
            submissionTextDrawable.setText(suboperation.getMission().toString()).setFontSize(15).setColor("#FFF").setAlignment("middle");
            this.drawables.push(submissionTextDrawable);
        }
    };


    OperationVisual.prototype.construct2LevelSubmissions = function(mainOpBBox, subactorPadding, subactorH) {

        var mainOpStarttime = parseFloat(this.operation.getInfo("StartTime").value);
        var mainOpEndtime = parseFloat(this.operation.getInfo("EndTime").value);
        var mainOpDuration = parseFloat(this.operation.getInfo("Duration").value);

        for(var i = 0; i < this.suboperations.length; ++i) {

            var suboperation = this.suboperations[i];

            var isExpandable = (suboperation.getSubactors().length == 1);


            var subOpStarttime =  parseFloat(suboperation.getInfo("StartTime").value);
            var subOpEndtime = parseFloat(suboperation.getInfo("EndTime").value);
            var subOpDuration = parseFloat(suboperation.getInfo("Duration").value);

            var subactor = this.suboperations[i].getActor();
            var subactorPos;
            _.find(this.subactors, function(actor, j){
                if(actor.uuid == subactor.uuid ){ subactorPos = j; return true;};
            });

            var suboperationX = mainOpBBox.x + mainOpBBox.w * (subOpStarttime - mainOpStarttime) / mainOpDuration;
            var suboperationY = mainOpBBox.y + subactorPadding + (subactorH + subactorPadding) * subactorPos;
            var suboperationW = mainOpBBox.w * (subOpDuration / mainOpDuration);
            var suboperationH = subactorH;

            var submissionDrawable = new RectDrawable();
            submissionDrawable.setCoordinate(suboperationX, suboperationY, suboperationW, (isExpandable) ? suboperationH / 5: suboperationH);

            submissionDrawable.setUuid(suboperation.uuid)
            submissionDrawable.setHint(suboperation.getMission().name + " (" + suboperation.getInfo("Duration").value + "ms)");
            submissionDrawable.setColor(suboperation.getInfo("Color").value).setStroke(0.5, change_brightness(suboperation.getInfo("Color").value, -80));
            submissionDrawable.setClickEvent(function reloadOperationEvent(event) {
                drawOperation(this.attr("uuid"));
            });
            this.drawables.push(submissionDrawable);

            var subsuboperations = suboperation.getSuboperations();

            for(var j = 0; j < subsuboperations.length; ++j) {
                var subsuboperation = subsuboperations[j];


                var subsubOpStarttime =  parseFloat(subsuboperation.getInfo("StartTime").value);
                var subsubOpEndtime = parseFloat(subsuboperation.getInfo("EndTime").value);
                var subsubOpDuration = parseFloat(subsuboperation.getInfo("Duration").value);



                if(isExpandable) {
                    var subsuboperationX = mainOpBBox.x + mainOpBBox.w * (subsubOpStarttime - mainOpStarttime) / mainOpDuration;
                    var subsuboperationY = suboperationY + (subactorH * 0.2);
                    var subsuboperationW = mainOpBBox.w * (subsubOpDuration / mainOpDuration);
                    var subsuboperationH = subactorH * 0.8;

                    var subsubmissionDrawable = new RectDrawable();
                    subsubmissionDrawable.setCoordinate(subsuboperationX, subsuboperationY, subsuboperationW, subsuboperationH);

                    subsubmissionDrawable.setUuid(subsuboperation.uuid)
                    subsubmissionDrawable.setHint(subsuboperation.getMission().name + " (" + subsuboperation.getInfo("Duration").value + "ms)");
                    subsubmissionDrawable.setColor(subsuboperation.getInfo("Color").value).setStroke(0.5, change_brightness(suboperation.getInfo("Color").value, -80));
                    subsubmissionDrawable.setClickEvent(function reloadOperationEvent(event) {
                        drawOperation(this.attr("uuid"));
                    });
                    this.drawables.push(subsubmissionDrawable);
                }
            }

            if(!isExpandable) {
                var submissionTextDrawable = new CollapseTextDrawable();
                submissionTextDrawable.setCoordinate(suboperationX, suboperationY, suboperationW, suboperationH);
                submissionTextDrawable.setText(suboperation.getMission().toString()).setFontSize(15).setColor("#FFF").setAlignment("middle");
                this.drawables.push(submissionTextDrawable);
            }
        }
    };

    OperationVisual.prototype.construct3LevelSubmissions = function(mainOpBBox, subactorPadding, subactorH) {

        var mainOpStarttime = parseFloat(this.operation.getInfo("StartTime").value);
        var mainOpEndtime = parseFloat(this.operation.getInfo("EndTime").value);
        var mainOpDuration = parseFloat(this.operation.getInfo("Duration").value);

        for(var i = 0; i < this.suboperations.length; ++i) {

            var suboperation = this.suboperations[i];
            var isSubExpandable = (suboperation.getSubactors().length == 1);

            var subOpStarttime =  parseFloat(suboperation.getInfo("StartTime").value);
            var subOpEndtime = parseFloat(suboperation.getInfo("EndTime").value);
            var subOpDuration = parseFloat(suboperation.getInfo("Duration").value);

            var subactor = this.suboperations[i].getActor();
            var subactorPos;
            _.find(this.subactors, function(actor, j){
                if(actor.uuid == subactor.uuid ){ subactorPos = j; return true;};
            });

            var suboperationX = mainOpBBox.x + mainOpBBox.w * (subOpStarttime - mainOpStarttime) / mainOpDuration;
            var suboperationY = mainOpBBox.y + subactorPadding + (subactorH + subactorPadding) * subactorPos;
            var suboperationW = mainOpBBox.w * (subOpDuration / mainOpDuration);
            var suboperationH = !isSubExpandable ? subactorH : subactorH / 5;

            var submissionDrawable = new RectDrawable();
            submissionDrawable.setCoordinate(suboperationX, suboperationY, suboperationW, suboperationH);

            submissionDrawable.setUuid(suboperation.uuid)
            submissionDrawable.setHint(suboperation.getMission().name + " (" + suboperation.getInfo("Duration").value + "ms)");
            submissionDrawable.setColor(suboperation.getInfo("Color").value).setStroke(0.5, change_brightness(suboperation.getInfo("Color").value, -80));
            submissionDrawable.setClickEvent(function reloadOperationEvent(event) {
                drawOperation(this.attr("uuid"));
            });
            this.drawables.push(submissionDrawable);

            var subsuboperations = suboperation.getSuboperations();

            for(var j = 0; j < subsuboperations.length; ++j) {
                var subsuboperation = subsuboperations[j];
                var isSubSubExpandable = (subsuboperation.getSubactors().length == 1) && isSubExpandable;


                var subsubOpStarttime =  parseFloat(subsuboperation.getInfo("StartTime").value);
                var subsubOpEndtime = parseFloat(subsuboperation.getInfo("EndTime").value);
                var subsubOpDuration = parseFloat(subsuboperation.getInfo("Duration").value);

                if(isSubExpandable) {
                    var subsuboperationX = mainOpBBox.x + mainOpBBox.w * (subsubOpStarttime - mainOpStarttime) / mainOpDuration;
                    var subsuboperationY = suboperationY + (subactorH * 0.2);
                    var subsuboperationW = mainOpBBox.w * (subsubOpDuration / mainOpDuration);
                    var subsuboperationH = !isSubSubExpandable ? subactorH * 0.8 : subactorH * 0.2;

                    var subsubmissionDrawable = new RectDrawable();
                    subsubmissionDrawable.setCoordinate(subsuboperationX, subsuboperationY, subsuboperationW, subsuboperationH);

                    subsubmissionDrawable.setUuid(subsuboperation.uuid)
                    subsubmissionDrawable.setHint(subsuboperation.getMission().name + " (" + subsuboperation.getInfo("Duration").value + "ms)");
                    subsubmissionDrawable.setColor(subsuboperation.getInfo("Color").value).setStroke(0.5, change_brightness(subsuboperation.getInfo("Color").value, -80));
                    subsubmissionDrawable.setClickEvent(function reloadOperationEvent(event) {
                        drawOperation(this.attr("uuid"));
                    });
                    this.drawables.push(subsubmissionDrawable);
                }

                var subsubsuboperations = subsuboperation.getSuboperations();
                for(var k = 0; k < subsubsuboperations.length; ++k) {
                    var subsubsuboperation = subsubsuboperations[k];


                    var subsubsubOpStarttime =  parseFloat(subsubsuboperation.getInfo("StartTime").value);
                    var subsubsubOpEndtime = parseFloat(subsubsuboperation.getInfo("EndTime").value);
                    var subsubsubOpDuration = parseFloat(subsubsuboperation.getInfo("Duration").value);


                    if(isSubSubExpandable) {
                        var subsubsuboperationX = mainOpBBox.x + mainOpBBox.w * (subsubsubOpStarttime - mainOpStarttime) / mainOpDuration;
                        var subsubsuboperationY = suboperationY + (subactorH * 0.4);
                        var subsubsuboperationW = mainOpBBox.w * (subsubsubOpDuration / mainOpDuration);
                        var subsubsuboperationH = subactorH * 0.6;

                        var subsubsubmissionDrawable = new RectDrawable();
                        subsubsubmissionDrawable.setCoordinate(subsubsuboperationX, subsubsuboperationY, subsubsuboperationW, subsubsuboperationH);

                        subsubsubmissionDrawable.setUuid(subsubsuboperation.uuid)
                        subsubsubmissionDrawable.setHint(subsubsuboperation.getMission().name + " (" + subsubsuboperation.getInfo("Duration").value + "ms)");
                        subsubsubmissionDrawable.setColor(subsubsuboperation.getInfo("Color").value).setStroke(0.5, change_brightness(subsubsuboperation.getInfo("Color").value, -80));
                        subsubsubmissionDrawable.setClickEvent(function reloadOperationEvent(event) {
                            drawOperation(this.attr("uuid"));
                        });
                        this.drawables.push(subsubsubmissionDrawable);
                    }
                }

            }

            if(!isSubExpandable) {
                var submissionTextDrawable = new CollapseTextDrawable();
                submissionTextDrawable.setCoordinate(suboperationX, suboperationY, suboperationW, suboperationH);
                submissionTextDrawable.setText(suboperation.getMission().toString()).setFontSize(15).setColor("#FFF").setAlignment("middle");
                this.drawables.push(submissionTextDrawable);
            }
        }
    };

    OperationVisual.prototype.calcMainOpBBox = function(majorColumn, headerH, subactorPadding, subactorH) {

        var subactorsSize = this.subactors.length;

        var mainOpX = majorColumn.x;
        var mainOpY = headerH;
        var mainOpW = majorColumn.w;
        var mainOpH = (Math.max(1, subactorsSize) + 1) * subactorPadding + Math.max(1, subactorsSize) * subactorH;

        return new BoundingBox().setCoordinate(mainOpX, mainOpY, mainOpW, mainOpH);
    };

    OperationVisual.prototype.calcMainActorBBox = function(minorColumn, headerH, subactorPadding, subactorH) {

        var subactorsSize = this.subactors.length;

        var mainOpX = minorColumn.x;
        var mainOpY = headerH;
        var mainOpW = minorColumn.w;
        var mainOpH = (Math.max(1, subactorsSize) + 1) * subactorPadding + Math.max(1, subactorsSize) * subactorH;

        return new BoundingBox().setCoordinate(mainOpX, mainOpY, mainOpW, mainOpH);
    };

    OperationVisual.prototype.contructFooters = function(mainOpBBox, headerH, footerH) {

        var baseY = headerH + mainOpBBox.h;
        var bBox = new BoundingBox().setCoordinate(mainOpBBox.x, baseY, mainOpBBox.w, footerH);

        var timelineDrawable = new TimelineDrawable().setCoordinate(bBox.x, bBox.y, bBox.w, bBox.h);
        timelineDrawable.construct(this.operation);
        this.drawables.push(timelineDrawable);
    }

}

function DescriptionVisual(drptNode) {
    this.inheritFrom = Visual;
    this.inheritFrom();
    this.drptNode = drptNode;
    this.table = null;

    this.construct = function () {

        var summaryInfoUuid = this.drptNode.children("SummarySource").children("Source").attr("infoUuid");
        var summaryInfo = new SummaryInfo($(selectedJobArchive.file).find('Info[uuid=' + summaryInfoUuid + ']'));

        this.table = "";
        this.table += '<table class="table table-bordered drpt-table">';

        var summary = summaryInfo.summary;
        var infoStrings = summary.match(/\[\{[\d]+\}\]/g);
        for(var i=0; i< infoStrings.length; i++) {
            var infoString = infoStrings[i];
            var infoUuid = infoString.replace('\[\{', "").replace('\}\]', "");

            var labelHtml = "";
            labelHtml += '<button class="btn btn-xs trace-btn" uuid="' + infoUuid +'">';
            labelHtml += '<span class="glyphicon glyphicon glyphicon-zoom-in"></span>' + '</button>';

            summary = summary.replace(infoString, labelHtml);
        }

        this.table += '<tr><td>' + summary + '</td></tr>';

        this.table += '</table>';

        var testDiv = $('#test-div');
        testDiv.empty();
        var tableWidth = this.frame.board.w - this.frame.board.padding * 2;
        //alert(tableWidth);
        var tableContainter = $('<div style="width:' + tableWidth  + 'px;"></div>');
        tableContainter.html(this.table);
        testDiv.append(tableContainter);
        this.h = tableContainter.height();
        testDiv.empty();

        this.drawable = new TableDrawable();
        this.drawable.setCoordinate(this.x, this.y, this.w, this.h);
        this.drawable.setTable(this.table);
        this.drawables.push(this.drawable);
    };

}


function TableVisual(tblNode) {
    this.inheritFrom = Visual;
    this.inheritFrom();
    this.tblNode = tblNode;
    this.table = null;

    this.constructLabel = function (info) {
        var labelHtml = "";
        labelHtml += '<button class="btn btn-xs trace-btn" uuid="' + info.uuid +'">';
        labelHtml += '<span class="glyphicon glyphicon glyphicon-zoom-in"></span>' + '</button>';
        //labelHtml += '<td>'
        return labelHtml;
    }

    this.construct = function () {

        var randomPercentage = randomInt(10, 100);
        var randProgressBar = '<div class="progress"><div class="progress-bar" style="width:' + randomPercentage + '%">' + randomPercentage + '%</div></div>';


        var numRows = 0;

        var infoSources = this.tblNode.children("TableCell").children("Source");
        var infos = infoSources.map( function () { return new Info($(selectedJobArchive.file).find('Info[uuid=' + $(this).attr("infoUuid") + ']')); }).get();

        this.table = "";
        this.table += '<table class="table table-hover table-bordered">';
        this.table += '<colgroup><col span="1" style="width: 20%;"><col span="1" style="width: 30%;"><col span="1" style="width: 20%;"><col span="1" style="width: 30%;"></colgroup>';
        //this.table += infos.map(function (info) { return '<tr><td>' + info.name + '</td><td>' + info.value + '</td></tr>'}).join("");
        for(var i = 0; i < infos.length; i = i + 2) {
            this.table += '<tr>';

            this.table += '<td>' + '<b>' + infos[i].name + '</b>' + this.constructLabel(infos[i]) +'</td><td>' + infos[i].value + '</td>';
            if(i + 1 < infos.length) {
                this.table += '<td>'  + '<b>' + infos[i + 1].name + '</b>' + this.constructLabel(infos[i + 1]) + '</td>';
                this.table += '<td>' + infos[i + 1].value + '</td>';
            } else {
                this.table += '<td></td><td></td>';
            }
            this.table += '</tr>';
            numRows++;
        }
        this.table += '</table>';

        var testDiv = $('#test-div');
        testDiv.empty();
        var tableWidth = this.frame.board.w - this.frame.board.padding * 2;
        //alert(tableWidth);
        var tableContainter = $('<div style="width:' + tableWidth  + 'px;"></div>');
        tableContainter.html(this.table);
        testDiv.append(tableContainter);
        this.h = tableContainter.height() + 10;
        testDiv.empty();

        this.drawable = new TableDrawable();
        this.drawable.setCoordinate(this.x, this.y, this.w, this.h);
        this.drawable.setTable(this.table);
        this.drawables.push(this.drawable);
    };

}

function TimeSeriesVisual(tsVisualNode) {
    this.inheritFrom = Visual;
    this.inheritFrom();

    this.tsVisualNode = tsVisualNode;

    this.construct = function () {


        var pdd = 15;
        var hdrH = 40;
        var ftrH = 50;
        var grSpBBox = this.calcGrSpBBox(columns.majorColumn, hdrH);

        var lblBBox = this.calcLblBBox(columns.minorColumn, hdrH);
        this.h = hdrH + grSpBBox.h + ftrH;

        var bgDrawable = new RectDrawable();
        bgDrawable.setCoordinate(0, 0, this.w, this.h).setColor("#EEE").setRadius(5).setStroke(1, "#CCC")
        this.drawables.push(bgDrawable);

        this.constructMainContainer(grSpBBox, lblBBox, hdrH, this.tsVisualNode);


        this.contructHeaders(hdrH, grSpBBox, this.tsVisualNode);
        this.contructFooters(grSpBBox, hdrH, ftrH);
    };

    this.constructMainContainer = function(grSpBBox, lblBBox, hdrH, tsVisualNode) {


        var grSpDrawable = new LC_GridSpaceDrawable();
        grSpDrawable.setCoordinate(grSpBBox.x, grSpBBox.y, grSpBBox.w, grSpBBox.h);
        grSpDrawable.setTicks(5, 4).construct(tsVisualNode);
        this.drawables.push(grSpDrawable);
        
        var xAxisNode = tsVisualNode.children("Axis[type=x]");
        var y1AxisNode = tsVisualNode.children("Axis[type=y1]");
        var y2AxisNode = tsVisualNode.children("Axis[type=y2]");

        var y1Timelines = [];
        if(y1AxisNode.length > 0) {
            var y1TSInfoSources = tsVisualNode.children("Axis[type=y1]").children("TimeSeriesSources").children("Source");
            var y1TSInfos =  y1TSInfoSources.map( function () { return $(selectedJobArchive.file).find('Info[uuid=' + $(this).attr("infoUuid") + ']'); });

            y1Timelines = y1TSInfos.map( function () {
                var tsInfo = $(this);
                var timeline = new Timeline(tsInfo.attr("name"), tsInfo.children("MetricUnit").text(), 1);
                var data = tsInfo.children("TimeSeries").children("Data").text().split("#");
                for(var i = 0; i < data.length; i++) {
                    var rFactor = Math.round(Math.max(data.length / 100, 1));
                    if(data[i].length > 0) {
                        var t = (data[i].split("@"))[0];
                        var v = (data[i].split("@"))[1];

                        if(i % rFactor == 0) {
                            timeline.addDatapoint(t, v);
                        }

                    }
                }

                return timeline;
            }).get();
        }

        var y2Timelines = [];
        if(y2AxisNode.length > 0) {
            var y2TSInfoSources = tsVisualNode.children("Axis[type=y2]").children("TimeSeriesSources").children("Source");
            var y2TSInfos =  y2TSInfoSources.map( function () { return $(selectedJobArchive.file).find('Info[uuid=' + $(this).attr("infoUuid") + ']'); });

            y2Timelines = y2TSInfos.map( function () {
                var tsInfo = $(this);
                var timeline = new Timeline(tsInfo.attr("name"), tsInfo.children("MetricUnit").text(), 2);
                var data = tsInfo.children("TimeSeries").children("Data").text().split("#");
                for(var i = 0; i < data.length; i++) {
                    if(data[i].length > 0) {
                        var rFactor = Math.round(Math.max(data.length / 100, 1));
                        var t = (data[i].split("@"))[0];
                        var v = (data[i].split("@"))[1];
                        if(i % rFactor == 0) {
                            timeline.addDatapoint(t, v);
                        }
                    }
                }
                return timeline;
            }).get();
        }


        var timelines = y1Timelines.concat(y2Timelines);

        for(var i = 0; i < timelines.length; i++) {

            var yAxisNode = (timelines[i].axisId == 1) ? y1AxisNode : y2AxisNode;

            var tmlnDrawable = new LC_TimeLineDrawable();
            tmlnDrawable.setCoordinate(grSpBBox.x, grSpBBox.y, grSpBBox.w, grSpBBox.h);
            tmlnDrawable.setColor(getPredefinedColor(i));
            tmlnDrawable.construct(timelines[i], yAxisNode, xAxisNode);
            this.drawables.push(tmlnDrawable);

            var lblDrawable = new LC_LabelDrawable();
            var lblSpPadding = (grSpBBox.h - (5 + 25) * timelines.length)/2;
            lblDrawable.setCoordinate(lblBBox.x + 5, hdrH + lblSpPadding +  i * (5 + 25), lblBBox.w - 5 * 2, 25);
            lblDrawable.setColor(getPredefinedColor(i));
            lblDrawable.setLine(tmlnDrawable);
            lblDrawable.construct(timelines[i]);
            lblDrawable.setFontSize(11);
            this.drawables.push(lblDrawable);

        }


    };

    this.contructHeaders = function(hdrH, grSpBBox, tsVisualNode) {
        var textDrawable = new TextDrawable();
        textDrawable.setCoordinate(grSpBBox.x, 0, grSpBBox.w, hdrH);
        var title = tsVisualNode.children("Title").text();
        textDrawable.setText(title).setColor("#555").setAlignment("middle").setFontSize(18).setFontWeight("bold");
        this.drawables.push(textDrawable);

    };

    this.contructFooters = function(mainOpBBox, hdrH, ftrH) {

    }

    this.calcLblBBox = function(minorColumn, hdrH) {

        var mainOpX = minorColumn.x;
        var mainOpY = hdrH;
        var mainOpW = minorColumn.w;
        var mainOpH = 200;

        return new BoundingBox().setCoordinate(mainOpX, mainOpY, mainOpW, mainOpH);
    };

    this.calcGrSpBBox = function(majorColumn, hdrH) {

        var mainOpX = majorColumn.x;
        var mainOpY = hdrH;
        var mainOpW = majorColumn.w;
        var mainOpH = 200;

        return new BoundingBox().setCoordinate(mainOpX, mainOpY, mainOpW, mainOpH);
    };
}

function Drawable() {

    this.surface = null;
    this.svgs = [];
    this.drawables = [];

    this.opacity = 1;
    this.color = "#000";

    this.setSurface = function (surface) {
        this.surface = surface;
        return this;
    };

    this.setOpacity = function(opacity) {
        this.opacity = opacity;
        return this;
    };

    this.applyOpacity = function(svg) {
        svg.attr({"fill-opacity": this.opacity});
    };

    this.setColor = function(color) {
        this.color = color;
        return this;
    };

    this.applyColor = function(svg) {
        svg.attr({fill: this.color});
    };

    this.applyAttr = function(svg, name, value) {
        svg.attr({name: value});
    }

    this.setCoordinate = function(x, y, w, h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    };

    this.position = function (x, y) {
        this.x += x;
        this.y += y;
        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].position(this.x, this.y);
        }
    };

    this.showAttr = function() {
        console.log("Drawable: " + this.constructor.name + " x " + this.x + " y " + this.y + " w " + this.w + " h " + this.h);
        return this;
    };

}

function ClickDrawable() {
    this.inheritFrom = Drawable;
    this.inheritFrom();

    this.clickEvent = null;
    this.uuid = null;

    this.setClickEvent = function(clickEvent) {
        this.clickEvent = clickEvent;
        this.hasClickEvent = true;
    };

    this.setUuid = function(uuid) {
        this.uuid = uuid;
        return this;
    };

    this.applyClickEvent = function(svg, clickEvent) {
        if(this.hasClickEvent) {
                svg.click(this.clickEvent);
        }
    };

    this.applyUuid = function(svg) {
        svg.attr({uuid: this.uuid});
    };
}

function TableDrawable() {
    this.inheritFrom = Drawable;
    this.inheritFrom();

    this.table = null;

    this.setTable = function(table) {
        this.table = table;
        return this;
    };

    this.draw = function () {

        var drawable = this;

        var svg = Snap.parse('<svg><foreignObject ' +
        'x="' + this.x + '" y = "' + this.y + '" width="' + this.w + '" height="' + this.h + '">' + this.table +
        '</foreignObject></svg>');

        this.svgs.push(svg);

        $.each(this.svgs, function (i, svgElem) { drawable.surface.append(svgElem); })

        $(".trace-btn").each(function() {
            $(this).on("click", function(){
                drawInfoTraceModal($(this).attr("uuid"));
                showDefaultModal();
            });
        });

        return this;

    };
}

function TextDrawable() {
    this.inheritFrom = Drawable;
    this.inheritFrom();

    this.text = null;
    this.alignment = "left";

    this.fontSize = 10;
    this.fontWeight = "normal";

    this.rotation = 0;

    this.setText = function(text) {
        this.text = text;
        return this;
    };

    this.setFontSize = function(fontSize) {
        this.fontSize = fontSize;
        return this;
    };

    this.setFontWeight = function(fontWeight) {
        this.fontWeight = fontWeight;
        return this;
    };

    this.setAlignment = function(alignment) {
        this.alignment = alignment;
        return this;
    };

    this.applyFont = function(svg) {
        svg.attr({fill: this.color, "font-size": this.fontSize, "font-weight": this.fontWeight, textAnchor: "left", "pointer-events": "none"});
    };

    this.setRotation = function(rotation) {
        this.rotation = rotation;
    }

    this.applyRotation = function(svg) {

        var rMtx= new Snap.Matrix();
        rMtx.rotate(this.rotation, this.x + this.w / 2, this.y + this.h / 2);
        rMtx.add(svg.transform().localMatrix);

        svg.transform(rMtx);
    }

    this.draw = function () {

        var drawable = this;

        this.textBBox = this.calcTextBBox();
        var svg = this.surface.text(this.textBBox.x, this.textBBox.y , this.text);
        this.svgs.push(svg);
        $.each(this.svgs, function(i, svgElem) { drawable.applyFont(svgElem); });

        if(this.rotation !== 0) {
            $.each(this.svgs, function(i, svgElem) { drawable.applyRotation(svgElem); });
        }
        return this;

    };

    this.calcTextBBox = function() {

        var textBBox = new BoundingBox();
        var svg = this.surface.text(0, 0, this.text);
        this.applyFont(svg);
        var x = svg.getBBox().x;
        var w = svg.getBBox().w;
        var y = svg.getBBox().y;
        var h = svg.getBBox().h;
        svg.remove();

        var textTopPadding = (this.h - h) / 2;
        var textLeftPadding = 0;
        if(this.alignment == "middle") {
            textLeftPadding = (this.w - w) / 2;
        } else if (this.alignment == "right") {
            textLeftPadding = (this.w - w);
        }

        x = this.x - x + textLeftPadding;
        y = this.y - y + textTopPadding;

        return textBBox.setCoordinate(x, y, w, h);
    }
}

function CollapseTextDrawable(x, y, w, h) {
    this.inheritFrom = TextDrawable;
    this.inheritFrom();

    this.draw = function () {

        var drawable = this;

        var textBBox = this.calcTextBBox();
        if(textBBox.w < this.w && textBBox.h < this.h) {
            var svg = this.surface.text(textBBox.x, textBBox.y, this.text);
            this.svgs.push(svg);
            $.each(this.svgs, function(i, svgElem) { drawable.applyFont(svgElem); });
        } else {
            //var emptyText = "";
            //this.svg = this.surface.text(this.x - textBBox.x + textLeftPadding, this.y - textBBox.y + textTopPadding, emptyText);
        }
        return this;
    };

}

function LineDrawable() {
    this.inheritFrom = Drawable;
    this.inheritFrom();

    this.hasStroke = true;
    this.strokeColor = "#000";
    this.strokeSize = 1;

    this.setStroke = function(strokeSize, strokeColor) {
        this.strokeSize = strokeSize;
        this.strokeColor = (strokeColor === undefined) ? this.strokeColor : strokeColor;
        this.hasStroke = true;
        return this;
    };

    this.applyStroke = function(svg) {
        if(this.hasStroke) {
            svg.attr({strokeWidth: this.strokeSize, stroke: this.strokeColor});
        }
    };

    this.draw = function () {

        var drawable = this;

        var svg = this.surface.line(this.x, this.y, this.x + this.w, this.y + this.h);
        this.svgs.push(svg);



        $.each(this.svgs, function(i, svgElem) { drawable.applyColor(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyOpacity(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyStroke(svgElem); });

        return this;
    };
}



function RectDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.radius = 0;

    this.hasStroke = false;
    this.strokeColor = "#000";
    this.strokeSize = 0;

    this.hasShadow = false;
    this.shadow = null;

    this.hint = null;

    this.setHint = function(hint) {
        this.hint = hint;
        return this;
    };

    this.setRadius = function(radius) {
        this.radius = radius;
        return this;
    };

    this.setShadow = function() {
        this.shadow = Snap.filter.shadow(1, 1, 3);
        this.hasShadow = true;
        return this;
    };

    this.applyShadow = function(svg) {
        if(this.hasShadow) {
            svg.attr({filter: this.surface.filter(this.shadow)});
        }
    };

    this.setStroke = function(strokeSize, strokeColor) {
        this.strokeSize = strokeSize;
        this.strokeColor = (strokeColor === undefined) ? this.strokeColor : strokeColor;
        this.hasStroke = true;
        return this;
    };

    this.applyStroke = function(svg) {
        if(this.hasStroke) {
            svg.attr({strokeWidth: this.strokeSize, stroke: this.strokeColor});
        }
    };

    this.applyHint = function(svg) {
        if(this.hint) {
            svg.append(Snap.parse('<title>' + this.hint + '</title>'));
        }
    };

    RectDrawable.prototype.draw = function () {

        var drawable = this;

        var svg = this.surface.rect(this.x, this.y, this.w, this.h, this.radius);
        this.svgs.push(svg);

        $.each(this.svgs, function(i, svgElem) { drawable.applyColor(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyOpacity(svgElem); });

        $.each(this.svgs, function(i, svgElem) { drawable.applyUuid(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyClickEvent(svgElem); });

        $.each(this.svgs, function(i, svgElem) { drawable.applyShadow(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyStroke(svgElem); });

        if(this.hint) {
            $.each(this.svgs, function(i, svgElem) { drawable.applyHint(svgElem); });
        }


        return this;
    };
}


function TimelineCircleDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.radius = 0;
    this.hint = null;
    this.axis = null;

    this.setRadius = function(radius) {
        this.radius = radius;
        return this;
    };

    this.setHint = function(hint) {
        this.hint = hint;
        return this;
    };

    this.setAxis = function(axis) {
        this.axis = axis;
        return this;
    };

    this.applyHint = function(svg) {
            svg.append(Snap.parse('<title>' + this.hint + '</title>'));
    };

    this.draw = function () {

        var drawable = this;

        var svg = this.surface.circle(this.x, this.y, this.radius);
        svg.attr({axis: this.axis});
        this.svgs.push(svg);

        var surface = this.surface;
        svg.mouseover( function animateSVG(){
            if(1 == svg.attr("axis")) {
                tmpLineX = Snap("#board").line(0, svg.getBBox().cy, svg.getBBox().cx, svg.getBBox().cy)
                    .attr({strokeWidth: 1, stroke: "#666"});
            } else if (2 == svg.attr("axis")) {
                tmpLineX = Snap("#board").line(svg.getBBox().cx, svg.getBBox().cy, 10000, svg.getBBox().cy)
                    .attr({strokeWidth: 1, stroke: "#666"});
            }


            tmpLineY = Snap("#board").line(svg.getBBox().cx, 0, svg.getBBox().cx, svg.getBBox().cy)
                .attr({strokeWidth: 1, stroke: "#666"});
            //Snap("#board").circle(svg.getBBox().x, svg.getBBox().y, 50)
        } );

        svg.mouseout( function animateSVG(){
            tmpLineX.remove();
            tmpLineY.remove();
        } );

        $.each(this.svgs, function(i, svgElem) { drawable.applyColor(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyOpacity(svgElem); });

        $.each(this.svgs, function(i, svgElem) { drawable.applyUuid(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyClickEvent(svgElem); });

        if(this.hint) {
            $.each(this.svgs, function(i, svgElem) { drawable.applyHint(svgElem); });
        }
        return this;
    };
}


function CircleDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.radius = 0;
    this.hint = null;

    this.setRadius = function(radius) {
        this.radius = radius;
        return this;
    };

    this.setHint = function(hint) {
        this.hint = hint;
        return this;
    };

    this.applyHint = function(svg) {
        svg.append(Snap.parse('<title>' + this.hint + '</title>'));
    };

    this.draw = function () {

        var drawable = this;

        var svg = this.surface.circle(this.x, this.y, this.radius);
        this.svgs.push(svg);

        $.each(this.svgs, function(i, svgElem) { drawable.applyColor(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyOpacity(svgElem); });

        $.each(this.svgs, function(i, svgElem) { drawable.applyUuid(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyClickEvent(svgElem); });

        if(this.hint) {
            $.each(this.svgs, function(i, svgElem) { drawable.applyHint(svgElem); });
        }
        return this;
    };
}

function TimelineDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.numXTick = 4;
    this.radius = 0;

    this.setRadius = function(radius) {
        this.radius = radius;
        return this;
    };

    this.setNumXTick = function(numXTick) {
        this.numXTick = numXTick;
    };

    this.construct = function(operation) {

        this.setColor("#FFF").setOpacity(0).setRadius(10);
        this.setClickEvent(function reloadOperationEvent(event) {

        })

        var opStartTime = parseFloat(operation.getInfo("StartTime").value);
        var opEndTime = parseFloat(operation.getInfo("EndTime").value);

        var lineH = this.h * 2 / 5;
        var xTipH = this.h / 5;

        var opDuration = opEndTime - opStartTime;
        var numXTip = this.numXTick;
        var xUnit = "s";

        var xAxis = new LineDrawable();
        xAxis.setCoordinate(0, lineH, this.w, 0);
        xAxis.setStroke(1, "#999");
        this.drawables.push(xAxis);

        for(var i = 0; i < numXTip + 1; i++) {

            var xTip = new LineDrawable();

            xTip.setCoordinate(this.w * i / numXTip, lineH - xTipH, 0, xTipH);
            xTip.setStroke(1, "#999");
            this.drawables.push(xTip);

            var xTipText = new TextDrawable();
            xTipText.setCoordinate(this.w * i / numXTip, lineH + xTipH, 0, xTipH);
            var xTipValue = (i / numXTip * opDuration) / 1000;
            xTipText.setText(xTipValue.toFixed(2)).setAlignment("middle").setFontSize(13).setColor("#555");
            this.drawables.push(xTipText);
        }

        var xAxisUnit = new TextDrawable();
        xAxisUnit.setCoordinate(this.w * 1.04, lineH, 0, 0);
        xAxisUnit.setText('(' + xUnit + ')').setAlignment("middle").setFontSize(14).setColor("#555");
        this.drawables.push(xAxisUnit);
    }

    this.draw = function () {

        var drawable = this;

        var svg = this.surface.rect(this.x, this.y, this.w, this.h, this.radius);
        this.svgs.push(svg);

        $.each(this.svgs, function(i, svgElem) { drawable.applyColor(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyOpacity(svgElem); });

        $.each(this.svgs, function(i, svgElem) { drawable.applyUuid(svgElem); });
        $.each(this.svgs, function(i, svgElem) { drawable.applyClickEvent(svgElem); });

        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].setSurface(this.surface);
            this.drawables[i].draw();
        }

        return this;
    };
}

function FlippedTimelineDrawable() {
    this.inheritFrom = TimelineDrawable;
    this.inheritFrom();

    this.construct = function(operation) {

        this.setColor("#FFF").setOpacity(0).setRadius(10);
        this.setClickEvent(function reloadOperationEvent(event) {

        })

        var opStartTime = parseFloat(operation.getInfo("StartTime").value);
        var opEndTime = parseFloat(operation.getInfo("EndTime").value);

        var lineH = this.h * 3 / 5;
        var xTipH = this.h / 5;

        var jobStartTime = parseFloat(operation.getTopoperation().getInfo("StartTime").value);
        var jobEndTime = parseFloat(operation.getTopoperation().getInfo("EndTime").value);
        var opStartTime = parseFloat(operation.getInfo("StartTime").value);
        var opEndTime = parseFloat(operation.getInfo("EndTime").value);
        var opDuration = opEndTime - opStartTime;
        var numXTip = this.numXTick;

        var xAxis = new LineDrawable();
        xAxis.setCoordinate(0, lineH, this.w, 0);
        xAxis.setStroke(1, "#999");
        this.drawables.push(xAxis);

        for(var i = 0; i < numXTip + 1; i++) {

            var xTip = new LineDrawable();

            xTip.setCoordinate(this.w * i / numXTip, lineH, 0, xTipH);
            xTip.setStroke(1, "#999");
            this.drawables.push(xTip);

            var xTipText = new TextDrawable();
            xTipText.setCoordinate(this.w * i / numXTip, lineH - xTipH * 1.5, 0, 0);
            var xTipValue = (i / numXTip * opDuration + opStartTime - jobStartTime) / (jobEndTime - jobStartTime) * 100;
            xTipText.setText(xTipValue.toFixed(1) + '%').setAlignment("middle").setFontSize(13).setColor("#555");
            this.drawables.push(xTipText);
        }
    }
}

function LC_LabelDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.text = null;
    this.textDrawable = null;
    this.rectDrawable = null;
    this.lineDrawable - null;

    this.isSelected = true;

    this.setText = function(text) {
        this.text = text;
        return this;
    };

    this.setSelected = function (isSelected) {
        this.isSelected = isSelected;
    }

    this.setLine = function(lineDrawable) {
        this.lineDrawable = lineDrawable;
    }

    this.setFontSize = function(fontSize) {
        this.textDrawable.setFontSize(fontSize);
        return this;
    };

    this.toggle = function() {
        this.isSelected = !(this.isSelected);

        if(this.isSelected) {
            this.rectDrawable.setColor(this.color).applyColor(this.rectDrawable.svgs[0]);
            this.textDrawable.setColor("#FFF").applyColor(this.textDrawable.svgs[0]);
        } else {
            this.rectDrawable.setColor("#EEE").applyColor(this.rectDrawable.svgs[0]);
            this.textDrawable.setColor(this.color).applyColor(this.textDrawable.svgs[0]);
        }
    }

    this.construct = function(timeline) {

        var rectDrawable = new RectDrawable().setCoordinate(0, 0, this.w, this.h);
        rectDrawable.setColor(this.color).setRadius(3).setStroke(1, this.color);
        this.rectDrawable = rectDrawable;
        this.drawables.push(rectDrawable);


        var textDrawable = new TextDrawable().setCoordinate(1, 1, this.w, this.h);
        textDrawable.setText(timeline.name).setAlignment("middle").setColor("#FFF").setFontSize(14);
        this.textDrawable = textDrawable;
        this.drawables.push(textDrawable);

        var toggleDrawable = this;
        var toogleLineDrawable = this.lineDrawable;
        this.rectDrawable.setClickEvent(function () {
            toggleDrawable.toggle();
            toogleLineDrawable.toggleVisibility();
        });
    }

    this.draw = function () {

        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].setSurface(this.surface);
            this.drawables[i].draw();
        }

        return this;
    };

}

function LC_TimeLineDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.isVisible = true;

    this.tmlnDrawables = [];

    this.toggleVisibility = function() {
        this.isVisible = !(this.isVisible);
        if(this.isVisible) {
            for(var i = 0; i < this.drawables.length; i++) {
                this.drawables[i].svgs[0].attr({
                    visibility: "visible"
                });
            }

        } else {
            for(var i = 0; i < this.drawables.length; i++) {
                this.drawables[i].svgs[0].attr({
                    visibility: "hidden"
                });
            }

        }
    }

    this.construct = function(timeline, yAxisNode, xAxisNode) {

        var startValue = parseFloat(yAxisNode.children("StartValue").text());
        var endValue = parseFloat(yAxisNode.children("EndValue").text());
        var valueRange = endValue - startValue;


        var startTime = parseFloat(xAxisNode.children("StartValue").text());
        var endTime = parseFloat(xAxisNode.children("EndValue").text());
        var duration = (endTime - startTime);


        for(var i = 0; i < timeline.datapoints.length; i++) {

            var timestamp = timeline.datapoints[i].timestamp;
            var value = timeline.datapoints[i].value;

            var x = (timestamp - startTime) / duration * this.w;
            var y = this.h - (value - startValue) / valueRange * this.h;

            var pntDrawable = new TimelineCircleDrawable();
            pntDrawable.setCoordinate(x, y).setRadius(4).setColor(this.color).setAxis(timeline.axisId);
            pntDrawable.setHint('(t=' + ((timestamp - startTime) / 1000).toFixed(2) + ', v=' + parseFloat(value).toFixed(1) + ')');
            this.drawables.push(pntDrawable);
            this.tmlnDrawables.push(pntDrawable);


            if(i+1 < timeline.datapoints.length) {

                var dp1 = timeline.datapoints[i];
                var dp2 = timeline.datapoints[i+1];

                var x1 = (dp1.timestamp - startTime) / duration * this.w;
                var y1 = this.h - (dp1.value - startValue) / valueRange * this.h;
                var x2 = (dp2.timestamp - startTime) / duration * this.w;
                var y2 = this.h - (dp2.value - startValue) / valueRange * this.h;

                var line = new LineDrawable();
                line.setCoordinate(x1, y1, x2 - x1, y2 - y1);
                line.setStroke(2, this.color);
                this.drawables.push(line);
                this.tmlnDrawables.push(line);
            }

        }
    }

    this.draw = function () {

        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].setSurface(this.surface);
            this.drawables[i].draw();
        }


        var svgGrp = this.surface.group();

        for(var i = 0; i < this.tmlnDrawables.length; ++i) {
            var drawable = this.tmlnDrawables[i];
            for(var j = 0; j < drawable.svgs.length; ++j) {
                svgGrp.append(drawable.svgs[j]);
            }
        }

        var mask = this.surface.rect(this.x, this.y, this.w, this.h);
        mask.attr({fill: "#fff"});
        svgGrp.attr({mask: mask});

        return this;
    };


    this.calcBorderDataline = function(datapoints, startTime, endTime) {
        var dp1 = datapoints[0];
        var dp2 = datapoints[1];

        var d1State = 0;
        var d2State = 0;


        if(dp1.timestamp < startTime) {
            var d1State = -1;
        }

        if(dp2.timestamp < startTime) {
            var d2State = -1;
        }

        if(dp1.timestamp > endTime) {
            var d1State = 1;
        }

        if(dp2.timestamp > endTime) {
            var d2State = 1;
        }

        if(d1State == d2State && d1State !== 0) {
            return [];
        } else {

            var nDp1 = dp1;
            var nDp2 = dp2;

            if(d1State == -1) {
                var value = this.interpolate(startTime, dp1.timestamp, dp1.value, dp2.timestamp, dp2.value);
                nDp1 = new Datapoint(startTime, value);
            }

            if(d2State == 1) {
                var value = this.interpolate(endTime, dp1.timestamp, dp1.value, dp2.timestamp, dp2.value);
                nDp2 = new Datapoint(endTime, value);
            }

            return [nDp1, nDp2];
        }
    }

    this.interpolate = function(x, x1, y1, x2, y2) {
        var ratio = (x - x1) / (x2 - x1);
        return ratio * (y2 - y1) + y1;
    }

}

function LC_GridSpaceDrawable() {
    this.inheritFrom = ClickDrawable;
    this.inheritFrom();

    this.numXTick = 5;
    this.numYTick = 5;

    this.setTicks = function(numXTick, numYTick) {
        this.numXTick = numXTick;
        this.numYTick = numYTick;
        return this;
    };

    this.construct = function(tsVisualNode) {

        var ticks = this.numYTick - 1;

        for(var i = 0; i <= ticks; i++) {

            var yTick = new LineDrawable();
            this.drawables.push(yTick);
            yTick.setStroke(1, "#AAA");
            yTick.setCoordinate(0, this.h * i /ticks, this.w, 0);
        }

        for(var i = 0; i <= ticks; i++) {

            var yTick = new LineDrawable();
            this.drawables.push(yTick);
            yTick.setStroke(1, "#AAA");
            yTick.setCoordinate(0, this.h * i /ticks, this.w, 0);
        }

        var ticks = this.numXTick - 1;

        for(var i = 0; i <= ticks; i++) {

            var xTick = new LineDrawable();
            this.drawables.push(xTick);
            xTick.setStroke(1, "#AAA");
            xTick.setCoordinate(this.w * i / ticks, 0, 0, this.h);

        }


        this.constructXAxis(tsVisualNode.children("Axis[type=x]"));

        if(tsVisualNode.children("Axis[type=y1]").length > 0) {
            this.constructY1Axis(tsVisualNode.children("Axis[type=y1]"));
        }

        if(tsVisualNode.children("Axis[type=y2]").length > 0) {
            this.constructY2Axis(tsVisualNode.children("Axis[type=y2]"));
        }
    }

    this.constructXAxis = function(xAxisNode) {
        var title = xAxisNode.children("Title").text();
        var unit = xAxisNode.children("Unit").text();
        var duration = (parseFloat(xAxisNode.children("EndValue").text()) - parseFloat(xAxisNode.children("StartValue").text())) / 1000;

        var mtrPfx = new MetricPrefix("", 1);
        var ttlSuffix = "";


        if(mtrPfx.symbol !== "" || unit !== "") {
            ttlSuffix += ' (' + mtrPfx.symbol + unit + ')';
        }

        var lblH = 28;

        var ttlDrawable = new TextDrawable();
        ttlDrawable.setCoordinate(this.w / 2, this.h + lblH, 0, 0);
        ttlDrawable.setAlignment("middle").setFontSize(14).setColor("#555").setFontWeight("bold");

        ttlDrawable.setText(title + ttlSuffix);
        this.drawables.push(ttlDrawable);

        var ticks = this.numXTick - 1;


        for(var i = 0; i <= ticks; i++) {

            var xTickText = new TextDrawable();
            this.drawables.push(xTickText);
            xTickText.setAlignment("middle").setFontSize(13).setColor("#555");
            xTickText.setCoordinate(this.w * i / ticks, this.h + 10, 0, 0);

            var xTipValue = (i / ticks * duration);
            var xTipValue = xTipValue / mtrPfx.baseValue;
            xTickText.setText(xTipValue.toFixed(2));

        }
    }

    this.constructY1Axis = function(y1AxisNode) {

        var title = y1AxisNode.children("Title").text();
        var unit = y1AxisNode.children("Unit").text();
        var startValue = parseFloat(y1AxisNode.children("StartValue").text());
        var endValue = parseFloat(y1AxisNode.children("EndValue").text());
        var valueRange = (endValue - startValue);

        var mtrPfx = getMetricPrefix(endValue);
        var ttlSuffix = "";

        if(mtrPfx.symbol !== "" || unit !== "") {
            ttlSuffix += ' (' + mtrPfx.symbol + unit + ')';
        }

        var lblW = 48;

        var y1AxsLbl = new TextDrawable();
        y1AxsLbl.setCoordinate(-lblW,  this.h / 2, 0, 0);
        y1AxsLbl.setAlignment("middle").setFontSize(14).setColor("#555").setFontWeight("bold").setRotation(90);
        y1AxsLbl.setText(title + ttlSuffix);
        this.drawables.push(y1AxsLbl);

        var ticks = this.numYTick - 1;

        for(var i = 0; i <= ticks; i++) {

            var yTickText = new TextDrawable();
            yTickText.setAlignment("middle").setFontSize(13).setColor("#555");
            this.drawables.push(yTickText);

            var yTickLabelW = 40;
            var yTickTextH = this.h * i / ticks;
            yTickTextH = yTickTextH + (((ticks) / 2) - i) * 2;
            yTickText.setCoordinate(0 - yTickLabelW / 2, yTickTextH , 0, 0);

            var yTickValue = startValue + valueRange - i /ticks * valueRange;
            yTickValue = yTickValue / mtrPfx.baseValue;
            yTickText.setText(yTickValue.toFixed(1));
        }
    }

    this.constructY2Axis = function(y2AxisNode) {


        var title = y2AxisNode.children("Title").text();
        var unit = y2AxisNode.children("Unit").text();
        var startValue = parseFloat(y2AxisNode.children("StartValue").text());
        var endValue = parseFloat(y2AxisNode.children("EndValue").text());
        var valueRange = (endValue - startValue);

        var mtrPfx = getMetricPrefix(endValue);
        var ttlSuffix = "";

        if(mtrPfx.symbol !== "" || unit !== "") {
            ttlSuffix += ' (' + mtrPfx.symbol + unit + ')';
        }

        var lblW = 48;

        var y1AxsLbl = new TextDrawable();
        y1AxsLbl.setCoordinate(this.w + lblW, this.h / 2, 0, 0);
        y1AxsLbl.setAlignment("middle").setFontSize(14).setColor("#555").setFontWeight("bold").setRotation(90);
        y1AxsLbl.setText(title + ttlSuffix);
        this.drawables.push(y1AxsLbl);

        var ticks = this.numYTick - 1;

        for(var i = 0; i <= ticks; i++) {

            var yTickText = new TextDrawable();
            yTickText.setAlignment("middle").setFontSize(13).setColor("#555");
            this.drawables.push(yTickText);

            var yTickLabelW = 40;
            var yTickTextH = this.h * i / ticks;
            yTickTextH = yTickTextH + (((ticks) / 2) - i) * 2;
            yTickText.setCoordinate(this.w + yTickLabelW / 2, yTickTextH , 0, 0);

            var yTickValue = startValue + valueRange - i /ticks * valueRange;
            yTickValue = yTickValue / mtrPfx.baseValue;
            yTickText.setText(yTickValue.toFixed(1));
        }

    }

    this.draw = function () {

        for(var i = 0; i < this.drawables.length; ++i) {
            this.drawables[i].setSurface(this.surface);
            this.drawables[i].draw();
        }
        return this;
    };


}

function BoundingBox() {
    this.x = 0;
    this.y = 0;
    this.w = 0;
    this.h = 0;

    this.setCoordinate = function(x, y, w, h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    };
}
