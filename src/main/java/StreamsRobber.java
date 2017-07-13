import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class StreamsRobber {
	final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";

	List<String> getBrowserHhrefs(String http) throws IOException, InterruptedException {
		Elements translationHref = findAllAHrefs(findAllTables(getHtmlDocument(http)));

		ArrayList<String> listBrowserHref = new ArrayList<String>();

		for (Element href : translationHref) {
			if (!href.toString().contains("sopcast") && !href.toString().contains("acestream")) {
				listBrowserHref.add("http:"+href.attr("href"));
			}
		}

		for (int i = 1; i < listBrowserHref.size(); i++) {
			listBrowserHref.remove(i);
		}

		return listBrowserHref;
	}

	String getPlayer(String http) throws IOException, InterruptedException {
		Document doc = null ;
		while(doc==null){
			try{

				doc= getHtmlDocument(http);
			}catch(org.jsoup.HttpStatusException ex){
				//ex.printStackTrace();
				System.out.println("503");
				Thread.sleep(1000);
			}
			System.out.println(doc);
		}

		Elements idPlayerblock = doc.select("[id=playerblock]");
		String iframe = doc.getElementsByTag("iframe").toString();
		String script = idPlayerblock.select("[type=text/javascript]").toString();
		if(!(iframe.length() ==0)){
			System.out.print("baba");
		}

		if(!(iframe.length()==0)){
			return iframe;
		}
		else if(!(script.length()==0)){
			return script;
		}else{
			System.out.println("нема плеєра");
			return "-1";
		}

	}

	List<String> getSopHrefs(String http) throws IOException, InterruptedException {
		Elements translationHref = findAllAHrefs(findAllTables(getHtmlDocument(http)));

		ArrayList<String> listSopAndAceHref = new ArrayList<String>();

		for (Element href : translationHref) {
			if (href.toString().contains("sop://broker.sopcast.com:")) {
				listSopAndAceHref.add(href.attr("href"));
			}
		}

		return listSopAndAceHref;
	}

	private Document getHtmlDocument(String address) throws IOException, InterruptedException{
		Document doc=null;;
		while(doc==null){
			try{
				doc=Jsoup.connect(address).userAgent(USER_AGENT).get();
			}catch(org.jsoup.HttpStatusException ex){
				ex.printStackTrace();
				Thread.sleep(1500);
			}catch(java.net.SocketTimeoutException ex){
				Thread.sleep(2000);
			}
		}
		return doc;
	}

	private Elements findAllTables(Document doc){
		return doc.select("table[style=margin-left: 26px;]");
	}

	private Elements findAllAHrefs(Elements elements){
		return elements.select("a[href]");
	}
}