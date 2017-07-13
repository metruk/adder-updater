import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class NewsService {
	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML,"
			+ "like Gecko) Chrome/47.0.2526.106 Safari/537.36";
	private static String dateRegexp = "(<meta.+=" + "\"" + ")"
			+ "([0-9]+?)-([0-9]+?)-([0-9]+?)T([0-9]+?:[0-9]+?):";
	static Logger logger = Logger.getLogger(NewsService.class.getName());
	static LinkedHashMap <String, String> hashmapHrefAndTitle = new LinkedHashMap <String, String>();
	static DbAccess db=new DbAccess();

	static Document loadPage(String http) {
		Document doc = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT).get();
			} catch (java.net.UnknownHostException he) {
				logger.logp(Level.WARNING, "NewsService", "mainPageLoader", "Перепідключення");
				he.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.logp(Level.INFO, "NewsService", "mainPageLoader", "Підключення до головної");
		}
		return doc;
	}

	static String dataFinderNewsDate(String postBody) {
		Pattern patt = Pattern.compile(dateRegexp);
		Matcher match = patt.matcher(postBody);
		String date = "";
		while (match.find()) {
			date = match.group(2) + "-" + match.group(3) + "-" + match.group(4)+ " " + match.group(5) + ":00";
		}
		return date;
	}

	static String dataFinderNewsDateWithoutTime(String postBody) {
		Pattern patt = Pattern.compile(dateRegexp);
		Matcher match = patt.matcher(postBody);
		String date = "";
		while (match.find()) {
			date = match.group(4) + "." + match.group(3) + "." + match.group(2);
		}
		return date;
	}

	static String getNewsDate(String http) throws IOException {
		Document doc = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT)
						.get();
			} catch (java.net.SocketTimeoutException e) {
				logger.logp(Level.INFO, "NewsService", "getNewsDate", " Час очікування підключення вийшов");
				e.printStackTrace();
				getNewsDate(http);
			}
		}
		String data = dataFinderNewsDate(doc.select("meta[itemprop=startDate]").toString());
		return data;
	}

	String getNewsDateForHeader(String http) throws IOException, InterruptedException {
		final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
		Document doc = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT).get();
			} catch (java.net.SocketTimeoutException e) {
				logger.logp(Level.INFO, "NewsService", "getNewsDateForHeader", " Час очікування підключення вийшов");
				e.printStackTrace();
				Thread.sleep(600);
				getNewsDateForHeader(http);
			} catch(org.jsoup.HttpStatusException ex){
				logger.logp(Level.INFO, "NewsService", "getNewsDateForHeader", " Fetching URL!");
				ex.printStackTrace();
				Thread.sleep(600);
				getNewsDateForHeader(http);

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

	static String getNewsDateWithoutTime(String http) throws IOException {
		final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
		Document doc = null;
		String data = null;
		while (doc == null) {
			try {
				doc = Jsoup.connect(http).timeout(10000).userAgent(USER_AGENT).get();
				data = dataFinderNewsDateWithoutTime(doc.select("meta[itemprop=startDate]").toString());
			} catch (java.net.SocketTimeoutException e) {
				logger.logp(Level.INFO, "NewsService", "getNewsDateWithoutTime", " Час очікування підключення вийшов");
				getNewsDateWithoutTime(http);
			}
		}
		return data;
	}

	String getNewsHeader(String http) throws IOException {
		Document doc = null;
		String title = "";

		while (doc == null) {
			try{
				doc = Jsoup.connect(http).timeout(500).followRedirects(true).userAgent(USER_AGENT).get();
			}catch(java.net.SocketTimeoutException ex){
				ex.printStackTrace();
			}catch(org.jsoup.HttpStatusException e) {
				e.printStackTrace();
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
		System.out.println(title);
		/*title = title.replace("e", "е");
		title = title.replace("E", "е"); */
		System.out.println(title);
		//tested part if error don't touch
		/*
		 * title = title+" Смотреть онлайн трансляцию "; title =
		 * title.replaceAll("Прямая", "Онлайн");
		 */
		return title;
	}

	String getGuiMetaValue(String header) throws IOException {

		XmlWorker xml=new XmlWorker();
		String gui=xml.readXml(header,"guids.xml","post","postTitle","id");

		return gui;
	}

	int getTerm(String header) throws IOException {
		Integer termId;
		XmlWorker xml=new XmlWorker();
		String gui=xml.readXml(header,"terms.xml","term","termTitle","id");
		try{
			termId = Integer.valueOf(gui);
		}catch(java.lang.NumberFormatException ex){
			termId=57;
		}

		return termId;

	}

	String newsNameGenerator(String header) {
		header = header.toLowerCase();
		header = header.replace(" ", "-");
		header = header.replace(".", "-");
		header = header.replace("–", "");
		header = header.replace(",", "-");

		char[] english = { 'a', 'b', 'v', 'g', 'd', 'e', 'e', 'j', 'z', 'i',
				'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'h', 'f', 'u',
				's', 's', 'q', 'u', 'a', 'i', 'c', 'j', 'c' };

		char[] russian = { 'а', 'б', 'в', 'г', 'д', 'е', 'э', 'ж', 'з', 'и',
				'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'х', 'ф', 'у',
				'ш', 'щ', 'ь', 'ю', 'я', 'ы', 'ч', 'й', 'ц' };
		for (int i = 0; i < header.length(); i++) {
			for (int j = 0; j < russian.length; j++) {
				if (header.charAt(i) == russian[j]) {
					header = header.replace(russian[j], english[j]);

				}
			}
		}
		header = header.replaceAll("translacia", "online");
		header = header.replaceAll("matca", "");
		header = header.replaceAll("cempionat", "tournament");
		header = header.replace("---", "-");
		header = header.replace("--", "-");
		header = header.replace("-smotretq-onlajn", "");
		// header=header.replaceAll("[0-9].+", "");
		return header;
	}


	String postTextGenerator(String header) throws IOException {

		String[] time = header.split("[А-Я].+");
		List<String> list = Arrays.asList(time);

		String advMixer="<div id=\"MIXADV_1532\" class=\"MIXADVERT_NET\"></div>\n" +
				"<script type=\"text/javascript\" src=\"https://s.mixadvert.com/show/?id=1532\" async></script>";
		
		String sopcastAds= "<div id=\"ads\"> <div id=\"vfQDEphkIPN9qWtPHXBY\"></div><script type=\"text/javascript\" async=\"async\" src=\"http://mycdn4.ru/uploads/blockjs/vfQDEphkIPN9qWtPHXBY.js\"></script></div>";

		String mainText =
				"<h4><em><span style=\"color: #ff0000;\"><strong>Начало матча: " + list.get(0) +
						"(мск)</strong></span></em></h4>" +
						"Ссылки на плеера с трансляциями к матчу, а также ссылки на Sopcast трансляции " +
						"будут доступны за 15-20 минут до начала матча.\n" +
						
						"<span style=\"color: #ff0000;\"><em><strong>Выберите плеер. Трансляция откроется в новом" +
						" окне(под блоком рекламы):</strong></em></span>\n" +

						"<!-- MarketGidComposite Start --><div id=\"MarketGidScriptRootC601360\"> <div id=\"MarketGidPreloadC601360\">"+
						"<a id=\"mg_add601360\" href=\"http://usr.marketgid.com/demo/celevie-posetiteli/\" target=\"_blank\"><img src=\"//cdn.marketgid.com/images/marketgid_add_link.png\" style=\"border:0px\"></a><br> "
						+ "<a href=\"http://marketgid.com/\" target=\"_blank\">Загрузка...</a> </div> <script> "
						+ "(function(){"
						+ "var D=new Date(),d=document,b='body',ce='createElement',ac='appendChild',st='style',ds='display',n='none',gi='getElementById';"
						+ "var i=d[ce]('iframe');i[st][ds]=n;d[gi](\"MarketGidScriptRootC601360\")[ac](i);try{var iw=i.contentWindow.document;iw.open();iw.writeln(\"<ht\"+\"ml><bo\"+\"dy></bo\"+\"dy></ht\"+\"ml>\");iw.close();var c=iw[b];}"
						+ "catch(e){var iw=d;var c=d[gi](\"MarketGidScriptRootC601360\");}var dv=iw[ce]('div');dv.id=\"MG_ID\";dv[st][ds]=n;dv.innerHTML=601360;c[ac](dv);"
						+ "var s=iw[ce]('script');s.async='async';s.defer='defer';s.charset='utf-8';s.src=\"//jsc.marketgid.com/f/o/footlivehd.com.601360.js?t=\"+D.getYear()+D.getMonth()+D.getDate()+D.getHours();c[ac](s);})();"
						+ "</script></div><!-- MarketGidComposite End -->"+
						"<div id=\"streams1\">Плееры будут доступны перед началом матча</div>" +
						"<h4><em><strong>SopCast:</em></strong></h4>" +
						
						"<div id=\"sopstreams\"><em><strong>SopCast ссылки будут доступны перед началом матча</strong></em></div>" +
						"&nbsp;\n&nbsp;\n&nbsp;\n&nbsp;\n";

		return mainText;
	}

	void mainPagePublishCurrentDate(String header,String currentDateForPublishHeader, int idNews, DbAccess worker)
			throws ParseException, SQLException {
		int dateBorderSubstring = 5;
		// add to if == true if top match boolean flag=topMatch(header);
		if (idNews > 0) {
			String dateNews = header.substring(0, dateBorderSubstring);
			System.out.println(dateNews);
			boolean b=currentDateForPublishHeader.contains(dateNews);
			System.out.println(b);
			if (currentDateForPublishHeader.contains(dateNews)) {
				logger.logp(Level.INFO, "NewsService", "mainPagePublishCurrentDate", "Дати співавли, пубілкую в сьогоднішні");

				db.insertTerm(idNews,54);
				boolean flag=hasTopTeamNameInString(header);
				if(flag){
					db.insertTerm(idNews,52);
				}
			} else {
				logger.logp(Level.INFO, "NewsService", "mainPagePublishCurrentDate", "Дати не співпали,   в сьогоднішні не публікую");
			}
		} else {
			logger.logp(Level.INFO, "NewsService", "mainPagePublishCurrentDate", "id з даноим заголовком не існує в базі,не пуюлікую на головну");
		}
	}

	static List<String> getStreamsUrls(){
		List<String> list = new ArrayList<String>();
		list.add("http://livetv.sx/competitions/408/");//biathlon
		list.add("http://livetv.sx/competitions/657/");//тов турнир
		list.add("http://livetv.sx/competitions/84/");//ч.м хокей
		list.add("http://livetv.sx/competitions/65/");//fnl
		list.add("http://livetv.sx/competitions/418/");//hockey
		list.add("http://livetv.sx/competitions/167/");//hockey
		list.add("http://livetv.sx/competitions/73/"); //куб укр
		list.add("http://livetv.sx/competitions/43/"); // чемп укр
		list.add("http://livetv.sx/competitions/74/"); // куб фран
		list.add("http://livetv.sx/competitions/62/"); //куб фран л
		list.add("http://livetv.sx/competitions/37/"); //чемп фран
		list.add("http://livetv.sx/competitions/82/"); // куб нім
		list.add("http://livetv.sx/competitions/129/"); //суперкуб нім
		list.add("http://livetv.sx/competitions/36/"); //чемп нім
		list.add("http://livetv.sx/competitions/75/"); // куб італ
		list.add("http://livetv.sx/competitions/11/"); //чемп італ
		list.add("http://livetv.sx/competitions/5/"); //товарняк
		list.add("http://livetv.sx/competitions/55/"); //куб ісп
		list.add("http://livetv.sx/competitions/141/"); //суперкуб ісп
		list.add("http://livetv.sx/competitions/15/"); //чемп ісп
		list.add("http://livetv.sx/competitions/1/"); //чемп анг
		list.add("http://livetv.sx/competitions/143/"); //куб анг л
		list.add("http://livetv.sx/competitions/8/"); //куб анг
		list.add("http://livetv.sx/competitions/242/");//supercups
		list.add("http://livetv.sx/competitions/90/"); //куб рос
		list.add("http://livetv.sx/competitions/42/"); //чемп рос
		list.add("http://livetv.sx/competitions/900/");//q le
		list.add("http://livetv.sx/competitions/901/");//qualify
		list.add("http://livetv.sx/competitions/265/"); //ЛЄ
		list.add("http://livetv.sx/competitions/7/"); //ЛЧ
		list.add("http://livetv.sx/competitions/1250/");//EUR
		list.add("http://livetv.sx/competitions/201/");//club world

		return list;
	}

	static boolean hasTopTeamNameInString(String header){
		System.out.println(header);
		 String [] topTeams = new String [] {
				 "Арсенал",
				 "Челси",
				 "Манчестер",
				 "Лестер",
				 "Ливерпуль",
				 "Эвертон",
				 "Бавария",
				 "Боруссия",
				 "Милан",
				 "Ювентус",
				 "Интер",
				 "Наполи",
				 "Мадрид",
				 "Барселона",
				 "Севилья",
				 "ЦСКА",
				 "Ростов",
				 "Зенит",
				 "Краснодар",
				 "Локомотив",
				 "Динамо",
				 "Рубин",
				 "Спартак",
				 "Суперкубок",
				 "Чемпионат России",
				 "Шахтер",
				 "ПСЖ",
				 "Тоттенхэм",
				 "Лига Европы",
				 "Лига Чемпионов",
				 "Чемпионат Мира",
				 "Чемпионат Европы",
				 "ЧМ-2018",
				 "КХЛ"
		 };

		for(int i=0;i<topTeams.length;i++){
			if(header.contains(topTeams[i])){
				return true;
			}
		}
		return false;
	}

	List<String> todayTranslations(String title,String href,String currentDateForPublishHeader){
		hashmapHrefAndTitle.put(title, href);

		ArrayList<String> todayHrefs = new ArrayList<String>();
		Set<Map.Entry<String, String>> set = hashmapHrefAndTitle.entrySet();
		for (Map.Entry<String, String> me : set) {
			if (me.getKey().contains(currentDateForPublishHeader)) {
				logger.logp(Level.INFO, "NewsService", "todayTranslations", "Сьогоднішні дати співавли, додано в поточний список трансляцій");
				todayHrefs.add(me.getValue());
			}else{
				logger.logp(Level.INFO, "NewsService", "todayTranslations", "Сьогоднішні дати не співавли,не додано в поточний список трансляцій");
			}
		}
		return todayHrefs;
	}

	void todayTranslationsWriter(List<String> todayHrefAndTitle){
		try {
			String  content = null;
			File file = new File("todayTranslationsDesk.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i=0;i<todayHrefAndTitle.size();i++){
				content=(String) todayHrefAndTitle.get(i);
				bw.write(content+"\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	String timeCutter(String title){
		title=title.substring(11,title.length());
		return title;
	}

	void deleteSameNewsMaker() throws SQLException{
		ArrayList<String> cuttedTitles = new ArrayList<String>();
		LinkedHashMap<String, Integer> newsPost = new  LinkedHashMap<String,Integer>();
		List<String> allTranslationTitles = new ArrayList<String>();
		allTranslationTitles=db.sameTitlesFinder();
		for (int i = 0; i < allTranslationTitles.size(); i++) {
			String newTitle=timeCutter(allTranslationTitles.get(i));
			cuttedTitles.add(newTitle);
		}

		for(int i=0;i<cuttedTitles.size();i++){
			for(int j=i+1;j<cuttedTitles.size();j++){
				if(cuttedTitles.get(i).equals(cuttedTitles.get(j))){

					db.sameTitlesContent(cuttedTitles.get(i),newsPost);
				}
			}
		}

		Set<Entry<String, Integer>> set = newsPost.entrySet();
		String header;
		Integer id;
		ArrayList<String> headers= new ArrayList<String>();
		ArrayList<Integer> ids= new ArrayList<Integer>();
		for (Entry<String, Integer> me : set) {
			header=me.getKey();
			headers.add(header);
			id=me.getValue();
			ids.add(id);
		}
		for(int i=0;i<headers.size()-1;i++){
			logger.logp(Level.INFO, "NewsService", "deleteSameNews","The same news"+"Head "+timeCutter(headers.get(i)) +"id "+ ids.get(i));
			if(timeCutter(headers.get(i)).equals(timeCutter(headers.get(i+1)))){
				if(ids.get(i)>ids.get(i+1)){
					db.deleteSameNews(ids.get(i));
				}else if(ids.get(i+1)>ids.get(i)){
					db.deleteSameNews(ids.get(i+1));
				}
			}
		}

	}

	public static void main(String[] args) throws ParseException, SQLException{
		NewsService s= new NewsService();
		//s.mainPagePublishCurrentDate("16.04,17-00 Трансляция матча guse – pes. Чемпионат Англии. Премьер-Лига. Смотреть онлайн","16.04", 18947, db);
		;
		ArrayList<String> list = new ArrayList();
		list.add("Vbkf");
		list.add("Милан");
		list.add("Милан");
		list.add("Мадрид");
		list.add("Челси");
		list.add("Ливерпуль");
		list.add("рак");
		list.add("Ювентус");
		list.add("ПСЖ");
		for(int i=0;i<list.size();i++){
			s.hasTopTeamNameInString(list.get(i));
		}

	}

}