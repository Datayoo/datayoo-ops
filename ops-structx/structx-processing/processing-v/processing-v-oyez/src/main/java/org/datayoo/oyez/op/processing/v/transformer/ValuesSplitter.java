//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.datayoo.oyez.op.processing.v.transformer;

import org.apache.log4j.Level;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.colset.ColumnInfo;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.v.transformer.ValuesSplitterDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.LinkedList;
import java.util.List;

@OpDefiner(name = "ValuesSplitter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters = "",
    compoxes = {})
public class ValuesSplitter extends BaseProcessOperator {

  protected String separator;

  protected String[] columns;

  protected boolean hasVariableLength = false;

  //待切割最大长度
  protected int[] splittedLengths;

  //待切割的列
  protected ColumnInfo[] columnInfos;

  protected ColumnSetMetadata outputColumnSetMetadata = null;

  public ValuesSplitter(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = (PlRowSet) this.inputPort.read();
    if (columnInfos == null) {
      // 绑定列索引
      bindColumnInfo(rowSet.getColumnSetMetadata());
      // 计算分割长度
      if (hasVariableLength)
        calcSplittedLengths(rowSet.getRows());
    }
    ColumnSetMetadata columnSetMetadata = buildColumnSetMetadata(
        rowSet.getColumnSetMetadata());
    PlRowSet plRowSet = createRowSet(columnSetMetadata);
    plRowSet.getRows().addAll(
        splitData(rowSet.getRows(), columnSetMetadata.getColumns().size()));
    this.outputPort.write(plRowSet, this.inputPort.getWaterMark());
  }

  protected void bindColumnInfo(ColumnSetMetadata columnSetMetadata) {
    columnInfos = ProcessOperatorHelper.bindColumnInfos(columnSetMetadata,
        columns, null);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    this.separator = parameters.getParameterValue(
        ValuesSplitterDescriptor.PARAM_SEPARATOR);
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        ValuesSplitterDescriptor.PARAM_COLUMN_SET);
    columns = new String[columnSetParameter.getParameters().size()];
    splittedLengths = new int[columns.length];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      columns[i] = rowParameter.getParameterValue(
          ValuesSplitterDescriptor.PARAM_COLUMN_NAME);
      splittedLengths[i] = rowParameter.getParameterValueAsInt(
          ValuesSplitterDescriptor.PARAM_NEW_COLUMN_LENGTH, -1);
      if (splittedLengths[i] < 0)
        hasVariableLength = true;
      i++;
    }
  }

  @Override
  protected void buildFeatureStates() {
  }

  @Override
  protected void operatorInitialize() {
  }

  @Override
  protected void operatorDestroy() {
  }

  protected void calcSplittedLengths(List<Object[]> rows) {
    for (int i = 0; i < splittedLengths.length; i++) {
      if (splittedLengths[i] < 0) {
        int j = 0;
        for (Object[] objects : rows) {
          Object value =  objects[columnInfos[i].getColumnIndex()];
          if (null != value) {
            String stringValue = (String) value;
            j = Math.max(j, stringValue.split(separator).length);
          }
        }
        splittedLengths[i] = j;
      }
    }
  }

  protected ColumnSetMetadata buildColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    if (outputColumnSetMetadata == null) {
      outputColumnSetMetadata = new GeneralColumnSetMetadata(columnSetMetadata);
      for (int i = 0; i < columnInfos.length; i++) {
        ColumnMetadata columnMetadata = columnSetMetadata.getColumn(
            columnInfos[i].getColumnIndex());
        int newColumnLength = splittedLengths[i];
        for (int j = 1; j <= newColumnLength; j++) {
          ColumnMetadata newColumnMetadata = new GeneralColumnMetadata(
              columnMetadata.getName() + "_" + j);
          newColumnMetadata.setType(DataTypeUtils.STRING_TYPE);
          outputColumnSetMetadata.addColumn(newColumnMetadata);
        }
      }
    }
    return outputColumnSetMetadata;
  }

  protected List<Object[]> splitData(List<Object[]> rows, int columnSize) {
    List<Object[]> outRows = new LinkedList<>();
    for (Object[] row : rows) {
      Object[] outRow = new Object[columnSize];
      System.arraycopy(row, 0, outRow, 0, row.length);
      int offset = row.length;
      for (int i = 0; i < columnInfos.length; i++) {
        Object value = row[columnInfos[i].getColumnIndex()];
        if(null != value){
          String stringValue = (String) value;
          String[] valArr = stringValue.split(separator, splittedLengths[i]);
 /*       if (valArr.length > splittedLengths[i]) {
          this.getEngineContext().log(this.alias, Level.ERROR,
              String.format("'%s' split failed! un-matched splint count", value), null);
          System.arraycopy(valArr, 0, outRow, offset, splittedLengths[i]);
          continue;
        }*/
          System.arraycopy(valArr, 0, outRow, offset, valArr.length);
          offset += splittedLengths[i];
        }
      }
      outRows.add(outRow);
    }
    return outRows;
  }
}
