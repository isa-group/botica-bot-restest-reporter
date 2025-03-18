package es.us.isa.restest.bot.report;

import es.us.isa.botica.bot.BotLauncher;

public class TestReporterBotLauncher {
  public static void main(String[] args) {
    BotLauncher.run(new TestReporterBot(), args);
  }
}
