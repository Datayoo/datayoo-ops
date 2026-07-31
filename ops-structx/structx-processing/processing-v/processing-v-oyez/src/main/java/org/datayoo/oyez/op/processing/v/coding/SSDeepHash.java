package org.datayoo.oyez.op.processing.v.coding;

import org.datayoo.algox.hash.spamsum.SpamSum;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.SSDeepHashDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "SSDeepHash",
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
public class SSDeepHash extends AbstractCodecOperator {

  protected SpamSum spamSum = new SpamSum();

  public SSDeepHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return SSDeepHashDescriptor.COL_SUFFIX;
  }

  protected Object codec(Object data) {
    if (data == null)
      return null;
    if (data instanceof byte[]) {
      return spamSum.hash((byte[]) data);
    } else {
      return spamSum.hashString(data.toString());
    }
  }

}
