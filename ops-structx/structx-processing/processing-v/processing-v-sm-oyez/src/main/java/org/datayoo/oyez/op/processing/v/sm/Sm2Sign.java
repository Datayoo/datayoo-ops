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
import org.datayoo.sengee.op.processing.v.sm.Sm2SignDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.security.KeyFactory;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

@OpDefiner(name = "Sm2Sign",
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
public class Sm2Sign extends AbstractCodecOperator {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  protected Signature signature;

  public Sm2Sign(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    String privateKeyHex = parameters.getParameterValue(
        Sm2SignDescriptor.PARAM_PRIVATE_KEY);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
      privateKeyHex = privateKeyHex.replaceAll("\\s+", "");
      byte[] keyBytes = Hex.decodeHex(privateKeyHex);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
      signature = Signature.getInstance("SM3withSM2", "BC");
      signature.initSign(keyFactory.generatePrivate(keySpec));
    } catch (Exception e) {
      throw new OperationRuntimeException(
          String.format("SM2 sign init failed: %s", e.getMessage()), e);
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
    // Signature is not thread-safe, create new instance per call
    // Clone the parameters by re-initializing
    // Since SM2Sign is stateful (sign mode only), we can reuse after reset
    // But to be safe, we update and sign per call
    // BouncyCastle's Signature.update() accumulates, so we need a fresh instance
    // Actually, Signature.sign() resets the state, so we can reuse
    signature.update(input);
    byte[] result = signature.sign();
    if (isStr) {
      return Hex.encodeHexString(result);
    }
    return result;
  }

  @Override
  protected String getColumnSuffix() {
    return Sm2SignDescriptor.COL_SUFFIX;
  }
}
