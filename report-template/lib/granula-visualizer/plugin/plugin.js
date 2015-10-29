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


function loadPlugin() {
    // customization

    //loadGranulaPlugin();
    loadGraphalyticsPlugin();

}

function loadGranulaPlugin() {
    $('#cache-div').load('../data/history.htm', function () {
        $('#cache-div #recent-arc a').each(function() {
            addJobList($(this).text());
            drawDashboard();
        });
    });
}

function loadGraphalyticsPlugin() {
    var overviewPage;
    var domainUrl = getDomainURL();
    var isLocal = isDomainLocal();
    if(isLocal) {
        overviewPage = getParentURL(domainUrl, 2) + "/index.html";
    } else {
        overviewPage = getParentURL(domainUrl, 2) + "/index.html";
    }
    var navList = $('#navbar').find('ul');
    navList.append($('<li><a id="overview-btn" href="' + overviewPage + '">Overview</a></li>'));

    var brand = $('#nav-container').find('.navbar-brand');
    brand.text("Graphalytics Benchmark Report");

    var title = $('title');
    title.text("Graphalytics Benchmark Report");
}
