package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.DesEncodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * DES加密
 *
 * @author he
 */
@OpDefiner(name = "DesEncode",
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
public class DesEncode extends AbstractCodecOperator {
  protected Cipher cipher;

  public DesEncode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    String keyValue = groupParameter.getParameterValue(
        DesEncodeDescriptor.PARAM_KEY);
    try {
      SecretKey secretKey = generateKey(keyValue);
      cipher = Cipher.getInstance("DES/ECB/PKCS5Padding ");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Object codec(Object data) throws Exception {
    if (data == null) {
      return null;
    }
    if (data instanceof String) {
      if (((String) data).isEmpty()) {
        return "";
      }
      return Hex.encodeHexString(cipher.doFinal(((String) data).getBytes()));
    } else {
      return cipher.doFinal((byte[]) data);
    }
  }

  @Override
  protected String getColumnSuffix() {
    return DesEncodeDescriptor.COL_SUFFIX;
  }

  public SecretKey generateKey(String keyValue) {
    byte[] keyBytes = new byte[8];
    System.arraycopy(Base64.getEncoder().encode(keyValue.getBytes()), 0,
        keyBytes, 0, 8);
    return new SecretKeySpec(keyBytes, "DES");
  }
}
