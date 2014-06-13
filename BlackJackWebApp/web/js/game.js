/**
 * Created by idmlogic on 07-Jun-14.
 */

var playerListID = "#playerList";
var dealerCardsID = "#dealerCards";

function Game() {

    var players = [
        { name: 'Alex', isHuman: true, cards: [ { rank: 'A', suit:'H' }, { rank: '6', suit: 'S' },{ rank: 'J', suit:'C' } ] },
        { name: 'Computer', isHuman: false, cards: [ { rank: 'J', suit:'C' } ] }
    ]

    var dealer = [
        { rank: 'A', suit: 'H' },
        { rank: 'J', suit: 'C' }
    ];

    var playerList = $(playerListID);
    var dealerCards = $(dealerCardsID);

    for(var index in players) {
        var player = players[index];
        playerList.append(new Player(player).getDOMElement());
    }

    dealerCards.append(new Cards(dealer, "right").getDOMElement());
}

function Player(details) {
    this.playerName = details.name;
    this.isHuman = details.isHuman;
    this.cards = details.cards;
    this.getDOMElement = function() {
        var base = $("<div>");
        var icon = $("<i>").addClass("fa fa-" + (this.isHuman ? "male" : "laptop"));
        var name = $("<span>").text(this.playerName);
        $("<div>")
            .addClass("playername-container")
            .append(icon, name)
            .appendTo(base);
        new Cards(this.cards).getDOMElement().appendTo(base);
        return base;
    }
}

function Cards(cards, direction) {
    this.direction = direction || "left";
    this.cards = cards;
    this.getDOMElement = function() {
        var base = $("<div>").addClass("cards-container");
        for(var i = 0; i<cards.length; i++) {
            new Card(cards[i].rank, cards[i].suit).getDOMElement().css(this.direction,-i*55).appendTo(base);
        }
        return base;
    }
}

function Card(rank, suit) {
    this.rank = rank;
    this.suit = suit;
    this.getDOMElement = function() {
        return $("<span>").addClass("card card-" + this.suit.toLocaleLowerCase() + this.rank.toLocaleLowerCase());
    }
}