function Player(details) {
    var self = this;
    this.money = details.money;
    this.playerName = details.name;
    this.isHuman = details.type == "HUMAN";
    this.cards = details.cards;
    this.isSplit = false;
    this.currentBet = 0;

    this.element = (function() {
        var base = $("<div>");
        var icon = $("<i>").addClass("ui-widget-header ui-corner-all fa fa-" + (self.isHuman ? "male" : "laptop"));
        var money = $("<span>").addClass("ui-widget-header ui-corner-all").append("<i class='fa fa-dollar'></i>");
        self.moneyContainer = $("<span>").appendTo(money);
        self.arrow = $("<i class='fa fa-arrow-right'></i>");
        var bet = self.bet = $("<span>").addClass("ui-widget-header ui-corner-all").append(self.arrow);
        self.betContainer = $("<span>").appendTo(bet);
        var name = $("<span class='playername-text'>").text(self.playerName);

        base.addClass("player-container");

        $("<div>")
            .addClass("playername-container ui-widget-content ui-corner-all")
            .append(icon, money, bet, name)
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

    this.setBet = function(amount) {
        if(!amount) {
            this.betContainer.empty();
            this.bet.hide();
        }
        else {
            this.currentBet = amount;
            this.betContainer.text(amount);
            this.bet.show();
        }
    }

    this.setMoney = function(amount) {
        this.moneyContainer.text(amount);
    };

    this.setMoney(details.money);

    this.clearPanel = function() {
        self.actionPanel.empty();
    }

    this.reset = function() {
        self.setBetArrow("right");
        self.element.removeClass("winner");
        self.cards = self.cards1;
        self.cards1.clear();
        self.cards2.clear();
        self.container.append(self.cards2.element);
        self.isSplit = false;
        self.prize = 0;
        self.currentBet = 0;
        self.setBet();
    }

    this.requireBet = function(callback) {
        self.actionPanel.html(
            WidgetFactory.get('betSlider', self.lastBet || DEFAULT_BET_AMOUNT, function(value) {
                self.lastBet = value;
                self.clearPanel();
                callback(value);
            })
        );
    };

    this.placeBet = function(amount) {
        self.clearPanel();
        this.setMoney(self.money-=amount);
        this.setBet(self.currentBet=amount);
    };

    this.doubleBet = function() {
        this.setMoney(self.money-=self.currentBet);
        this.setBet(self.currentBet+=self.currentBet);
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
        self.setMoney(self.money-=self.currentBet);
        self.setBet(self.currentBet+=self.currentBet);
    }

    this.switchHand = function() {
        if(self.isSplit) {
            self.cards = self.cards2;
            self.cards1.element.appendTo(self.container);
        }
    }

    this.declareWinner = function(prize) {
        self.setMoney(self.money+=prize);
        self.setBet(self.prize+=prize);
        self.setBetArrow("left");
    }

    this.setBetArrow = function(dir) {
        self.arrow.removeClass("fa-arrow-right fa-arrow-left");
        self.arrow.addClass("fa-arrow-" + dir);
    }

    this.declareBlackjack = function() {
        self.element.addClass("winner");
    }

    this.reset();
}