package secondTd.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
  Dotenv dotenv = Dotenv.load();
  private final String URL = dotenv.get("DB_URL");
  private final String USER = dotenv.get("DB_USER");
  private final String PASSWORD = dotenv.get("DB_PASSWORD");

  public DBConnection() {}

  public String getURL() {
    return URL;
  }

  public String getUSER() {
    return USER;
  }

  public String getPASSWORD() {
    return PASSWORD;
  }

  public Connection getConnection() throws SQLException {
      if (URL == null || USER == null || PASSWORD == null) {
        throw new IllegalArgumentException("Url, Username and password are mandatory");
      }
      return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  public void closeJDBCRessources(AutoCloseable... ressources) {
    for (AutoCloseable ressource : ressources) {
      if (ressource != null) {
        try {
          ressource.close();
        } catch (Exception e) {
          throw new RuntimeException("Error while closing resource: " + e);
        }
      }
    }
  }

}
