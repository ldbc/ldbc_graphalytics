

$(document).ready(function () {
    loadVisualizer();
});

function loadVisualizer() {
    loadPage();
}

function loadPage() {

    var title = "Graphalytics Benchmark " +
        data.specification + ' [' + data.system.platform.acronym.toUpperCase() + " @ " + data.system.environment.acronym.toUpperCase() + ']';
    var tabItems = [];

    tabItems.push({name: "System under Test", link: "system", content: "systemPage"});
    tabItems.push({name: "Benchmark Configuration", link: "benchmark", content: "benchmarkPage"});
    tabItems.push({name: "Experimental Result", link: "result", content: "resultPage"});

    $("title").text(title);

    loadNavbar(title, tabItems);
    loadSidebar(tabItems);
    loadContent(tabItems);
    loadTab();
    // loadFooter();
}

function loadTab() {
    var tabName = getHttpParameters("tab");

    if(tabName) {
        $(".tab-pane").removeClass("active");
        $('#'+tabName).addClass("active");
    }
}


function loadNavbar(title, tabItems) {
    $('#navbar-div').empty();
    $('#navbar-div').append(navbar(title, tabItems));
}

function loadSidebar(tabItems) {
    $('#sidebar-div').empty();
    $('#sidebar-div').append(sidebar(tabItems));
}

function loadContent(tabItems) {
    $('#content-div').empty();
    $('#content-div').append(content(tabItems));
}

function sidebar(tabItems) {
    var sideItems = $('<ul class="nav nav-pills nav-stacked" />');

    tabItems.forEach(function(item){
        sideItems.append($('<li class="nav-item"><a class="nav-link" data-toggle="tab"' +
        ' href="#'+ item.link +'">'+item.name +'</a></li>'))
    });

    return sideItems;
}


function content(tabItems) {
    var content = $('<div class="tab-content"></div>');

    var isAssigned = false;

    tabItems.forEach(function(item){
        var tab = $('<div class="tab-pane'+((!isAssigned) ? " active" : " ") + '" id="'+item.link+'"></div>');
        isAssigned = true;
        content.append(tab);
        tab.append(runFunction(item.content, item.data));

    });

    return content;
}



function navbar(title, tabItems) {
    var navBar = $('<nav class="navbar navbar-dark navbar-fixed-top bg-inverse" />');
    var navLink = $('<a class="navbar-brand hidden-sm-down" href="#" >'+title+'</a>');
    navBar.append(navLink);
    navBar.append(navDropdown(title, tabItems));
    return navBar;
}


function navDropdown(title, tabItems) {

    var navBtnG = $('<ul class="nav hidden-md-up">');

    var navBtns = $('<li class="dropdown ">');
    navBtns.append($('<button href="#" class="dropdown-toggle btn btn-dark btn" data-toggle="dropdown" role="button" ' +
        'aria-haspopup="true" aria-expanded="false">' + title + '<span class="caret"></span></button>'));

    var navMenu = $('<ul class="dropdown-menu" aria-labelledby="nav-menu" />');

    tabItems.forEach(function(item){
        navMenu.append($('<li class="nav-item"><a class="nav-link" data-toggle="tab"' +
            ' href="#'+ item.link +'">'+item.name +'</a></li>'))
    });

    navBtns.append(navMenu);
    navBtnG.append(navBtns);
    return navBtnG
}


function loadFooter() {
    var footer = $('<footer id="footer" class="footer" />');
    footer.append($('<p>Granula Visualizer 0.1.2 &copy; 2016 Distributed Systems Group, Delft University of Technology</p>'));
    return footer;
}