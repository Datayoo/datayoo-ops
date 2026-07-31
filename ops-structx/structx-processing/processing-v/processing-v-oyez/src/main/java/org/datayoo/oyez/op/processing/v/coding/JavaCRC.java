package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.codec.digest.PureJavaCrc32C;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.lang.util.BytesConvertor;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.JavaCRCDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "JavaCRC",
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
public class JavaCRC extends AbstractCodecOperator {

  protected JavaCRCDescriptor.JavaCRCAlgorithm javaCRCAlgorithm;

  protected PureJavaCrc32 pureJavaCrc32;

  protected PureJavaCrc32C pureJavaCrc32C;

  public JavaCRC(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return JavaCRCDescriptor.COL_SUFFIX;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    javaCRCAlgorithm = JavaCRCDescriptor.JavaCRCAlgorithm.valueOf(
        parameters.getParameterValue(JavaCRCDescriptor.PARAM_CRC_ALGORITHM));
    if (javaCRCAlgorithm == JavaCRCDescriptor.JavaCRCAlgorithm.PureJavaCrc32) {
      pureJavaCrc32 = new PureJavaCrc32();
    } else {
      pureJavaCrc32C = new PureJavaCrc32C();
    }
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
    byte[] result = hash(digest);
    if (isStr)
      return Hex.encodeHexString(result);
    return result;
  }

  private byte[] hash(byte[] digest) {
    if (javaCRCAlgorithm == JavaCRCDescriptor.JavaCRCAlgorithm.PureJavaCrc32) {
      pureJavaCrc32.reset();
      pureJavaCrc32.update(digest, 0, digest.length);
      return BytesConvertor.long2Bytes(pureJavaCrc32.getValue());
    } else {
      pureJavaCrc32C.reset();
      pureJavaCrc32C.update(digest, 0, digest.length);
      return BytesConvertor.long2Bytes(pureJavaCrc32C.getValue());
    }
  }

}
