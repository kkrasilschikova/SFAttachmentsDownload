package SfAttachDownload

import com.sforce.soap.partner.{PartnerConnection, QueryResult}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class SfQuery {
  def prepareResults(connection: Try[PartnerConnection], caseNumber: String): (List[List[String]], Option[String]) = {

    //find all emails, then filter only that are attachments
    val xmlResult: QueryResult = connection.get
      .query(s"select (select Subject, HTMLBody from EmailMessages order by CreatedDate) from Case where CaseNumber='$caseNumber'")
    Try {
      (for (record <- xmlResult.getRecords) yield record.getChild("EmailMessages")).toList.head
    } match {
      case Success(emailMessages) =>
        val allRecords: java.util.Iterator[com.sforce.ws.bind.XmlObject] = emailMessages.getChildren("records")

        @tailrec
        def getLinks(it: java.util.Iterator[com.sforce.ws.bind.XmlObject], acc: List[String]): List[String] = {

          if (it.hasNext) {
            val next = it.next()
            if (next.getField("Subject") == s"Customer's attachments - Case # $caseNumber")
              getLinks(it, next.getChild("HtmlBody").getValue.toString :: acc)
            else getLinks(it, acc)
          }
          else
            acc
        }

        val links: List[String] = getLinks(allRecords, List())
        if (links.isEmpty) println("Case doesn't have any attachments.");System.exit(1) //exit program if there is no attachments
      val allLinks: List[String] = links.map(_.replace(">", "\n"))

        val regex = "'http.*'".r
        val listOfLinks: List[List[String]] = allLinks.map(x => regex.findAllIn(x).toList.map(_.replace("'", "")))

        //find ftp link with credentials
        val xmlFtp: QueryResult = connection.get.query(s"Select LogLocationFtpURL__c from Case where CaseNumber='$caseNumber'")
        val optionFtp: Object = (for (record <- xmlFtp.getRecords) yield record.getField("LogLocationFTPURL__c")).toList.head
        val ftp: Option[String] = optionFtp match {
          case x: String =>
            println(s"Ftp for case $caseNumber is $x\n")
            Some(x)
          case _ =>
            println(s"Ftp for case $caseNumber doesn't exist.")
            None
        }

        (listOfLinks, ftp)

      case Failure(ex) =>
        println("Case number is incorrect or there are no emails.")
        System.exit(1)
        (List(), None)
    }
  }

}