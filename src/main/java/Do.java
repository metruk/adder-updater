import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Date;

public class Do {

	private static ArrayList<String> postedHeaders = new ArrayList<String>();
	private static List<String> currentDbTranslationList = null;
	private static int dbPostCounter = 0;

	private static DbAccess dbWorker = new DbAccess();
	private static NewsService newsService = new NewsService();
	private static String dateTodayForTranslationPublish;
	private static String yesterdayDateForTranslationPublish;
	private static Logger logger = Logger.getLogger(Do.class.getName());
	private static Logger loggerFile = Logger.getLogger(Do.class.getName());
	private static List<String> todayHrefAndTitle = new ArrayList<String>();
	private static List<Integer> yesterdayMainPageNews = new ArrayList<Integer>();

	// second part
	static Service service = new Service();
	static StreamsRobber robber = new StreamsRobber();
	static ArrayList<String> hrefsFrames = new ArrayList<String>();
	static ArrayList<String> todayAdded = new ArrayList<String>();

	static ArrayList<String> todayTranslationsHrefs = new ArrayList<String>();
	static ArrayList<String> todayTranslationsHeaders = new ArrayList<String>();
	static ArrayList<String> currentTranslations = new ArrayList<String>();
	static List<String> browserHrefs;
	static ArrayList<String> todayTranslations;
	static List<String> sopHrefs;



	public static void main(String[] args) throws IOException, ParseException,
			ClassNotFoundException, InterruptedException, SQLException {
		BasicConfigurator.configure();
		MysqlDAO mysql = new MysqlDAO();
		/*Integer number = null;
		boolean b = false;

		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print( "Enter\n 1-for publish\n 2-for update.\n Any digit for both part\n Your number:");

			number = Integer.valueOf(in.readLine());
			b=Character.isDigit(number);
		}catch(Exception ex){
			logger.info("Invalid Format! Try again");
		}


		if(number==1){
			partOne(dbWorker, newsService);
		}else if(number==2){
			updater(mysql, service, robber);
		}else{
		
			partOne(dbWorker, newsService);
			updater(mysql, service, robber);
		}*/
		
	partOne(dbWorker, newsService);
		updater(mysql, service, robber);
	}

	static void partOne(DbAccess dbWorker, NewsService newsService)
			throws InterruptedException, IOException, ParseException,
			SQLException {

		dbWorker.deleteTrash();


		List<String> streamUrlsList = NewsService.getStreamsUrls();

		for (int listHrefs = 0; listHrefs < streamUrlsList.size(); listHrefs++) {
			Thread.sleep(150);
			dateTodayForTranslationPublish = DateFormator.formatDate(new Date(), DateFormator.ShortDateFormat);
			yesterdayDateForTranslationPublish = DateFormator.formatDate(DateFormator.substractDaysFromToday(-1),
					DateFormator.ShortDateFormat);

			logger.info("Yesterday's date news "+ yesterdayDateForTranslationPublish);
			logger.info("Today date news "+ dateTodayForTranslationPublish);

			Document doc = NewsService.loadPage(streamUrlsList.get(listHrefs));
			Elements newsHref = doc.select("table[class=main]");
			Elements hrefs = newsHref.select("a[class=live]");
			currentDbTranslationList = (dbWorker.selectTranslationQuery());

			// href
			String href = "http://livetv.sx";
			for (Element hrefsNews : hrefs) {
				href += hrefsNews.attr("href");
				String newsHeader = newsService.getNewsDateForHeader(href)
						+ newsService.getNewsHeader(href);
				// logger.info( "Parser", "main", "Посилання" +
				// href);
				// logger.info( "Parser", "main",
				// "Заголовок:"+newsHeader);
				System.out.println(newsHeader);
				todayHrefAndTitle = newsService.todayTranslations(newsHeader,
						href, dateTodayForTranslationPublish);

				// перевірка БД на опублікованість і вставка
				for (int i = 0; i < currentDbTranslationList.size(); i++) {
					boolean isTopTeam = NewsService.hasTopTeamNameInString(newsHeader);
					
					if(isTopTeam){
						if (currentDbTranslationList.contains(newsHeader)) {
							break;
						} else if (i == currentDbTranslationList.size() - 1) {
							logger.info("Заголовок новини не співпав з новиною в БД, Публікую:"+ newsHeader);
							String postName = newsService.newsNameGenerator(newsHeader);
							logger.info( "PostName:"+ postName);
							dbWorker.insertTranslationQuery(newsService.postTextGenerator(newsHeader),
									newsHeader, postName);
							dbPostCounter++;
							postedHeaders.add(newsHeader);
						}
					}
				}
				// href
				href = "http://livetv.sx";
				logger.info( "------");
			}
			logger.info("Перевірка на опублікованість завершена ");
			long maxId = dbWorker.selectMaxID();
			// logger.info( "Parser", "main",
			// "Кількість опублікованих трансляцій "+ dbPostCounter);
			long idCounter = maxId - dbPostCounter + 1;
			// seo thumb term

			if (dbPostCounter > 0) {
				for (int i = 0; i < postedHeaders.size(); i++, idCounter++) {
					String metaValueThumbnail = newsService
							.getGuiMetaValue(postedHeaders.get(i));
					int termTaxonomyId = newsService.getTerm(postedHeaders
							.get(i));

					dbWorker.insertThumbnail(idCounter, metaValueThumbnail);
					dbWorker.insertTerm(idCounter, termTaxonomyId);
					dbWorker.insertSeo(idCounter, postedHeaders.get(i));
				}
			}
		}

		dbWorker.deleteAllNewsFromTop();
		int todayTaxonomy=54;
		dbWorker.deleteTaxonomy(todayTaxonomy);
		// публікація на головну TOP
		for (int i = 0; i < currentDbTranslationList.size(); i++) {
			int postedId = dbWorker.postedId(currentDbTranslationList.get(i));

			newsService.mainPagePublishCurrentDate(currentDbTranslationList.get(i),
					dateTodayForTranslationPublish, postedId, dbWorker);

		}
		// write hrefs to a file
		newsService.todayTranslationsWriter(todayHrefAndTitle);
		// delete yesterday's news
		yesterdayMainPageNews = dbWorker.getYesterdayNews(yesterdayDateForTranslationPublish);
		for (int i = 0; i < yesterdayMainPageNews.size(); i++) {
			dbWorker.deleteYesterdayNewsFromMainPage(yesterdayMainPageNews.get(i));

		}

		// remove all yesterday players
		dbWorker.deleteYesterdayPlayers(yesterdayDateForTranslationPublish);

		// remove the same news
		//newsService.deleteSameNewsMaker();
		logger.info(postedHeaders);
		logger.info(todayHrefAndTitle);
		logger.info(dateTodayForTranslationPublish);

	}

	static void updater(MysqlDAO mysql, Service service, StreamsRobber robber)
			throws IOException, SQLException, ParseException,
			InterruptedException {
		String newsHeader;

		todayTranslations = service.todayTranslationReader();

		for (int translationHref = todayTranslations.size() - 1; translationHref >= 0; translationHref--) {

			newsHeader = service.getNewsDateForHeader(todayTranslations.get(translationHref))+ service.getNewsHeader(todayTranslations.get(translationHref));
			logger.info("News Header " + newsHeader);
			browserHrefs = robber.getBrowserHhrefs(todayTranslations.get(translationHref));
			sopHrefs = robber.getSopHrefs(todayTranslations.get(translationHref));

			// take from DB old postContent of main translation
			String postContent = mysql.selectContentQuery(newsHeader);
			if (postContent != null) {

				logger.info("news content length=" + postContent.length());
				String oldPlayerContent = service.newContent(postContent);
				String oldSopContent = service.newSopContent(postContent);
				logger.info("news old Content length=" + oldPlayerContent.length());
				// String newContent = "";
				if (!browserHrefs.isEmpty()) {
					// players of translation
					String contentIframe = "";
					// for number of players in text
					int trueStreams = 0;
					logger.info("Amount of players length=" + browserHrefs.size());
					for (int frame = 0; frame < browserHrefs.size(); frame++) {
						System.out.println("cur"+browserHrefs.get(frame));
						// створюємо плеєр і вставляємо в базу даних..
						// get one iframe from livetv
						String postIframeContent = null;
						if (browserHrefs.get(frame).contains("livetv")) {
							System.out.println("browserHREF "+browserHrefs.get(frame));
							Thread.sleep(500);
							postIframeContent = robber.getPlayer(browserHrefs.get(frame));


						}else if(!browserHrefs.get(frame).contains("livetv")){
							System.out.println("Break");
							break;
						}


						if (!postIframeContent.equals("")) {
							trueStreams++;
							// get id of player translation
							int postedNewsId = mysql.selectTranslationQuery(newsHeader);
							// set title of one player, using random digits:
							String postTitleAdress = postedNewsId + "-"
									+ (int) (0 + Math.random() * 99)
									+ (int) (0 + Math.random() * 99);
							// name of the players
							logger.info( "Player name " + postTitleAdress);
							// insert player to DB
							// System.err.println(postIframeContent);
							//<!--noindex--><div id="ambn11106"></div><!--/noindex-->

							//String ads="";
							
							String adsBanner = "<script id=\"sofaAffiliateScript\" type=\"text/javascript\" src=\"http://www.sofascore.com/bundles/sofascoreweb/js/bin/util/affiliate.min.js\" data-custom=\"custom\" data-width=\"620\" data-height=\"350\"></script>";
							
							String adsUnderPlayer="<!-- MarketGidComposite Start --><div id=\"MarketGidScriptRootC601360\"> <div id=\"MarketGidPreloadC601360\">"
									+ "<a id=\"mg_add601360\" href=\"http://usr.marketgid.com/demo/celevie-posetiteli/\" target=\"_blank\"><img src=\"//cdn.marketgid.com/images/marketgid_add_link.png\" style=\"border:0px\"></a><br> "
									+ "<a href=\"http://marketgid.com/\" target=\"_blank\">Загрузка...</a> </div> <script> "
									+ "(function(){"
									+ "var D=new Date(),d=document,b='body',ce='createElement',ac='appendChild',st='style',ds='display',n='none',gi='getElementById';"
									+ "var i=d[ce]('iframe');i[st][ds]=n;d[gi](\"MarketGidScriptRootC601360\")[ac](i);try{var iw=i.contentWindow.document;iw.open();iw.writeln(\"<ht\"+\"ml><bo\"+\"dy></bo\"+\"dy></ht\"+\"ml>\");iw.close();var c=iw[b];}"
									+ "catch(e){var iw=d;var c=d[gi](\"MarketGidScriptRootC601360\");}var dv=iw[ce]('div');dv.id=\"MG_ID\";dv[st][ds]=n;dv.innerHTML=601360;c[ac](dv);"
									+ "var s=iw[ce]('script');s.async='async';s.defer='defer';s.charset='utf-8';s.src=\"//jsc.marketgid.com/f/o/footlivehd.com.601360.js?t=\"+D.getYear()+D.getMonth()+D.getDate()+D.getHours();c[ac](s);})();"
									+ "</script></div><!-- MarketGidComposite End -->"
									+ adsBanner;
		

							String advMixer="<div id=\"MIXADV_1532\" class=\"MIXADVERT_NET\"></div>\n" +
									"<script type=\"text/javascript\" src=\"https://s.mixadvert.com/show/?id=1532\" async></script>";
							
							
							
							String finishcontent = "<div id=\"stream\" style=\"width: 680px;\">"+"<br/>"+adsUnderPlayer+postIframeContent+"</div>"+advMixer;


							//String finishcontent = "<div style=\"width: 680px;2\">"+ads+"<br/>"+postIframeContent+adsUnderPlayer;
							postIframeContent=finishcontent;

							if (!postIframeContent.equals("-1")) {
								mysql.insertTranslationQuery(postIframeContent,
										postTitleAdress, postTitleAdress);
							}
							// переробить на max id і вставка
							int playerId = mysql
									.postedPlayerId(postTitleAdress);
							mysql.insertPlayerPageTerm(playerId);
							// add player to text of main translation
							contentIframe += "[button color=\"red\" size=\"medium\" link=\"http://www.matchttv.ru/"
									+ postTitleAdress
									+ "/\" target=\"blank\" ]"
									+ "ПЛЕЕР "
									+ trueStreams + "[/button]" ;
						}
					}
					// update text with players in postContent
					postContent = postContent.replace(oldPlayerContent,contentIframe);
					// end of iframe editing
				}
				
				String sopContent = "";
				
				logger.info("Amount of sopcasts " + sopHrefs.size());
				if (!sopHrefs.isEmpty()) {
					for (int sop = 0; sop < sopHrefs.size(); sop++) {
						sopContent += "[button color=\"white\" size=\"small\" link=\""
								+ sopHrefs.get(sop)
								+ "/\" target=\"blank\" ]"
								+ sopHrefs.get(sop) + "[/button]<br/>";
						System.out.println(sopContent);
					}
					
					postContent = postContent.replace(oldSopContent, sopContent);
				}
				// update postContent in DB..
				if (!(browserHrefs.isEmpty() && sopHrefs.isEmpty())) {
					mysql.updateContentQuery(newsHeader, postContent);
				}

			} // else!!! first program
		}

	}
}