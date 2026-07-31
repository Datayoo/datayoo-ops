package org.datayoo.oyez.op.processing.v.coding;

import org.datayoo.base.lang.Hex;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.v.coding.HexEncodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "HexDecode",
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
public class HexDecode extends AbstractCodecOperator {

  public HexDecode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  protected Object codec(Object data) {
    if (data == null) {
      return null;
    }
    String txt = data.toString();
    return Hex.toBytes(txt);
  }

  @Override
  protected String getColumnSuffix() {
    return HexEncodeDescriptor.COL_SUFFIX;
  }

  @Override
  protected DataType getOutputDataType() {
    return DataTypeUtils.BINARY_TYPE;
  }
}
