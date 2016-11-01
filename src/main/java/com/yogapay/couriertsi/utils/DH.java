package com.yogapay.couriertsi.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by lei on 15-8-12.
 */
public class DH {


//  public static String decodePinBlock(String ksn,String bdk,String encPinBlock,String model,String cardNo, String clearTpk){
//      String pinBlock = "";
//      String clearPin = "000000";
//      if(model.equals(Constants.MPOS_MODEL) || model.equals(Constants.SHUA_MODEL)){
//          byte[] byte_ksn = parseHexStr2Byte(ksn);
//          byte[] byte_bdk = parseHexStr2Byte(bdk);
//
//          byte[] ipek = GenerateIPEK(byte_ksn,byte_bdk);
//          String ipekStr = parseByte2HexStr(ipek);
//          System.out.println("ipekStr=" + ipekStr);
//
//          byte[] pinKey = GetPinKeyVariant(byte_ksn,ipek);
//          String pinKeyStr = parseByte2HexStr(pinKey);
//          System.out.println("pinKeyStr=" + pinKeyStr);
//
//          clearPin = parseByte2HexStr(TriDesDecryption(parseHexStr2Byte(pinKeyStr), parseHexStr2Byte(encPinBlock)));
//          System.out.println("pin: "+clearPin);
//      }
//      cardNo = cardNo.substring(cardNo.length() - 13,
//              cardNo.length() - 1);
//      // 将卡号与密码进行异或
//      byte[] x = ISOUtil.xor(ISOUtil.hex2byte("06" + clearPin + "FFFFFFFF"),
//              ISOUtil.hex2byte("0000" + cardNo));
//      System.out.println("clearTpk====${clearTpk}");
//      pinBlock = JCEHandler.encryptData(ISOUtil.hexString(x), clearTpk);
//      return pinBlock;
//  }
  public static String decodeTracks2(String ksn,String bdk,String encTracks,String model){
      String track2 = "";
      System.out.println("ksn======="+ksn);
      System.out.println("ksn======="+bdk);
      System.out.println("ksn======="+encTracks);
      if(model.equals("mpos") || model.equals("shua")){
          byte[] byte_ksn = parseHexStr2Byte(ksn);
          byte[] byte_bdk = parseHexStr2Byte(bdk);

          byte[] ipek = GenerateIPEK(byte_ksn,byte_bdk);
          String ipekStr = parseByte2HexStr(ipek);
          System.out.println("ipekStr=" + ipekStr);

          byte[] dataKey = GetDataKeyVariant(byte_ksn,ipek);
          String dataKeyStr = parseByte2HexStr(dataKey);
          System.out.println("dataKeyStr=" + dataKeyStr);

          byte[] dataKey__ = GetDataKey(byte_ksn,ipek);
          String dataKeyStr__ = parseByte2HexStr(dataKey__);
          System.out.println("dataKeyStr__=" + dataKeyStr__);

          track2 = parseByte2HexStr(TriDesDecryptionCBC(parseHexStr2Byte(dataKeyStr__), parseHexStr2Byte(encTracks)));
          System.out.println("track2: "+track2);
      }

      return track2;
  }


  public static void main(String[] args) {
//		// TODO Auto-generated method stub

      String ksn = "01314040800763E00015";
      String bdk = "0123456789ABCDEFFEDCBA9876543210";
      byte [] byte_ksn = parseHexStr2Byte(ksn);
      byte [] byte_bdk = parseHexStr2Byte(bdk);

      byte[] ipek = GenerateIPEK(byte_ksn,byte_bdk);
      String ipekStr = parseByte2HexStr(ipek);
      System.out.println("ipekStr=" + ipekStr);

      byte[] dataKey = GetDataKeyVariant(byte_ksn,ipek);
      String dataKeyStr = parseByte2HexStr(dataKey);
      System.out.println("dataKeyStr=" + dataKeyStr);

      byte[] dataKey__ = GetDataKey(byte_ksn,ipek);
      String dataKeyStr__ = parseByte2HexStr(dataKey__);
      System.out.println("dataKeyStr__=" + dataKeyStr__);

//		3352DE66 4E684C2C B45873C4 B82F1E35
      String datastr = "DBDF621246DA73D54D1CE315DC15D239FCF5B50A229653858CA52F7D0048B423AC29D84B930A918D19B8B1EE43BAEDDA64E8B77E71398B4DBD4CC9080C9350586AB166310BEEB3551409F880E50C6959F3D5C919393C3C7701326B832D9FCDDEADE05D1C0953560B6D0C735CFF07D8EEE8AA072F749C181635174A04B9FF8D3AF61F28D1AE715EE6F010874F665F868C5D6CE0685F526D1B64E5A42F892CC9497558C863288A13B5E1065CB7DB74CF16A5B9862ED148CAC0E098C7198C5D01013B86A09FAE9BC29432ABFE805A38E526164244BF983EEE04891195F3737D1DF1A8591C0C457AB3E4F8A44A0B7688F40DF010874F665F868C5DC0F86BF7CF7E71DE608D042613846E938193E94572091BC407666DDB58495E835321C05B3B9766713637818EB9D4C80BD366EF03C7A71D72D9EC605D5C73CC18B4FBE13C98FBFD560C8C75D296858A4C9E6498B4F6ABB4FA54D5D422FE710E";
      String deResultStr = parseByte2HexStr(TriDesDecryptionCBC(parseHexStr2Byte(dataKeyStr__), parseHexStr2Byte(datastr)));
      System.out.println("data: "+deResultStr);


      ksn = "00000332100300E00008";
      bdk = "0123456789ABCDEFFEDCBA9876543210";
      byte_ksn = parseHexStr2Byte(ksn);
      byte_bdk = parseHexStr2Byte(bdk);

      ipek = GenerateIPEK(byte_ksn,byte_bdk);
      ipekStr = parseByte2HexStr(ipek);
      System.out.println("ipekStr=" + ipekStr);

      byte[] pinKey = GetPinKeyVariant(byte_ksn,ipek);
      String pinKeyStr = parseByte2HexStr(pinKey);
      System.out.println("pinKeyStr=" + pinKeyStr);

      datastr = "A5F3FCB015C3D9C7";
      deResultStr = parseByte2HexStr(TriDesDecryption(parseHexStr2Byte(pinKeyStr), parseHexStr2Byte(datastr)));
      System.out.println("pin: "+deResultStr);
      // 测试加密
//		String keyStr = "AE6FB4E2A908F38D61719F2121965AEB";
//		String dataStr = "56EFDC1D0A6BCAC76B3D1B35B8766374ADF90100B1511F80F35CAAE4251A7FE47DA7D1AD8D6C9A526A75DABC3CE935189837BF639D776DB5FA2B11012522827A6553EE80CBC6516F90D17C83ECF6E586";
//		dataStr = dataFill(dataStr);
//		System.out.println(dataStr);
//		String enResultStr = parseByte2HexStr(TriDesEncryption(parseHexStr2Byte(keyStr),parseHexStr2Byte(dataStr)));
//		System.out.println("enResultStr加密后" + enResultStr);
//
//		// 测试解密
//		String deResultStr = parseByte2HexStr(TriDesDecryption(parseHexStr2Byte(keyStr),parseHexStr2Byte(dataStr)));
//		System.out.println(deResultStr);
  }


  /// <summary>
  /// Gerneate Initial PIN Encryption Key (IPEK)
  /// IPEK is use to intialize DUKPT table which will be injected into device
  /// Each device should has different IPEK
  /// </summary>
  /// <param name="ksn">Key serial number(KSN). A 10 bytes data. Which use to determine which BDK will be used and calculate IPEK. With different KSN, the DUKPT system will ensure different IPEK will be generated.
  /// Normally, the first 4 digit of KSN is used to determine which BDK is used. The last 21 bit is a counter which indicate the current key.
  /// </param>
  /// <param name="DK">Base Derivation Key(BDK). A 16 bytes data. Which use to derive IPEK and current keys.</param>
  /// <returns>IPEK. (16 bytes)</returns>
  public static byte[] GenerateIPEK(byte[] ksn, byte[] bdk)
  {
      byte[] result;
      byte[] temp, temp2, keyTemp;

      result = new byte[16];
      temp = new byte[8];
      keyTemp = new byte[16];

//      Array.Copy(bdk, keyTemp, 16);
      System.arraycopy(bdk, 0, keyTemp, 0, 16);   //Array.Copy(bdk, keyTemp, 16);
//      Array.Copy(ksn, temp, 8);
      System.arraycopy(ksn, 0, temp, 0, 8);    //Array.Copy(ksn, temp, 8);
      temp[7] &= 0xE0;
//      TDES_Enc(temp, keyTemp, out temp2);
      temp2 = TriDesEncryption(keyTemp,temp);    //TDES_Enc(temp, keyTemp, out temp2);temp
//      Array.Copy(temp2, result, 8);
      System.arraycopy(temp2, 0, result, 0, 8);   //Array.Copy(temp2, result, 8);
      keyTemp[0] ^= 0xC0;
      keyTemp[1] ^= 0xC0;
      keyTemp[2] ^= 0xC0;
      keyTemp[3] ^= 0xC0;
      keyTemp[8] ^= 0xC0;
      keyTemp[9] ^= 0xC0;
      keyTemp[10] ^= 0xC0;
      keyTemp[11] ^= 0xC0;
//      TDES_Enc(temp, keyTemp, out temp2);
      temp2 = TriDesEncryption(keyTemp,temp);    //TDES_Enc(temp, keyTemp, out temp2);
//      Array.Copy(temp2, 0, result, 8, 8);
      System.arraycopy(temp2, 0, result, 8, 8);   //Array.Copy(temp2, 0, result, 8, 8);
      return result;
  }

  public static byte[] GetDUKPTKey(byte[] ksn, byte[] ipek)
  {
//  	System.out.println("ksn===" + parseByte2HexStr(ksn));
      byte[] key;
      byte[] cnt;
      byte[] temp;
//  	byte shift;
      int shift;

      key = new byte[16];
//      Array.Copy(ipek, key, 16);
      System.arraycopy(ipek, 0, key, 0, 16);

      temp = new byte[8];
      cnt = new byte[3];
      cnt[0] = (byte)(ksn[7] & 0x1F);
      cnt[1] = ksn[8];
      cnt[2] = ksn[9];
//      Array.Copy(ksn, 2, temp, 0, 6);
      System.arraycopy(ksn, 2, temp, 0, 6);
      temp[5] &= 0xE0;

      shift = 0x10;
      while (shift > 0)
      {
          if ((cnt[0] & shift) > 0)
          {
//          	System.out.println("**********");
              temp[5] |= shift;
              NRKGP(key, temp);
          }
          shift >>= 1;
      }
      shift = 0x80;
      while (shift > 0)
      {
          if ((cnt[1] & shift) > 0)
          {
//          	System.out.println("&&&&&&&&&&");
              temp[6] |= shift;
              NRKGP(key, temp);
          }
          shift >>= 1;
      }
      shift = 0x80;
      while (shift > 0)
      {
          if ((cnt[2] & shift) > 0)
          {
//          	System.out.println("^^^^^^^^^^");
              temp[7] |= shift;
              NRKGP(key, temp);
          }
          shift >>= 1;
      }

      return key;
  }

  /// <summary>
  /// Non Reversible Key Generatino Procedure
  /// private function used by GetDUKPTKey
  /// </summary>
  private static void NRKGP(byte[] key, byte[] ksn)
  {

      byte[] temp, key_l, key_r, key_temp;
      int i;

      temp = new byte[8];
      key_l = new byte[8];
      key_r = new byte[8];
      key_temp = new byte[8];

//      Console.Write("");

//      Array.Copy(key, key_temp, 8);
      System.arraycopy(key, 0, key_temp, 0, 8);
      for (i = 0; i < 8; i++)
          temp[i] = (byte)(ksn[i] ^ key[8 + i]);
//      DES_Enc(temp, key_temp, out key_r);
      key_r = TriDesEncryption(key_temp,temp);
      for (i = 0; i < 8; i++)
          key_r[i] ^= key[8 + i];

      key_temp[0] ^= 0xC0;
      key_temp[1] ^= 0xC0;
      key_temp[2] ^= 0xC0;
      key_temp[3] ^= 0xC0;
      key[8] ^= 0xC0;
      key[9] ^= 0xC0;
      key[10] ^= 0xC0;
      key[11] ^= 0xC0;

      for (i = 0; i < 8; i++)
          temp[i] = (byte)(ksn[i] ^ key[8 + i]);
//      DES_Enc(temp, key_temp, out key_l);
      key_l = TriDesEncryption(key_temp,temp);
      for (i = 0; i < 8; i++)
          key[i] = (byte)(key_l[i] ^ key[8 + i]);
//      Array.Copy(key_r, 0, key, 8, 8);
      System.arraycopy(key_r, 0, key, 8, 8);
  }

  /// <summary>
  /// Get current Data Key variant
  /// Data Key variant is XOR DUKPT Key with 0000 0000 00FF 0000 0000 0000 00FF 0000
  /// </summary>
  /// <param name="ksn">Key serial number(KSN). A 10 bytes data. Which use to determine which BDK will be used and calculate IPEK. With different KSN, the DUKPT system will ensure different IPEK will be generated.
  /// Normally, the first 4 digit of KSN is used to determine which BDK is used. The last 21 bit is a counter which indicate the current key.</param>
  /// <param name="ipek">IPEK (16 byte).</param>
  /// <returns>Data Key variant (16 byte)</returns>
  public static byte[] GetDataKeyVariant(byte[] ksn, byte[] ipek)
  {
      byte[] key;

      key = GetDUKPTKey(ksn, ipek);
      key[5] ^= 0xFF;
      key[13] ^= 0xFF;

      return key;
  }

  /// <summary>
  /// Get current PIN Key variant
  /// PIN Key variant is XOR DUKPT Key with 0000 0000 0000 00FF 0000 0000 0000 00FF
  /// </summary>
  /// <param name="ksn">Key serial number(KSN). A 10 bytes data. Which use to determine which BDK will be used and calculate IPEK. With different KSN, the DUKPT system will ensure different IPEK will be generated.
  /// Normally, the first 4 digit of KSN is used to determine which BDK is used. The last 21 bit is a counter which indicate the current key.</param>
  /// <param name="ipek">IPEK (16 byte).</param>
  /// <returns>PIN Key variant (16 byte)</returns>
  public static byte[] GetPinKeyVariant(byte[] ksn, byte[] ipek)
  {
      byte[] key;

      key = GetDUKPTKey(ksn, ipek);
      key[7] ^= 0xFF;
      key[15] ^= 0xFF;

      return key;
  }

  public static byte[] GetDataKey(byte[] ksn, byte[] ipek)
  {
      byte[] temp1 = GetDataKeyVariant(ksn, ipek);
      byte[] temp2 = temp1;

      byte[] key = TriDesEncryption(temp2,temp1);

      return key;
  }

  // 3DES加密
  public static byte[] TriDesEncryption(byte[] byteKey, byte[] dec) {

      try {
          byte[] en_key = new byte[24];
          if (byteKey.length == 16) {
              System.arraycopy(byteKey, 0, en_key, 0, 16);
              System.arraycopy(byteKey, 0, en_key, 16, 8);
          } else if (byteKey.length == 8) {
              System.arraycopy(byteKey, 0, en_key, 0, 8);
              System.arraycopy(byteKey, 0, en_key, 8, 8);
              System.arraycopy(byteKey, 0, en_key, 16, 8);
          } else {
              en_key = byteKey;
          }
          SecretKeySpec key = new SecretKeySpec(en_key, "DESede");

          Cipher ecipher = Cipher.getInstance("DESede/ECB/NoPadding");
          ecipher.init(Cipher.ENCRYPT_MODE, key);

          // Encrypt
          byte[] en_b = ecipher.doFinal(dec);

          // String en_txt = parseByte2HexStr(en_b);
          // String en_txt =byte2hex(en_b);
          return en_b;
      } catch (Exception e) {
          e.printStackTrace();
      }
      return null;
  }

  // 3DES解密 CBC
  public static byte[] TriDesDecryptionCBC(byte[] byteKey, byte[] dec) {
      byte[] en_key = new byte[24];
      if (byteKey.length == 16) {
          System.arraycopy(byteKey, 0, en_key, 0, 16);
          System.arraycopy(byteKey, 0, en_key, 16, 8);
      } else if (byteKey.length == 8) {
          System.arraycopy(byteKey, 0, en_key, 0, 8);
          System.arraycopy(byteKey, 0, en_key, 8, 8);
          System.arraycopy(byteKey, 0, en_key, 16, 8);
      } else {
          en_key = byteKey;
      }

      try {
          Key deskey = null;
          byte[] keyiv=new byte[8];
          DESedeKeySpec spec = new DESedeKeySpec(en_key);
          SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
          deskey = keyfactory.generateSecret(spec);

          Cipher cipher = Cipher.getInstance("desede" + "/CBC/NoPadding");
          IvParameterSpec ips = new IvParameterSpec(keyiv);

          cipher.init(Cipher.DECRYPT_MODE, deskey, ips);

          byte[] de_b = cipher.doFinal(dec);

          return de_b;
      } catch (Exception e) {
          e.printStackTrace();
      }
      return null;
  }


  // 3DES解密
  public static byte[] TriDesDecryption(byte[] byteKey, byte[] dec) {
      // private String TriDesDecryption(String dnc_key, byte[] dec){
      // byte[] byteKey = parseHexStr2Byte(dnc_key);
      byte[] en_key = new byte[24];
      if (byteKey.length == 16) {
          System.arraycopy(byteKey, 0, en_key, 0, 16);
          System.arraycopy(byteKey, 0, en_key, 16, 8);
      } else if (byteKey.length == 8) {
          System.arraycopy(byteKey, 0, en_key, 0, 8);
          System.arraycopy(byteKey, 0, en_key, 8, 8);
          System.arraycopy(byteKey, 0, en_key, 16, 8);
      } else {
          en_key = byteKey;
      }
      SecretKey key = null;

      try {
          key = new SecretKeySpec(en_key, "DESede");
      } catch (Exception e) {
          e.printStackTrace();
          return null;
      }

      try {
          Cipher dcipher = Cipher.getInstance("DESede/ECB/NoPadding");
          dcipher.init(Cipher.DECRYPT_MODE, key);

          // byte[] dec = parseHexStr2Byte(en_data);

          // Decrypt
          byte[] de_b = dcipher.doFinal(dec);

          // String de_txt = parseByte2HexStr(removePadding(de_b));
          return de_b;
      } catch (Exception e) {
          e.printStackTrace();
      }
      return null;
  }

  // 十六进制字符串转字节数组
  public static byte[] parseHexStr2Byte(String hexStr) {
      if (hexStr.length() < 1)
          return null;
      byte[] result = new byte[hexStr.length() / 2];
      for (int i = 0; i < hexStr.length() / 2; i++) {
          int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
          int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                  16);
          result[i] = (byte) (high * 16 + low);
      }
      return result;
  }

  // 字节数组转十六进制字符串
  public static String parseByte2HexStr(byte buf[]) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < buf.length; i++) {
          String hex = Integer.toHexString(buf[i] & 0xFF);
          if (hex.length() == 1) {
              hex = '0' + hex;
          }
          sb.append(hex.toUpperCase());
      }
      return sb.toString();
  }

  // 数据补位
  public static String dataFill(String dataStr) {
      int len = dataStr.length();
      if (len%16 != 0) {
          dataStr += "80";
          len = dataStr.length();
      }
      while (len%16 != 0) {
          dataStr += "0";
          len ++;
          System.out.println(dataStr);
      }
      return dataStr;
  }
}
