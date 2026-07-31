package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Base64;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.BaseAlgorithm;
import org.datayoo.sengee.op.processing.v.coding.BaseDecodeDescriptor;
import org.datayoo.sengee.op.processing.v.coding.BaseEncodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.coder.Base62;

@OpDefiner(name = "BaseDecode",
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
public class BaseDecode extends AbstractCodecOperator {

  protected BaseAlgorithm baseAlgorithm;

  protected Base62 base62;

  protected String outPutMode;

  private static final String CODE_TABLE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public BaseDecode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    this.baseAlgorithm = BaseAlgorithm.valueOf(parameters.getParameterValue(
        BaseDecodeDescriptor.PARAM_BASE_ALGORITHM));
    this.base62 = new Base62(CODE_TABLE);
    this.outPutMode = groupParameter.getParameterValue(BaseDecodeDescriptor.PARAM_OUTPUT_MODE);
  }

  @Override
  protected DataType getOutputDataType() {
    if(outPutMode.equals("string")){
      return DataTypeUtils.STRING_TYPE;
    }else {
      return DataTypeUtils.BINARY_TYPE;
    }
  }

  @Override
  protected String getColumnSuffix() {
    return BaseEncodeDescriptor.COL_SUFFIX;
  }

  protected Object codec(Object param) {
    if (param instanceof String) {
      if (baseAlgorithm == BaseAlgorithm.BASE62) {
        return base62.decode(String.valueOf(param));
      } else {
        if(outPutMode.equals("string")){
          return new String(Base64.decodeBase64(((String) param).getBytes()));
        }else {
          return Base64.decodeBase64(((String) param).getBytes());
        }
      }
    } else {
      if (baseAlgorithm == BaseAlgorithm.BASE62) {
        return base62.decode(new String((byte[]) param));
      } else {
        if(outPutMode.equals("string")){
          return new String(Base64.decodeBase64((byte[]) param));
        }else {
          return Base64.decodeBase64((byte[]) param);
        }
      }
    }
  }
}
