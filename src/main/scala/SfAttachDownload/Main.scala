package SfAttachDownload

import com.sforce.soap.partner.PartnerConnection

import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
    case class Config(caseNumber: String = "", targetFolder: String = "", userName: String = "", passwordToken: String = "",
                      threads: Int = Runtime.getRuntime.availableProcessors() - 1, only: Int = 2, ftp: Boolean = true)

    val parser = new scopt.OptionParser[Config]("salesforce-attachments-download") {
      opt[String]('c', "caseNumber").required().valueName("<8_digit_value>")
        .text("required").action((x, c) => c.copy(caseNumber = x))
        .validate(x => if (x.matches("\\d{8}")) success else failure("Invalid case number"))
      opt[String]('d', "destinationFolder").optional().valueName("<target_for_downloads>")
        .text("optional, by default current_dir\\case_number").action((x, c) => c.copy(targetFolder = x))
      opt[String]('u', "userName").optional().valueName("<SF_username>")
        .text("required on Windows for the first run only").action((x, c) => c.copy(userName = x))
      opt[String]('p', "passwordToken").optional().valueName("<SFpasswordToken (no spaces)>")
        .text("required on Windows for the first run only").action((x, c) => c.copy(passwordToken = x))
      opt[Int]('t', "threads").optional().valueName("<number_of_threads>")
        .text("optional, by default equal to available CPUs-1").action((x, c) => c.copy(threads = x))
        .validate(x => if (x != 0) success else failure("Not enough threads"))
      opt[Int]('o', "only").optional().valueName("<0, 1 or nothing>")
        .text("optional, if 1 then download only the latest attachment(s), if 0 - only the first, by default download all files")
        .action((x, c) => c.copy(only = x))
      opt[Boolean]('f', "ftp").optional().valueName("<true (1) or false (0)>").action((x, c) => c.copy(ftp = x))
        .text("optional, by default - true - uploads files on ftp")
      help("help").text("prints this usage text")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val partnerConnection: Try[PartnerConnection] = if (System.getProperty("os.name").matches(".*Windows.*")) {
          //actions in case of Windows
          //username and password validation
          val encr = new Encryption
          val (userName, password): (String, String) = if (config.userName.isEmpty || config.passwordToken.isEmpty) {
            encr.decrypt match {
              case (Some(user), Some(pass)) => (user, pass)
              case _ =>
                System.exit(-1)
                ("none", "none")
            }
          } else {
            encr.encrypt(config.userName, config.passwordToken)
            (config.userName, config.passwordToken)
          }
          //connect to SalesForce
          val connection = new SfConnect
          connection.windowsConnect(userName, password)
        }
        else {
          //actions in case of Linux
          //connect to SalesForce
          val connection = new SfConnect
          connection.connect
        }

        partnerConnection match {
          case Success(_) =>
            //query emails and ftp
            val query = new SfQuery
            val listOfLinks: List[List[String]] =
              query.prepareResults(partnerConnection, config.caseNumber, config.only)
            val ftpLink: Option[String] = if (config.ftp) query.getFtp(partnerConnection, config.caseNumber) else None

            //download attachments locally
            val down = new SfDownload
            down.download(listOfLinks, config.caseNumber, config.targetFolder, config.threads, ftpLink)
          case Failure(ex) => println(ex)
        }

      case None => // arguments are bad, error message will be displayed
    }
  }

}