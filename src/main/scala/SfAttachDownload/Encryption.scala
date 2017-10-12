package SfAttachDownload

import java.io._
import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Files, Path, StandardCopyOption}

import SfAttachDownload.JSONConverter.{credsReads, credsWrites}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.sys.process._
import scala.util.{Failure, Success, Try}

class Encryption {
  //copy String.Protector.exe to temp dir
  val stream: InputStream = getClass.getResourceAsStream("/StringProtector.exe")
  val stringProtector: Path = Files.createTempFile("StringProtector", ".exe")
  Files.copy(stream, stringProtector, StandardCopyOption.REPLACE_EXISTING)
  stream.close()
  stringProtector.toFile.deleteOnExit()
  //file where credentials are saved
  val filename: String = System.getProperty("user.home") + "\\Documents\\salesforce-attachments-download.txt"

  //define vals for stdout and stderr
  val stdoutStream = new ByteArrayOutputStream
  val stderrStream = new ByteArrayOutputStream
  val stdoutWriter = new PrintWriter(stdoutStream)
  val stderrWriter = new PrintWriter(stderrStream)

  def encrypt(username: String, password: String): Unit = {
    //write encrypted password and username to salesforce-attachments-download.txt in user Documents folder
    s"$stringProtector protect $password".!(ProcessLogger(stdoutWriter.println, stderrWriter.println)) match {
      //check exit code
      case 0 =>
        stdoutWriter.close()
        stderrWriter.close()
        val encrPass = stdoutStream.toString("UTF-8")
        val jsValue = transformToJSON(username, encrPass.take(encrPass.length-2))
        writeToFile(jsValue)
        println(s"Credentials are saved to $filename")
      case _ =>
        stdoutWriter.close()
        stderrWriter.close()
        println("Unable to encrypt password, credentials are not saved.")
    }

    def transformToJSON(username: String, password: String): JsValue = {
      Json.toJson(Credentials(username, password))
    }

    def writeToFile(input: JsValue): Unit = {
      val output: String = Json.stringify(input)
      val fw = new FileWriter(filename)
      fw.write(s"$output")
      fw.close()
    }
  }

  def decrypt: (Option[String], Option[String]) = {
    //get username and password from text file
    if (new File(filename).exists) {
      val path = FileSystems.getDefault.getPath(s"$filename")
      val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)
      //check if possible to read saved file with credentials
      Try{reader.readLine()} match {
        case Success(input) =>
          //parse string to JSON
          Try {
            Json.parse(input)
          } match {
            case Success(validCreds) =>
              //try to transform to case class Credentials
              validCreds.validate[Credentials](credsReads) match {
                case success: JsSuccess[Credentials] =>
                  val encrPass = (validCreds \ "password").get
                  s"$stringProtector unprotect $encrPass".!(ProcessLogger(stdoutWriter.println, stderrWriter.println)) match {
                    //check exit code
                    case 0 =>
                      stdoutWriter.close()
                      stderrWriter.close()
                      val decrPass = stdoutStream.toString.substring(0, stdoutStream.toString.length - 2)
                      println("Found saved credentials.")
                      (Some(success.get.username), Some(decrPass))
                    case _ =>
                      stdoutWriter.close()
                      stderrWriter.close()
                      println("Unable to decrypt password.")
                      (None, None)
                  }
                case error: JsError =>
                  println("Unable to read saved credentials.")
                  (None, None)
              }
            case Failure(ex) =>
              println("Unable to validate saved credentials.")
              (None, None)
          }
        case Failure(ex) =>
          println("Unable to read saved file with credentials.")
          (None, None)
      }
    }
    else {
      //no file - no credentials
      println("There are no saved credentials.\n")
      (None, None)
    }
  }

}