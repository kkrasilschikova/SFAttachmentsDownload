package SfAttachDownload

import java.io.{BufferedInputStream, FileInputStream}

import org.apache.commons.net.ftp._

import scala.util.Try
import scala.util.matching.Regex

object MyFTPClient{
  def apply(): FTPClass = FTPClass(new FTPClient)
}

final case class FTPClass(client: FTPClient) {
  def connect(host: String): Try[Unit] = Try {
    client.connect(host)
    client.enterLocalPassiveMode()
  }
  def login(username: String, password: String): Try[Boolean] = Try {
    client.login(username, password)
    client.setFileType(FTP.BINARY_FILE_TYPE)
  }
  def uploadOnFtp(sourceFile: String, targetName: String): Try[Boolean] = Try {
    client.storeFile(targetName, new BufferedInputStream(new FileInputStream(sourceFile)))
  }
  def disconnect(): Unit = client.disconnect()
}

final case class FtpTarget(ftp: String) {
  val ftpRe: Regex = "ftp://(ftp\\d{8}):(.*)@(supportftp\\d{0,2}.veeam.com)".r
  def user: String = ftp match {
    case ftpRe(user, _, _) => user
    case _ => "bad_user"
  }
  def pass: String = ftp match {
    case ftpRe(_, pass, _) => pass
    case _ => "bad_pass"
  }
  def server: String = ftp match {
    case ftpRe(_, _, server) => server
    case _ => "bad_server"
  }
}