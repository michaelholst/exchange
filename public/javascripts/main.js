var drawChart = function(data) {
    // Get the list and draw the bar chart, max 90 days.
};

var loadRates = function(curr) {
    // This one is tricky, instead of lazy loading on the server (the Promises were already quite chained up),
    // so we'll go and fetch from the DB. If nothing is found, then refresh and load again.
    // Gets the job done from the user's perspective, could be better and more transparent if done on the server.
    $.get("/exchangerates/" + curr, function(data) {
        if (data.length === 0) {
            refresh(curr, function() {
                alert("Refreshed " + curr);
                loadRates(curr);
            });
        } else {
            alert("Loaded " + curr);
            drawChart(data);
        }
    });
};

var refresh = function(curr, success) {
    // Post to refresh the currency, then callback
    $.post("/refresh/" + curr, function(data) {
        success();
    });
};

var refreshCurrent = function() {
    // Get the current value in the combo
    var curr = $("#currency").val();

    // Reload the data from the 3rd party and load the new rates
    refresh(curr, function() {
        alert("Refreshed " + curr);
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

    // Refresh the default
    refreshCurrent();
};