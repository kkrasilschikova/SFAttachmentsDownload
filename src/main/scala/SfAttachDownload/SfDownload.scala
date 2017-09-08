package SfAttachDownload

import scala.sys.process._
import java.io.File

class SfDownload {
  def download(listOfLinksAndFtp: (List[List[String]], String) ): Unit = {
    val regex = "[^https://cptl.s3.amazonaws.com/ticket/\\d{8}/].*\\.[a-z]+".r
    val curl="E:\\downloaded_programs\\curl-7.55.0-win64-mingw\\bin\\curl.exe"
    for {
      list <- listOfLinksAndFtp._1
      link <- list
    } yield {
      //val fileName = regex.findFirstIn(link).get
      val fileName = link.substring(link.lastIndexOf("/")+1, link.indexOf("?"))
      if (!fileName.contains("/")) {
        println(fileName)
        println(listOfLinksAndFtp._2)
        s"$curl -o $fileName $link".!!
        s"$curl -T $fileName ${listOfLinksAndFtp._2}".!!
        val file = new File(fileName)
        if (file.exists) file.delete
      }
    }
  }

}
