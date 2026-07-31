package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.HmacHashDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import javax.crypto.Mac;

@OpDefiner(name = "HmacHash",
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
public class HmacHash extends AbstractCodecOperator {

  protected HmacAlgorithms hmacAlgorithms;

  public HmacHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return HmacHashDescriptor.COL_SUFFIX;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    hmacAlgorithms = HmacAlgorithms.valueOf(
        parameters.getParameterValue(HmacHashDescriptor.PARAM_HMAC_ALGORITHM));
  }

  protected Object codec(Object data) {
    byte[] digest = null;
    boolean isStr = true;
    if (data instanceof String) {
      digest = ((String) data).getBytes();
    } else {
      digest = (byte[]) data;
      isStr = false;
    }
    // HASH
    Mac mac = HmacUtils.getInitializedMac(hmacAlgorithms, digest);
    byte[] result = mac.doFinal(digest);
    if (isStr)
      return Hex.encodeHexString(result);
    return result;
  }

}
