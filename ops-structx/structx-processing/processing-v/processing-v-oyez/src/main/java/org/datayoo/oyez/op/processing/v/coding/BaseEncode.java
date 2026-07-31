package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Base64;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.BaseAlgorithm;
import org.datayoo.sengee.op.processing.v.coding.BaseEncodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.coder.Base62;

import java.nio.charset.StandardCharsets;

@OpDefiner(name = "BaseEncode",
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
public class BaseEncode extends AbstractCodecOperator {
  private static final String CODE_TABLE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  protected BaseAlgorithm baseAlgorithm;
  protected Base62 base62;

  public BaseEncode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    this.baseAlgorithm = BaseAlgorithm.valueOf(parameters.getParameterValue(
        BaseEncodeDescriptor.PARAM_BASE_ALGORITHM));
    this.base62 = new Base62(CODE_TABLE);
  }

  protected Object codec(Object data) {
    if (data instanceof String) {
      if (baseAlgorithm == BaseAlgorithm.BASE62) {
        return base62.encode(Long.valueOf((String) data));
      } else {
        return Base64.encodeBase64String(((String) data).getBytes());
      }
    } else {
      if (baseAlgorithm == BaseAlgorithm.BASE62) {
        return base62.encode(Long.valueOf((String) data))
            .getBytes(StandardCharsets.UTF_8);
      } else {
        return Base64.encodeBase64String((byte[]) data);
      }
    }
  }

  @Override
  protected String getColumnSuffix() {
    return BaseEncodeDescriptor.COL_SUFFIX;
  }
}
