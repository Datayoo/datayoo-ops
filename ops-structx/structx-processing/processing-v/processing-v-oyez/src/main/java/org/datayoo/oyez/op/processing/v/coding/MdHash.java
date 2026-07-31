package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.Operand;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.v.coding.AbstractCodecDescriptor;
import org.datayoo.sengee.op.processing.v.coding.AesDecodeDescriptor;
import org.datayoo.sengee.op.processing.v.coding.MdHashDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.security.MessageDigest;

@OpDefiner(name = "MdHash",
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
public class MdHash extends AbstractCodecOperator {

  protected MdHashDescriptor.MessageDigestAlgorithm messageDigestAlgorithm;

  protected MessageDigest messageDigest;

  protected Operand[] salts;

  protected boolean bind = false;

  public MdHash(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected String getColumnSuffix() {
    return MdHashDescriptor.COL_SUFFIX;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    workingMode = parameters.getParameterValue(
        AbstractCodecDescriptor.PARAM_WORKING_MODE,
        AbstractCodecDescriptor.WM_OVERWITE);
    if (workingMode.equals(AbstractCodecDescriptor.WM_ADD_COLUMN))
      addedColumn = true;
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        AesDecodeDescriptor.PARAM_COLUMN_SET);
    columns = new String[columnSetParameter.getParameters().size()];
    salts = new Operand[columnSetParameter.getParameters().size()];
    int i = 0;
    String salt = "";
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      columns[i] = rowParameter.getParameterValue(
          AesDecodeDescriptor.PARAM_COLUMN_NAME);
      try {
        salt = rowParameter.getParameterValue(MdHashDescriptor.PARAM_SALT, "");
        if (salt.isEmpty()) {
          continue;
        }
        salts[i] = ExpressionFactory.createExpression(
            rowParameter.getParameterValue(MdHashDescriptor.PARAM_SALT));
      } catch (MoqlException e) {
        throw new RuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias, salt));
      }
      i++;
    }
    messageDigestAlgorithm = MdHashDescriptor.MessageDigestAlgorithm.valueOf(
        parameters.getParameterValue(MdHashDescriptor.PARAM_MD_ALGORITHM));
  }

  @Override
  protected void operatorInitialize() {
    messageDigest = DigestUtils.getDigest(
        messageDigestAlgorithm.getAlgorName());
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    Object[] out = objects;
    if (!bind) {
      for (Operand operand : salts) {
        if (operand != null) {
          String[] columns = new String[columnSetMetadata.getColumns().size()];
          for (int j = 0; j < columnSetMetadata.getColumns().size(); j++) {
            columns[j] = columnSetMetadata.getColumn(j).getName();
          }
          operand.bind(columns);
          bind = true;
        }
      }
    }
    if (addedColumn) {
      out = new Object[objects.length + columnIndexes.length];
      System.arraycopy(objects, 0, out, 0, objects.length);
    }
    int j = 0;
    for (int index : columnIndexes) {
      Object data = objects[index];
      String salt = "";
      try {
        if (salts[j] != null) {
          salt = (String) salts[j].operate(objects);
          if (salt == null) {
            salt = "";
          }
        }
        if (addedColumn) {
          out[outputColumnIndexes[j]] = codec(data, salt);
        } else {
          out[index] = codec(data, salt);
        }
      } catch (Throwable t) {
        throw new OperationRuntimeException(
            String.format("The value '%s' of column '%s' codec failed!",
                (String) objects[index], columns[j]), t);
      }
      j++;
    }
    return out;
  }

  @Override
  protected Object codec(Object data) throws Exception {
    return null;
  }

  protected Object codec(Object data, String salt) {
    byte[] digest = null;
    if (data == null) {
      return null;
    }
    boolean isStr = true;
    if (data instanceof String) {
      if (!salt.isEmpty()) {
        digest = (String.format("%s%s", (String) data, salt)).getBytes();
      } else {
        digest = ((String) data).getBytes();
      }
    } else {
      digest = (byte[]) data;
      isStr = false;
    }
    // HASH
    byte[] result = messageDigest.digest(digest);
    if (isStr)
      return Hex.encodeHexString(result);
    return result;
  }

}
