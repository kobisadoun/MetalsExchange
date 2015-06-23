package com.kobi.metalsexchange.app.sync;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kobi.metalsexchange.app.Utility;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LastDayCurrenciesPerNisXMLParser {
    public final String LOG_TAG = LastDayCurrenciesPerNisXMLParser.class.getSimpleName();
	// XML node keys
	static final String KEY_CURRENCY = "CURRENCY";
    static final String KEY_CURRENCYCODE = "CURRENCYCODE";
	static final String KEY_RATE = "RATE";
	static final String KEY_CURRENCIES = "CURRENCIES";
	static final String KEY_LAST_UPDATE = "LAST_UPDATE";

    private String lastUpdate = "";
    private Context context;

    private HashMap<String, Double> rawValuesMap = new HashMap<String, Double>();
	public LastDayCurrenciesPerNisXMLParser(Context context){
        this.context = context;
		String xml = getXmlFromUrl(); // getting XML
		Document doc = getDomElement(xml); // getting DOM element


        NodeList n = doc.getElementsByTagName(KEY_CURRENCIES);
        // looping through all item nodes <item>
        for (int i = 0; i < n.getLength(); i++) {
            // creating new HashMap
            Element e = (Element) n.item(i);
            lastUpdate = getValue(e, KEY_LAST_UPDATE);
        }

		NodeList nl = doc.getElementsByTagName(KEY_CURRENCY);
		// looping through all item nodes <item>
		for (int i = 0; i < nl.getLength(); i++) {
			// creating new HashMap
			Element e = (Element) nl.item(i);
			// adding each child node to HashMap key => value
            rawValuesMap.put(getValue(e, KEY_CURRENCYCODE), Double.parseDouble(getValue(e, KEY_RATE)));
		}
	}

    public HashMap<String, HashMap<String, Double>> getCurrenciesPerNIS(){
        HashMap<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();
        map.put(lastUpdate ,rawValuesMap);
        return map;
    }

    private String getXmlFromUrl() {
        String xml = null;
        HttpURLConnection urlConnection = null;
        try {

            BufferedReader reader = null;
            Uri builtUri = Uri.parse("http://www.boi.org.il/currency.xml").buildUpon().build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "Chrome/41.0.2272.89");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                //setRatesStatus(getContext(), RATES_STATUS_SERVER_DOWN);
                return "";
            }
            return buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            Utility.setRatesStatus(context, MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_DOWN);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            Utility.setRatesStatus(context, MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_INVALID);
//            Handler mainHandler = new Handler(getContext().getMainLooper());
//            mainHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_server_response_not_valid), Toast.LENGTH_LONG).show();
//                }
//            });
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        // return XML
        return xml;
    }

    private Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        return doc;
    }

    /** Getting node value
     * @param elem element
     */
    private final String getElementValue( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    private String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }
}