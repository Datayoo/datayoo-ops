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
import org.datayoo.sengee.op.processing.v.sm.Sm4CryptoDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

@OpDefiner(name = "Sm4Crypto",
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
public class Sm4Crypto extends AbstractCodecOperator {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  protected boolean encrypt;
  protected Cipher cipher;

  public Sm4Crypto(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    String mode = parameters.getParameterValue(Sm4CryptoDescriptor.PARAM_MODE,
        Sm4CryptoDescriptor.MODE_ENCRYPT);
    encrypt = Sm4CryptoDescriptor.MODE_ENCRYPT.equals(mode);
    String key = parameters.getParameterValue(Sm4CryptoDescriptor.PARAM_KEY);
    try {
      byte[] keyBytes = fixKeyLength(key.getBytes(), 16);
      SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
      cipher = Cipher.getInstance("SM4/ECB/PKCS7Padding", "BC");
      cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec);
    } catch (Exception e) {
      throw new OperationRuntimeException(
          String.format("SM4 cipher init failed: %s", e.getMessage()), e);
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
      input = ((String) data).getBytes();
    } else {
      input = (byte[]) data;
    }
    byte[] result = cipher.doFinal(input);
    if (isStr) {
      return Hex.encodeHexString(result);
    }
    return result;
  }

  @Override
  protected String getColumnSuffix() {
    return Sm4CryptoDescriptor.COL_SUFFIX;
  }

  private byte[] fixKeyLength(byte[] keyBytes, int targetLen) {
    if (keyBytes.length == targetLen) {
      return keyBytes;
    }
    byte[] fixed = new byte[targetLen];
    System.arraycopy(keyBytes, 0, fixed, 0,
        Math.min(keyBytes.length, targetLen));
    return fixed;
  }
}
