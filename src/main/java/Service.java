import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Service {
	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML," +
			"like Gecko) Chrome/47.0.2526.106 Safari/537.36";
	private static String dateRegexp = "(<meta.+=" + "\"" + ")" +
			"([0-9]+?)-([0-9]+?)-([0-9]+?)T([0-9]+?:[0-9]+?):";
	private static String contentIframeRegexp="(<div id=\"streams1\">)(.*?)(</div>)";
	private static String contentSopRegexp="(<div id=\"sopstreams\">)(.*?)(</div>)";

	Document mainPageLoader(String http) {
		Document doc = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT)
						.get();
			} catch (java.net.UnknownHostException he) {
				System.out.println("Reload");
				he.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.err.println("Підключення до головної");
		}
		return doc;
	}

	String dataFinderNewsDate(String postBody) {
		Pattern patt = Pattern.compile(dateRegexp);
		Matcher match = patt.matcher(postBody);
		String date = "";
		while (match.find()) {
			date = match.group(2) + "-" + match.group(3) + "-" + match.group(4) +
					" " + match.group(5) + ":00";
		}
		return date;
	}

	String dataFinderNewsTime(String postBody) {
		Pattern patt = Pattern.compile(dateRegexp);
		Matcher match = patt.matcher(postBody);
		String date = "";
		while (match.find()) {
			date = match.group(2) + "-" + match.group(3) + "-" + match.group(4) +
					" " + match.group(5) + ":00";
		}
		return date;
	}

	String getNewsDate(String http) throws IOException {
		Document doc = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT)
						.get();
			} catch (java.net.SocketTimeoutException e) {
				System.err.println("Read time out");
				e.printStackTrace();
				getNewsDate(http);
			}
		}
		String data = dataFinderNewsDate(doc.select("meta[itemprop=startDate]").toString());
		return data;
	}

	String getNewsDateForHeader(String http) throws IOException, InterruptedException {
		Document doc = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT).get();
			} catch (java.net.SocketTimeoutException e) {
				System.err.println("Read time out");
				e.printStackTrace();
				getNewsDate(http);
			}catch(org.jsoup.HttpStatusException e){
				e.printStackTrace();
				Thread.sleep(600);

			}
		}
		String data = dataFinderNewsDate(doc.select("meta[itemprop=startDate]").toString());
		String dataDay = data.substring(8, 10);
		String dataMonth = data.substring(5, 7);
		String dataTime = data.substring(11, data.length() - 3);
		dataTime = dataTime.replace(":", "-");
		String generalData = dataDay + "." + dataMonth + "," + dataTime + " ";
		return generalData;
	}

	String getNewsHeader(String http) throws IOException, InterruptedException {
		Document doc = null;
		String title = "";
		while(doc==null){
			try{

				doc = Jsoup.connect(http).timeout(15000).followRedirects(true).userAgent(USER_AGENT).get();
			}catch(org.jsoup.HttpStatusException ex){
				ex.printStackTrace();
				Thread.sleep(1000);

			}
		}
		// return doc.select("h1[itemprop=name]").toString();
		title = doc.title();
		title = title.replace("LiveTV", "");
		title = title.replaceAll("/", "");
		title = title.replace("Прямая трансляция  Футбол.", "");
		title = title.replace("  ", " ");
		title = title.substring(0, title.length() - 1);
		title = "Трансляция матча " + title + ". Смотреть онлайн";
		title = title.replaceAll(" [0-9]+ [а-я]+", "");
		/*
		 * title = title+" Смотреть онлайн трансляцию "; title =
		 * title.replaceAll("Прямая", "Онлайн");
		 */
		return title;
	}

	String newContent(String oldContent){
		Pattern patt = Pattern.compile(contentIframeRegexp);
		Matcher match = patt.matcher(oldContent);
		String content = "";
		while (match.find()) {
			content = match.group(2);
		}
		return content;
	}

	String newSopContent(String oldContent){
		Pattern patt = Pattern.compile(contentSopRegexp);
		Matcher match = patt.matcher(oldContent);
		String content = "";
		while (match.find()) {
			content = match.group(2);
		}
		return content;
	}

	ArrayList<String> todayTranslationReader(){
		BufferedReader br = null;
		ArrayList<String> todayTranslationHrefs =new ArrayList<String>();
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("todayTranslationsDesk.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				todayTranslationHrefs.add(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return todayTranslationHrefs;
	}

}