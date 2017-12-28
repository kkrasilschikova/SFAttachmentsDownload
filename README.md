# SfAttachDownload
#### Purpose:
Download all Customer's attachments (that are displayed as separate emails) to provided folder (or to current dir) and upload them on ftp (if needed).

#### Prerequisites
Java should be installed, ftp for case should be created in advance.
##### Prerequisites for Linux:
SALESFORCE_USERNAME and SALESFORCE_PASSWORD environment variables.  
SALESFORCE_PASSWORD should have a format like passwordSecurityToken.  

#### In order to run the program

1) download [download.jar](https://github.com/kkrasilschikova/SFAttachmentsDownload/raw/master/download.jar)  
2) start it with needed parameters, examples:
- Windows, the first run of the program: *java -jar download.jar -c 12345678 -u username@domain.com -p passwordToken*.
- Windows, all subsequent runs: *java -jar download.jar -c 12345678*
- Linux: *java -jar download.jar -c 12345678*  

#### salesforce-attachments-download usage:

  -c, --caseNumber \<8_digit_value> required  
  -d, --destinationFolder \<target_for_downloads> optional, by default current_dir\case_number  
  -u, --userName \<SF_username> required on Windows for the first run only  
  -p, --passwordToken \<SFpasswordToken (no spaces)> required on Windows for the first run only  
  -t, --threads \<number_of_threads> optional, by default equal to available CPUs-1  
  -o, --only \<0, 1 or nothing> optional, if 1 then download only the latest attachment(s), if 0 - only the first, by default download all files    
  --help prints this usage text

#### How to generate SalesForce security token

1) login to SalesForce  
2) in the top right corner click on your name -> My settings  
3) click Personal -> Reset My Security Token, or enter 'reset' in quick find

You'll receive an e-mail with a new token.

#### Remarks
1) Already downloaded file won't be downloaded again  
2) If there are several attachments with the same name, the newest will be downloaded as is, all the rest will have GUID in their names  
3) As a side-effect of point 2, file with GUID in its name will be downloaded again  
4) Encrypted password will be stored in %userprofile%\Documents\salesforce-attachments-download.txt
