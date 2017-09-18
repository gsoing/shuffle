package com.gso.shuffle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebServlet(name = "Shuffle", value = "/shuffle")
public class ShuffleAppEngine extends HttpServlet {


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");

    Pattern pattern = Pattern.compile("file:(\\s)'([^']+)'");


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/rss+xml");

        final SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");

        feed.setTitle("Shuffle");
        feed.setLink("http://www.europe1.fr/emissions/shuffle");
        feed.setDescription("Shuffle");

        final List entries = new ArrayList();

        Jsoup.connect("https://www.europe1.fr/emissions/shuffle")
                .get()
                .select("a[class=titre]")
                .forEach( link -> {
                    try {
                        log("found link " + link.attr("href"));


                        Document page = Jsoup.connect(link.attr("href")).get();


                        String title = page.select("meta[property=og:title]").attr("content");

                        Matcher matcher = pattern.matcher(page.toString());
                        String itemUrl = null;
                        if(matcher.find()){
                            log("find !");
                            itemUrl = matcher.group(2);
                        }

                        log("page title "+ title + " " + "itemUrl : " + itemUrl);

                        String[] desc = title.split("-");

                        SyndEntry entry = new SyndEntryImpl();
                        entry.setTitle(title);
                        entry.setLink(itemUrl);

                        entry.setPublishedDate(simpleDateFormat.parse(desc[2].trim()));
                        entry.setUri(itemUrl);

                        SyndContent description = new SyndContentImpl();
                        description.setType("audio/mpeg");
                        description.setValue(title);
                        entry.setDescription(description);
                        entries.add(entry);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });


        feed.setEntries(entries);
        SyndFeedOutput output = new SyndFeedOutput();
        try {
            output.output(feed,response.getWriter());
        } catch (FeedException e) {
            log("unable to generate feed",e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"unable to generate feed");
        }
    }


}
