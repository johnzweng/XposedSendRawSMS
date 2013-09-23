package at.zweng.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.StringTokenizer;

import com.android.internal.telephony.gsm.SmsMessage.SubmitPdu;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Module for the Xposed framework whioch allows sending of raw SMS PDUs by
 * using a special sms message text:<br>
 * sendSmsByRawPDU|xxxxxx|yyyyyyyyyyyyyy<br>
 * <br>
 * where "sendSmsByRawPDU" is a hardcoded keyword, "xxxxxx" represents the SMSC
 * part of a valid SMS PDU in hexadecimal string representation (ore use "00"
 * for default smsc), and "yyyyyyyyyyyyyy" represents the message part of a
 * valid SMS PDU in hexadecimal string representation.<br>
 * <br>
 * see 3GPP TS 23.040 specification for description of GSM SMS PDU format <br>
 * <br>
 * <br>
 * Example: the message:
 * "sendSmsByRawPDU|00|01000A91214365870900000CC8329BFD065DDF72363904" would
 * result in a normal text SMS to the international number "+1234567890" with
 * the text "Hello World!" using the phones default SMSC.
 * 
 * @author Johannes Zweng
 */
public class ModSmsMessage implements IXposedHookLoadPackage {
	private static final String TARGET_PACKAGE = "com.android.phone";
	private static final String RAWSMS_MESSAGE_PREFIX = "sendSmsByRawPDU";

	static int hexCharToInt(char c) {
		if (c >= '0' && c <= '9')
			return (c - '0');
		if (c >= 'A' && c <= 'F')
			return (c - 'A' + 10);
		if (c >= 'a' && c <= 'f')
			return (c - 'a' + 10);
		throw new RuntimeException("invalid hex char '" + c + "'");
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
		if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
			// XposedBridge.log("SendRawSMSMod: ignoring package: " +
			// lpparam.packageName);
			return;
		}
		XposedBridge
				.log("SendRawSMSMod: we are in phone application. Will place method hooks.");

		findAndHookMethod("com.android.internal.telephony.gsm.SmsMessage",
				lpparam.classLoader, "getSubmitPdu", String.class,
				String.class, String.class, boolean.class, byte[].class,
				int.class, int.class, int.class, new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {

						XposedBridge
								.log("SendRawSMSMod: getSubmitPdu() was called");

						// if message paramete is null do nothing
						if (param.args[2] == null) {
							return;
						}

						try {
							// try to get message text
							String message = (String) param.args[2];
							XposedBridge.log("SendRawSMSMod: got SMS message: "
									+ message);
							// and if it starts with special prefix, return our
							// own PDU
							if (message.startsWith(RAWSMS_MESSAGE_PREFIX)) {
								StringTokenizer tokenizer = new StringTokenizer(
										message, "|", false);
								if (tokenizer.countTokens() < 3) {
									XposedBridge
											.log("SendRawSMSMod: sms message does not contain 3 parts separated by pipe | symbol. Will do nothing.");
									return;
								}

								// first remove the prefix part from string
								tokenizer.nextToken();

								SubmitPdu rawPdu = new SubmitPdu();

								// second part should be hex string SC adress
								// part of PDU
								rawPdu.encodedScAddress = hexStringToBytes(tokenizer
										.nextToken());

								// third part should be hex string encoded
								// message part of PDU
								rawPdu.encodedMessage = hexStringToBytes(tokenizer
										.nextToken());
								// and set return value for hooked message call
								param.setResult(rawPdu);

								XposedBridge
										.log("SendRawSMSMod: intercepted getSubmitPdu() call and will return our own PDU.");

								return;
							}

						} catch (Exception e) {
							XposedBridge
									.log("SendRawSMSMod: catched exception: "
											+ e + ", " + e.getMessage());
						}
					}

				});
		XposedBridge.log("SendRawSMSMod: method hook in place! :-)");
	}

	/**
	 * Converts a hex String to a byte array.
	 * 
	 * @param s
	 *            A string of hexadecimal characters, must be an even number of
	 *            chars long
	 * 
	 * @return byte array representation
	 * 
	 * @throws RuntimeException
	 *             on invalid format
	 */
	public static byte[] hexStringToBytes(String s) {
		byte[] ret;
		if (s == null)
			return null;
		int sz = s.length();
		ret = new byte[sz / 2];
		for (int i = 0; i < sz; i += 2) {
			ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s
					.charAt(i + 1)));
		}
		return ret;
	}

}
