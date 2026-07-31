package org.datayoo.oyez.op.processing.v.coding;

import org.apache.commons.codec.binary.Hex;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.lang.util.BytesConvertor;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.v.coding.AbstractCodecDescriptor;
import org.datayoo.sengee.op.processing.v.coding.AesDecodeDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2023/4/11 14:29
 */
public abstract class AbstractCodecOperator extends BaseProcessOperator {

  protected String workingMode;

  protected String[] columns;

  protected int[] columnIndexes;

  protected int[] outputColumnIndexes;

  protected boolean addedColumn = false;

  public AbstractCodecOperator(FlowNodeMetadata operatorMetadata,
      FlowNode parent, EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return AbstractCodecDescriptor.createCodecOutputColumnSetMetadata(
        columnSetMetadata, workingMode, columns, getColumnSuffix(),
        getOutputDataType());
  }

  protected abstract String getColumnSuffix();

  protected DataType getOutputDataType() {
    return DataTypeUtils.STRING_TYPE;
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
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      columns[i] = rowParameter.getParameterValue(
          AesDecodeDescriptor.PARAM_COLUMN_NAME);
      i++;
    }
  }

  @Override
  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    if (columnIndexes == null) {
      columnIndexes = new int[columns.length];
      outputColumnIndexes = new int[columns.length];
      int i = 0;
      for (String column : columns) {
        int index = columnSetMetadata.getColumnIndex(column);
        if (addedColumn) {
          outputColumnIndexes[i] = columnSetMetadata.getColumns().size() + i;
        }
        columnIndexes[i++] = index;
      }
    }
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    Object[] out = objects;
    if (addedColumn) {
      out = new Object[objects.length + columnIndexes.length];
      System.arraycopy(objects, 0, out, 0, objects.length);
    }
    int j = 0;
    for (int index : columnIndexes) {
      Object data = objects[index];
      try {
        if (addedColumn) {
          if (data != null) {
            out[outputColumnIndexes[j]] = codec(data);
          } else {
            out[outputColumnIndexes[j]] = null;
          }
        } else {
          if (data != null)
            out[index] = codec(data);
          else
            out[index] = null;
        }
      } catch (Throwable t) {
        throw new OperationRuntimeException(
            String.format("[%s]The value '%s' of column '%s' codec failed!",
                this.alias, (String) objects[index], columns[j]), t);
      }
      j++;
    }
    return out;
  }

  protected abstract Object codec(Object data) throws Exception;

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }

  protected String toString(long[] hashes) {
    StringBuilder sbud = new StringBuilder();
    for (long hash : hashes) {
      sbud.append(Hex.encodeHexString(BytesConvertor.long2Bytes(hash)));
    }
    return sbud.toString();
  }

  protected byte[] toBytes(long[] hashes) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (long hash : hashes) {
      try {
        baos.write(BytesConvertor.long2Bytes(hash));
      } catch (IOException e) {
      }
    }
    return baos.toByteArray();
  }
}
