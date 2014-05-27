/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package blackjack.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author idmlogic
 */
public class Settings {
    private static blackjack.xml.Settings binding = null;
    private final static String filepath = "settings.xml";
    private final static String url = "/blackjack/BlackJackWebService";
    
    private static void load() throws UnableToLoadSettingsException {
        if(binding == null) {
            try {
                JAXBContext jc = JAXBContext.newInstance(blackjack.xml.Settings.class);
                Unmarshaller u = jc.createUnmarshaller();
                binding = (blackjack.xml.Settings)u.unmarshal(new File(filepath));
            } 
            catch (JAXBException ex) {
                throw new UnableToLoadSettingsException();
            }
        }
    }
    
    private static void save() throws UnableToSaveSettingsException {
        if(binding != null) {
            try {
                JAXBContext jc = JAXBContext.newInstance(blackjack.xml.Settings.class);
                Marshaller m = jc.createMarshaller();
                m.marshal(binding, new File(filepath));
            } 
            catch (JAXBException ex) {
                throw new UnableToSaveSettingsException();
            }
        }
    }
    
    private static blackjack.xml.Settings buildSettings(String url) {
        blackjack.xml.Settings settings = new blackjack.xml.Settings();
        blackjack.xml.Settings.Server server = new blackjack.xml.Settings.Server();
        server.setUrl(url);
        settings.setServer(server);
        return settings;
    }
    
    public static String getServerURL() throws UnableToLoadSettingsException {
        load();
        return binding.getServer().getUrl() + url;
    }
    
    public static void setServerURL(String url) throws UnableToSaveSettingsException {
        binding = buildSettings(url);
        save();
    }

    public static class UnableToLoadSettingsException extends Exception {
        public UnableToLoadSettingsException() {
            
        }
    }
    
    public static class UnableToSaveSettingsException extends Exception {
        public UnableToSaveSettingsException() {
            
        }
    }
}
