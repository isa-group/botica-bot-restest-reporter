package es.us.isa.restest.bot.report;

import es.us.isa.botica.bot.BaseBot;
import es.us.isa.botica.bot.OrderHandler;
import es.us.isa.restest.reporting.AllureReportManager;
import es.us.isa.restest.reporting.StatsReportManager;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import org.json.JSONObject;

/**
 * Generates the convenient reports for an incoming batch of test execution results.
 *
 * @author Alberto Mimbrero
 */
public class TestReporterBot extends BaseBot {

  @OrderHandler("generate_reports")
  public void generateReports(JSONObject message) {
    String batchId = message.getString("batchId");
    String userConfigPath = message.getString("userConfigPath");

    RESTestLoader loader = new RESTestLoader(userConfigPath);
    Collection<TestCase> testCases = readTestCases(new File(loader.getTargetDirJava(), batchId));

    AllureReportManager allureReportManager = loader.createAllureReportManager();
    allureReportManager.generateReport();

    StatsReportManager statsReportManager = loader.createStatsReportManager();
    statsReportManager.setTestCases(testCases);
    statsReportManager.generateReport(batchId, true);

    publishOrder(
        "oracle_identifier_bots",
        "analyze_invariants",
        message.put("batchSize", testCases.size()).toString());
  }

  @SuppressWarnings("unchecked")
  private static Collection<TestCase> readTestCases(File file) {
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      try (ObjectInputStream in = new ObjectInputStream(fileInputStream)) {
        return (Collection<TestCase>) in.readObject();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
