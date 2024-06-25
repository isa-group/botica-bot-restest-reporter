package es.us.isa.restest.bot.report;

import es.us.isa.botica.bot.AbstractBotApplication;
import es.us.isa.restest.reporting.StatsReportManager;
import es.us.isa.restest.runners.BoticaRESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;
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
public class TestReporterBot extends AbstractBotApplication {
  @Override
  public void onOrderReceived(String raw) {
    JSONObject message = new JSONObject(raw);
    String batchId = message.getString("batchId");
    String userConfigPath = message.getString("userConfigPath");

    BoticaRESTestLoader loader = new BoticaRESTestLoader(userConfigPath);
    Collection<TestCase> testCases = readTestCases(new File(loader.getTargetDirJava(), batchId));

    /* At this moment, reporting does not work incrementally, so we need to treat every batch
     * as a different experiment. StatsReportManager#generateReport iterates over the test execution
     * results, including those from previous batches, and tries to find its associated test case by
     * its test ID among the test cases of the current batch, resulting in an exception.
     */
    loader.setExperimentName(loader.getExperimentName().concat("-").concat(batchId));

    try {
      loader.createGenerator(); // loads RESTestLoader#spec, necessary to create the stats report
      // manager
      StatsReportManager statsReportManager = loader.createStatsReportManager();
      statsReportManager.setTestCases(testCases);

      loader.createAllureReportManager().generateReport();
      statsReportManager.generateReport(loader.getExperimentName(), true);
    } catch (RESTestException e) {
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
