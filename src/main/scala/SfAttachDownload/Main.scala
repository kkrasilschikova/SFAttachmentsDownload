package SfAttachDownload

import com.sforce.soap.partner.PartnerConnection

import scala.util.Try

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length == 1) {
      val caseNumber = args(0)

      val connection = new SfConnect
      val partnerConnection: Try[PartnerConnection] = connection.connect

      val query = new SfQuery
      val listOfLinksAndFtp: (List[List[String]], String) = query.prepareResults(partnerConnection, caseNumber)

      val down = new SfDownload
      down.download(listOfLinksAndFtp)
    }
    else {
      println(s"\nWrong parameters! Please specify case number.\n")

    }
  }
}
