package SfAttachDownload

import scala.sys.process._
import java.io.{File, InputStream}
import java.nio.file.Files

class SfDownload {
  def download(listOfLinksAndFtp: (List[List[String]], String) ): Unit = {
    //copy curl.exe to C:/temp/curl.exe from download.jar
    val stream: InputStream = getClass.getResourceAsStream("/curl.exe")
    val file: File = new File("C:\\temp")
    if (!file.exists()) file.mkdir()
    val curl: File = new File("C:\\temp\\curl.exe")
    if (!curl.exists()) {
      Files.copy(stream, curl.toPath)
      stream.close()
    }

    //download attachment locally and on ftp
    println(s"Local folder is ${System.getProperty("user.dir")}")
    for {
      list <- listOfLinksAndFtp._1
      link <- list
    } yield {
      val fileName = link.substring(link.lastIndexOf("/")+1, link.indexOf("?"))
      if (!fileName.contains("/")) {
        println(s"\nDownloading $fileName")
        s"$curl -k -o $fileName $link".!! //download locally
        s"$curl -k -T $fileName ${listOfLinksAndFtp._2}".!! //upload to ftp
      }
    }

  }

}