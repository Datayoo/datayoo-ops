package org.datayoo.oyez.op.processing.v.coding;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.v.coding.AesDecodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.encrypt.AesSecurity;

import java.security.GeneralSecurityException;

/**
 * Aes解码
 *
 * @author he
 */
@OpDefiner(name = "AesDecode",
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
public class AesDecode extends AbstractCodecOperator {
  protected String key;

  protected AesSecurity aesSecurity;

  public AesDecode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    this.key = parameters.getParameterValue(AesDecodeDescriptor.PARAM_KEY);
    try {
      this.aesSecurity = new AesSecurity(key);
    } catch (GeneralSecurityException e) {
      throw new OperationRuntimeException(
          String.format("aesKey cann't be empty!"));
    }
  }

  @Override
  protected Object codec(Object data) throws GeneralSecurityException {
    if (data instanceof String) {
      return new String(aesSecurity.decrypt((String) data));
    } else {
      return aesSecurity.nativeDecrypt((byte[]) data);
    }
  }

  @Override
  protected String getColumnSuffix() {
    return AesDecodeDescriptor.COL_SUFFIX;
  }
}
