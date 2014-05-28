//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.14 at 04:59:47 PM IST 
//


package blackjack.engine.xml;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the blackjack.engine.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: blackjack.engine.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Cards }
     * 
     */
    public Cards createCards() {
        return new Cards();
    }

    /**
     * Create an instance of {@link Blackjack }
     * 
     */
    public Blackjack createBlackjack() {
        return new Blackjack();
    }

    /**
     * Create an instance of {@link Bet }
     * 
     */
    public Bet createBet() {
        return new Bet();
    }

    /**
     * Create an instance of {@link Players }
     * 
     */
    public Players createPlayers() {
        return new Players();
    }

    /**
     * Create an instance of {@link Player }
     * 
     */
    public Player createPlayer() {
        return new Player();
    }

    /**
     * Create an instance of {@link Bets }
     * 
     */
    public Bets createBets() {
        return new Bets();
    }

    /**
     * Create an instance of {@link Cards.Card }
     * 
     */
    public Cards.Card createCardsCard() {
        return new Cards.Card();
    }

}