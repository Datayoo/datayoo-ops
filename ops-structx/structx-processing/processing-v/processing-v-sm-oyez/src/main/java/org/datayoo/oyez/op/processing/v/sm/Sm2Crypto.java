package org.datayoo.oyez.op.processing.v.sm;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.v.coding.AbstractCodecOperator;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.v.sm.Sm2CryptoDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@OpDefiner(name = "Sm2Crypto",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class Sm2Crypto extends AbstractCodecOperator {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  protected boolean encrypt;
  protected Cipher cipher;

  public Sm2Crypto(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    String mode = parameters.getParameterValue(Sm2CryptoDescriptor.PARAM_MODE,
        Sm2CryptoDescriptor.MODE_ENCRYPT);
    encrypt = Sm2CryptoDescriptor.MODE_ENCRYPT.equals(mode);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
      cipher = Cipher.getInstance("SM2", "BC");
      if (encrypt) {
        String publicKeyHex = parameters.getParameterValue(
            Sm2CryptoDescriptor.PARAM_PUBLIC_KEY);
        publicKeyHex = stripWhitespace(publicKeyHex);
        byte[] keyBytes = Hex.decodeHex(publicKeyHex);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(keySpec));
      } else {
        String privateKeyHex = parameters.getParameterValue(
            Sm2CryptoDescriptor.PARAM_PRIVATE_KEY);
        privateKeyHex = stripWhitespace(privateKeyHex);
        byte[] keyBytes = Hex.decodeHex(privateKeyHex);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpec));
      }
    } catch (Exception e) {
      throw new OperationRuntimeException(
          String.format("SM2 cipher init failed: %s", e.getMessage()), e);
    }
  }

  @Override
  protected Object codec(Object data) throws Exception {
    if (data == null) {
      return null;
    }
    byte[] input;
    boolean isStr = data instanceof String;
    if (isStr) {
      if (((String) data).isEmpty()) {
        return "";
      }
      if (encrypt) {
        input = ((String) data).getBytes();
      } else {
        input = Hex.decodeHex(((String) data).toCharArray());
      }
    } else {
      input = (byte[]) data;
    }
    byte[] result = cipher.doFinal(input);
    if (isStr) {
      if (encrypt) {
        return Hex.encodeHexString(result);
      } else {
        return new String(result);
      }
    }
    return result;
  }

  @Override
  protected String getColumnSuffix() {
    return Sm2CryptoDescriptor.COL_SUFFIX;
  }

  private String stripWhitespace(String s) {
    return s.replaceAll("\\s+", "");
  }
}
