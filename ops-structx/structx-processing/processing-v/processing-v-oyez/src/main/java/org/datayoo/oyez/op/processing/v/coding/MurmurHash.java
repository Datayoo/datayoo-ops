package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.MurmurHash2;
import org.apache.commons.codec.digest.MurmurHash3;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.lang.util.BytesConvertor;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.MurmurHashDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "MurmurHash",
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
public class MurmurHash extends AbstractCodecOperator {

  protected MurmurHashDescriptor.MurmurAlgorithm murmurAlgorithm;

  public MurmurHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return MurmurHashDescriptor.COL_SUFFIX;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    murmurAlgorithm = MurmurHashDescriptor.MurmurAlgorithm.valueOf(
        parameters.getParameterValue(
            MurmurHashDescriptor.PARAM_MURMUR_ALGORITHM));
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
    String hash = null;
    if (murmurAlgorithm
        == MurmurHashDescriptor.MurmurAlgorithm.MurmurHash2_32) {
      return BytesConvertor.int2Bytes(
          MurmurHash2.hash32(digest, digest.length));
    } else if (murmurAlgorithm
        == MurmurHashDescriptor.MurmurAlgorithm.MurmurHash2_64) {
      return BytesConvertor.long2Bytes(
          MurmurHash2.hash64(digest, digest.length));
    } else if (murmurAlgorithm
        == MurmurHashDescriptor.MurmurAlgorithm.MurmurHash3_32) {
      return BytesConvertor.long2Bytes(MurmurHash3.hash32(digest));
    } else if (murmurAlgorithm
        == MurmurHashDescriptor.MurmurAlgorithm.MurmurHash3_32x86) {
      return BytesConvertor.int2Bytes(MurmurHash3.hash32x86(digest));
    } else if (murmurAlgorithm
        == MurmurHashDescriptor.MurmurAlgorithm.MurmurHash3_128) {
      return toBytes(MurmurHash3.hash128(digest));
    } else {
      return toBytes(MurmurHash3.hash128x64(digest));
    }
  }

}
