package SfAttachDownload

import scala.util.Try
import scala.annotation.tailrec
import com.sforce.soap.partner.{PartnerConnection, QueryResult}
class SfQuery {
  def prepareResults(connection: Try[PartnerConnection], caseNumber: String): (List[List[String]], String)= {

    val xmlResult: QueryResult = connection.get
      .query(s"select (select Subject, HTMLBody from EmailMessages order by CreatedDate) from Case where CaseNumber='$caseNumber'")
    val emailMessages: com.sforce.ws.bind.XmlObject=(for (record<-xmlResult.getRecords) yield record.getChild("EmailMessages")).toList.head
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
    val allLinks: List[String] = links.map(_.replace(">", "\n"))

    val regex = "'http.*'".r
    val listOfLinks: List[List[String]] = allLinks.map(x=>regex.findAllIn(x).toList.map(_.replace("'", "")))

    val xmlFtp: QueryResult = connection.get.query(s"Select LogLocationFtpURL__c from Case where CaseNumber='$caseNumber'")
    val ftp: String =(for (record<-xmlFtp.getRecords) yield record.getField("LogLocationFTPURL__c")).toList.head.toString

    (listOfLinks, ftp)
  }
}
