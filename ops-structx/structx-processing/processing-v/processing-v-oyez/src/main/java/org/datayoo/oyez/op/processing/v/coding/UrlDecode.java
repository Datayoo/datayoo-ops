package org.datayoo.oyez.op.processing.v.coding;

import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.UrlDecodeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.net.URLDecoder;

@OpDefiner(name = "UrlDecode",
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
public class UrlDecode extends AbstractCodecOperator {

  public UrlDecode(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return UrlDecodeDescriptor.COL_SUFFIX;
  }

  @Override
  protected Object codec(Object data) throws Exception {
    return URLDecoder.decode((String) data, "utf-8");
  }
}
