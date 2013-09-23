
Module for the Xposed framework (http://repo.xposed.info/) which allows 
sending of raw SMS PDUs by using a special sms message text:

  sendSmsByRawPDU|xxxxxx|yyyyyyyyyyyyyy<br>

where "sendSmsByRawPDU" is a hardcoded keyword, "xxxxxx" represents the SMSC
part of a valid SMS PDU in hexadecimal string representation (or use "00"
for default smsc), and "yyyyyyyyyyyyyy" represents the message part of a
valid SMS PDU in hexadecimal string representation.<br>

(see 3GPP TS 23.040 specification for description of GSM SMS PDU format)



  Example:
  --------
  sending the sms message:
  
   "sendSmsByRawPDU|00|01000A91214365870900000CC8329BFD065DDF72363904"

  would result in a normal text SMS to the international number "+1234567890" 
  with the text "Hello World!" using the phones default SMSC.
  

tested only with API level 18 (Android 4.3)
