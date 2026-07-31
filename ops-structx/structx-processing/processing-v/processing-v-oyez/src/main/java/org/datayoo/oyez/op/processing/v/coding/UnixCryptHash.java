package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.digest.UnixCrypt;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.UnixCryptHashDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "UnixCryptHash",
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
public class UnixCryptHash extends AbstractCodecOperator {

  protected String key;

  public UnixCryptHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return UnixCryptHashDescriptor.COL_SUFFIX;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    key = parameters.getParameterValue(UnixCryptHashDescriptor.PARAM_KEY);
  }

  protected Object codec(Object data) {
    byte[] digest = null;
    if (data instanceof String) {
      digest = ((String) data).getBytes();
      return hash(digest);
    } else {
      digest = (byte[]) data;
      String hash = hash(digest);
      if (hash != null)
        return hash.getBytes();
      return null;
    }
  }

  private String hash(byte[] digest) {
    String hash = null;
    return UnixCrypt.crypt(digest, key);
  }

}
