package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.XXHash32;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.lang.util.BytesConvertor;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.XXHashDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "XXHash",
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
public class XXHash extends AbstractCodecOperator {

  protected XXHash32 xxHash32 = new XXHash32();

  public XXHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return XXHashDescriptor.COL_SUFFIX;
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
    xxHash32.reset();
    // HASH
    xxHash32.update(digest, 0, digest.length);
    if (isStr)
      return Hex.encodeHexString(
          BytesConvertor.long2Bytes(xxHash32.getValue()));
    return BytesConvertor.long2Bytes(xxHash32.getValue());
  }

}
