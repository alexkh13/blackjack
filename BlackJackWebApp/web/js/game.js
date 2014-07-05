
var playerListID = "#playerList";
var dealerCardsID = "#dealerCards";

function Game(gameName, players) {

    var self = this;

    this.lastEventId = -1;

    this.gameName = gameName;
    this.playerList = $(playerListID).empty();

    this.dealerCards = new Cards([], "left");
    $(dealerCardsID).empty().append(this.dealerCards.element);

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
                self.nextPullTimout = setTimeout(self.pullEvents, 1000);
            })
            .fail(function() {
                showAvailableGames();
            })
    }

    function parseEvent(event) {
        if(!event) return;
        eventHandlers[event.type].apply(self,[event]);
        return event;
    }

    this.pullEvents();
}

var eventHandlers = {
    'NEW_ROUND': function() {
        this.numOfBets = 0;
        _.each(this.playersMap, function(player) {
            player.reset();
        });
        this.dealerCards.clear();
    },
    'PLAYER_TURN': function(event) {
        var player = this.playersMap[event.playerName];
        if(player.isHuman) {
            this.playerList.prepend(this.playersMap[event.playerName].element);
        }
    },
    'PLAYER_RESIGNED': function(event) {
        this.playersMap[event.playerName].element.remove();
        if(playerName == event.playerName) {
            clearTimeout(this.nextPullTimout);
            showError("You've been kicked out of the game due to timeout.");
            showAvailableGames();
        }
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
                if(event.money == 21) {
                    player.declareBlackjack();
                }
                if(event.money > 20) {
                    player.switchHand();
                }
                break;
            case "SPLIT":
                player.splitCards(event.cards);
                break;
            case "STAND":
                player.switchHand();
                break;
            case "DOUBLE":
                player.doubleBet();
                player.cards.add(event.cards);
                if(event.money == 21) {
                    player.declareBlackjack();
                }
                if(event.money > 20) {
                    player.switchHand();
                }
                break;
        }
    },
    'GAME_WINNER': function(event) {
        var player = this.playersMap[event.playerName];
        player.declareWinner(event.money);
    },
    'PROMPT_PLAYER_TO_TAKE_ACTION': function(event) {
        var self = this;
        if(event.timeout) startTimer(event.timeout);
        var player = this.playersMap[event.playerName];
        if(playerName != event.playerName) {

        }
        else if(this.numOfBets == this.playerCount) {
            player.requireAction(function(action) {
                hideTimer();
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
                        $("#actionErrorMessage").empty();
                        showTimer();
                        stopTimer();
                    })
                    .fail(function(xhr) {
                        $("#actionErrorMessage").text(xhr.responseJSON ? xhr.responseJSON.message : "Can't do that");
                        showTimer();
                        delete event.timeout;
                        eventHandlers['PROMPT_PLAYER_TO_TAKE_ACTION'].apply(self, [event]);
                    })
            });
        }
        else {
            player.requireBet(function(amount) {
                hideTimer();
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
                        $("#betErrorMessage").empty();
                        showTimer();
                        stopTimer();
                    })
                    .fail(function(xhr) {
                        $("#betErrorMessage").text(xhr.responseJSON ? xhr.responseJSON.message : "Can't do that");
                        showTimer();
                        delete event.timeout;
                        eventHandlers['PROMPT_PLAYER_TO_TAKE_ACTION'].apply(self, [event]);
                    })
            });
        }
    }
}