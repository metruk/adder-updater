
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbAccess {

	private static Connection connection;
	
	/*private static final String URL = "jdbc:mysql://51.15.78.0:3306/matchtw?useUnicode=true&characterEncoding=UTF-8";
	private static final String USERNAME = "chuck";
	private static final String PASSWORD = "QQ123123qq";*/
	private static final String URL = "jdbc:mysql://51.15.52.97:3306/matchttv?useUnicode=true&characterEncoding=UTF-8";
	private static final String USERNAME = "chuck";
	private static final String PASSWORD = "QQ123123qq";
	/*private static final String URL = "jdbc:mysql://51.15.53.49:3306/dbCom?useUnicode=true&characterEncoding=UTF-8";
	private static final String USERNAME = "chuck";
	private static final String PASSWORD = "QQ123123qq";*/

	final private static String INSERT_TRANSLATION_WITHOUT_MINIATURE = "INSERT INTO wp_posts (post_author, post_date, post_date_gmt, post_content,"
			+ " post_excerpt,post_title, post_status, comment_status, ping_status,post_name,to_ping,pinged,"
			+ "post_modified, post_modified_gmt,post_content_filtered, guid,post_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	final private static String THUMBNAIL_POST = "INSERT into wp_postmeta(post_id,meta_key,meta_value) VALUES (?,?,?);";
	final private static String TERM_POST = "INSERT into wp_term_relationships(object_id,term_taxonomy_id) VALUES (?,?);";
	final private static String EASY_SEO_HEADER_MAKER = "INSERT into wp_postmeta(post_id,meta_key,meta_value) VALUES (?,?,?);";
	final private static String SELECT_POSTED_POST_ID = "SELECT id FROM wp_posts WHERE post_title= ? AND post_status='publish'";
	static Logger logger = Logger.getLogger(DbAccess.class.getName());


	DbAccess() {
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			String query="SELECT ID , post_content FROM wp_posts WHERE post_mime_type = 'image/jpeg' and post_content !=\"\"";
			mapXmlWriter(query,"guids.xml","guids","post","anyValue","postTitle","id");

			String query1="SELECT term_id, name FROM wp_terms";
			mapXmlWriter(query1, "terms.xml","terms","term","value","termTitle","id");

		} catch (SQLException ex) {
			ex.printStackTrace();
			logger.logp(Level.INFO, "DbAccess", "DbWorker()", "З'єднання з БД встановлено");
		}
	}

	public Connection getConnection() {
		return connection;
	}

	void insertTranslationQuery( String postContent,
								 String postTitle, String postName) throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			String currentDate = DateFormator.currentDateDAO();
			String currentGmtDate = DateFormator.currentDateGrinvichTime();
			preparedStatement = getConnection().prepareStatement(INSERT_TRANSLATION_WITHOUT_MINIATURE);
			preparedStatement.setInt(1, 2);
			preparedStatement.setString(2, currentDate);
			preparedStatement.setString(3, currentGmtDate);
			preparedStatement.setString(4, postContent);
			preparedStatement.setString(5, "");
			preparedStatement.setString(6, postTitle);
			preparedStatement.setString(7, "publish");
			preparedStatement.setString(8, "open");
			preparedStatement.setString(9, "open");
			preparedStatement.setString(10, postName);
			preparedStatement.setString(11, "");
			preparedStatement.setString(12, "");
			preparedStatement.setString(13, currentDate);
			preparedStatement.setString(14, currentGmtDate);
			preparedStatement.setString(15, "");
			preparedStatement.setString(16, "");
			preparedStatement.setString(17, "post");
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertTranslationQuery", "Трансляцію додано");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "insertTranslationQuery", "Не вдалось вставити дані в БД");
			e.printStackTrace();
		}
	}

	List<String> selectTranslationQuery() {
		long checkedTranslation=selectMaxID()-500;
		if(checkedTranslation < 0){
			checkedTranslation = 1;
		}

		String SELECT_CURRENT_BD_TRANSLATIONS = "SELECT id,post_title FROM wp_posts WHERE ID>"+checkedTranslation +" AND post_title LIKE '%Трансляция матча%' AND post_status='publish';";
		ArrayList<String> list = new ArrayList<String>();

		String title = null;
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(SELECT_CURRENT_BD_TRANSLATIONS);
			ResultSet set = preparedStatement.executeQuery();
			while (set.next()) {
				title = set.getString("post_title");
				list.add(title);
			}
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "selectTranslationQuery", "Вибірка завершена");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "selectTranslationQuery", "Не вдалось вибрати дані з БД");
			System.out.println();
			e.printStackTrace();
		}
		return list;
	}

	long selectMaxID() {
		String query="SELECT MAX(ID) FROM wp_posts ";
		int maxId = 0;
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(query);
			ResultSet set = preparedStatement.executeQuery();
			while (set.next()) {
				maxId = set.getInt(1);
			}
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "selectMaxID", "Максимальне id вибрано");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "selectMaxID", "не вдалось вибрати максимальне id");
			e.printStackTrace();
		}
		return maxId;
	}

	void insertThumbnail(long idCounter, String metaValue)
			throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(THUMBNAIL_POST);
			preparedStatement.setFloat(1, idCounter);
			preparedStatement.setString(2, "_thumbnail_id");
			preparedStatement.setString(3, metaValue);
			preparedStatement.execute();
			System.out.println();
			logger.logp(Level.INFO, "DbAccess", "insertThumbnail","Мініатюру додано");
		} catch (SQLException e) {
			logger.logp(Level.INFO, "DbAccess", "insertThumbnail","Мініатюру не додано");
			e.printStackTrace();
		}
	}

	void insertTerm(long idCounter, int termTaxonomyId)throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(TERM_POST);
			preparedStatement.setFloat(1, idCounter);
			preparedStatement.setInt(2, termTaxonomyId);
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertTerml","рубрику додано");

		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "insertTerml","рубрику не додано");
			e.printStackTrace();
		}
	}
	void insertMainMainTerm(int objectId)
			throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(TERM_POST);
			preparedStatement.setInt(1, objectId);
			preparedStatement.setInt(2, 22);
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertMainPageTerm","Додано на головну сторінку");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "insertMainPageTerm","не додано на головну сторінку");
			e.printStackTrace();
		}
	}
	void insertMainPageTerm(int objectId)
			throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(TERM_POST);
			preparedStatement.setInt(1, objectId);
			preparedStatement.setInt(2, 54);
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertMainPageTerm","Додано на головну сторінку");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "insertMainPageTerm","не додано на головну сторінку");
			e.printStackTrace();
		}
	}


	void insertTop(int objectId)throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(TERM_POST);
			preparedStatement.setInt(1, objectId);
			preparedStatement.setInt(2, 52);
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertTop","Додано в ТОП");
		} catch (SQLException e) {
			System.out.println();
			logger.logp(Level.WARNING, "штіукеЕшщ", "insertTop","В топ не додано");
			e.printStackTrace();
		}

	}

	void insertTerm(int objectId,int termId)throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(TERM_POST);
			preparedStatement.setInt(1, objectId);
			preparedStatement.setInt(2, termId);
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertTop","Додано в ТОП");
		} catch (SQLException e) {
			System.out.println();
			logger.logp(Level.WARNING, "штіукеЕшщ", "insertTop","В топ не додано");
			e.printStackTrace();
		}

	}

	// SEO
	void insertSeo(long idCounter, String newsTitle)
			throws ParseException {
		try {
			PreparedStatement preparedFocusKeyword = null;
			preparedFocusKeyword = getConnection().prepareStatement(EASY_SEO_HEADER_MAKER);
			preparedFocusKeyword.setFloat(1, idCounter);
			preparedFocusKeyword.setString(2, "_yoast_wpseo_title");
			preparedFocusKeyword.setString(3, newsTitle + "-footlivehd.com");
			preparedFocusKeyword.execute();
			logger.logp(Level.INFO, "DbAccess", "insertSeo","Сео заголовок додано");
			preparedFocusKeyword = getConnection().prepareStatement(EASY_SEO_HEADER_MAKER);
			preparedFocusKeyword.setFloat(1, idCounter);
			preparedFocusKeyword.setString(2, "_yoast_wpseo_metadesc");
			preparedFocusKeyword.setString(3, newsTitle);
			preparedFocusKeyword.execute();
			logger.logp(Level.INFO, "DbAccess", "insertSeo","Сео мету додано");
			preparedFocusKeyword = getConnection().prepareStatement(EASY_SEO_HEADER_MAKER);
			preparedFocusKeyword.setFloat(1, idCounter);
			preparedFocusKeyword.setString(2, "_yoast_wpseo_focuskw");
			preparedFocusKeyword.setString(3,
					newsTitle.replaceAll("[0-9].+[0-9]", ""));
			preparedFocusKeyword.execute();
			logger.logp(Level.INFO, "DbAccess", "insertSeo","Сео ключові слова додано");
		} catch (SQLException e) {
			System.out.println();
			logger.logp(Level.WARNING, "DbAccess", "insertSeo","Сео не зроблено.");
			e.printStackTrace();
		}
	}

	int postedId(String newsTitle) throws SQLException {
		PreparedStatement preparedStatement = null;
		int min=0;
		int postId = 0;
		preparedStatement = getConnection().prepareStatement(SELECT_POSTED_POST_ID);
		preparedStatement.setString(1, newsTitle);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			postId = rs.getInt(1);
			min=postId;
		}
		preparedStatement.execute();
		return min;
	}

	List<Integer> getYesterdayNews(String yesterdayDate) throws SQLException {
		ArrayList<Integer> yesterdayPosts = new ArrayList<Integer>();
		final String yesterayPostId = "SELECT ID FROM wp_posts WHERE post_title LIKE '"+yesterdayDate+"%'";

		PreparedStatement preparedStatement = null;
		int id=0;
		int postId = 0;
		preparedStatement = getConnection().prepareStatement(yesterayPostId);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			postId = rs.getInt(1);
			id=postId;
			yesterdayPosts.add(id);
		}
		preparedStatement.execute();
		return yesterdayPosts;
	}

	List<Integer> getYesterdayPlayers() throws SQLException {
		ArrayList<Integer> yesterdayPosts = new ArrayList<Integer>();
		final String yesterayPostId = "SELECT ID FROM wp_posts WHERE post_title REGEXP '^[0-9]+-[0-9]+$'";
		PreparedStatement preparedStatement = null;
		int id=0;
		int postId = 0;
		preparedStatement = getConnection().prepareStatement(yesterayPostId);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			postId = rs.getInt(1);
			id=postId;
			yesterdayPosts.add(id);
		}
		preparedStatement.execute();
		return yesterdayPosts;
	}

	void deleteYesterdayPlayers(String yesterdayDate) throws SQLException {

		final String yesterayDeletePlayer = "delete FROM wp_posts WHERE post_title REGEXP '^[0-9]+-[0-9]+$' and date_format( now() , '%j%y' ) > date_format(post_modified , '%j%y' )  ";
		PreparedStatement preparedStatement = null;
		preparedStatement = getConnection().prepareStatement(yesterayDeletePlayer);
		preparedStatement.executeUpdate();
		logger.logp(Level.INFO, "DbAccess", "deleteYesterdayPlayers"," Yesrerday players were deleted");
	}

	void deleteYesterdayNewsFromMainPage(int id) throws SQLException {
		PreparedStatement preparedStatement = null;
		//top 52 - main page id
		String deleteSQL = "DELETE FROM wp_term_relationships WHERE object_id = ? AND term_taxonomy_id=22";
		preparedStatement = getConnection().prepareStatement(deleteSQL);
		preparedStatement.setInt(1, id);
		preparedStatement.execute();
		logger.logp(Level.INFO, "DbAccess", "deleteYesterdayNewsFromMainPage"," Yesrerday record was deleted from main page");
	}

	void deleteAllNewsFromTop() throws SQLException {
		PreparedStatement preparedStatement = null;
		//top 52 - main page id
		String deleteSQL = "DELETE FROM wp_term_relationships WHERE term_taxonomy_id=52";
		preparedStatement = getConnection().prepareStatement(deleteSQL);
		preparedStatement.execute();
		logger.logp(Level.INFO, "DbAccess", "deleteYesterdayNewsFromMainPage"," Yesrerday record was deleted from main page");
	}

	void deleteTaxonomy(int taxonomy) throws SQLException {
		PreparedStatement preparedStatement = null;
		//top 52 - main page id
		String deleteSQL = "DELETE FROM wp_term_relationships WHERE term_taxonomy_id="+taxonomy;
		preparedStatement = getConnection().prepareStatement(deleteSQL);
		preparedStatement.execute();
		logger.logp(Level.INFO, "DbAccess", "deleteYesterdayNewsFromMainPage"," Yesrerday record was deleted from main page");
	}


	//same titles method block
	List<String> sameTitlesGetContent(String title) throws SQLException {
		ArrayList<String> titles = new ArrayList<String>();
		final String yesterayPostId = "SELECT post_content FROM wp_posts WHERE post_title REGEXP '([0-9]+.[0-9]+,[0-9]+-[0-9]+)( .+)' AND post_status='publish'";
		PreparedStatement preparedStatement = null;
		preparedStatement = getConnection().prepareStatement(yesterayPostId);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			String postTitle= rs.getString(2);
			titles.add(postTitle);
		}
		preparedStatement.execute();
		return titles;
	}

	List<String> sameTitlesFinder() throws SQLException {
		ArrayList<String> titles = new ArrayList<String>();
		final String yesterayPostId = "SELECT id, post_title ,post_date_gmt FROM wp_posts WHERE post_title REGEXP '([0-9]+.[0-9]+,[0-9]+-[0-9]+)( .+)' AND post_status='publish'";
		PreparedStatement preparedStatement = null;
		String postTitle;
		preparedStatement = getConnection().prepareStatement(yesterayPostId);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			postTitle= rs.getString(2);
			titles.add(postTitle);
		}
		preparedStatement.execute();
		return titles;
	}

	void sameTitlesContent(String postTitle, LinkedHashMap<String,Integer> newsPost) throws SQLException {
		final String yesterayPostId = "SELECT post_content, post_title, id, post_date FROM wp_posts WHERE post_title REGEXP '([0-9]+.[0-9]+,[0-9]+-[0-9]+)"+postTitle+"' AND post_status='publish'";
		PreparedStatement preparedStatement = null;
		String postTitleNews = null;
		int id;
		preparedStatement = getConnection().prepareStatement(yesterayPostId);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			postTitleNews= rs.getString(2);
			id= rs.getInt(3);
			newsPost.put(postTitleNews, id);
		}
		preparedStatement.execute();
		//return postContentNews.length();
	}

	String sameTitleSelector(String postTitle) throws SQLException {

		final String query = "SELECT id, post_date  FROM wp_posts WHERE post_title REGEXP '([0-9]+.[0-9]+,[0-9]+-[0-9]+)"+postTitle+"' AND post_status='publish'";
		PreparedStatement preparedStatement = null;
		String postDate = null;
		preparedStatement = getConnection().prepareStatement(query);
		ResultSet rs = preparedStatement.executeQuery();
		ArrayList<String> list=new ArrayList<String>();
		while (rs.next()) {
			postDate= rs.getString(2);
			list.add(postDate);
		}
		preparedStatement.execute();
		return postDate;
	}

	void deleteSameNews(int id) throws SQLException {
		PreparedStatement preparedStatement = null;
		//top 52 - main page id
		String deleteSQL = "DELETE FROM wp_posts WHERE id = ?";
		preparedStatement = getConnection().prepareStatement(deleteSQL);
		preparedStatement.setInt(1, id);
		preparedStatement.execute();
		logger.logp(Level.INFO, "DbAccess", "deleteYSameNews"," Same news where deleted");
	}

	void insertAllTop(DbAccess dbWorker, int objectId)
			throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = dbWorker.getConnection().prepareStatement(TERM_POST);
			preparedStatement.setInt(1, objectId);
			preparedStatement.setInt(2, 54);
			preparedStatement.execute();
			logger.logp(Level.INFO, "DbAccess", "insertAllTop","Додано в ТОП УСІ");
		} catch (SQLException e) {
			System.out.println();
			logger.logp(Level.WARNING, "DbAccess", "insertTop","В топ УСІ не додано");
			e.printStackTrace();
		}
	}

	void deleteTrash() throws SQLException {
		PreparedStatement preparedStatement = null;
		String deleteSQL = "DELETE FROM wp_posts WHERE post_status = 'trash' AND post_title LIKE '%Трансляция%' ";
		preparedStatement = getConnection().prepareStatement(deleteSQL);
		preparedStatement.execute();
		logger.logp(Level.INFO, "DbAccess", "deleteYSameNews"," Same news where deleted");
	}


	void mapXmlWriter(String query,String xmlName,String root,String element,String elementValue,String objectField1,String objectField2) {
		Integer id = 0;
		String title=null;
		XmlWorker xml = new XmlWorker();
		Map<String,String> guids = new HashMap<String,String>();

		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(query);
			ResultSet set = preparedStatement.executeQuery();
			while (set.next()) {
				id = set.getInt(1);
				title=set.getString(2);
				System.out.println(id);
				String ID=id.toString();
				System.out.println(title);
				System.out.println(ID);
				guids.put(ID, title);


			}
			preparedStatement.execute();
			xml.createXml(guids, xmlName,root,element,elementValue,objectField1,objectField2);

			logger.logp(Level.INFO, "DbAccess", "selectMaxID", "Максимальне id вибрано");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "DbAccess", "selectMaxID", "не вдалось вибрати максимальне id");
			e.printStackTrace();
		}

	}

	public static void main(String[]args) throws SQLException{
		DbAccess db = new DbAccess();
		db.deleteYesterdayPlayers("29051-157");

	}

}