package es.us.isa.restest.bot.report;

import es.us.isa.botica.bot.AbstractBotApplication;
import es.us.isa.restest.reporting.AllureReportManager;
import es.us.isa.restest.reporting.StatsReportManager;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import org.json.JSONObject;

/**
 * Generates the convenient reports for an incoming batch of test execution results.
 *
 * @author Alberto Mimbrero
 */
public class TestReporterBot extends AbstractBotApplication {
  @Override
  public void onOrderReceived(String raw) {
    this.logReceivedOrder(raw);

    JSONObject message = new JSONObject(raw);
    String batchId = message.getString("batchId");
    String userConfigPath = message.getString("userConfigPath");

    RESTestLoader loader = new RESTestLoader(userConfigPath);
    Collection<TestCase> testCases = readTestCases(new File(loader.getTargetDirJava(), batchId));

    AllureReportManager allureReportManager = loader.createAllureReportManager();
    allureReportManager.generateReport();

    StatsReportManager statsReportManager = loader.createStatsReportManager();
    statsReportManager.setTestCases(testCases);
    statsReportManager.generateReport(batchId, true);
  }

  private void logReceivedOrder(String message) {
    this.logEvaluation(message, "received.txt");
  }

  private void logEvaluation(String message, String fileName) {
    try {
      String directory = String.format("/app/target/evaluation/%s/", getBotId());
      Files.createDirectories(Path.of(directory));
      Files.writeString(
          Path.of(directory, fileName),
          message + "\n",
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
