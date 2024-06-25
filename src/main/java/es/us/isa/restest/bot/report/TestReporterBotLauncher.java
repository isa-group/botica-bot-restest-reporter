package es.us.isa.restest.bot.report;

import es.us.isa.botica.bot.BotApplicationRunner;

public class TestReporterBotLauncher {
  public static void main(String[] args){
    BotApplicationRunner.run(new TestReporterBot(), args);
  }
}
