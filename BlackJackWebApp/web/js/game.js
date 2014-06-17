/**
 * Created by idmlogic on 07-Jun-14.
 */

var playerListID = "#playerList";
var dealerCardsID = "#dealerCards";

function Game(gameName, players) {

    var self = this;

    this.lastEventId = -1;

    this.gameName = gameName;
    this.playerList = $(playerListID);

    this.dealerCards = new Cards([], "right");
    $(dealerCardsID).append(this.dealerCards.element);

    this.numOfBets = 0;
    this.playerCount = 0;

    $("#inGameNameDisplay").text(gameName);

    var playersMap = this.playersMap = {}
    _.each(players, function(player) {
        var player = playersMap[player.name] = new Player(player);
        self.playerList.append(player.element);
        self.playerCount++;
    });

    this.pullEvents = function() {
        console.log("pullEvents from " + (self.lastEventId + 1));
        $.ajax({
            url: 'api/' + self.gameName
        })
            .done(function(data) {
                function filter(event) {
                    return event.id > self.lastEventId;
                }
                var events = _.sortBy(_.filter(data.events, filter), "id");
                _.each(events, function(event) {
                    var currentEvent = parseEvent(event);
                    self.lastEventId = currentEvent.id;
                });
                setTimeout(self.pullEvents, 1000);
            })
    }

    function parseEvent(event) {
        if(!event) return;
        console.log("parseEvent", event);
        eventHandlers[event.type].apply(self,[event]);
        return event;
    }

    this.pullEvents();
}

var eventHandlers = {
    'NEW_ROUND': function() {
        this.numOfBets = 0;
        _.each(this.playersMap, function(player) {
            player.cards.clear();
        });
        this.dealerCards.clear();
    },
    'PLAYER_TURN': function(event) {
        this.playerList.prepend(this.playersMap[event.playerName].element);
    },
    'CARDS_DEALT': function(event) {
        if(event.playerName) {
            this.playersMap[event.playerName].cards.add(event.cards);
        }
        else {
            this.dealerCards.clear();
            this.dealerCards.add(event.cards);
        }
    },
    'USER_ACTION': function(event) {
        var player = this.playersMap[event.playerName];
        player.actionPanel.empty();
        switch(event.playerAction) {
            case "PLACE_BET":
                this.numOfBets++;
                player.placeBet(event.money);
                break;
            case "HIT":
                player.cards.add(event.cards);
                break;
        }
    },
    'GAME_WINNER': function(event) {

    },
    'PROMPT_PLAYER_TO_TAKE_ACTION': function(event) {
        var player = this.playersMap[event.playerName];
        if(playerName != event.playerName) {
            player.wait(event.timeout, function() {

            });

        }
        else if(this.numOfBets == this.playerCount) {
            player.requireAction(function(action) {
                var data = {
                    type: 'ACTION',
                    action: action,
                    gameName: gameName
                }
                $.ajax({
                    url: 'api',
                    type: 'POST',
                    data: JSON.stringify(data)
                })
                    .done(function() {
                    })
            });
        }
        else {
            player.requireBet(function(amount) {
                var data = {
                    type: 'ACTION',
                    action: 'PLACE_BET',
                    money: amount,
                    gameName: gameName
                }
                $.ajax({
                    url: 'api',
                    type: 'POST',
                    data: JSON.stringify(data)
                })
                    .done(function() {
                    })
            });
        }
    }
}

function Player(details) {
    var self = this;
    this.playerName = details.name;
    this.isHuman = details.isHuman;
    this.cards = details.cards;
    this.element = (function() {
        var base = $("<div>");
        var icon = $("<i>").addClass("fa fa-" + (self.isHuman ? "male" : "laptop"));
        var name = $("<span>").text(self.playerName);
        base.addClass("player-container");
        $("<div>")
            .addClass("playername-container")
            .append(icon, name)
            .appendTo(base);
        self.actionPanel = $("<div>")
            .addClass("action-panel")
            .appendTo(base);
        self.cards = new Cards();
        self.cards.element.appendTo(base);
        return base;
    })();

    this.requireBet = function(callback) {
        self.actionPanel.html($("#betContainer").clone().show());
        $("#betContainer #betAmount").text("\uf155" + DEFAULT_BET_AMOUNT);
        $("#betContainer #betSlider").slider({
            range: "min",
            value: DEFAULT_BET_AMOUNT,
            min: 50,
            max: 1000,
            step: 50,
            slide: function( event, ui ) {
                $("#betContainer #betAmount").text( "\uf155" + ui.value );
            }
        });
        $("#betContainer #betTrigger").bind("click",function() {
            self.actionPanel.empty();
            callback($("#betContainer #betSlider").slider("value"));
        });
    };

    this.placeBet = function(amount) {
        self.actionPanel.empty();
        //todo
    };

    this.wait = function(time, callback) {
        self.actionPanel.html($("#timerContainer").clone().show());
        $("#timerContainer").pietimer({
            timerSeconds: time,
            showPercentage: true,
            callback: function() {
                self.actionPanel.empty();
                callback();
            }
        });
    }

    this.requireAction = function(callback) {
        self.actionPanel.html($("#actionContainer").clone().show());
        $("#actionContainer #hitButton").bind("click",function() {
            self.actionPanel.empty();
            callback('HIT');
        });
        $("#actionContainer #standButton").bind("click",function() {
            self.actionPanel.empty();
            callback('STAND');
        });
        $("#actionContainer #splitButton").bind("click",function() {
            self.actionPanel.empty();
            callback('SPLIT');
        });
        $("#actionContainer #doubleButton").bind("click",function() {
            self.actionPanel.empty();
            callback('DOUBLE');
        });
    }
}

function Cards(cards, direction) {
    var self = this;
    this.direction = direction || "left";
    this.cards = cards;
    this.element = (function() {
        return $("<div>").addClass("cards-container");
    })();
    this.add = function(cards) {
        var index = this.element.children().length;
        _.each(cards, function(card){
            new Card(card.rank, card.suit).element.css(self.direction,-(index++)*55).appendTo(self.element);
        });
    }
    this.clear = function() {
        self.element.empty();
    }
}

function Card(rank, suit) {
    this.rank = rank;
    this.suit = suit;

    function getRankClass(rank) {
        switch(rank) {
            case "TWO": return 2;
            case "THREE": return 3;
            case "FOUR": return 4;
            case "FIVE": return 5;
            case "SIX": return 6;
            case "SEVEN": return 7;
            case "EIGHT": return 8;
            case "NINE": return 9;
            case "TEN": return 10;
            case "JACK": return 'j';
            case "QUEEN": return 'q';
            case "KING": return 'k';
            case "ACE": return 'a';
        }
    }
    function getSuitClass(suit) {
        return suit ? suit.toLowerCase()[0] : suit;
    }

    this.element = (function() {
        return $("<span>").addClass("card card-" + getSuitClass(suit) + getRankClass(rank));
    })();
}