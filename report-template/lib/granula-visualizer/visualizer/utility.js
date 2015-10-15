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


function getHttpParameters(name) {
    qs = document.location.search.split("+").join(" ");

    var params = {}, tokens,
        re = /[?&]?([^=]+)=([^&]*)/g;

    while (tokens = re.exec(qs)) {
        params[decodeURIComponent(tokens[1])]
            = decodeURIComponent(tokens[2]);
    }
    return params[name];
}

function isChrome() {
    var isOpera = !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
    // Opera 8.0+ (UA detection to detect Blink/v8-powered Opera)
    var isFirefox = typeof InstallTrigger !== 'undefined';   // Firefox 1.0+
    var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
    // At least Safari 3+: "[object HTMLElementConstructor]"
    var isChrome = !!window.chrome && !isOpera;              // Chrome 1+
    var isIE = /*@cc_on!@*/false || !!document.documentMode; // At least IE6

    return isChrome;
}

function isFirefox() {
    var isFirefox = typeof InstallTrigger !== 'undefined';   // Firefox 1.0+
    return isFirefox;
}

function fullUrl(url) {
    var a = document.createElement('a');
    a.href = url;
    return a.href;
}

function isUrlCompatible(url) {

    var isCompatible = true;
    var isLocal = (fullUrl(url)).lastIndexOf("file:///", 0) === 0;
    if(isFirefox()) {

        return true;
    } else if(isChrome()) {
        if(isLocal) {
            displayAlert('You are running Chrome on local web pages. Try to start Chrome with \"google-chrome (or chromium-browser) --allow-file-access-from-files\".');
            return false;
        } else {
            return true;
        }
    } else {
        displayAlert('Your browser may not be supported (not Firefox or Chrome).');
        return false;
    }

}

function displayAlert(text) {
    alert(text);
}

function isSameOrigin(url) {

    var loc = window.location,
        a = document.createElement('a');

    a.href = url;

    return a.hostname == loc.hostname &&
        a.port == loc.port &&
        a.protocol == loc.protocol;
}

function getFilename(fullPath) {
    return fullPath.replace(/^.*[\\\/]/, '');
}

function get2Digit(number) {
    return ("0" + number).slice(-2);
}

function getDomainURL() {
    return [location.protocol, '//', location.host, location.pathname].join('');
}

function getParentURL(url, rep) {
    if(rep === 0) {
        return url.substring(0, url.lastIndexOf( "/" ));
    } else {
        return getParentURL(url.substring(0, url.lastIndexOf( "/" )), rep - 1)
    }

}

function isDomainLocal() {
    return getDomainURL().lastIndexOf("file:///", 0) === 0 || getDomainURL().indexOf("://localhost") > -1;
}

function isUsingFileProtocol() {
    return getDomainURL().lastIndexOf("file:///", 0) === 0;
}

function printFast(object) {
    console.log(JSON.stringify(object))
}

function printSize(object) {
    if(object == undefined) {
        console.log("Undefined Object" + ": " +  '0');
    } else if(object.length == undefined) {
        console.log(object.constructor.name + ": " +  '1');
    } else {
        console.log(object[0].constructor.name + ": " +  object.length);
    }

}

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

function getPredefinedColor(number) {
    if (number == 0) {
        return "#D33";
    } else if (number == 1) {
        return "#3D3";
    } else if (number == 2) {
        return "#33D";
    } else if (number == 3) {
        return "#3DD";
    } else if (number == 4) {
        return "#DD3";
    } else if (number == 5) {
        return "#D3D";
    } else {
        return getRandomColor();
    }
}

function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function getMetricPrefix(valueRange) {

    var numDigit = Math.log10(Math.abs(valueRange));

    if(isBetween(numDigit, 0, 3)) {
        return new MetricPrefix("", "1");
    } else if(isBetween(numDigit, 3, 6)) {
        return new MetricPrefix("k", "1000");
    } else if(isBetween(numDigit, 6, 9)) {
        return new MetricPrefix("M", "1000000");
    } else if(isBetween(numDigit, 9, 12)) {
        return new MetricPrefix("G", "1000000000");
    } else if(isBetween(numDigit, 12, 15)) {
        return new MetricPrefix("T", "1000000000000");
    } else if(isBetween(numDigit, -3, 0)) {
        return new MetricPrefix("m", "0.001");
    } else if(isBetween(numDigit, -6, -3)) {
        return new MetricPrefix("μ", "0.000001");
    } else {
        console.error("Unexpected valueRange " + valueRange)
    }
}

function MetricPrefix(symbol, baseValue) {
    this.symbol = symbol;
    this.baseValue = baseValue;
}

function isBetween(value, startValue, endValue) {
    return value >= startValue && value <=endValue;
}

function pickRandom(objects) {
    return objects[randomInt(0, objects.length - 1)];
}

/** escapest html special characters to html. */
function textToHtml(str) {

}

function getPrintableXml(xmlNode) {

    var xml = (new XMLSerializer()).serializeToString(xmlNode);

    xml = xml.replace(' xmlns="http://www.w3.org/1999/xhtml"', '');
    var text = vkbeautify.xml(xml);

    var pr_amp = /&/g;
    var pr_lt = /</g;
    var pr_gt = />/g;
    text = text.replace(pr_amp, '&amp;').replace(pr_lt, '&lt;').replace(pr_gt, '&gt;');
    text = text.replace(new RegExp("\n", "g"), '<br>');

    return text;

}

function change_brightness(hex, percent){
    // strip the leading # if it's there
    hex = hex.replace(/^\s*#|\s*$/g, '');

    // convert 3 char codes --> 6, e.g. `E0F` --> `EE00FF`
    if(hex.length == 3){
        hex = hex.replace(/(.)/g, '$1$1');
    }

    var r = parseInt(hex.substr(0, 2), 16),
        g = parseInt(hex.substr(2, 2), 16),
        b = parseInt(hex.substr(4, 2), 16);

    if(percent >= 0) {
        hex =  '#' +
        ((0|(1<<8) + r + (256 - r) * percent / 100).toString(16)).substr(1) +
        ((0|(1<<8) + g + (256 - g) * percent / 100).toString(16)).substr(1) +
        ((0|(1<<8) + b + (256 - b) * percent / 100).toString(16)).substr(1);
    } else if(percent < 0) {
        hex =  '#' +
        ((0|(1<<8) + r * (100 - percent) / 100).toString(16)).substr(1) +
        ((0|(1<<8) + g * (100 - percent) / 100).toString(16)).substr(1) +
        ((0|(1<<8) + b * (100 - percent) / 100).toString(16)).substr(1);
    }

    return hex;
}










