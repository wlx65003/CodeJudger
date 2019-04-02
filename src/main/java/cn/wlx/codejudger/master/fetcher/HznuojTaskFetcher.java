package cn.wlx.codejudger.master.fetcher;

import cn.wlx.codejudger.common.entities.Task;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class HznuojTaskFetcher implements ITaskFetcher {

  private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  private String DB_URL = "jdbc:mysql://localhost:3306/jol";

  static final String USER = "root";
  static final String PASS = "root";

  @Override
  public List<Task> fetch() {
    // register JDBC driver
    try {
      Class.forName(JDBC_DRIVER);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
      while (true) {
        Statement stmt = conn.createStatement();
        String sql = "SELECT printer_code.id, code, team.user_id, team.nick FROM printer_code "
            + "LEFT JOIN team ON printer_code.contest_id=team.contest_id AND printer_code.user_id=team.user_id"
            + " WHERE status=0";
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
          int id = rs.getInt("id");
          String code = rs.getString("code");
          String user_id = rs.getString("user_id");
          String nick = rs.getString("nick");
          System.out.println(String.format("got code, user_id=%s, nick=%s, id=%d",
              user_id, nick, id));
        }
        rs.close();
        stmt.close();
        Thread.sleep(3000);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
