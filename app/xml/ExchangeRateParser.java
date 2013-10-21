package xml;

import model.ExchangeRate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parser for ECB XML Feed
 * */
public class ExchangeRateParser {

    /**
     * Use STaX events to parse the XML feed
     * @param xml InputStream of XML test
     * @param currency String of currency to find in the XML
     * @return List of ExchangeRate items
     */
    public List<ExchangeRate> parseRates(InputStream xml, String currency) {
        List<ExchangeRate> rates = new ArrayList<ExchangeRate>();

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader r = factory.createXMLStreamReader(xml);
            try {
                int event = r.getEventType();
                String date = null;
                boolean matchCurrency = false;
                boolean continueParse = true;
                while (continueParse) {
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        // Both the date and rates use the Cube element
                        if (r.getLocalName().equals("Cube")) {
                            for(int i = 0, n = r.getAttributeCount(); i < n; ++i) {
                                // First mark the date
                                if (r.getAttributeLocalName(i).equals("time")) {
                                    date = r.getAttributeValue(i);
                                }

                                // Now get the currency
                                if ((r.getAttributeLocalName(i).equals("currency")) && r.getAttributeValue(i).equals(currency)) {
                                    matchCurrency = true;
                                }

                                // Finally, get the rate and add to the list
                                if (r.getAttributeLocalName(i).equals("rate")) {
                                    if (matchCurrency) {
                                        ExchangeRate rate = new ExchangeRate(date, currency, Double.parseDouble(r.getAttributeValue(i)));
                                        rates.add(rate);
                                        matchCurrency = false;
                                    }

                                }
                            }
                        }
                    }

                    if (!r.hasNext()) {
                        continueParse = false;
                    } else {
                        event = r.next();
                    }
                }
            } finally {
                r.close();
            }
        } catch (Exception e) {
            Logger.error("Error parsing XML", e);
        }


        return rates;
    }

}
