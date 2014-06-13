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

function createGame() {
    new Game();
    gotToGame();
}

function gotToGame() {
    loadPage(PAGES.GAME, function() {
    });
}

function joinGame() {
    $(mainMenuContainerID).hide();
    $(gamesListContainerID).show();
}

loadPage(PAGES.WELCOME);

function hideAllMenuContainers() {
    $(actionContainerID).hide();
    $(betContainerID).hide();
    $(timerContainerID).hide();
    $(resignButtonID).hide();
    $(gamesListContainerID).hide();
}

$(function() {
    hideAllMenuContainers();

    $(betAmountID).text("\uf155" + DEFAULT_BET_AMOUNT);
    $(betSliderID).slider({
        range: "min",
        value: DEFAULT_BET_AMOUNT,
        min: 50,
        max: 1000,
        step: 50,
        slide: function( event, ui ) {
            $(betAmountID).text( "\uf155" + ui.value );
        }
    });

    $(actionTimerID).pietimer({
        timerSeconds: 10,
        showPercentage: true,
        callback: function() {
            $(actionTimerID).pietimer('reset');
        }
    });

    $(".button").each(function(i,el) {
        $(el).bind("mouseover",function(){
            $(this).addClass("ui-state-highlight");
        });
        $(el).bind("mouseout",function(){
           $(this).removeClass("ui-state-highlight");
        });
    });
});