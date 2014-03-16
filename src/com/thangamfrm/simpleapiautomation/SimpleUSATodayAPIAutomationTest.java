package com.thangamfrm.simpleapiautomation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class SimpleUSATodayAPIAutomationTest extends Assert {

    private Properties props;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        try {
            // initialize props
            props = new Properties();

            // read from file using LineNumberReader
            LineNumberReader lnr = new LineNumberReader(new BufferedReader(
                    new FileReader(new File("resources/config.properties"))));
            String tmp = null;
            while ((tmp = lnr.readLine()) != null) {
                tmp = tmp.trim();
                if (tmp.equals("") || tmp.startsWith("#")) {
                    continue;
                }
                String[] keyValuePair = tmp.split("=");
                props.put(keyValuePair[0].trim(), keyValuePair[1].trim());
            }

            // load
            if (new File("resources/overrides.properties").exists()) {
                props.load(new FileReader(new File(
                        "resources/overrides.properties")));
            }
            lnr.close();
        } catch (Exception e) {
            throw new APIAutomationException("Unable to configure!", e);
        }
    }

    @DataProvider(name = "sections")
    public Object[][] sectionDataProvider() {
        try {
            List<String> sections = new ArrayList<String>();
            LineNumberReader lnr = new LineNumberReader(new BufferedReader(
                    new FileReader(new File("resources/rss_sections.txt"))));
            String tmp = null;
            while ((tmp = lnr.readLine()) != null) {
                tmp = tmp.trim();
                sections.add(tmp);
            }

            // convert list of string to two dimensional string array
            String[][] data = new String[sections.size()][1];
            int index = 0;
            for (String section : sections) {
                data[index] = new String[] { section };
                index++;
            }
            lnr.close();
            return data;
        } catch (Exception e) {
            throw new APIAutomationException(
                    "Unable to load and populate sections.txt file!", e);
        }
    }

    @Test(groups = { "regression", "smoke" }, dataProvider = "sections")
    public void testUSATodayAPI(String section) {

        sleep(750);

        String apiKey = props.getProperty("api.key");
        String url = props.getProperty("api.url");
        try {
            String urlWithParam = String.format("%s?section=%s", url, section);
            URL usaTodayURL = new URL(String.format("%s&api_key=%s",
                    urlWithParam, apiKey));

            URLConnection conn = usaTodayURL.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine = null;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine).append(
                        System.getProperty("line.separator"));
            }

            assertTrue(sb.toString().length() > 0,
                    "RSS content not found! URL: " + urlWithParam);

            // Reading the feed
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(
                    sb.toString().getBytes("UTF-8"))));
            @SuppressWarnings("unchecked")
            List<SyndEntry> entries = feed.getEntries();

            assertTrue(entries.size() > 0, "Unable to find syndicate entries!");

            Iterator<SyndEntry> itEntries = entries.iterator();

            while (itEntries.hasNext()) {
                SyndEntry entry = itEntries.next();
                assertNotNull(entry.getTitle(),
                        "Syndicate entry title found null!");
                assertNotNull(entry.getLink(),
                        "Syndicate entry link found null!");
                assertNotNull(entry.getPublishedDate(),
                        "Syndicate entry Publish Date found null");
                assertNotNull(entry.getDescription().getValue(),
                        "Syndicate entry Description found null");
            }

        } catch (Exception e) {
            fail("Error occurred when running usatoday api test!", e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        props = null;
    }

    protected void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ie) {
            // ignore
        }
    }
}
