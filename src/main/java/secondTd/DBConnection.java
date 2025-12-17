package secondTd;

import io.github.cdimascio.dotenv.Dotenv;

public class DBConnection {
  private final String URL;
  private final String USER;
  private final String PASSWORD;

  public DBConnection() {
    Dotenv dotenv = Dotenv.load();
    URL = dotenv.get("DB_URL");
    USER = dotenv.get("DB_USER");
    PASSWORD = dotenv.get("DB_PASSWORD");
  }
}
