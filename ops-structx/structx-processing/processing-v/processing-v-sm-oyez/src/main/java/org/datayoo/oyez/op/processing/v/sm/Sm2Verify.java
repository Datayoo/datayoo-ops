package org.datayoo.oyez.op.processing.v.sm;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.GeneralColumnMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.v.coding.AbstractCodecOperator;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.v.coding.AbstractCodecDescriptor;
import org.datayoo.sengee.op.processing.v.sm.Sm2VerifyDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.security.KeyFactory;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

@OpDefiner(name = "Sm2Verify",
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
public class Sm2Verify extends AbstractCodecOperator {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  protected Signature signature;
  protected String dataColumn;
  protected String signColumn;
  protected int dataColumnIndex;
  protected int signColumnIndex;
  protected int outputColumnIndex;

  public Sm2Verify(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    workingMode = parameters.getParameterValue(
        AbstractCodecDescriptor.PARAM_WORKING_MODE,
        AbstractCodecDescriptor.WM_OVERWITE);
    if (workingMode.equals(AbstractCodecDescriptor.WM_ADD_COLUMN))
      addedColumn = true;
    dataColumn = parameters.getParameterValue(
        Sm2VerifyDescriptor.PARAM_DATA_COLUMN);
    signColumn = parameters.getParameterValue(
        Sm2VerifyDescriptor.PARAM_SIGN_COLUMN);
    columns = new String[] { signColumn };
    String publicKeyHex = parameters.getParameterValue(
        Sm2VerifyDescriptor.PARAM_PUBLIC_KEY);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
      publicKeyHex = publicKeyHex.replaceAll("\\s+", "");
      byte[] keyBytes = Hex.decodeHex(publicKeyHex);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      signature = Signature.getInstance("SM3withSM2", "BC");
      signature.initVerify(keyFactory.generatePublic(keySpec));
    } catch (Exception e) {
      throw new OperationRuntimeException(
          String.format("SM2 verify init failed: %s", e.getMessage()), e);
    }
  }

  @Override
  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    dataColumnIndex = columnSetMetadata.getColumnIndex(dataColumn);
    signColumnIndex = columnSetMetadata.getColumnIndex(signColumn);
    if (addedColumn) {
      outputColumnIndex = columnSetMetadata.getColumns().size();
    }
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    if (workingMode.equals(AbstractCodecDescriptor.WM_OVERWITE)) {
      return new GeneralColumnSetMetadata(columnSetMetadata);
    } else {
      ColumnSetMetadata csm = new GeneralColumnSetMetadata(columnSetMetadata);
      String newColumn = signColumn + "_" + Sm2VerifyDescriptor.COL_SUFFIX;
      ColumnMetadata cm = new GeneralColumnMetadata(newColumn);
      csm.addColumn(cm);
      return csm;
    }
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    Object[] out = objects;
    if (addedColumn) {
      out = new Object[objects.length + 1];
      System.arraycopy(objects, 0, out, 0, objects.length);
    }
    Object dataObj = objects[dataColumnIndex];
    Object signObj = objects[signColumnIndex];
    Boolean valid = null;
    if (dataObj != null && signObj != null) {
      try {
        byte[] dataBytes;
        if (dataObj instanceof String) {
          dataBytes = ((String) dataObj).getBytes();
        } else {
          dataBytes = (byte[]) dataObj;
        }
        byte[] signBytes;
        if (signObj instanceof String) {
          signBytes = Hex.decodeHex(((String) signObj).toCharArray());
        } else {
          signBytes = (byte[]) signObj;
        }
        signature.update(dataBytes);
        valid = signature.verify(signBytes);
      } catch (Exception e) {
        throw new OperationRuntimeException(
            String.format("SM2 verify failed for data column '%s': %s",
                dataColumn, e.getMessage()), e);
      }
    }
    if (addedColumn) {
      out[outputColumnIndex] = valid;
    } else {
      out[signColumnIndex] = valid;
    }
    return out;
  }

  @Override
  protected Object codec(Object data) throws Exception {
    return null;
  }

  @Override
  protected String getColumnSuffix() {
    return Sm2VerifyDescriptor.COL_SUFFIX;
  }
}
