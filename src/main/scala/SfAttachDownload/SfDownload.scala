package SfAttachDownload

import java.io.InputStream
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

import scala.sys.process._

class SfDownload {
  def download(listOfLinksAndFtp: (List[List[String]], Option[String])): Unit = {
    //copy curl.exe to default temp directory from download.jar in case of Windows OS
    //and remove curl.exe when the program is finished
    val curl: Path = if (System.getProperty("os.name").matches("Windows.*")) {
      val stream: InputStream = getClass.getResourceAsStream("/curl.exe")
      val targetPath: Path = Files.createTempFile("curl", ".exe")
      Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING)
      stream.close()
      targetPath.toFile.deleteOnExit()
      targetPath
    }
    else Paths.get("") //empty string if OS is not Windows, this val won't be used in the program

    //download attachments locally and on ftp, if it exists
    println(s"Local folder for downloading file(s) is ${System.getProperty("user.dir")}")
    for {
      list <- listOfLinksAndFtp._1
      link <- list
    } yield {
      val fileName = link.substring(link.lastIndexOf("/") + 1, link.indexOf("?"))

      if (System.getProperty("os.name").matches("Windows.*")) {
        println(s"\nDownloading $fileName locally")
        s"${curl.toFile} -k -o $fileName $link".! //download locally
        if (listOfLinksAndFtp._2.isDefined) {
          println(s"\nUploading $fileName to ftp")
          s"${curl.toFile} -k -T $fileName ${listOfLinksAndFtp._2.get}".! //upload to ftp
        }
      }
      else {
        println(s"\nDownloading $fileName locally")
        s"curl -k -o $fileName $link".! //download locally
        if (listOfLinksAndFtp._2.isDefined) {
          println(s"\nUploading $fileName to ftp")
          s"curl -k -T --fail $fileName ${listOfLinksAndFtp._2.get}".! //upload to ftp
        }
      }
    }
  }

}