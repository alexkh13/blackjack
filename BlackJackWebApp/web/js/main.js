
// constants
var DEFAULT_SERVER_URL = "http://localhost:8081/blackjack/BlackJackWebService";
var DEFAULT_BET_AMOUNT = 100;
var MAX_NUMBER_OF_PLAYERS = 6;

var currentTimeout;

$(function() {
    $('.spinner .btn:first-of-type').on('click', function() {
        var input = $(this).parent().parent().children('input');
        var val = parseInt(input.val(), 10) + 1;
        if(val <= MAX_NUMBER_OF_PLAYERS) input.val( val );
    });
    $('.spinner .btn:last-of-type').on('click', function() {
        var input = $(this).parent().parent().children('input');
        var min = parseInt(input.attr("min-val"));
        var val = parseInt(input.val(), 10) - 1;
        if(val >= min) input.val( val );
    });

    $("#serverUrl").val(DEFAULT_SERVER_URL);

    $.ajax({
        url: 'login'
    })
        .done(function(data) {
            serverUrl = data.serverUrl;
            playerName = data.playerName;
            showAvailableGames();
        })
        .fail(function() {
            $("#login").modal('show');
        });
});

var serverUrl;
var playerName;

function escape(text) {
    return text.replace(/'/g,"\\\\'");
}

function showAvailableGames(current) {
    $("#game").hide();
    if(current) {
        $(current).modal("hide");
    }
    $("#games").modal('show');
    $("#serverUrlDisplay").text(serverUrl);
    $("#playerNameDisplay").text(playerName);
    var list = $("#availableGamesList");
    list.empty();
    $.ajax({
        url: 'api'
    })
        .done(function(games) {
            list.empty();
            $(games).each(function(i, game) {
                var row = $("<tr>");
                var button = $("<button class='btn btn-xs'>");
                if(game.playerId) {
                    button.text("Resume game");
                    button.addClass("btn-primary");
                    button.bind("click",function() {
                        if(game.status == 'ACTIVE') {
                            $.ajax({
                                url: 'api/' + game.name
                            })
                                .done(function(data) {
                                    showGame(game.name, data.players, true);
                                })
                                .fail(function() {
                                    // todo:
                                })
                        }
                        else {
                            showGameLobby(game.name);
                        }
                    });
                }
                else {
                    button.text(game.status == 'ACTIVE' ? "Active game" : "Join game");
                    button.addClass("btn-default");
                    if(game.status == 'ACTIVE') {
                        button.attr('disabled', true);
                    }
                    else {
                        button.addClass("btn-success");
                        button.bind("click",function() {
                            joinGame(game.name);
                        });
                    }
                }
                row.addClass("game-row");
                row.append("<td>" + game.name + "</td>");
                row.append("<td>" + game.numberOfHumans + "</td>");
                row.append("<td>" + game.numberOfComputers + "</td>");
                row.append("<td>" + game.numberOfJoinedHumans + "/" + game.numberOfHumans + "</td>");
                row.append("<td>" + game.status + "</td>");
                row.append($("<td>").append(button));
                list.append(row);
            });
            if(!games.length) {
                var row = $("<tr>");
                row.addClass("game-row-empty");
                row.append("<td colspan='6'> No available games on server</td>");
                list.append(row);
            }
        })
        .fail(function() {
            $("#games").modal('hide');
            $("#login").modal('show');
        })
}

function login() {
    serverUrl = $("#serverUrl").val();
    playerName = $("#playerName").val();
    $.ajax({
        url: 'login',
        type: 'POST',
        data: JSON.stringify({
            serverUrl: serverUrl,
            playerName: playerName
        })
    })
        .done(function() {
            $("#login").modal('hide');
            showAvailableGames();
        })
        .fail(function(xhr,status,err) {
            console.log(status, err, xhr);
        })
}

function showCreateGame() {
    $("#games").modal("hide");
    $("#create").modal("show");
    $("#game-name").val(playerName + "'s game");
}

var gameName;


function refereshPlayerList(players, theRest) {
    var list = $("#playersList");
    list.empty();
    $(players).each(function(i, player) {
        var row = $("<tr>");
        row.append("<td style='text-align:center'><i class='fa fa-" + (player.type == "HUMAN" ? 'male' : 'laptop') + "'></i></td>");
        row.append("<td>" + player.name + "</td>");
        row.append("<td>" + player.money + "<i class='fa fa-dollar'></i></td>");
        list.append(row);
    });
    for(var i=0; i<theRest;i++) {
        var row = $("<tr>");
        row.append("<td colspan='3'>Empty player slot</td>");
        list.append(row);
    }
    return
}

function showGameLobby(name){
    gameName = name;
    $("#games").modal("hide");
    $("#lobby").modal("show");
    $("#gameNameDisplay").text(name);

    $("#playersList").empty();

    function refresher() {
        $.ajax({
            url: 'api/' + name
        })
            .done(function(data){
                refereshPlayerList(data.players, data.numberOfHumans - data.numberOfJoinedHumans);
                if(data.events.length) {
                    showGame(name, data.players);
                }
                else {
                    currentTimeout = setTimeout(refresher, 1000);
                }
            })
            .fail(function() {

            })
    }

    $("#lobbyStatusText").removeClass("btn-success");
    $("#lobbyStatusText").text("Waiting for other players...");

    if(!$("#lobby").data("bind")) {
        $("#lobby").on('shown.bs.modal', refresher);
        $("#lobby").data("bind", true);
    }
}

function createGame() {
    var numHumans = parseInt($("#num-humans").val());
    var numComputers = parseInt($("#num-computers").val());
    if(numHumans+numComputers > MAX_NUMBER_OF_PLAYERS) {
        $("#createGameErrorMessage").text("Max number of player is " + MAX_NUMBER_OF_PLAYERS);
        return;
    }
    $("#createGameErrorMessage").empty();
    $.ajax({
        url: 'api',
        type: 'POST',
        data: JSON.stringify({
            gameName: gameName = $("#game-name").val(),
            humans: $("#num-humans").val(),
            computers: $("#num-computers").val(),
            playerName: playerName,
            type: 'CREATE'
        })
    })
        .done(function() {
            $("#create").modal("hide");
            showGameLobby(gameName);
        })
        .fail(function(xhr) {
            $("#createGameErrorMessage").text(xhr.responseJSON.message);
        })
}

function joinGame(name) {
    $.ajax({
        url: 'api',
        type: 'POST',
        data: JSON.stringify({
            type: 'JOIN',
            gameName: name,
            playerName: playerName
        })
    })
        .done(function() {
            gameName = name;
            $("#games").modal("hide");
            showGameLobby(name);
        })
        .fail(function() {

        })
}

function showGame(game, players, fast) {
    gameName = game;
    if(!fast) {
        $("#lobbyStatusText").addClass("btn-success");
        $("#lobbyStatusText").text("Game ready! Please wait...");
    }
    setTimeout(function() {
        $("#games").modal("hide");
        $("#lobby").modal("hide");
        $("#game").show();
        new Game(game, players);

    }, fast ? 0 : 1000);
}

function stopTimer() {
    $("#actionTimer").pietimer('reset');
}

function hideTimer() {
    $("#actionTimer").hide();
}

function showTimer() {
    $("#actionTimer").show();
}

function startTimer(timeout) {
    stopTimer();
    $("#actionTimer").pietimer({
        timerSeconds: timeout,
        showPercentage: true,
        callback: function() {
            stopTimer();
        }
    });
}

function resign(from) {
    function handler() {
        showAvailableGames(from);
    }
    $.ajax({
        url: 'api',
        type: 'POST',
        data: JSON.stringify({
            gameName: gameName,
            type: 'RESIGN'
        })
    })
        .done(handler)
        .fail(handler)
}

function showError(message) {
    $("#errorMessage").text(message);
    $("#error").modal("show");
}

function closeError() {
    $("#error").modal("hide");
}