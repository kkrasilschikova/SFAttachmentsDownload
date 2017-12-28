package SfAttachDownload

import java.io.File
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, Future}
import cats.Eval
import cats.data.Writer

import org.apache.commons.io.FileUtils

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class SfDownload {
  def download(listOfLinks: List[List[String]], caseNumber: String, targetFolder: String, threads: Int, ftpLink: Option[String]): Unit = {
    //if target folder is not provided
    //then in working dir try to create folder called case number (8 digits) and store files there
    val caseDir: String = {
      val targetDir = targetFolder match {
        case x: String if x.isEmpty =>
          if (System.getProperty("os.name").matches("Windows.*"))
            new File(s"${System.getProperty("user.dir")}\\$caseNumber")
          else new File(s"${System.getProperty("user.dir")}/$caseNumber")
        case x: String if x.nonEmpty =>
          new File(s"$targetFolder")
        case _ =>
          println("Please specify another target folder"); System.exit(-1); new File("")
      }
      if (targetDir.mkdir || targetDir.exists) targetDir.getAbsolutePath else {
        println("Please specify another target folder")
        System.exit(-1)
        ""
      }
    }

    println(s"Local folder for downloading file(s) is $caseDir\n")

    val links: Eval[List[String]] = Eval.always(listOfLinks.flatten)
    val fileNames: Eval[List[String]] = Eval.always(links.value
      .map(link => link.substring(link.lastIndexOf("/") + 1, link.indexOf("?"))))

    //add GUID to duplicated files in order not to overwrite
    @tailrec
    def renameDuplicates(l: List[String], acc: List[String]): List[String] = {
      l match {
        case head :: tail =>
          //rename if source list has file with the same name added several times
          if (l.count(_ == head) > 1)
            renameDuplicates(tail, acc :+ head.replace(".", s"_${UUID.randomUUID().toString.toUpperCase}."))
          else renameDuplicates(tail, acc :+ head)

        case Nil => acc
      }
    }

    val renamedFiles = renameDuplicates(fileNames.value, List())
    val links_files: List[(String, String)] = links.value.zip(renamedFiles)

    implicit val ec = ExecutionContext.fromExecutor(Executors.newWorkStealingPool(threads))

    //get file name out of full path
    def defineTargetName(fullName: String) = {
      val index = if (System.getProperty("os.name").matches("Windows.*"))
        fullName.lastIndexOf("\\")
      else fullName.lastIndexOf("/")
      fullName.substring(index + 1)
    }

    //download attachments locally
    def downloading(source: String, target: String): Unit = {
      val targetName = defineTargetName(target)
      Try(FileUtils.copyURLToFile(new URL(s"$source"), new File(s"$target")))
      match {
        case Success(_) => println(s"\nFile $targetName downloaded successfully")
        case Failure(ex) => println(s"\nFailed to download $targetName:\n  ${ex.getMessage}")
        case _ => println(s"\nFailed to download $targetName")
      }
    }

    //establish ftp connection
    val (connected, ftpClient): (Boolean, FTPClass) = if (ftpLink.isDefined) {
      val ftp = FtpTarget(ftpLink.get)
      val client: FTPClass = MyFTPClient()
      client.connect(ftp.server)
        .flatMap(_ => client.login(ftp.user, ftp.pass)) match {
        case Success(true) => (true, client)
        case _ => println("Ftp connection failed");(false, client)
      }
    } else (false, MyFTPClient())

    //async download and upload
    def futureDownloadAndUpload(link_file: (String, String)) = {
      val targetFile: String = if (System.getProperty("os.name").matches("Windows.*")) s"$caseDir\\${link_file._2}" else s"$caseDir/${link_file._2}"
      Future(if (!new File(targetFile).exists)
        Writer(println(s"Downloading ${link_file._2}"), downloading(link_file._1, targetFile))
          .map (_ => if (connected) Writer(println(s"Uploading ${link_file._2} on ftp"), ftpClient.uploadOnFtp(targetFile, link_file._2))))
    }

    val res = Future.traverse(links_files)(futureDownloadAndUpload)
    //show "dotted" progress while files are being downloaded
    while (!res.isCompleted) {
      print(".")
      Thread.sleep(700)
    }
    Await.result(res, Duration.Inf)

    ftpClient.disconnect()

    //open file explorer with downloaded files
    if (System.getProperty("os.name").matches("Windows.*")) {
      import scala.sys.process._
      val explorer = "C:\\Windows\\explorer"
      s"$explorer $caseDir".!
    }
  }

}