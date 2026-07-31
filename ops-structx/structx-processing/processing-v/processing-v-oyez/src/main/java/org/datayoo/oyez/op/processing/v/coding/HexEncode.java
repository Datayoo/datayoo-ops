package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.HexEncodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.io.InputStreamUtils;

@OpDefiner(name = "HexEncode",
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
public class HexEncode extends AbstractCodecOperator {

  public HexEncode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  protected Object codec(Object data) {
    byte[] bytes = InputStreamUtils.toBytes(data);
    return Hex.encodeHexString(bytes, false);
  }

  @Override
  protected String getColumnSuffix() {
    return HexEncodeDescriptor.COL_SUFFIX;
  }
}
