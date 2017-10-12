package SfAttachDownload

import com.sforce.soap.partner.PartnerConnection

import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
    if (System.getProperty("os.name").matches(".*Windows.*")) {
      //actions in case of Windows
      args.length match {
        case 1 =>
          val caseNumber = args(0)
          if (caseNumber.length != 8) {
            println("Please enter valid case number.")
            System.exit(1)
          }
          val encr = new Encryption
          val (userDecr, passDecr) = encr.decrypt
          userDecr match {
            case Some(user) => passDecr match {
              case Some(pass) =>
                //connect to SalesForce
                val connection = new SfConnect
                val partnerConnection: Try[PartnerConnection] = connection.windowsConnect(user, pass)
                partnerConnection match {
                  case Success(v) =>
                    //query emails and ftp
                    val query = new SfQuery
                    val listOfLinksAndFtp: (List[List[String]], Option[String]) = query.prepareResults(partnerConnection, caseNumber)

                    //download attachments locally and upload to ftp, if it exists
                    val down = new SfDownload
                    down.download(listOfLinksAndFtp, caseNumber)
                  case Failure(ex) => println(ex)
                }

              case None => println("Unable to get password.\nPlease run jar with all 3 parameters.")
            }
            case None => println("Please run jar with all 3 parameters.")
          }
        case 3 =>
          val caseNumber = args(0)
          if (caseNumber.length != 8) {
            println("Please enter valid case number.")
            System.exit(1)
          }
          val userName = args(1)
          val password = args(2)

          //connect to SalesForce
          val connection = new SfConnect
          val partnerConnection: Try[PartnerConnection] = connection.windowsConnect(userName, password)
          partnerConnection match {
            case Success(v) =>
              //encrypt password and write to file
              val encr = new Encryption
              encr.encrypt(userName, password)

              //query emails and ftp
              val query = new SfQuery
              val listOfLinksAndFtp: (List[List[String]], Option[String]) = query.prepareResults(partnerConnection, caseNumber)

              //download attachments locally and upload to ftp, if it exists
              val down = new SfDownload
              down.download(listOfLinksAndFtp, caseNumber)
            case Failure(ex) => println(ex)
          }
        case _ =>
          println(s"\nPlease run jar with parameters:\n\n" +
            "- case number\n" +
            "- username (required for the first run of the program)\n" +
            "- passwordSecurityToken (no space)(required for the first run of the program)\n\n" +
            "Example:\n\n" +
            """PS C:\Users\Administrator> java -jar "C:\temp\download.jar" "12345678" "username@domain.com" "PasswordSecurityToken" """ + "\n")
      }
    }
    else {
      //actions in case of Linux
      args.length match {
        case 1 =>
          val caseNumber = args(0)
          if (caseNumber.length != 8) {
            println("Please enter valid case number.")
            System.exit(1)
          }

          //connect to SalesForce
          val connection = new SfConnect
          val partnerConnection: Try[PartnerConnection] = connection.connect

          //query emails and ftp
          val query = new SfQuery
          val listOfLinksAndFtp: (List[List[String]], Option[String]) = query.prepareResults(partnerConnection, caseNumber)

          //download attachments locally and upload to ftp, if it exists
          val down = new SfDownload
          down.download(listOfLinksAndFtp, caseNumber)

        case _ => println(s"\nWrong parameters! For Linux please specify case number.\n" +
          "Example:\n\n" +
          """root@localhost$ java -jar "C:\temp\download.jar" 12345678 """ + "\n")
      }
    }

  }

}