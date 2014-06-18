/**
 * Created by idmlogic on 02-Jun-14.
 */

// constants
var PAGES_DIRECTORY = "pages";
var DEFAULT_BET_AMOUNT = 100;

// DOM elements identifiers
var mainContainerID = "#mainContainer";
var betSliderID = "#betSlider";
var betAmountID = "#betAmount";
var actionTimerID = "#actionTimer";
var betContainerID = "#betContainer";
var timerContainerID = "#timerContainer";
var actionContainerID = "#actionContainer";
var gamesListID = "#gamesList";
var gamesListContainerID = "#gamesContainer";
var backButtonID = "#backButton";
var resignButtonID = "#resignButton";
var mainMenuContainerID = "#mainMenuContainer";

// enums
var PAGES = {
    SETUP: "setup.html",
    GAME: "game.html",
    WELCOME: "welcome.html"
};

var currentTimeout;

function loadPage(page, callback) {
    $.get(PAGES_DIRECTORY + "/" + page, function(html) {
        $(mainContainerID).html(html);
        if(callback) callback();
    });
}

function goBack() {
    loadPage(PAGES.WELCOME, function() {
        toggleGoBackButton(false);
        hideAllMenuContainers();
        $(mainMenuContainerID).show();
    });
}

function toggleGoBackButton(value) {
    $(backButtonID + " span").text(value ? "Back" : "BlackJack");
}

function goToCreateGame() {
    loadPage(PAGES.SETUP, function() {
       $(mainMenuContainerID).hide();
    });
}

function hideAllMenuContainers() {
    $(actionContainerID).hide();
    $(betContainerID).hide();
    $(timerContainerID).hide();
    $(resignButtonID).hide();
    $(gamesListContainerID).hide();
}

$(function() {
    hideAllMenuContainers();

    $('.spinner .btn:first-of-type').on('click', function() {
        var input = $(this).parent().parent().children('input');
        input.val( parseInt(input.val(), 10) + 1);
    });
    $('.spinner .btn:last-of-type').on('click', function() {
        var input = $(this).parent().parent().children('input');
        input.val( parseInt(input.val(), 10) - 1);
    });

    $(".button").each(function(i,el) {
        $(el).bind("mouseover",function(){
            $(this).addClass("ui-state-highlight");
        });
        $(el).bind("mouseout",function(){
           $(this).removeClass("ui-state-highlight");
        });
    });

    $("#serverUrl").val("http://localhost:8081/blackjack/BlackJackWebService");

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
            $(games).each(function(i, game) {
                var row = $("<tr>");
                var button = $("<button class='btn btn-xs'>");
                if(game.playerId) {
                    button.text("Resume game");
                    button.addClass("btn-success");
                    button.bind("click",function() {
                        if(game.status == 'ACTIVE') {
                        $.ajax({
                            url: 'api/' + game.name,
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
                    button.text("Join game");
                    button.addClass("btn-default");
                    button.bind("click",function() {
                        joinGame(game.name);
                    });
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
        row.append("<td>" + player.name + "</td>");
        row.append("<td>" + player.type + "</td>");
        row.append("<td>" + player.money + "</td>");
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

    $("#lobbyStatusText").text("Waiting for other players...");

    $("#lobby").on('shown.bs.modal', function() {
        refresher();
    });
}

function createGame() {
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
        .fail(function(xhr, status, err) {
            console.log(xhr, status, err);
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

function runInterval(run, checker, finish, interval) {
    var interval = setInterval(function() {
        if(!checker(run())) {
            clearInterval(interval);
            finish();
        }
    }, interval);
    return interval;
}

function showGame(game, players, fast) {
    gameName = game;
    if(!fast) {
        $("#lobbyStatusText").text("Game ready! Please wait...");
    }
    setTimeout(function() {
        $("#games").modal("hide");
        $("#lobby").modal("hide");

        new Game(game, players);

    }, fast ? 0 : 3000);
}

function startTimer(timeout, callback) {
    console.log("start timer")
    $("#actionTimer").pietimer('reset');
    $("#actionTimer").pietimer({
        timerSeconds: timeout,
        showPercentage: true,
        callback: function() {
            callback();
        }
    });
}