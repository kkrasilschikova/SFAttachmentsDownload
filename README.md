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

OR simply download [download.jar file](https://github.com/kkrasilschikova/SFAttachmentsDownload/target/scala-2.12/download.jar) and start it with parameter case number.
