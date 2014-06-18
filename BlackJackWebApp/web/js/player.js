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

        var temp = $("<div>").appendTo(base);

        self.actionPanel = $("<div>")
            .addClass("action-panel")
            .appendTo(temp);


        self.cards1 = new Cards();
        self.cards1.element.appendTo(temp);


        self.cards2 = new Cards();
        self.cards2.element.appendTo(temp);

        return base;
    })();

    this.clearPanel = function() {
        self.actionPanel.html("&nbsp;");
    }

    this.reset = function() {
        self.element.removeClass("winner");
        self.cards = self.cards1;
        self.cards1.clear();
        self.cards2.clear();
        self.cards2.element.insertAfter(self.cards1.element);
    }

    this.requireBet = function(callback) {
        self.actionPanel.html(
            WidgetFactory.get('betSlider', DEFAULT_BET_AMOUNT, function(value) {
                self.clearPanel();
                callback(value);
            })
        );
    };

    this.placeBet = function(amount) {
        self.actionPanel.empty();
        //todo
    };

    this.wait = function(time) {
        self.actionPanel.html(
            WidgetFactory.get('actionTimer', time, function() {
                self.actionPanel.empty();
            })
        );
    }

    this.requireAction = function(callback) {
        self.actionPanel.html(
            WidgetFactory.get('actionPanel', function(action) {
                self.clearPanel();
                callback(action);
            })
        );
    }

    this.splitCards = function(cards) {
        var cards1 = [self.cards1.cards[0],cards[0]];
        var cards2 = [self.cards1.cards[1],cards[1]];
        self.cards1.clear();
        self.cards1.add(cards1);
        self.cards2.add(cards2);
    }

    this.switchHand = function() {
        self.cards = self.cards2;
        self.cards1.element.insertAfter(self.cards2.element);
    }

    this.declareWinner = function(prize) {
        self.element.addClass("winner");
    }

    this.reset();
}