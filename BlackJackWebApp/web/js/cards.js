function Cards(cards, direction) {
    var self = this;
    this.direction = direction || "left";
    this.cards = cards || [];
    this.element = (function() {
        return $("<div>").addClass("cards-container");
    })();
    this.add = function(cards) {
        var index = this.element.children().length;
        _.each(cards, function(card){
            self.cards.push(card);
            new Card(card.rank, card.suit).element.css(self.direction,(index++)*30).appendTo(self.element);
        });
        self.element.width(this.element.children().length*30);
    }
    this.clear = function() {
        self.cards = [];
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