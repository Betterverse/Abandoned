package net.betterverse.kiwiadmin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public class Database
{
  public Database(Plugin plugin)
  {
    if (KiwiAdmin.useMysql) {
      Connection conn = SQLConnection.getSQLConnection();
      if (conn == null) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Could not establish SQL connection. Disabling KiwiAdmin");
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        return;
      }

      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
        ps = conn.prepareStatement("SELECT * FROM " + KiwiAdmin.mysqlTable);
        rs = ps.executeQuery();
        while (rs.next()) { String pName = rs.getString("name").toLowerCase();
          Timestamp pTime;
          try { pTime = rs.getTimestamp("temptime");
          }
          catch (SQLException ex)
          {
            pTime = new Timestamp(0L);
          }
          KiwiAdmin.bannedPlayers.add(pName.toLowerCase());
          if (pTime.getTime() != 0L) {
            KiwiAdmin.tempBans.put(rs.getString("name").toLowerCase(), Long.valueOf(pTime.getTime()));
          }
        }
        ps = conn.prepareStatement("SELECT * FROM " + KiwiAdmin.mysqlTableIp);
        rs = ps.executeQuery();
        while (rs.next()) {
          String ip = rs.getString("ip");
          System.out.println("Added " + ip);
          KiwiAdmin.bannedIPs.add(ip);
        }
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Couldn't execute MySQL statement: ", ex);
        try
        {
          if (ps != null)
            ps.close();
          if (conn != null)
            conn.close();
        } catch (SQLException exx) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", exx);
        }
      }
      finally
      {
        try
        {
          if (ps != null)
            ps.close();
          if (conn != null)
            conn.close();
        } catch (SQLException ex) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
        }
      }
      try
      {
        conn.close();
        KiwiAdmin.log.log(Level.INFO, "[KiwiAdmin] Initialized db connection");
      } catch (SQLException e) {
        e.printStackTrace();
        plugin.getServer().getPluginManager().disablePlugin(plugin);
      }

    }
    else
    {
      try
      {
        File banlist = new File("plugins/KiwiAdmin/banlist.txt");

        if (banlist.exists()) {
          BufferedReader in = new BufferedReader(new FileReader(banlist));
          String data = null;

          while ((data = in.readLine()) != null)
          {
            if ((data.startsWith("#")) || 
              (data.length() <= 0)) continue;
            String[] values = data.split(">>");
            String player = values[0];
            try {
              Timestamp pTime = Timestamp.valueOf(values[4]);

              KiwiAdmin.tempBans.put(player.toLowerCase(), Long.valueOf(pTime.getTime()));
            }
            catch (Exception localException)
            {
            }
            KiwiAdmin.bannedPlayers.add(player.toLowerCase());
          }

          in.close();
        }

        File banlistip = new File("plugins/KiwiAdmin/iplist.txt");

        if (banlistip.exists()) {
          BufferedReader in = new BufferedReader(new FileReader(banlistip));
          String data = null;

          while ((data = in.readLine()) != null)
          {
            if ((data.startsWith("#")) || 
              (data.length() <= 0)) continue;
            String[] values = data.split(">>");
            String ip = values[1];
            KiwiAdmin.bannedIPs.add(ip);
          }

          in.close();
        }

      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  public static boolean removeFromBanlist(String p)
  {
    if (!KiwiAdmin.bannedPlayers.contains(p.toLowerCase())) {
      return false;
    }
    if (KiwiAdmin.useMysql)
    {
      Connection conn = null;
      PreparedStatement ps = null;
      try {
        conn = SQLConnection.getSQLConnection();
        ps = conn.prepareStatement("DELETE FROM " + KiwiAdmin.mysqlTable + " WHERE name = ?");
        ps.setString(1, p.toLowerCase());
        ps.executeUpdate();
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Couldn't execute MySQL statement: ", ex);
        return false;
      } finally {
        try {
          if (ps != null)
            ps.close();
          if (conn != null)
            conn.close();
        } catch (SQLException ex) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
        }
      }
      try
      {
        if (ps != null)
          ps.close();
        if (conn == null)  return true;
					conn.close();
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
      }
    }
    else
    {
      try
      {
        String file = "plugins/KiwiAdmin/banlist.txt";
        File banlist = new File(file);

        File tempFile = new File(banlist.getAbsolutePath() + ".tmp");

        BufferedReader br = new BufferedReader(new FileReader(file));
        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

        String line = null;

        while ((line = br.readLine()) != null) {
          if ((!line.trim().toLowerCase().startsWith(p.toLowerCase())) && (line.length() > 0)) {
            pw.println(line);
            pw.flush();
          }
        }

        pw.close();
        br.close();

        banlist.delete();
        tempFile.renameTo(banlist);

        return true;
      }
      catch (FileNotFoundException ex)
      {
        ex.printStackTrace();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    label387: return true;
  }

  public void addPlayer(String p, String reason, String kicker)
  {
    addPlayer(p, reason, kicker, 0L);
  }

  public void addPlayer(String p, String reason, String kicker, long tempTime)
  {
    Date date = new Date();
    Timestamp time = new Timestamp(date.getTime());
    Timestamp temptime = new Timestamp(tempTime);

    if (KiwiAdmin.useMysql)
    {
      Connection conn = null;
      PreparedStatement ps = null;
      try {
        conn = SQLConnection.getSQLConnection();
        if (tempTime > 0L) {
          ps = conn.prepareStatement("INSERT INTO " + KiwiAdmin.mysqlTable + " (name,reason,admin,time,temptime) VALUES(?,?,?,?,?)");
          ps.setTimestamp(5, temptime);
        } else {
          ps = conn.prepareStatement("INSERT INTO " + KiwiAdmin.mysqlTable + " (name,reason,admin,time) VALUES(?,?,?,?)");
        }ps.setString(1, p);
        ps.setString(2, reason);
        ps.setString(3, kicker);
        ps.setTimestamp(4, time);
        ps.executeUpdate();
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Couldn't execute MySQL statement: ", ex);
        try
        {
          if (ps != null)
            ps.close();
          if (conn == null) return;
					conn.close();
        } catch (SQLException exx) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", exx);
        }
      }
      finally
      {
        try
        {
          if (ps != null)
            ps.close();
          if (conn != null)
            conn.close();
        } catch (SQLException ex) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
        }
      }
      try
      {
        if (ps != null)
          ps.close();
        if (conn == null) return; conn.close();
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
      }
    }
    else
    {
      String temptimeStr;
      if (tempTime <= 0L)
        temptimeStr = "0";
      else
        temptimeStr = temptime.toString();
      try
      {
        BufferedWriter banlist = new BufferedWriter(new FileWriter("plugins/KiwiAdmin/banlist.txt", true));
        banlist.newLine();
        banlist.write(p + ">>" + reason + ">>" + kicker + ">>" + time + ">>" + temptimeStr);
        banlist.close();
      }
      catch (IOException e)
      {
        KiwiAdmin.log.log(Level.SEVERE, "KiwiAdmin: Couldn't write to banlist.txt");
      }
    }
  }

  public static String getReason(String p)
  {
    if (KiwiAdmin.useMysql) {
      Connection conn = SQLConnection.getSQLConnection();
      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
        ps = conn.prepareStatement("SELECT * FROM " + KiwiAdmin.mysqlTable + " WHERE name = ?");
        ps.setString(1, p);
        rs = ps.executeQuery();
        if (rs.next()) {
          String reason = rs.getString("reason");
          String str1 = reason;
          return str1;
        }
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Couldn't execute MySQL statement: ", ex);
      } finally {
        try {
          if (ps != null)
            ps.close();
          if (conn != null)
            conn.close();
        } catch (SQLException ex) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
        }
      }
      try
      {
        if (ps != null)
          ps.close();
        if (conn != null)
          conn.close();
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
      }

      return null;
    }
    try {
      BufferedReader in = new BufferedReader(new FileReader("plugins/KiwiAdmin/banlist.txt"));
      String data = null;

      while ((data = in.readLine()) != null)
      {
        if ((data.startsWith("#")) || 
          (!data.trim().toLowerCase().startsWith(p.toLowerCase())) || (data.length() <= 0))
          continue;
        String[] values = data.split(">>");

        return values[1];
      }

      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public void addAddress(String p, InetSocketAddress address)
  {
    String ip = address.getAddress().getHostAddress();

    if (KiwiAdmin.useMysql)
    {
      Connection conn = null;
      PreparedStatement ps = null;
      try {
        conn = SQLConnection.getSQLConnection();
        ps = conn.prepareStatement("INSERT INTO " + KiwiAdmin.mysqlTableIp + " (name,ip) VALUES(?,?)");
        ps.setString(1, p);
        ps.setString(2, ip);
        ps.executeUpdate();
      } catch (SQLException ex) {
        KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Couldn't execute MySQL statement: ", ex);
        try
        {
          if (ps != null)
            ps.close();
          if (conn == null) return; conn.close();
        } catch (SQLException exx) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", exx);
        }
      }
      finally
      {
        try
        {
          if (ps != null)
            ps.close();
          if (conn != null)
            conn.close();
        } catch (SQLException ex) {
          KiwiAdmin.log.log(Level.SEVERE, "[KiwiAdmin] Failed to close MySQL connection: ", ex);
        }
      }
    }
    else
    {
      try {
        BufferedWriter banlist = new BufferedWriter(new FileWriter("plugins/KiwiAdmin/iplist.txt", true));
        banlist.newLine();
        banlist.write(p + ">>" + ip);
        banlist.close();
      }
      catch (IOException e)
      {
        KiwiAdmin.log.log(Level.SEVERE, "KiwiAdmin: Couldn't write to iplist.txt");
      }
    }
  }

  public void exportBans()
  {
    try {
      BufferedWriter banlist = new BufferedWriter(new FileWriter("banned-players.txt", true));
      for (String player : KiwiAdmin.bannedPlayers) {
        banlist.newLine();
        banlist.write(player);
      }
      banlist.close();
    }
    catch (IOException e)
    {
      KiwiAdmin.log.log(Level.SEVERE, "KiwiAdmin: Couldn't write to iplist.txt");
    }
  }
}