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

public class DayCurrenciesPerNisXMLParser {
    public final String LOG_TAG = DayCurrenciesPerNisXMLParser.class.getSimpleName();
	// XML node keys
	static final String KEY_CURRENCY = "CURRENCY";
    static final String KEY_CURRENCYCODE = "CURRENCYCODE";
	static final String KEY_RATE = "RATE";
	static final String KEY_CURRENCIES = "CURRENCIES";
	static final String KEY_LAST_UPDATE = "LAST_UPDATE";

    private String dateString;
    private Context context;

    private HashMap<String, Double> rawValuesMap = new HashMap<>();
    public DayCurrenciesPerNisXMLParser(Context context, String dateArg){
        this.context = context;
        String xml = getXmlFromUrl(dateArg); // getting XML
        if(!xml.isEmpty()) {
            Document doc = getDomElement(xml); // getting DOM element
            if(doc != null){
                NodeList n = doc.getElementsByTagName(KEY_CURRENCIES);
                // looping through all item nodes <item>
                for (int i = 0; i < n.getLength(); i++) {
                    // creating new HashMap
                    Element e = (Element) n.item(i);
                    dateString = getValue(e, KEY_LAST_UPDATE);
                    if (dateString == null || dateString.isEmpty()) {
                        //no currencies for date
                        return;
                    }
                }

                NodeList nl = doc.getElementsByTagName(KEY_CURRENCY);
                if (nl != null) {
                    // looping through all item nodes <item>
                    for (int i = 0; i < nl.getLength(); i++) {
                        // creating new HashMap
                        Element e = (Element) nl.item(i);
                        // adding each child node to HashMap key => value
                        double rate = 0;
                        try {
                            rate = Double.parseDouble(getValue(e, KEY_RATE));
                        }
                        catch (Exception e1){
                            Log.e(LOG_TAG, "Error parsing "+getValue(e, KEY_CURRENCYCODE), e1);
                        }
                        rawValuesMap.put(getValue(e, KEY_CURRENCYCODE), rate);
                    }
                }
            }
        }
    }

    public HashMap<String, HashMap<String, Double>> getCurrenciesPerNIS(){
        if(dateString == null || dateString.isEmpty() ){
            return null;
        }
        HashMap<String, HashMap<String, Double>> map = new HashMap<>();
        map.put(dateString ,rawValuesMap);
        return map;
    }

    private String getXmlFromUrl(String dateArg) {
        HttpURLConnection urlConnection = null;
        try {
            String args = dateArg !=null && !dateArg.isEmpty() ? "?rdate="+dateArg : "";

            Uri builtUri = Uri.parse("http://www.boi.org.il/currency.xml"+args).buildUpon().build();
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.setRequestProperty("User-Agent", "Chrome/43.0.2357.130");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return "";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

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
//            Handler mainHandler = new Handler(context.getMainLooper());
//            mainHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(context, context.getResources().getString(R.string.error_no_response_from_server), Toast.LENGTH_LONG).show();
//                }
//            });
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            Utility.setRatesStatus(context, MetalsExchangeSyncAdapter.RATES_STATUS_SERVER_INVALID);
//            Handler mainHandler = new Handler(context.getMainLooper());
//            mainHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(context, context.getResources().getString(R.string.error_server_response_not_valid), Toast.LENGTH_LONG).show();
//                }
//            });
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "";
    }

    private Document getDomElement(String xml){
        xml = xml.replaceAll("[^\\x20-\\x7e]", "");
        Document doc;
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
    private String getElementValue( Node elem ) {
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