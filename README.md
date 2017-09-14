# SFAttachmentsDownload
#### Purpose:
Download all Customer's attachments (that are displayed as separate emails) to current folder and upload them on ftp.

#### Prerequisites:
Java should be installed to run download.jar file.  
FTP for case should be created in advance.  
SALESFORCE_USERNAME and SALESFORCE_PASSWORD environment variables.  
SALESFORCE_PASSWORD should have a format like passwordSecurityToken.  
Curl.exe is already included in download.jar, no need to download it manually.

#### In order to run the program

- run *sbt assembly*
- run resulting jar with parameter case number, example:
*java -jar download.jar 12345678*

OR simply download [download.jar](https://github.com/kkrasilschikova/SFAttachmentsDownload/blob/master/download.jar) and start it with parameter case number, example: *java -jar download.jar 12345678*.

#### How to generate SalesForce security token

1) login to SalesForce
2) in the top right corner click on your name -> My settings
3) click Personal -> Reset My Security Token, or enter 'reset' in quick find

You'll receive an e-mail with a new token.
