function Player(details) {
    var self = this;
    this.playerName = details.name;
    this.isHuman = details.isHuman;
    this.cards = details.cards;
    this.isSplit = false;

    this.element = (function() {
        var base = $("<div>");
        var icon = $("<i>").addClass("ui-widget-header ui-corner-all fa fa-" + (self.isHuman ? "male" : "laptop"));
        var name = $("<span>").text(self.playerName);

        base.addClass("player-container");

        $("<div>")
            .addClass("playername-container ui-widget-content ui-corner-all")
            .append(icon, name)
            .appendTo(base);

        self.container = $("<div>")
            .addClass("player-main")
            .appendTo(base);

        self.actionPanel = $("<div>")
            .addClass("action-panel")
            .appendTo(self.container);

        self.cards1 = new Cards();
        self.cards1.element
            .addClass("hand1")
            .appendTo(self.container);

        self.cards2 = new Cards();
        self.cards2.element
            .addClass("hand2")
            .appendTo(self.container);

        self.cards = self.cards1;

        return base;
    })();

    this.clearPanel = function() {
        self.actionPanel.empty();
    }

    this.reset = function() {
        self.element.removeClass("winner");
        self.cards = self.cards1;
        self.cards1.clear();
        self.cards2.clear();
        self.container.append(self.cards2.element);
        self.isSplit = false;
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
        self.clearPanel();
        //todo
    };

    this.wait = function(time) {
        self.actionPanel.html(
            WidgetFactory.get('actionTimer', time, function() {
                self.clearPanel();
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
        self.isSplit = true;
    }

    this.switchHand = function() {
        if(self.isSplit) {
            self.cards = self.cards2;
            self.cards1.element.appendTo(self.container);
        }
    }

    this.declareWinner = function(prize) {
        self.element.addClass("winner");
    }

    this.reset();
}