import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MysqlDAO {

	private static Connection connection;
	
	
	private static final String URL = "jdbc:mysql://51.15.52.97:3306/matchttv?useUnicode=true&characterEncoding=UTF-8";
	private static final String USERNAME = "chuck";
	private static final String PASSWORD = "QQ123123qq";
	
	/*private static final String URL = "jdbc:mysql://51.15.53.49:3306/dbCom?useUnicode=true&characterEncoding=UTF-8";
	private static final String USERNAME = "chuck";
	private static final String PASSWORD = "QQ123123qq";*/
	

	private static String insertPlayerPage = "INSERT INTO wp_posts (post_author, post_date, post_date_gmt, post_content," +
			" post_excerpt,post_title, post_status, comment_status, ping_status,post_name,to_ping,pinged," +
			"post_modified, post_modified_gmt,post_content_filtered, guid,post_type)" +
			" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String selectIdTranslation="SELECT id FROM wp_posts WHERE post_title=? AND post_status='publish';";
	private static String selectContentTranslation="SELECT post_content FROM wp_posts " +
			"WHERE post_title=? AND post_status='publish';";
	private static String updateTranslation="UPDATE wp_posts SET post_content = ? " +
			"WHERE post_title=? AND post_status='publish';";
	final private static String TERM_POST_PLAYER = "INSERT into wp_term_relationships(object_id,term_taxonomy_id) " +
			"VALUES (?,?);";
	final private static String SELECT_POSTED_POST_ID = "SELECT id FROM wp_posts " +
			"WHERE post_title= ? AND post_status='publish';";
	private static Logger logger = Logger.getLogger(MysqlDAO.class.getName());

	private static ArrayList<Integer> ints = new ArrayList<Integer>();
	private static ArrayList <Integer> id=new ArrayList<Integer>();


	MysqlDAO() throws SQLException  {
		connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
	}


	public Connection getConnection() {
		return connection;
	}

	void insertTranslationQuery( String postContent,
								 String postTitle, String postName) throws ParseException, InterruptedException {

		try {
			String currentDate = DateFormator.currentDateDAO();
			String currentGmtDate = DateFormator.currentDateGrinvichTime();

			
			PreparedStatement preparedStatement = getConnection().prepareStatement(insertPlayerPage);
			preparedStatement.setInt(1, 2);
			preparedStatement.setString(2, currentDate);
			preparedStatement.setString(3, currentGmtDate);
			preparedStatement.setString(4, postContent);
			preparedStatement.setString(5, "footlivehd.com");
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

			logger.logp(Level.INFO, "PublishTranslation", "insertTranslationQuery", "Player was added into DB");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "PublishTranslation", "insertTranslationQuery", "Player was not added into DB");
			e.printStackTrace();
		}
	}

	int selectTranslationQuery(String title) throws SQLException {
		PreparedStatement preparedStatement = null;
		int postId = 0;
		preparedStatement = getConnection().prepareStatement(selectIdTranslation);
		preparedStatement.setString(1, title);
		ResultSet rs = preparedStatement.executeQuery();

		while (rs.next()) {
			postId = rs.getInt(1);
			ints.add(postId);
		}
		preparedStatement.execute();
		return (int) ints.get(0);
	}

	String selectContentQuery(String title) throws SQLException {
		PreparedStatement preparedStatement = null;
		preparedStatement = getConnection().prepareStatement(selectContentTranslation);
		preparedStatement.setString(1, title);
		ResultSet rs = preparedStatement.executeQuery();
		String content = null;
		while (rs.next()) {
			content = rs.getString("post_content");
		}
		preparedStatement.execute();
		return content;
	}

	void updateContentQuery(String title,String content) throws SQLException {
		PreparedStatement preparedStatement = null;
		preparedStatement = getConnection().prepareStatement(updateTranslation);
		preparedStatement.setString(1, content);
		preparedStatement.setString(2, title);
		preparedStatement.execute();
		logger.logp(Level.INFO, "PublishTranslation", "updateContentQuery", "Translation Content was updated sucessfully!");
	}

	void insertPlayerPageTerm(int objectId)throws ParseException {
		try {
			PreparedStatement preparedStatement = null;
			preparedStatement = getConnection().prepareStatement(TERM_POST_PLAYER);
			preparedStatement.setInt(1, objectId);
			preparedStatement.setInt(2, 53);
			preparedStatement.execute();
			logger.logp(Level.INFO, "PublishTranslation", "insertPlayerPageTerm", "Player wass added to term successfully");
		} catch (SQLException e) {
			logger.logp(Level.WARNING, "PublishTranslation", "insertPlayerPageTerm", "Player wass not added to term successfully");
			e.printStackTrace();
		}
	}

	int postedPlayerId(String newsTitle) throws SQLException {
		PreparedStatement preparedStatement = null;
		int postId = 0;
		preparedStatement = getConnection().prepareStatement(SELECT_POSTED_POST_ID);
		preparedStatement.setString(1, newsTitle);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			postId = rs.getInt(1);
			id.add(postId);
		}
		preparedStatement.execute();

		return postId;
	}
}