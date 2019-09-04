package org.redpill_linpro.alfresco.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlEmbedImages {
  public static final String REGEX_FILENAME = "(?:img src=\")(.*)(?:\")";

  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      System.out.println("Usage: java -jar html-embed-images.jar sourcefile.html");
    } else {
      try {
        boolean verbose = false;
        for (String arg : args) {
          if ("--verbose".equalsIgnoreCase(arg.trim())) {
            verbose = true;
          }
        }
        new HtmlEmbedImages(args[0], verbose);
      } catch (FileNotFoundException e) {
        System.out.println(e.getMessage());
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }


  public HtmlEmbedImages(final String sourceFile, boolean verbose) throws IOException {
    File file = new File(sourceFile);
    if (!file.exists()) {
      throw new FileNotFoundException(sourceFile + " does not exist");
    }
    System.out.println("Embedding images in " + sourceFile);
    StringBuffer sb = new StringBuffer();
    List<String> lines = Files.readAllLines(file.toPath());
    Set<String> imgFileNames = new HashSet<>();
    for (String line : lines) {
      if (line.contains("<img")) {
        Pattern pattern = Pattern.compile(REGEX_FILENAME);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
          String imgFileName = matcher.group(1);
          if (verbose)
            System.out.println("Embedding " + imgFileName);
          imgFileNames.add(imgFileName);
          //Find image
          File parentFile = file.getParentFile();
          String parentAbsPath = parentFile.getAbsolutePath();
          String imgAbsPath = parentAbsPath + "/" + imgFileName;
          File imgFile = new File(imgAbsPath);
          if (!imgFile.exists()) {
            throw new FileNotFoundException("Referenced image file " + imgFileName + " does not exist");
          }

          String embedFilePrefix;
          if (imgFileName.toLowerCase().trim().endsWith(".png")) {
            embedFilePrefix = "data:image/png;base64,";
          } else if (imgFileName.toLowerCase().trim().endsWith(".jpg") || imgFileName.toLowerCase().trim().endsWith(".jpeg")) {
            embedFilePrefix = "data:image/jpeg;base64,";
          } else {
            throw new UnsupportedEncodingException("File extension for " + imgFile + " is not supported");
          }

          //Embed image
          byte[] imgBytes = Files.readAllBytes(imgFile.toPath());
          String encodedFile = Base64.getEncoder().encodeToString(imgBytes);
          line = line.replaceAll(imgFileName, embedFilePrefix + encodedFile);
        }
        sb.append(line);
      } else {
        sb.append(line);
      }
    }
    //Write back changed string
    Files.write(file.toPath(), sb.toString().getBytes());
  }

}
