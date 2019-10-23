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
  public static final String REGEX_IMG_HEIGHT = "(?:<img)(.*)(?:height)(\\D*)(\\d*)(?:)(.*)(?:>)";
  public static final String REGEX_IMG_WIDTH = "(?:<img)(.*)(?:width)(\\D*)(\\d*)(?:)(.*)(?:>)";
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
        line = removeImageHeight(verbose, line);
        line = replaceImageSrc(verbose, file, imgFileNames, line);
        sb.append(line);
      } else {
        sb.append(line);
      }
    }
    //Write back changed string
    Files.write(file.toPath(), sb.toString().getBytes());
  }

  private String replaceImageSrc(boolean verbose, File file, Set<String> imgFileNames, String line) throws IOException {
    //Find and replace image contents
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
        System.out.println("Referenced image file " + imgFileName + " does not exist");
        continue;
      }

      String embedFilePrefix;
      if (imgFileName.toLowerCase().trim().endsWith(".png")) {
        embedFilePrefix = "data:image/png;base64,";
      } else if (imgFileName.toLowerCase().trim().endsWith(".jpg") || imgFileName.toLowerCase().trim().endsWith(".jpeg")) {
        embedFilePrefix = "data:image/jpeg;base64,";
      } else {
        System.out.println("File extension for " + imgFile + " is not supported");
        continue;
      }

      //Embed image
      byte[] imgBytes = Files.readAllBytes(imgFile.toPath());
      String encodedFile = Base64.getEncoder().encodeToString(imgBytes);
      line = line.replaceAll(imgFileName, embedFilePrefix + encodedFile);
    }
    return line;
  }

  private String removeImageHeight(boolean verbose, String line) throws IOException {
    //Find and replace image contents
    Pattern pattern = Pattern.compile(REGEX_IMG_HEIGHT);
    Matcher matcher = pattern.matcher(line);

    while (matcher.find()) {
      String height = matcher.group(3);
      if (verbose)
        System.out.println("Removing height " + height + "from image tag");

      line = line.replaceAll("height="+height, "");
      line = line.replaceAll("height=\""+height+"\"", "");
      line = line.replaceAll("height='"+height+"'", "");
    }
    return line;
  }
}
