package br.com.charlesalves.batchcompilation.tasklets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import br.com.charlesalves.batchcompilation.dao.BachDataDao;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/insert-data.sql")
@Sql(scripts = "/clean-up-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class ExportFileTaskletTest {

	@Autowired
	private ExportFileTasklet exportFileTasklet;

	@Autowired
	private BachDataDao bachDataDao;

	@Value("${file.testDir}")
	private String testDir;

	@Value("${file.output-path}")
	private String outputPath;

	@Value("${file.csv-separator}")
	private String separator;

	private File outputFile;

	@BeforeEach
	public void setup() throws URISyntaxException, IOException {
		outputFile = new File(outputPath);
	}

	@AfterEach
	public void teardown() throws IOException {
		if (outputFile.exists()) {
			outputFile.delete();
		}

		File testDirFile = new File(testDir);
		FileUtils.deleteDirectory(testDirFile);
	}

	@Test
	public void testExportData() throws Exception {
		RepeatStatus status = exportFileTasklet.execute(null, null);

		assertThat(status, is(equalTo(RepeatStatus.FINISHED)));
		assertThat(outputFile.exists(), is(true));

		String fileData = getFileData();
		assertThat(fileData, is(equalTo(getTestData())));
	}

	private String getFileData() throws IOException {
		return Files.readAllLines(Paths.get(outputPath)).stream()
			.findFirst()
			.orElse("");
	}

	private String getTestData() {
		return new StringBuilder()
			.append(4)
			.append(separator)
			.append(4)
			.append(separator)
			.append(2)
			.append(separator)
			.append("Margarida")
			.toString();
	}

	@Test
	public void testExportEmptyDatabase() throws Exception {
		bachDataDao.deleteAll();

		RepeatStatus status = exportFileTasklet.execute(null, null);

		assertThat(status, is(equalTo(RepeatStatus.FINISHED)));
		assertThat(outputFile.exists(), is(false));
	}
}
