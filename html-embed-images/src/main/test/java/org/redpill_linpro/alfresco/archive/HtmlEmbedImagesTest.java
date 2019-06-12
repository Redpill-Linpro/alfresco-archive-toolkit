package org.redpill_linpro.alfresco.archive;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class HtmlEmbedImagesTest {

  @Test
  public void testMainNoArgs() {
    HtmlEmbedImages.main(null);
    HtmlEmbedImages.main(new String[]{});
  }

  @Test
  public void testMainWithFileNotExist() throws IOException {
    Path tempDirectory = Files.createTempDirectory("html");

    try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/simple.html")) {
      assertNotNull(resourceAsStream);

      File targetFile = new File(tempDirectory.toFile(), "simple.html");
      Files.copy(resourceAsStream, targetFile.toPath());

      String absPath = tempDirectory.toAbsolutePath() + "simple.html";
      HtmlEmbedImages.main(new String[]{absPath});
    }
  }

  @Test
  public void testMainWithFileImg() throws IOException {
    Path tempDirectory = Files.createTempDirectory("html");
    try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/simple.html")) {
      assertNotNull(resourceAsStream);

      File targetFile = new File(tempDirectory.toFile(), "simple.html");
      Files.copy(resourceAsStream, targetFile.toPath());

      String absPath = tempDirectory.toAbsolutePath() + "/simple.html";
      HtmlEmbedImages.main(new String[]{absPath});
    }
  }

  @Test
  public void testWithFileNotExist() throws IOException {
    Path tempDirectory = Files.createTempDirectory("html");
    try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/simple.html")) {
      assertNotNull(resourceAsStream);

      File targetFile = new File(tempDirectory.toFile(), "simple.html");
      Files.copy(resourceAsStream, targetFile.toPath());

      String absPath = tempDirectory.toAbsolutePath() + "simple.html";
      try {
        new HtmlEmbedImages(absPath, true);
        assertFalse(true);
      } catch (FileNotFoundException e) {

      }
    }
  }

  @Test
  public void testWithSimpleFileNoImg() throws IOException {
    Path tempDirectory = Files.createTempDirectory("html");
    try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/simple.html")) {
      assertNotNull(resourceAsStream);

      File targetFile = new File(tempDirectory.toFile(), "simple.html");
      Files.copy(resourceAsStream, targetFile.toPath());

      String absPath = tempDirectory.toAbsolutePath() + "/simple.html";
      new HtmlEmbedImages(absPath, true);
    }
  }

  @Test
  public void testWithFamilyBudget() throws IOException {
    Path tempDirectory = Files.createTempDirectory("html");
    try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/family_budget.html");
         InputStream resourceAsStream2 = this.getClass().getResourceAsStream("/family_budget_html_7acd3ca0932d29a2.png")) {
      assertNotNull(resourceAsStream);

      File targetFile = new File(tempDirectory.toFile(), "family_budget.html");
      Files.copy(resourceAsStream, targetFile.toPath());
      targetFile = new File(tempDirectory.toFile(), "family_budget_html_7acd3ca0932d29a2.png");
      Files.copy(resourceAsStream2, targetFile.toPath());

      String absPath = tempDirectory.toAbsolutePath() + "/family_budget.html";
      new HtmlEmbedImages(absPath, true);
      String contents = new String(Files.readAllBytes(Paths.get(absPath)));
      assert(!contents.contains("family_budget_html_7acd3ca0932d29a2.png"));
    }
  }
}
