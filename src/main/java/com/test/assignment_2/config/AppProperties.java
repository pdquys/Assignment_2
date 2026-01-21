package com.test.assignment_2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private Inventory inventory = new Inventory();
  private Tracking tracking = new Tracking();
  private Admin admin = new Admin();

  @Getter @Setter
  public static class Inventory {
    private int defaultHoldMinutes = 15;
  }

  @Getter @Setter
  public static class Tracking {
    private String baseUrl;
  }

  @Getter @Setter
  public static class Admin {
    private Basic basic = new Basic();
    @Getter @Setter
    public static class Basic {
      private String user;
      private String password;
      private String staffUser;
      private String staffPassword;
    }
  }
}
