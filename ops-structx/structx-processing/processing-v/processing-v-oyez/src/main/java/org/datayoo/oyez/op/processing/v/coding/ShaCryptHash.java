package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Sha2Crypt;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.coding.ShaCryptHashDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "ShaCryptHash",
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
public class ShaCryptHash extends AbstractCodecOperator {

  protected ShaCryptHashDescriptor.CryptAlgorithm cryptAlgorithm;

  protected String salt;

  public ShaCryptHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return ShaCryptHashDescriptor.COL_SUFFIX;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    super.presetAttributes(groupParameter);
    cryptAlgorithm = ShaCryptHashDescriptor.CryptAlgorithm.valueOf(
        parameters.getParameterValue(
            ShaCryptHashDescriptor.PARAM_CRYPT_ALGORITHM));
    salt = parameters.getParameterValue(ShaCryptHashDescriptor.PARAM_SALT);
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
    String hash = hash(digest);
    // trim key
    String key = hash.substring(salt.length() + 1);
    if (isStr)
      return key;
    return Base64.decodeBase64(key);
  }

  private String hash(byte[] digest) {
    String hash = null;
    if (cryptAlgorithm == ShaCryptHashDescriptor.CryptAlgorithm.Sha2Crypt_256) {
      return Sha2Crypt.sha256Crypt(digest, salt);
    } else {
      return Sha2Crypt.sha512Crypt(digest, salt);
    }
  }

}
