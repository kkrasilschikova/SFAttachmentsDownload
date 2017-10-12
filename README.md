# SfAttachDownload
#### Purpose:
Download all Customer's attachments (that are displayed as separate emails) to current folder (where folder called case number will be created) and upload them on ftp.

#### Prerequisites
FTP for case should be created in advance, if you need to upload attachments to ftp. 
##### Prerequisites for Windows:
Java should be installed to run download.jar file.  
##### Prerequisites for Linux:
SALESFORCE_USERNAME and SALESFORCE_PASSWORD environment variables.  
SALESFORCE_PASSWORD should have a format like passwordSecurityToken.  
Check that curl and java are installed.

#### In order to run the program

1) download [download.jar](https://github.com/kkrasilschikova/SFAttachmentsDownload/blob/master/download.jar)  
2) start it with parameters:
- Windows, the first run of the program: *java -jar download.jar 12345678 username@domain.com passwordToken*.
- Windows, all subsequent runs: *java -jar download.jar 12345678*
- Linux: *java -jar download.jar 12345678*.

#### How to generate SalesForce security token

1) login to SalesForce  
2) in the top right corner click on your name -> My settings  
3) click Personal -> Reset My Security Token, or enter 'reset' in quick find

You'll receive an e-mail with a new token.
