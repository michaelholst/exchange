var error = function(m) {
    $("#error").text(m);
    $("#error").show("slow", function() {
        $("#error").hide("slow");
    });
}

var disableButtons = function() {
    $("#currency").attr("disabled", "disabled");
    $("#refreshCurrent").attr("disabled", "disabled");
    $("#refreshAll").attr("disabled", "disabled");
}

var enableButtons = function() {
    $("#currency").removeAttr("disabled");
    $("#refreshCurrent").removeAttr("disabled");
    $("#refreshAll").removeAttr("disabled");
}

var drawChart = function(data) {
    // Clear the div
    $("#barChart").empty();

    // Get the list and draw the bar chart, max 90 days.
    var barWidth = 14;
    var width = (barWidth + 3) * data.length;
    var height = 200;

    var x = d3.scale.linear().domain([0, data.length]).range([0, width]);
    var y = d3.scale.linear().domain([0, d3.max(data, function(datum) { return datum.rate; })]).
        rangeRound([0, height]);

    // Add the canvas to the DOM
    var barChart = d3.select("#barChart").
        append("svg:svg").
        attr("width", width).
        attr("height", height + 30);

    barChart.selectAll("rect").
        data(data).
        enter().
        append("svg:rect").
        attr("x", function(datum, index) { return x(index); }).
        attr("y", function(datum) { return height - y(datum.rate); }).
        attr("height", function(datum) { return y(datum.rate); }).
        attr("width", barWidth).
        attr("fill", "#2d578b");

    barChart.selectAll("text.yAxis").
        data(data).
        enter().append("svg:text").
        attr("x", function(datum, index) { return x(index) + barWidth; }).
        attr("y", height).
        attr("dx", -barWidth/2).
        attr("text-anchor", "middle").
        attr("style", "font-size: 6; font-family: Helvetica, sans-serif").
        text(function(datum) { return datum.rate;}).
        attr("transform", "translate(0, 18)").
        attr("class", "yAxis");
};

var loadRates = function(curr) {
    // This one is tricky, instead of lazy loading on the server (the Promises were already quite chained up),
    // so we'll go and fetch from the DB. If nothing is found, then refresh and load again.
    // Gets the job done from the user's perspective, could be better and more transparent if done on the server.
    disableButtons();
    $.get("/exchangerates/" + curr, function(data) {
        if (data.length === 0) {
            refresh(curr, function() {
                loadRates(curr);
            });
        } else {
            drawChart(data);
            enableButtons();
        }
    });
};

var refresh = function(curr, f) {
    // Post to refresh the currency, then callback
    $.post("/refresh/" + curr, function(data) {
        f();
    });
};

var refreshCurrent = function() {
    disableButtons();

    // Get the current value in the combo
    var curr = $("#currency").val();

    // Reload the data from the 3rd party and load the new rates
    refresh(curr, function() {
        loadRates(curr);
    });
};

var refreshAll = function() {
    // We're going to reload all the currencies, not very nice, but by design our server-side is dumb
    refresh("USD", function() {

    });
    refresh("JPY", function() {

    });
    refresh("BGN", function() {

    });
    refresh("CZK", function() {

    });
    refresh("DKK", function() {

    });
    refresh("GBP", function() {

    });
    refresh("HUF", function() {

    });
    refresh("LTL", function() {

    });
    refresh("LVL", function() {

    });
    refresh("PLN", function() {

    });
    refresh("RON", function() {

    });
    refresh("SEK", function() {

    });
    refresh("CHF", function() {

    });
    refresh("NOK", function() {

    });
    refresh("HRK", function() {

    });
    refresh("RUB", function() {

    });
    refresh("TRY", function() {

    });
    refresh("AUD", function() {

    });
    refresh("BRL", function() {

    });
    refresh("CAD", function() {

    });
    refresh("CNY", function() {

    });
    refresh("HKD", function() {

    });
    refresh("IDR", function() {

    });
    refresh("ILS", function() {

    });
    refresh("INR", function() {

    });
    refresh("KRW", function() {

    });
    refresh("MXN", function() {

    });
    refresh("MYR", function() {

    });
    refresh("NZD", function() {

    });
    refresh("PHP", function() {

    });
    refresh("SGD", function() {

    });
    refresh("THB", function() {

    });
    refresh("ZAR", function() {

    });
};

var init = function() {
    // Register events
    $("#currency").on("change", function() {
       loadRates($(this).val());
    });

    $("#refreshCurrent").on("click", function() {
        refreshCurrent();
    });

    $("#refreshAll").on("click", function() {
        refreshAll();
    });

    // Register global error reporting
    $( document ).ajaxError(function( event, jqxhr, settings, exception ) {
        error(exception);
    });

    // Refresh the default
    loadRates("USD");
};